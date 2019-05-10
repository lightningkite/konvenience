package com.lightningkite.konvenience.gradle

import okhttp3.*
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import java.lang.IllegalStateException
import java.net.URI

fun PublishingExtension.appendToPoms(action: MavenPom.() -> Unit) {
    publications.asSequence()
            .mapNotNull { it as? MavenPublication }
            .map { it.pom }
            .forEach { with(it, action) }
}

fun MavenPom.github(owner: String, repositoryName: String) {
    url.set("https://github.com/$owner/$repositoryName")
    scm {
        it.url.set("https://github.com/$owner/$repositoryName.git")
    }
    issueManagement {
        it.system.set("GitHub")
        it.url.set("https://github.com/$owner/$repositoryName/issues")
    }
}

fun MavenPom.licenseMIT() {
    licenses {
        it.license {
            it.name.set("MIT License")
            it.url.set("http://www.opensource.org/licenses/mit-license.php")
            it.distribution.set("repo")
        }
    }
}

fun RepositoryHandler.bintray(
        project: Project,
        organization: String,
        repository: String
): MavenArtifactRepository? {
    val username = project.localProperties.getProperty("bintrayUser") ?: System.getenv("BINTRAY_USER") ?: return null
    val key = project.localProperties.getProperty("bintrayKey") ?: project.localProperties.getProperty("bintrayApiKey")
    ?: System.getenv("BINTRAY_API_KEY") ?: return null

    project.task("releaseBintray") {
        it.dependsOn("publish")
        it.doLast {
            val req = Request.Builder()
                    .post(RequestBody.create(MediaType.parse("application/json"), """{"publish_wait_for_secs":15}"""))
                    .url("https://api.bintray.com/content/$organization/$repository/${project.name}/${project.version}/publish")
                    .addHeader("Authorization", Credentials.basic(username, key))
                    .build()
            val client = OkHttpClient.Builder().build()
            val result = client.newCall(req).execute()
            if (!result.isSuccessful) {
                throw IllegalStateException("Bintray release request failed.")
            }
        }
    }

    return maven {
        it.name = "Bintray"
        it.credentials {
            it.username = username
            it.password = key
        }
        it.url = URI("https://api.bintray.com/maven/$organization/$repository/${project.name}")
    }
}
