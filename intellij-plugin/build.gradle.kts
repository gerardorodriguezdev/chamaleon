import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    kotlin("jvm")
    alias(libs.plugins.intellij)
}

group = "io.github.gerardorodriguezdev.chamaleon"
version = libs.versions.release.get()

repositories {
    mavenCentral()

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
        implementation(libs.kmp.serialization)

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