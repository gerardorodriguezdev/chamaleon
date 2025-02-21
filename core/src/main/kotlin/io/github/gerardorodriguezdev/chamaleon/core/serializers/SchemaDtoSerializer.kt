package io.github.gerardorodriguezdev.chamaleon.core.serializers

import io.github.gerardorodriguezdev.chamaleon.core.dtos.SchemaDto
import io.github.gerardorodriguezdev.chamaleon.core.dtos.SchemaDto.PropertyDefinitionDto
import io.github.gerardorodriguezdev.chamaleon.core.entities.PlatformType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.*

internal object SchemaDtoSerializer : KSerializer<SchemaDto> {
    private const val SUPPORTED_PLATFORMS_INDEX = 0
    private const val PROPERTY_DEFINITIONS_DTOS_INDEX = 1
    private val supportedPlatformsSerializer = NonEmptySetSerializer(PlatformType.serializer())
    private val propertyDefinitionsDtosSerializer = NonEmptySetSerializer(PropertyDefinitionDto.serializer())

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("SchemaDto") {
        element("supportedPlatforms", supportedPlatformsSerializer.descriptor)
        element("propertyDefinitions", propertyDefinitionsDtosSerializer.descriptor)
    }

    override fun serialize(encoder: Encoder, schemaDto: SchemaDto) {
        schemaDto.propertyDefinitionsDtos.verify(schemaDto.globalSupportedPlatforms)

        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(
                descriptor = descriptor,
                index = SUPPORTED_PLATFORMS_INDEX,
                serializer = supportedPlatformsSerializer,
                value = schemaDto.globalSupportedPlatforms
            )

            encodeSerializableElement(
                descriptor = descriptor,
                index = PROPERTY_DEFINITIONS_DTOS_INDEX,
                serializer = propertyDefinitionsDtosSerializer,
                value = schemaDto.propertyDefinitionsDtos
            )
        }
    }

    override fun deserialize(decoder: Decoder): SchemaDto =
        decoder.decodeStructure(descriptor) {
            val globalSupportedPlatforms = globalSupportedPlatforms()
            val propertyDefinitionsDtos = propertyDefinitionsDtos()

            propertyDefinitionsDtos.verify(globalSupportedPlatforms)

            SchemaDto(
                globalSupportedPlatforms = globalSupportedPlatforms,
                propertyDefinitionsDtos = propertyDefinitionsDtos
            )
        }

    private fun CompositeDecoder.globalSupportedPlatforms(): Set<PlatformType> {
        verifyAndAdvanceIndex(SUPPORTED_PLATFORMS_INDEX)
        return decodeSerializableElement(
            descriptor = descriptor,
            index = SUPPORTED_PLATFORMS_INDEX,
            deserializer = supportedPlatformsSerializer
        )
    }

    private fun CompositeDecoder.propertyDefinitionsDtos(): Set<PropertyDefinitionDto> {
        verifyAndAdvanceIndex(PROPERTY_DEFINITIONS_DTOS_INDEX)
        return decodeSerializableElement(
            descriptor = descriptor,
            index = PROPERTY_DEFINITIONS_DTOS_INDEX,
            deserializer = propertyDefinitionsDtosSerializer,
        )
    }

    private fun CompositeDecoder.verifyAndAdvanceIndex(targetIndex: Int) {
        val index = decodeElementIndex(descriptor)
        if (index != targetIndex) throw SerializationException("Missing required fields at index $index")
    }

    private fun Set<PropertyDefinitionDto>.verify(globalSupportedPlatforms: Set<PlatformType>) {
        verifySupportedPlatforms(globalSupportedPlatforms)
        verifyUnique()
    }

    private fun Set<PropertyDefinitionDto>.verifySupportedPlatforms(globalSupportedPlatforms: Set<PlatformType>) {
        forEach { propertyDefinitionDto ->
            if (propertyDefinitionDto.containsUnsupportedPlatforms(globalSupportedPlatforms)) {
                throw SerializationException(
                    "Property definition '${propertyDefinitionDto.name}' contains unsupported platform"
                )
            }
        }
    }

    private fun PropertyDefinitionDto.containsUnsupportedPlatforms(globalSupportedPlatforms: Set<PlatformType>): Boolean =
        !globalSupportedPlatforms.containsAll(this.supportedPlatforms)

    private fun Set<PropertyDefinitionDto>.verifyUnique() {
        val uniqueNames = distinctBy { propertyDefinitionDto -> propertyDefinitionDto.name }
        if (uniqueNames.size != this.size) {
            throw SerializationException("Duplicated property definition")
        }
    }
}