import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
    `kotlin-dsl`
    alias(libs.plugins.gradle.publish)
}

group = "io.github.gerardorodriguezdev.chamaleon"
version = libs.versions.release.get()

kotlin {
    explicitApi = ExplicitApiMode.Strict

    jvmToolchain(libs.versions.java.get().toInt())
}

dependencies {
    implementation(projects.core)
    implementation(libs.kmp.coroutines)
    implementation(libs.kmp.arrow.core)

    testImplementation(gradleTestKit())
    testImplementation(libs.kmp.test)
    testImplementation(libs.jvm.test.parameterized)
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }

    test {
        useJUnitPlatform()
    }
}

gradlePlugin {
    website = "https://github.com/gerardorodriguezdev/chamaleon/tree/master/gradle-plugin"
    vcsUrl = "https://github.com/gerardorodriguezdev/chamaleon"

    plugins {
        register("io.github.gerardorodriguezdev.chamaleon") {
            id = "io.github.gerardorodriguezdev.chamaleon"
            displayName = "Chamaleon"
            description = "Simplify managing multiple environments for any Kotlin project"

            tags = listOf(
                "kotlin",
                "kmp",
                "kotlin multiplatform",
                "environments",
                "configuration",
            )
            implementationClass = "io.github.gerardorodriguezdev.chamaleon.gradle.plugin.ChamaleonGradlePlugin"
        }
    }
}