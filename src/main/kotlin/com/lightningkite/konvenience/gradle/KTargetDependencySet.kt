package com.lightningkite.konvenience.gradle

import org.jetbrains.kotlin.gradle.plugin.KotlinTarget

data class KTargetDependencySet(
        val name: String,
        val type: KDependencyType = KDependencyType.Implementation,
        var api: KDependency = KTargetDependencySet.ignore,
        var filterSoFar: KTargetPredicate = { false },
        val perTarget: MutableList<Pair<KTargetPredicate, KDependency>> = ArrayList()
) {

    constructor(
            name: String,
            type: KDependencyType = KDependencyType.Implementation,
            setup: KTargetDependencySet.() -> Unit
    ) : this(name, type) {
        apply(setup)
    }

    fun type(type: KDependencyType) = this.copy(type = type)

    infix fun KTargetPredicate.uses(dependency: KDependency) {
        perTarget.add(this to dependency)
    }

    infix fun KTarget.uses(dependency: KDependency) {
        perTarget.add(isTarget(this) to dependency)
    }

    val satisfiesTargets get() = perTarget.asSequence().map { it.first }.any()

    companion object : KDependencyBuilder, TargetPredicateBuilder
}
