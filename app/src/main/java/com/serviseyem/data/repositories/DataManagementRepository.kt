package com.serviseyem.data.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await

interface IDataManagementRepository {
    suspend fun countDocuments(collectionName: String, sectionFilter: String? = null): Int
    suspend fun wipeCollectionInBatches(
        collectionName: String,
        sectionFilter: String? = null,
        performedBy: String = "ADMIN",
        onProgress: (deleted: Int, total: Int) -> Unit
    ): Result<Int>
}

class DataManagementRepositoryImpl(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : IDataManagementRepository {

    override suspend fun countDocuments(collectionName: String, sectionFilter: String?): Int {
        val cleanCollection = collectionName.trim()
        if (cleanCollection.isBlank()) return 0
        return try {
            val targetCol = if (cleanCollection == "restaurants" || cleanCollection == "medical") "stores" else cleanCollection
            val snapshot = db.collection(targetCol).get().await()
            filterDocuments(snapshot, cleanCollection, sectionFilter).size
        } catch (e: Exception) {
            0
        }
    }

    override suspend fun wipeCollectionInBatches(
        collectionName: String,
        sectionFilter: String?,
        performedBy: String,
        onProgress: (deleted: Int, total: Int) -> Unit
    ): Result<Int> {
        val cleanCollection = collectionName.trim()
        if (cleanCollection.isBlank()) {
            return Result.failure(IllegalArgumentException("اسم المجموعة فارغ"))
        }
        return try {
            val targetCol = if (cleanCollection == "restaurants" || cleanCollection == "medical") "stores" else cleanCollection
            val snapshot = db.collection(targetCol).get().await()
            val docsToDelete = filterDocuments(snapshot, cleanCollection, sectionFilter)
            val total = docsToDelete.size
            var deletedCount = 0

            if (total == 0) {
                return Result.success(0)
            }

            // Firestore batch limit is 500 documents per batch
            val batches = docsToDelete.chunked(450)
            for (chunk in batches) {
                val batch = db.batch()
                for (doc in chunk) {
                    batch.delete(doc.reference)
                }
                batch.commit().await()
                deletedCount += chunk.size
                onProgress(deletedCount, total)
            }

            // Write Audit Log
            try {
                val now = System.currentTimeMillis()
                val logDocId = "log_${now}_${java.util.UUID.randomUUID().toString().take(6)}"
                val auditLog = mapOf(
                    "logId" to logDocId,
                    "collection" to cleanCollection,
                    "targetFirestoreCollection" to targetCol,
                    "sectionFilter" to (sectionFilter ?: ""),
                    "deletedCount" to deletedCount,
                    "performedBy" to performedBy,
                    "timestamp" to now,
                    "status" to "SUCCESS"
                )
                db.collection("audit_logs").document(logDocId).set(auditLog).await()
            } catch (_: Exception) {
                // Ignore audit log failure if main deletion succeeded
            }

            Result.success(deletedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun isRestaurantDoc(doc: com.google.firebase.firestore.DocumentSnapshot): Boolean {
        val sectionId = (doc.getString("sectionId") ?: "").trim()
        val type = (doc.getString("type") ?: "").trim()
        return sectionId.equals("restaurants", ignoreCase = true) ||
            type.equals("RESTAURANT", ignoreCase = true)
    }

    private fun isMedicalDoc(doc: com.google.firebase.firestore.DocumentSnapshot): Boolean {
        val sectionId = (doc.getString("sectionId") ?: "").trim()
        val type = (doc.getString("type") ?: "").trim()
        return sectionId.equals("medical", ignoreCase = true) ||
            type.equals("MEDICAL", ignoreCase = true)
    }

    private fun filterDocuments(
        snapshot: QuerySnapshot,
        collectionName: String,
        sectionFilter: String?
    ): List<com.google.firebase.firestore.DocumentSnapshot> {
        val cleanFilter = sectionFilter?.trim().orEmpty()
        return when {
            collectionName.equals("restaurants", ignoreCase = true) ->
                snapshot.documents.filter { isRestaurantDoc(it) }
            collectionName.equals("medical", ignoreCase = true) ->
                snapshot.documents.filter { isMedicalDoc(it) }
            cleanFilter.isNotBlank() ->
                snapshot.documents.filter {
                    val docSection = (it.getString("sectionId") ?: "").trim()
                    docSection.equals(cleanFilter, ignoreCase = true)
                }
            collectionName.equals("stores", ignoreCase = true) ->
                snapshot.documents.filter { !isRestaurantDoc(it) && !isMedicalDoc(it) }
            else -> snapshot.documents
        }
    }
}
