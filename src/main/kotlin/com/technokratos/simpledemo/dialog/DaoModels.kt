package com.technokratos.simpledemo.dialog

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable
import org.jetbrains.kotlin.psi.KtClass

enum class FieldType(val alt: String) {
    IntType("Int"),
    StringType("String"),
    LongType("Long"),
    DoubleType("Double"),
    FloatType("Float"),
    BooleanType("Boolean");

    companion object {
        fun fromAlt(alt: String): FieldType {
            return when(alt) {
                "Int" -> IntType
                "String" -> StringType
                "Long" -> LongType
                "Double" -> DoubleType
                "Float" -> FloatType
                "Boolean" -> BooleanType
                else -> IntType
            }
        }
    }
}

enum class AsyncWay {
    NONE, OBSERVABLE, SINGLE
}

enum class SelectOption {
    ALL, WHERE,
}

data class DaoModel(
    val daoKtClass: KtClass,
    val entityFqnName: String,
    val tableName: String,
    val selectQuery: SelectQuery
) {
    sealed class SelectQuery(
        open val asyncWay: AsyncWay? = null
    ) {
        data class All(
            override val asyncWay: AsyncWay? = null
        ): SelectQuery(asyncWay)

        data class Where(
            override val asyncWay: AsyncWay? = null,
            val fieldName: String,
            val fieldType: FieldType,
        ): SelectQuery(asyncWay)
    }

    val entityShortName: String
        get() = entityFqnName.split(".").last()
}