package com.salesforce.mobilesyncexplorerkotlintemplate.core.repos

import com.salesforce.androidsdk.smartstore.store.QuerySpec
import com.salesforce.androidsdk.smartstore.store.SmartStore
import com.salesforce.androidsdk.smartstore.store.StoreUpdatesEventBus
import com.salesforce.mobilesyncexplorerkotlintemplate.core.extensions.coerceToPositiveInt
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.json.JSONArray

class AllRecordsView(
    val soupName: String,
    pageSize: UInt,
    pageIndex: UInt,
    private val store: SmartStore,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    val pageSize: Int = pageSize.coerceToPositiveInt()
    val pageIndex: Int = pageIndex.coerceToPositiveInt()
    val query = QuerySpec.buildAllQuerySpec(soupName, null, null, this.pageSize)

    private suspend fun runQuery() = withContext(ioDispatcher) {
        // TODO how to handle exceptions?
        store.query(querySpec = query, pageIndex = pageIndex)
    }

    val items = flow {
        StoreUpdatesEventBus.bus
            .filter { it.soupNamesToUpdates[soupName] != null }
            .collect { emit(runQuery()) }

        emit(runQuery())
    }.distinctUntilChanged()
}
