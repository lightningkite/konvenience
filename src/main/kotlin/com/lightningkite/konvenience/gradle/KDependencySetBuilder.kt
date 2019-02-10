package com.lightningkite.konvenience.gradle

import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

interface KDependencySetBuilder : KDependencyBuilder, TargetPredicateBuilder {
    val targets: KTargetPredicate
    val tryTargets: Set<KTarget>
    val kotlin: KotlinMultiplatformExtension
}

val KDependencySetBuilder.standardLibrary
    get() = KTargetDependencySet("Standard Library") {
        api = maven(group = "org.jetbrains.kotlin", artifact = "kotlin-stdlib-common")
        isJs uses maven(group = "org.jetbrains.kotlin", artifact = "kotlin-stdlib-js")
        isJvm uses maven(group = "org.jetbrains.kotlin", artifact = "kotlin-stdlib")
        isNative uses ignore
    }
val KDependencySetBuilder.testing
    get() = KTargetDependencySet("Standard Testing") {
        api = maven(group = "org.jetbrains.kotlin", artifact = "kotlin-test-common")
        isJs uses maven(group = "org.jetbrains.kotlin", artifact = "kotlin-test-js")
        isJvm uses maven(group = "org.jetbrains.kotlin", artifact = "kotlin-test-junit")
        isNative uses ignore
    }
val KDependencySetBuilder.testingAnnotations
    get() = KTargetDependencySet("Standard Testing Annotations") {
        api = maven("org.jetbrains.kotlin", "kotlin-test-annotations-common")
        allTargets uses ignore
    }

fun KDependencySetBuilder.serialization(version: String) = KTargetDependencySet("KotlinX Serialization") {
    api = maven("org.jetbrains.kotlinx", "kotlinx-serialization-runtime-common", version)
    isJvm uses maven("org.jetbrains.kotlinx", "kotlinx-serialization-runtime", version)
    isJs uses maven("org.jetbrains.kotlinx", "kotlinx-serialization-runtime-js", version)
    isNativeCommonlyReleased uses maven("org.jetbrains.kotlinx", "kotlinx-serialization-runtime-native", version)
}

fun KDependencySetBuilder.coroutines(version: String) = KTargetDependencySet("KotlinX Coroutines") {
    api = maven("org.jetbrains.kotlinx", "kotlinx-coroutines-core-common", version)
    isJvm uses maven("org.jetbrains.kotlinx", "kotlinx-coroutines-jdk8", version)
    isJs uses maven("org.jetbrains.kotlinx", "kotlinx-coroutines-core-js", version)
    isNativeCommonlyReleased uses maven("org.jetbrains.kotlinx", "kotlinx-coroutines-core-native", version)
}

fun KDependencySetBuilder.mavenGradleMetadata(group: String, artifact: String, version: String, targets: KTargetPredicate = allTargets) = KTargetDependencySet("KotlinX Coroutines") {
    api = maven(group, artifact, version)
    targets uses ignore
}

fun KDependencySetBuilder.projectOrMavenGradleMetadata(group: String, artifact: String, version: String, name: String = artifact, targets: KTargetPredicate = allTargets) = KTargetDependencySet("KotlinX Coroutines") {
    api = projectOrMaven(group, artifact, version, name)
    targets uses ignore
}

fun KDependencySetBuilder.manual(
        name: String,
        api: KDependency,
        setup: KTargetDependencySet.() -> Unit
) = KTargetDependencySet(name = name, api = api).apply(setup)

fun KDependencySetBuilder.mavenDashPlatform(
        group: String,
        artifactStart: String,
        version: String,
        groupings: List<TargetPredicateWithName> = listOf(),
        api: KDependency = KTargetDependencySet.maven(group = group, artifact = "$artifactStart-metadata", version = version),
        setup: KTargetDependencySet.() -> Unit = {}
) = KTargetDependencySet(name = artifactStart) {
    this.api = api
    apply(setup)
    for (target in tryTargets.filter(this@mavenDashPlatform.targets)) {
        groupings.firstOrNull { it(target) }?.let { targetGroup ->
            target uses KTargetDependencySet.maven(group = group, artifact = "$artifactStart-${targetGroup.name.toLowerCase()}", version = version)
        } ?: run {
            target uses KTargetDependencySet.maven(group = group, artifact = "$artifactStart-${target.name.toLowerCase()}", version = version)
        }
    }
}

fun KDependencySetBuilder.projectOrMavenDashPlatform(
        group: String,
        artifactStart: String,
        version: String,
        groupings: List<TargetPredicateWithName> = listOf(),
        api: KDependency = KTargetDependencySet.projectOrMaven(
                group = group,
                artifact = "$artifactStart-metadata",
                version = version,
                name = ":$artifactStart"
        ),
        setup: KTargetDependencySet.() -> Unit = {}
) = KTargetDependencySet(name = artifactStart) {
    this.api = api
    apply(setup)
    for (target in tryTargets.filter(this@projectOrMavenDashPlatform.targets)) {
        groupings.firstOrNull { it(target) }?.let { targetGroup ->
            target uses KTargetDependencySet.projectOrMaven(
                    group = group,
                    artifact = "$artifactStart-${targetGroup.name.toLowerCase()}",
                    version = version,
                    name = ":$artifactStart"
            )
        } ?: run {
            target uses KTargetDependencySet.projectOrMaven(
                    group = group,
                    artifact = "$artifactStart-${target.name.toLowerCase()}",
                    version = version,
                    name = ":$artifactStart"
            )
        }
    }
}
