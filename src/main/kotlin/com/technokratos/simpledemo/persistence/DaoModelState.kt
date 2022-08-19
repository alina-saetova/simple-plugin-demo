package com.technokratos.simpledemo.persistence

import com.intellij.openapi.project.Project
import com.technokratos.simpledemo.dialog.DaoModel
import com.technokratos.simpledemo.dialog.SelectOption
import com.technokratos.simpledemo.utils.findKtClass

data class DaoModelState(
    val daoKtClassName: String = "",
    val entityFqnName: String = "",
    val tableName: String = "",
    val selectQuery: DaoModel.SelectQuery = DaoModel.SelectQuery.All(),
) {
    companion object {

        fun CreateDaoMethodsFormState.createModel(project: Project): DaoModel {
            val daoKtClass = project.findKtClass(daoKtClassName)!!
            val selectQuery = when (selectQOption) {
                SelectOption.ALL -> DaoModel.SelectQuery.All(asyncWay)
                SelectOption.WHERE -> DaoModel.SelectQuery.Where(asyncWay, fieldName, fieldType)
            }
            return DaoModel(
                daoKtClass = daoKtClass,
                entityFqnName = entityFqnName,
                tableName = tableName,
                selectQuery = selectQuery,
            )
        }
    }
}