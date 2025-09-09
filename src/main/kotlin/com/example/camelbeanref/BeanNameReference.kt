package org.gaydabura.com.example.camelbeanref

import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil

/**
 * Ссылка для 1-го аргумента .bean(<bean>, ...)
 * Работает и для строкового литерала, и для константы (PsiReferenceExpression).
 * Наводит на @Bean-метод или @Component/@Service класс.
 */
class BeanNameReference(
    element: PsiElement,
    private val beanName: String
) : PsiReferenceBase<PsiElement>(
    element,
    // Покрываем полезную часть: для литерала — без кавычек, для идентификатора — целиком.
    computeRange(element),
    /* soft = */ true
) {

    override fun resolve(): PsiElement? {
        val project = element.project
        val targets = SpringLikeBeanResolver.resolveBean(project, beanName)
        // Навигация хотя бы к классу бина (или к @Bean-классу)
        return targets.firstOrNull()?.beanClass
    }


    override fun getVariants(): Array<Any> = emptyArray()

    companion object {
        private fun computeRange(el: PsiElement): TextRange {
            return when (el) {
                is PsiLiteralExpression -> {
                    val t = el.text
                    val q1 = t.indexOf('"')
                    val q2 = t.lastIndexOf('"')
                    if (q1 >= 0 && q2 > q1) TextRange(q1 + 1, q2) else TextRange(0, t.length)
                }
                is PsiReferenceExpression -> {
                    // идентификатор константы целиком
                    TextRange(0, el.textLength)
                }
                else -> TextRange(0, el.textLength)
            }
        }
    }
}
