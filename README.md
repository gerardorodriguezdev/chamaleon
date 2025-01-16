# Chamaleon

Simplify the management of multiple environments for any Kotlin project

## Features

- Switch between environments easily (qa, debug, prod, or anything really)
- Define multiple environments in a consistent way between different types of projects (even mobile apps or services)
- Works great for any Kotlin project (multiplatform as well)

## What problems does Chamaleon solve?

### Switching between environments

If you have multiple environments like staging, debug, qa, production or some custom one, it becomes a pain to
switch between them as you would usually need to pass some arguments to each task that you run or have multiple run
configurations with those arguments saved there

### Different way of working with environments between different projects

If you have multiple environments for a mobile app, you usually need to generate a file depending on which environment
you want to configure, but for services you would need to expose the environment contents by environment variables

### Environment variables all over the place

It's usually not explicit what the environment variables you need or are used in the project

## How to use?

1. Install the [Chamaleon Gradle Plugin](gradle-plugin/README.md)
2. (Optional) Install the [Chamaleon IntelliJ Plugin](intellij-plugin/README.md)

## FAQ

### How to contribute?

- Create a branch from master
- Add the code changes there
- Send a PR to merge with master
- If all good the maintainers will merge the PR

### How to release?

- Create a branch from master
- Update the release version on the [release version](/gradle/libs.versions.toml) in this file using semantic versioning
- Send a PR to merge with release
- If all good the maintainers will merge the PR
- When this happens a deployment will be done
- After the deployment is finished it will upload the artifacts and create the GitHub release

### How to update the jvm?

- Update the jvm version on the [jvm version](/gradle/libs.versions.toml) in this file
- Update the jvm version on the [java-version](.github/actions/setup-action/action.yml) in this file