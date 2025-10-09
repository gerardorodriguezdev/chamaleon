import org.jetbrains.compose.ExperimentalComposeLibrary

plugins {
    kotlin("jvm")
    alias(libs.plugins.kmp.compose.api)
    alias(libs.plugins.kmp.compose.compiler)
}

kotlin {
    compilerOptions {
        extraWarnings = true
    }

    jvmToolchain(libs.versions.java.get().toInt())

    dependencies {
        compileOnly(libs.intellij.jewel.standalone)
        compileOnly(libs.intellij.jewel.ui)
        compileOnly(compose.preview)
        implementation(libs.kmp.immutable)
        implementation(libs.jvm.coroutines)
        implementation(projects.intellijPlugin.shared)

        @OptIn(ExperimentalComposeLibrary::class)
        testImplementation(compose.uiTest)
        testImplementation(libs.kmp.test)
    }
}