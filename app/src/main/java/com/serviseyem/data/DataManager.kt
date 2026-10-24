package com.serviseyem.data

import com.serviseyem.utils.AppConstants
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

object DataManager {
    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Mapper converting a Firestore document (including legacy Review fields if present) into the unified RatingEntity.
     */
    fun mapDocumentToRatingEntity(doc: DocumentSnapshot): RatingEntity? {
        return try {
            val entity = doc.toObject(RatingEntity::class.java)?.copy(id = doc.id)
            if (entity != null) {
                val resolvedTargetId = entity.targetId.ifBlank { doc.getString("shopId") ?: doc.getString("providerId") ?: "" }
                val resolvedComment = entity.comment.ifBlank { doc.getString("text") ?: "" }
                entity.copy(
                    targetId = resolvedTargetId,
                    comment = resolvedComment
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun submitReview(
        shopId: String,
        ratingEntity: RatingEntity,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        val docId = ratingEntity.id.ifBlank { java.util.UUID.randomUUID().toString() }
        val normalized = ratingEntity.copy(
            id = docId,
            targetId = shopId.ifBlank { ratingEntity.targetId },
            targetType = ratingEntity.targetType.ifBlank { "STORE" }
        )
        firestore.collection(AppConstants.COL_RATINGS)
            .document(docId)
            .set(normalized)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun getRatingReviews(shopId: String): Flow<List<RatingEntity>> = callbackFlow {
        val listener = firestore.collection(AppConstants.COL_RATINGS)
            .whereEqualTo("targetId", shopId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val reviewsList = snapshot.documents.mapNotNull { doc ->
                        mapDocumentToRatingEntity(doc)
                    }.sortedByDescending { it.timestamp }
                    trySend(reviewsList)
                }
            }
        awaitClose { listener.remove() }
    }

    fun getReviews(shopId: String): Flow<List<RatingEntity>> = getRatingReviews(shopId)
}
