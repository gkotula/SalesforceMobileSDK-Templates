package com.salesforce.mobilesyncexplorerkotlintemplate.core.ui.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.salesforce.mobilesyncexplorerkotlintemplate.R
import com.salesforce.mobilesyncexplorerkotlintemplate.contacts.listcomponent.ui.ContactCard
import com.salesforce.mobilesyncexplorerkotlintemplate.core.salesforceobject.LocalStatus
import com.salesforce.mobilesyncexplorerkotlintemplate.core.salesforceobject.SObject
import com.salesforce.mobilesyncexplorerkotlintemplate.core.salesforceobject.SObjectRecord
import com.salesforce.mobilesyncexplorerkotlintemplate.core.ui.state.ListSearchableUiState
import com.salesforce.mobilesyncexplorerkotlintemplate.core.ui.state.SObjectListCommonUiState
import com.salesforce.mobilesyncexplorerkotlintemplate.core.ui.state.toUiSyncState
import com.salesforce.mobilesyncexplorerkotlintemplate.core.ui.theme.SalesforceMobileSDKAndroidTheme
import com.salesforce.mobilesyncexplorerkotlintemplate.model.contacts.ContactObject

@Composable
fun <T : SObject> SObjectListContent(
    modifier: Modifier = Modifier,
    listUiState: SObjectListCommonUiState<T>,
    searchUiState: ListSearchableUiState?,
    itemContent: @Composable (SObjectRecord<T>) -> Unit
) {
    LazyColumn(modifier = modifier) {
        if (searchUiState != null) {
            SearchBar(uiState = searchUiState)
        }

        items(items = listUiState.records, key = { it.id }) { record ->
            itemContent(record)
        }
    }
}

private fun LazyListScope.SearchBar(uiState: ListSearchableUiState) {
    item {
        val searchTerm = uiState.curSearchTerm
        val isSearchActive = uiState.isSearchJobRunning
        FloatingTextEntryBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            value = searchTerm,
            onValueChange = uiState.onSearchTermUpdated,
            placeholder = { Text(stringResource(id = R.string.cta_search)) },
            leadingIcon = {
                if (isSearchActive) {
                    val angle: Float by rememberSimpleSpinAnimation(hertz = 1f)
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = stringResource(id = R.string.cta_search),
                        modifier = Modifier.graphicsLayer { rotationZ = angle }
                    )
                } else {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = stringResource(id = R.string.cta_search)
                    )
                }
            },
            trailingIcon = {
                if (searchTerm.isNotBlank()) {
                    IconButton(onClick = { uiState.onSearchTermUpdated("") }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(id = R.string.content_desc_cancel_search)
                        )
                    }
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun SObjectListContentPreview() {
    data class PreviewListUiState(
        override val records: List<SObjectRecord<ContactObject>>,
        override val curSelectedRecordId: String?,
        val isDoingInitialLoad: Boolean,
        val isDoingDataAction: Boolean,
        override val isSearchJobRunning: Boolean,
        override val curSearchTerm: String,
        override val onSearchTermUpdated: (newSearchTerm: String) -> Unit
    ) : SObjectListCommonUiState<ContactObject>, ListSearchableUiState

    val searchTerm = "5"

    val contacts = (1..30).map {
        SObjectRecord(
            id = it.toString(),
            localStatus = LocalStatus.MatchesUpstream,
            sObject = ContactObject(
                accountId = null,
                firstName = "First",
                lastName = "Last $it",
                title = "Title",
                department = "Department"
            )
        )
    }.filter { it.sObject.fullName.contains(searchTerm, ignoreCase = true) }

    val uiState = PreviewListUiState(
        records = contacts,
        curSelectedRecordId = "2",
        isDoingInitialLoad = false,
        isDoingDataAction = false,
        isSearchJobRunning = false,
        curSearchTerm = searchTerm,
        onSearchTermUpdated = {}
    )

    SalesforceMobileSDKAndroidTheme {
        Surface {
            SObjectListContent(
                modifier = Modifier.padding(8.dp),
                listUiState = uiState,
                searchUiState = uiState
            ) {
                ContactCard(
                    modifier = Modifier.padding(4.dp),
                    model = it.sObject,
                    syncState = it.localStatus.toUiSyncState(),
                    onCardClick = { /*TODO*/ },
                    onDeleteClick = { /*TODO*/ },
                    onUndeleteClick = { /*TODO*/ },
                    onEditClick = { /*TODO*/ })
            }
        }
    }
}
