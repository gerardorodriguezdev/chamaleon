import io.gitlab.arturbosch.detekt.Detekt

//TODO: Are this needed?
plugins {
    alias(libs.plugins.kmp.kotlin) apply false
    alias(libs.plugins.kmp.serialization) apply false
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.intellij) apply false
    alias(libs.plugins.kmp.compose.api) apply false
    alias(libs.plugins.detekt)
}

detekt {
    buildUponDefaultConfig = true
    allRules = true
    config.from("detekt.yml")
    autoCorrect = true
}

dependencies {
    detektPlugins(libs.detekt.formatting)
    detektPlugins(libs.detekt.compose)
}

tasks.withType<Detekt> detekt@{
    setSource(files(project.projectDir))
    exclude("**/build/**")
}

tasks.register("releaseVersion") {
    println(libs.versions.release.get())
}