import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
    kotlin("jvm")
    alias(libs.plugins.kmp.serialization)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.kmp.binary.compability.validator)
}

group = "io.github.gerardorodriguezdev.chamaleon"
version = libs.versions.release.get()

kotlin {
    explicitApi = ExplicitApiMode.Strict

    compilerOptions {
        extraWarnings = true
    }

    jvmToolchain(libs.versions.java.get().toInt())

    dependencies {
        implementation(libs.kmp.serialization)
        implementation(libs.kmp.coroutines)
        implementation(libs.kmp.arrow.core)

        testImplementation(libs.kmp.test)
        testImplementation(libs.kmp.test.coroutines)
        testImplementation(libs.jvm.test.parameterized)
    }
}

mavenPublishing {
    coordinates(
        groupId = group.toString(),
        artifactId = "chamaleon-core",
        version = version.toString()
    )

    pom {
        name = "Chamaleon Core"
        description = "Chamaleon core library to parse chamaleon files"
        url = "https://github.com/gerardorodriguezdev/chamaleon"

        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "gerardorodriguezdev"
                name = "Gerardo Rodriguez"
                url = "https://github.com/gerardorodriguezdev"
            }
        }
        scm {
            url = "https://github.com/gerardorodriguezdev/chamaleon"
            connection = "scm:git:git://github.com/gerardorodriguezdev/chamaleon.git"
            developerConnection = "scm:git:ssh://github.com/gerardorodriguezdev/chamaleon.git"
        }
    }

    publishToMavenCentral(host = SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)

    signAllPublications()
}

tasks {
    test {
        useJUnitPlatform()
    }
}

val versionsDirectory = layout.buildDirectory.dir("generated/versions")
val releaseVersion = libs.versions.release
val generateVersionsClassTaskName = "generateVersionsClass"
tasks.register(generateVersionsClassTaskName) {
    dependsOn(tasks.named("sourcesJar"))

    val versionsDirectory = versionsDirectory
    val releaseVersion = releaseVersion
    outputs.dir(versionsDirectory)

    doLast {
        val file = versionsDirectory.get().file("Versions.kt").asFile
        file.writeText(
            """
            package io.github.gerardorodriguezdev.chamaleon.core

            public object Versions {
                public const val CORE: String = "${releaseVersion.get()}"
            }
            """.trimIndent()
        )
    }
}

tasks.named("compileKotlin") {
    dependsOn(generateVersionsClassTaskName)
}

sourceSets {
    main {
        java {
            srcDir(versionsDirectory)
        }
    }
}