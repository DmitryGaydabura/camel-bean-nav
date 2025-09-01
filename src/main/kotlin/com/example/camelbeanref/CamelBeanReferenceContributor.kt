package org.gaydabura.com.example.camelbeanref

import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar

class CamelBeanReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(PsiLiteralExpression::class.java),
            CamelBeanReferenceProvider()
        )
    }
}
