import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    kotlin("jvm")
    alias(libs.plugins.intellij)
    alias(libs.plugins.kmp.compose)
    alias(libs.plugins.kmp.compose.compiler)
}

group = "io.github.gerardorodriguezdev.chamaleon"
version = libs.versions.release.get()

repositories {
    google()
    mavenCentral()
    maven("https://packages.jetbrains.team/maven/p/kpm/public/")

    intellijPlatform {
        defaultRepositories()
    }
}

kotlin {
    compilerOptions {
        extraWarnings = true
    }

    jvmToolchain(libs.versions.java.get().toInt())

    dependencies {
        listOf(
            compose.desktop.linux_x64,
            compose.desktop.linux_arm64,
            compose.desktop.windows_x64,
            compose.desktop.macos_x64,
            compose.desktop.macos_arm64,
        ).forEach { dependency ->
            implementation(dependency) {
                exclude(group = "org.jetbrains.kotlinx")
                exclude(group = "org.jetbrains.compose.material")
            }
        }
        implementation(libs.intellij.jewel.laf)
        implementation(libs.jvm.coroutines) {
            exclude(group = "org.jetbrains.kotlinx")
        }
        implementation(libs.kmp.immutable) {
            exclude(group = "org.jetbrains.kotlinx")
        }
        implementation(projects.core) {
            exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core")
        }
        implementation(projects.intellijPlugin.ui) {
            exclude(group = "org.jetbrains.kotlinx")
        }

        testImplementation(libs.kmp.test)
        testImplementation(libs.kmp.test.coroutines)

        intellijPlatform {
            intellijIdeaCommunity("2023.3")

            pluginVerifier()
            zipSigner()
            testFramework(TestFrameworkType.Platform)
        }
    }
}

intellijPlatform {
    buildSearchableOptions = false

    pluginConfiguration {
        version = libs.versions.release.get()

        ideaVersion {
            sinceBuild = "233"
            untilBuild = "251.*"
        }
    }

    signing {
        certificateChain = providers.environmentVariable("JETBRAINS_CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("JETBRAINS_PRIVATE_KEY")
        password = providers.environmentVariable("JETBRAINS_PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token.set(providers.environmentVariable("JETBRAINS_PUBLISH_TOKEN"))
    }

    pluginVerification {
        ides {
            recommended()
        }
    }
}

intellijPlatformTesting {
    runIde {
        register("runIdeForUiTests") {
            task {
                jvmArgumentProviders += CommandLineArgumentProvider {
                    listOf(
                        "-Drobot-server.port=8082",
                        "-Dide.mac.message.dialogs.as.sheets=false",
                        "-Djb.privacy.policy.text=<!--999.999-->",
                        "-Djb.consents.confirmation.enabled=false",
                    )
                }
            }

            plugins {
                robotServerPlugin()
            }
        }
    }
}

val GENERATE_VERSIONS_CLASS_TASK_NAME = "generateVersionsClass"
tasks.register(GENERATE_VERSIONS_CLASS_TASK_NAME) {
    val versionsDirectory = project.versionsDirectory()
    outputs.dir(versionsDirectory)

    doLast {
        val file = versionsDirectory.get().file("Versions.kt").asFile
        file.writeText(
            """
            package io.github.gerardorodriguezdev.chamaleon.intellij.plugin

            object Versions {
                const val GRADLE_PLUGIN: String = "${libs.versions.release.get()}"
            }
            """.trimIndent()
        )
    }
}

tasks.named("compileKotlin") {
    dependsOn(GENERATE_VERSIONS_CLASS_TASK_NAME)
}

sourceSets {
    main {
        java {
            srcDir(project.versionsDirectory())
        }
    }
}

fun Project.versionsDirectory(): Provider<Directory> = layout.buildDirectory.dir("generated/versions")