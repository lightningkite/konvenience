package com.lightningkite.konvenience.gradle

import org.gradle.api.attributes.Attribute
import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.dsl.KotlinTargetContainerWithPresetFunctions
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.konan.target.KonanTarget

data class KTarget(
        val name: String,
        val platformType: KotlinPlatformType,
        val konanTarget: KonanTarget? = null,
        val worksOnMyPlatform: () -> Boolean = { true },
        val configure: KotlinTargetContainerWithPresetFunctions.() -> Unit
) {

    companion object {
        val detailed = Attribute.of("detailedPlatform", String::class.java)

        val jvm = KTarget(
                name = "jvm",
                platformType = KotlinPlatformType.jvm,
                konanTarget = null,
                worksOnMyPlatform = { true },
                configure = { jvm() }
        )
        val android = KTarget(
                name = "android",
                platformType = KotlinPlatformType.androidJvm,
                konanTarget = null,
                worksOnMyPlatform = { true },
                configure = { android() }
        )
        val js = KTarget(
                name = "js",
                platformType = KotlinPlatformType.js,
                konanTarget = null,
                worksOnMyPlatform = { true },
                configure = { js() }
        )
        val androidNativeArm32 = KTarget(
                name = "androidNativeArm32",
                platformType = KotlinPlatformType.native,
                konanTarget = KonanTarget.ANDROID_ARM32,
                worksOnMyPlatform = { OperatingSystem.current().isLinux || OperatingSystem.current().isMacOsX },
                configure = { androidNativeArm32() }
        )
        val androidNativeArm64 = KTarget(
                name = "androidNativeArm64",
                platformType = KotlinPlatformType.native,
                konanTarget = KonanTarget.ANDROID_ARM64,
                worksOnMyPlatform = { OperatingSystem.current().isLinux || OperatingSystem.current().isMacOsX },
                configure = { androidNativeArm64() }
        )
        val iosArm32 = KTarget(
                name = "iosArm32",
                platformType = KotlinPlatformType.native,
                konanTarget = KonanTarget.IOS_ARM32,
                worksOnMyPlatform = { OperatingSystem.current().isMacOsX },
                configure = { iosArm32() }
        )
        val iosArm64 = KTarget(
                name = "iosArm64",
                platformType = KotlinPlatformType.native,
                konanTarget = KonanTarget.IOS_ARM64,
                worksOnMyPlatform = { OperatingSystem.current().isMacOsX },
                configure = { iosArm64() }
        )
        val iosX64 = KTarget(
                name = "iosX64",
                platformType = KotlinPlatformType.native,
                konanTarget = KonanTarget.IOS_X64,
                worksOnMyPlatform = { OperatingSystem.current().isMacOsX },
                configure = { iosX64() }
        )
        val linuxX64 = KTarget(
                name = "linuxX64",
                platformType = KotlinPlatformType.native,
                konanTarget = KonanTarget.LINUX_X64,
                worksOnMyPlatform = { OperatingSystem.current().isLinux },
                configure = { linuxX64() }
        )
        val linuxArm32Hfp = KTarget(
                name = "linuxArm32Hfp",
                platformType = KotlinPlatformType.native,
                konanTarget = KonanTarget.LINUX_ARM32_HFP,
                worksOnMyPlatform = { OperatingSystem.current().isLinux },
                configure = { linuxArm32Hfp() }
        )
        val linuxMips32 = KTarget(
                name = "linuxMips32",
                platformType = KotlinPlatformType.native,
                konanTarget = KonanTarget.LINUX_MIPS32,
                worksOnMyPlatform = { OperatingSystem.current().isLinux },
                configure = { linuxMips32() }
        )
        val linuxMipsel32 = KTarget(
                name = "linuxMipsel32",
                platformType = KotlinPlatformType.native,
                konanTarget = KonanTarget.LINUX_MIPSEL32,
                worksOnMyPlatform = { OperatingSystem.current().isLinux },
                configure = { linuxMipsel32() }
        )
        val mingwX64 = KTarget(
                name = "mingwX64",
                platformType = KotlinPlatformType.native,
                konanTarget = KonanTarget.MINGW_X64,
                worksOnMyPlatform = { OperatingSystem.current().isWindows },
                configure = { mingwX64() }
        )
        val macosX64 = KTarget(
                name = "macosX64",
                platformType = KotlinPlatformType.native,
                konanTarget = KonanTarget.MACOS_X64,
                worksOnMyPlatform = { OperatingSystem.current().isMacOsX },
                configure = { macosX64() }
        )
        val wasm32 = KTarget(
                name = "wasm32",
                platformType = KotlinPlatformType.native,
                konanTarget = KonanTarget.WASM32,
                worksOnMyPlatform = { true },
                configure = { wasm32() }
        )


        val all = setOf(
                jvm,
                android,
                js,
                androidNativeArm32,
                androidNativeArm64,
                iosArm32,
                iosArm64,
                iosX64,
                linuxX64,
                linuxArm32Hfp,
                linuxMips32,
                linuxMipsel32,
                mingwX64,
                macosX64,
                wasm32
        )
        val allExceptAndroid = all - android

    }
}
