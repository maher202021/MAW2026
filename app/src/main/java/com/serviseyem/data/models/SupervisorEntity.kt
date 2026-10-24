package com.serviseyem.data

import androidx.annotation.Keep

@Keep
data class SupervisorEntity(
    val id: String = "",
    val name: String = "",
    val role: String = "", // "ADMIN", "AUDITOR", "SUPPORT", "OPERATIONS"
    val passcodeHash: String = "",
    val permissions: List<String> = emptyList()
)
