package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.psi

import arrow.core.raise.option
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.types.symbol
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType

internal class PropertyDefinitionReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(
        element: PsiElement,
        context: ProcessingContext
    ): Array<out PsiReference?> =
        option {
            val element = (element as? KtStringTemplateExpression).bind()
            val callExpression = element.getStrictParentOfType<KtCallExpression>().bind()
            val callee = callExpression.calleeExpression?.text.bind()
            ensure(callee.isFunctionCallSupported())

            val qualifiedExpression = (callExpression.parent as? KtDotQualifiedExpression).bind()
            val receiverExpression = qualifiedExpression.receiverExpression

            analyze(receiverExpression) {
                val receiverType = receiverExpression.expressionType.bind()
                val classFqName =
                    receiverType.symbol?.classId?.asSingleFqName()?.asString().bind()
                ensure(classFqName == PLATFORM_MODEL_FULL_NAME)
            }

            arrayOf(PropertyDefinitionPsiReference(element))
        }.getOrNull() ?: PsiReference.EMPTY_ARRAY

    private fun String.isFunctionCallSupported(): Boolean = supportedFunctions.contains(this)

    private companion object {
        const val PLATFORM_MODEL_FULL_NAME = "io.github.gerardorodriguezdev.chamaleon.core.models.Platform"
        val supportedFunctions = setOf(
            "propertyStringValue",
            "propertyStringValueOrNull",
            "propertyBooleanValue",
            "propertyBooleanValueOrNull",
        )
    }
}