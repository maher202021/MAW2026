package com.serviseyem.ui.screens.dashboard.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serviseyem.data.repositories.IDashboardExtensionsRepository
import com.serviseyem.data.repositories.DashboardExtensionsRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.serviseyem.data.SpecialOfferEntity
import com.serviseyem.data.repositories.InventoryItem
import com.serviseyem.data.repositories.LoyaltyProgram
import com.serviseyem.data.repositories.StaffMember

class DashboardExtensionsViewModel(
    private val ownerId: String,
    private val repository: IDashboardExtensionsRepository = DashboardExtensionsRepositoryImpl()
) : ViewModel() {

    private val _specialOffers = MutableStateFlow<List<SpecialOfferEntity>>(emptyList())
    val specialOffers: StateFlow<List<SpecialOfferEntity>> = _specialOffers.asStateFlow()

    private val _coupons = MutableStateFlow<List<SpecialOfferEntity>>(emptyList())
    val coupons: StateFlow<List<SpecialOfferEntity>> = _coupons.asStateFlow()

    private val _inventory = MutableStateFlow<List<InventoryItem>>(emptyList())
    val inventory: StateFlow<List<InventoryItem>> = _inventory.asStateFlow()

    private val _loyaltyPrograms = MutableStateFlow<List<LoyaltyProgram>>(emptyList())
    val loyaltyPrograms: StateFlow<List<LoyaltyProgram>> = _loyaltyPrograms.asStateFlow()

    private val _staff = MutableStateFlow<List<StaffMember>>(emptyList())
    val staff: StateFlow<List<StaffMember>> = _staff.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getSpecialOffers(ownerId).collect { _specialOffers.value = it }
        }
        viewModelScope.launch {
            repository.getCoupons(ownerId).collect { _coupons.value = it }
        }
        viewModelScope.launch {
            repository.getInventory(ownerId).collect { _inventory.value = it }
        }
        viewModelScope.launch {
            repository.getLoyaltyPrograms(ownerId).collect { _loyaltyPrograms.value = it }
        }
        viewModelScope.launch {
            repository.getStaff(ownerId).collect { _staff.value = it }
        }
    }

    // ✨ م2-ج2: دوال إدارة المخزون عبر الـ Repository
    fun updateInventoryQuantity(itemId: String, newQty: Int, inStock: Boolean) {
        repository.updateInventoryQuantity(itemId, newQty, inStock)
    }

    fun deleteInventoryItem(itemId: String) {
        repository.deleteInventoryItem(itemId)
    }

    fun addInventoryItem(item: InventoryItem) {
        repository.addInventoryItem(item)
    }

    // ✨ م2-ج2: دوال الكوبونات عبر الـ ViewModel
    fun updateCouponStatus(couponId: String, isEnabled: Boolean) {
        repository.updateCouponStatus(couponId, isEnabled)
    }

    fun deleteCoupon(couponId: String) {
        repository.deleteCoupon(couponId)
    }

    fun addCoupon(coupon: SpecialOfferEntity) {
        repository.addCoupon(coupon)
    }

    // ✨ م2-ج2: دوال برامج الولاء عبر الـ ViewModel
    fun updateLoyaltyProgramStatus(programId: String, isEnabled: Boolean) {
        repository.updateLoyaltyProgramStatus(programId, isEnabled)
    }

    fun deleteLoyaltyProgram(programId: String) {
        repository.deleteLoyaltyProgram(programId)
    }

    fun addLoyaltyProgram(program: LoyaltyProgram) {
        repository.addLoyaltyProgram(program)
    }

    // ✨ م2-ج2: دوال الموظفين عبر الـ ViewModel
    fun deleteStaff(staffId: String) {
        repository.deleteStaff(staffId)
    }

    fun addStaff(staffMember: StaffMember) {
        repository.addStaff(staffMember)
    }

    // ✨ م2-ج2: دوال العروض الخاصة عبر الـ ViewModel
    fun updateSpecialOfferStatus(offerId: String, isEnabled: Boolean) {
        repository.updateSpecialOfferStatus(offerId, isEnabled)
    }

    fun deleteSpecialOffer(offerId: String) {
        repository.deleteSpecialOffer(offerId)
    }

    fun addSpecialOffer(offer: SpecialOfferEntity) {
        repository.addSpecialOffer(offer)
    }
}
