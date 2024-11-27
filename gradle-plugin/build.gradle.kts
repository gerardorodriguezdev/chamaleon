plugins {
    `kotlin-dsl`
    alias(libs.plugins.gradle.publish)
}

group = "org.chamaleon"
version = "1.0.0"

kotlin {
    compilerOptions {
        allWarningsAsErrors.set(true)
    }

    jvmToolchain(libs.versions.jvm.get().toInt())
}

dependencies {
    implementation(projects.core)
    testImplementation(gradleTestKit())
    testImplementation(libs.kmp.test)
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
    website = "https://github.com/gerardorodriguezdev/chamaleon"
    vcsUrl = "https://github.com/gerardorodriguezdev/chamaleon"

    plugins {
        register("org.chamaleon") {
            id = "org.chamaleon"
            displayName = "Chamaleon"
            description = "Environments configurator"
            tags = listOf(
                "kotlin",
                "kmp",
                "kotlin multiplatform",

                "environments",
                "configuration",
            )
            implementationClass = "org.chamaleon.gradle.plugin.ChamaleonGradlePlugin"
        }
    }
}
