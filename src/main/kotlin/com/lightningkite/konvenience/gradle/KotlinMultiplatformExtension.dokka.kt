package com.lightningkite.konvenience.gradle

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.dokka.gradle.SourceRoot
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File

//fun DoubleSourceSetBuilder.dokka(project: Project){
//    project.tasks.create("dokka$name", DokkaTask::class.java){ task ->
//        task.impliedPlatforms = mutableListOf("Common")
//        task.outputFormat = "kotlin"
//        task.outputDirectory = File(File(project.rootDir, "documentation"), this.name).path
//        task.includes = listOf("packages.md", "extra.md")
//        task.include(main)
//    }
//}
//
//fun DokkaTask.include(set: KotlinSourceSet){
//    set.kotlin.srcDirs.forEach { dir ->
//        this.sourceRoots.add(SourceRoot().apply {
//            platforms = listOf(set.name.removeSuffix("Main").capitalize())
//            path = dir.path
//        })
//    }
//    set.dependsOn.forEach { depends ->
//        include(depends)
//    }
//}


fun KotlinMultiplatformExtension.dokka(project: Project, configure: DokkaTask.() -> Unit = {}) {
    val dokkaTask = project.tasks.create("dokkaCommon", DokkaTask::class.java) { task ->
        task.kotlinTasks(object : Closure<Any?>(task) {
            override fun call(): Any? = listOf<Any?>()
        })
        task.impliedPlatforms = mutableListOf("Common")
        task.outputDirectory = File(project.rootDir, "docs").path
        task.includeWithSubs(this, sourceSets.findByName("commonMain")!!)
        task.apply(configure)
    }
    project.tasks.create("dokkaJar", Jar::class.java) { task ->
        task.dependsOn("dokkaCommon")
        task.classifier = "javadoc"
        task.from(dokkaTask.outputDirectory)
    }
    targets.forEach {
        it.mavenPublication(Action { pub ->
            pub.artifact(project.tasks.findByName("dokkaJar"))
        })
    }
}

fun DokkaTask.includeWithSubs(kme: KotlinMultiplatformExtension, set: KotlinSourceSet) {
    set.kotlin.srcDirs.forEach { dir ->
        this.sourceRoots.add(SourceRoot().apply {
            platforms = listOf(set.name.removeSuffix("Main").capitalize())
            path = dir.path
        })
    }
    for (sub in kme.sourceSets) {
        if (sub.dependsOn.contains(set)) {
            includeWithSubs(kme, sub)
        }
    }
}
