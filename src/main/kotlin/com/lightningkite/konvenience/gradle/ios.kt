package com.lightningkite.konvenience.gradle

import org.gradle.api.Project
import org.gradle.api.Task
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import java.io.File
import java.util.concurrent.TimeUnit

fun Project.iosApp(
        frameworkName: String = project.name.replace("-", "_")
) {
    val extension: KotlinMultiplatformExtension = this.extensions.getByType(KotlinMultiplatformExtension::class.java)
    KTarget.allLogic.filter { KTargetPredicates.isIos(it) }.forEach { target ->
        if (target.worksOnMyPlatform()) {
            extension.targets.findByName(target.name)?.let { it as? KotlinNativeTarget }?.run {
                binaries.framework {
                    this.baseName = frameworkName
                }
            }
        }
    }
    taskXcodeAccess(frameworkName = frameworkName)
    taskXcodeGetFrameworkBuildPhase(frameworkName = frameworkName)
    taskLinkFrameworkIosFat(frameworkName = frameworkName)
    //iosDistribute - auto-upload to itunes connect via openbakery plugin
}

fun Project.taskXcodeAccess(
        name: String = "xcodeAccess",
        frameworkName: String = project.name.replace("-", "_")
) {
    println("xcode.ARCHS = ${findProperty("xcode.ARCHS")}")
    println("xcode.ONLY_ACTIVE_ARCH = ${findProperty("xcode.ONLY_ACTIVE_ARCH")}")
    println("xcode.CONFIGURATION = ${findProperty("xcode.CONFIGURATION")}")
    println("xcode.CONFIGURATION_BUILD_DIR = ${findProperty("xcode.CONFIGURATION_BUILD_DIR")}")
    var architectures = (findProperty("xcode.ARCHS") as? String)?.split(' ')?.mapNotNull {
        when (it.toLowerCase()) {
            "armv7" -> KTarget.iosArm32
            "armv7s" -> KTarget.iosArm32
            "arm64" -> KTarget.iosArm64
            "x86_64" -> KTarget.iosX64
            else -> null
        }
    }?.distinct() ?: listOf()

    if((findProperty("xcode.ONLY_ACTIVE_ARCH") as? String
                    ?: "YES") == "NO") {
        architectures = KTarget.allLogic.filter(KTargetPredicates.isIos)
    }
    val buildType = (findProperty("xcode.CONFIGURATION") as? String ?: "DEBUG").toLowerCase()
    val destination = File(findProperty("xcode.CONFIGURATION_BUILD_DIR") as? String ?: "./build/framework")

    val frameworkType = if (architectures.size == 1) {
        architectures.first().name
    } else {
        "iosFat"
    }
    val dependsOn = "link${buildType.capitalize()}Framework${frameworkType.capitalize()}"
    val frameworkMainLocation = file("build/bin/$frameworkType/${buildType}Framework/$frameworkName.framework")
    val frameworkDsymLocation = file("build/bin/$frameworkType/${buildType}Framework/$frameworkName.framework.dSYM")

    tasks.create(name) {
        it.group = "ios"
        it.dependsOn(dependsOn)
        it.doLast {
            frameworkMainLocation.copyRecursively(File(destination, "$frameworkName.framework"), overwrite = true)
            frameworkDsymLocation.copyRecursively(File(destination, "$frameworkName.framework.dSYM"), overwrite = true)
        }
    }
}

fun Project.taskXcodeGetFrameworkBuildPhase(
        name: String = "printXcodeFrameworkBuildPhase",
        accessTaskName: String = "xcodeAccess",
        frameworkName: String = project.name.replace("-", "_")
) {
    tasks.create(name) {
        it.group = "ios"
        it.doLast {
            println("")
            println("BUILD SCRIPT TEXT:")
            println("")
            println(
                    """
                "${rootProject.file("gradlew")}" -p "${rootProject.file("gradlew").parentFile}" "${project.path}:${accessTaskName}" \
                -Pxcode.ARCHS="${"$"}ARCHS"                             \
                -Pxcode.ONLY_ACTIVE_ARCH="${"$"}ONLY_ACTIVE_ARCH"                     \
                -Pxcode.CONFIGURATION="${"$"}CONFIGURATION"                           \
                -Pxcode.CONFIGURATION_BUILD_DIR="${"$"}CONFIGURATION_BUILD_DIR"
                 """.trimIndent()
            )
            println("")
            println("To use this, open your XCode project and:")
            println("- Click on your project in the left pane.")
            println("- Under 'TARGETS', click the plus button.")
            println("- Enter your framework's name ($frameworkName).")
            println("- Remove the test target it created.")
            println("- Remove the source folders it created.")
            println("- Select the target and go to 'Build Phases'.")
            println("- Remove all tasks except 'Target Dependencies.'")
            println("- Add a 'Run Script' build phase.")
            println("- Copy the script into it.")
            println("")
            println("You may need to set the bitcode options to NO in the build settings on both the project and the framework.")
            println("")
        }
    }
}

fun Project.taskLinkFrameworkIosFat(
        name: String = "link___FrameworkIosFat",
        frameworkName: String = project.name.replace("-", "_")
) {
    taskLinkFrameworkIosFat(
            buildType = "Debug",
            frameworkName = frameworkName,
            name = name.replace("___", "Debug")
    )
    taskLinkFrameworkIosFat(
            buildType = "Release",
            frameworkName = frameworkName,
            name = name.replace("___", "Release")
    )
}

fun Project.taskLinkFrameworkIosFat(
        buildType: String = "Debug",
        frameworkName: String = project.name.replace("-", "_"),
        name: String = "link${buildType}FrameworkIosFat"
) {
    tasks.create(name) {
        it.group = "ios"
        for (d in tasks.filter { it.name.startsWith("link${buildType}Framework") && !it.name.endsWith("Fat") }) {
            it.dependsOn(d.name)
        }
        it.doLast {
            fatFramework(file("build/bin"), frameworkName, buildType)
        }
    }
}

private fun fatFramework(binaryFolder: File, frameworkName: String, buildType: String) {
    val filesToMerge = binaryFolder.listFiles()
            .asSequence()
            .filter { it.name != "iosFat" }
            .map { File(it, buildType.toLowerCase() + "Framework") }
            .filter { it.exists() }
            .map { File(it, "$frameworkName.framework") }
            .filter { it.exists() }
            .map { File(it, frameworkName) }
            .filter { it.exists() }

    val outputFolder = File(binaryFolder, "iosFat/${buildType.toLowerCase()}Framework/$frameworkName.framework")
    outputFolder.mkdirs()

    val toCopy = binaryFolder.listFiles()
            .asSequence()
            .filter { it.name != "iosFat" }
            .map { File(it, buildType.toLowerCase() + "Framework") }
            .filter { it.exists() }
            .map { File(it, "$frameworkName.framework") }
            .filter { it.exists() }
            .firstOrNull() ?: run {
        println("No framework to copy found.")
        return
    }

    toCopy.copyRecursively(outputFolder, overwrite = true)
    File(toCopy.parentFile, "$frameworkName.framework.dSYM").copyRecursively(File(outputFolder.parentFile, "$frameworkName.framework.dSYM"), overwrite = true)

    File(outputFolder, frameworkName).takeIf { it.exists() }?.delete()
    Thread.sleep(100L)

    val stuffToRun = arrayOf("lipo", "-create", *filesToMerge.map { "\"" + it.path + "\"" }.toList().toTypedArray(), "-output", "\"" + File(outputFolder, frameworkName).path + "\"")
    println("Executing ${stuffToRun.joinToString(" ")}")
    ProcessBuilder(*stuffToRun)
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()
            .waitFor(20_000, TimeUnit.MILLISECONDS)

}
