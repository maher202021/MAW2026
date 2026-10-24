package com.serviseyem.ui.screens.bookings

import androidx.lifecycle.ViewModel
import com.serviseyem.ui.*
import com.serviseyem.data.BookingEntity
import com.serviseyem.data.ProviderEntity
import com.serviseyem.utils.DateFormatter
import com.serviseyem.utils.HolidayManager
import com.serviseyem.utils.ScheduleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import javax.inject.Inject

data class BookingCalendarUiState(
    val calendarMonthOffset: Int = 0,
    // ✨ م2-ج3: استخدام DateFormatter
    val selectedDateString: String = DateFormatter.formatDateDash(System.currentTimeMillis()),
    val selectedTimeSlot: ScheduleManager.TimeSlot? = null,
    val recurrenceOption: String = "NONE", // "NONE", "WEEKLY", "MONTHLY"
    val clientNotes: String = "",
    val clientAddress: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class BookingCalendarViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(BookingCalendarUiState())
    val uiState: StateFlow<BookingCalendarUiState> = _uiState.asStateFlow()

    fun nextMonth() {
        _uiState.value = _uiState.value.copy(calendarMonthOffset = _uiState.value.calendarMonthOffset + 1)
    }

    fun previousMonth() {
        if (_uiState.value.calendarMonthOffset > 0) {
            _uiState.value = _uiState.value.copy(calendarMonthOffset = _uiState.value.calendarMonthOffset - 1)
        }
    }

    fun selectDate(dateString: String) {
        _uiState.value = _uiState.value.copy(
            selectedDateString = dateString,
            selectedTimeSlot = null
        )
    }

    fun selectTimeSlot(slot: ScheduleManager.TimeSlot?) {
        _uiState.value = _uiState.value.copy(selectedTimeSlot = slot)
    }

    fun setRecurrenceOption(option: String) {
        _uiState.value = _uiState.value.copy(recurrenceOption = option)
    }

    fun setClientNotes(notes: String) {
        _uiState.value = _uiState.value.copy(clientNotes = notes)
    }

    fun setClientAddress(address: String) {
        _uiState.value = _uiState.value.copy(clientAddress = address)
    }

    fun setSubmitting(isSubmitting: Boolean) {
        _uiState.value = _uiState.value.copy(isSubmitting = isSubmitting)
    }

    fun setErrorMessage(message: String?) {
        _uiState.value = _uiState.value.copy(errorMessage = message)
    }
}
