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
        isOracleJvm uses maven(group = "org.jetbrains.kotlin", artifact = "kotlin-stdlib-jdk8")
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
    isAndroid uses maven("org.jetbrains.kotlinx", "kotlinx-coroutines-android", version)
    KTarget.javafx uses maven("org.jetbrains.kotlinx", "kotlinx-coroutines-javafx", version)
    KTarget.swing uses maven("org.jetbrains.kotlinx", "kotlinx-coroutines-swing", version)
    isJvm uses maven("org.jetbrains.kotlinx", "kotlinx-coroutines-jdk8", version)
    isJs uses maven("org.jetbrains.kotlinx", "kotlinx-coroutines-core-js", version)
    isNativeCommonlyReleased uses maven("org.jetbrains.kotlinx", "kotlinx-coroutines-core-native", version)
}

fun KDependencySetBuilder.ktorClient(version: String) = KTargetDependencySet("KTor Client") {
    api = maven("io.ktor", "ktor-client-core", version)
    isAndroid uses maven("io.ktor", "ktor-client-android", version)
    isJvm uses maven("io.ktor", "ktor-client-cio", version)
    isJs uses maven("io.ktor", "ktor-client-js", version)
    isIos uses maven("io.ktor", "ktor-client-ios", version)
    isMingwX64 uses maven("io.ktor", "ktor-client-curl-mingwx64", version)
    isLinuxX64 uses maven("io.ktor", "ktor-client-curl-linuxx64", version)
    isMacosX64 uses maven("io.ktor", "ktor-client-curl-macosx64", version)
}

fun KDependencySetBuilder.mavenGradleMetadata(group: String, artifact: String, version: String, targets: KTargetPredicate = allTargets) = KTargetDependencySet("$group:$artifact:$version") {
    api = maven(group, artifact, version)
    targets uses ignore
}

fun KDependencySetBuilder.projectOrMavenGradleMetadata(group: String, artifact: String, version: String, name: String = artifact, targets: KTargetPredicate = allTargets) = KTargetDependencySet("$group:$artifact:$version") {
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
        if (satisfiesTargets(target)) continue
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
        if (satisfiesTargets(target)) continue
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

fun KDependencySetBuilder.project(
        name: String,
        groupings: List<TargetPredicateWithName> = listOf(),
        setup: KTargetDependencySet.() -> Unit = {}
) = KTargetDependencySet(name = name) {
    this.api = KTargetDependencySet.project(name)
    apply(setup)
    for (target in tryTargets.filter(this@project.targets)) {
        if (satisfiesTargets(target)) continue
        groupings.firstOrNull { it(target) }?.let { targetGroup ->
            target uses KTargetDependencySet.project(name)
        } ?: run {
            target uses KTargetDependencySet.project(name)
        }
    }
}