package com.salesforce.mobilesyncexplorerkotlintemplate.core.repos

import com.salesforce.androidsdk.mobilesync.util.Constants
import com.salesforce.androidsdk.smartstore.store.QuerySpec
import com.salesforce.androidsdk.smartstore.store.SmartStore
import com.salesforce.androidsdk.smartstore.store.StoreUpdatesEventBus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import org.json.JSONObject

class ExactIdView(
    val id: String,
    val soupName: String,
    private val store: SmartStore,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    val query = QuerySpec.buildExactQuerySpec(
        soupName,
        Constants.ID,
        id,
        null,
        null,
        1
    )

    private suspend fun runQuery() = withContext(ioDispatcher) {
        // TODO how to handle exceptions?
        val results = store.query(querySpec = query, pageIndex = 1)

        if (results.length() != 1) {
            TODO("More than one record returned with ID = $id: $results")
        }

        results.getJSONObject(0)
    }

    val items = flow<JSONObject> {
        StoreUpdatesEventBus.bus
            .filter { event -> event.soupNamesToUpdates[soupName]?.updates?.contains(id) == true }
            .collect { emit(runQuery()) }

        emit(runQuery())
    }.distinctUntilChanged()
}
