package com.serviseyem.ui.screens.register

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.serviseyem.data.*
import com.serviseyem.ui.helpers.AppPreferenceHelper
import com.serviseyem.ui.isGuestSessionId
import com.serviseyem.ui.screens.register.forms.UnifiedRegistrationForm
import com.serviseyem.ui.screens.register.status.JoinStatus
import com.serviseyem.ui.screens.register.status.JoinStatusUseCase
import com.serviseyem.utils.VisualThemePalette
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RegistrationAndRecoveryTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun testThemePalette(): VisualThemePalette {
        return VisualThemePalette(
            activeId = "test",
            primary = Color(0xFF8B5CF6),
            secondary = Color(0xFF6366F1),
            background = Color(0xFF0B1120),
            surface = Color(0xFF1E293B),
            textPrimary = Color.White,
            textSecondary = Color(0xFF94A3B8),
            accent = Color(0xFFFFD166),
            gradientBrush = Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6366F1))),
            scheme = darkColorScheme()
        )
    }

    @Test
    fun `UnifiedRegistrationForm requires only base mandatory fields for PROVIDER and keeps extras optional`() {
        var submitted: Map<String, String>? = null
        composeTestRule.setContent {
            UnifiedRegistrationForm(
                role = "PROVIDER",
                themeColors = testThemePalette(),
                onRegistrationSuccess = { submitted = it }
            )
        }

        // Disabled initially
        composeTestRule.onNodeWithTag("reg_submit_button").assertIsNotEnabled()

        // Fill only mandatory base fields (no address, no specialization, no experience, no images)
        composeTestRule.onNodeWithTag("reg_full_name_input").performTextInput("محمد علي الأحمدي")
        composeTestRule.onNodeWithTag("reg_phone_input").performTextInput("771234567")
        composeTestRule.onNodeWithTag("reg_password_input").performTextInput("Abc1234")
        composeTestRule.onNodeWithTag("reg_confirm_password_input").performTextInput("Abc1234")
        composeTestRule.onNodeWithTag("reg_terms_checkbox").performScrollTo().performClick()

        composeTestRule.onNodeWithTag("reg_submit_button").performScrollTo().assertIsEnabled().performClick()

        assertNotNull(submitted)
        assertEquals("محمد علي الأحمدي", submitted!!["fullName"])
        assertEquals("771234567", submitted!!["phone"])
        assertEquals("صنعاء", submitted!!["city"])
        assertEquals("", submitted!!["address"])
        assertEquals("", submitted!!["specialization"])
    }

    @Test
    fun `UnifiedRegistrationForm requires entityName for STORE while keeping address workingHours and specialization optional`() {
        var submitted: Map<String, String>? = null
        composeTestRule.setContent {
            UnifiedRegistrationForm(
                role = "STORE",
                themeColors = testThemePalette(),
                onRegistrationSuccess = { submitted = it }
            )
        }

        // Fill base mandatory fields WITHOUT entityName -> still disabled
        composeTestRule.onNodeWithTag("reg_full_name_input").performTextInput("سالم حسن الهمداني")
        composeTestRule.onNodeWithTag("reg_phone_input").performTextInput("+967731234567")
        composeTestRule.onNodeWithTag("reg_password_input").performTextInput("Pass9876")
        composeTestRule.onNodeWithTag("reg_confirm_password_input").performTextInput("Pass9876")
        composeTestRule.onNodeWithTag("reg_terms_checkbox").performScrollTo().performClick()

        composeTestRule.onNodeWithTag("reg_submit_button").performScrollTo().assertIsNotEnabled()

        // Now fill entityName -> becomes enabled and normalizes phone
        composeTestRule.onNodeWithTag("reg_entity_name_input").performScrollTo().performTextInput("متجر النجم للإلكترونيات")
        composeTestRule.onNodeWithTag("reg_submit_button").performScrollTo().assertIsEnabled().performClick()

        assertNotNull(submitted)
        assertEquals("STORE", submitted!!["role"])
        assertEquals("متجر النجم للإلكترونيات", submitted!!["entityName"])
        assertEquals("سالم حسن الهمداني", submitted!!["fullName"])
        assertEquals("731234567", submitted!!["phone"])
    }

    @Test
    fun `JoinStatusUseCase handles JOB_SEEKER pending and active transitions properly`() {
        val useCase = JoinStatusUseCase()
        val pendingSeeker = PendingProviderEntity(
            id = "771112233",
            name = "خالد صالح الحداد",
            phone = "771112233",
            categoryId = "JOB_SEEKER",
            profession = "JOB_SEEKER",
            specialization = "محاسبة",
            area = "صنعاء",
            status = "PENDING",
            submittedAt = 2000L
        )

        val pendingStatus = useCase.determineStatus(
            joinPhone = "771112233",
            pendingProviders = listOf(pendingSeeker),
            providers = emptyList(),
            stores = emptyList(),
            properties = emptyList(),
            categories = emptyList(),
            notifications = emptyList()
        )
        assertTrue(pendingStatus is JoinStatus.PendingJobSeeker)

        val activeStatus = useCase.determineStatus(
            joinPhone = "771112233",
            pendingProviders = emptyList(),
            providers = emptyList(),
            stores = emptyList(),
            properties = emptyList(),
            categories = emptyList(),
            notifications = emptyList(),
            jobSeekers = listOf(
                mapOf(
                    "id" to "771112233",
                    "name" to "خالد صالح الحداد",
                    "phone" to "771112233",
                    "city" to "صنعاء",
                    "isApproved" to true
                )
            )
        )
        assertTrue(activeStatus is JoinStatus.ActiveJobSeeker)
    }

    @Test
    fun `JoinStatusUseCase prevents stale rejection notification from shadowing newer pending submission`() {
        val useCase = JoinStatusUseCase()
        val newerPending = PendingProviderEntity(
            id = "775556677",
            name = "مطعم الأصالة",
            ownerName = "علي قاسم",
            phone = "775556677",
            categoryId = "RESTAURANT",
            profession = "STORE_OWNER",
            area = "تعز",
            status = "PENDING",
            submittedAt = 5000L
        )
        val oldRejection = NotificationEntity(
            id = "notif_old",
            title = "تم رفض الطلب",
            message = "نقص في البيانات",
            targetValue = "775556677",
            timestamp = 1000L
        )

        val resolved = useCase.determineStatus(
            joinPhone = "775556677",
            pendingProviders = listOf(newerPending),
            providers = emptyList(),
            stores = emptyList(),
            properties = emptyList(),
            categories = emptyList(),
            notifications = listOf(oldRejection)
        )
        assertTrue(resolved is JoinStatus.PendingRestaurant)
        assertEquals("علي قاسم", (resolved as JoinStatus.PendingRestaurant).store.ownerName)
    }

    @Test
    fun `isGuestSessionId and Yemen phone normalization distinguish guests from real accounts`() {
        assertTrue(isGuestSessionId(""))
        assertTrue(isGuestSessionId("guest"))
        assertTrue(isGuestSessionId("USR_GUEST_482910"))
        assertFalse(isGuestSessionId("prov_771234567"))
        assertFalse(isGuestSessionId("store_731234567"))

        assertEquals("771234567", AppPreferenceHelper.normalizePhoneNumber("+967 77 123 4567"))
        assertEquals("731234567", AppPreferenceHelper.normalizePhoneNumber("00967731234567"))
        assertEquals("781234567", AppPreferenceHelper.normalizePhoneNumber("٠٧٨١٢٣٤٥٦٧"))
    }
}
