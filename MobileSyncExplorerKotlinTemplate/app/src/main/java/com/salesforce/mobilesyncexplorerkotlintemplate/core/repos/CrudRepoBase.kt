package com.salesforce.mobilesyncexplorerkotlintemplate.core.repos

import com.salesforce.androidsdk.accounts.UserAccount
import com.salesforce.androidsdk.mobilesync.app.MobileSyncSDKManager
import com.salesforce.androidsdk.mobilesync.target.SyncTarget
import com.salesforce.androidsdk.mobilesync.util.Constants
import com.salesforce.androidsdk.smartstore.store.SmartStore
import com.salesforce.mobilesyncexplorerkotlintemplate.app.MobileSyncExplorerKotlinTemplateApp.Companion.appScope
import com.salesforce.mobilesyncexplorerkotlintemplate.core.extensions.map
import com.salesforce.mobilesyncexplorerkotlintemplate.core.extensions.partitionBySuccess
import com.salesforce.mobilesyncexplorerkotlintemplate.core.extensions.retrieveSingleById
import com.salesforce.mobilesyncexplorerkotlintemplate.core.salesforceobject.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject

abstract class CrudRepoBase<T : SObject>(
    userAccount: UserAccount,
    protected val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : CrudRepo<T> {
    protected abstract val soupName: String
    protected abstract val deserializer: SObjectDeserializer<T>

    protected val store: SmartStore = MobileSyncSDKManager.getInstance().getSmartStore(userAccount)

    private val viewsMutex = Mutex()
    protected val exactIdQueryViews = mutableMapOf<String, SharedFlow<SObjectRecord<T>>>()

    protected data class PageSizeAndIndex(val pageSize: UInt, val pageIndex: UInt)

    protected val allQueryViews =
        mutableMapOf<PageSizeAndIndex, SharedFlow<List<SObjectRecord<T>>>>()

    init {
        MobileSyncSDKManager.getInstance().apply {
            setupUserStoreFromDefaultConfig()
            setupUserSyncsFromDefaultConfig()
        }
    }

    override suspend fun allView(pageSize: UInt, pageIndex: UInt): Flow<List<SObjectRecord<T>>> =
        viewsMutex.withLock {
            val key = PageSizeAndIndex(pageSize = pageSize, pageIndex = pageIndex)
            allQueryViews[key]?.let { return@withLock it }

            val flow = AllRecordsView(
                soupName = soupName,
                pageSize = pageSize,
                pageIndex = pageIndex,
                store = store
            )
                .items
                .map { array ->
                    // TODO how to handle parse failures?
                    array.map { runCatching { deserializer.coerceFromJsonOrThrow(it) } }
                        .partitionBySuccess()
                        .successes
                }
                .shareIn(appScope, started = SharingStarted.WhileSubscribed(), replay = 1)

            allQueryViews[key] = flow
            flow
        }

    override suspend fun exactIdView(id: String): Flow<SObjectRecord<T>> = viewsMutex.withLock {
        exactIdQueryViews[id]?.let { return@withLock it }

        val flow = ExactIdView(
            soupName = soupName,
            id = id,
            store = store
        ).items
            .map { deserializer.coerceFromJsonOrThrow(it) }
            .shareIn(appScope, started = SharingStarted.WhileSubscribed(), replay = 1)

        exactIdQueryViews[id] = flow
        flow
    }
}
