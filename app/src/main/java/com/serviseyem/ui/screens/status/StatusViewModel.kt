package com.serviseyem.ui.screens.status

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.serviseyem.data.BookingEntity
import com.serviseyem.data.NotificationEntity
import com.serviseyem.data.PendingProviderEntity
import com.serviseyem.data.models.InstantRequestEntity
import com.serviseyem.data.repositories.IStatusRepository
import com.serviseyem.data.repositories.SystemStatusMetrics
import com.serviseyem.domain.usecases.ApproveJoinRequestUseCase
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 📊 StatusUiState
 */
@Immutable
data class StatusUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val metrics: SystemStatusMetrics = SystemStatusMetrics(),
    val pendingJoinRequests: List<PendingProviderEntity> = emptyList(),
    val systemBookings: List<BookingEntity> = emptyList(),
    val instantRequests: List<InstantRequestEntity> = emptyList(),
    val notifications: List<NotificationEntity> = emptyList(),
    val selectedTab: StatusTab = StatusTab.OVERVIEW,
    val errorMessage: String? = null
)

/**
 * ⚡ StatusEvent (SharedFlow for one-time events)
 */
sealed class StatusEvent {
    data class ShowToast(val message: String) : StatusEvent()
    data class ShowSnackbar(val message: String) : StatusEvent()
}

/**
 * 🧠 StatusViewModel
 * ViewModel for Platform Status Center, metrics dashboard, pending join requests, and platform notifications.
 * Implements auto-refresh every 30 seconds and offline-first state handling.
 */
class StatusViewModel(
    private val statusRepository: IStatusRepository,
    private val approveJoinRequestUseCase: ApproveJoinRequestUseCase = ApproveJoinRequestUseCase(statusRepository)
) : ViewModel() {

    companion object {
        fun provideFactory(repository: IStatusRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return StatusViewModel(repository) as T
                }
            }
    }

    private val _uiState = MutableStateFlow(StatusUiState())
    val uiState: StateFlow<StatusUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<StatusEvent>()
    val eventFlow: SharedFlow<StatusEvent> = _eventFlow.asSharedFlow()

    private var autoRefreshJob: Job? = null
    private var loadDataJob: Job? = null
    private val refreshMutex = Mutex()
    private val isRefreshingGuard = AtomicBoolean(false)

    init {
        loadStatusData()
        startAutoRefresh()
    }

    fun selectTab(tab: StatusTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun loadStatusData() {
        viewModelScope.launch {
            refreshMutex.withLock {
                loadDataJob?.cancel()
                loadDataJob = viewModelScope.launch {
                    _uiState.value = _uiState.value.copy(isLoading = _uiState.value.metrics.providersCount == 0 && !_uiState.value.isRefreshing)

                    coroutineScope {
                        launch {
                            statusRepository.getSystemMetricsFlow()
                                .catch { e ->
                                    _uiState.value = _uiState.value.copy(
                                        isLoading = false,
                                        errorMessage = e.localizedMessage ?: "خطأ في تحميل مؤشرات النظام"
                                    )
                                }
                                .collect { metrics ->
                                    _uiState.value = _uiState.value.copy(metrics = metrics, isLoading = false)
                                }
                        }

                        launch {
                            statusRepository.getPendingJoinRequestsFlow()
                                .catch { e ->
                                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.localizedMessage)
                                }
                                .collect { requests ->
                                    _uiState.value = _uiState.value.copy(pendingJoinRequests = requests, isLoading = false)
                                }
                        }

                        launch {
                            statusRepository.getSystemBookingsFlow()
                                .catch { e ->
                                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.localizedMessage)
                                }
                                .collect { bookings ->
                                    _uiState.value = _uiState.value.copy(systemBookings = bookings, isLoading = false)
                                }
                        }

                        launch {
                            statusRepository.getInstantRequestsFlow()
                                .catch { e ->
                                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.localizedMessage)
                                }
                                .collect { instantReqs ->
                                    _uiState.value = _uiState.value.copy(instantRequests = instantReqs, isLoading = false)
                                }
                        }

                        launch {
                            statusRepository.getNotificationsFlow()
                                .catch { e ->
                                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.localizedMessage)
                                }
                                .collect { notifs ->
                                    _uiState.value = _uiState.value.copy(notifications = notifs, isLoading = false)
                                }
                        }
                    }
                }
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            if (!isRefreshingGuard.compareAndSet(false, true)) return@launch
            try {
                _uiState.value = _uiState.value.copy(isRefreshing = true)
                val result = statusRepository.refreshSystemStatus()
                loadStatusData()
                _uiState.value = _uiState.value.copy(isLoading = false)
                if (result.isSuccess) {
                    _eventFlow.emit(StatusEvent.ShowSnackbar("🔄 تم تحديث حالات وبيانات المنصة"))
                } else {
                    _eventFlow.emit(StatusEvent.ShowToast("تعذر التحديث: ${result.exceptionOrNull()?.localizedMessage}"))
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                _eventFlow.emit(StatusEvent.ShowToast("تعذر التحديث: ${e.localizedMessage}"))
            } finally {
                _uiState.value = _uiState.value.copy(isRefreshing = false)
                isRefreshingGuard.set(false)
            }
        }
    }

    fun approveJoinRequest(request: PendingProviderEntity, actorRole: String = "ADMIN") {
        viewModelScope.launch {
            val result = approveJoinRequestUseCase(request, actorRole)
            result.onSuccess {
                _eventFlow.emit(StatusEvent.ShowSnackbar("✅ تم قبول طلب انضمام: ${request.name}"))
            }.onFailure { e ->
                _eventFlow.emit(StatusEvent.ShowToast("❌ فشل إجراء القبول: ${e.localizedMessage}"))
            }
        }
    }

    fun rejectJoinRequest(request: PendingProviderEntity, reason: String = "", actorRole: String = "ADMIN") {
        viewModelScope.launch {
            if (actorRole !in listOf("OWNER", "ADMIN", "SUPERVISOR")) {
                _eventFlow.emit(StatusEvent.ShowToast("ليس لديك صلاحية لرفض طلبات الانضمام"))
                return@launch
            }
            val result = statusRepository.rejectJoinRequest(request, reason)
            result.onSuccess {
                _eventFlow.emit(StatusEvent.ShowSnackbar("❌ تم رفض طلب انضمام: ${request.name}"))
            }.onFailure { e ->
                _eventFlow.emit(StatusEvent.ShowToast("فشل إجراء الرفض: ${e.localizedMessage}"))
            }
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            val result = statusRepository.clearNotifications()
            result.onSuccess {
                _eventFlow.emit(StatusEvent.ShowSnackbar("🧹 تم مسح كافة الإشعارات"))
            }
        }
    }

    private fun startAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewModelScope.launch {
            while (isActive) {
                delay(30_000L) // 30 seconds auto refresh loop
                if (isRefreshingGuard.compareAndSet(false, true)) {
                    try {
                        statusRepository.refreshSystemStatus()
                    } catch (e: kotlinx.coroutines.CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        // Silent fail on background refresh
                    } finally {
                        isRefreshingGuard.set(false)
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        autoRefreshJob?.cancel()
        autoRefreshJob = null
        loadDataJob?.cancel()
        loadDataJob = null
    }
}
