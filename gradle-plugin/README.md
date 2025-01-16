# Chamaleon Gradle Plugin

Gradle plugin for [Chamaleon](../README.md)

## Quick start

### 1. Apply the plugin in your `build.gradle.kts`

```kotlin
plugins {
    id("io.github.gerardorodriguezdev.chamaleon") version "xxx" // Latest release version
}
```

> This plugin is hosted on
> the [Gradle Plugins Portal](https://plugins.gradle.org/plugin/io.github.gerardorodriguezdev.chamaleon)

### 2. Setup configuration files

Create a directory named `environments` in the root of your project and create the following empty files:

```text
myProject --> The root of your project
    environments --> The directory you created
        properties.chamaleon.json
        template.chamaleon.json
        local.environment.chamaleon.json
```

You can see a sample on [Sample Gradle Project](../samples/gradle-project)

> The json schemas for this files have been uploaded to [Schema store](https://www.schemastore.org/json/) so you can see
> hints if the schema is valid or not and have auto-completions as well. Alternatively you can find them
> here [Schemas](../schemas)

### 3. Fill the `properties.chamaleon.json`

This file will be used to select an environment. In this case `local`

```json
{
  "selectedEnvironmentName": "local"
}
```

### 4. Fill the `template.chamaleon.json`

This file will be used only to validate that all the environments have the same structure

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

- **supportedPlatforms:** Can be one or all the supported platforms `(android, wasm, ios, jvm)`
- **propertyDefinitions:** It's an array of property definition
    - **propertyDefinition:**
        - **name:** The name of your property (cannot be an empty string) -> `required`
        - **propertyType:** Can be `String` or `Boolean `-> `required`
        - **nullable:** If the property is `nullable` or not (default=false)-> `optional`

### 5. Fill the `local.environment.chamaleon.json`

This file is your first environment. Any json file with this suffix `.environment.chamaleon.json` inside the
`environments` directory will be considered an environment. You can have as many as you want

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

- **platformType:** Can be any of the supported platforms `(android, wasm, ios, jvm)` -> `required`
- **properties:** It's an array of property
    - **property:**
        - **name:** The name of your property (cannot be an empty string) -> `required`
        - **value:** The value of your property. Can be `String`, `Boolean` or `null` -> `optional` if it's nullable
          otherwise `required`

## 6. Using the plugin

Now that you have applied the plugin and added the required files you should be able to do this on the
`build.gradle.kts`

```kotlin
chamaleon.selectedEnvironmentName // The currently selectedEnvironmentName
chamaleon.environments // All the defined environments. You can search for one in particular or see it's properties
```