package com.lightningkite.konvenience.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.internal.publication.DefaultMavenPublication
import java.io.File
import java.util.*


open class KonveniencePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val locals = Properties().apply {
            generateSequence(target.rootDir) { it.parentFile }
                    .take(3)
                    .toList()
                    .asReversed()
                    .asSequence()
                    .mapNotNull { File(it, "local.properties").takeIf { it.exists() } }
                    .forEach { load(it.inputStream()) }
        }
        target.extensions.add("localProperties", locals)
        target.afterEvaluate { BuildInfo.print() }
    }
}

