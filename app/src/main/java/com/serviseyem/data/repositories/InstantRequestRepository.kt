package com.serviseyem.data.repositories

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.serviseyem.data.models.InstantRequestEntity
import com.serviseyem.data.models.RequestOfferEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ⚡ InstantRequestRepository
 * مستودع إدارة الطلبات الفورية والعاجلة مع دعم التدفق الحي (Flow)،
 * المؤقت التنازلي للطلب (30 دقيقة)، تقديم العروض، والمصادقة الأمنية.
 */
class InstantRequestRepository(private val context: Context? = null) {

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val _requests = MutableStateFlow<List<InstantRequestEntity>>(emptyList())
    val requests: StateFlow<List<InstantRequestEntity>> = _requests.asStateFlow()

    private val queryAndCreationDelegate = InstantRequestQueryAndCreationDelegate(
        context = context,
        firestoreProvider = { firestore },
        requestsState = _requests
    )

    private val lifecycleDelegate = InstantRequestLifecycleDelegate(
        context = context,
        firestoreProvider = { firestore },
        requestsState = _requests
    )

    /**
     * 1. إنشاء طلب فوري جديد مع مؤقت زمني مدته 30 دقيقة
     */
    fun createInstantRequest(
        request: InstantRequestEntity,
        onSuccess: (InstantRequestEntity) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        queryAndCreationDelegate.createInstantRequest(request, onSuccess, onError)
    }

    /**
     * 2. تدفق حي لطلبات مستخدم معين (العميل)
     */
    fun getUserInstantRequests(userId: String): Flow<List<InstantRequestEntity>> =
        queryAndCreationDelegate.getUserInstantRequests(userId)

    /**
     * 3. تدفق حي للطلبات المتاحة للفنيين في مدينة/تصنيف معين (فلترة على السيرفر)
     */
    fun getAvailableInstantRequests(category: String = "", city: String = ""): Flow<List<InstantRequestEntity>> =
        queryAndCreationDelegate.getAvailableInstantRequests(category, city)

    /**
     * 4. تقديم عرض سعر من مزود خدمة على طلب فوري
     */
    fun submitOffer(
        offer: RequestOfferEntity,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        lifecycleDelegate.submitOffer(offer, onSuccess, onError)
    }

    /**
     * 5. قبول عرض السعر وبدء الخدمة
     */
    fun acceptOffer(
        requestId: String,
        offerId: String,
        providerId: String,
        providerName: String,
        providerPhone: String,
        acceptedPrice: Double,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        lifecycleDelegate.acceptOffer(
            requestId = requestId,
            offerId = offerId,
            providerId = providerId,
            providerName = providerName,
            providerPhone = providerPhone,
            acceptedPrice = acceptedPrice,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    /**
     * 6. إلغاء الطلب الفوري مع التحقق الأمني
     */
    fun cancelInstantRequest(
        requestId: String,
        userPin: String = "",
        cancelReason: String = "",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        lifecycleDelegate.cancelInstantRequest(requestId, userPin, cancelReason, onSuccess, onError)
    }

    /**
     * 7. إكمال الطلب الفوري بنجاح
     */
    fun completeInstantRequest(
        requestId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        lifecycleDelegate.completeInstantRequest(requestId, onSuccess, onError)
    }
}
