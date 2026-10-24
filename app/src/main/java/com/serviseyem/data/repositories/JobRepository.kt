package com.serviseyem.data.repositories

import com.serviseyem.data.LocalAppCacheManager
import com.serviseyem.domain.entities.JobPostItem
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class JobRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val cacheManager: LocalAppCacheManager
) {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val listAdapter = moshi.adapter<List<JobPostItem>>(
        Types.newParameterizedType(List::class.java, JobPostItem::class.java)
    )

    private fun getCachedJobs(ownerId: String): List<JobPostItem> {
        return try {
            val raw = cacheManager.getJobsCacheRaw(ownerId)
            if (raw.isNotBlank() && raw != "[]") {
                listAdapter.fromJson(raw) ?: emptyList()
            } else emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveCachedJobs(ownerId: String, jobs: List<JobPostItem>) {
        try {
            cacheManager.saveJobsCache(ownerId, listAdapter.toJson(jobs))
        } catch (_: Exception) {}
    }

    fun getJobs(ownerId: String): Flow<List<JobPostItem>> = callbackFlow {
        val cached = getCachedJobs(ownerId)
        if (cached.isNotEmpty()) {
            trySend(cached)
        }

        val listener = firestore.collection(com.serviseyem.utils.AppConstants.COL_JOBS)
            .whereEqualTo("ownerId", ownerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(getCachedJobs(ownerId))
                    return@addSnapshotListener
                }
                val list = snapshot.documents.mapNotNull { doc ->
                    JobPostItem(
                        id = doc.id,
                        title = doc.getString("title") ?: doc.getString("jobTitle") ?: "",
                        companyName = doc.getString("companyName") ?: "",
                        salary = doc.getString("salary") ?: doc.getString("salaryRange") ?: "",
                        requirements = doc.getString("requirements") ?: doc.getString("jobRequirements") ?: "",
                        applicantsCount = doc.getLong("applicantsCount")?.toInt() ?: 0
                    )
                }
                saveCachedJobs(ownerId, list)
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun postJob(ownerId: String, job: JobPostItem): Result<String> {
        return try {
            val docId = job.id.ifBlank { java.util.UUID.randomUUID().toString() }
            val data = mapOf(
                "id" to docId,
                "ownerId" to ownerId,
                "title" to job.title,
                "jobTitle" to job.title,
                "companyName" to job.companyName,
                "salary" to job.salary,
                "salaryRange" to job.salary,
                "requirements" to job.requirements,
                "applicantsCount" to job.applicantsCount,
                "createdAt" to System.currentTimeMillis()
            )
            firestore.collection(com.serviseyem.utils.AppConstants.COL_JOBS).document(docId).set(data).await()
            Result.success(docId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteJob(id: String): Result<Unit> {
        if (id.isBlank()) return Result.failure(IllegalArgumentException("Job ID is blank"))
        return try {
            firestore.collection(com.serviseyem.utils.AppConstants.COL_JOBS).document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
