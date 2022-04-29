/*
 * Copyright (c) 2022-present, salesforce.com, inc.
 * All rights reserved.
 * Redistribution and use of this software in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met:
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * - Neither the name of salesforce.com, inc. nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission of salesforce.com, inc.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.salesforce.mobilesyncexplorerkotlintemplate.core.repos

import com.salesforce.androidsdk.accounts.UserAccount
import com.salesforce.androidsdk.mobilesync.app.MobileSyncSDKManager
import com.salesforce.androidsdk.smartstore.store.SmartStore
import com.salesforce.mobilesyncexplorerkotlintemplate.app.MobileSyncExplorerKotlinTemplateApp.Companion.appScope
import com.salesforce.mobilesyncexplorerkotlintemplate.core.extensions.map
import com.salesforce.mobilesyncexplorerkotlintemplate.core.extensions.partitionBySuccess
import com.salesforce.mobilesyncexplorerkotlintemplate.core.repos.views.AllRecordsView
import com.salesforce.mobilesyncexplorerkotlintemplate.core.repos.views.ExactIdView
import com.salesforce.mobilesyncexplorerkotlintemplate.core.salesforceobject.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

abstract class CrudRepoBase<T : SObject>(
    userAccount: UserAccount,
    protected val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    protected val workerDispatcher: CoroutineDispatcher = Dispatchers.Default
) : CrudRepo<T> {
    protected abstract val soupName: String
    protected abstract val deserializer: SObjectDeserializer<T>

    protected val store: SmartStore = MobileSyncSDKManager.getInstance().getSmartStore(userAccount)

    private val viewsMutex = Mutex()
    protected val exactIdQueryViews = mutableMapOf<String, SharedFlow<SObjectRecord<T>>>()

    protected data class PageSizeAndIndex(val pageSize: UInt, val pageIndex: UInt)

    protected val allQueryViews =
        mutableMapOf<PageSizeAndIndex, SharedFlow<Map<String, SObjectRecord<T>>>>()

    init {
        MobileSyncSDKManager.getInstance().apply {
            setupUserStoreFromDefaultConfig()
            setupUserSyncsFromDefaultConfig()
        }
    }

    override suspend fun allView(pageSize: UInt, pageIndex: UInt): Flow<Map<String, SObjectRecord<T>>> =
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
                        .associateBy { it.id }
                }
                .flowOn(workerDispatcher)
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
            .flowOn(workerDispatcher)
            .shareIn(appScope, started = SharingStarted.WhileSubscribed(), replay = 1)

        exactIdQueryViews[id] = flow
        flow
    }
}
