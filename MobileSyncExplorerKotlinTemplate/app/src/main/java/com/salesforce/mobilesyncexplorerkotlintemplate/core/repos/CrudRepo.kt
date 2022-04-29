package com.salesforce.mobilesyncexplorerkotlintemplate.core.repos

import com.salesforce.mobilesyncexplorerkotlintemplate.core.salesforceobject.SObject
import com.salesforce.mobilesyncexplorerkotlintemplate.core.salesforceobject.SObjectRecord
import kotlinx.coroutines.flow.Flow

// TODO is there a better name that doesn't include the word "crud"?
interface CrudRepo<T : SObject> {
    suspend fun allView(pageSize: UInt, pageIndex: UInt): Flow<List<SObjectRecord<T>>>
    suspend fun exactIdView(id: String): Flow<SObjectRecord<T>>

    @Throws(RepoOperationException::class)
    suspend fun locallyUpdate(id: String, so: T): SObjectRecord<T>

    @Throws(RepoOperationException::class)
    suspend fun locallyCreate(so: T): SObjectRecord<T>

    @Throws(RepoOperationException::class)
    suspend fun locallyDelete(id: String): SObjectRecord<T>?

    @Throws(RepoOperationException::class)
    suspend fun locallyUndelete(id: String): SObjectRecord<T>
}
