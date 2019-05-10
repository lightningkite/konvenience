package com.lightningkite.konvenience.gradle

import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.bundling.Zip
import org.gradle.jvm.application.tasks.CreateStartScripts
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmTargetPreset
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinOnlyTarget
import java.io.File

fun Project.javaApp(forTarget: KTarget = KTarget.jvm, mainClassName: String) {
    //${target}Run
    //${target}FatJar
    //${target}Distribution
    //${target}DistributionZip
    //${target}Upload

    /*
    https://docs.gradle.org/current/userguide/application_plugin.html

    https://github.com/gradle/gradle/blob/master/subprojects/plugins/src/main/java/org/gradle/api/plugins/ApplicationPlugin.java
    custom CreateStartScripts task, depending on ${forTarget}Jar

    I'd really like to be able to upload to a server - how might that be possible?
    For a server, it needs to upload it via SSH and restart a task
    Actually, it probably needs to do even more thanks to multiple servers - AWS integration of some kind?

    For a downloadable app, it should simply be pushed up to the CDN -
    https://github.com/classmethod/gradle-aws-plugin
    */
    val extension: KotlinMultiplatformExtension = this.extensions.getByType(KotlinMultiplatformExtension::class.java)
    val target = extension.targets.findByName(forTarget.name) as KotlinOnlyTarget<KotlinJvmCompilation>

    fun getDependencies(): List<File> {
        val dependencies = ArrayList<File>()
        try {
            for (configName in target.compilations.findByName("main")!!.relatedConfigurationNames) {
                try {
                    val config = project.configurations.getByName(configName)
                    for (file in config) {
                        dependencies.add(file)
                    }
                } catch (e: Throwable) {
                    /*squish*/
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return dependencies
    }

    val jarTask = tasks.findByName("${forTarget.name}Jar")!!

    val runTask = tasks.create("${forTarget.name}Run", JavaExec::class.java) { task ->
        task.dependsOn(jarTask)
        task.description = "Runs the target ${forTarget.name} as a JVM application"
        task.group = TaskGroups.RUN

        afterEvaluate {
            println("Using java ${task.executable}")
            task.classpath = jarTask.outputs.files.filter { it.extension == "jar" } + files(getDependencies())
        }
        task.main = mainClassName
    }

    val startScriptsTask = tasks.create("${forTarget.name}StartScripts", CreateStartScripts::class.java) { task ->
        task.group = TaskGroups.INSTALL

        task.outputDir = file("build/${forTarget.name}/startScripts")
        afterEvaluate {
            task.classpath = jarTask.outputs.files.filter { it.extension == "jar" } + files(getDependencies())
        }
        task.mainClassName = mainClassName
        task.applicationName = name
    }

    val installTask = tasks.create("${forTarget.name}Install", Copy::class.java) { task ->
        task.group = forTarget.name
        task.group = TaskGroups.INSTALL

        task.dependsOn(jarTask)
        task.dependsOn(startScriptsTask)
        task.destinationDir = file("build/${forTarget.name}/install")

        afterEvaluate {
            task.into("lib") {
                it.from(files(getDependencies()))
                it.from(jarTask.outputs.files.filter { it.extension == "jar" })
            }
        }
        task.into("bin") {
            it.from(startScriptsTask.outputDir)
        }
    }

    val zipTask = tasks.create("${forTarget.name}InstallZip", Zip::class.java) { task ->
        task.group = forTarget.name
        task.group = TaskGroups.DEPLOY

        task.dependsOn(installTask)
        task.from("build/${forTarget.name}/install")
        task.into("build/${forTarget.name}/install.zip")
    }
}