package com.salesforce.mobilesyncexplorerkotlintemplate.contacts.detailscomponent

import com.salesforce.mobilesyncexplorerkotlintemplate.core.ui.state.SObjectUiSyncState
import com.salesforce.mobilesyncexplorerkotlintemplate.model.contacts.ContactObject

sealed interface ContactDetailsUiState {
    val doingInitialLoad: Boolean
    val recordId: String?

    val detailsFieldChangeHandler: ContactDetailsFieldChangeHandler
    val detailsClickHandler: ContactDetailsClickHandler

    data class ViewingContactDetails(
        override val recordId: String?,
        val firstNameField: ContactDetailsField.FirstName,
        val lastNameField: ContactDetailsField.LastName,
        val titleField: ContactDetailsField.Title,
        val departmentField: ContactDetailsField.Department,

        val uiSyncState: SObjectUiSyncState,
        val isEditingEnabled: Boolean,
        val shouldScrollToErrorField: Boolean,

        override val detailsClickHandler: ContactDetailsClickHandler,
        override val detailsFieldChangeHandler: ContactDetailsFieldChangeHandler,

        override val doingInitialLoad: Boolean = false
    ) : ContactDetailsUiState {
        val fullName = ContactObject.formatFullName(
            firstName = firstNameField.fieldValue,
            lastName = lastNameField.fieldValue
        )
    }

    data class NoContactSelected(
        override val doingInitialLoad: Boolean = false,
        override val detailsClickHandler: ContactDetailsClickHandler,
        override val detailsFieldChangeHandler: ContactDetailsFieldChangeHandler
    ) : ContactDetailsUiState {
        override val recordId: String? = null
    }
}

fun ContactDetailsUiState.copy(
    doingInitialLoad: Boolean = this.doingInitialLoad,
    recordId: String? = this.recordId,
    detailsFieldChangeHandler: ContactDetailsFieldChangeHandler = this.detailsFieldChangeHandler,
    detailsClickHandler: ContactDetailsClickHandler = this.detailsClickHandler
): ContactDetailsUiState = when (this) {
    is ContactDetailsUiState.NoContactSelected -> this.copy(
        doingInitialLoad = doingInitialLoad,
        recordId = recordId,
        detailsFieldChangeHandler = detailsFieldChangeHandler,
        detailsClickHandler = detailsClickHandler
    )
    is ContactDetailsUiState.ViewingContactDetails -> this.copy(
        doingInitialLoad = doingInitialLoad,
        recordId = recordId,
        detailsFieldChangeHandler = detailsFieldChangeHandler,
        detailsClickHandler = detailsClickHandler
    )
}
