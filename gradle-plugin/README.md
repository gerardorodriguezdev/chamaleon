# Chamaleon Gradle Plugin

Simplify the management of multiple environments for any Kotlin project

## Features

- Define multiple environments and swap between them
- Keep the same structure of all your environments
- Kotlin multiplatform support in mind from the creation

## Quick start

### 1. Apply the plugin in your `build.gradle.kts` file

```kotlin
plugins {
    id("org.chamaleon") version "1.0.0"
}
```

> This plugin is hosted on the [Gradle Plugins Portal](https://plugins.gradle.org/)

### 2. Create the configuration files

Create a directory named environments in the root of your project. Inside this directory, create the following files:

```text
myProject
    environments
        template.json
        development.json
```

You can see a sample on [Sample Gradle Project](../samples/gradle-project)

### 3. Fill the `template.json`

This is the file that will be used to validate all the **environment json files** follow this structure

```json
{
  "supportedPlatforms": [
    "android"
  ],
  "propertyDefinitions": [
    {
      "name": "HOST",
      "propertyType": "String",
      "nullable": false
    }
  ]
}
```

- **supportedPlatforms:** Can be any or all of this `android`, `wasm`, `jvm` or `ios`
- **propertyDefinitions:** It's an array of property definition
    - **propertyDefinition:**
        - **name:** The name of your property (cannot be an empty string) -> `required`
        - **propertyType:** Can be *String* or *Boolean* -> `required`
        - **nullable:** If the property is nullable or not (default=false)-> `optional`

### 4. Fill the `development.json`

Any json file named not named `template.json` inside the `environments` directory will be considered an environment.
You can as many as you want with any name like `local.json` or `production.json`. For now we added `development.json`.
Let's just add this content:

```json
[
  {
    "platformType": "android",
    "properties": [
      {
        "name": "HOST",
        "value": "localhost"
      }
    ]
  }
]
```

- **platformType:** Can be any or all of this `android`, `wasm`, `jvm` or `ios`
- **properties:** It's an array of property
    - **property:**
        - **name:** The name of your property (cannot be an empty string) `required`
        - **value:** The value of your property. Can be *String*, *Boolean* or *null* `optional if it's nullable`

## Using the plugin

Now that you have applied the plugin and added the required files you should be able to do this on the
`build.gradle.kts`

```kotlin
plugins {
    id("org.chamaleon") version "1.0.0"
}

chamaleon.environments // The environments are parsed and you can read the properties per environment and/or per platform
```

## Selecting an environment

If you want to set an environment as the selected one, you can do it by adding `chamaleon.local.properties` file in
`environments` with this content:

```properties
CHAMALEON_SELECTED_ENVIRONMENT=development
```

> If you have a `production.json` and want to test locally but avoid commiting it just add `production.json` to your
`.gitignore` file