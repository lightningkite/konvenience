package com.lightningkite.konvenience.gradle

import org.gradle.api.Project
import org.gradle.api.publish.maven.internal.publication.DefaultMavenPublication
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinCommonCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinMetadataTargetPreset
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinOnlyTargetPreset

fun Project.doNotPublishMetadata() {
    afterEvaluate {
        val publishing = (this as org.gradle.api.plugins.ExtensionAware).extensions.getByName("publishing") as org.gradle.api.publish.PublishingExtension
        publishing.publications.asSequence()
                .filter { it.name == "metadata" }
                .mapNotNull { it as? DefaultMavenPublication }
                .forEach {
                    it.setModuleDescriptorGenerator(null)
                }
    }
}
