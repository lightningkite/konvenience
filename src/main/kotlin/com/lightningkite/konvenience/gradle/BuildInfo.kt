package com.lightningkite.konvenience.gradle

object BuildInfo {
    val onceMessages = HashSet<String>()
    val usingLocalProjects = HashSet<String>()
    val usingRemoteProjects = HashSet<String>()
    val targets = HashSet<String>()
    fun print() {
        for (thing in onceMessages) {
            println(thing)
        }
        if (usingLocalProjects.isNotEmpty()) {
            println(usingLocalProjects.joinToString(", ", "Using local projects: "))
        }
        if (usingRemoteProjects.isNotEmpty()) {
            println(usingRemoteProjects.joinToString(", ", "Using remote projects: "))
        }
        if (targets.isNotEmpty()) {
            println(targets.joinToString(", ", "Configuring targets: "))
        }

        onceMessages.clear()
        usingLocalProjects.clear()
        usingRemoteProjects.clear()
        targets.clear()
    }
}