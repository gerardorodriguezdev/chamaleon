plugins {
    kotlin("jvm")
}

kotlin {
    compilerOptions {
        extraWarnings = true
    }

    jvmToolchain(libs.versions.java.get().toInt())

    dependencies {
        implementation(libs.kmp.immutable)
    }
}