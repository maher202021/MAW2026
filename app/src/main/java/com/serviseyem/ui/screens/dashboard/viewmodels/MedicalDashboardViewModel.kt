package com.serviseyem.ui.screens.dashboard.viewmodels

import androidx.lifecycle.ViewModel
import com.serviseyem.ui.*
import androidx.lifecycle.viewModelScope
import com.serviseyem.data.repositories.IDashboardRepository
import com.serviseyem.data.repositories.IProductsRepository
import com.serviseyem.data.repositories.IRatingsRepository
import com.serviseyem.domain.entities.ProductItemEntity
import com.serviseyem.domain.entities.DoctorItem
import com.serviseyem.ui.screens.dashboard.DashboardEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.flow.catch
import javax.inject.Inject

@HiltViewModel
class MedicalDashboardViewModel @Inject constructor(
    private val dashboardRepository: IDashboardRepository,
    private val productsRepository: IProductsRepository,
    private val ratingsRepository: IRatingsRepository,
    private val medicalRepository: com.serviseyem.data.repositories.MedicalRepository
) : ViewModel() {

    private var ownerId: String = ""

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _doctors = MutableStateFlow<List<DoctorItem>>(emptySet<DoctorItem>().toList())
    val doctors: StateFlow<List<DoctorItem>> = _doctors.asStateFlow()

    private val _eventFlow = MutableSharedFlow<DashboardEvent>()
    val eventFlow: SharedFlow<DashboardEvent> = _eventFlow.asSharedFlow()

    private var doctorsJob: kotlinx.coroutines.Job? = null

    fun initialize(id: String) {
        if (ownerId == id) return
        ownerId = id
        loadDashboardData()
        doctorsJob?.cancel()
        doctorsJob = viewModelScope.launch {
            medicalRepository.getDoctors(ownerId)
                .catch { /* silent */ }
                .collect {
                    _doctors.value = it
                }
        }
    }

    fun selectTab(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(activeTab = tabIndex)
    }

    fun loadDashboardData() {
        if (ownerId.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            supervisorScope {
                launch {
                    dashboardRepository.getDashboardStats(ownerId, "MEDICAL")
                        .catch { e -> 
                            _eventFlow.emit(DashboardEvent.ShowToast(e.message ?: "خطأ في الإحصائيات")) 
                            _uiState.value = _uiState.value.copy(isLoading = false)
                        }
                        .collect { stats ->
                            _uiState.value = _uiState.value.copy(stats = stats, isLoading = false)
                        }
                }
                launch {
                    productsRepository.getOwnerProducts(ownerId)
                        .catch { e -> _eventFlow.emit(DashboardEvent.ShowToast(e.message ?: "خطأ في المنتجات")) }
                        .collect { prods ->
                            _uiState.value = _uiState.value.copy(products = prods)
                        }
                }
                launch {
                    ratingsRepository.getTargetRatings(ownerId)
                        .catch { e -> _eventFlow.emit(DashboardEvent.ShowToast(e.message ?: "خطأ في التقييمات")) }
                        .collect { revs ->
                            _uiState.value = _uiState.value.copy(reviews = revs)
                        }
                }
            }
        }
    }

    fun addDoctor(name: String, specialty: String, workingHours: String) {
        if (name.isBlank()) return
        
        val docId = java.util.UUID.randomUUID().toString()
        val existing = _doctors.value.find { it.name == name && it.specialty == specialty }
        if (existing != null) {
            viewModelScope.launch {
                _eventFlow.emit(DashboardEvent.ShowToast("الطبيب موجود مسبقاً ⚠️"))
            }
            return
        }

        val doc = DoctorItem(
            id = docId,
            name = name,
            specialty = specialty,
            workingHours = workingHours
        )
        viewModelScope.launch {
            medicalRepository.addDoctor(ownerId, doc).onSuccess {
                _eventFlow.emit(DashboardEvent.ShowToast("تمت إضافة الطبيب بنجاح 🩺"))
            }.onFailure {
                _eventFlow.emit(DashboardEvent.ShowToast("حدث خطأ أثناء الإضافة"))
            }
        }
    }

    fun deleteDoctor(id: String) {
        viewModelScope.launch {
            medicalRepository.deleteDoctor(id).onSuccess {
                _eventFlow.emit(DashboardEvent.ShowToast("تم حذف الطبيب 🗑️"))
            }.onFailure {
                _eventFlow.emit(DashboardEvent.ShowToast("حدث خطأ أثناء الحذف"))
            }
        }
    }

    fun addMedicalService(title: String, priceYer: Double, description: String = "") {
        viewModelScope.launch {
            if (title.isBlank()) return@launch
            val service = ProductItemEntity(
                ownerId = ownerId,
                title = title,
                priceYer = priceYer,
                description = description,
                category = "MEDICAL"
            )
            productsRepository.addProduct(service).onSuccess {
                _eventFlow.emit(DashboardEvent.ShowToast("تمت إضافة الخدمة الطبية 💊"))
            }.onFailure { e ->
                _eventFlow.emit(DashboardEvent.ShowToast(e.localizedMessage ?: "فشل إضافة الخدمة"))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        doctorsJob?.cancel()
        doctorsJob = null
    }
}
