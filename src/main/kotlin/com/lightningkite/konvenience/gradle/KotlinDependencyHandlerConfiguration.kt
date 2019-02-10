package com.lightningkite.konvenience.gradle

import groovy.lang.Closure
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.util.ConfigureUtil
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

interface KotlinDependencyHandlerConfiguration : KotlinDependencyHandler {
    fun add(dependencyNotation: Any): Dependency?
    fun add(dependencyNotation: String, configure: ExternalModuleDependency.() -> Unit): ExternalModuleDependency
    fun <T : Dependency> add(dependency: T, configure: T.() -> Unit): T
    fun add(dependencyNotation: String, configure: Closure<*>) = add(dependencyNotation) { ConfigureUtil.configure(configure, this) }
    fun <T : Dependency> add(dependency: T, configure: Closure<*>) = add(dependency) { ConfigureUtil.configure(configure, this) }
}


enum class KDependencyType {
    Api {
        override fun get(handler: KotlinDependencyHandler): KotlinDependencyHandlerConfiguration {
            return handler.api
        }
    },
    Implementation {
        override fun get(handler: KotlinDependencyHandler): KotlinDependencyHandlerConfiguration {
            return handler.implementation
        }
    },
    RuntimeOnly {
        override fun get(handler: KotlinDependencyHandler): KotlinDependencyHandlerConfiguration {
            return handler.runtimeOnly
        }
    },
    CompileOnly {
        override fun get(handler: KotlinDependencyHandler): KotlinDependencyHandlerConfiguration {
            return handler.compileOnly
        }
    };

    abstract fun get(handler: KotlinDependencyHandler): KotlinDependencyHandlerConfiguration
}

val KotlinDependencyHandler.api: KotlinDependencyHandlerConfiguration
    get() = object : KotlinDependencyHandlerConfiguration, KotlinDependencyHandler by this {
        override fun add(dependencyNotation: Any): Dependency? = api(dependencyNotation)
        override fun add(dependencyNotation: String, configure: ExternalModuleDependency.() -> Unit): ExternalModuleDependency = api(dependencyNotation, configure)
        override fun <T : Dependency> add(dependency: T, configure: T.() -> Unit): T = api(dependency, configure)
    }

val KotlinDependencyHandler.implementation: KotlinDependencyHandlerConfiguration
    get() = object : KotlinDependencyHandlerConfiguration, KotlinDependencyHandler by this {
        override fun add(dependencyNotation: Any): Dependency? = implementation(dependencyNotation)
        override fun add(dependencyNotation: String, configure: ExternalModuleDependency.() -> Unit): ExternalModuleDependency = implementation(dependencyNotation, configure)
        override fun <T : Dependency> add(dependency: T, configure: T.() -> Unit): T = implementation(dependency, configure)
    }

val KotlinDependencyHandler.runtimeOnly: KotlinDependencyHandlerConfiguration
    get() = object : KotlinDependencyHandlerConfiguration, KotlinDependencyHandler by this {
        override fun add(dependencyNotation: Any): Dependency? = runtimeOnly(dependencyNotation)
        override fun add(dependencyNotation: String, configure: ExternalModuleDependency.() -> Unit): ExternalModuleDependency = runtimeOnly(dependencyNotation, configure)
        override fun <T : Dependency> add(dependency: T, configure: T.() -> Unit): T = runtimeOnly(dependency, configure)
    }

val KotlinDependencyHandler.compileOnly: KotlinDependencyHandlerConfiguration
    get() = object : KotlinDependencyHandlerConfiguration, KotlinDependencyHandler by this {
        override fun add(dependencyNotation: Any): Dependency? = compileOnly(dependencyNotation)
        override fun add(dependencyNotation: String, configure: ExternalModuleDependency.() -> Unit): ExternalModuleDependency = compileOnly(dependencyNotation, configure)
        override fun <T : Dependency> add(dependency: T, configure: T.() -> Unit): T = compileOnly(dependency, configure)
    }
