package com.salesforce.mobilesyncexplorerkotlintemplate.model.contacts

import com.salesforce.mobilesyncexplorerkotlintemplate.core.repos.CrudRepo
import com.salesforce.mobilesyncexplorerkotlintemplate.model.accounts.AccountRecord
import kotlinx.coroutines.flow.Flow

interface ContactsRepo : CrudRepo<ContactObject> {
    fun allWithRelatedAccountsView(pageSize: UInt, pageIndex: UInt): Flow<Map<String, ContactWithRelatedAccount>>

    data class ContactWithRelatedAccount(val contact: ContactRecord, val account: AccountRecord?)
}
