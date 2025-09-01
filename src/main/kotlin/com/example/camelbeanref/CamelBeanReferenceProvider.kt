package org.gaydabura.com.example.camelbeanref

import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.util.ProcessingContext

/** Вешаем PsiReference на строковые литералы внутри .bean(...). */
class CamelBeanReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        if (element !is PsiLiteralExpression) return PsiReference.EMPTY_ARRAY
        val value = element.value as? String ?: return PsiReference.EMPTY_ARRAY
        if (!CamelCallUtils.isStringLiteralOfBeanCall(element)) return PsiReference.EMPTY_ARRAY

        val refs = mutableListOf<PsiReference>()

        if (CamelCallUtils.isFirstArg(element)) {
            refs += BeanNameReference(element, value)
        } else if (CamelCallUtils.isSecondArg(element)) {
            val project = element.project
            val beanClass =
                CamelCallUtils.beanClassFromClassLiteral(element)
                    ?: resolveBeanClassFromFirstArg(project, element)

            refs += BeanMethodReference(element, value, beanClass)
        }

        return refs.toTypedArray()
    }

    private fun resolveBeanClassFromFirstArg(project: Project, element: PsiElement): PsiClass? {
        val call = CamelCallUtils.findCamelBeanCall(element) ?: return null
        val nameLit = call.firstArgString ?: return null
        val name = nameLit.value as? String ?: return null
        val targets = SpringLikeBeanResolver.resolveBean(project, name)
        return targets.firstOrNull()?.beanClass
    }
}
