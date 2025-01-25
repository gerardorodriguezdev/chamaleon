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
        production.environment.chamaleon.json
```

The plugin will see this files and extract your environments properties

You can see a sample on [Sample Gradle Project](../samples/gradle-project)

### 3. Use your environments properties on your module's `build.gradle.kts` file

```kotlin
val myPropertyValue = chamaleon.selectedEnvironment().jvmPlatform().propertyStringValue("YourPropertyName")

// When building the project this should print `YourPropertyValueForLocalEnvironment`
println(myPropertyValue)
```

Is as simple as that to get started :)

## Files

> The json schemas for this files have been uploaded to [Schema store](https://www.schemastore.org/json/) so you can see
> hints if the schema is valid or not and have auto-completions as well. Alternatively you can find them
> here [Schemas](../schemas)

### `properties.chamaleon.json` file

This file will be used to select an environment. In this case `local`

```json
{
  "selectedEnvironmentName": "local"
}
```

### `template.chamaleon.json` file

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

### `local.environment.chamaleon.json` file

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

Usually on mobile applications you can't pass arguments through the command line, so you would need to generate a file
that will be inside the app with the properties of your environment. We are going to use `BuildKonfig` as it does that
for our Kotlin Multiplatform project

```kotlin
// A default configuration is required to use `BuildKonfig`
defaultConfigs {
    buildConfigField(
        FieldSpec.Type.STRING,
        name = "HOST",
        value = ""
    )
}

targetConfigs {
    // This will bring the environment selected on `properties.chamaleon.json` file
    val selectedEnvironment = chamaleon.selectedEnvironment()

    create("wasm") {
        // Get the property for the wasm platform
        val host = selectedEnvironment.wasmPlatform().propertyStringValue("HOST")
        buildConfigField(FieldSpec.Type.STRING, name = "HOST", value = host)
    }

    create("android") {
        // Get the property for the android platform
        val host = selectedEnvironment.androidPlatform().propertyStringValue("HOST")
        buildConfigField(FieldSpec.Type.STRING, name = "HOST", value = host)
    }
}
```

You can easily integrate with other solutions that generate your files as well

### Using properties on a server like [Ktor](https://github.com/ktorio/ktor)

```kotlin
tasks.named<JavaExec>("run") {
    // This will bring the environment selected on `properties.chamaleon.json` file
    val selectedEnvironment = chamaleon.selectedEnvironment()

    // Iterate through all jvm platform properties
    selectedEnvironment.jvmPlatform().properties.forEach { property ->

        // Add each property to the jvm environment when running the server with the `run` task
        environment(property.name, property.value.toString())
    }
}
```

### Working locally and on CI

Usually when working locally you would have some `local` or `staging` environment, and on CI you would have a
`production` environment that contains all your secrets. These are the steps to setup your workflow on CI:

#### 1. Add your production environment file to .gitignore

Add `myProductionEnvironment.environment.chamaleon.json` to `.gitignore` in your root project, so you are sure you'll
never commit this file to your repository

#### 2. Create the production environment file with your secrets on CI only from the command line

You would need to generate your production environment file programmatically, as it would need to accept your CI
secrets. Here is an example on how to do it:

`./gradlew :chamaleonGenerateEnvironment -Pchamaleon.environment="myProductionEnvironment.jvm.properties[mySecretName=mySecretValue]"`

The input command, in this case `myProductionEnvironment.jvm.properties[mySecretName=mySecretValue]`, has the following
structure:

```text
environmentName.platformType.properties[propertyName=propertyValue,otherPropertyName=otherPropertyValue]
```

After running this task, your production environment file will be generated on your module's root

> If you want to create an environment with more than one platform, you can run the same command with each of the
> additional platforms, and they will be merged in the environment
>
> ./gradlew :chamaleonGenerateEnvironment
>
> -Pchamaleon.environment="myProductionEnvironment.jvm.properties[mySecretName=mySecretValue]"
>
> -Pchamaleon.environment="myProductionEnvironment.android.properties[mySecretName=mySecretValue]"
>
> This would create an environment with `jvm` and `android` platforms

#### 3. Select the production environment file on CI from the command line

Finally, the only remaining thing to do is to select the generated environment like this:

`./gradlew :chamaleonSelectEnvironment -Pchamaleon.newSelectedEnvironment=myProductionEnvironment`

#### Complete example on Github Actions

```yaml
    - name: Generate and select production environment # Your step name
      run: |
        ./gradlew :chamaleonGenerateEnvironment -Pchamaleon.environment="myProductionEnvironment.jvm.properties[mySecretName=${{ secrets.MY_GITHUB_SECRET }}]"
        ./gradlew :chamaleonSelectEnvironment -Pchamaleon.newSelectedEnvironment=myProductionEnvironment
```

### Restricting some properties only for certain platforms

If there are properties only relevant to certain platforms, you can select which platforms should contain this property
in the `template.chamaleon.json` file

```json
{
  "supportedPlatforms": [
    "jvm",
    "android"
  ],
  "propertyDefinitions": [
    {
      "name": "HOST",
      "propertyType": "String",
      "nullable": false,
      "supportedPlatforms": [
        "android"
      ]
    }
  ]
}
```

In this case, the `HOST` property will only be present on the `android` platform and not in any other platform