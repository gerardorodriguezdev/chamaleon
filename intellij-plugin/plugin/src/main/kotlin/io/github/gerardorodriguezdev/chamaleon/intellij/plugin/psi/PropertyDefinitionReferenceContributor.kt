package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.psi

import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import org.jetbrains.kotlin.psi.KtStringTemplateExpression

internal class PropertyDefinitionReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(KtStringTemplateExpression::class.java),
            PropertyDefinitionReferenceProvider(),
        )
    }
}