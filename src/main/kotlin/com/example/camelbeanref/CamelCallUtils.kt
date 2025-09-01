package org.gaydabura.com.example.camelbeanref

/** Utility to recognize Camel .bean(...) calls and extract arguments in a language-agnostic way (UAST). */
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil

/**
 * Утилиты распознавания вызовов Camel .bean(...) на чистом Java PSI.
 * Поддерживаем:
 *   .bean("beanName")
 *   .bean("beanName", "methodName")
 *   .bean(MyBean.class)
 *   .bean(MyBean.class, "methodName")
 */
object CamelCallUtils {

    data class BeanCall(
        val call: PsiMethodCallExpression,
        val firstArgString: PsiLiteralExpression?,            // "beanName" (если 1-й аргумент строка)
        val classArg: PsiClassObjectAccessExpression?,        // MyBean.class (если 1-й аргумент класслит)
        val secondArgString: PsiLiteralExpression?            // "methodName" (если 2-й строка)
    )

    fun findCamelBeanCall(context: PsiElement): BeanCall? {
        val call = PsiTreeUtil.getParentOfType(context, PsiMethodCallExpression::class.java, false)
            ?: return null

        val methodName = call.methodExpression.referenceName ?: return null
        if (methodName != "bean") return null

        val args = call.argumentList.expressions

        var firstString: PsiLiteralExpression? = null
        var classLit: PsiClassObjectAccessExpression? = null
        var secondString: PsiLiteralExpression? = null

        if (args.size >= 1) {
            firstString = args[0] as? PsiLiteralExpression
            classLit = args[0] as? PsiClassObjectAccessExpression
        }
        if (args.size >= 2) {
            secondString = args[1] as? PsiLiteralExpression
        }

        return BeanCall(call, firstString, classLit, secondString)
    }

    fun isStringLiteralOfBeanCall(context: PsiElement): Boolean {
        val call = findCamelBeanCall(context) ?: return false
        val lit = context as? PsiLiteralExpression ?: return false
        return call.firstArgString == lit || call.secondArgString == lit
    }

    fun isFirstArg(context: PsiElement): Boolean {
        val call = findCamelBeanCall(context) ?: return false
        val lit = context as? PsiLiteralExpression ?: return false
        return call.firstArgString == lit
    }

    fun isSecondArg(context: PsiElement): Boolean {
        val call = findCamelBeanCall(context) ?: return false
        val lit = context as? PsiLiteralExpression ?: return false
        return call.secondArgString == lit
    }

    /** Возвращает PsiClass из MyBean.class, если 1-й аргумент — класслит. */
    fun beanClassFromClassLiteral(context: PsiElement): PsiClass? {
        val call = findCamelBeanCall(context) ?: return null
        val classLit = call.classArg ?: return null
        val type = classLit.operand.type
        return (type as? PsiClassType)?.resolve()
    }
}
