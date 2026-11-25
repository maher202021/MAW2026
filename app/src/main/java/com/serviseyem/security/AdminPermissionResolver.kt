package com.serviseyem.security

/**
 * AdminPermissionResolver
 * سياسة الرفض افتراضياً للصلاحيات الإدارية:
 * - المالك / المدير العام (OWNER, MAIN_ADMIN, SUPER_ADMIN) يمتلك كل الصلاحيات.
 * - الإداري والمشرف: الصلاحيات الصريحة الخاصة بالحساب -> مصفوفة الدور من Firestore -> الحد الأدنى الافتراضي للدور.
 * - لا يُمنح "كل الصلاحيات" لمجرد فراغ القائمة.
 */
object AdminPermissionResolver {
    const val ALL = "ALL"
    const val ALL_STAR = "*"
    const val ALL_538 = "ALL_538"

    val DEFAULT_SUPERVISOR: Set<String> = setOf(
        "STATS",
        "REG_REQ",
        "CHATS",
        "COMPLAINTS"
    )

    val DEFAULT_ADMIN: Set<String> = DEFAULT_SUPERVISOR + setOf(
        "PROVIDERS",
        "STORES",
        "RESTAURANTS",
        "MEDICAL",
        "PROPERTIES",
        "JOBS",
        "APPLICANTS",
        "BOOKINGS",
        "NOTIFICATIONS"
    )

    fun getAllAdminPermissions(): Set<String> {
        val registryKeys = try {
            com.serviseyem.data.models.AdminPermissionsRegistry.allPermissions
                .filter { it.category != com.serviseyem.data.models.PermissionCategory.OWNER_CONTROL }
                .map { it.key }
                .toSet()
        } catch (_: Exception) {
            emptySet()
        }
        return DEFAULT_ADMIN + registryKeys + setOf(
            "PASSWORDS_RESET", "PAYMENTS", "WALLETS", "SETTINGS", "USERS",
            "BANNERS", "CATEGORIES", "CITIES", "OFFERS", "COUPONS", "REPORTS",
            "AUDIT_LOGS", "ROLES_PERMISSIONS", "ANALYTICS", "MAP", "QUICK_SERVICE",
            "REVIEWS", "CALLS"
        )
    }

    fun resolve(
        role: String,
        explicit: Collection<String>? = null,
        matrix: Map<String, Collection<String>> = emptyMap()
    ): Set<String> {
        val r = role.uppercase().trim()
        if (r == "OWNER" || r == "MAIN_ADMIN" || r == "SUPER_ADMIN") {
            return setOf(ALL)
        }
        val own = explicit?.map { it.trim() }?.filter { it.isNotBlank() }?.toSet().orEmpty()
        if (own.isNotEmpty()) return own

        val byRole = matrix[r]?.map { it.trim() }?.filter { it.isNotBlank() }?.toSet().orEmpty()
        if (byRole.isNotEmpty()) return byRole

        return when (r) {
            "ADMIN" -> getAllAdminPermissions()
            "SUPERVISOR", "SUPPORT", "AUDITOR", "OPERATIONS" -> DEFAULT_SUPERVISOR
            else -> emptySet()
        }
    }

    fun hasAccess(perms: Collection<String>, key: String): Boolean {
        if (perms.isEmpty()) return false
        return ALL in perms || ALL_STAR in perms || ALL_538 in perms || key in perms
    }
}
