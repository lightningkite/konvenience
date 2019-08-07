package com.lightningkite.konvenience.gradle

val isSyncingIntelliJ: Boolean get() = System.getProperty("idea.resolveSourceSetDependencies") != null