package com.serviseyem.ui.viewmodels

import com.google.firebase.firestore.FirebaseFirestore
import com.serviseyem.ui.*
import kotlinx.coroutines.tasks.await

class AdminCrudOperations(private val db: FirebaseFirestore) {

    companion object {
        val ALLOWED_COLLECTIONS = setOf(
            "providers",
            "pending_providers",
            "join_requests",
            "stores",
            "properties",
            "products",
            "jobs",
            "job_listings",
            "job_applications",
            "job_seekers",
            "bookings",
            "orders",
            "instant_requests",
            "special_offers",
            "offers",
            "coupons",
            "notifications",
            "supervisors",
            "admins",
            "admin_users",
            "users",
            "registered_users",
            "ratings",
            "reviews",
            "reports",
            "banners",
            "categories",
            "cities",
            "custom_profile_tabs",
            "settings",
            "settings_backups",
            "activity_logs",
            "admin_audit_logs",
            "audit_logs",
            "calls",
            "payment_wallets",
            "internal_wallets",
            "wallet_transactions",
            "payments",
            "transactions",
            "password_recovery_requests",
            "password_resets",
            "fcm_tokens",
            "chat_channels",
            "messages"
        )
    }

    private fun validateCollectionAndId(collection: String, id: String) {
        require(collection in ALLOWED_COLLECTIONS) {
            "Unauthorized or unknown Firestore collection: $collection"
        }
        require(id.isNotBlank()) {
            "Document ID cannot be blank for collection: $collection"
        }
    }

    suspend fun <T : Any> saveEntity(
        collection: String,
        id: String,
        data: T,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        try {
            validateCollectionAndId(collection, id)
            db.collection(collection).document(id).set(data).await()
            onSuccess()
        } catch (e: Exception) {
            com.serviseyem.utils.AppErrorLogManager.logFirestoreError("AdminCrudOperations", "Error saving entity in $collection/$id", e)
            onError(e)
        }
    }

    suspend fun deleteEntity(
        collection: String,
        id: String,
        softDelete: Boolean = true,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        try {
            validateCollectionAndId(collection, id)
            if (softDelete) {
                val now = System.currentTimeMillis()
                try {
                    db.collection(collection).document(id)
                        .update("isDeleted", true, "deletedAt", now)
                        .await()
                } catch (_: Exception) {
                    db.collection(collection).document(id)
                        .set(mapOf("isDeleted" to true, "deletedAt" to now), com.google.firebase.firestore.SetOptions.merge())
                        .await()
                }
            } else {
                db.collection(collection).document(id).delete().await()
            }
            onSuccess()
        } catch (e: Exception) {
            com.serviseyem.utils.AppErrorLogManager.logFirestoreError("AdminCrudOperations", "Error deleting entity in $collection/$id (softDelete=$softDelete)", e)
            onError(e)
        }
    }

    suspend fun toggleEntityStatus(
        collection: String,
        id: String,
        field: String,
        value: Boolean,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        try {
            validateCollectionAndId(collection, id)
            try {
                db.collection(collection).document(id).update(field, value).await()
            } catch (_: Exception) {
                db.collection(collection).document(id)
                    .set(mapOf(field to value), com.google.firebase.firestore.SetOptions.merge())
                    .await()
            }
            onSuccess()
        } catch (e: Exception) {
            com.serviseyem.utils.AppErrorLogManager.logFirestoreError("AdminCrudOperations", "Error toggling $field in $collection/$id", e)
            onError(e)
        }
    }

    suspend fun updateFields(
        collection: String,
        id: String,
        fields: Map<String, Any?>,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        try {
            validateCollectionAndId(collection, id)
            try {
                db.collection(collection).document(id).update(fields).await()
            } catch (_: Exception) {
                db.collection(collection).document(id)
                    .set(fields, com.google.firebase.firestore.SetOptions.merge())
                    .await()
            }
            onSuccess()
        } catch (e: Exception) {
            com.serviseyem.utils.AppErrorLogManager.logFirestoreError("AdminCrudOperations", "Error updating fields in $collection/$id", e)
            onError(e)
        }
    }
}
