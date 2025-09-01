package org.gaydabura.com.example.camelbeanref

import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.util.IncorrectOperationException

/**
 * PsiReference для второго аргумента .bean(..., "methodName(...)").
 * Поддерживает строки с плейсхолдерами: "saveTransaction(${...}, ...)".
 * Ссылка покрывает только имя метода (первые символы-идентификатора в строке).
 */
class BeanMethodReference(
    element: PsiLiteralExpression,
    private val rawText: String,
    private val beanClass: PsiClass?
) : PsiReferenceBase<PsiLiteralExpression>(
    element,
    computeNameTextRange(element),
    /* soft = */ true
) {

    private val methodName: String = extractLeadingIdentifier(rawText)

    override fun resolve(): PsiElement? {
        val cls = beanClass ?: return null
        // Ищем по имени (учитываются унаследованные/перегруженные методы)
        return cls.findMethodsByName(methodName, true).firstOrNull()
    }

    override fun getVariants(): Array<Any> {
        val cls = beanClass ?: return emptyArray()
        return cls.allMethods
            .filter { it.hasModifierProperty(PsiModifier.PUBLIC) }
            .map { it.name }
            .distinct()
            .toTypedArray()
    }

    override fun handleElementRename(newElementName: String): PsiElement {
        // Меняем только ведущий идентификатор в строковом литерале
        val litText = element.text
        val q1 = litText.indexOf('"')
        val q2 = litText.lastIndexOf('"')
        if (q1 == -1 || q2 <= q1) return element

        val inner = litText.substring(q1 + 1, q2)
        val replacedInner = replaceLeadingIdentifier(inner, newElementName)

        val factory = JavaPsiFacade.getElementFactory(element.project)
        val newLiteral = factory.createExpressionFromText("\"$replacedInner\"", element)
        return element.replace(newLiteral)
    }

    // -------------------- helpers --------------------

    private companion object {
        // Имя метода: первый идентификатор в строке (буква/_, далее буквы/цифры/_/$)
        private val IDENT_REGEX = Regex("^([A-Za-z_][\\\$A-Za-z0-9_]*)")

        fun extractLeadingIdentifier(s: String): String {
            val inner = s.trimStart()
            val m = IDENT_REGEX.find(inner)
            return m?.groupValues?.get(1) ?: ""
        }

        fun replaceLeadingIdentifier(inner: String, newName: String): String {
            val trimmedLeft = inner.dropWhile { it.isWhitespace() }
            val leadingWs = inner.length - trimmedLeft.length
            val m = IDENT_REGEX.find(trimmedLeft)
            return if (m != null) {
                " ".repeat(leadingWs) + newName + trimmedLeft.removePrefix(m.value)
            } else {
                newName
            }
        }

        /** Диапазон только имени метода внутри строкового литерала. */
        fun computeNameTextRange(literal: PsiLiteralExpression): TextRange {
            val full = literal.text
            val q1 = full.indexOf('"')
            val q2 = full.lastIndexOf('"')
            if (q1 == -1 || q2 <= q1) {
                // fallback — почти весь литерал без кавычек
                return TextRange(1, full.length - 1)
            }
            val inner = full.substring(q1 + 1, q2)
            val m = IDENT_REGEX.find(inner.trimStart())
            val leadingWs = inner.length - inner.trimStart().length
            val startInInner = if (m != null) leadingWs else 0
            val len = m?.value?.length ?: inner.length.coerceAtMost(1) // чтобы был хоть 1 символ
            val start = q1 + 1 + startInInner
            val end = start + len
            return TextRange(start, end)
        }
    }
}
