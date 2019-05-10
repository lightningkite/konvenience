package com.lightningkite.konvenience.gradle

import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec
import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJsCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinOnlyTarget
import java.io.File
import java.util.jar.JarFile

fun Project.jsApp(forTarget: KTarget) {
    //${target}DebugRun
    //${target}ReleaseRun
    //${target}AsResourceForServer

    /*
    POTENTIAL SOLUTIONS

    - kotlinFrontend plugin now works with multiplatform!
        - https://github.com/Kotlin/kotlin-frontend-plugin/blob/master/examples/new-mpp/build.gradle

    - Deploy could use [https://github.com/classmethod/gradle-aws-plugin] to distribute the new version

    STEPS:

    If 'package.json' isn't present, fill in a default
    Copy .js files to 'build/target'
    Copy other web files to 'build/target'
    Run 'npm run build'

    */
    val extension: KotlinMultiplatformExtension = this.extensions.getByType(KotlinMultiplatformExtension::class.java)
    @Suppress("UNCHECKED_CAST") val target = extension.targets.findByName(forTarget.name) as KotlinOnlyTarget<KotlinJsCompilation>

    target.compilations.getByName("main").compileKotlinTask.kotlinOptions.main = "call"

    fun getDependencies(): FileCollection {
        val dependencies = ArrayList<File>()
        try {
            for (configName in target.compilations.findByName("main")!!.relatedConfigurationNames) {
                try {
                    val config = project.configurations.getByName(configName)
                    for (file in config) {
                        dependencies.add(file)
                    }
                    println("Successfully read config ${configName}")
                } catch (e: Throwable) {
                    /*squish*/
                    println("Failed read config ${configName}")
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return files(dependencies)
//        return project.configurations.getByName("${target.name}Default")
    }

    fun File.jarFileToJsFiles(): FileCollection {
        return if (exists()) {
            zipTree(this).filter { it.extension == "js" || it.extension == "map" }
        } else {
            files()
        }
    }

    val jarTask = tasks.findByName("${forTarget.name}Jar")!!
    val dceTask = tasks.findByName("runDce${forTarget.name.capitalize()}Kotlin")

    val npmExecName = if (OperatingSystem.current().isWindows) "npm.cmd" else "npm"

    val initTask = tasks.create("${forTarget.name}InitializeProject") { task ->
        task.group = "build"
        val file = file("build/${forTarget.name}/n/package.json")
        task.outputs.file(file)

        task.doLast {
            file.parentFile.mkdirs()
            if (file.exists()) {
                return@doLast
            }
            println("Writing package.json")
            file.writeText("""
                {
                    "name": "$name",
                    "version": "$version",
                    "description": "",
                    "main": "$name.js",
                    "scripts": {
                        "build": "webpack",
                        "test": "echo \"Error: no test specified\" && exit 1"
                    },
                    "keywords": [],
                    "author": "",
                    "license": "None",
                    "devDependencies": {
                      "webpack": "^4.20.2",
                      "webpack-cli": "^3.1.2"
                    }
                }
            """.trimIndent())
        }
    }
    val npmInstallTask = tasks.create("${forTarget.name}NpmInstall", Exec::class.java) { task ->
        task.group = "build"
        task.dependsOn(initTask)

        task.workingDir = file("build/${forTarget.name}/n")
        task.executable = npmExecName
        task.args = listOf("install")
    }

    val copyJsTask = tasks.create("${forTarget.name}CopyJs") { task ->
        //, Copy::class.java) { task ->
        task.group = "build"
        if (dceTask != null) {
            task.dependsOn(dceTask)
            task.doLast {
                copy { c ->
                    c.into("build/${forTarget.name}/n/src")
                    c.from(dceTask.outputs.files)
                }
            }
        } else {
            task.dependsOn(jarTask)
            task.doLast {
                copy { c ->
                    c.into("build/${forTarget.name}/n/src")
                    jarTask.outputs.files
                            .filter { it.extension == "jar" }
                            .flatMap { it.jarFileToJsFiles() }
                            .forEach {
                                c.from(it)
                            }
                    c.from(getDependencies().flatMap { it.jarFileToJsFiles() })
                }
            }
        }
    }

    val indexJsTask = tasks.create("${forTarget.name}IndexJs") { task ->
        task.dependsOn(copyJsTask)
        val outputFile = file("build/${forTarget.name}/n/src/index.js")
        task.doLast {
            val directJsDependencies = file("build/${forTarget.name}/n/src").listFiles()
                    .asSequence()
                    .map { it.name.substringBefore('.') }
                    .filter { it != "index" }
                    .distinct()
            outputFile.writeText(directJsDependencies.joinToString("\n") { "import ${it.replace('-', '_')} from '$it'" })
        }
    }

    val configureWebpackTask = tasks.create("${forTarget.name}ConfigureWebpack") { task ->
        task.group = "build"
        task.dependsOn(copyJsTask)

        val file = file("build/${forTarget.name}/n/webpack.config.js")
        task.outputs.file(file)

        val startIndicator = "/*konvenience alias start*/"
        val endIndicator = "/*konvenience alias end*/"

        task.doLast {

            val directJsDependencies = file("build/${forTarget.name}/n/src").listFiles()
                    .asSequence()
                    .map { it.name.substringBefore('.') }
                    .filter { it != "index" }
                    .distinct()

            file.parentFile.mkdirs()

            val existing = file
                    .takeIf { it.exists() }
                    ?.readText()
                    ?.takeIf { it.contains(startIndicator) && it.contains(endIndicator) }
                    ?: """
                |const path = require('path');
                |
                |module.exports = {
                |    resolve: {
                |        alias: {
                |            $startIndicator
                |            $endIndicator
                |        }
                |    }
                |};
            """.trimMargin()

            var isInKonv = false
            var tabbing: String = ""
            val newText = existing.lineSequence()
                    .filter {
                        //Filter all between start and end indicators EXCEPT for the end indicator
                        if (it.trim().startsWith(startIndicator)) {
                            isInKonv = true
                        }
                        if (it.trim().startsWith(endIndicator)) {
                            isInKonv = false
                            tabbing = endIndicator.substringBefore(endIndicator)
                        }
                        !isInKonv
                    }
                    .map { line ->
                        if (line.trim().startsWith(endIndicator)) {
                            "$tabbing$startIndicator\n" + directJsDependencies.joinToString("\n") {
                                "$tabbing'$it': path.resolve(__dirname, 'src/$it.js'),"
                            } + "\n$tabbing$endIndicator\n"
                        } else line
                    }
                    .joinToString("\n")

            file.writeText(newText)
        }
    }

    val copyOtherTask = tasks.create("${forTarget.name}CopyOther", Copy::class.java) { task ->
        task.group = "build"
        task.dependsOn(initTask)

        task.destinationDir = file("build/${forTarget.name}/n/dist")
        task.from("src/${forTarget.name}Main/web")
    }
//    val runTask = tasks.create("${forTarget.name}Run", Exec::class.java) { task ->
//        task.group = TaskGroups.RUN
//        task.dependsOn(indexJsTask)
//        task.dependsOn(configureWebpackTask)
//        task.dependsOn(npmInstallTask)
//        task.dependsOn(copyOtherTask)
//
//        task.workingDir = file("build/${forTarget.name}/n")
//        task.executable = npmExecName
//        task.args = listOf("run", "start")
//    }
    val installTask = tasks.create("${forTarget.name}Install", Exec::class.java) { task ->
        task.group = TaskGroups.INSTALL
        task.dependsOn(indexJsTask)
        task.dependsOn(configureWebpackTask)
        task.dependsOn(npmInstallTask)
        task.dependsOn(copyOtherTask)

        task.workingDir = file("build/${forTarget.name}/n")
        task.executable = npmExecName
        task.args = listOf("run", "build")
    }


}