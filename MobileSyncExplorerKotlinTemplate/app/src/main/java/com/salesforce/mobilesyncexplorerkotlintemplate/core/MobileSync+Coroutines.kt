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
package com.salesforce.mobilesyncexplorerkotlintemplate.core

import com.salesforce.androidsdk.mobilesync.manager.SyncManager
import com.salesforce.androidsdk.mobilesync.util.SyncState
import com.salesforce.mobilesyncexplorerkotlintemplate.core.repos.SyncException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Throws(CleanResyncGhostsException::class)
suspend fun SyncManager.suspendCleanResyncGhosts(syncName: String) = withContext(NonCancellable) {
    suspendCoroutine<Int> { cont ->
        val callback = object : SyncManager.CleanResyncGhostsCallback {
            override fun onSuccess(numRecords: Int) = cont.resume(numRecords)

            override fun onError(e: java.lang.Exception) {
                cont.resumeWithException(
                    CleanResyncGhostsException.FailedToFinish(
                        message = "Clean Resync Ghosts failed to run to completion",
                        cause = e
                    )
                )
            }
        }

        try {
            cleanResyncGhosts(syncName, callback)
        } catch (ex: Exception) {
            throw CleanResyncGhostsException.FailedToStart(
                message = "Clean Resync Ghosts operation failed to start",
                cause = ex
            )
        }
    }
}

sealed class CleanResyncGhostsException : Exception() {
    data class FailedToFinish(
        override val message: String?,
        override val cause: Throwable? = null
    ) : CleanResyncGhostsException()

    data class FailedToStart(
        override val message: String?,
        override val cause: Throwable?
    ) : CleanResyncGhostsException()
}

@Throws(SyncException::class)
suspend fun SyncManager.suspendReSync(syncName: String) = withContext(NonCancellable) {
    suspendCoroutine<Unit> { cont ->
        val callback: (SyncState) -> Unit = {
            when (it.status) {
                // terminal states
                SyncState.Status.DONE -> cont.resume(Unit)
                SyncState.Status.FAILED,
                SyncState.Status.STOPPED -> cont.resumeWithException(
                    SyncException.FailedToFinish(
                        message = "Sync Down operation failed with terminal Sync State = $it"
                    )
                )

                SyncState.Status.NEW,
                SyncState.Status.RUNNING,
                null -> {
                    /* no-op; suspending for terminal state */
                }
            }
        }

        try {
            reSync(syncName, callback)
        } catch (ex: Exception) {
            cont.resumeWithException(SyncException.FailedToStart(cause = ex))
        }
    }
}
