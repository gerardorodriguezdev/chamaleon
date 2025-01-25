# Chamaleon

<img src="/assets/Logo.svg" alt="Chamaleon logo" width="100" height="100">

Simplify the management of multiple environments for any Kotlin project

## What is Chamaleon?

It's a [Gradle plugin](gradle-plugin/README.md) that allows you to:

- Switch between environments easily `(qa, debug, prod, or anything really)`
- Define multiple environments in a consistent way between different types of projects (even mobile apps or backend
  services)
- Use in any Kotlin project (multiplatform as well)

Additionally, there is an [Intellij Plugin](intellij-plugin/README.md) that makes it easier to switch environments

## How to use start?

1. Set up the [Gradle Plugin](gradle-plugin/README.md)
2. (Optional) Install the [IntelliJ Plugin](intellij-plugin/README.md)

## What problems does it solve?

### Switching between environments

If you have multiple environments like `staging, debug, qa, production` or some custom one, it becomes a pain to
switch between them as you would usually need to pass some arguments to each `task` that you run or have multiple
`run configurations` with those arguments saved there

### Different way of working with environments between different projects

If you a mobile app with multiple environments, you usually need to generate a file depending on which environment
you want to configure, but for backend services you would need to expose the environment contents by environment
variables, having 2 different ways when working with environments

### Environment variables all over the place

It's usually not explicit what the environment variables you need to run or configure a project