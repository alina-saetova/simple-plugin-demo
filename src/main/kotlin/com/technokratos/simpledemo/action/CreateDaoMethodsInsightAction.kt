package com.technokratos.simpledemo.action

import com.android.tools.idea.kotlin.getQualifiedName
import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.codeInsight.actions.CodeInsightAction
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.JavaCodeStyleManager
import com.technokratos.simpledemo.dialog.AsyncWay
import com.technokratos.simpledemo.dialog.CreateDaoMethodsDialog
import com.technokratos.simpledemo.dialog.DaoModel
import com.technokratos.simpledemo.persistence.CreateDaoMethodsFormState
import com.technokratos.simpledemo.persistence.DaoModelState.Companion.createModel
import com.technokratos.simpledemo.utils.getKtClassWithEditor
import org.jetbrains.kotlin.idea.core.ShortenReferences
import org.jetbrains.kotlin.idea.formatter.commitAndUnblockDocument
import org.jetbrains.kotlin.idea.util.application.executeWriteCommand
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtPsiFactory
import java.util.*

private const val daoAnnotationName = "androidx.room.Dao"

class CreateDaoMethodsInsightAction : CodeInsightAction(), CodeInsightActionHandler {

    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        val daoKtClass = file.getKtClassWithEditor(editor) ?: return
        val wasDialogOK = CreateDaoMethodsDialog(
            project,
            daoKtClass
        ).showAndGet()
        if (wasDialogOK) {
            handleDialogOk(project, daoKtClass)
        }
    }

    private fun handleDialogOk(project: Project, ktClass: KtClass) {
        val model = service<CreateDaoMethodsFormState>().state.createModel(project)
        val psiFactory = KtPsiFactory(project)
        project.executeWriteCommand("GenerateDaoMethod") {
            val func = psiFactory.createFunction(
                getFunctionDecl(model)
            )
            val annotation = psiFactory.createAnnotationEntry(getAnnotationForQueryOp(model))

            func.addAnnotationEntry(annotation)
            ktClass.addDeclaration(func)

            ShortenReferences.DEFAULT.process(ktClass.containingKtFile)
            CodeStyleManager.getInstance(project).reformat(ktClass.containingKtFile)
        }
    }

    override fun isValidForFile(project: Project, editor: Editor, file: PsiFile): Boolean {
        val ktClass = file.getKtClassWithEditor(editor) ?: return false

        return ktClass.annotationEntries
            .map { it.getQualifiedName() }
            .contains(daoAnnotationName)
    }
    override fun getHandler(): CodeInsightActionHandler = this
    override fun startInWriteAction(): Boolean  = false
}


private fun getFunctionDecl(model: DaoModel): String {
    val funName = getNameForQueryOp(model)
    val funReturnValue = getReturnValueForQueryOp(model)
    val funArgs = getArgsForQueryOp(model.selectQuery)
    return "fun ${funName}($funArgs)$funReturnValue"
}

private fun getAnnotationForQueryOp(model: DaoModel): String {
    val query = "SELECT * FROM ${model.tableName}"
    val where = if (model.selectQuery is DaoModel.SelectQuery.Where) {
        val fieldName = model.selectQuery.fieldName
        " WHERE $fieldName = :$fieldName"
    } else ""
    return "@androidx.room.Query(\"$query$where\")"
}

private fun getNameForQueryOp(model: DaoModel): String {
    val prefix = if (model.selectQuery is DaoModel.SelectQuery.Where) {
        "By${model.selectQuery.fieldName.replaceFirstChar { it.uppercase() }}"
    } else ""
    return "get${model.entityShortName}$prefix"
}

private fun getReturnValueForQueryOp(model: DaoModel): String {
    return when (model.selectQuery.asyncWay) {
        AsyncWay.NONE, null -> ": List<${model.entityFqnName}>"
        AsyncWay.SINGLE -> ": io.reactivex.Single<List<${model.entityFqnName}>>"
        AsyncWay.OBSERVABLE -> ": io.reactivex.Observable<List<${model.entityFqnName}>>"
    }
}

private fun getArgsForQueryOp(model: DaoModel.SelectQuery): String {
    return if (model is DaoModel.SelectQuery.Where) {
        "${model.fieldName}: ${model.fieldType.alt}"
    } else ""
}