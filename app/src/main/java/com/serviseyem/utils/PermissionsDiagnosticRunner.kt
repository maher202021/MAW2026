package com.serviseyem.utils

import android.content.Context
import com.serviseyem.data.models.AdminPermissionsRegistry
import com.serviseyem.data.models.AdminRole
import com.serviseyem.data.models.PermissionCategory
import com.serviseyem.data.models.UserRole
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PermissionFullReport(
    val timestamp: String,
    val totalChecks: Int,
    val passed: Int,
    val failed: Int,
    val details: List<TestDetail>
)

class PermissionsDiagnosticRunner(context: Context) {

    private val db = FirebaseFirestore.getInstance()
    private val testSupervisorDocId = "test_diag_supervisor_777"

    suspend fun cleanAllTestData(onProgress: (String) -> Unit = {}) {
        try {
            db.collection("supervisors").document(testSupervisorDocId).delete().await()
        } catch (_: Exception) {
        }
    }

    suspend fun runPermissionsDiagnostics(
        onProgress: (String) -> Unit,
        onComplete: (PermissionFullReport) -> Unit
    ) {
        // [تعديل مهندس النظام] فحص صارم للصلاحيات والأدوار وحراسة الأمن تحت الضغط
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val details = mutableListOf<TestDetail>()
        var errorIndex = 1

        fun recordCheck(
            id: String,
            subCat: String,
            testName: String,
            fileName: String,
            line: Int,
            fnName: String,
            durationMs: Long,
            passed: Boolean,
            expected: String,
            actual: String,
            notes: String,
            probableCauseIfFailed: String = "",
            fixIfFailed: String = ""
        ) {
            val err = if (!passed) {
                TestError(
                    errorIndex = errorIndex++,
                    testName = testName,
                    failedStep = subCat,
                    fileName = fileName,
                    lineNumber = line,
                    functionName = fnName,
                    timestamp = timestamp,
                    errorType = "SecurityPermissionAssertionError",
                    fullMessage = notes,
                    probableCause = probableCauseIfFailed,
                    suggestedFix = fixIfFailed,
                    expectedOutcome = expected,
                    actualOutcome = actual
                )
            } else null

            details.add(
                TestDetail(
                    id = id,
                    systemName = "12. نظام صلاحيات الأدمن والمالك",
                    subCategory = subCat,
                    testName = testName,
                    fileName = fileName,
                    lineNumber = line,
                    functionName = fnName,
                    status = if (passed) "SUCCESS" else "FAILED",
                    message = notes,
                    durationMs = durationMs,
                    expectedOutcome = expected,
                    actualOutcome = actual,
                    error = err
                )
            )
        }

        // 1. Check Owner (AdminRole.OWNER) full access across all registered permissions
        onProgress("🛡️ [1/7] فحص الصلاحيات المطلقة للمالك (OWNER) عبر كافة التبويبات الـ 38...")
        val t1Start = System.currentTimeMillis()
        val allPerms = AdminPermissionsRegistry.allPermissions
        val categoriesCount = PermissionCategory.values().size
        val ownerDenied = allPerms.filter { !PermissionGuard.hasPermission(AdminRole.OWNER, it.key) }
        val ownerBackdoorAllowed = PermissionGuard.hasPermission(AdminRole.OWNER, "BACKDOOR")
        val t1Dur = System.currentTimeMillis() - t1Start
        val t1Passed = allPerms.isNotEmpty() && ownerDenied.isEmpty() && ownerBackdoorAllowed
        recordCheck(
            id = "perm_owner_full",
            subCat = "صلاحيات المالك (OWNER)",
            testName = "التحقق من امتلاك المالك لـ ${allPerms.size} صلاحية في $categoriesCount تبويب إداري",
            fileName = "PermissionGuard.kt",
            line = 58,
            fnName = "PermissionGuard.hasPermission(AdminRole.OWNER)",
            durationMs = t1Dur,
            passed = t1Passed,
            expected = "وصول كامل 100% لجميع الصلاحيات (${allPerms.size}/${allPerms.size}) شاملة البوابة الخلفية",
            actual = "الصلاحيات المفعلة للمالك: ${allPerms.size - ownerDenied.size}/${allPerms.size} | البوابة الخلفية: $ownerBackdoorAllowed",
            notes = if (t1Passed) "✅ المالك يمتلك كافة الصلاحيات (${allPerms.size} صلاحية عبر $categoriesCount فئة) بدون أي نقص" else "❌ المالك فقد ${ownerDenied.size} صلاحية!",
            probableCauseIfFailed = "تعديل غير مقصود في شرط AdminRole.OWNER داخل PermissionGuard.kt السطر 58.",
            fixIfFailed = "تأكد من وجود الشرط: if (role == AdminRole.OWNER || role == AdminRole.SUPER_ADMIN) return true في بداية PermissionGuard.hasPermission."
        )
        delay(200)

        // 2. Check SUPER_ADMIN & ADMIN roles
        onProgress("🛡️ [2/7] فحص صلاحيات المدير العام (SUPER_ADMIN & ADMIN)...")
        val t2Start = System.currentTimeMillis()
        val superAdminDenied = allPerms.filter { !PermissionGuard.hasPermission(AdminRole.SUPER_ADMIN, it.key) }
        val adminDefaultDenied = allPerms.filter { !PermissionGuard.hasPermission(AdminRole.ADMIN, it.key, emptyList()) }
        val t2Dur = System.currentTimeMillis() - t2Start
        val t2Passed = superAdminDenied.isEmpty() && adminDefaultDenied.isEmpty()
        recordCheck(
            id = "perm_admin_full",
            subCat = "صلاحيات الأدمن (ADMIN)",
            testName = "التحقق من صلاحيات المدير العام والمدير التنفيذي عبر جميع الأقسام",
            fileName = "PermissionGuard.kt",
            line = 63,
            fnName = "PermissionGuard.hasPermission(AdminRole.ADMIN)",
            durationMs = t2Dur,
            passed = t2Passed,
            expected = "تمكين كامل لصلاحيات SUPER_ADMIN و ADMIN الافتراضية (${allPerms.size} صلاحية)",
            actual = "SUPER_ADMIN المرفوضة: ${superAdminDenied.size} | ADMIN المرفوضة: ${adminDefaultDenied.size}",
            notes = if (t2Passed) "✅ المدير العام والمدير يمتلكان كامل الصلاحيات الإدارية بدون أي تعارض" else "❌ نقص في صلاحيات الأدمن",
            probableCauseIfFailed = "قائمة الصلاحيات الفارغة للمدير تم تفسيرها كمنع بدلاً من السماح الكامل.",
            fixIfFailed = "راجع السطر 63 في PermissionGuard.kt للتأكد من أن القائمة الفارغة تعطي الأدمن صلاحية كاملة."
        )
        delay(200)

        // 3. Check GUEST isolation
        onProgress("🔒 [3/7] فحص عزل وحجب الزائر (GUEST) عن اللوحة الإدارية والبوابة الخلفية...")
        val t3Start = System.currentTimeMillis()
        val guestAllowed = allPerms.filter { PermissionGuard.hasPermission(AdminRole.GUEST, it.key) }
        val guestBackdoor = PermissionGuard.hasPermission(AdminRole.GUEST, "BACKDOOR")
        val t3Dur = System.currentTimeMillis() - t3Start
        val t3Passed = guestAllowed.isEmpty() && !guestBackdoor
        recordCheck(
            id = "perm_guest_isolation",
            subCat = "حماية وعزل الزائر (GUEST)",
            testName = "التأكد من حجب جميع الصلاحيات الإدارية الـ ${allPerms.size} عن حساب الزائر",
            fileName = "PermissionGuard.kt",
            line = 57,
            fnName = "PermissionGuard.hasPermission(AdminRole.GUEST)",
            durationMs = t3Dur,
            passed = t3Passed,
            expected = "0 صلاحية ممنوحة للزائر (حجب تام 100%)",
            actual = "الصلاحيات المسربة للزائر: ${guestAllowed.size} | البوابة الخلفية: $guestBackdoor",
            notes = if (t3Passed) "✅ حماية تامة: الزائر ممنوع من جميع الصلاحيات الإدارية الـ ${allPerms.size}" else "❌ ثغرة: الزائر يمتلك بعض الصلاحيات!",
            probableCauseIfFailed = "غياب فحص if (role == AdminRole.GUEST) return false في بداية PermissionGuard.",
            fixIfFailed = "أضف if (role == AdminRole.GUEST) return false في السطر 57 من PermissionGuard.kt."
        )
        delay(200)

        // 4. Check Supervisor custom permissions matrix
        onProgress("🔑 [4/7] فحص مصفوفة صلاحيات المشرف المخصص (SUPERVISOR Custom Grants)...")
        val t4Start = System.currentTimeMillis()
        val customGrants = listOf(PermissionGuard.PERMISSION_BOOKINGS, "BOOKINGS")
        val hasBookingsPerm = PermissionGuard.hasPermission(
            AdminRole.SUPERVISOR,
            PermissionGuard.PERMISSION_BOOKINGS,
            customGrants
        )
        val hasAll538Token = PermissionGuard.hasPermission(
            AdminRole.SUPERVISOR,
            PermissionGuard.PERMISSION_THEMES,
            listOf("ALL_538")
        )
        val t4Dur = System.currentTimeMillis() - t4Start
        val t4Passed = hasBookingsPerm && hasAll538Token
        recordCheck(
            id = "perm_supervisor_matrix",
            subCat = "مصفوفة المشرفين (SUPERVISOR)",
            testName = "اختبار منح صلاحيات محددة للمشرف واختبار رمز الصلاحيات الشاملة ALL_538",
            fileName = "PermissionGuard.kt",
            line = 60,
            fnName = "PermissionGuard.hasPermission(AdminRole.SUPERVISOR)",
            durationMs = t4Dur,
            passed = t4Passed,
            expected = "تفعيل صلاحية الحجوزات الممنوحة وتفعيل رمز ALL_538 بنجاح",
            actual = "صلاحية الحجوزات: $hasBookingsPerm | رمز ALL_538: $hasAll538Token",
            notes = if (t4Passed) "✅ مصفوفة صلاحيات المشرفين تعمل بدقة وتدعم التخصيص الفردي والفئوي" else "❌ خلل في التحقق من صلاحيات المشرف",
            probableCauseIfFailed = "عدم مطابقة مفتاح الفئة tabKey مع الصلاحيات الممنوحة للمشرف.",
            fixIfFailed = "راجع منطق مطابقة الفئات في PermissionGuard.kt السطور 60-76."
        )
        delay(200)

        // 5. Check UserRole unified enum (all 11 roles)
        onProgress("👥 [5/7] فحص نظام توحيد الأدوار (UserRole.kt - 11 دوراً)...")
        val t5Start = System.currentTimeMillis()
        val expectedRoles = listOf(
            "GUEST", "CLIENT", "TECHNICIAN", "STORE_OWNER", "RESTAURANT_OWNER",
            "MEDICAL_CENTER", "REAL_ESTATE", "JOB_POSTER", "SUPERVISOR", "ADMIN", "OWNER"
        )
        val resolvedRoles = expectedRoles.map { UserRole.fromCode(it).code }
        val unknownFallback = UserRole.fromCode("INVALID_ROLE_XYZ") == UserRole.GUEST
        val t5Dur = System.currentTimeMillis() - t5Start
        val t5Passed = resolvedRoles == expectedRoles && unknownFallback
        recordCheck(
            id = "perm_user_role_enum",
            subCat = "توحيد الأدوار (UserRole)",
            testName = "فحص جميع الأدوار الـ 11 في UserRole.kt ودالة التحويل الآمن fromCode()",
            fileName = "UserRole.kt",
            line = 32,
            fnName = "UserRole.fromCode()",
            durationMs = t5Dur,
            passed = t5Passed,
            expected = "مطابقة 11/11 دور مع إرجاع GUEST كقيمة افتراضية آمنة للنصوص غير المعروفة",
            actual = "الأدوار المطابقة: ${resolvedRoles.size}/11 | حماية القيم غير المعروفة: $unknownFallback",
            notes = if (t5Passed) "✅ جميع الأدوار الـ 11 في UserRole.kt موحدة وتعمل بشكل سليم" else "❌ نقص في تعريفات UserRole.kt",
            probableCauseIfFailed = "تغيير في أسماء الـ code داخل enum class UserRole.",
            fixIfFailed = "راجع ملف data/models/UserRole.kt وتأكد من وجود الأدوار الـ 11 المعتمدة."
        )
        delay(200)

        // 6. Check RoleManager & AdminSecurityManager role conversion
        onProgress("🔐 [6/7] فحص RoleManager و AdminSecurityManager لتحويل وحماية الأدوار الإدارية...")
        val t6Start = System.currentTimeMillis()
        val rOwner = RoleManager.fromRoleString("OWNER") == AdminRole.OWNER
        val rAdmin = RoleManager.fromRoleString("ADMIN") == AdminRole.ADMIN
        val rSup = RoleManager.fromRoleString("SUPERVISOR") == AdminRole.SUPERVISOR
        val rGuest = RoleManager.fromRoleString("GUEST") == AdminRole.GUEST
        val t6Dur = System.currentTimeMillis() - t6Start
        val t6Passed = rOwner && rAdmin && rSup && rGuest
        recordCheck(
            id = "perm_role_manager_bridge",
            subCat = "تحويل الأدوار (RoleManager)",
            testName = "التحقق من تطابق RoleManager.fromRoleString مع AdminSecurityManager",
            fileName = "RoleManager.kt",
            line = 10,
            fnName = "RoleManager.fromRoleString()",
            durationMs = t6Dur,
            passed = t6Passed,
            expected = "تحويل دقيق لـ OWNER, ADMIN, SUPERVISOR, GUEST إلى AdminRole المقابل",
            actual = "OWNER=$rOwner, ADMIN=$rAdmin, SUPERVISOR=$rSup, GUEST=$rGuest",
            notes = if (t6Passed) "✅ جسر التحويل بين النصوص و AdminRole يعمل بدقة 100%" else "❌ فشل في تحويل نص الدور الإداري",
            probableCauseIfFailed = "خلل في دالة AdminSecurityManager.fromRoleString().",
            fixIfFailed = "راجع دالة fromRoleString في AdminSecurityManager.kt."
        )
        delay(200)

        // 7. Check Firestore supervisor permission persistence & cleanup
        onProgress("☁️ [7/7] فحص حفظ وقراءة صلاحيات المشرفين في Firestore (supervisors collection)...")
        val t7Start = System.currentTimeMillis()
        try {
            val supMap = mapOf(
                "id" to testSupervisorDocId,
                "name" to "مشرف فحص الصلاحيات",
                "phone" to "777999888",
                "role" to "SUPERVISOR",
                "permissions" to listOf("MANAGE_BOOKINGS", "MANAGE_STORES", "MANAGE_REVIEWS"),
                "isActive" to true,
                "createdAt" to System.currentTimeMillis()
            )
            db.collection("supervisors").document(testSupervisorDocId).set(supMap).await()
            val snap = db.collection("supervisors").document(testSupervisorDocId).get().await()
            val savedPerms = (snap.get("permissions") as? List<*>)?.map { it.toString() } ?: emptyList()
            val t7Dur = System.currentTimeMillis() - t7Start
            val t7Passed = snap.exists() && savedPerms.containsAll(listOf("MANAGE_BOOKINGS", "MANAGE_STORES", "MANAGE_REVIEWS"))
            recordCheck(
                id = "perm_firestore_supervisor_sync",
                subCat = "مزامنة صلاحيات المشرفين (Firestore)",
                testName = "كتابة وقراءة مصفوفة صلاحيات مشرف في كولكشن supervisors والتحقق من تطابقها",
                fileName = "PermissionsDiagnosticRunner.kt",
                line = 228,
                fnName = "runPermissionsDiagnostics()",
                durationMs = t7Dur,
                passed = t7Passed,
                expected = "حفظ واسترجاع 3 صلاحيات في وثيقة المشرف بنجاح",
                actual = "الوثيقة موجودة: ${snap.exists()} | الصلاحيات المسترجعة: ${savedPerms.joinToString()}",
                notes = if (t7Passed) "✅ حفظ واسترجاع صلاحيات المشرفين في Firestore يعمل بكفاءة (${t7Dur}ms)" else "❌ لم تتطابق الصلاحيات المحفوظة في Firestore",
                probableCauseIfFailed = "مشكلة في تحويل قائمة الصلاحيات List<String> في Firestore.",
                fixIfFailed = "تأكد من تمرير قائمة نصوص قياسية List<String> للحقل permissions في كولكشن supervisors."
            )
        } catch (e: Exception) {
            val t7Dur = System.currentTimeMillis() - t7Start
            val err = DiagnosticErrorAnalyzer.analyzeException(
                e = e,
                testName = "مزامنة صلاحيات المشرفين في Firestore",
                failedStep = "كتابة/قراءة supervisors/$testSupervisorDocId",
                fallbackFile = "PermissionsDiagnosticRunner.kt",
                fallbackLine = 228,
                fallbackFunction = "runPermissionsDiagnostics()",
                expected = "حفظ وقراءة وثيقة المشرف في Firestore بدون أخطاء",
                errorIndex = errorIndex++,
                timestamp = timestamp
            )
            details.add(
                TestDetail(
                    id = "perm_firestore_supervisor_sync",
                    systemName = "12. نظام صلاحيات الأدمن والمالك",
                    subCategory = "مزامنة صلاحيات المشرفين (Firestore)",
                    testName = "كتابة وقراءة مصفوفة صلاحيات مشرف في كولكشن supervisors",
                    fileName = err.fileName,
                    lineNumber = err.lineNumber,
                    functionName = err.functionName,
                    status = "FAILED",
                    message = "❌ فشل: ${err.fullMessage}",
                    durationMs = t7Dur,
                    expectedOutcome = err.expectedOutcome,
                    actualOutcome = err.actualOutcome,
                    error = err
                )
            )
        } finally {
            cleanAllTestData()
        }

        val passedCount = details.count { it.status == "SUCCESS" }
        val failedCount = details.size - passedCount

        onComplete(
            PermissionFullReport(
                timestamp = timestamp,
                totalChecks = details.size,
                passed = passedCount,
                failed = failedCount,
                details = details
            )
        )
    }
}
