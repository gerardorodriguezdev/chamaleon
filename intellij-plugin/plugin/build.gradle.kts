import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.tasks.RunIdeTask

plugins {
    kotlin("jvm")
    alias(libs.plugins.intellij)
    alias(libs.plugins.kmp.compose.api)
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
        implementation(projects.intellijPlugin.shared) {
            exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core")
        }
        implementation(projects.intellijPlugin.ui) {
            exclude(group = "org.jetbrains.kotlinx")
        }
        implementation(projects.intellijPlugin.presentation) {
            exclude(group = "org.jetbrains.kotlinx")
        }
        implementation(libs.kmp.arrow.core) {
            exclude(group = "org.jetbrains.kotlinx")
        }

        testImplementation(libs.kmp.test)
        testImplementation(libs.kmp.test.coroutines)

        intellijPlatform {
            intellijIdeaCommunity("2025.2.3")

            bundledPlugins("org.jetbrains.kotlin", "com.intellij.modules.json")

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
            sinceBuild = "251"
            untilBuild = "252.*"
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
        register(
            "runIdeForUiTests",
            Action {
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
        )
    }
}

tasks.named<RunIdeTask>("runIde") {
    jvmArgumentProviders += CommandLineArgumentProvider {
        listOf("-Didea.kotlin.plugin.use.k2=true")
    }
}