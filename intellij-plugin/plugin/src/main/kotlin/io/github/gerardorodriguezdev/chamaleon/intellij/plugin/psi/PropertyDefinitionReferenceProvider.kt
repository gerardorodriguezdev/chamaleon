package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.psi

import arrow.core.raise.option
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull

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
            val bindingContext = receiverExpression.analyze()
            val type = bindingContext.getType(receiverExpression).bind()
            val classDescriptor = type.constructor.declarationDescriptor.bind()
            val platformModelFullName = classDescriptor.fqNameOrNull()?.asString()?.bind()
            ensure(platformModelFullName == "io.github.gerardorodriguezdev.chamaleon.core.models.Platform")

            arrayOf(PropertyDefinitionPsiReference(element))
        }.getOrNull() ?: PsiReference.EMPTY_ARRAY

    private fun String.isFunctionCallSupported(): Boolean =
        this == "propertyStringValue" ||
                this == "propertyStringValueOrNull" ||
                this == "propertyBooleanValue" ||
                this == "propertyBooleanValueOrNull"
}