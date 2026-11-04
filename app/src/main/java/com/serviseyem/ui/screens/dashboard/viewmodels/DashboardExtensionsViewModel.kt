package com.serviseyem.ui.screens.dashboard.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serviseyem.data.SpecialOfferEntity
import com.serviseyem.data.repositories.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardExtensionsViewModel @Inject constructor(
    private val repository: IDashboardExtensionsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var activeOwnerId: String = savedStateHandle["ownerId"] ?: ""
    private var offersJob: Job? = null
    private var couponsJob: Job? = null
    private var inventoryJob: Job? = null
    private var loyaltyJob: Job? = null
    private var staffJob: Job? = null

    // Secondary constructor for backward compatibility
    constructor(ownerId: String) : this(
        DashboardExtensionsRepositoryImpl(),
        SavedStateHandle(mapOf("ownerId" to ownerId))
    )

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
        if (activeOwnerId.isNotBlank()) {
            loadForOwner(activeOwnerId)
        }
    }

    fun setOwnerId(ownerId: String) {
        if (ownerId.isNotBlank() && ownerId != activeOwnerId) {
            activeOwnerId = ownerId
            loadForOwner(ownerId)
        }
    }

    private fun loadForOwner(id: String) {
        offersJob?.cancel()
        couponsJob?.cancel()
        inventoryJob?.cancel()
        loyaltyJob?.cancel()
        staffJob?.cancel()

        offersJob = viewModelScope.launch {
            repository.getSpecialOffers(id).collect { _specialOffers.value = it }
        }
        couponsJob = viewModelScope.launch {
            repository.getCoupons(id).collect { _coupons.value = it }
        }
        inventoryJob = viewModelScope.launch {
            repository.getInventory(id).collect { _inventory.value = it }
        }
        loyaltyJob = viewModelScope.launch {
            repository.getLoyaltyPrograms(id).collect { _loyaltyPrograms.value = it }
        }
        staffJob = viewModelScope.launch {
            repository.getStaff(id).collect { _staff.value = it }
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
