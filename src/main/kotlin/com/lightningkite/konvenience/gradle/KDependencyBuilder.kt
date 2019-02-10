package com.lightningkite.konvenience.gradle

interface KDependencyBuilder : TargetPredicateBuilder {
}

val KDependencyBuilder.ignore: KDependency get() = { null }
fun KDependencyBuilder.maven(group: String, artifact: String, version: String): KDependency {
    return { add("$group:$artifact:$version") }
}

fun KDependencyBuilder.maven(group: String, artifact: String): KDependency {
    return { add("$group:$artifact") }
}

fun KDependencyBuilder.project(name: String, configuration: String? = null): KDependency {
    return { add(project(name, configuration)) }
}

fun KDependencyBuilder.projectOrMaven(
        group: String,
        artifact: String,
        version: String,
        name: String = ":$artifact",
        configuration: String? = null
): KDependency {
    return {
        try {
            val dep = add(project(name, configuration))
            println("Using local project $name")
            dep
        } catch (e: Throwable) {
            println("Using remote artifact $group:$artifact:$version due to $e")
            add("$group:$artifact:$version")
        }
    }
}
