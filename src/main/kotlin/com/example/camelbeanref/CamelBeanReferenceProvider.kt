package org.gaydabura.com.example.camelbeanref

import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext

/** Вешаем PsiReference на аргументы внутри .bean(...). */
class CamelBeanReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        // Интересуют либо строковые литералы, либо ссылки (константы) внутри аргументов .bean(...)
        if (element !is PsiLiteralExpression && element !is PsiReferenceExpression) {
            return PsiReference.EMPTY_ARRAY
        }

        val call = findEnclosingBeanCall(element) ?: return PsiReference.EMPTY_ARRAY
        val (argIndex, argExpr) = findWhichArgumentOfCall(element, call) ?: return PsiReference.EMPTY_ARRAY

        if (call.methodExpression.referenceName != "bean") return PsiReference.EMPTY_ARRAY

        val refs = mutableListOf<PsiReference>()

        when (argIndex) {
            0 -> { // bean name
                val beanName = evalStringConstant(element.project, argExpr) ?: return PsiReference.EMPTY_ARRAY
                refs += BeanNameReference(element, beanName)
            }
            1 -> { // method spec — ожидаем строковый литерал
                val lit = PsiTreeUtil.getChildOfType(argExpr, PsiLiteralExpression::class.java)
                    ?: (argExpr as? PsiLiteralExpression)
                    ?: return PsiReference.EMPTY_ARRAY

                val methodSpec = lit.value as? String ?: return PsiReference.EMPTY_ARRAY
                val beanClass = resolveBeanClassFromFirstArg(element.project, call)
                refs += BeanMethodReference(lit, methodSpec, beanClass)
            }
        }

        return refs.toTypedArray()
    }

    /** Резолвит класс бина по 1-му аргументу с учётом констант/конкатенаций. */
    private fun resolveBeanClassFromFirstArg(project: Project, call: PsiMethodCallExpression): PsiClass? {
        val first = call.argumentList.expressions.getOrNull(0) ?: return null
        val beanName = evalStringConstant(project, first) ?: return null
        val targets = SpringLikeBeanResolver.resolveBean(project, beanName)
        return targets.firstOrNull()?.beanClass
    }

    /** Окружающий вызов .bean(...) для произвольного PSI-узла. */
    private fun findEnclosingBeanCall(element: PsiElement): PsiMethodCallExpression? =
        PsiTreeUtil.getParentOfType(element, PsiMethodCallExpression::class.java, /* strict = */ false)
            ?.takeIf { it.methodExpression.referenceName == "bean" }

    /** Возвращает (index, topArgExpression) для аргумента, внутри которого находится element. */
    private fun findWhichArgumentOfCall(
        element: PsiElement,
        call: PsiMethodCallExpression
    ): Pair<Int, PsiExpression>? {
        val args = call.argumentList.expressions
        val expr = PsiTreeUtil.getParentOfType(element, PsiExpression::class.java, /* strict = */ false)
            ?: return null
        args.forEachIndexed { idx, top ->
            if (PsiTreeUtil.isAncestor(top, expr, /* strict = */ false)) return idx to top
        }
        return null
    }
}

/* =========================
   Helpers
   ========================= */

/** Пытаемся вычислить строку из выражения (литерал, public static final, конкатенации). */
fun evalStringConstant(project: Project, expr: PsiExpression?): String? {
    if (expr == null) return null

    if (expr is PsiLiteralExpression) (expr.value as? String)?.let { return it }

    val helper = JavaPsiFacade.getInstance(project).constantEvaluationHelper
    runCatching { helper.computeConstantExpression(expr) }.getOrNull()?.let { c ->
        if (c is String) return c
    }

    if (expr is PsiReferenceExpression) {
        val resolved = expr.resolve()
        if (resolved is PsiField && resolved.type.equalsToText(CommonClassNames.JAVA_LANG_STRING)) {
            val init = resolved.initializer
            if (init is PsiLiteralExpression && init.value is String) return init.value as String
            runCatching { helper.computeConstantExpression(init) }.getOrNull()?.let { c ->
                if (c is String) return c
            }
        }
    }

    if (expr is PsiPolyadicExpression && expr.operationTokenType == JavaTokenType.PLUS) {
        val parts = expr.operands.mapNotNull { evalStringConstant(project, it) }
        if (parts.size == expr.operands.size) return parts.joinToString("")
    }

    return null
}
