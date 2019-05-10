//package com.lightningkite.konvenience.gradle
//
//import org.jetbrains.kotlin.gradle.dsl.KotlinTargetContainerWithPresetFunctions
//import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
//import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
//import org.jetbrains.kotlin.konan.target.KonanTarget
//
//
//interface HasKotlinTargetConfigure {
//    val configure: (KotlinTargetContainerWithPresetFunctions.() -> KotlinTarget)?
//}
//
//interface Implies {
//    val implies: List<Enum<*>>
//}
//
//enum class Platform(
//        val targetName: String,
//        val platformType: KotlinPlatformType,
//        val konanTarget: KonanTarget? = null,
//        val worksOnMyPlatform: () -> Boolean = { true },
//        override val configure: KotlinTargetContainerWithPresetFunctions.() -> KotlinTarget
//) : HasKotlinTargetConfigure {
//    Jvm(
//            targetName = "jvm",
//            platformType = KotlinPlatformType.jvm,
//            configure = { jvm() }
//    ),
//    Js(
//            targetName = "js",
//            platformType = KotlinPlatformType.js,
//            configure = { js() }
//    ),
//    AndroidNativeArm32(
//            targetName = "androidNativeArm32",
//            platformType = KotlinPlatformType.native,
//            konanTarget = KonanTarget.ANDROID_ARM32,
//            configure = { androidNativeArm32() }
//    ),
//    AndroidNativeArm64(
//            targetName = "androidNativeArm64",
//            platformType = KotlinPlatformType.native,
//            konanTarget = KonanTarget.ANDROID_ARM64,
//            configure = { androidNativeArm64() }
//    ),
//    IosArm32(
//            targetName = "iosArm32",
//            platformType = KotlinPlatformType.native,
//            konanTarget = KonanTarget.IOS_ARM32,
//            configure = { iosArm32() }
//    ),
//    IosArm64(
//            targetName = "iosArm64",
//            platformType = KotlinPlatformType.native,
//            konanTarget = KonanTarget.IOS_ARM64,
//            configure = { iosArm64() }
//    ),
//    IosX64(
//            targetName = "iosX64",
//            platformType = KotlinPlatformType.native,
//            konanTarget = KonanTarget.IOS_X64,
//            configure = { iosX64() }
//    ),
//    LinuxX64(
//            targetName = "linuxX64",
//            platformType = KotlinPlatformType.native,
//            konanTarget = KonanTarget.LINUX_X64,
//            configure = { linuxX64() }
//    ),
//    LinuxArm32Hfp(
//            targetName = "linuxArm32Hfp",
//            platformType = KotlinPlatformType.native,
//            konanTarget = KonanTarget.LINUX_ARM32_HFP,
//            configure = { linuxArm32Hfp() }
//    ),
//    LinuxMips32(
//            targetName = "linuxMips32",
//            platformType = KotlinPlatformType.native,
//            konanTarget = KonanTarget.LINUX_MIPS32,
//            configure = { linuxMips32() }
//    ),
//    LinuxMipsel32(
//            targetName = "linuxMipsel32",
//            platformType = KotlinPlatformType.native,
//            konanTarget = KonanTarget.LINUX_MIPSEL32,
//            configure = { linuxMipsel32() }
//    ),
//    MingwX64(
//            targetName = "mingwX64",
//            platformType = KotlinPlatformType.native,
//            konanTarget = KonanTarget.MINGW_X64,
//            configure = { mingwX64() }
//    ),
//    MacosX64(
//            targetName = "macosX64",
//            platformType = KotlinPlatformType.native,
//            konanTarget = KonanTarget.MACOS_X64,
//            configure = { macosX64() }
//    ),
//    Wasm32(
//            targetName = "wasm32",
//            platformType = KotlinPlatformType.native,
//            konanTarget = KonanTarget.WASM32,
//            configure = { wasm32() }
//    )
//}
//
//typealias Requirements = List<Enum<*>>
//
//fun Requirements.target(container: KotlinTargetContainerWithPresetFunctions): KotlinTarget {
//    asReversed().asSequence()
//            .tree
//            .mapNotNull { it as? HasKotlinTargetConfigure }
//            .mapNotNull { it.configure }
//            .first()
//            .invoke(container)
//    //ORDER MATTERS
//
//}
//
///*
//
//
//set("common") {
//    kotlinStandardLib()
//
//
//    set("android") {
//        android()
//        maven("...")
//    }
//
//    set("name") {
//
//    }
//}
//
//
//DEPENDENCY:
//    - include: DependencySet.()->Unit
//    - satisfies: List<Map<String, String>>
//
//
//OUTPUTS:
//    - Discover flavor types and values
//    - Filter out which are possible
//        - intersect dependencies
//        - union sets
//    - Run possibilities through target generator
//
//*/
//
////typealias PotentialTarget = Map<String, String>
////
////object PotentialTargetConstants {
////    object Platform {
////        const val key = "kotlin.platform"
////
////        const val jvm = "jvm"
////        const val js = "js"
////        const val androidNativeArm32 = "androidNativeArm32"
////        const val androidNativeArm64 = "androidNativeArm64"
////        const val iosArm32 = "iosArm32"
////        const val iosArm64 = "iosArm64"
////        const val iosX64 = "iosX64"
////        const val linuxX64 = "linuxX64"
////        const val linuxArm32Hfp = "linuxArm32Hfp"
////        const val linuxMips32 = "linuxMips32"
////        const val linuxMipsel32 = "linuxMipsel32"
////        const val mingwX64 = "mingwX64"
////        const val macosX64 = "macosX64"
////        const val wasm32 = "wasm32"
////
////        val possibilities = listOf(
////                jvm,
////                js,
////                androidNativeArm32,
////                androidNativeArm64,
////                iosArm32,
////                iosArm64,
////                iosX64,
////                linuxX64,
////                linuxArm32Hfp,
////                linuxMips32,
////                linuxMipsel32,
////                mingwX64,
////                macosX64,
////                wasm32
////        )
////    }
////    object JVM {
////        const val key = "kotlin.platform.jvm"
////        const val desktop = "desktop"
////        const val desktop = "desktop"
////    }
////    object UI {
////        const val key = "kotlin.platform.ui"
////
////        const val android = "android"
////        const val js = "js"
////        const val iosArm32 = "ios"
////        const val linuxX64 = "linuxX64"
////        const val linuxArm32Hfp = "linuxArm32Hfp"
////        const val linuxMips32 = "linuxMips32"
////        const val linuxMipsel32 = "linuxMipsel32"
////        const val mingwX64 = "mingwX64"
////        const val macosX64 = "macosX64"
////        const val wasm32 = "wasm32"
////
////        val possibilities = listOf(
////                jvm,
////                js,
////                androidNativeArm32,
////                androidNativeArm64,
////                iosArm32,
////                iosArm64,
////                iosX64,
////                linuxX64,
////                linuxArm32Hfp,
////                linuxMips32,
////                linuxMipsel32,
////                mingwX64,
////                macosX64,
////                wasm32
////        )
////    }
////}