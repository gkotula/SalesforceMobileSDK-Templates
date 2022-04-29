package com.salesforce.mobilesyncexplorerkotlintemplate.model.accounts

import com.salesforce.mobilesyncexplorerkotlintemplate.core.repos.CrudRepo
import com.salesforce.mobilesyncexplorerkotlintemplate.model.contacts.ContactObject
import kotlinx.coroutines.flow.Flow

// WIP
interface AccountsRepo : CrudRepo<AccountObject> {
    val relatedContactsView: Flow<ContactObject>
}
