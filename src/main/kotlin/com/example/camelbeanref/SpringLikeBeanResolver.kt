package org.gaydabura.com.example.camelbeanref

import com.intellij.psi.search.PsiShortNamesCache

import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.AnnotatedElementsSearch

object SpringLikeBeanResolver {

    private val STEREOTYPES = listOf(
        "org.springframework.stereotype.Component",
        "org.springframework.stereotype.Service",
        "org.springframework.stereotype.Repository",
        "org.springframework.stereotype.Controller",
        "org.springframework.web.bind.annotation.RestController"
    )

    private const val QUALIFIER = "org.springframework.beans.factory.annotation.Qualifier"
    private const val CONFIGURATION = "org.springframework.context.annotation.Configuration"
    private const val BEAN = "org.springframework.context.annotation.Bean"

    data class BeanTarget(
        val psiElement: PsiElement,   // @Bean method or component class
        val beanName: String,
        val beanClass: PsiClass?      // class of bean (for method lookup)
    )

    fun resolveBean(project: Project, beanName: String): List<BeanTarget> {
        val scope = GlobalSearchScope.projectScope(project)
        val javaPsi = JavaPsiFacade.getInstance(project)
        val results = mutableListOf<BeanTarget>()

        // 1) @Bean methods inside @Configuration
        val confAnno = javaPsi.findClass(CONFIGURATION, scope)
        val beanAnno = javaPsi.findClass(BEAN, scope)
        if (confAnno != null && beanAnno != null) {
            AnnotatedElementsSearch.searchPsiClasses(confAnno, scope).forEach { conf ->
                conf.methods.filter { it.hasAnnotation(BEAN) }.forEach { m ->
                    val ann = m.getAnnotation(BEAN)
                    val explicit = (ann?.findAttributeValue("name") as? PsiLiteralExpression)?.value as? String
                    val annValue = (ann?.findAttributeValue("value") as? PsiLiteralExpression)?.value as? String
                    val name = explicit ?: annValue ?: m.name
                    if (name == beanName) {
                        val beanClass = (m.returnType as? PsiClassType)?.resolve()
                        results += BeanTarget(m, name, beanClass)
                    }
                }
            }
        }

        // 2) @Component/@Service/... classes
        for (annoFqn in STEREOTYPES) {
            val anno = javaPsi.findClass(annoFqn, scope) ?: continue
            AnnotatedElementsSearch.searchPsiClasses(anno, scope).forEach { cls ->
                val qualifier = cls.getAnnotation(QUALIFIER)
                val qualifierName = ((qualifier?.findAttributeValue("value")) as? PsiLiteralExpression)?.value as? String

                // @Component("name") / @Service("name")
                val stereo = cls.getAnnotation(annoFqn)
                val stereoName = ((stereo?.findAttributeValue("value")) as? PsiLiteralExpression)?.value as? String
                    ?: ((stereo?.findAttributeValue("name")) as? PsiLiteralExpression)?.value as? String

                val defaultName = decapitalize(cls.name ?: "")

                val names = listOfNotNull(qualifierName, stereoName, defaultName)
                if (names.contains(beanName)) {
                    results += BeanTarget(cls, beanName, cls)
                }
            }
        }

        if (results.isNotEmpty()) return results

        // 3) Fallback: match by simpleName decapitalize == beanName (без аннотаций)
        val shortNames = PsiShortNamesCache.getInstance(project)
        // перебираем ВСЕ классы, это недешево — но как резерв
        val allNames = shortNames.allClassNames
        allNames.forEach { simple ->
            if (decapitalize(simple) == beanName) {
                shortNames.getClassesByName(simple, scope).forEach { cls ->
                    results += BeanTarget(cls, beanName, cls)
                }
            }
        }

        return results
    }

    fun resolveMethod(beanClass: PsiClass, methodName: String): List<PsiMethod> {
        return beanClass.findMethodsByName(methodName, true).toList()
    }

    private fun decapitalize(s: String): String =
        if (s.isNotEmpty() && s[0].isUpperCase()) s[0].lowercaseChar() + s.substring(1) else s
}
