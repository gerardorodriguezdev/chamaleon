plugins {
    `kotlin-dsl`
}

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
    plugins {
        register("org.chamaleon") {
            id = "org.chamaleon"
            implementationClass = "org.chamaleon.gradle.plugin.ChamaleonGradlePlugin"
        }
    }
}
