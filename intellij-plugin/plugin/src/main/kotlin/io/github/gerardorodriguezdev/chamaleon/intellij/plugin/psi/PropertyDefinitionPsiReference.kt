package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.psi

import arrow.core.Option
import arrow.core.raise.option
import com.intellij.extapi.psi.ASTDelegatePsiElement
import com.intellij.json.psi.JsonArray
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonValue
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.PsiTreeUtil
import io.github.gerardorodriguezdev.chamaleon.core.models.Project.Companion.ENVIRONMENTS_DIRECTORY_NAME
import io.github.gerardorodriguezdev.chamaleon.core.models.Project.Companion.SCHEMA_FILE
import org.jetbrains.kotlin.psi.KtStringTemplateExpression

class PropertyDefinitionPsiReference(element: KtStringTemplateExpression) :
    PsiReferenceBase<KtStringTemplateExpression>(element, true) {

    override fun resolve(): PsiElement? =
        option {
            val project = element.project

            val schemaPsiFile = project.schemaPsiFile().bind()
            val schemaJsonFile = (schemaPsiFile as? JsonFile).bind()

            val schemaJsonObject = PsiTreeUtil.findChildOfType(
                schemaJsonFile,
                JsonObject::class.java
            ).bind()

            val propertyDefinitionsJsonValue = schemaJsonObject.findProperty("propertyDefinitions")?.value.bind()

            propertyDefinitionPsiElement(
                propertyDefinitionValueToFind = element.cleanedText(),
                propertyDefinitionsJsonArray = (propertyDefinitionsJsonValue as? JsonArray).bind(),
            ).bind()
        }.getOrNull()

    private fun Project.schemaPsiFile(): Option<PsiFile> =
        option {
            val module = ModuleUtilCore.findModuleForPsiElement(element).bind()
            val moduleRootManager = ModuleRootManager.getInstance(module)
            val virtualFiles = moduleRootManager.contentRoots
            schemaPsiFile(this@schemaPsiFile, virtualFiles).bind()
        }

    private fun schemaPsiFile(
        project: Project,
        virtualFiles: Array<VirtualFile>,
    ): Option<PsiFile> =
        option {
            val schemaFileRelativePath = "$ENVIRONMENTS_DIRECTORY_NAME/$SCHEMA_FILE"
            val schemaVirtualFile = virtualFiles.firstNotNullOfOrNull { root ->
                VfsUtil.findRelativeFile(schemaFileRelativePath, root)
            }.bind()
            PsiManager.getInstance(project).findFile(schemaVirtualFile).bind()
        }

    private fun propertyDefinitionPsiElement(
        propertyDefinitionValueToFind: String,
        propertyDefinitionsJsonArray: JsonArray,
    ): Option<PsiElement> =
        option {
            propertyDefinitionsJsonArray
                .valueList
                .firstOrNull { propertyDefinition ->
                    val propertyDefinitionJsonObject = propertyDefinition as? JsonObject
                    val propertyDefinitionJsonProperty = propertyDefinitionJsonObject?.findProperty("name")
                    propertyDefinitionJsonProperty?.value?.cleanedText() == propertyDefinitionValueToFind
                }
                .bind()
                .firstChild
        }

    private fun String.clean(): String = removeSurrounding("\"")

    private fun ASTDelegatePsiElement.cleanedText(): String = text.clean()

    private fun JsonValue.cleanedText(): String = text.clean()

    override fun getVariants(): Array<Any> = emptyArray()
}