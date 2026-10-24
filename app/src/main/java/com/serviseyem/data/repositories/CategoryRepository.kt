package com.serviseyem.data.repositories

import com.serviseyem.data.CategoryEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * 🏷️ CategoryRepository
 * عزل منطق استرجاع وتحديث الفئات من Firestore باتباع Clean Architecture و Flow
 */
class CategoryRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    fun observeCategories(): Flow<Result<List<CategoryEntity>>> = callbackFlow {
        var registration: ListenerRegistration? = null
        try {
            registration = db.collection("categories")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(Result.failure(error))
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val fetched = snapshot.documents.mapNotNull { doc ->
                            try {
                                val obj = doc.toObject(CategoryEntity::class.java)
                                if (obj != null) {
                                    if (obj.id.isEmpty()) obj.copy(id = doc.id) else obj
                                } else null
                            } catch (e: Exception) {
                                null
                            }
                        }.distinctBy { it.id }.sortedWith(
                            compareByDescending<CategoryEntity> { it.isPinned }
                                .thenBy { it.order }
                        )
                        trySend(Result.success(fetched))
                    }
                }
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }

        awaitClose {
            registration?.remove()
        }
    }
}
