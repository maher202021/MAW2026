package com.serviseyem.data.repositories

import android.content.Context
import com.serviseyem.data.PropertyEntity
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.StoreEntity

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MapRepository(context: Context? = null) {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    suspend fun getProviders(): List<ProviderEntity> {
        return try {
            val snapshot = try {
                firestore.collection("providers")
                    .whereEqualTo("isDeleted", false)
                    .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(200)
                    .get()
                    .await()
            } catch (_: Exception) {
                firestore.collection("providers")
                    .whereEqualTo("isDeleted", false)
                    .limit(200)
                    .get()
                    .await()
            }
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(ProviderEntity::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getStores(): List<StoreEntity> {
        return try {
            val snapshot = try {
                firestore.collection("stores")
                    .whereEqualTo("isDeleted", false)
                    .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(200)
                    .get()
                    .await()
            } catch (_: Exception) {
                firestore.collection("stores")
                    .whereEqualTo("isDeleted", false)
                    .limit(200)
                    .get()
                    .await()
            }
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(StoreEntity::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getProperties(): List<PropertyEntity> {
        return try {
            val snapshot = try {
                firestore.collection("properties")
                    .whereEqualTo("isDeleted", false)
                    .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(200)
                    .get()
                    .await()
            } catch (_: Exception) {
                firestore.collection("properties")
                    .whereEqualTo("isDeleted", false)
                    .limit(200)
                    .get()
                    .await()
            }
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(PropertyEntity::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
