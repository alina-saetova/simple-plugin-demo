package com.technokratos.simpledemo.dialog

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.layout.LayoutBuilder
import com.intellij.ui.layout.applyToComponent
import com.intellij.ui.layout.panel
import com.intellij.util.containers.toArray
import com.technokratos.simpledemo.persistence.CreateDaoMethodsFormState
import com.technokratos.simpledemo.utils.dummyTextBinding
import com.technokratos.simpledemo.utils.log
import com.technokratos.simpledemo.utils.selectedItem
import org.jetbrains.kotlin.psi.KtClass
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent
import javax.swing.JTextField

class CreateDaoMethodsDialog(
    private val project: Project,
    private val daoKtClass: KtClass,
) : DialogWrapper(project, true) {

    init {
        title = "Create Dao Methods"
        init()
    }

    private lateinit var entityFqnNameTextField: JTextField
    private lateinit var tableNameTextField: JTextField

    private var whatToSelect: SelectOption = SelectOption.ALL
    private lateinit var selectAllRadioButton: JBRadioButton
    private lateinit var selectWhereRadioButton: JBRadioButton
    private lateinit var selectWhereFieldNameTextField: JTextField
    private lateinit var selectWhereFieldTypeComboBox: ComboBox<String>
    private lateinit var needAsyncSelectCheckBox: JBCheckBox
    private lateinit var asyncSelectComboBox: ComboBox<AsyncWay>

    override fun doOKAction() {
        saveModel()
        super.doOKAction()
    }

    override fun doCancelAction() {
        saveModel()
        super.doCancelAction()
    }

    override fun createCenterPanel(): JComponent? {
        val state = service<CreateDaoMethodsFormState>().state
        whatToSelect = state.selectQOption
        project.log("createCenterPanel ---- ${state.selectQOption}")
        return panel {
            createInfoSection(state)
            createQuerySection(state)
        }.withPreferredWidth(600)
    }

    private fun saveModel() {
        val selectedAsync = if (needAsyncSelectCheckBox.isSelected) {
            asyncSelectComboBox.selectedItem()
        } else {
            AsyncWay.NONE
        }
        val selectQuery1 = when (whatToSelect) {
            SelectOption.ALL -> {
                DaoModel.SelectQuery.All(
                    selectedAsync
                )
            }
            SelectOption.WHERE -> {
                DaoModel.SelectQuery.Where(
                    selectedAsync,
                    selectWhereFieldNameTextField.text,
                    FieldType.fromAlt(selectWhereFieldTypeComboBox.selectedItem())
                )
            }
        }
        val model = DaoModel(
            daoKtClass = daoKtClass,
            entityFqnName = entityFqnNameTextField.text,
            tableName = tableNameTextField.text,
            selectQuery = selectQuery1,
        )
        project.log(model.toString())
        service<CreateDaoMethodsFormState>().setModel(model)
    }

    private fun LayoutBuilder.createInfoSection(lastState: CreateDaoMethodsFormState) {
        row("Entity full qualified name: ") {
            textField(dummyTextBinding)
                .applyToComponent {
                    text = lastState.entityFqnName
                    entityFqnNameTextField = this
                }
        }
        row("Table name: ") {
            textField(dummyTextBinding)
                .applyToComponent {
                    text = lastState.tableName
                    tableNameTextField = this
                }
        }
    }

    private fun LayoutBuilder.createQuerySection(lastState: CreateDaoMethodsFormState) {
        row {
            buttonGroup("What to select") {
                row {
                    radioButton("All")
                        .applyToComponent {
                            addChangeListener {
                                whatToSelect = SelectOption.ALL
                            }
                            isSelected = lastState.selectQOption == SelectOption.ALL
                            selectAllRadioButton = this
                        }
                }

                row {
                    radioButton("With WHERE equals condition")
                        .applyToComponent {
                            isSelected = lastState.selectQOption == SelectOption.WHERE
                            addChangeListener {
                                whatToSelect = SelectOption.WHERE
                                selectWhereFieldNameTextField.isEnabled = isSelected
                                selectWhereFieldTypeComboBox.isEnabled = isSelected
                            }
                            selectWhereRadioButton = this
                        }

                    row("Entity field name: ") {
                        textField(dummyTextBinding)
                            .applyToComponent {
                                isEnabled = lastState.selectQOption == SelectOption.WHERE
                                text = if (lastState.selectQOption == SelectOption.WHERE) lastState.fieldName else ""
                                selectWhereFieldNameTextField = this
                            }
                    }
                    row("Entity field type: ") {
                        comboBox(
                            model = DefaultComboBoxModel(FieldType.values().map { it.alt }.toTypedArray()),
                            getter = { FieldType.IntType.alt }, setter = {}
                        ).applyToComponent {
                            isEnabled = lastState.selectQOption == SelectOption.WHERE
                            selectedItem = if (lastState.selectQOption == SelectOption.WHERE) lastState.fieldType.alt else null
                            selectWhereFieldTypeComboBox = this
                        }
                    }
                }
            }
        }
        row {
            checkBox(
                text = "Make asynchronous",
                isSelected = lastState.asyncWay != null
            ).applyToComponent {
                addActionListener {
                    asyncSelectComboBox.isEnabled = isSelected
                }
                needAsyncSelectCheckBox = this
            }
            row {
                comboBox(
                    model = DefaultComboBoxModel(AsyncWay.values()),
                    getter = { AsyncWay.NONE }, setter = {}
                ).applyToComponent {
                    isEnabled = lastState.asyncWay != null
                    selectedItem = lastState.asyncWay
                    asyncSelectComboBox = this
                }
            }
        }
    }
}
