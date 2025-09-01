package org.gaydabura.com.example.camelbeanref


import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiModifier
import com.intellij.util.ProcessingContext

/**
 * Completion for the 2nd argument of .bean(..., "methodName(...)").
 * Suggests public method names of the resolved bean class.
 */
class CamelBeanCompletionContributor : CompletionContributor() {

    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withParent(PsiLiteralExpression::class.java),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    val lit = parameters.position.parent as? PsiLiteralExpression ?: return
                    if (!CamelCallUtils.isStringLiteralOfBeanCall(lit)) return
                    if (!CamelCallUtils.isSecondArg(lit)) return

                    val beanClass =
                        CamelCallUtils.beanClassFromClassLiteral(lit)
                            ?: resolveBeanClassFromFirstArg(lit)
                            ?: return

                    // Текущий префикс — первые символы идентификатора в начале строки (если есть)
                    val innerText = (lit.value as? String).orEmpty()
                    val currentPrefix = extractLeadingMethodName(innerText)
                    val rs = if (currentPrefix.isNotEmpty()) result.withPrefixMatcher(currentPrefix) else result

                    beanClass.allMethods
                        .asSequence()
                        .filter { it.hasModifierProperty(PsiModifier.PUBLIC) }
                        .map { it.name }
                        .distinct()
                        .forEach { name ->
                            rs.addElement(LookupElementBuilder.create(name))
                        }
                }
            }
        )
    }

    private fun resolveBeanClassFromFirstArg(element: PsiLiteralExpression): com.intellij.psi.PsiClass? {
        val call = CamelCallUtils.findCamelBeanCall(element) ?: return null
        val nameLit = call.firstArgString ?: return null
        val name = nameLit.value as? String ?: return null
        val targets = SpringLikeBeanResolver.resolveBean(element.project, name)
        return targets.firstOrNull()?.beanClass
    }

    /** Extracts the leading identifier (method name) from a string like "save(${...})" or "save(" */
    private fun extractLeadingMethodName(s: String): String {
        val trimmedLeft = s.dropWhile { it.isWhitespace() }
        val m = Regex("^([A-Za-z_][\$A-Za-z0-9_]*)").find(trimmedLeft)
        return m?.groupValues?.get(1) ?: ""
    }

}
