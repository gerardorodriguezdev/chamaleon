rootProject.name = "Chamaleon"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
        maven("https://packages.jetbrains.team/maven/p/kpm/public/")
    }
}

include(
    ":core",
    ":gradle-plugin",
    ":intellij-plugin:plugin",
    ":intellij-plugin:ui",
    ":intellij-plugin:presentation",
    ":intellij-plugin:shared",
)