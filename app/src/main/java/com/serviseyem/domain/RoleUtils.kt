package com.serviseyem.domain

/**
 * Unified alias to [com.serviseyem.data.models.AdminRole] for backward compatibility across domain and UI layers.
 */
typealias AdminRole = com.serviseyem.data.models.AdminRole

fun String.toAdminRole(): AdminRole = AdminRole.fromString(this)

fun String.isAdmin(): Boolean {
    val role = toAdminRole()
    return role == AdminRole.OWNER ||
           role == AdminRole.ADMIN ||
           role == AdminRole.SUPER_ADMIN ||
           role == AdminRole.MAIN_ADMIN
}

fun String.isSupervisor(): Boolean = toAdminRole() == AdminRole.SUPERVISOR
fun String.isOwner(): Boolean {
    val role = toAdminRole()
    return role == AdminRole.OWNER || role == AdminRole.MAIN_ADMIN
}
fun String.canManageContent(): Boolean =
    isAdmin() || isSupervisor() || toAdminRole() == AdminRole.OPERATIONS
