package com.serviseyem.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serviseyem.data.JobEntity
import com.serviseyem.data.JobApplicationEntity
import com.serviseyem.ui.helpers.AppState
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class JobManagementViewModel @Inject constructor(
    val appState: AppState,
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    val crud = AdminCrudOperations(db)

    val _jobs: MutableStateFlow<List<JobEntity>> get() = appState._jobs
    val jobs: StateFlow<List<JobEntity>> = _jobs.asStateFlow()

    val _jobApplications: MutableStateFlow<List<JobApplicationEntity>> get() = appState._jobApplications
    val jobApplications: StateFlow<List<JobApplicationEntity>> = _jobApplications.asStateFlow()

    var onTriggerNotification: ((String) -> Unit)? = null

    fun saveJob(job: JobEntity) {
        val targetId = job.id.ifBlank { java.util.UUID.randomUUID().toString() }
        val finalJob = if (job.id.isBlank()) job.copy(id = targetId) else job
        viewModelScope.launch {
            crud.saveEntity("jobs", targetId, finalJob,
                onSuccess = { onTriggerNotification?.invoke("✅ تم حفظ الوظيفة بنجاح") },
                onError = { onTriggerNotification?.invoke("❌ فشل حفظ الوظيفة: ${it.message}") }
            )
        }
    }

    fun deleteJob(jobId: String) {
        viewModelScope.launch {
            crud.deleteEntity("jobs", jobId, softDelete = true,
                onSuccess = { onTriggerNotification?.invoke("🗑️ تم نقل الوظيفة للمهملات") },
                onError = { onTriggerNotification?.invoke("❌ فشل الحذف: ${it.message}") }
            )
        }
    }

    fun restoreJob(jobId: String) {
        viewModelScope.launch {
            crud.updateFields("jobs", jobId, mapOf("isDeleted" to false, "deletedAt" to null),
                onSuccess = { onTriggerNotification?.invoke("♻️ تم استعادة الوظيفة بنجاح") }
            )
        }
    }

    fun deleteJobPermanently(jobId: String) {
        viewModelScope.launch {
            crud.deleteEntity("jobs", jobId, softDelete = false,
                onSuccess = { onTriggerNotification?.invoke("🗑️ تم حذف الوظيفة نهائياً") }
            )
        }
    }

    fun setJobApproved(jobId: String, isApproved: Boolean) {
        viewModelScope.launch {
            crud.toggleEntityStatus("jobs", jobId, "isApproved", isApproved)
        }
    }

    fun setJobBlocked(jobId: String, isBlocked: Boolean, reason: String = "") {
        viewModelScope.launch {
            crud.updateFields("jobs", jobId, mapOf("isBlocked" to isBlocked, "blockReason" to reason))
        }
    }

    fun setJobPinned(jobId: String, isPinned: Boolean) {
        viewModelScope.launch {
            crud.toggleEntityStatus("jobs", jobId, "isPinned", isPinned)
        }
    }

    fun setJobVip(jobId: String, isVip: Boolean) {
        viewModelScope.launch {
            crud.toggleEntityStatus("jobs", jobId, "isVip", isVip)
        }
    }

    fun setJobChatDisabled(jobId: String, isDisabled: Boolean) {
        viewModelScope.launch {
            crud.toggleEntityStatus("jobs", jobId, "isChatDisabled", isDisabled)
        }
    }

    fun submitJobApplication(application: JobApplicationEntity) {
        val targetId = application.id.ifBlank { java.util.UUID.randomUUID().toString() }
        val finalApp = if (application.id.isBlank()) application.copy(id = targetId) else application
        viewModelScope.launch {
            crud.saveEntity("job_applications", targetId, finalApp,
                onSuccess = { onTriggerNotification?.invoke("📨 تم تقديم طلب التوظيف بنجاح") },
                onError = { onTriggerNotification?.invoke("❌ فشل التقديم: ${it.message}") }
            )
        }
    }

    fun updateJobApplicationStatus(appId: String, status: String, reason: String = "") {
        viewModelScope.launch {
            crud.updateFields("job_applications", appId, mapOf("status" to status, "statusReason" to reason),
                onSuccess = { onTriggerNotification?.invoke("تم تحديث حالة طلب التوظيف إلى $status") }
            )
        }
    }

    fun acceptJobApplication(appId: String) {
        updateJobApplicationStatus(appId, "ACCEPTED")
    }

    fun rejectJobApplication(appId: String, reason: String) {
        updateJobApplicationStatus(appId, "REJECTED", reason)
    }

    fun deleteJobApplication(appId: String) {
        viewModelScope.launch {
            crud.deleteEntity("job_applications", appId, softDelete = false,
                onSuccess = { onTriggerNotification?.invoke("🗑️ تم حذف طلب التوظيف") }
            )
        }
    }
}
