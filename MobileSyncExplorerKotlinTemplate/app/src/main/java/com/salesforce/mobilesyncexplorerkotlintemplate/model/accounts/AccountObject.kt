package com.salesforce.mobilesyncexplorerkotlintemplate.model.accounts

import com.salesforce.androidsdk.mobilesync.util.Constants
import com.salesforce.mobilesyncexplorerkotlintemplate.core.extensions.optStringOrNull
import com.salesforce.mobilesyncexplorerkotlintemplate.core.salesforceobject.SObject
import com.salesforce.mobilesyncexplorerkotlintemplate.core.salesforceobject.SObjectDeserializerBase
import com.salesforce.mobilesyncexplorerkotlintemplate.core.salesforceobject.SObjectRecord
import org.json.JSONObject

data class AccountObject(
    val description: String?,
    val name: String?
) : SObject {
    override fun JSONObject.applyObjProperties(): JSONObject = this.apply {
        putOpt(KEY_DESCRIPTION, description)
        putOpt(Constants.NAME, name)
    }

    companion object : SObjectDeserializerBase<AccountObject>(objectType = Constants.ACCOUNT) {
        const val KEY_DESCRIPTION = "Description"

        override fun buildModel(fromJson: JSONObject): AccountObject {
            return AccountObject(
                description = fromJson.optStringOrNull(KEY_DESCRIPTION),
                name = fromJson.optStringOrNull(Constants.NAME)
            )
        }
    }
}

typealias AccountRecord = SObjectRecord<AccountObject>
