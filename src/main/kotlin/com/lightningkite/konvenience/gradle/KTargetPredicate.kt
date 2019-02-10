package com.lightningkite.konvenience.gradle

import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.Architecture
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget

typealias KTargetPredicate = (KTarget) -> Boolean

data class TargetPredicateWithName(val name: String, val predicate: KTargetPredicate) : KTargetPredicate by predicate

fun KTargetPredicate.withName(name: String) = TargetPredicateWithName(name, this)

interface TargetPredicateBuilder {
    fun isTarget(target: KTarget): KTargetPredicate = { target.name == it.name }

    val allTargets: TargetPredicateWithName get() = TargetPredicateWithName("") { true }

    val isMetadata: TargetPredicateWithName get() = TargetPredicateWithName("metadata") { it.platformType == KotlinPlatformType.common }

    val isNonNative: TargetPredicateWithName get() = TargetPredicateWithName("nonNative") { it.platformType == KotlinPlatformType.jvm || it.platformType == KotlinPlatformType.androidJvm || it.platformType == KotlinPlatformType.js }
    val isJvm: TargetPredicateWithName get() = TargetPredicateWithName("jvm") { it.platformType == KotlinPlatformType.jvm || it.platformType == KotlinPlatformType.androidJvm }
    val isOracleJvm: TargetPredicateWithName get() = TargetPredicateWithName("oracleJvm") { it.platformType == KotlinPlatformType.jvm }
    val isAndroidJvm: TargetPredicateWithName get() = TargetPredicateWithName("androidJvm") { it.platformType == KotlinPlatformType.androidJvm }
    val isJs: TargetPredicateWithName get() = TargetPredicateWithName("js") { it.platformType == KotlinPlatformType.js }
    val isNative: TargetPredicateWithName get() = TargetPredicateWithName("native") { it.platformType == KotlinPlatformType.native }

    val isAndroidArm32: TargetPredicateWithName get() = TargetPredicateWithName("androidArm32") { it.konanTarget == KonanTarget.ANDROID_ARM32 }
    val isAndroidArm64: TargetPredicateWithName get() = TargetPredicateWithName("androidArm64") { it.konanTarget == KonanTarget.ANDROID_ARM64 }
    val isIosArm32: TargetPredicateWithName get() = TargetPredicateWithName("iosArm32") { it.konanTarget == KonanTarget.IOS_ARM32 }
    val isIosArm64: TargetPredicateWithName get() = TargetPredicateWithName("iosArm64") { it.konanTarget == KonanTarget.IOS_ARM64 }
    val isIosX64: TargetPredicateWithName get() = TargetPredicateWithName("iosX64") { it.konanTarget == KonanTarget.IOS_X64 }
    val isLinuxX64: TargetPredicateWithName get() = TargetPredicateWithName("linuxX64") { it.konanTarget == KonanTarget.LINUX_X64 }
    val isMingwX64: TargetPredicateWithName get() = TargetPredicateWithName("mingwX64") { it.konanTarget == KonanTarget.MINGW_X64 }
    val isMacosX64: TargetPredicateWithName get() = TargetPredicateWithName("macosX64") { it.konanTarget == KonanTarget.MACOS_X64 }
    val isLinuxArm32Hfp: TargetPredicateWithName get() = TargetPredicateWithName("linuxArm32Hfp") { it.konanTarget == KonanTarget.LINUX_ARM32_HFP }
    val isLinuxMips32: TargetPredicateWithName get() = TargetPredicateWithName("linuxMips32") { it.konanTarget == KonanTarget.LINUX_MIPS32 }
    val isLinuxMipsel32: TargetPredicateWithName get() = TargetPredicateWithName("linuxMipsel32") { it.konanTarget == KonanTarget.LINUX_MIPSEL32 }
    val isWasm32: TargetPredicateWithName get() = TargetPredicateWithName("wasm32") { it.konanTarget == KonanTarget.WASM32 }

    val isOsx: TargetPredicateWithName get() = TargetPredicateWithName("osx") { it.konanTarget?.family == Family.OSX }
    val isIos: TargetPredicateWithName get() = TargetPredicateWithName("ios") { it.konanTarget?.family == Family.IOS }
    val isLinux: TargetPredicateWithName get() = TargetPredicateWithName("linux") { it.konanTarget?.family == Family.LINUX }
    val isMinGW: TargetPredicateWithName get() = TargetPredicateWithName("minGW") { it.konanTarget?.family == Family.MINGW }
    val isAndroidNative: TargetPredicateWithName get() = TargetPredicateWithName("androidNative") { it.konanTarget?.family == Family.ANDROID }
    val isWasm: TargetPredicateWithName get() = TargetPredicateWithName("wasm") { it.konanTarget?.family == Family.WASM }
    val isZephyr: TargetPredicateWithName get() = TargetPredicateWithName("zephyr") { it.konanTarget?.family == Family.ZEPHYR }

    val isX64: TargetPredicateWithName get() = TargetPredicateWithName("x64") { it.konanTarget?.architecture == Architecture.X64 }
    val isArm64: TargetPredicateWithName get() = TargetPredicateWithName("arm64") { it.konanTarget?.architecture == Architecture.ARM64 }
    val isArm32: TargetPredicateWithName get() = TargetPredicateWithName("arm32") { it.konanTarget?.architecture == Architecture.ARM32 }
    val isMips32: TargetPredicateWithName get() = TargetPredicateWithName("mips32") { it.konanTarget?.architecture == Architecture.MIPS32 }
    val isMipsel32: TargetPredicateWithName get() = TargetPredicateWithName("mipsel32") { it.konanTarget?.architecture == Architecture.MIPSEL32 }

    val isApple: TargetPredicateWithName get() = TargetPredicateWithName("apple") { (it.konanTarget?.family == Family.OSX || it.konanTarget?.family == Family.IOS) }
    val isPosix: TargetPredicateWithName get() = TargetPredicateWithName("posix", isApple or isLinux or isAndroidNative)
    val isNativeCommonlyReleased: TargetPredicateWithName
        get() = TargetPredicateWithName("nativeCommonlyReleased") {
            when (it.konanTarget) {
                KonanTarget.IOS_ARM32,
                KonanTarget.IOS_ARM64,
                KonanTarget.IOS_X64,
                KonanTarget.LINUX_X64,
                KonanTarget.MINGW_X64,
                KonanTarget.MACOS_X64 -> true
                else -> false
            }
        }
}

operator fun KTargetPredicate.not(): KTargetPredicate = { !this(it) }
infix fun KTargetPredicate.and(other: KTargetPredicate): KTargetPredicate = { this(it) && other(it) }
infix fun KTargetPredicate.or(other: KTargetPredicate): KTargetPredicate = { this(it) || other(it) }
fun Iterable<KTargetPredicate>.any(): KTargetPredicate = { this.fold(false) { r, p -> r || p(it) } }
fun Iterable<KTargetPredicate>.all(): KTargetPredicate = { this.fold(true) { r, p -> r && p(it) } }
fun Iterable<KTargetPredicate>.none(): KTargetPredicate = { this.fold(true) { r, p -> r && !p(it) } }
fun Sequence<KTargetPredicate>.any(): KTargetPredicate = { this.fold(false) { r, p -> r || p(it) } }
fun Sequence<KTargetPredicate>.all(): KTargetPredicate = { this.fold(true) { r, p -> r && p(it) } }
fun Sequence<KTargetPredicate>.none(): KTargetPredicate = { this.fold(true) { r, p -> r && !p(it) } }
