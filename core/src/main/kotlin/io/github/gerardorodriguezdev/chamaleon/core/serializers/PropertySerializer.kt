package io.github.gerardorodriguezdev.chamaleon.core.serializers

import io.github.gerardorodriguezdev.chamaleon.core.models.Platform.Property
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyValue
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyValue.BooleanProperty
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyValue.StringProperty
import io.github.gerardorodriguezdev.chamaleon.core.safeCollections.NonEmptyString.Companion.toNonEmptyString
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

internal object PropertySerializer : KSerializer<Property> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Property") {
        element<String>("name")
        element<JsonElement>("value", isOptional = true)
    }

    override fun serialize(encoder: Encoder, value: Property) {
        if (encoder !is JsonEncoder) throw SerializationException("This class can only be serialized to JSON")
        val jsonObject = jsonObject(value)
        encoder.encodeJsonElement(jsonObject)
    }

    private fun jsonObject(property: Property): JsonObject =
        buildJsonObject {
            put(key = "name", element = JsonPrimitive(property.name.value))

            put(key = "value", element = property.value.toJsonElement())
        }

    private fun PropertyValue?.toJsonElement(): JsonElement =
        when (this) {
            null -> JsonNull
            is StringProperty -> JsonPrimitive(value.value)
            is BooleanProperty -> JsonPrimitive(value)
        }

    override fun deserialize(decoder: Decoder): Property {
        if (decoder !is JsonDecoder) throw SerializationException("This class can only be deserialized from JSON")

        val jsonObject = decoder.decodeJsonElement().jsonObject

        val name = jsonObject["name"]?.jsonPrimitive?.content
        val nonEmptyName = name?.toNonEmptyString()
        if (nonEmptyName == null) throw SerializationException("Property name was empty")

        val valueElement = jsonObject["value"]
        return Property(name = nonEmptyName, value = valueElement.toPropertyValueOrNull())
    }

    private fun JsonElement?.toPropertyValueOrNull(): PropertyValue? =
        when {
            this == null -> null
            this is JsonNull -> null
            jsonPrimitive.isString == true -> {
                val stringValue = jsonPrimitive.content
                val nonEmptyStringValue = stringValue.toNonEmptyString()
                if (nonEmptyStringValue == null) throw SerializationException("StringProperty value was empty")
                StringProperty(nonEmptyStringValue)
            }

            jsonPrimitive.booleanOrNull != null -> BooleanProperty(jsonPrimitive.boolean)
            else -> throw SerializationException("Unsupported value type: $this")
        }
}