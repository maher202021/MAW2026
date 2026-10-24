package com.serviseyem.data.repositories

import com.serviseyem.data.SpecialOfferEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class DashboardExtensionsRepositoryImpl : IDashboardExtensionsRepository {
    private val firestore = FirebaseFirestore.getInstance()

    override fun getSpecialOffers(ownerId: String): Flow<List<SpecialOfferEntity>> = callbackFlow {
        val query = if (ownerId.isNotBlank()) {
            firestore.collection("special_offers").whereEqualTo("providerId", ownerId)
        } else {
            firestore.collection("special_offers")
        }
        val listener = query.addSnapshotListener { snap, _ ->
            if (snap != null) {
                trySend(snap.documents.mapNotNull { it.toObject(SpecialOfferEntity::class.java)?.copy(id = it.id) })
            } else {
                trySend(emptyList())
            }
        }
        awaitClose { listener.remove() }
    }

    // ✨ م2-ج2: دوال العروض الخاصة عبر الـ Repository
    override fun updateSpecialOfferStatus(offerId: String, isEnabled: Boolean) {
        if (offerId.isBlank()) return
        try {
            firestore.collection("special_offers")
                .document(offerId)
                .update("isEnabled", isEnabled)
        } catch (e: Exception) {
            android.util.Log.e("DashboardExtRepo", "Failed to update offer status", e)
        }
    }

    override fun deleteSpecialOffer(offerId: String) {
        if (offerId.isBlank()) return
        try {
            firestore.collection("special_offers")
                .document(offerId)
                .delete()
        } catch (e: Exception) {
            android.util.Log.e("DashboardExtRepo", "Failed to delete offer", e)
        }
    }

    override fun addSpecialOffer(offer: SpecialOfferEntity) {
        try {
            val docId = offer.id.ifBlank { java.util.UUID.randomUUID().toString() }
            firestore.collection("special_offers")
                .document(docId)
                .set(offer.copy(id = docId))
        } catch (e: Exception) {
            android.util.Log.e("DashboardExtRepo", "Failed to add offer", e)
        }
    }

    override fun getCoupons(ownerId: String): Flow<List<SpecialOfferEntity>> = callbackFlow {
        val query = if (ownerId.isNotBlank()) {
            firestore.collection("coupons").whereEqualTo("providerId", ownerId)
        } else {
            firestore.collection("coupons")
        }
        val listener = query.addSnapshotListener { snap, _ ->
            if (snap != null) {
                val list = snap.documents.mapNotNull { doc ->
                    val base = doc.toObject(SpecialOfferEntity::class.java)
                    if (base != null) {
                        val code = base.couponCode.ifBlank { doc.getString("code") ?: "" }
                        val discount = if (base.discountPercent > 0) base.discountPercent else (doc.getDouble("discountPercentage")?.toInt() ?: 0)
                        val enabled = doc.getBoolean("isEnabled") ?: doc.getBoolean("isActive") ?: base.isEnabled
                        base.copy(
                            id = doc.id,
                            couponCode = code,
                            discountPercent = discount,
                            isEnabled = enabled
                        )
                    } else null
                }
                trySend(list)
            } else {
                trySend(emptyList())
            }
        }
        awaitClose { listener.remove() }
    }

    // ✨ م2-ج2: دوال الكوبونات عبر الـ Repository
    override fun updateCouponStatus(couponId: String, isEnabled: Boolean) {
        if (couponId.isBlank()) return
        try {
            firestore.collection("coupons")
                .document(couponId)
                .update(mapOf("isEnabled" to isEnabled, "isActive" to isEnabled))
        } catch (e: Exception) {
            android.util.Log.e("DashboardExtRepo", "Failed to update coupon status", e)
        }
    }

    override fun deleteCoupon(couponId: String) {
        if (couponId.isBlank()) return
        try {
            firestore.collection("coupons")
                .document(couponId)
                .delete()
        } catch (e: Exception) {
            android.util.Log.e("DashboardExtRepo", "Failed to delete coupon", e)
        }
    }

    override fun addCoupon(coupon: SpecialOfferEntity) {
        try {
            val docId = coupon.id.ifBlank { java.util.UUID.randomUUID().toString() }
            firestore.collection("coupons")
                .document(docId)
                .set(coupon.copy(id = docId))
        } catch (e: Exception) {
            android.util.Log.e("DashboardExtRepo", "Failed to add coupon", e)
        }
    }

    override fun getInventory(ownerId: String): Flow<List<InventoryItem>> = callbackFlow {
        val query = if (ownerId.isNotBlank()) {
            firestore.collection("inventory").whereEqualTo("ownerId", ownerId)
        } else {
            firestore.collection("inventory")
        }
        val listener = query.addSnapshotListener { snap, _ ->
            if (snap != null) {
                trySend(snap.documents.mapNotNull { it.toObject(InventoryItem::class.java)?.copy(id = it.id) })
            } else {
                trySend(emptyList())
            }
        }
        awaitClose { listener.remove() }
    }

    // ✨ م2-ج2: نقل منطق Firestore لحفظ وإعادة تعديل المخزون
    override fun updateInventoryQuantity(itemId: String, newQty: Int, inStock: Boolean) {
        if (itemId.isBlank()) return
        try {
            firestore.collection("inventory")
                .document(itemId)
                .update("quantity", newQty, "inStock", inStock)
        } catch (e: Exception) {
            android.util.Log.e("DashboardExtRepo", "Failed to update inventory", e)
        }
    }

    override fun deleteInventoryItem(itemId: String) {
        if (itemId.isBlank()) return
        try {
            firestore.collection("inventory")
                .document(itemId)
                .delete()
        } catch (e: Exception) {
            android.util.Log.e("DashboardExtRepo", "Failed to delete inventory", e)
        }
    }

    override fun addInventoryItem(item: InventoryItem) {
        try {
            val docId = item.id.ifBlank { java.util.UUID.randomUUID().toString() }
            firestore.collection("inventory")
                .document(docId)
                .set(item.copy(id = docId))
        } catch (e: Exception) {
            android.util.Log.e("DashboardExtRepo", "Failed to add inventory", e)
        }
    }

    override fun getLoyaltyPrograms(ownerId: String): Flow<List<LoyaltyProgram>> = callbackFlow {
        val query = if (ownerId.isNotBlank()) {
            firestore.collection("loyalty_programs").whereEqualTo("ownerId", ownerId)
        } else {
            firestore.collection("loyalty_programs")
        }
        val listener = query.addSnapshotListener { snap, _ ->
            if (snap != null) {
                trySend(snap.documents.mapNotNull { it.toObject(LoyaltyProgram::class.java)?.copy(id = it.id) })
            } else {
                trySend(emptyList())
            }
        }
        awaitClose { listener.remove() }
    }

    // ✨ م2-ج2: دوال برامج الولاء عبر الـ Repository
    override fun updateLoyaltyProgramStatus(programId: String, isEnabled: Boolean) {
        if (programId.isBlank()) return
        try {
            firestore.collection("loyalty_programs")
                .document(programId)
                .update("isEnabled", isEnabled)
        } catch (e: Exception) {
            android.util.Log.e("DashboardExtRepo", "Failed to update loyalty program", e)
        }
    }

    override fun deleteLoyaltyProgram(programId: String) {
        if (programId.isBlank()) return
        try {
            firestore.collection("loyalty_programs")
                .document(programId)
                .delete()
        } catch (e: Exception) {
            android.util.Log.e("DashboardExtRepo", "Failed to delete loyalty program", e)
        }
    }

    override fun addLoyaltyProgram(program: LoyaltyProgram) {
        try {
            val docId = program.id.ifBlank { java.util.UUID.randomUUID().toString() }
            firestore.collection("loyalty_programs")
                .document(docId)
                .set(program.copy(id = docId))
        } catch (e: Exception) {
            android.util.Log.e("DashboardExtRepo", "Failed to add loyalty program", e)
        }
    }

    override fun getStaff(ownerId: String): Flow<List<StaffMember>> = callbackFlow {
        val query = if (ownerId.isNotBlank()) {
            firestore.collection("staff").whereEqualTo("ownerId", ownerId)
        } else {
            firestore.collection("staff")
        }
        val listener = query.addSnapshotListener { snap, _ ->
            if (snap != null) {
                trySend(snap.documents.mapNotNull { it.toObject(StaffMember::class.java)?.copy(id = it.id) })
            } else {
                trySend(emptyList())
            }
        }
        awaitClose { listener.remove() }
    }

    // ✨ م2-ج2: دوال الموظفين عبر الـ Repository
    override fun deleteStaff(staffId: String) {
        if (staffId.isBlank()) return
        try {
            firestore.collection("staff")
                .document(staffId)
                .delete()
        } catch (e: Exception) {
            android.util.Log.e("DashboardExtRepo", "Failed to delete staff", e)
        }
    }

    override fun addStaff(staffMember: StaffMember) {
        try {
            val docId = staffMember.id.ifBlank { java.util.UUID.randomUUID().toString() }
            firestore.collection("staff")
                .document(docId)
                .set(staffMember.copy(id = docId))
        } catch (e: Exception) {
            android.util.Log.e("DashboardExtRepo", "Failed to add staff", e)
        }
    }
}
