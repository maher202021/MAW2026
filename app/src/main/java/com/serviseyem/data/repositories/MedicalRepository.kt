package com.serviseyem.data.repositories

import com.serviseyem.data.LocalAppCacheManager
import com.serviseyem.domain.entities.DoctorItem
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class MedicalRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val cacheManager: LocalAppCacheManager
) {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val listAdapter = moshi.adapter<List<DoctorItem>>(
        Types.newParameterizedType(List::class.java, DoctorItem::class.java)
    )

    private fun getCachedDoctors(ownerId: String): List<DoctorItem> {
        return try {
            val raw = cacheManager.getDoctorsCacheRaw(ownerId)
            if (raw.isNotBlank() && raw != "[]") {
                listAdapter.fromJson(raw) ?: emptyList()
            } else emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveCachedDoctors(ownerId: String, doctors: List<DoctorItem>) {
        try {
            cacheManager.saveDoctorsCache(ownerId, listAdapter.toJson(doctors))
        } catch (_: Exception) {}
    }

    fun getDoctors(ownerId: String): Flow<List<DoctorItem>> = callbackFlow {
        val cached = getCachedDoctors(ownerId)
        if (cached.isNotEmpty()) {
            trySend(cached)
        }

        val listener = firestore.collection("doctors")
            .whereEqualTo("ownerId", ownerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(getCachedDoctors(ownerId))
                    return@addSnapshotListener
                }
                val list = snapshot.documents.mapNotNull { doc ->
                    DoctorItem(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        specialty = doc.getString("specialty") ?: "",
                        workingHours = doc.getString("workingHours") ?: ""
                    )
                }
                saveCachedDoctors(ownerId, list)
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addDoctor(ownerId: String, doctor: DoctorItem): Result<String> {
        return try {
            val docId = doctor.id.ifBlank { java.util.UUID.randomUUID().toString() }
            val data = mapOf(
                "id" to docId,
                "ownerId" to ownerId,
                "name" to doctor.name,
                "specialty" to doctor.specialty,
                "workingHours" to doctor.workingHours,
                "createdAt" to System.currentTimeMillis()
            )
            firestore.collection("doctors").document(docId).set(data).await()
            Result.success(docId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDoctor(id: String): Result<Unit> {
        if (id.isBlank()) return Result.failure(IllegalArgumentException("Doctor ID is blank"))
        return try {
            firestore.collection("doctors").document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
