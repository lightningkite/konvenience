package com.lightningkite.konvenience.gradle

import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ExternalModuleDependency
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

typealias KDependency = KotlinDependencyHandlerConfiguration.() -> Dependency?
