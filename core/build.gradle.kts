plugins {
    kotlin("jvm")
    alias(libs.plugins.kmp.serialization)
}

kotlin {
    compilerOptions {
        extraWarnings.set(true)
        allWarningsAsErrors.set(true)
    }

    jvmToolchain(libs.versions.jvm.get().toInt())

    dependencies {
        implementation(libs.kmp.serialization)

        testImplementation(libs.kmp.test)
    }
}

tasks {
    test {
        useJUnitPlatform()
    }
}