package com.example.camelbeanref

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.PsiModifier
import com.intellij.psi.PsiClass
import com.intellij.openapi.editor.colors.TextAttributesKey
import org.gaydabura.com.example.camelbeanref.CamelCallUtils
import org.gaydabura.com.example.camelbeanref.SpringLikeBeanResolver
import org.gaydabura.com.example.camelbeanref.evalStringConstant

/**
 * Подсвечивает:
 *  - 1-й аргумент .bean(...) (имя бина), если бин резолвится
 *  - 2-й аргумент .bean(..., "method(...)"), если публичный метод резолвится в классе бина
 *
 * Подсветка — только полезная часть: для 1-го аргумента без кавычек, для 2-го — только имя метода.
 */
class CamelBeanNavigationAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        // Во время индексирования не трогаем PSI
        if (DumbService.isDumb(element.project)) return

        // Интересуемся только строковыми литералами внутри вызовов .bean(...)
        val lit = element as? PsiLiteralExpression ?: return
        if (!CamelCallUtils.isStringLiteralOfBeanCall(lit)) return

        val call = CamelCallUtils.findCamelBeanCall(lit) ?: return
        val project = element.project

        // 1-й аргумент: имя бина
        if (CamelCallUtils.isFirstArg(lit)) {
            val beanName = lit.value as? String ?: return
            val targets = SpringLikeBeanResolver.resolveBean(project, beanName)
            if (targets.isNotEmpty()) {
                // подсвечиваем содержимое без кавычек (АБСОЛЮТНЫЙ диапазон)
                val range = stringInnerRangeAbsolute(lit)
                highlight(holder, range)
            }
            return
        }

        // 2-й аргумент: имя метода
        if (CamelCallUtils.isSecondArg(lit)) {
            val methodSpec = lit.value as? String ?: return

            // Найдём класс бина: либо из MyBean.class, либо из 1-го аргумента
            val beanClass: PsiClass = CamelCallUtils.beanClassFromClassLiteral(lit)
                ?: resolveBeanClassFromFirstArg(project, call.call)
                ?: return

            val methodName = extractLeadingIdentifier(methodSpec)
            if (methodName.isEmpty()) return

            val hasPublic = beanClass.findMethodsByName(methodName, true)
                .any { it.hasModifierProperty(PsiModifier.PUBLIC) }

            if (hasPublic) {
                // Подсвечиваем только имя метода внутри кавычек (АБСОЛЮТНЫЙ диапазон)
                val range = methodNameRangeAbsolute(lit)
                highlight(holder, range)
            }
        }
    }

    private fun resolveBeanClassFromFirstArg(
        project: com.intellij.openapi.project.Project,
        call: PsiMethodCallExpression
    ): PsiClass? {
        val first = call.argumentList.expressions.getOrNull(0) ?: return null
        val beanName = evalStringConstant(project, first) ?: return null
        val targets = SpringLikeBeanResolver.resolveBean(project, beanName)
        return targets.firstOrNull()?.beanClass
    }

    /** Создаёт тихую аннотацию с нашими атрибутами. */
    private fun highlight(holder: AnnotationHolder, range: TextRange) {
        // Важно: range должен лежать внутри element.textRange (мы даём абсолютные координаты)
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(range)
            .textAttributes(NAVIGABLE_ATTR_KEY)
            .create()
    }

    companion object {
        /** Ключ атрибутов подсветки (мапится в XML схемы colorSchemes/...) */
        val NAVIGABLE_ATTR_KEY: TextAttributesKey =
            TextAttributesKey.createTextAttributesKey("CAMEL_BEAN_NAV.NAVIGABLE")

        /** Имя метода: первый идентификатор в строке (буква/_, далее буквы/цифры/_/$). */
        private val IDENT_REGEX = Regex("^([A-Za-z_][\\\$A-Za-z0-9_]*)")

        fun extractLeadingIdentifier(s: String): String {
            val trimmed = s.dropWhile { it.isWhitespace() }
            return IDENT_REGEX.find(trimmed)?.groupValues?.get(1) ?: ""
        }

        /** Абсолютный диапазон: внутренность строкового литерала без кавычек. */
        fun stringInnerRangeAbsolute(literal: PsiLiteralExpression): TextRange {
            val text = literal.text
            val base = literal.textRange.startOffset
            val q1 = text.indexOf('"')
            val q2 = text.lastIndexOf('"')
            return if (q1 >= 0 && q2 > q1) {
                TextRange(base + q1 + 1, base + q2)
            } else {
                // fallback — подсветим весь литерал
                literal.textRange
            }
        }

        /** Абсолютный диапазон: только имя метода внутри строкового литерала второго аргумента. */
        fun methodNameRangeAbsolute(literal: PsiLiteralExpression): TextRange {
            val full = literal.text
            val base = literal.textRange.startOffset
            val q1 = full.indexOf('"')
            val q2 = full.lastIndexOf('"')
            if (q1 == -1 || q2 <= q1) return literal.textRange

            val inner = full.substring(q1 + 1, q2)
            val trimmed = inner.dropWhile { it.isWhitespace() }
            val leadingWs = inner.length - trimmed.length
            val m = IDENT_REGEX.find(trimmed) ?: return literal.textRange

            val start = base + q1 + 1 + leadingWs
            val end = start + m.value.length
            return TextRange(start, end)
        }
    }
}
