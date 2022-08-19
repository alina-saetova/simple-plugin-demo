package com.technokratos.simpledemo.persistence

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.RoamingType
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import com.technokratos.simpledemo.dialog.AsyncWay
import com.technokratos.simpledemo.dialog.DaoModel
import com.technokratos.simpledemo.dialog.FieldType
import com.technokratos.simpledemo.dialog.SelectOption

@Service
@State(
    name = "CreateDaoMethodsForm",
    storages = [Storage(value = "create_dao_methods_form.xml", roamingType = RoamingType.DISABLED)],
)
class CreateDaoMethodsFormState : PersistentStateComponent<CreateDaoMethodsFormState> {

    var entityFqnName: String = ""
    var tableName: String = ""
    var daoKtClassName: String = ""
    var selectQOption: SelectOption = SelectOption.ALL
    var asyncWay: AsyncWay? = null
    var fieldName: String = ""
    var fieldType: FieldType = FieldType.IntType

    override fun getState() = this

    override fun loadState(state: CreateDaoMethodsFormState) {
        XmlSerializerUtil.copyBean(state, this);
    }

    fun setModel(model: DaoModel) {
        daoKtClassName = model.daoKtClass.fqName?.toString().orEmpty()
        entityFqnName = model.entityFqnName
        tableName = model.tableName
        when (model.selectQuery) {
            is DaoModel.SelectQuery.All -> {
                selectQOption = SelectOption.ALL
            }
            is DaoModel.SelectQuery.Where -> {
                selectQOption = SelectOption.WHERE
                fieldName = model.selectQuery.fieldName
                fieldType = model.selectQuery.fieldType
            }
        }
        asyncWay = model.selectQuery.asyncWay
    }
}