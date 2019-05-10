package com.lightningkite.konvenience.gradle

import org.gradle.api.Project
import java.util.*

val Project.localProperties get() = extensions.getByName("localProperties") as Properties
