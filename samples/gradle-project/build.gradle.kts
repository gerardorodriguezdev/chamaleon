plugins {
    id("io.github.gerardorodriguezdev.chamaleon")
}

val myPropertyValue = chamaleon.selectedEnvironment().jvmPlatform.propertyStringValue("YourPropertyName")
println(myPropertyValue)