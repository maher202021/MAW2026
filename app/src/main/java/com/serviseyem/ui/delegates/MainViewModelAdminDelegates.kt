package com.serviseyem.ui

import androidx.lifecycle.viewModelScope
import com.serviseyem.data.*
import com.serviseyem.data.models.*
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

fun MainViewModel.approveRequest(request: PendingProviderEntity) = adminViewModel.approveRequest(request)
fun MainViewModel.rejectRequest(request: PendingProviderEntity, reason: String) = adminViewModel.rejectRequest(request, reason)
fun MainViewModel.approveTechnician(providerId: String) = adminViewModel.approveTechnician(providerId)
fun MainViewModel.loadPendingTechnicians() = adminViewModel.loadPendingTechnicians()
fun MainViewModel.approvePendingProvider(pending: PendingProviderEntity) = adminViewModel.approvePendingProvider(pending)
fun MainViewModel.saveStore(store: com.serviseyem.data.StoreEntity) = adminViewModel.saveStore(store)
fun MainViewModel.deleteStore(storeId: String) = adminViewModel.deleteStore(storeId)
fun MainViewModel.restoreStore(storeId: String) = adminViewModel.restoreStore(storeId)
fun MainViewModel.deleteStorePermanently(storeId: String) = adminViewModel.deleteStorePermanently(storeId)
fun MainViewModel.setStoreActive(storeId: String, isActive: Boolean) = adminViewModel.setStoreActive(storeId, isActive)
fun MainViewModel.setStorePinned(storeId: String, isPinned: Boolean) = adminViewModel.setStorePinned(storeId, isPinned)
fun MainViewModel.setStoreVip(storeId: String, isVip: Boolean) = adminViewModel.setStoreVip(storeId, isVip)
fun MainViewModel.setStoreVerified(storeId: String, isVerified: Boolean) = adminViewModel.setStoreVerified(storeId, isVerified)
fun MainViewModel.setStoreRecommended(storeId: String, isRecommended: Boolean) = adminViewModel.setStoreRecommended(storeId, isRecommended)
fun MainViewModel.setStoreChatDisabled(storeId: String, isDisabled: Boolean) = adminViewModel.setStoreChatDisabled(storeId, isDisabled)
fun MainViewModel.setStoreNotificationsDisabled(storeId: String, isDisabled: Boolean) = adminViewModel.setStoreNotificationsDisabled(storeId, isDisabled)
fun MainViewModel.setStorePaymentEnabled(storeId: String, isEnabled: Boolean) = adminViewModel.setStorePaymentEnabled(storeId, isEnabled)
fun MainViewModel.toggleStoreBlocked(storeId: String, isBlocked: Boolean) = adminViewModel.toggleStoreBlocked(storeId, isBlocked)
fun MainViewModel.toggleStoreActive(storeId: String) = adminViewModel.toggleStoreActive(storeId)
fun MainViewModel.toggleStorePinned(storeId: String) = adminViewModel.toggleStorePinned(storeId)
fun MainViewModel.toggleStoreChatDisabled(storeId: String) = adminViewModel.toggleStoreChatDisabled(storeId)
fun MainViewModel.approveStorePdf(storeId: String, approve: Boolean) = adminViewModel.approveStorePdf(storeId, approve)

fun MainViewModel.saveProperty(property: com.serviseyem.data.PropertyEntity) = adminViewModel.saveProperty(property)
fun MainViewModel.deleteProperty(propertyId: String) = adminViewModel.deleteProperty(propertyId)
fun MainViewModel.restoreProperty(propertyId: String) = adminViewModel.restoreProperty(propertyId)
fun MainViewModel.deletePropertyPermanently(propertyId: String) = adminViewModel.deletePropertyPermanently(propertyId)
fun MainViewModel.setPropertyActive(propertyId: String, isActive: Boolean) = adminViewModel.setPropertyActive(propertyId, isActive)
fun MainViewModel.setPropertyPinned(propertyId: String, isPinned: Boolean) = adminViewModel.setPropertyPinned(propertyId, isPinned)
fun MainViewModel.setPropertyVip(propertyId: String, isVip: Boolean) = adminViewModel.setPropertyVip(propertyId, isVip)
fun MainViewModel.setPropertyVerified(propertyId: String, isVerified: Boolean) = adminViewModel.setPropertyVerified(propertyId, isVerified)
fun MainViewModel.setPropertyRecommended(propertyId: String, isRecommended: Boolean) = adminViewModel.setPropertyRecommended(propertyId, isRecommended)
fun MainViewModel.setPropertyChatDisabled(propertyId: String, isDisabled: Boolean) = adminViewModel.setPropertyChatDisabled(propertyId, isDisabled)
fun MainViewModel.setPropertyNotificationsDisabled(propertyId: String, isDisabled: Boolean) = adminViewModel.setPropertyNotificationsDisabled(propertyId, isDisabled)
fun MainViewModel.setPropertyPaymentEnabled(propertyId: String, isEnabled: Boolean) = adminViewModel.setPropertyPaymentEnabled(propertyId, isEnabled)
fun MainViewModel.togglePropertyBlocked(propertyId: String, isBlocked: Boolean) = adminViewModel.togglePropertyBlocked(propertyId, isBlocked)
fun MainViewModel.approvePropertyPdf(propertyId: String, approve: Boolean) = adminViewModel.approvePropertyPdf(propertyId, approve)

fun MainViewModel.saveJob(job: com.serviseyem.data.JobEntity) = adminViewModel.saveJob(job)
fun MainViewModel.deleteJob(jobId: String) = adminViewModel.deleteJob(jobId)
fun MainViewModel.restoreJob(jobId: String) = adminViewModel.restoreJob(jobId)
fun MainViewModel.deleteJobPermanently(jobId: String) = adminViewModel.deleteJobPermanently(jobId)
fun MainViewModel.setJobApproved(jobId: String, isApproved: Boolean) = adminViewModel.setJobApproved(jobId, isApproved)
fun MainViewModel.setJobPinned(jobId: String, isPinned: Boolean) = adminViewModel.setJobPinned(jobId, isPinned)
fun MainViewModel.setJobVip(jobId: String, isVip: Boolean) = adminViewModel.setJobVip(jobId, isVip)
fun MainViewModel.setJobChatDisabled(jobId: String, isDisabled: Boolean) = adminViewModel.setJobChatDisabled(jobId, isDisabled)
fun MainViewModel.submitJobApplication(application: com.serviseyem.data.JobApplicationEntity) = adminViewModel.submitJobApplication(application)
fun MainViewModel.updateJobApplicationStatus(appId: String, status: String) = adminViewModel.updateJobApplicationStatus(appId, status)
fun MainViewModel.acceptJobApplication(appId: String) = adminViewModel.acceptJobApplication(appId)
fun MainViewModel.rejectJobApplication(appId: String, reason: String) = adminViewModel.rejectJobApplication(appId, reason)
fun MainViewModel.deleteJobApplication(appId: String) = adminViewModel.deleteJobApplication(appId)

fun MainViewModel.deleteReport(reportId: String) = adminViewModel.deleteReport(reportId)
fun MainViewModel.sendReport(providerId: String, providerName: String, reporterName: String, content: String) = adminViewModel.sendReport(providerId, providerName, reporterName, content)
fun MainViewModel.submitReport(report: com.serviseyem.data.ReportEntity, onComplete: () -> Unit = {}) = adminViewModel.submitReport(report, onComplete)

fun MainViewModel.saveCoupon(coupon: CouponEntity) = adminViewModel.saveCoupon(coupon)
fun MainViewModel.deleteCoupon(couponId: String) = adminViewModel.deleteCoupon(couponId)
fun MainViewModel.addCoupon(code: String, pointsValue: Int, expiryMs: Long, discountPercentage: Int = 0, maxUsageCount: Int = 100) =
    adminViewModel.addCoupon(code, pointsValue, expiryMs, discountPercentage, maxUsageCount)

fun MainViewModel.saveInternalWallet(wallet: com.serviseyem.data.InternalWalletEntity) = adminViewModel.saveInternalWallet(wallet)
fun MainViewModel.performWalletTransaction(
    walletId: String,
    ownerName: String,
    ownerPhone: String,
    ownerType: String,
    type: String,
    amount: Double,
    note: String
) = adminViewModel.performWalletTransaction(walletId, ownerName, ownerPhone, ownerType, type, amount, note)
fun MainViewModel.addPaymentWallet(wallet: PaymentWalletEntity) = adminViewModel.addPaymentWallet(wallet)
fun MainViewModel.updatePaymentWallet(wallet: PaymentWalletEntity) = adminViewModel.updatePaymentWallet(wallet)
fun MainViewModel.deletePaymentWallet(walletId: String) = adminViewModel.deletePaymentWallet(walletId)
fun MainViewModel.togglePaymentWalletVisibility(walletId: String, currentVisible: Boolean) = adminViewModel.togglePaymentWalletVisibility(walletId, currentVisible)
fun MainViewModel.confirmPayment(
    paymentId: String,
    transferId: String,
    transferPhoto: String,
    walletProvider: String,
    walletNumber: String,
    walletAccountName: String
) = adminViewModel.confirmPayment(paymentId, transferId, transferPhoto, walletProvider, walletNumber, walletAccountName)
fun MainViewModel.verifyPayment(paymentId: String, isVerified: Boolean, note: String, adminName: String) = adminViewModel.verifyPayment(paymentId, isVerified, note, adminName)
fun MainViewModel.refundPayment(paymentId: String, reason: String) = adminViewModel.refundPayment(paymentId, reason)

fun MainViewModel.saveProduct(product: com.serviseyem.data.ProductEntity) = adminViewModel.saveProduct(product)
fun MainViewModel.deleteProduct(productId: String) = adminViewModel.deleteProduct(productId)
fun MainViewModel.updateProductPrice(productId: String, newPrice: Double) = adminViewModel.updateProductPrice(productId, newPrice)

fun MainViewModel.saveOffer(offer: com.serviseyem.data.models.Offer) = adminViewModel.saveOffer(offer)
fun MainViewModel.deleteOffer(offerId: String) = adminViewModel.deleteOffer(offerId)
fun MainViewModel.toggleOfferStatus(offerId: String, isActive: Boolean) = adminViewModel.toggleOfferStatus(offerId, isActive)
fun MainViewModel.listenToOffersForEntity(
    entityId: String,
    onResult: (List<com.serviseyem.data.models.Offer>) -> Unit
): ListenerRegistration = adminViewModel.listenToOffersForEntity(entityId, onResult)
fun MainViewModel.listenToProductsForStore(
    storeId: String,
    onResult: (List<com.serviseyem.data.ProductEntity>) -> Unit
): ListenerRegistration = adminViewModel.listenToProductsForStore(storeId, onResult)

fun MainViewModel.saveCustomProfileTab(tab: com.serviseyem.data.CustomProfileTabEntity) = adminViewModel.saveCustomProfileTab(tab)
fun MainViewModel.deleteCustomProfileTab(tabId: String) = adminViewModel.deleteCustomProfileTab(tabId)
fun MainViewModel.toggleCustomProfileTab(tabId: String) = adminViewModel.toggleCustomProfileTab(tabId)

fun MainViewModel.deleteCategory(categoryId: String) = adminViewModel.deleteCategory(categoryId)
fun MainViewModel.togglePinCategory(categoryId: String) = adminViewModel.togglePinCategory(categoryId)
fun MainViewModel.mergeCategories(sourceCategoryId: String, targetCategoryId: String) = adminViewModel.mergeCategories(sourceCategoryId, targetCategoryId)
fun MainViewModel.saveCategoryEntity(cat: CategoryEntity) = adminViewModel.saveCategoryEntity(cat)
fun MainViewModel.addSubCategory(parentId: String, nameAr: String, icon: String) = adminViewModel.addSubCategory(parentId, nameAr, icon)
fun MainViewModel.convertCategoryType(catId: String, newParentId: String, isMain: Boolean) = adminViewModel.convertCategoryType(catId, newParentId, isMain)
fun MainViewModel.reorderCategories(newOrderedList: List<CategoryEntity>) = adminViewModel.reorderCategories(newOrderedList)
fun MainViewModel.addNewCategory(nameAr: String, nameEn: String, icon: String, description: String, parentId: String = "", isMainCategory: Boolean = true) =
    adminViewModel.addNewCategory(nameAr, nameEn, icon, description, parentId, isMainCategory)
fun MainViewModel.editCategory(categoryId: String, newName: String, newIcon: String, parentId: String = "", isMainCategory: Boolean = true) =
    adminViewModel.editCategory(categoryId, newName, newIcon, parentId, isMainCategory)

fun MainViewModel.updateCity(city: CityEntity) = adminViewModel.updateCity(city)
fun MainViewModel.removeCity(cityId: String) = adminViewModel.removeCity(cityId)
fun MainViewModel.addNewCity(nameAr: String, nameEn: String, icon: String = "📍", photoUrl: String = "", sortOrder: Int = 0) =
    adminViewModel.addNewCity(nameAr, nameEn, icon, photoUrl, sortOrder)

fun MainViewModel.removeProvider(providerId: String) = adminViewModel.removeProvider(providerId)
fun MainViewModel.removeProviderPermanently(providerId: String) = adminViewModel.removeProviderPermanently(providerId)
fun MainViewModel.restoreProvider(providerId: String) = adminViewModel.restoreProvider(providerId)
fun MainViewModel.pinProvider(providerId: String, isPinned: Boolean) = adminViewModel.pinProvider(providerId, isPinned)
fun MainViewModel.recommendProvider(providerId: String, isRecommended: Boolean) = adminViewModel.recommendProvider(providerId, isRecommended)
fun MainViewModel.verifyProviderBadge(providerId: String, isVerified: Boolean) = adminViewModel.verifyProviderBadge(providerId, isVerified)
fun MainViewModel.toggleProviderSubscription(providerId: String, status: String) = adminViewModel.toggleProviderSubscription(providerId, status)
fun MainViewModel.setProviderChatDisabled(providerId: String, disabled: Boolean) = adminViewModel.setProviderChatDisabled(providerId, disabled)
fun MainViewModel.setProviderNotificationsDisabled(providerId: String, disabled: Boolean) = adminViewModel.setProviderNotificationsDisabled(providerId, disabled)
fun MainViewModel.setProviderPaymentRequired(providerId: String, required: Boolean) = adminViewModel.setProviderPaymentRequired(providerId, required)
fun MainViewModel.extendProviderSubscription(providerId: String, extraMs: Long) = adminViewModel.extendProviderSubscription(providerId, extraMs)
fun MainViewModel.toggleProviderBlock(providerId: String) = adminViewModel.toggleProviderBlock(providerId)
fun MainViewModel.toggleProviderStatus(provider: ProviderEntity) = adminViewModel.toggleProviderStatus(provider)
fun MainViewModel.toggleProviderPin(providerId: String) = adminViewModel.toggleProviderPin(providerId)
fun MainViewModel.toggleProviderVerification(providerId: String) = adminViewModel.toggleProviderVerification(providerId)
fun MainViewModel.toggleProviderRecommendation(providerId: String) = adminViewModel.toggleProviderRecommendation(providerId)
fun MainViewModel.updateProviderEntity(provider: ProviderEntity) = adminViewModel.updateProviderEntity(provider)
fun MainViewModel.editProviderPhoneAndCategory(providerId: String, newPhone: String, newCategoryId: String) = adminViewModel.editProviderPhoneAndCategory(providerId, newPhone, newCategoryId)
fun MainViewModel.addNewProvider(name: String, phone: String, catId: String, area: String, price: Double, isVip: Boolean) = adminViewModel.addNewProvider(name, phone, catId, area, price, isVip)
fun MainViewModel.addNewProviderCustom(
    name: String,
    phone: String,
    catId: String,
    street: String,
    cityId: String,
    profileImage: String,
    idCardImage: String,
    forensicImage: String,
    price: Double,
    isVip: Boolean
) = adminViewModel.addNewProviderCustom(name, phone, catId, street, cityId, profileImage, idCardImage, forensicImage, price, isVip)
fun MainViewModel.rejectTechnician(providerId: String, reason: String = "لم يستوفِ الشروط") =
    adminViewModel.rejectTechnician(providerId, reason)

fun MainViewModel.deleteBanner(bannerId: String) = adminViewModel.deleteBanner(bannerId)
fun MainViewModel.reorderBanners(newOrderedList: List<BannerEntity>) = adminViewModel.reorderBanners(newOrderedList)

fun MainViewModel.placeOrder(order: com.serviseyem.data.OrderEntity) = adminViewModel.placeOrder(order)
fun MainViewModel.updateOrderStatus(orderId: String, status: String) = adminViewModel.updateOrderStatus(orderId, status)
fun MainViewModel.deleteOrder(orderId: String) = adminViewModel.deleteOrder(orderId)
fun MainViewModel.deleteAllOrders(customerPhone: String) = adminViewModel.deleteAllOrders(customerPhone)

fun MainViewModel.addRating(rating: com.serviseyem.data.RatingEntity) = adminViewModel.addRating(rating)
fun MainViewModel.addRatingReply(ratingId: String, replyText: String) = adminViewModel.addRatingReply(ratingId, replyText)
fun MainViewModel.deleteRating(ratingId: String) = adminViewModel.deleteRating(ratingId)
fun MainViewModel.approveRating(ratingId: String, isApproved: Boolean) = adminViewModel.approveRating(ratingId, isApproved)
fun MainViewModel.submitRating(providerId: String, rating: Int) = adminViewModel.submitRating(providerId, rating)
fun MainViewModel.submitRating(ratingEntity: com.serviseyem.data.RatingEntity, onComplete: () -> Unit = {}) =
    adminViewModel.submitRating(ratingEntity, onComplete)
fun MainViewModel.recalculateTargetRating(targetId: String, targetType: String) = adminViewModel.recalculateTargetRating(targetId, targetType)

fun MainViewModel.logAdminActivity(action: String) = adminViewModel.logAdminActivity(action)
fun MainViewModel.logCall(providerId: String, providerName: String) = adminViewModel.logCall(providerId, providerName)
fun MainViewModel.checkAndGetDuplicateAccountType(phone: String, excludeId: String): String? = adminViewModel.checkAndGetDuplicateAccountType(phone, excludeId)

fun MainViewModel.updateProviderPortfolio(providerId: String, images: List<String>) = adminViewModel.updateProviderPortfolio(providerId, images)
fun MainViewModel.addPortfolioImage(providerId: String, imageBase64: String) = adminViewModel.addPortfolioImage(providerId, imageBase64)
fun MainViewModel.removePortfolioImage(providerId: String, index: Int) = adminViewModel.removePortfolioImage(providerId, index)
fun MainViewModel.clearPortfolio(providerId: String) = adminViewModel.clearPortfolio(providerId)

fun MainViewModel.redirectBookingToEntity(bookingId: String, targetEntityId: String, targetEntityName: String, targetPhone: String) = adminViewModel.redirectBookingToEntity(bookingId, targetEntityId, targetEntityName, targetPhone)
fun MainViewModel.unbanEntity(entityType: String, entityId: String) = adminViewModel.unbanEntity(entityType, entityId)
fun MainViewModel.restoreEntity(entityType: String, entityId: String) = adminViewModel.restoreEntity(entityType, entityId)
fun MainViewModel.hardDeleteEntity(entityType: String, entityId: String) = adminViewModel.hardDeleteEntity(entityType, entityId)
fun MainViewModel.exportJobApplicantsCsv(context: android.content.Context) = adminViewModel.exportJobApplicantsCsv(context)

fun MainViewModel.sendNotificationToApplicants(title: String, message: String, jobId: String = "") {
    adminViewModel.sendNotificationToApplicants(title, message, jobId)
}

fun MainViewModel.approveRegisteredUser(userId: String, userName: String = "") =
    adminViewModel.approveRegisteredUser(userId, userName)
fun MainViewModel.toggleBlockRegisteredUser(userId: String, currentBlocked: Boolean, userName: String = "") =
    adminViewModel.toggleBlockRegisteredUser(userId, currentBlocked, userName)
fun MainViewModel.deleteRegisteredUser(userId: String, userName: String = "") =
    adminViewModel.deleteRegisteredUser(userId, userName)
