plugins {
    kotlin("jvm")
}

kotlin {
    compilerOptions {
        extraWarnings = true
    }

    jvmToolchain(libs.versions.java.get().toInt())

    dependencies {
        implementation(libs.jvm.coroutines)
        implementation(libs.kmp.arrow.core)
        implementation(projects.core)
        implementation(projects.intellijPlugin.shared)

        testImplementation(libs.kmp.test)
        testImplementation(libs.kmp.test.coroutines)
        testImplementation(libs.jvm.test.parameterized)
    }
}