package io.github.gerardorodriguezdev.chamaleon.core.serializers

import io.github.gerardorodriguezdev.chamaleon.core.dtos.PlatformDto.PropertyDto
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyValue
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyValue.BooleanProperty
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyValue.StringProperty
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

internal object PropertyDtoSerializer : KSerializer<PropertyDto> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("PropertyDto") {
        element<String>("name")
        element<JsonElement>("value", isOptional = true)
    }

    override fun serialize(encoder: Encoder, value: PropertyDto) {
        if (encoder !is JsonEncoder) throw SerializationException("This class can only be serialized to JSON")
        encoder.encodeJsonElement(
            jsonObject(value)
        )
    }

    private fun jsonObject(propertyDto: PropertyDto): JsonObject =
        buildJsonObject {
            if (propertyDto.name.isEmpty()) throw SerializationException("PropertyDto name was empty")
            put(key = "name", element = JsonPrimitive(propertyDto.name))

            put(key = "value", element = propertyDto.value.toJsonElement())
        }

    private fun PropertyValue?.toJsonElement(): JsonElement =
        when (this) {
            null -> JsonNull
            is StringProperty -> {
                if (value.isEmpty()) throw SerializationException("StringProperty value was empty")
                JsonPrimitive(value)
            }

            is BooleanProperty -> JsonPrimitive(value)
        }

    override fun deserialize(decoder: Decoder): PropertyDto {
        if (decoder !is JsonDecoder) throw SerializationException("This class can only be deserialized from JSON")

        val jsonObject = decoder.decodeJsonElement().jsonObject

        val name = jsonObject["name"]?.jsonPrimitive?.content
        if (name.isNullOrEmpty()) throw SerializationException("PropertyDto name was empty")

        val valueElement = jsonObject["value"]
        return PropertyDto(name = name, value = valueElement.toPropertyValueOrNull())
    }

    private fun JsonElement?.toPropertyValueOrNull(): PropertyValue? =
        when {
            this == null -> null
            this is JsonNull -> null
            jsonPrimitive.isString == true -> {
                val stringValue = jsonPrimitive.content
                if (stringValue.isEmpty()) throw SerializationException("StringProperty value was empty")
                StringProperty(stringValue)
            }

            jsonPrimitive.booleanOrNull != null -> BooleanProperty(jsonPrimitive.boolean)
            else -> throw SerializationException("Unsupported value type: $this")
        }
}