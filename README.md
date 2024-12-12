# Chamaleon

Simplify the management of multiple environments for any Kotlin project

## Features

- Define multiple environments and switch between them
- Keep the same structure of all your environments
- Kotlin multiplatform support in mind from the creation

## What problems do this solve?

If you have a kotlin multiplatform app or server that has *staging*, *debug*, *qa*, *production* or more variants, this
plugin allows you to define in an easy way all the environment variables for each environment, check that each has the
same structure and switch between them easily, so you don't have to pass which environment to select on the command line
per task

For ease of use you should use both the [Gradle Plugin](gradle-plugin) and the [Intellij Plugin]()

## Projects

The project is separated into 3 different projects:

- **core:** The actual code that makes the magic [Core library](core)
- **gradle-plugin:** Gradle plugin to use Chamaleon [Gradle Plugin](gradle-plugin)
- **intellij-plugin:** Intellij plugin to switch between the different environments [Intellij Plugin]()