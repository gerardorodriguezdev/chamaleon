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
            compose.desktop.currentOs,
            compose.preview,
        ).forEach { dependency ->
            implementation(dependency) {
                exclude(group = "org.jetbrains.kotlinx")
                exclude(group = "org.jetbrains.compose.material")
            }
        }
        implementation(libs.intellij.jewel.standalone)
        implementation(libs.jvm.coroutines)
        implementation(projects.ui)
    }
}