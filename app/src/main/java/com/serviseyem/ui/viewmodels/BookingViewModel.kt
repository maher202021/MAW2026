package com.serviseyem.ui.viewmodels

import com.serviseyem.ui.helpers.AppState
import android.content.Context
import com.serviseyem.ui.*
import androidx.lifecycle.viewModelScope
import com.serviseyem.data.BookingEntity
import com.serviseyem.data.repositories.BookingRepository
import com.serviseyem.data.*
import com.serviseyem.data.models.*
import com.serviseyem.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 📌 Architectural Note: UI-focused BookingStatus enum providing UI labels and hex colors
 * for state rendering in Compose views.
 */

data class BookingFormFields(
    val tripleName: Boolean = true,
    val phoneNumber: Boolean = true,
    val serviceType: Boolean = true,
    val residenceArea: Boolean = true,
    val preferredTime: Boolean = true,
    val description: Boolean = false,
    val tripleNameRequired: Boolean = true,
    val phoneNumberRequired: Boolean = true,
    val serviceTypeRequired: Boolean = true,
    val residenceAreaRequired: Boolean = true,
    val preferredTimeRequired: Boolean = true,
    val descriptionRequired: Boolean = false
)

enum class BookingDistributionMode(val label: String) {
    CATEGORY_SUPERVISOR("لمشرف القسم أولاً"),
    NEAREST_PROVIDER("لأقرب فني جغرافياً"),
    ALL_PROVIDERS("لكل فنيي القسم"),
    SPECIFIC_PROVIDER("لفني محدد مسبقاً"),
    ADMIN_ONLY("للأدمن أولاً")
}


open class BookingViewModel @Inject constructor(
    private val injectedRepository: BookingRepository,
    val appState: AppState
) : BaseViewModel() {

    internal val _bookings get() = appState._bookings
    val bookings: StateFlow<List<BookingEntity>> = _bookings.asStateFlow()

    internal val _createBookingStatus = MutableStateFlow<Result<BookingEntity>?>(null)
    val createBookingStatus: StateFlow<Result<BookingEntity>?> = _createBookingStatus.asStateFlow()

    internal val _bookingFormFields get() = appState._bookingFormFields
    val bookingFormFields: StateFlow<BookingFormFields> = _bookingFormFields.asStateFlow()

    internal val _distributionMode get() = appState._distributionMode
    val distributionMode: StateFlow<BookingDistributionMode> = _distributionMode.asStateFlow()

    val bookingRepository: BookingRepository
        get() = injectedRepository

    // --- Callback/Lambda Properties for decoupling ---
    var getCoupons: (() -> List<com.serviseyem.data.CouponEntity>)? = null
    var getProviders: (() -> List<com.serviseyem.data.ProviderEntity>)? = null
    var getCurrentUserPhone: (() -> String)? = null
    var getCurrentUserName: (() -> String)? = null
    var getCurrentUserResidence: (() -> String)? = null
    var setCurrentUserPhone: ((String) -> Unit)? = null
    var setCurrentUserName: ((String) -> Unit)? = null
    var setCurrentUserResidence: ((String) -> Unit)? = null
    var onAddNotification: ((title: String, message: String, targetType: String, targetValue: String) -> Unit)? = null
    var triggerNotificationCallback: ((String) -> Unit)? = null
    var onOpenOrCreateChatChannel: ((targetId: String, targetType: String, targetName: String, targetPhone: String, targetCategory: String, relatedEntityId: String, relatedEntityType: String, onComplete: (com.serviseyem.data.ChatChannelEntity?) -> Unit) -> Unit)? = null

    fun createBooking(context: Context, booking: BookingEntity, rawPasswordPin: String = "") {
        appContext = context.applicationContext
        viewModelScope.launch {
            bookingRepository.createBooking(
                booking = booking,
                rawPasswordPin = rawPasswordPin,
                onSuccess = { createdBooking ->
                    _createBookingStatus.value = Result.success(createdBooking)
                    triggerToast("🎉 تم إنشاء حجزك بنجاح برقم: ${createdBooking.bookingNumber}")
                },
                onError = { errorMsg ->
                    _createBookingStatus.value = Result.failure(Exception(errorMsg))
                    triggerToast("❌ فشل إنشاء الحجز: $errorMsg")
                }
            )
        }
    }

    fun createBooking(booking: BookingEntity, onResult: (Boolean) -> Unit = {}) {
        if (booking.isRecurring && booking.recurrenceRule != "NONE") {
            createRecurringBookings(booking, onResult)
            return
        }
        val bId = booking.id.ifEmpty { java.util.UUID.randomUUID().toString() }
        val bNum = booking.bookingNumber.ifEmpty { "YEM-${(10000..99999).random()}" }
        val bPass = "${(1000..9999).random()}"
        val finalized = booking.copy(
            id = bId,
            bookingNumber = bNum,
            pinCode = if (booking.pinCode.isNotBlank()) booking.pinCode else com.serviseyem.utils.SecureHasher.hashPin(bPass),
            createdAt = if (booking.createdAt == 0L) System.currentTimeMillis() else booking.createdAt,
            updatedAt = System.currentTimeMillis()
        )
        safeFirestoreCallWithCallback(
            operation = { onSuccess, onFailure ->
                db.collection("bookings").document(bId).set(finalized)
                    .addOnSuccessListener {
                        _bookings.value = _bookings.value + finalized
                        val custPhone = finalized.customerPhone.ifEmpty { finalized.clientPhone }
                        val custName = finalized.customerName.ifEmpty { finalized.clientName.ifEmpty { "العميل" } }
                        val provPhone = finalized.providerPhone.ifEmpty {
                            getProviders?.invoke()?.find { it.id == finalized.providerId || it.name.trim() == finalized.providerName.trim() }?.phone?.trim() ?: finalized.providerId
                        }
                        
                        // 1. User notification
                        onAddNotification?.invoke(
                            "📅 حجز جديد رقم $bNum",
                            "تم تسجيل طلب حجز موعد لدى ${finalized.providerName} بنجاح. رقم الحجز: $bNum ورمز المرور: $bPass.",
                            "USER",
                            custPhone
                        )
                        
                        // 2. Provider notification
                        if (provPhone.isNotBlank()) {
                            onAddNotification?.invoke(
                                "⚡ حجز جديد وارد رقم $bNum",
                                "العميل $custName ($custPhone) قام بحجز خدمة (${finalized.serviceType}) لديك.",
                                "PROVIDER",
                                provPhone
                            )
                        }
                        
                        // 3. Admin notification
                        onAddNotification?.invoke(
                            "📢 حجز جديد مسجل في النظام",
                            "نوع العملية: (إنشاء حجز) | رقم الحجز: $bNum | العميل: $custName ($custPhone) لدى: ${finalized.providerName}",
                            "ADMIN_ONLY",
                            ""
                        )
                        onSuccess()
                        onResult(true)
                    }
                    .addOnFailureListener { e ->
                        onFailure(e)
                        onResult(false)
                    }
            },
            onSuccess = { triggerToast("✅ تم إنشاء الحجز بنجاح") },
            onError = { triggerToast("⚠️ تم حفظ الحجز محلياً، سيتم المزامنة تلقائياً") },
            errorMessage = "فشل إنشاء الحجز"
        )
    }

    private fun createRecurringBookings(baseBooking: BookingEntity, onResult: (Boolean) -> Unit) {
        val parentId = java.util.UUID.randomUUID().toString()
        val dates = com.serviseyem.utils.ScheduleManager.calculateRecurringDates(baseBooking.effectiveDate, baseBooking.recurrenceRule)
        
        val batch = db.batch()
        val newBookings = mutableListOf<BookingEntity>()
        
        dates.forEachIndexed { index, dateStr ->
            val bId = "${parentId}_${index}"
            val bNum = "YEM-${(10000..99999).random()}"
            val bPass = "${(1000..9999).random()}"
            val b = baseBooking.copy(
                id = bId,
                parentId = parentId,
                date = dateStr,
                dateString = dateStr,
                bookingNumber = bNum,
                pinCode = com.serviseyem.utils.SecureHasher.hashPin(bPass),
                isRecurring = true,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            newBookings.add(b)
            val docRef = db.collection("bookings").document(bId)
            batch.set(docRef, b)
        }
        
        safeFirestoreCallWithCallback(
            operation = { onSuccess, onFailure ->
                batch.commit().addOnSuccessListener {
                    _bookings.value = _bookings.value + newBookings
                    
                    val custPhone = baseBooking.effectiveCustomerPhone
                    val custName = baseBooking.effectiveCustomerName
                    val provPhone = baseBooking.providerPhone.ifEmpty {
                        getProviders?.invoke()?.find { it.id == baseBooking.providerId || it.name.trim() == baseBooking.providerName.trim() }?.phone?.trim() ?: baseBooking.providerId
                    }
                    
                    // Notify provider about the batch
                    val msg = "لديك سلسلة حجوزات جديدة متكررة (${newBookings.size} مواعيد) من $custName برقم $custPhone"
                    if (provPhone.isNotBlank()) {
                        onAddNotification?.invoke("سلسلة حجوزات جديدة", msg, "PROVIDER", provPhone)
                    }
                    
                    // Log to admin
                    val auditLog = mapOf(
                        "id" to java.util.UUID.randomUUID().toString(),
                        "action" to "CREATE_RECURRING_BOOKING",
                        "details" to "تم إنشاء ${newBookings.size} حجوزات للمزود ${baseBooking.providerName}",
                        "timestamp" to System.currentTimeMillis()
                    )
                    db.collection("security_audit_logs").document(auditLog["id"].toString()).set(auditLog)
                    
                    onSuccess()
                    onResult(true)
                }.addOnFailureListener { e ->
                    onFailure(e)
                    onResult(false)
                }
            },
            onSuccess = { triggerToast("✅ تم إنشاء السلسلة المتكررة بنجاح") },
            onError = { triggerToast("⚠️ خطأ في المزامنة، يرجى التحقق من اتصالك") },
            errorMessage = "فشل إنشاء الحجوزات المتكررة"
        )
    }

    fun updateBookingFormFields(fields: BookingFormFields) {
        _bookingFormFields.value = fields
        try {
            db.collection("settings").document("booking_fields").set(fields)
        } catch (e: Exception) {}
    }

    fun updateDistributionMode(mode: BookingDistributionMode) {
        _distributionMode.value = mode
        try {
            db.collection("settings").document("distribution_mode").set(mapOf("mode" to mode.name))
        } catch (e: Exception) {}
    }

    fun addBooking(
        name: String, 
        phone: String, 
        area: String, 
        serviceType: String, 
        providerId: String, 
        providerName: String, 
        dateString: String = "2026-06-20", 
        timeString: String = "12:00 م",
        couponCode: String = "",
        pinCode: String = "",
        customBookingId: String = "",
        customPassword: String = ""
    ) = addBookingImpl(name, phone, area, serviceType, providerId, providerName, dateString, timeString, couponCode, pinCode, customBookingId, customPassword)

    fun addBookingImpl(
        name: String, 
        phone: String, 
        area: String, 
        serviceType: String, 
        providerId: String, 
        providerName: String, 
        dateString: String = "2026-06-20", 
        timeString: String = "12:00 م",
        couponCode: String = "",
        pinCode: String = "",
        customBookingId: String = "",
        customPassword: String = ""
    ) {
        val cleanPhone = phone.trim()
        val cleanName = name.trim()
        
        // 1. Verification of identity of registered Yemeni user phone
        val isValidYemeniPhone = cleanPhone.length == 9 && (
            cleanPhone.startsWith("77") || 
            cleanPhone.startsWith("73") || 
            cleanPhone.startsWith("71") || 
            cleanPhone.startsWith("70") || 
            cleanPhone.startsWith("78")
        )
        if (!isValidYemeniPhone) {
            triggerNotificationCallback?.invoke("❌ الهوية غير مسجلة: رقم الهاتف يجب أن يكون يمنياً صحيحاً مفعلاً ومكوناً من 9 أرقام يبدأ بـ 77 أو 73 أو 71 أو 70!")
            return
        }

        // 2. Duplication & Overlap prevention scan
        val isTimeSlotTaken = _bookings.value.any {
            it.providerId == providerId &&
            it.effectiveDate.trim() == dateString.trim() &&
            it.effectiveTime.trim() == timeString.trim() &&
            (it.status == "PENDING" || it.status == "APPROVED" || it.status == "ACCEPTED" || it.status == "IN_PROGRESS")
        }
        if (isTimeSlotTaken) {
            triggerNotificationCallback?.invoke("⚠️ عذراً، هذا الموعد ($dateString في $timeString) محجوز مسبقاً لدى هذا الفني. يرجى اختيار وقت آخر!")
            return
        }

        // Determine specific pricing via admin settings or coupon logic
        var discountPercent = 0.0
        var finalPrice = 0.0

        if (couponCode.isNotBlank()) {
            val couponsList = getCoupons?.invoke() ?: emptyList()
            val coupon = couponsList.find { it.code.trim().uppercase() == couponCode.trim().uppercase() && it.status == "ACTIVE" }
            if (coupon != null) {
                val now = System.currentTimeMillis()
                if (now <= coupon.expiryTimestamp) {
                    discountPercent = coupon.discountPercentage.toDouble()
                    // Valid coupon! Increment used count in Firestore
                    val updatedCount = coupon.usedCount + 1
                    db.collection("coupons").document(coupon.id).update("usedCount", updatedCount)
                    // Apply discount or points
                    triggerNotificationCallback?.invoke("🎫 تم تطبيق كوبون الخصم بنجاح! خصم بقيمة ${coupon.discountPercentage}%")
                } else {
                    triggerNotificationCallback?.invoke("⚠️ الكوبون المستخدم منتهي الصلاحية")
                }
            } else {
                triggerNotificationCallback?.invoke("⚠️ الكوبون غير صحيح أو غير مفعل")
            }
        }

        // Retrieve provider details to ensure accuracy
        val providersList = getProviders?.invoke() ?: emptyList()
        val prov = providersList.find { it.id == providerId || (providerId.isBlank() && it.name.trim() == providerName.trim()) }
        val basePrice = prov?.previewPrice ?: 0.0
        finalPrice = if (discountPercent > 0.0) {
            basePrice * (1.0 - (discountPercent / 100.0))
        } else {
            basePrice
        }

        val effectiveProviderPhone = prov?.phone?.trim()?.ifBlank {
            providersList.find { it.name.trim() == providerName.trim() }?.phone?.trim() ?: ""
        } ?: ""

        val finalBookingId = if (customBookingId.isNotBlank()) customBookingId else java.util.UUID.randomUUID().toString()
        val finalBookingNumber = "B-${(100000..999999).random()}"
        val generatedPass = if (customPassword.isNotBlank()) customPassword else "${(1000..9999).random()}"

        val newBooking = BookingEntity(
            id = finalBookingId,
            customerName = cleanName,
            customerPhone = cleanPhone,
            customerArea = area,
            serviceType = serviceType,
            providerId = providerId,
            providerName = providerName,
            providerPhone = effectiveProviderPhone,
            date = dateString,
            time = timeString,
            dateString = dateString,
            timeString = timeString,
            status = "PENDING",
            bookingNumber = finalBookingNumber,
            pinCode = if (pinCode.isNotBlank()) pinCode else com.serviseyem.utils.SecureHasher.hashPin(generatedPass),
            totalAmount = finalPrice,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        try {
            // Mark slot as booked
            db.collection("bookings").document(finalBookingId).set(newBooking)
                .addOnSuccessListener {
                    _bookings.value = _bookings.value + newBooking
                    
                    // Log custom Firebase Analytics event
                    try {
                        val bundle = android.os.Bundle().apply {
                            putString("service_type", serviceType)
                            putString("provider_name", providerName)
                            putDouble("price", finalPrice)
                            putString("booking_number", finalBookingNumber)
                        }
                        com.serviseyem.MyApplication.logFirebaseEvent("submit_booking", bundle)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    // Auto-save user identity in memory if empty to ensure they can track notifications immediately
                    if (getCurrentUserPhone?.invoke()?.isEmpty() == true) {
                        setCurrentUserPhone?.invoke(cleanPhone)
                        setCurrentUserName?.invoke(cleanName)
                        setCurrentUserResidence?.invoke(area)
                    }

                    // Check Admin bookingRouting setting to control notification distribution
                    val bookingRouting = appState._settings.value.bookingRouting
                    val sendUserNotif = bookingRouting == "BOTH" || bookingRouting == "PROVIDER" || bookingRouting == "USER" || bookingRouting.isBlank()
                    val sendProviderNotif = bookingRouting == "BOTH" || bookingRouting == "PROVIDER" || bookingRouting.isBlank()
                    val sendAdminNotif = bookingRouting == "BOTH" || bookingRouting == "ADMIN" || bookingRouting.isBlank()

                    // 1. Notify the customer (user) with booking number and password
                    if (sendUserNotif) {
                        onAddNotification?.invoke(
                            "📅 تم تسجيل طلب حجزك رقم $finalBookingNumber",
                            "مرحباً بك $cleanName، تم استقبال طلب الحجز لدى الفني $providerName بنجاح. رقم الحجز السري هو: $finalBookingNumber ورمز المرور لإلغاء وتعديل الحجز هو: $generatedPass. يرجى الاحتفاظ بهما للتحكم بالحجز وإثبات الهوية عند إنجاز الخدمة.",
                            "USER",
                            cleanPhone
                        )
                    }

                    // 2. Notify the Technician (PROVIDER) with actual phone or ID
                    if (sendProviderNotif) {
                        val formattedPrice = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(finalPrice)
                        val technicianTarget = effectiveProviderPhone.ifBlank { providerId.ifBlank { "PROVIDER" } }
                        onAddNotification?.invoke(
                            "⚡ حجز عاجل جديد رقم $finalBookingNumber",
                            "العميل $cleanName ($cleanPhone) من ($area) حجز خدمة ($serviceType) لدى الفني $providerName بموعد $dateString $timeString. سعر الخدمة: $formattedPrice ريال يمني.",
                            "PROVIDER",
                            technicianTarget
                        )
                    }

                    // 3. Notify the Admin/Supervisor according to settings
                    if (sendAdminNotif) {
                        onAddNotification?.invoke(
                            "📢 حجز جديد مسجل في النظام",
                            "العميل $cleanName حجز لدى $providerName في مدينة $area. رقم الحجز: $finalBookingNumber والرمز السري: $generatedPass.",
                            "ADMIN_ONLY",
                            ""
                        )
                    }
                }
                .addOnFailureListener { e ->
                    triggerNotificationCallback?.invoke("❌ فشل الحجز: ${e.message}")
                }

            triggerNotificationCallback?.invoke("تم إرسال طلب الحجز، سيتم مراجعته")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateBookingStatus(bookingId: String, newStatus: BookingStatus) =
        updateBookingStatusImpl(bookingId, newStatus.name)

    fun updateBookingStatus(bookingId: String, newStatus: String, rejectionReason: String = "") =
        updateBookingStatusImpl(bookingId, newStatus, rejectionReason)

    fun updateBookingStatusImpl(bookingId: String, newStatus: String, rejectionReason: String = "") {
        db.collection("bookings").document(bookingId).get().addOnSuccessListener { snapshot ->
            val b = snapshot.toObject(BookingEntity::class.java)
            if (b != null) {
                val updated = b.copy(status = newStatus, rejectionReason = rejectionReason)
                db.collection("bookings").document(bookingId).set(updated)
                _bookings.value = _bookings.value.map { if (it.id == bookingId) updated else it }
                
                // Automatically trigger getOrCreateChannel with relatedEntityId & relatedEntityType = "BOOKING" upon acceptance/approval
                if (newStatus == "APPROVED" || newStatus == "ACCEPTED" || newStatus == "IN_PROGRESS") {
                    val otherId = b.providerId.ifEmpty { b.providerPhone.ifEmpty { "ADMIN" } }
                    val otherName = b.providerName.ifEmpty { "مقدم الخدمة" }
                    val otherPhone = b.providerPhone
                    onOpenOrCreateChatChannel?.invoke(
                        otherId,
                        "BOOKING",
                        otherName,
                        otherPhone,
                        b.category,
                        bookingId,
                        "BOOKING"
                    ) { createdCh ->
                        // Channel auto-provisioned upon booking approval with relatedChatChannelId stored
                        if (createdCh != null && createdCh.id.isNotEmpty()) {
                            db.collection("bookings").document(bookingId).update("relatedChatChannelId", createdCh.id)
                        }
                    }
                }
                
                val arabicStatusMsg = when(newStatus) {
                    "APPROVED", "ACCEPTED", "IN_PROGRESS" -> "قبول وتأكيد حجزك بنجاح وسيتواصل معك الفني قريباً"
                    "PENDING", "UNDER_REVIEW" -> "وضع حجزك قيد المراجعة والتدقيق الإداري"
                    "REJECTED" -> "رفض وإلغاء حجزك" + (if (rejectionReason.isNotBlank()) " لسبب: $rejectionReason" else "")
                    "COMPLETED" -> "إكمال وإنجاز الخدمة بنجاح وتقييم العمل"
                    else -> "تعديل حالة طلب حجزك إلى: $newStatus"
                }

                // Always send critical user notifications for booking transitions so they can track progress
                onAddNotification?.invoke(
                    "📅 تحديث حالة الحجز (رقم ${b.bookingCode.ifBlank { b.bookingNumber.ifBlank { b.id } }})",
                    "عزيزي العميل، تم $arabicStatusMsg للخدمة المقدمة من ${b.providerName}.",
                    "USER",
                    b.customerPhone.ifBlank { b.clientPhone }
                )

                // Also notify the technician (provider)
                val provTarget = b.providerPhone.ifBlank {
                    val provObj = getProviders?.invoke()?.find { it.id == b.providerId || it.name.trim() == b.providerName.trim() }
                    provObj?.phone?.trim()?.ifBlank { b.providerId } ?: b.providerId
                }
                if (provTarget.isNotBlank()) {
                    onAddNotification?.invoke(
                        "📅 تحديث حالة الحجز (رقم ${b.bookingCode.ifBlank { b.bookingNumber.ifBlank { b.id } }})",
                        "تم تغيير حالة الحجز للعميل ${b.customerName} إلى: $arabicStatusMsg",
                        "PROVIDER",
                        provTarget
                    )
                }

                // Notify admin
                onAddNotification?.invoke(
                    "📢 تحديث حالة حجز",
                    "تم تحديث حالة الحجز رقم ${b.bookingCode.ifBlank { b.bookingNumber.ifBlank { b.id } }} إلى $arabicStatusMsg للعميل ${b.customerName} والفني ${b.providerName}.",
                    "ADMIN_ONLY",
                    ""
                )
            }
        }
        val toastMsg = when(newStatus) {
            "APPROVED", "ACCEPTED", "IN_PROGRESS" -> "⚡ تم قبول وتأكيد الحجز بنجاح"
            "PENDING", "UNDER_REVIEW" -> "⏳ تم وضع الحجز قيد المراجعة"
            "REJECTED" -> "❌ تم رفض الحجز وإلغائه"
            "COMPLETED" -> "🎉 تم إكمال الخدمة بنجاح وتوثيق الإنجاز"
            else -> "تم تحديث حالة الحجز بنجاح"
        }
        triggerNotificationCallback?.invoke(toastMsg)
    }

    fun deleteBooking(bookingId: String) = deleteBookingImpl(bookingId)

    fun deleteBookingImpl(bookingId: String) {
        val b = _bookings.value.find { it.id == bookingId }
        val status = b?.status?.uppercase() ?: ""
        if (b != null && status != "COMPLETED" && status != "CANCELLED") {
            triggerNotificationCallback?.invoke("⚠️ لا يمكن حذف الحجز إلا إذا كان مكتملاً أو ملغياً")
            return
        }
        _bookings.value = _bookings.value.filter { it.id != bookingId }
        db.collection("bookings").document(bookingId).delete()
        bookingRepository.deleteBooking(bookingId, {}, {})
        triggerNotificationCallback?.invoke("🗑️ تم حذف الحجز من السجلات")

        val bkCode = b?.bookingCode?.ifBlank { b.bookingNumber.ifBlank { bookingId } } ?: bookingId
        val custName = b?.effectiveCustomerName ?: "عميل"
        onAddNotification?.invoke(
            "🗑️ إشعار إداري: حذف حجز",
            "نوع العملية: (حذف) | رقم الحجز: $bkCode | اسم العميل: $custName",
            "ADMIN_ONLY",
            ""
        )
    }

    fun deleteAllBookings(customerPhone: String) = deleteAllBookingsImpl(customerPhone)

    fun deleteAllBookingsImpl(customerPhone: String) {
        _bookings.value = _bookings.value.filter { it.customerPhone != customerPhone }
        db.collection("bookings")
            .whereEqualTo("customerPhone", customerPhone)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val batch = db.batch()
                for (doc in querySnapshot.documents) {
                    batch.delete(doc.reference)
                }
                batch.commit().addOnSuccessListener {
                    triggerNotificationCallback?.invoke("🗑️ تم تصفية وحذف سجل جميع الحجوزات بنجاح.")
                }
            }
    }

    fun updateBooking(booking: BookingEntity) = updateBookingImpl(booking)

    fun updateBookingImpl(booking: BookingEntity) {
        db.collection("bookings").document(booking.id).set(booking)
        _bookings.value = _bookings.value.map { if (it.id == booking.id) booking else it }
        triggerNotificationCallback?.invoke("💾 تم تحديث بيانات الحجز بنجاح")

        val bkCode = booking.bookingCode.ifBlank { booking.bookingNumber.ifBlank { booking.id } }
        val custName = booking.effectiveCustomerName
        onAddNotification?.invoke(
            "✏️ إشعار إداري: تعديل حجز",
            "نوع العملية: (تعديل) | رقم الحجز: $bkCode | اسم العميل: $custName",
            "ADMIN_ONLY",
            ""
        )
    }




    fun attemptCancelBooking(bookingId: String, input: String, reason: String = "ملغي بطلب العميل", cancelledByParam: String = "USER", onResult: (Boolean, String) -> Unit) =
        attemptCancelBookingImpl(bookingId, input, reason, cancelledByParam, onResult)

        fun attemptCancelBookingImpl(bookingId: String, input: String, reason: String = "ملغي بطلب العميل", cancelledByParam: String = "USER", onResult: (Boolean, String) -> Unit) {
        val b = _bookings.value.find { it.id == bookingId }
        if (b == null) {
            onResult(false, "❌ الحجز غير موجود")
            return
        }
        
        injectedRepository.cancelBookingWithSecurity(
            booking = b,
            inputPinOrPassword = input,
            cancellationReason = reason,
            cancelledBy = cancelledByParam,
            onSuccess = {
                onResult(true, "✅ تم إلغاء الحجز بنجاح")
                triggerNotificationCallback?.invoke("✅ تم إلغاء الحجز بنجاح")
            },
            onError = { msg ->
                onResult(false, msg)
            }
        )
    }

    fun editBookingByUser(bookingId: String, newDate: String, newTime: String, newServiceType: String, providerId: String = "", providerName: String = "") {
        val targetProviderId = providerId.ifEmpty {
            _bookings.value.find { it.id == bookingId }?.providerId ?: ""
        }
        val isTimeSlotTaken = _bookings.value.any {
            it.id != bookingId &&
            it.providerId == targetProviderId &&
            it.effectiveDate.trim() == newDate.trim() &&
            it.effectiveTime.trim() == newTime.trim() &&
            (it.status == "PENDING" || it.status == "APPROVED" || it.status == "ACCEPTED" || it.status == "IN_PROGRESS")
        }
        if (isTimeSlotTaken) {
            triggerToast("❌ عذراً! هذا الوقت (${newTime}) وتاريخ (${newDate}) محجوز بالفعل لدى مقدم الخدمة. يرجى اختيار موعد آخر.")
            return
        }

        val updates = mutableMapOf<String, Any>(
            "date" to newDate,
            "time" to newTime,
            "dateString" to newDate,
            "timeString" to newTime,
            "serviceType" to newServiceType,
            "updatedAt" to System.currentTimeMillis()
        )
        if (providerId.isNotEmpty()) {
            updates["providerId"] = providerId
        }
        if (providerName.isNotEmpty()) {
            updates["providerName"] = providerName
        }
        db.collection("bookings").document(bookingId).update(updates)
            .addOnSuccessListener {
                triggerToast("✅ تم تعديل الحجز بنجاح!")
            }
    }

    fun getBookingStatusColor(status: String): String {
        return when (status) {
            "PENDING" -> "#FF9800" // Orange
            "APPROVED" -> "#2196F3" // Blue
            "IN_PROGRESS" -> "#9C27B0" // Purple
            "COMPLETED" -> "#4CAF50" // Green
            "CANCELLED" -> "#F44336" // Red
            else -> "#757575" // Grey
        }
    }

    fun getBookingStatusLabel(status: String): String {
        return when (status) {
            "PENDING" -> "قيد الانتظار"
            "APPROVED" -> "مقبول"
            "IN_PROGRESS" -> "جاري التنفيذ"
            "COMPLETED" -> "مكتمل"
            "CANCELLED" -> "ملغي"
            else -> "غير معروف"
        }
    }

    fun getBookingProgress(status: String): Float {
        return when (status) {
            "PENDING" -> 0.25f
            "APPROVED" -> 0.50f
            "IN_PROGRESS" -> 0.75f
            "COMPLETED" -> 1.0f
            "CANCELLED" -> 1.0f
            else -> 0.0f
        }
    }
    
    fun createBookingDirectly(
        provider: com.serviseyem.data.ProviderEntity,
        notes: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val custPhone = getCurrentUserPhone?.invoke()?.ifBlank { "777000000" } ?: "777000000"
        val custName = getCurrentUserName?.invoke()?.ifBlank { "عميل الخريطة" } ?: "عميل الخريطة"
        val bookingDate = com.serviseyem.utils.DateFormatter.formatDateDash(System.currentTimeMillis())
        val bookingTime = com.serviseyem.utils.DateFormatter.formatTime24(System.currentTimeMillis() + 3600 * 1000L)
        val rawPin = "${(1000..9999).random()}"
        val bNum = "YEM-${(10000..99999).random()}"
        val bId = java.util.UUID.randomUUID().toString()

        val directBooking = BookingEntity(
            id = bId,
            bookingNumber = bNum,
            bookingCode = bNum,
            providerId = provider.id,
            providerName = provider.name,
            providerPhone = provider.phone,
            customerName = custName,
            customerPhone = custPhone,
            clientName = custName,
            clientPhone = custPhone,
            clientId = custPhone,
            serviceType = provider.profession.ifBlank { provider.customCategoryName.ifBlank { "حجز فوري من الخريطة" } },
            category = provider.customCategoryName.ifBlank { provider.profession },
            serviceDetails = notes.ifBlank { "حجز مباشر من خريطة التطبيق" },
            date = bookingDate,
            dateString = bookingDate,
            time = bookingTime,
            timeString = bookingTime,
            status = "PENDING",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        injectedRepository.createBooking(
            booking = directBooking,
            rawPasswordPin = rawPin,
            onSuccess = { createdBooking ->
                _bookings.value = _bookings.value.filter { it.id != createdBooking.id } + createdBooking

                // Triple notification for USER, PROVIDER, and ADMIN_ONLY
                onAddNotification?.invoke(
                    "📅 حجز خريطة جديد #${createdBooking.bookingNumber}",
                    "تم إرسال طلب حجزك بنجاح إلى ${createdBooking.providerName}. رقم الحجز #${createdBooking.bookingNumber} ورمز المرور: $rawPin.",
                    "USER",
                    custPhone
                )
                if (createdBooking.providerPhone.isNotBlank()) {
                    onAddNotification?.invoke(
                        "⚡ طلب حجز جديد من الخريطة #${createdBooking.bookingNumber}",
                        "قام العميل $custName ($custPhone) بطلب حجز مباشر من الخريطة لديك.",
                        "PROVIDER",
                        createdBooking.providerPhone
                    )
                }
                onAddNotification?.invoke(
                    "📢 حجز خريطة جديد",
                    "حجز خريطة جديد #${createdBooking.bookingNumber} للعميل $custName لدى ${createdBooking.providerName}.",
                    "ADMIN_ONLY",
                    "ALL"
                )

                triggerToast("🎉 تم إنشاء الحجز بنجاح برقم #${createdBooking.bookingNumber}")
                onSuccess()
            },
            onError = { errorMsg ->
                onError(errorMsg)
            }
        )
    }

    fun lockBookingAfterFailedAttempts(bookingId: String, lockDurationMs: Long, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = injectedRepository.lockBooking(bookingId, lockDurationMs)
            onResult(result.isSuccess)
        }
    }

    fun updateBookingRelatedChatChannel(bookingId: String, channelId: String) {
        if (bookingId.isBlank() || channelId.isBlank()) return
        viewModelScope.launch {
            try {
                db.collection("bookings").document(bookingId).update("relatedChatChannelId", channelId)
            } catch (_: Exception) {}
        }
    }

}
