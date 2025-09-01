package org.gaydabura.com.example.camelbeanref

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.util.IncorrectOperationException

/** PsiReference for the 1st argument: "beanName" or class literal's implied bean name (if present as string). */
class BeanNameReference(
    element: PsiLiteralExpression,
    private val beanName: String
) : PsiReferenceBase<PsiLiteralExpression>(element, TextRange(1, element.textLength - 1), /* soft = */ true) {

    override fun resolve(): PsiElement? {
        val project = element.project
        val targets = SpringLikeBeanResolver.resolveBean(project, beanName)
        return targets.firstOrNull()?.psiElement
    }

    override fun getVariants(): Array<Any> {
        // Можно добавить перечисление всех бинов проекта, но это дорого.
        return emptyArray()
    }

    @Throws(IncorrectOperationException::class)
    override fun handleElementRename(newElementName: String): PsiElement {
        // поддержка Rename — просто поменять строковый литерал
        val factory = JavaPsiFacade.getElementFactory(element.project)
        val newLiteral = factory.createExpressionFromText("\"$newElementName\"", element)
        return element.replace(newLiteral)
    }
}
