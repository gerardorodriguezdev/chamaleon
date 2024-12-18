# Chamaleon Core

Library that parses the `cha` files and returns the set of environments.
It's consumed by the [Gradle plugin](../gradle-plugin) and the [Intellij Plugin](../intellij-plugin)

## How it works?

Just create an EnvironmentsProcessor, and it will parse the directory provided and give you the environments if present

````kotlin
val file = File("MyDirectory")

val environmentsProcessor = DefaultEnvironmentsProcessor(file)

val environments = environmentsProcessor.environments()

val someEnvironment = environments.first()
````