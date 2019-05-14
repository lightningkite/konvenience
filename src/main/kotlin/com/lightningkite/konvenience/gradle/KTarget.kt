package com.lightningkite.konvenience.gradle

import org.gradle.api.attributes.Attribute
import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.dsl.KotlinTargetContainerWithPresetFunctions
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.konan.target.KonanTarget

data class KTarget(
        val name: String,
        val platformType: KotlinPlatformType,
        val konanTarget: KonanTarget? = null,
        val worksOnMyPlatform: () -> Boolean = { true },
        val attributes: Map<String, String> = mapOf(),
        val configure: KotlinTargetContainerWithPresetFunctions.(KTarget) -> KotlinTarget
) {

    fun setup(container: KotlinTargetContainerWithPresetFunctions) {
        configure(container, this).apply {
            attributes {
                for((key, value) in this@KTarget.attributes) {
                    attribute(Attribute.of(key, String::class.java), value)
                }
            }
        }
    }

    companion object {
        const val UI = "com.lightningkite.konvenience.ui"
        val attributeUI = Attribute.of(UI, String::class.java)

        val jvm = KTarget(
                name = "jvm",
                platformType = KotlinPlatformType.jvm,
                konanTarget = null,
                worksOnMyPlatform = { true },
                configure = { jvm(it.name) }
        )
        val js = KTarget(
                name = "js",
                platformType = KotlinPlatformType.js,
                konanTarget = null,
                worksOnMyPlatform = { true },
                configure = {
                    js(it.name) {
                        compilations.getByName("main") {
                            it.kotlinOptions {
                                moduleKind = "umd"
                                sourceMap = true
                                sourceMapEmbedSources = "always"
                            }
                        }
                    }
                }
        )
        val androidNativeArm32 = KTarget(
                name = "androidNativeArm32",
                platformType = KotlinPlatformType.native,
                konanTarget = KonanTarget.ANDROID_ARM32,
//                worksOnMyPlatform = { OperatingSystem.current().isLinux || OperatingSystem.current().isMacOsX },
                configure = { androidNativeArm32(it.name) }
        )
        val androidNativeArm64 = KTarget(
                name = "androidNativeArm64",
                platformType = KotlinPlatformType.native,
                konanTarget = KonanTarget.ANDROID_ARM64,
                worksOnMyPlatform = { OperatingSystem.current().isLinux || OperatingSystem.current().isMacOsX },
                configure = { androidNativeArm64(it.name) }
        )
        val iosArm32 = KTarget(
                name = "iosArm32",
                platformType = KotlinPlatformType.native,
                konanTarget = KonanTarget.IOS_ARM32,
                worksOnMyPlatform = { OperatingSystem.current().isMacOsX },
                configure = { iosArm32(it.name) }
        )
        val iosArm64 = KTarget(
                name = "iosArm64",
                platformType = KotlinPlatformType.native,
                konanTarget = KonanTarget.IOS_ARM64,
                worksOnMyPlatform = { OperatingSystem.current().isMacOsX },
                configure = { iosArm64(it.name) }
        )
        val iosX64 = KTarget(
                name = "iosX64",
                platformType = KotlinPlatformType.native,
                konanTarget = KonanTarget.IOS_X64,
                worksOnMyPlatform = { OperatingSystem.current().isMacOsX },
                configure = { iosX64(it.name) }
        )
        val linuxX64 = KTarget(
                name = "linuxX64",
                platformType = KotlinPlatformType.native,
                konanTarget = KonanTarget.LINUX_X64,
                configure = { linuxX64(it.name) }
        )
        val linuxArm32Hfp = KTarget(
                name = "linuxArm32Hfp",
                platformType = KotlinPlatformType.native,
                konanTarget = KonanTarget.LINUX_ARM32_HFP,
                configure = { linuxArm32Hfp(it.name) }
        )
        val linuxMips32 = KTarget(
                name = "linuxMips32",
                platformType = KotlinPlatformType.native,
                konanTarget = KonanTarget.LINUX_MIPS32,
                worksOnMyPlatform = { OperatingSystem.current().isLinux },
                configure = { linuxMips32(it.name) }
        )
        val linuxMipsel32 = KTarget(
                name = "linuxMipsel32",
                platformType = KotlinPlatformType.native,
                konanTarget = KonanTarget.LINUX_MIPSEL32,
                worksOnMyPlatform = { OperatingSystem.current().isLinux },
                configure = { linuxMipsel32(it.name) }
        )
        val mingwX64 = KTarget(
                name = "mingwX64",
                platformType = KotlinPlatformType.native,
                konanTarget = KonanTarget.MINGW_X64,
                worksOnMyPlatform = { OperatingSystem.current().isWindows },
                configure = { mingwX64(it.name) }
        )
        val macosX64 = KTarget(
                name = "macosX64",
                platformType = KotlinPlatformType.native,
                konanTarget = KonanTarget.MACOS_X64,
                worksOnMyPlatform = { OperatingSystem.current().isMacOsX },
                configure = { macosX64(it.name) }
        )
        val wasm32 = KTarget(
                name = "wasm32",
                platformType = KotlinPlatformType.native,
                konanTarget = KonanTarget.WASM32,
                worksOnMyPlatform = { true },
                configure = { wasm32(it.name) }
        )


        val jvmVirtual = jvm.copy(name = "jvmVirtual", attributes = mapOf(UI to "jvmVirtual"))
        val javafx = jvm.copy(name = "javafx", attributes = mapOf(UI to "javafx"))
        val swing = jvm.copy(name = "swing", attributes = mapOf(UI to "swing"))

        val android = KTarget(
                name = "android",
                platformType = KotlinPlatformType.androidJvm,
                konanTarget = null,
                worksOnMyPlatform = { true },
                attributes = mapOf(UI to "android"),
                configure = {
                    android {
                        this.publishAllLibraryVariants()
                    }
                }
        )



        val allLogic = setOf(
                jvm,
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
        val allUI = setOf(
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
                js,
                android,
                javafx,
                swing,
                jvmVirtual
        )
        val allExceptAndroid get() = allLogic

    }
}
