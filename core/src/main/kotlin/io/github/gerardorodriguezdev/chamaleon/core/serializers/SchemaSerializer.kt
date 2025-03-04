package io.github.gerardorodriguezdev.chamaleon.core.serializers

import io.github.gerardorodriguezdev.chamaleon.core.models.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.models.Schema
import io.github.gerardorodriguezdev.chamaleon.core.models.Schema.Companion.schemaOf
import io.github.gerardorodriguezdev.chamaleon.core.models.Schema.PropertyDefinition
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyKeySetStore
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptySet
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.*

internal object SchemaSerializer : KSerializer<Schema> {
    private const val SUPPORTED_PLATFORMS_INDEX = 0
    private const val PROPERTY_DEFINITIONS_INDEX = 1

    private val supportedPlatformsSerializer = NonEmptySetSerializer(PlatformType.serializer())
    private val propertyDefinitionsSerializer = NonEmptyKeyStoreSerializer(
        keySerializer = String.serializer(),
        valueSerializer = PropertyDefinition.serializer(),
    )

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Schema") {
        element("supportedPlatforms", supportedPlatformsSerializer.descriptor)
        element("propertyDefinitions", propertyDefinitionsSerializer.descriptor)
    }

    override fun serialize(encoder: Encoder, schema: Schema) {
        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(
                descriptor = descriptor,
                index = SUPPORTED_PLATFORMS_INDEX,
                serializer = supportedPlatformsSerializer,
                value = schema.globalSupportedPlatformTypes
            )

            encodeSerializableElement(
                descriptor = descriptor,
                index = PROPERTY_DEFINITIONS_INDEX,
                serializer = propertyDefinitionsSerializer,
                value = schema.propertyDefinitions,
            )
        }
    }

    override fun deserialize(decoder: Decoder): Schema =
        decoder.decodeStructure(descriptor) {
            val globalSupportedPlatforms = globalSupportedPlatforms()
            val propertyDefinitions = propertyDefinitions()

            val schema = schemaOf(globalSupportedPlatforms, propertyDefinitions)
            if (schema == null) throw SerializationException("Property definition contains unsupported platforms")

            schema
        }

    private fun CompositeDecoder.globalSupportedPlatforms(): NonEmptySet<PlatformType> {
        verifyAndAdvanceIndex(SUPPORTED_PLATFORMS_INDEX)
        return decodeSerializableElement(
            descriptor = descriptor,
            index = SUPPORTED_PLATFORMS_INDEX,
            deserializer = supportedPlatformsSerializer
        )
    }

    private fun CompositeDecoder.propertyDefinitions(): NonEmptyKeySetStore<String, PropertyDefinition> {
        verifyAndAdvanceIndex(PROPERTY_DEFINITIONS_INDEX)
        return decodeSerializableElement(
            descriptor = descriptor,
            index = PROPERTY_DEFINITIONS_INDEX,
            deserializer = propertyDefinitionsSerializer,
        )
    }

    private fun CompositeDecoder.verifyAndAdvanceIndex(targetIndex: Int) {
        val index = decodeElementIndex(descriptor)
        if (index != targetIndex) throw SerializationException("Missing required fields at index $index")
    }
}