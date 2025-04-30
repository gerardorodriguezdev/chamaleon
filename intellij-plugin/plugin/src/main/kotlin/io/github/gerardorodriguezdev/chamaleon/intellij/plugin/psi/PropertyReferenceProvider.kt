package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType

internal class PropertyReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(
        element: PsiElement,
        context: ProcessingContext
    ): Array<out PsiReference?> {
        if (element !is KtStringTemplateExpression) return PsiReference.EMPTY_ARRAY

        val callExpression = element.getStrictParentOfType<KtCallExpression>() ?: return PsiReference.EMPTY_ARRAY
        val callee = callExpression.calleeExpression?.text ?: return PsiReference.EMPTY_ARRAY

        //TODO: Add package
        //TODO: Add propertyStringValue
        //TODO: Add propertyStringValueOrNull
        //TODO: Add propertyBooleanValue
        //TODO: Add propertyBooleanValueOrNull

        if (callee != "propertyStringValue") return PsiReference.EMPTY_ARRAY

        return arrayOf(PropertyPsiReference(element))
    }
}