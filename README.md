# Konvenience

by Lightning Kite

[![Download](https://api.bintray.com/packages/lightningkite/com.lightningkite.krosslin/konvenience/images/download.svg) ](https://bintray.com/lightningkite/com.lightningkite.krosslin/konvenience/_latestVersion)

A Gradle plugin for simplifying your build script for Kotlin multiplatform projects

## A Full Example for OSS Libraries

```kotlin
//build.gradle.kts

//Normal Gradle stuff here for plugins and buildscript

import com.lightningkite.konvenience.gradle.*
import java.util.Properties

plugins {
    kotlin("multiplatform") version "1.3.21"
    `maven-publish`
}

buildscript {
    repositories {
        mavenLocal()
        maven("https://dl.bintray.com/lightningkite/com.lightningkite.krosslin")
    }
    dependencies {
        classpath("com.lightningkite:konvenience:+")ts
    }
}
apply(plugin = "com.lightningkite.konvenience")

//Normal repositories entry

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://dl.bintray.com/lightningkite/com.lightningkite.krosslin")
    maven("https://kotlin.bintray.com/kotlinx")
}

//Normal Gradle project information

group = "com.lightningkite"
version = "0.0.1"

//The good stuff - the additions to Kotlin are used here

kotlin {
    sources {
        main {
            //Dependencies and their per-platform implementations
            dependency(standardLibrary)
            dependency(serialization("0.10.0"))
            dependency(projectOrMavenDashPlatform("com.lightningkite", "kommon", "0.1.3"))
            
            //Also just a regular source set here
            dependencies {
                api("something:or:other")
            }
        }
        test {
            dependency(testing)
            dependency(testingAnnotations)
            dependency(projectOrMavenDashPlatform("com.lightningkite", "lokalize", "0.1.1"))
            dependency(projectOrMavenDashPlatform("com.lightningkite", "recktangle", "0.1.1"))
        }
        
        //Child source sets of common
        isNative.sources {
            isPosix.sources {
            }
            isMingwX64.sources {
            
            }
        }
        isNotNative.sources {
            isJs.sources {
            
            }
            isJvm.sources {
                //Access regular source sets for isJvm
                main { 
                    //That weird custom dependency for JVM
                    dependencies {
                        api("something:or:other")
                    }
                }
                test { }
            }
        }
        
        //No need to declare per-target source sets UNLESS you have code for them.
    }
}

publishing {
    repositories {
        //Wahoo!  Bintray shortcut!
        bintray(
                project = project,
                organization = "lightningkite",
                repository = "com.lightningkite.krosslin"
        )
    }

    //Let's get our POM together
    appendToPoms {
        //Just get all that GitHub stuff entered
        github("lightningkite", "mirror-kotlin")
        
        //MIT license
        licenseMIT()
        
        //Other normal POM information
        developers {
            developer {
                id.set("UnknownJoe796")
                name.set("Joseph Ivie")
                email.set("joseph@lightningkite.com")
                timezone.set("America/Denver")
                roles.set(listOf("architect", "developer"))
                organization.set("Lightning Kite")
                organizationUrl.set("http://www.lightningkite.com")
            }
        }
    }
}

```


## Features

### Hierarchical Source Sets

You can structure your source sets much more concisely now.  For example:

```kotlin
kotlin {
    sources {  //<- This starts a block of source set declarations, by default named common
        main {
            //Your regular commonMain source set can be configured as usual here.
        }
        test {
            //Your regular commonTest source set can be configured as usual here.
        }
        
        //You can specify additional source sets by:
        //<predicate for target>.sources("name") {}
        //Which will start another block like the first one, except with a 
        //different name and a dependency on the containing source sets.
        
        { target: KTarget -> target.platform == KotlinPlatform.androidJvm }.sources("androidJvm"){
            //Once again, you can access main and test
            main { }
            test { }
        }
        
        //Here are some predefined predicates, which also automatically use 
        //the name after the `is` part
        
        isAndroidArm32.sources { }
        isAndroidArm64.sources { }
        isAndroidJvm.sources { }
        isAndroidNative.sources { }
        isApple.sources { }
        isArm32.sources { }
        isArm64.sources { }
        isIos.sources { }
        isIosArm32.sources { }
        isIosArm64.sources { }
        isIosX64.sources { }
        isJs.sources { }
        isJvm.sources { }
        isLinux.sources { }
        isLinuxArm32Hfp.sources { }
        isLinuxMips32.sources { }
        isLinuxMipsel32.sources { }
        isLinuxX64.sources { }
        isMacosX64.sources { }
        isMetadata.sources { }
        isMinGW.sources { }
        isMingwX64.sources { }
        isMips32.sources { }
        isMipsel32.sources { }
        isNative.sources { }
        isNativeCommonlyReleased.sources { }
        isNonNative.sources { }
        isOracleJvm.sources { }
        isOsx.sources { }
        isPosix.sources { }
        isWasm.sources { }
        isWasm32.sources { }
        isX64.sources { }
        isZephyr.sources { }
        
        //You are allowed to put source sets within other source sets
        //in a nested manner.  A good example might be:
        isNotNative.sources {
            isJvm.sources{}
            isJs.sources{}
        }
        //The above would make source sets 'jvmMain' and 'jsMain' depend on 'notNativeMain'
        //which in turn depends on 'commonMain'
        
        //Note that you can still access main and test source sets directly, so this
        //does not at all prevent you from manually configuring anything you please.
    }
}
```

#### `sources(tryTargets: Set<KTarget>){ }`

- `tryTargets` is the set of targets you want to attempt to build to.  By default, it
is `KTarget.allExceptAndroid`.  Any targets specified here will be built to **if** every
multiplatform dependency (system described in next section) is met.  If one cannot be met,
a warning will appear and the target will not be set up.


### Easy Multiplatform Dependencies

You can now describe your dependencies in a more centralized manner.

```kotlin
kotlin {
    sources {
        main {
            //We can define a dependency that's used in this source set, as well
            //as what each target should supplement it with.
            dependency(manual(
                name = "My Dependency",
                api = maven("group", "artifact-probably-common", "version")
            ){
                isJvm uses maven("group", "artifact-probably-jvm", "version")
                isJs uses maven("group", "artifact-probably-js", "version")
                isNative uses maven("group", "artifact-probably-native", "version")
            })
            
            //For libraries that mostly end in -target, we can use this:
            dependency(mavenDashPlatform(
                group = "group",
                artifactStart = "artifact", //-target will be appended
                version = "version"
            ){
                //You can override anything needed in here.
                //This block is optional.
            })
            
            //If you have a library that fully depends on Gradle metadata, you could use this:
            dependency(mavenGradleMetadata(
                group = "group",
                artifact = "artifact",
                version = "version"
            ))
            
            //There are also some built-in dependencies.
            dependency(standardLibrary)
            dependency(testing)
            dependency(testingAnnotations)
            dependency(serialization("version"))
            dependency(coroutines("version"))
        }
    }
}
```


### Easy publishing to Bintray

Pulls `bintrayUser` and a `bintrayKey` from 'local.properties', either in the directory of the project or at maximum 2 directories up from it.

You need a `bintrayUser` and a `bintrayKey` to upload to Bintray.

This will also add a task called `releaseBintray`, which will finalize the release of uploaded files.

```kotlin
publishing {
    repositories {
        bintray(
                project = project,
                organization = "organization",
                repository = "my.repository"
        )
    }
}
```


### Quick POM Data

```kotlin
publishing {
    appendToPoms {
        //Appends anything here to every POM released by Kotlin Multiplatform
        //This just uses the standard API for modifying POMs (MavenPom)
        developers {
            developer {
                id.set("UnknownJoe796")
                name.set("Joseph Ivie")
                email.set("joseph@lightningkite.com")
                timezone.set("America/Denver")
                roles.set(listOf("architect", "developer"))
                organization.set("Lightning Kite")
                organizationUrl.set("http://www.lightningkite.com")
            }
        }
        
        //This is a shortcut to set up issues, version control, and website in the POM
        github("owner", "RepositoryName")
        
        //This is a shortcut to use the MIT license in your POM
        licenseMIT()
    }
    
}
```


### Single-line Dokka

Your mileage may vary with this one - it's very much still in development.

Includes documentation in all publications.

```kotlin
kotlin {
    //At the very end after setting up all source sets/targets
    dokka(project) {
        //Normal lambda for configuring Dokka, optional
    }
}
```
