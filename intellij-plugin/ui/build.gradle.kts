import org.jetbrains.compose.ExperimentalComposeLibrary

plugins {
    kotlin("jvm")
    alias(libs.plugins.kmp.compose)
    alias(libs.plugins.kmp.compose.compiler)
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
                exclude(group = "org.jetbrains.compose.material")
            }
        }
        implementation(libs.kmp.immutable)
        implementation(libs.intellij.jewel.ui)
        implementation(libs.intellij.jewel.standalone)
        implementation(compose.preview)
        implementation(libs.jvm.coroutines)
        implementation(projects.core)

        @OptIn(ExperimentalComposeLibrary::class)
        testImplementation(compose.uiTest)
        testImplementation(libs.kmp.test)
    }
}