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
        allWarningsAsErrors = true
    }

    jvmToolchain(libs.versions.jvm.get().toInt())

    dependencies {
        implementation(libs.jvm.coroutines) {
            exclude(
                group = "org.jetbrains.kotlinx",
                module = "kotlinx-coroutines-core"
            )
        }
        implementation(compose.desktop.currentOs) {
            exclude(group = "org.jetbrains.compose.material")
            exclude(group = "org.jetbrains.kotlinx")
        }
        implementation(libs.intellij.jewel) { exclude(group = "org.jetbrains.kotlinx") }

        implementation(projects.core)

        testImplementation(libs.kmp.test)

        intellijPlatform {
            intellijIdeaCommunity("2023.3")

            pluginVerifier()
            zipSigner()
            testFramework(TestFrameworkType.Platform)
        }
    }
}

intellijPlatform {
    pluginConfiguration {
        version = libs.versions.release.get()

        ideaVersion {
            sinceBuild = "233"
            untilBuild = "242.*"
        }
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