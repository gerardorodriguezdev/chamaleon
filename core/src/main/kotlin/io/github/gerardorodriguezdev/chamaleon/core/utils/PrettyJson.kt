package io.github.gerardorodriguezdev.chamaleon.core.utils

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
internal val PrettyJson = Json {
    prettyPrint = true
    prettyPrintIndent = "  "
}