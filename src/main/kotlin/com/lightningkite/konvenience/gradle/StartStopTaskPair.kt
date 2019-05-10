//package com.lightningkite.konvenience.gradle
//
//import net.rubygrapefruit.platform.Native
//import net.rubygrapefruit.platform.ProcessLauncher
//import org.gradle.api.Project
//import org.gradle.internal.os.OperatingSystem
//import java.io.File
//
//fun Project.startStopTaskPair(
//        baseName: String,
//        group: String = TaskGroups.RUN,
//        directory: File = file("build"),
//        command: List<String>
//) {
//    val pidFile = file("build/startstop/${baseName}Pid.txt")
//
//    tasks.create(baseName + "Start"){ task ->
//        task.doLast {
//            val fullCommand = command
//            val processBuilder = ProcessBuilder()
//                    .command(fullCommand)
//                    .directory(directory)
//                    .inheritIO()
//            val launcher = Native.get(ProcessLauncher::class.java)
//            val process = launcher.start(processBuilder)
//            val pid = process.
//        }
//    }
//
//    tasks.create(baseName + "Stop"){ task ->
//        task.doLast {
//
//        }
//    }
//}