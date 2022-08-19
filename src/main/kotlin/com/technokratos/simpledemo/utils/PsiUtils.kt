package com.technokratos.simpledemo.utils

import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.ui.layout.PropertyBinding
import org.jetbrains.kotlin.asJava.unwrapped
import org.jetbrains.kotlin.psi.KtClass

val dummyTextBinding: PropertyBinding<String> = PropertyBinding({ "" }, {})

@Suppress("UNCHECKED_CAST")
fun <T> ComboBox<T>.selectedItem(): T {
    return selectedItem as T
}

fun PsiFile.getKtClassWithEditor(editor: Editor): KtClass? {
    val offset = editor.caretModel.offset
    val psiElement = findElementAt(offset)
    return PsiTreeUtil.getParentOfType(psiElement, KtClass::class.java)
}

fun Project.findKtClass(fqName: String): KtClass? {
    return JavaPsiFacade.getInstance(this)
        .findClass(fqName, GlobalSearchScope.projectScope(this))
        ?.unwrapped as? KtClass
}

class MyProjectService(
    private val project: Project
) {

    fun doWork() {
        //
    }
}

