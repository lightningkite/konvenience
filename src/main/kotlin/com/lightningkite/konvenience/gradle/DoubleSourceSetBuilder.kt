package com.lightningkite.konvenience.gradle

import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

class DoubleSourceSetBuilder(
        override val kotlin: KotlinMultiplatformExtension,
        val name: String,
        override val tryTargets: Set<KTarget>,
        override val targets: KTargetPredicate
) : KDependencySetBuilder {
    val main = kotlin.sourceSets.maybeCreate(name + "Main")
    fun main(config: KotlinSourceSet.() -> Unit) = main.apply(config)
    val test = kotlin.sourceSets.maybeCreate(name + "Test")//.apply { dependsOn(main) }
    fun test(config: KotlinSourceSet.() -> Unit) = test.apply(config)
    var supportedTargets: KTargetPredicate = targets

    init {
        tryTargets.filter(targets).forEach { target ->
            if (target.worksOnMyPlatform() && target.name.toLowerCase() != name.toLowerCase()) {
                target.sources {
                    main.dependsOn(this@DoubleSourceSetBuilder.main)
                    test.dependsOn(this@DoubleSourceSetBuilder.test)
                }
            }
        }
    }

    fun KTarget.sources(setup: DoubleSourceSetBuilder.() -> Unit) {
        val builder = DoubleSourceSetBuilder(kotlin, name, tryTargets, isTarget(this))
        builder.main.dependsOn(main)
        builder.test.dependsOn(test)
        builder.apply(setup)
    }

    fun KTargetPredicate.sources(name: String, setup: DoubleSourceSetBuilder.() -> Unit) {
        val builder = DoubleSourceSetBuilder(kotlin, name, tryTargets, targets and this)
        builder.main.dependsOn(main)
        builder.test.dependsOn(test)
        builder.apply(setup)
    }

    fun TargetPredicateWithName.sources(setup: DoubleSourceSetBuilder.() -> Unit) = sources(name, setup)

    fun KotlinSourceSet.dependency(set: KTargetDependencySet) {
        val unsupported = tryTargets.filter(targets and !set.satisfiesTargets)
        unsupported.forEach {
            println("WARNING: '${it.name}' could not be compiled for due to unsatisfied dependency for '${set.name}'.  Removing target.")
        }
        dependencies { set.api.invoke(set.type.get(this)) }

        supportedTargets = supportedTargets and set.satisfiesTargets
        tryTargets.filter(supportedTargets).forEach { target ->
            if (target.worksOnMyPlatform()) {
                target.sources {
                    val targetSourceSet = if (this@dependency.name.endsWith("Test")) {
                        test
                    } else {
                        main
                    }
                    targetSourceSet.dependencies {
                        set.perTarget.firstOrNull { it.first(target) }?.second?.invoke(set.type.get(this@dependencies))
                    }
                }
            }
        }
    }
}

fun KotlinMultiplatformExtension.sources(tryTargets: Set<KTarget> = KTarget.allExceptAndroid, targets: KTargetPredicate = { true }, setup: DoubleSourceSetBuilder.() -> Unit) {
    val builder = DoubleSourceSetBuilder(this, "common", tryTargets, targets)
    builder.apply(setup)
    for (target in tryTargets.filter(builder.supportedTargets)) {
        target.configure(this)
    }
}
