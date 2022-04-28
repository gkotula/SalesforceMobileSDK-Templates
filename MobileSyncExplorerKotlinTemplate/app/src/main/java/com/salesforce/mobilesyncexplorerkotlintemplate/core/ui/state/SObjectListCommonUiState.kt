package com.salesforce.mobilesyncexplorerkotlintemplate.core.ui.state

import com.salesforce.mobilesyncexplorerkotlintemplate.core.salesforceobject.SObject
import com.salesforce.mobilesyncexplorerkotlintemplate.core.salesforceobject.SObjectRecord

interface SObjectListCommonUiState<T : SObject> {
    val records: List<SObjectRecord<T>>
    val curSelectedRecordId: String?
}

interface ListSearchableUiState {
    val isSearchJobRunning: Boolean
    val curSearchTerm: String
    val onSearchTermUpdated: (newSearchTerm: String) -> Unit
}
