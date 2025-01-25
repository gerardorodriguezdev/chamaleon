# Chamaleon Gradle Plugin

Gradle plugin for [Chamaleon](../README.md)

## Quick start

### 1. Apply the plugin in your module's `build.gradle.kts` file

```kotlin
plugins {
    id("io.github.gerardorodriguezdev.chamaleon") version "xxx" // Latest release version
}
```

> This plugin is hosted on
> the [Gradle Plugins Portal](https://plugins.gradle.org/plugin/io.github.gerardorodriguezdev.chamaleon)

### 2. Run `./gradlew :chamaleonGenerateSample`

This task will generate the following files:

```text
myModule --> The root of your module
    environments --> The directory where this files will be created to
        properties.chamaleon.json
        template.chamaleon.json
        local.environment.chamaleon.json
```

The plugin will see this files and extract your environments properties

You can see a sample on [Sample Gradle Project](../samples/gradle-project)

### 3. Use your environments properties on your module's `build.gradle.kts` file

```kotlin
val myPropertyValue = chamaleon.selectedEnvironment().jvmPlatform().propertyStringValue("YourPropertyName")
println(myPropertyValue)
```

Is as simple as that to get started :)

## Files descriptions

> The json schemas for this files have been uploaded to [Schema store](https://www.schemastore.org/json/) so you can see
> hints if the schema is valid or not and have auto-completions as well. Alternatively you can find them
> here [Schemas](../schemas)

### `properties.chamaleon.json`

This file will be used to select an environment. In this case `local`

```json
{
  "selectedEnvironmentName": "local"
}
```

### `template.chamaleon.json`

This file will be used only to validate that all the environments have the same structure

```json
{
  "supportedPlatforms": [
    "jvm"
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

- **supportedPlatforms:** Can be one or all the supported platforms `(android, wasm, ios, jvm)`
- **propertyDefinitions:** It's an array of property definition
    - **propertyDefinition:**
        - **name:** The name of your property (cannot be an empty string) -> `required`
        - **propertyType:** Can be `String` or `Boolean `-> `required`
        - **nullable:** If the property is `nullable` or not (default=false)-> `optional`
      - **supportedPlatforms:** It's an array of `supportedPlatforms` that will override the global platforms on the
        template for this property only. Only read if is not empty  (default=[])-> `optional`

### `local.environment.chamaleon.json`

Any json file with this suffix `.environment.chamaleon.json` inside the `environments` directory will be considered an
environment. You can have as many as you want like `myEnvironmentName.environment.chamaleon.json`

```json
[
  {
    "platformType": "jvm",
    "properties": [
      {
        "name": "HOST",
        "value": "localhost"
      }
    ]
  }
]
```

- **platformType:** Can be any of the supported platforms `(android, wasm, ios, jvm)` -> `required`
- **properties:** It's an array of property
    - **property:**
        - **name:** The name of your property (cannot be an empty string) -> `required`
        - **value:** The value of your property. Can be `String`, `Boolean` or `null` -> `optional` if it's nullable
          otherwise `required`

## Recipes

### Using properties on generated files like [BuildKonfig](https://github.com/yshrsmz/BuildKonfig)

### Using properties on a server like [Ktor](https://github.com/ktorio/ktor)

### Working locally and on CI