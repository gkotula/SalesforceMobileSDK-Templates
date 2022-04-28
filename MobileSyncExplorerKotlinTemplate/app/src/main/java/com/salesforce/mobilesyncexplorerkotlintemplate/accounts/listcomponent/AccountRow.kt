package com.salesforce.mobilesyncexplorerkotlintemplate.accounts.listcomponent

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.salesforce.mobilesyncexplorerkotlintemplate.contacts.activity.SyncImage
import com.salesforce.mobilesyncexplorerkotlintemplate.core.salesforceobject.LocalStatus
import com.salesforce.mobilesyncexplorerkotlintemplate.core.salesforceobject.SObjectRecord
import com.salesforce.mobilesyncexplorerkotlintemplate.core.ui.components.SObjectListContent
import com.salesforce.mobilesyncexplorerkotlintemplate.core.ui.state.SObjectUiSyncState
import com.salesforce.mobilesyncexplorerkotlintemplate.core.ui.state.toUiSyncState
import com.salesforce.mobilesyncexplorerkotlintemplate.core.ui.theme.SalesforceMobileSDKAndroidTheme
import com.salesforce.mobilesyncexplorerkotlintemplate.model.accounts.AccountObject

@Composable
fun AccountRow(
    modifier: Modifier = Modifier,
    model: AccountObject,
    syncState: SObjectUiSyncState,
    onRowClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clickable(onClick = onRowClick)
            .padding(8.dp)
    ) {
        Text(
            model.name,
            modifier = Modifier.weight(1f)
        )

        SyncImage(uiSyncState = syncState)
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun AccountRowPreview() {
    val searchTerm = "2"
    val accounts = (1..20).map {
        SObjectRecord(
            id = it.toString(),
            localStatus = LocalStatus.MatchesUpstream,
            sObject = AccountObject(
                description = "Description",
                name = "Name $it"
            )
        )
    }.filter { it.sObject.name.contains(searchTerm, ignoreCase = true) }

    val accountUiState = AccountsListUiState(
        records = accounts,
        curSelectedRecordId = "2",
        isDoingInitialLoad = false,
        isSearchJobRunning = false,
        curSearchTerm = searchTerm,
        onSearchTermUpdated = {}
    )

    SalesforceMobileSDKAndroidTheme {
        Surface {
            SObjectListContent(listUiState = accountUiState, searchUiState = accountUiState) {
                AccountRow(
                    model = it.sObject,
                    syncState = it.localStatus.toUiSyncState(),
                    onRowClick = {}
                )
            }
        }
    }
}
