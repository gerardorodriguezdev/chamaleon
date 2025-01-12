import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
    kotlin("jvm")
    alias(libs.plugins.kmp.serialization)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.kmp.binary.compability.validator)
}

group = "io.github.gerardorodriguezdev.chamaleon"
version = libs.versions.release.get()

kotlin {
    explicitApi = ExplicitApiMode.Strict

    compilerOptions {
        extraWarnings = true
        allWarningsAsErrors = true
    }

    jvmToolchain(libs.versions.java.get().toInt())

    dependencies {
        implementation(libs.kmp.serialization)
        implementation(libs.kmp.coroutines)

        testImplementation(libs.kmp.test)
        testImplementation(libs.kmp.test.coroutines)
    }
}

mavenPublishing {
    coordinates(
        groupId = group.toString(),
        artifactId = "chamaleon-core",
        version = version.toString()
    )

    pom {
        name = "Chamaleon Core"
        description = "Chamaleon core library to parse chamaleon files"
        url = "https://github.com/gerardorodriguezdev/chamaleon"

        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "gerardorodriguezdev"
                name = "Gerardo Rodriguez"
                url = "https://github.com/gerardorodriguezdev"
            }
        }
        scm {
            url = "https://github.com/gerardorodriguezdev/chamaleon"
            connection = "scm:git:git://github.com/gerardorodriguezdev/chamaleon.git"
            developerConnection = "scm:git:ssh://github.com/gerardorodriguezdev/chamaleon.git"
        }
    }

    publishToMavenCentral(host = SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)

    signAllPublications()
}

tasks {
    test {
        useJUnitPlatform()
    }
}