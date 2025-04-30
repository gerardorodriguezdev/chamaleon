package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.psi

import com.intellij.json.psi.JsonArray
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.PsiTreeUtil
import io.github.gerardorodriguezdev.chamaleon.core.models.Project.Companion.ENVIRONMENTS_DIRECTORY_NAME
import org.jetbrains.kotlin.psi.KtStringTemplateExpression

// TODO: Refactor
class PropertyPsiReference(
    element: KtStringTemplateExpression
) : PsiReferenceBase<KtStringTemplateExpression>(element, true) {

    override fun resolve(): PsiElement? {
        val project = element.project
        val value = element.text.removeSurrounding("\"")
        val psiFile = psiFile(project) ?: return null
        val jsonFile = psiFile as? JsonFile ?: return null
        val jsonObject = PsiTreeUtil.findChildOfType(jsonFile, JsonObject::class.java) ?: return null
        val jsonProperty = jsonObject.findProperty("propertyDefinitions")?.value as? JsonArray ?: return null
        jsonProperty.valueList.forEach { property ->
            val jsonObj = property as? JsonObject
            val prop = jsonObj?.findProperty("name")
            if (prop?.value?.text?.removeSurrounding("\"") == value) return prop.firstChild
        }

        return null
    }

    private fun psiFile(project: Project): PsiFile? {
        val module = ModuleUtilCore.findModuleForPsiElement(element) ?: return null
        val moduleRootManager = ModuleRootManager.getInstance(module)
        val roots = moduleRootManager.contentRoots

        roots.forEach { root ->
            val uri = "$ENVIRONMENTS_DIRECTORY_NAME/SCHEMA_FILE"
            val virtualFile = VfsUtil.findRelativeFile(uri, root)
            if (virtualFile != null) {
                val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
                return psiFile
            }
        }

        return null
    }

    override fun getVariants(): Array<Any> = emptyArray()
}