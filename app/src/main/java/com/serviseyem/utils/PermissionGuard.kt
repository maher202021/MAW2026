package com.serviseyem.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.models.AdminPermissionsRegistry
import com.serviseyem.data.models.AdminRole

object PermissionGuard {
    const val PERMISSION_BOOKINGS = "MANAGE_BOOKINGS"
    const val PERMISSION_RESTAURANTS = "MANAGE_RESTAURANTS"
    const val PERMISSION_NOTIFICATIONS = "MANAGE_NOTIFICATIONS"
    const val PERMISSION_BANNERS = "MANAGE_BANNERS"
    const val PERMISSION_REG_FORMS = "MANAGE_REG_FORMS"
    const val PERMISSION_QUICK_SERVICE = "MANAGE_QUICK_SERVICE"
    const val PERMISSION_CHAT = "MANAGE_ADVANCED_CHAT"
    const val PERMISSION_THEMES = "MANAGE_THEMES"
    const val PERMISSION_SECTIONS = "MANAGE_NEW_SECTIONS"
    const val PERMISSION_MAP = "MANAGE_MAP"
    const val PERMISSION_STORES = "MANAGE_STORES"
    const val PERMISSION_MEDICAL = "MANAGE_MEDICAL"
    const val PERMISSION_PROPERTIES = "MANAGE_PROPERTIES"
    const val PERMISSION_JOBS = "MANAGE_JOBS"
    const val PERMISSION_CUSTOM_TABS = "MANAGE_CUSTOM_TABS"

    private val legacyPermissionAliases: Map<String, List<String>> = mapOf(
        PERMISSION_BOOKINGS to listOf("BOOKINGS"),
        PERMISSION_RESTAURANTS to listOf("RESTAURANTS"),
        PERMISSION_NOTIFICATIONS to listOf("NOTIFICATIONS"),
        PERMISSION_BANNERS to listOf("BANNERS"),
        PERMISSION_REG_FORMS to listOf("REG_FORMS_MANAGER"),
        PERMISSION_QUICK_SERVICE to listOf("BOOKINGS", "REG_REQ"),
        PERMISSION_CHAT to listOf("CHATS", "ADVANCED_CHAT"),
        PERMISSION_THEMES to listOf("COLORS", "GOLDEN_ICONS"),
        PERMISSION_SECTIONS to listOf("NEW_SECTION_CREATOR", "CATEGORIES"),
        PERMISSION_MAP to listOf("MAP_CONTROLS"),
        PERMISSION_STORES to listOf("STORES"),
        PERMISSION_MEDICAL to listOf("MEDICAL"),
        PERMISSION_PROPERTIES to listOf("PROPERTIES"),
        PERMISSION_JOBS to listOf("JOBS", "APPLICANTS"),
        PERMISSION_CUSTOM_TABS to listOf("CUSTOM_TABS")
    )

    private val tabCategoryMap: Map<String, String> = mapOf(
        "BACKDOOR" to "OWNER_CONTROL",
        "REG_REQ" to "REG_REQ",
        "MANUAL_ADD" to "MANUAL_ADD",
        "STORES" to "STORES",
        "RESTAURANTS" to "RESTAURANTS",
        "MEDICAL" to "MEDICAL",
        "PROPERTIES" to "PROPERTIES",
        "JOBS" to "JOBS",
        "APPLICANTS" to "APPLICANTS",
        "STATS" to "STATS",
        "BOOKINGS" to "BOOKINGS",
        "CHATS" to "CHATS",
        "PROVIDERS" to "PROVIDERS",
        "PASSWORDS_RESET" to "PASSWORDS_RESET",
        "BANNERS" to "BANNERS",
        "CATEGORIES" to "CATEGORIES",
        "CITIES" to "CITIES",
        "COMPLAINTS" to "COMPLAINTS",
        "VIP" to "VIP",
        "SUPERVISORS" to "SUPERVISORS",
        "COLORS" to "COLORS",
        "NOTIFICATIONS" to "NOTIFICATIONS",
        "BACKUP" to "BACKUP",
        "CLEAN" to "CLEAN",
        "REVIEWS" to "REVIEWS",
        "CALLS" to "CALLS",
        "COUPONS" to "COUPONS",
        "BLOCKED" to "BLOCKED",
        "DELETED" to "DELETED",
        "PAYMENTS" to "PAYMENTS",
        "CUSTOM_TABS" to "CUSTOM_TABS",
        "GOLDEN_ICONS" to "GOLDEN_ICONS",
        "ADVANCED_CHAT" to "ADVANCED_CHAT",
        "CARD_CUSTOMIZER" to "CARD_CUSTOMIZER",
        "NEW_SECTION_CREATOR" to "NEW_SECTION_CREATOR",
        "REG_FORMS_MANAGER" to "REG_FORMS_MANAGER",
        "ROLES_PERMISSIONS" to "ROLES_PERMISSIONS",
        "MAP_CONTROLS" to "MAP_CONTROLS",
        "AI_ASSISTANT_PANEL" to "OWNER_CONTROL",
        "ADMIN_PAYMENT_PANEL" to "PAYMENTS",
        "VOICE_CALLS_PANEL" to "CALLS",
        "QUICK_SERVICE" to "BOOKINGS",
        "FINANCIAL_REPORTS" to "STATS",
        "STORAGE_QUOTA" to "BACKUP",
        "SECURITY_AUDIT" to "ROLES_PERMISSIONS",
        "SYSTEM_SETTINGS" to "OWNER_CONTROL",
        "AUTO_DISPATCH" to "BOOKINGS",
        "API_MANAGEMENT" to "OWNER_CONTROL",
        "MAINTENANCE_MODE" to "OWNER_CONTROL",
        "DATA_MANAGEMENT" to "STORES"
    )

    private fun listContainsPermissionOrCategory(
        granted: List<String>,
        permission: String
    ): Boolean {
        if (granted.contains("ALL") || granted.contains("ALL_538")) return true
        if (granted.contains(permission)) return true

        legacyPermissionAliases[permission]?.forEach { tabKey ->
            if (granted.contains(tabKey)) return true
            val hasAnyInCat = AdminPermissionsRegistry.allPermissions.any {
                it.category.tabKey == tabKey && granted.contains(it.key)
            }
            if (hasAnyInCat) return true
        }

        val permItem = AdminPermissionsRegistry.allPermissions.find {
            it.key == permission || it.id == permission
        }
        if (permItem != null && granted.contains(permItem.category.tabKey)) {
            return true
        }

        val hasDirectCategoryKey = AdminPermissionsRegistry.allPermissions.any {
            it.category.tabKey == permission && granted.contains(it.key)
        }
        if (hasDirectCategoryKey) return true

        return false
    }

    /**
     * Verifies if the role and assigned permission set has access to a specific permission key.
     * Owner / Main Admin / Super Admin have all permissions automatically.
     * Supervisor MUST have the permission (or its category/tabKey) in supervisorGrantedPermissions.
     * Admin follows adminMatrix when a custom admin matrix has been configured in Firestore.
     */
    fun hasPermission(
        role: AdminRole,
        permission: String,
        supervisorGrantedPermissions: List<String> = emptyList(),
        adminMatrix: List<String> = emptyList()
    ): Boolean {
        if (role == AdminRole.GUEST) return false
        if (role == AdminRole.OWNER || role == AdminRole.SUPER_ADMIN || role == AdminRole.MAIN_ADMIN) {
            return true
        }

        if (role == AdminRole.ADMIN) {
            // If no custom matrix is saved for ADMIN yet, ADMIN has access;
            // once saved in Firestore (even if empty when deliberately restricted, or a subset), enforce it.
            return if (adminMatrix.isEmpty() && supervisorGrantedPermissions.isEmpty()) {
                true
            } else {
                val effective = adminMatrix.ifEmpty { supervisorGrantedPermissions }
                listContainsPermissionOrCategory(effective, permission)
            }
        }

        // SUPERVISOR and specialized roles (SUPPORT, AUDITOR, OPERATIONS)
        if (supervisorGrantedPermissions.isEmpty()) return false
        return listContainsPermissionOrCategory(supervisorGrantedPermissions, permission)
    }

    fun hasPermission(
        role: String,
        permission: String,
        grantedPermissions: List<String> = emptyList(),
        adminMatrix: List<String> = emptyList()
    ): Boolean {
        return hasPermission(RoleManager.fromRoleString(role), permission, grantedPermissions, adminMatrix)
    }

    fun hasTabAccess(
        roleStr: String,
        tabId: String,
        supervisorPermissions: List<String> = emptyList(),
        adminMatrix: List<String> = emptyList()
    ): Boolean {
        val normalizedRole = roleStr.uppercase().trim()
        if (normalizedRole == "OWNER" || normalizedRole == "MAIN_ADMIN" || normalizedRole == "SUPER_ADMIN") {
            return true
        }
        if (normalizedRole == "GUEST") return false

        val categoryTabKey = tabCategoryMap[tabId] ?: tabId
        val roleEnum = RoleManager.fromRoleString(normalizedRole)
        if (roleEnum == AdminRole.ADMIN) {
            if (adminMatrix.isEmpty()) return true
            if (adminMatrix.contains(tabId) || adminMatrix.contains(categoryTabKey)) return true
            return AdminPermissionsRegistry.allPermissions.any {
                it.category.tabKey == categoryTabKey && adminMatrix.contains(it.key)
            }
        }

        // Strict check for SUPERVISOR and other roles
        if (supervisorPermissions.isEmpty()) return false
        if (supervisorPermissions.contains("ALL") || supervisorPermissions.contains("ALL_538")) return true
        if (supervisorPermissions.contains(tabId) || supervisorPermissions.contains(categoryTabKey)) return true
        return AdminPermissionsRegistry.allPermissions.any {
            it.category.tabKey == categoryTabKey && supervisorPermissions.contains(it.key)
        }
    }

    @Composable
    fun UnauthorizedView(
        customMessage: String = "🔒 ليس لديك صلاحية للوصول إلى هذه الميزة",
        subMessage: String = "يرجى التواصل مع المالك أو المدير الرئيسي لمنحك الصلاحية اللازمة."
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "غير مصرح",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "عذراً - وصول غير مصرح!",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = customMessage,
                        color = Color(0xFFFCA5A5),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = subMessage,
                        color = Color.Gray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
