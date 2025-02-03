rootProject.name = "Chamaleon"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
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
    ":intellij-plugin",
    ":ui",
    ":standalone",
)