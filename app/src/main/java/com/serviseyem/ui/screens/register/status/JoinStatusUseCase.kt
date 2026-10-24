package com.serviseyem.ui.screens.register.status

import com.serviseyem.data.*
import com.serviseyem.ui.*
import com.serviseyem.ui.helpers.AppPreferenceHelper

/**
 * 🎯 الحالات المحددة لطلب الانضمام أو الدخول للوحة التحكم
 */
sealed class JoinStatus {
    object NoRequest : JoinStatus()
    data class ActiveStore(val store: StoreEntity, val businessType: String) : JoinStatus()
    data class ActiveProperty(val property: PropertyEntity) : JoinStatus()
    data class ApprovedTechnician(val provider: ProviderEntity, val categoryName: String) : JoinStatus()
    data class ActiveJobPoster(val job: JobEntity) : JoinStatus()
    data class ActiveClient(val userMap: Map<String, Any>) : JoinStatus()
    data class Rejected(val reason: String) : JoinStatus()
    data class PendingStore(val store: StoreEntity) : JoinStatus()
    data class PendingRestaurant(val store: StoreEntity) : JoinStatus()
    data class PendingMedical(val store: StoreEntity) : JoinStatus()
    data class PendingProperty(val property: PropertyEntity) : JoinStatus()
    data class PendingJob(val job: JobEntity) : JoinStatus()
    data class PendingTechnician(val provider: PendingProviderEntity) : JoinStatus()
    data class PendingClient(val userMap: Map<String, Any>) : JoinStatus()
    data class PendingGeneric(val phone: String) : JoinStatus()
}

/**
 * 💼 حالات واجهة مستخدم متابعة حالة الطلب
 */
sealed class JoinStatusUiState {
    object Loading : JoinStatusUiState()
    data class Ready(val status: JoinStatus) : JoinStatusUiState()
    data class Error(val message: String) : JoinStatusUiState()
}

/**
 * 🔍 UseCase لتحديد وتصنيف حالة طلب الانضمام للمستخدم
 */
class JoinStatusUseCase {

    fun determineStatus(
        joinPhone: String,
        pendingProviders: List<PendingProviderEntity>,
        providers: List<ProviderEntity>,
        stores: List<StoreEntity>,
        properties: List<PropertyEntity>,
        categories: List<CategoryEntity>,
        notifications: List<NotificationEntity>,
        jobs: List<JobEntity> = emptyList(),
        registeredUsersList: List<Map<String, Any>> = emptyList()
    ): JoinStatus {
        val cleanPhone = AppPreferenceHelper.normalizePhoneNumber(joinPhone)
        if (cleanPhone.isEmpty()) {
            return JoinStatus.NoRequest
        }

        // 1. Check Active Store / Restaurant / Medical
        val matchingStore = stores.find {
            (AppPreferenceHelper.normalizePhoneNumber(it.ownerId) == cleanPhone ||
                    AppPreferenceHelper.normalizePhoneNumber(it.phone) == cleanPhone) && !it.isDeleted
        }
        if (matchingStore != null && (matchingStore.isActive || matchingStore.isApproved)) {
            val sec = matchingStore.sectionId.lowercase()
            val name = matchingStore.name
            val isRest = sec == "restaurants" || sec.startsWith("restaurant") || name.contains("مطعم") || name.contains("كافيه")
            val isMed = sec == "medical" || sec.startsWith("medical") || name.contains("عيادة") || name.contains("مركز") || name.contains("طبي")
            val businessType = if (isRest) "restaurants" else if (isMed) "medical" else "stores"
            return JoinStatus.ActiveStore(matchingStore, businessType)
        }

        // 2. Check Active Property
        val matchingProperty = properties.find {
            (AppPreferenceHelper.normalizePhoneNumber(it.ownerId) == cleanPhone ||
                    AppPreferenceHelper.normalizePhoneNumber(it.phone) == cleanPhone) && !it.isDeleted
        }
        if (matchingProperty != null && (matchingProperty.isActive || matchingProperty.isApproved)) {
            return JoinStatus.ActiveProperty(matchingProperty)
        }

        // 3. Check Approved Provider / Technician
        val matchingApproved = providers.find { 
            AppPreferenceHelper.normalizePhoneNumber(it.phone) == cleanPhone && !it.isDeleted
        }
        if (matchingApproved != null) {
            val catName = categories.find { it.id == matchingApproved.categoryId }?.name ?: matchingApproved.customCategoryName.ifBlank { "صيانة فنية" }
            return JoinStatus.ApprovedTechnician(matchingApproved, catName)
        }

        // 4. Check Active Job Poster
        val matchingJob = jobs.find {
            AppPreferenceHelper.normalizePhoneNumber(it.phone) == cleanPhone && (it.isActive || it.isApproved) && !it.isDeleted
        }
        if (matchingJob != null) {
            return JoinStatus.ActiveJobPoster(matchingJob)
        }

        // 5. Check Active Client
        val matchingClient = registeredUsersList.find {
            val p = (it["phone"] as? String) ?: ""
            val st = (it["status"] as? String)?.uppercase() ?: ""
            val appSt = (it["approvalStatus"] as? String)?.uppercase() ?: ""
            AppPreferenceHelper.normalizePhoneNumber(p) == cleanPhone && (it["isApproved"] == true || st == "APPROVED" || st == "ACCEPTED" || appSt == "APPROVED" || appSt == "ACCEPTED")
        }
        if (matchingClient != null) {
            return JoinStatus.ActiveClient(matchingClient)
        }

        // 6. Check Pending entity with APPROVED or ACCEPTED status
        val matchingPending = pendingProviders.find { 
            AppPreferenceHelper.normalizePhoneNumber(it.phone) == cleanPhone 
        }
        if (matchingPending != null && (matchingPending.status.equals("APPROVED", ignoreCase = true) || matchingPending.status.equals("ACCEPTED", ignoreCase = true))) {
            val cat = matchingPending.categoryId.uppercase()
            val custom = matchingPending.customCategoryName
            val prof = matchingPending.profession.uppercase()
            val pName = matchingPending.name

            val isRestaurant = cat == "RESTAURANT" || cat.startsWith("RESTAURANT_") || custom.contains("مطعم") || pName.contains("مطعم")
            val isMedical = cat == "MEDICAL" || cat.startsWith("MEDICAL_") || custom.contains("طبي") || pName.contains("عيادة")
            val isProperty = cat == "PROPERTY" || cat.startsWith("PROPERTY_") || prof == "PROPERTY_OWNER"
            val isJob = (cat == "JOB" || cat.startsWith("JOB_")) && cat != "JOB_SEEKER" || prof == "JOB_POSTER"
            val isStore = cat == "STORE" || cat.startsWith("STORE_") || prof == "STORE_OWNER"
            val isClient = cat == "CLIENT" || cat.startsWith("CLIENT_") || prof == "CLIENT"

            return when {
                isRestaurant -> JoinStatus.ActiveStore(
                    StoreEntity(id = "store_$cleanPhone", name = pName, phone = matchingPending.phone, sectionId = "restaurants", isActive = true, isApproved = true),
                    "restaurants"
                )
                isMedical -> JoinStatus.ActiveStore(
                    StoreEntity(id = "store_$cleanPhone", name = pName, phone = matchingPending.phone, sectionId = "medical", isActive = true, isApproved = true),
                    "medical"
                )
                isStore -> JoinStatus.ActiveStore(
                    StoreEntity(id = "store_$cleanPhone", name = pName, phone = matchingPending.phone, sectionId = "stores", isActive = true, isApproved = true),
                    "stores"
                )
                isProperty -> JoinStatus.ActiveProperty(
                    PropertyEntity(id = "prop_$cleanPhone", title = pName, phone = matchingPending.phone, isActive = true, isApproved = true)
                )
                isJob -> JoinStatus.ActiveJobPoster(
                    JobEntity(id = "job_$cleanPhone", title = custom.ifBlank { "وظيفة - $pName" }, companyName = pName, phone = matchingPending.phone, isActive = true, isApproved = true)
                )
                isClient -> JoinStatus.ActiveClient(
                    mapOf("name" to pName, "phone" to matchingPending.phone, "residence" to matchingPending.area, "isApproved" to true)
                )
                else -> JoinStatus.ApprovedTechnician(
                    ProviderEntity(id = "prov_$cleanPhone", name = pName, phone = matchingPending.phone, categoryId = matchingPending.categoryId, subscriptionStatus = "APPROVED", isAvailable = true),
                    matchingPending.customCategoryName.ifBlank { "صيانة فنية" }
                )
            }
        }

        // 7. Check Rejection Notifications or Pending Provider Rejection Status
        if (matchingPending != null && (matchingPending.status == "REJECTED" || matchingPending.reason.isNotBlank())) {
            return JoinStatus.Rejected(matchingPending.reason.ifBlank { "تم رفض طلب الانضمام من قبل الإدارة لعدم استيفاء الشروط." })
        }

        val rejectionNotif = notifications.find {
            val cleanTarget = AppPreferenceHelper.normalizePhoneNumber(it.targetValue)
            cleanTarget == cleanPhone && (it.title.contains("رفض") || it.message.contains("رفض"))
        }
        if (rejectionNotif != null) {
            return JoinStatus.Rejected(rejectionNotif.message)
        }

        // 8. Check Pending entities in collections
        if (matchingStore != null && !matchingStore.isActive && !matchingStore.isApproved) {
            val sec = matchingStore.sectionId.lowercase()
            val cat = matchingStore.categoryId.lowercase()
            return when {
                sec == "restaurants" || sec.startsWith("restaurant") || cat.contains("مطعم") || cat.contains("كافيه") -> JoinStatus.PendingRestaurant(matchingStore)
                sec == "medical" || sec.startsWith("medical") || cat.contains("طبي") || cat.contains("عياد") || cat.contains("صيدل") -> JoinStatus.PendingMedical(matchingStore)
                else -> JoinStatus.PendingStore(matchingStore)
            }
        }
        if (matchingProperty != null && !matchingProperty.isActive && !matchingProperty.isApproved) {
            return JoinStatus.PendingProperty(matchingProperty)
        }
        val pendingJob = jobs.find {
            AppPreferenceHelper.normalizePhoneNumber(it.phone) == cleanPhone && !it.isActive && !it.isApproved
        }
        if (pendingJob != null) {
            return JoinStatus.PendingJob(pendingJob)
        }

        if (matchingPending != null) {
            val cat = matchingPending.categoryId.uppercase()
            val custom = matchingPending.customCategoryName
            val prof = matchingPending.profession.uppercase()
            val pName = matchingPending.name

            val isRestaurant = cat == "RESTAURANT" || cat.startsWith("RESTAURANT_") ||
                    custom.contains("مطعم") || custom.contains("كافيه") || pName.contains("مطعم") || pName.contains("كافيه")

            val isMedical = cat == "MEDICAL" || cat.startsWith("MEDICAL_") ||
                    custom.contains("طبي") || custom.contains("عياد") || custom.contains("صيدل") || custom.contains("مستشفى") ||
                    pName.contains("طبي") || pName.contains("عيادة") || pName.contains("مستشفى") || pName.contains("صيدلية")

            val isProperty = cat == "PROPERTY" || cat.startsWith("PROPERTY_") || prof == "PROPERTY_OWNER" ||
                    custom.contains("عقار") || custom.contains("شقة") || custom.contains("أرض") || pName.contains("عقار")

            val isJob = (cat == "JOB" || (cat.startsWith("JOB_") && cat != "JOB_SEEKER") || prof == "JOB_POSTER" ||
                    custom.contains("وظيفة") || custom.contains("توظيف") || custom.contains("شاغر")) &&
                    cat != "JOB_SEEKER" && prof != "JOB_SEEKER"

            val isStore = cat == "STORE" || cat.startsWith("STORE_") || prof == "STORE_OWNER" ||
                    custom.contains("متجر") || custom.contains("محل") || custom.contains("معرض") || custom.contains("سوق") ||
                    pName.contains("متجر") || pName.contains("محل")

            val isClient = cat == "CLIENT" || cat.startsWith("CLIENT_") || prof == "CLIENT" || custom.contains("عميل")

            return when {
                isClient -> {
                    val userMap = mapOf(
                        "name" to matchingPending.name,
                        "phone" to matchingPending.phone,
                        "residence" to matchingPending.area
                    )
                    JoinStatus.PendingClient(userMap)
                }
                isRestaurant -> {
                    val tempStore = StoreEntity(
                        id = matchingPending.id,
                        name = matchingPending.name,
                        phone = matchingPending.phone,
                        ownerName = matchingPending.name,
                        cityId = matchingPending.area,
                        localNeighborhood = matchingPending.localNeighborhood,
                        sectionId = "restaurants",
                        categoryId = "مطاعم وكافيهات",
                        isActive = false
                    )
                    JoinStatus.PendingRestaurant(tempStore)
                }
                isMedical -> {
                    val tempStore = StoreEntity(
                        id = matchingPending.id,
                        name = matchingPending.name,
                        phone = matchingPending.phone,
                        ownerName = matchingPending.name,
                        cityId = matchingPending.area,
                        localNeighborhood = matchingPending.localNeighborhood,
                        sectionId = "medical",
                        categoryId = "مراكز طبية وعيادات",
                        isActive = false
                    )
                    JoinStatus.PendingMedical(tempStore)
                }
                isProperty -> {
                    val tempProp = PropertyEntity(
                        id = matchingPending.id,
                        title = matchingPending.name,
                        phone = matchingPending.phone,
                        cityId = matchingPending.area,
                        localNeighborhood = matchingPending.localNeighborhood,
                        isActive = false
                    )
                    JoinStatus.PendingProperty(tempProp)
                }
                isJob -> {
                    val tempJob = JobEntity(
                        id = matchingPending.id,
                        title = custom.ifBlank { "وظيفة - ${matchingPending.name}" },
                        companyName = matchingPending.name,
                        phone = matchingPending.phone,
                        cityId = matchingPending.area,
                        isActive = false
                    )
                    JoinStatus.PendingJob(tempJob)
                }
                isStore -> {
                    val tempStore = StoreEntity(
                        id = matchingPending.id,
                        name = matchingPending.name,
                        phone = matchingPending.phone,
                        ownerName = matchingPending.name,
                        cityId = matchingPending.area,
                        localNeighborhood = matchingPending.localNeighborhood,
                        sectionId = "stores",
                        categoryId = "محلات ومراكز تجارية",
                        isActive = false
                    )
                    JoinStatus.PendingStore(tempStore)
                }
                else -> JoinStatus.PendingTechnician(matchingPending)
            }
        }

        return JoinStatus.PendingGeneric(joinPhone)
    }
}
