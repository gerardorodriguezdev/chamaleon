package io.github.gerardorodriguezdev.chamaleon.core.safeCollections

//TODO: Could this be closed?
public class ExistingFile(
    public val name: NonEmptyString,
    public val path: NonEmptyString,
    private val readContentDelegate: () -> String,
    private val writeContentDelegate: (String) -> Unit,
) {
    public fun readContent(): String = readContentDelegate()

    public fun writeContent(content: String) {
        writeContentDelegate(content)
    }
}