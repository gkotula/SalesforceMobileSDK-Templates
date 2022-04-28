package com.salesforce.mobilesyncexplorerkotlintemplate.accounts.listcomponent

import com.salesforce.mobilesyncexplorerkotlintemplate.core.ui.state.ListSearchableUiState
import com.salesforce.mobilesyncexplorerkotlintemplate.core.ui.state.SObjectListCommonUiState
import com.salesforce.mobilesyncexplorerkotlintemplate.model.accounts.AccountObject
import com.salesforce.mobilesyncexplorerkotlintemplate.model.accounts.AccountRecord

data class AccountsListUiState(
    override val records: List<AccountRecord>,
    override val curSelectedRecordId: String?,
    val isDoingInitialLoad: Boolean,
    override val isSearchJobRunning: Boolean,
    override val curSearchTerm: String = "",
    override val onSearchTermUpdated: (newSearchTerm: String) -> Unit
) : SObjectListCommonUiState<AccountObject>, ListSearchableUiState
