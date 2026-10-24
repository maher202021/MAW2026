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

    /**
     * Verifies if the role and assigned permission set has access to a specific permission key.
     * Owner (Main Admin) has ALL 320 permissions automatically.
     */
    fun hasPermission(
        role: AdminRole,
        permission: String,
        supervisorGrantedPermissions: List<String> = emptyList()
    ): Boolean {
        if (role == AdminRole.GUEST) return false
        if (role == AdminRole.OWNER || role == AdminRole.SUPER_ADMIN || role == AdminRole.MAIN_ADMIN) return true

        if (supervisorGrantedPermissions.contains("ALL") || supervisorGrantedPermissions.contains("ALL_538")) return true

        // Admin and Supervisor have full permission by default unless explicit custom list restricts it
        if (role == AdminRole.ADMIN || role == AdminRole.SUPERVISOR) {
            if (supervisorGrantedPermissions.isEmpty()) return true
            if (supervisorGrantedPermissions.contains(permission)) return true
        }

        // Supervisor permissions check
        if (supervisorGrantedPermissions.contains(permission)) return true

        // Category-level check (e.g. if supervisor has "MANAGE_BOOKINGS", they can access all booking permissions)
        val permItem = AdminPermissionsRegistry.allPermissions.find { it.key == permission || it.id == permission }
        if (permItem != null) {
            val mainCatKey = permItem.category.tabKey
            if (supervisorGrantedPermissions.contains(mainCatKey)) return true
        }

        return role == AdminRole.ADMIN || role == AdminRole.SUPER_ADMIN || role == AdminRole.OWNER || role == AdminRole.MAIN_ADMIN || role == AdminRole.SUPERVISOR
    }

    fun hasPermission(role: String, permission: String, grantedPermissions: List<String> = emptyList()): Boolean {
        return hasPermission(RoleManager.fromRoleString(role), permission, grantedPermissions)
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
