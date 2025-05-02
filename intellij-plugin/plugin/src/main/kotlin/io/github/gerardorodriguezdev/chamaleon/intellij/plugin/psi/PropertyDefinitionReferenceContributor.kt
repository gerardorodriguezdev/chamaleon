package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.psi

import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PlatformPatterns.psiFile
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import org.jetbrains.kotlin.psi.KtStringTemplateExpression

internal class PropertyDefinitionReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns
                .psiElement(KtStringTemplateExpression::class.java)
                .inFile(psiFile().withName(BUILD_GRADLE_FILE_NAME)),
            PropertyDefinitionReferenceProvider(),
        )
    }

    private companion object {
        const val BUILD_GRADLE_FILE_NAME = "build.gradle.kts"
    }
}