package org.chamaleon.core.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import org.chamaleon.core.dtos.PlatformDto.PropertyDto
import org.chamaleon.core.models.PropertyValue

internal object PropertyDtoSerializer : KSerializer<PropertyDto> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("PropertyDto") {
        element<String>(elementName = "name")
        element<JsonElement>(elementName = "value", isOptional = true)
    }

    override fun serialize(
        encoder: Encoder,
        value: PropertyDto
    ) {
        val jsonEncoder =
            encoder as? JsonEncoder ?: throw SerializationException("This class can only be serialized to JSON")

        val jsonObject = buildJsonObject {
            if (value.name.isEmpty()) throw SerializationException("PropertyDto name was empty")
            put(key = "name", element = JsonPrimitive(value.name))

            value.value?.let { propertyValue ->
                put(
                    key = "value",
                    element = when (propertyValue) {
                        is PropertyValue.StringProperty -> {
                            if (propertyValue.value.isEmpty()) throw SerializationException("StringProperty value was empty")
                            JsonPrimitive(propertyValue.value)
                        }

                        is PropertyValue.BooleanProperty -> JsonPrimitive(propertyValue.value)
                    }
                )
            }
        }

        jsonEncoder.encodeJsonElement(jsonObject)
    }

    override fun deserialize(decoder: Decoder): PropertyDto {
        val jsonDecoder =
            decoder as? JsonDecoder ?: throw SerializationException("This class can only be deserialized from JSON")

        val jsonObject = jsonDecoder.decodeJsonElement().jsonObject

        val name = jsonObject["name"]?.jsonPrimitive?.content ?: throw SerializationException("Missing name property")
        if (name.isEmpty()) throw SerializationException("PropertyDto name was empty")

        val valueElement = jsonObject["value"]
        val value = when {
            valueElement is JsonNull -> null
            valueElement?.jsonPrimitive?.isString == true -> {
                val stringValue = valueElement.jsonPrimitive.content
                if (stringValue.isEmpty()) throw SerializationException("StringProperty value was empty")
                PropertyValue.StringProperty(stringValue)
            }

            valueElement?.jsonPrimitive?.booleanOrNull != null ->
                PropertyValue.BooleanProperty(valueElement.jsonPrimitive.boolean)

            else -> throw SerializationException("Unsupported value type: $valueElement")
        }

        return PropertyDto(name = name, value = value)
    }
}
