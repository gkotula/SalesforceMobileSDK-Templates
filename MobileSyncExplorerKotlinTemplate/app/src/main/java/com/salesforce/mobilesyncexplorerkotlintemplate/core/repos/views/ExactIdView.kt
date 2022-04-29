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
package com.salesforce.mobilesyncexplorerkotlintemplate.core.repos.views

import com.salesforce.androidsdk.mobilesync.util.Constants
import com.salesforce.androidsdk.smartstore.store.QuerySpec
import com.salesforce.androidsdk.smartstore.store.SmartStore
import com.salesforce.androidsdk.smartstore.store.SmartStore.Companion.SOUP_ENTRY_ID
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

    // TODO what happens when the row is deleted?  Do we close the view, or
    private var soupId = INVALID_SOUP_ID

    @Throws(NoSuchElementException::class)
    private suspend fun runQuery() = withContext(ioDispatcher) {
        // TODO how to handle exceptions?
        val results = store.query(querySpec = query, pageIndex = 1)

        if (results.length() > 0) {
            results.getJSONObject(0)
        } else {
            throw NoSuchElementException()
        }
    }

    val items: Flow<JSONObject> = StoreUpdatesEventBus.bus
        .filter { event -> event.soupNamesToUpdates[soupName]?.updates?.contains(soupId) == true }
        .map { runQuery() }
        .onStart {
            val result = runQuery()

            soupId = result.optInt(SOUP_ENTRY_ID, INVALID_SOUP_ID)

            if (soupId == INVALID_SOUP_ID) {
                throw NoSuchElementException()
            }

            emit(result)
        }
        .flowOn(ioDispatcher)
        .distinctUntilChanged()

    private companion object {
        private const val INVALID_SOUP_ID = -1
    }
}
