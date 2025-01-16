# Chamaleon Gradle Plugin

Gradle plugin for [Chamaleon](../README.md)

## Quick start

### 1. Apply the plugin in your `build.gradle.kts`

```kotlin
plugins {
    id("io.github.gerardorodriguezdev.chamaleon")
}
```

> This plugin is hosted on
> the [Gradle Plugins Portal](https://plugins.gradle.org/plugin/io.github.gerardorodriguezdev.chamaleon)

### 2. Create the configuration files

Create a directory named `environments` in the root of your project. Inside this directory, create the following files:

```text
myProject
    environments
        template.chamaleon.json
        development.chamaleon.json
```

You can see a sample on [Sample Gradle Project](../samples/gradle-project)

### 3. Fill the `template.chamaleon.json`

This is the file that will be used to validate all the environment json files

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

- **supportedPlatforms:** Can be any or all of this platforms android, wasm, jvm or ios
- **propertyDefinitions:** It's an array of property definition
    - **propertyDefinition:**
        - **name:** The name of your property (cannot be an empty string) -> required
        - **propertyType:** Can be String or Boolean -> required
        - **nullable:** If the property is nullable or not (default=false)-> optional

### 4. Fill the `development.chamaleon.json`

Any json file named `anything.chamaleon.json` inside the `environments` directory will be considered an environment.
You can as many as you want with like `local.chamaleon.json` or `production.chamaleon.json`. For now, we added
`development.chamaleon.json`. Let's add this content to it:

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

- **platformType:** Can be any or all of this platforms android, wasm, jvm or ios
- **properties:** It's an array of property
    - **property:**
        - **name:** The name of your property (cannot be an empty string) required
        - **value:** The value of your property. Can be String, Boolean or null optional if it's nullable

## Using the plugin

Now that you have applied the plugin and added the required files you should be able to do this on the
`build.gradle.kts`

```kotlin
chamaleon.environments
```

## Selecting an environment

If you want to set an environment as the selected one, you can do it by adding `properties.chamaleon.json` file in
`environments` with this content:

```json
{
  "selectedEnvironmentName": "local"
}
```

## Using the json schemas

The json schemas have been uploaded to [Schema store](https://www.schemastore.org/json/) so you can see hints if the
schema is valid or not. Alternatively you can find them here [Schemas](../schemas)