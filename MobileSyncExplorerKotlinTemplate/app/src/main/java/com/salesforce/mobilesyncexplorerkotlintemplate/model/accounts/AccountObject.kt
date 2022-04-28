package com.salesforce.mobilesyncexplorerkotlintemplate.model.accounts

import com.salesforce.androidsdk.mobilesync.util.Constants
import com.salesforce.mobilesyncexplorerkotlintemplate.core.extensions.optStringOrNull
import com.salesforce.mobilesyncexplorerkotlintemplate.core.salesforceobject.*
import org.json.JSONObject

data class AccountObject @Throws(AccountValidationException::class) constructor(
    val description: String?,
    val name: String
) : SObject {

    init {
        validateName(name)
    }

    override fun JSONObject.applyObjProperties(): JSONObject = this.apply {
        putOpt(KEY_DESCRIPTION, description)
        putOpt(Constants.NAME, name)
    }

    companion object : SObjectDeserializerBase<AccountObject>(objectType = Constants.ACCOUNT) {
        const val KEY_DESCRIPTION = "Description"

        @Throws(CoerceException::class)
        override fun buildModel(fromJson: JSONObject): AccountObject {
            val name = fromJson.getRequiredStringOrThrow(Constants.NAME)
            return try {
                AccountObject(
                    description = fromJson.optStringOrNull(KEY_DESCRIPTION),
                    name = name
                )
            } catch (ex: AccountValidationException) {
                when (ex) {
                    is AccountValidationException.NameBlankException -> InvalidPropertyValue(
                        propertyKey = Constants.NAME,
                        allowedValuesDescription = "Account Name must not be blank",
                        offendingJsonString = fromJson.toString()
                    )
                }.let { throw it }
            }
        }

        @Throws(AccountValidationException.NameBlankException::class)
        fun validateName(name: String?) {
            if (name.isNullOrBlank()) throw AccountValidationException.NameBlankException()
        }
    }
}

sealed class AccountValidationException(override val message: String?) : Exception() {
    class NameBlankException :
        AccountValidationException(message = "Account Name must not be blank")
}

typealias AccountRecord = SObjectRecord<AccountObject>
