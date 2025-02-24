package io.github.gerardorodriguezdev.chamaleon.core.mappers

import io.github.gerardorodriguezdev.chamaleon.core.dtos.PlatformDto
import io.github.gerardorodriguezdev.chamaleon.core.dtos.PlatformDto.PropertyDto
import io.github.gerardorodriguezdev.chamaleon.core.dtos.SchemaDto
import io.github.gerardorodriguezdev.chamaleon.core.dtos.SchemaDto.PropertyDefinitionDto
import io.github.gerardorodriguezdev.chamaleon.core.models.Platform
import io.github.gerardorodriguezdev.chamaleon.core.models.Platform.Property
import io.github.gerardorodriguezdev.chamaleon.core.models.Schema
import io.github.gerardorodriguezdev.chamaleon.core.models.Schema.PropertyDefinition
import io.mcarle.konvert.api.Konvert
import io.mcarle.konvert.api.Konverter
import io.mcarle.konvert.api.Mapping

@Konverter
internal interface PropertyMapper {
    fun toDto(property: Property): PropertyDto
    fun toModel(propertyDto: PropertyDto): Property
}

@Konverter
internal interface PlatformMapper {
    fun toDto(platform: Platform): PlatformDto
    fun toModel(platformDto: PlatformDto): Platform
}

@Konverter
internal interface PropertyDefinitionMapper {
    fun toDto(propertyDefinition: PropertyDefinition): PropertyDefinitionDto
    fun toModel(propertyDefinitionDto: PropertyDefinitionDto): PropertyDefinition
}

@Konverter
internal interface PropertyDefinitionsMapper {
    fun toDtos(propertyDefinitions: Set<PropertyDefinition>): Set<PropertyDefinitionDto>
    fun toModels(propertyDefinitionsDtos: Set<PropertyDefinitionDto>): Set<PropertyDefinition>
}

@Konverter
internal interface SchemaMapper {
    @Konvert(
        mappings = [
            Mapping(
                source = "propertyDefinitions",
                target = "propertyDefinitionsDtos",
            )
        ]
    )
    fun toDto(schema: Schema): SchemaDto

    @Konvert(
        mappings = [
            Mapping(
                source = "propertyDefinitionsDtos",
                target = "propertyDefinitions",
            )
        ]
    )
    fun toModel(schemaDto: SchemaDto): Schema
}