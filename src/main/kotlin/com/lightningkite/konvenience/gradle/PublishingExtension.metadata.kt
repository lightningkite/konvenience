package com.lightningkite.konvenience.gradle

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.internal.publication.DefaultMavenPublication
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinCommonCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinMetadataTargetPreset
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinOnlyTargetPreset

fun PublishingExtension.doNotPublishMetadata() {
    publications.asSequence()
//            .filter { it.name != "metadata" }
            .mapNotNull { it as? DefaultMavenPublication }
            .forEach {
                println("Removing Gradle metadata from ${it.name}")
                it.setModuleDescriptorGenerator(null)
            }
}
