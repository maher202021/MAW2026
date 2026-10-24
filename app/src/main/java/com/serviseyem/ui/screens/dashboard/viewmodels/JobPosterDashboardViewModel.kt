package com.serviseyem.ui.screens.dashboard.viewmodels

import androidx.lifecycle.ViewModel
import com.serviseyem.ui.*
import androidx.lifecycle.viewModelScope
import com.serviseyem.data.repositories.IDashboardRepository
import com.serviseyem.data.repositories.IProductsRepository
import com.serviseyem.domain.entities.ProductItemEntity
import com.serviseyem.domain.entities.JobPostItem
import com.serviseyem.ui.screens.dashboard.DashboardEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.flow.catch
import javax.inject.Inject

@HiltViewModel
class JobPosterDashboardViewModel @Inject constructor(
    private val dashboardRepository: IDashboardRepository,
    private val productsRepository: IProductsRepository,
    private val jobRepository: com.serviseyem.data.repositories.JobRepository
) : ViewModel() {

    private var ownerId: String = ""

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _jobs = MutableStateFlow<List<JobPostItem>>(emptySet<JobPostItem>().toList())
    val jobs: StateFlow<List<JobPostItem>> = _jobs.asStateFlow()

    private val _eventFlow = MutableSharedFlow<DashboardEvent>()
    val eventFlow: SharedFlow<DashboardEvent> = _eventFlow.asSharedFlow()

    private var jobsJob: kotlinx.coroutines.Job? = null

    fun initialize(id: String) {
        if (ownerId == id) return
        ownerId = id
        loadDashboardData()
        jobsJob?.cancel()
        jobsJob = viewModelScope.launch {
            jobRepository.getJobs(ownerId)
                .catch { /* silent */ }
                .collect {
                    _jobs.value = it
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
                    dashboardRepository.getDashboardStats(ownerId, "JOB")
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
            }
        }
    }

    fun postJob(title: String, company: String, salary: String, requirements: String) {
        if (title.isBlank()) return
        
        val jobId = java.util.UUID.randomUUID().toString()
        val existing = _jobs.value.find { it.title == title && it.companyName == company }
        if (existing != null) {
            viewModelScope.launch {
                _eventFlow.emit(DashboardEvent.ShowToast("الوظيفة موجودة مسبقاً ⚠️"))
            }
            return
        }

        val item = JobPostItem(
            id = jobId,
            title = title,
            companyName = company,
            salary = salary,
            requirements = requirements,
            applicantsCount = 0
        )
        viewModelScope.launch {
            jobRepository.postJob(ownerId, item).onSuccess {
                _eventFlow.emit(DashboardEvent.ShowToast("تم نشر الشاغر الوظيفي بنجاح 💼"))
            }.onFailure {
                _eventFlow.emit(DashboardEvent.ShowToast("حدث خطأ أثناء النشر"))
            }
        }
    }

    fun deleteJob(id: String) {
        viewModelScope.launch {
            jobRepository.deleteJob(id).onSuccess {
                _eventFlow.emit(DashboardEvent.ShowToast("تم حذف إعلان الوظيفة 🗑️"))
            }.onFailure {
                _eventFlow.emit(DashboardEvent.ShowToast("حدث خطأ أثناء الحذف"))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        jobsJob?.cancel()
        jobsJob = null
    }
}
