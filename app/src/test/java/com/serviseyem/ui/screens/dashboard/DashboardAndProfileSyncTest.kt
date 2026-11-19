package com.serviseyem.ui.screens.dashboard

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.serviseyem.data.*
import com.serviseyem.ui.screens.dashboard.components.UnifiedBookingsSection
import com.serviseyem.ui.screens.dashboard.components.UnifiedProductsServicesSection
import com.serviseyem.ui.screens.dashboard.components.UnifiedSettingsSection
import com.serviseyem.utils.BookingStatus
import com.serviseyem.utils.PasswordHasher
import com.serviseyem.utils.VisualThemePalette
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DashboardAndProfileSyncTest {

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
    fun `UnifiedBusinessAccount reflects live availability across provider store and property`() {
        val unavailableProvider = ProviderEntity(id = "prov_1", name = "فني اختبار", isAvailable = false)
        val activeStore = StoreEntity(id = "store_1", name = "متجر اختبار", isActive = true)
        val inactiveProperty = PropertyEntity(id = "prop_1", title = "شقة اختبار", isActive = false)

        val provAcc = UnifiedBusinessAccount.fromProvider(unavailableProvider)
        val storeAcc = UnifiedBusinessAccount.fromStore(activeStore)
        val propAcc = UnifiedBusinessAccount.fromProperty(inactiveProperty)

        assertFalse(provAcc.isBookingEnabled)
        assertTrue(storeAcc.isBookingEnabled)
        assertFalse(propAcc.isBookingEnabled)
    }

    @Test
    fun `RatingEntity and RatingReviewEntity roundTrip preserves owner reply and timestamps`() {
        val original = RatingEntity(
            id = "rev_1",
            targetId = "store_1",
            targetType = "STORE",
            userName = "أحمد",
            userPhone = "770000001",
            rating = 4.5f,
            comment = "خدمة ممتازة",
            timestamp = 1700000000000L,
            reply = "شكراً لزيارتكم",
            replyTimestamp = 1700000500000L
        )
        val domainReview = original.toReviewEntity()
        val roundTripped = domainReview.toRatingEntity("STORE")

        assertEquals(original.id, roundTripped.id)
        assertEquals(original.targetId, roundTripped.targetId)
        assertEquals(original.userName, roundTripped.userName)
        assertEquals(original.reply, roundTripped.reply)
        assertEquals(original.replyTimestamp, roundTripped.replyTimestamp)
    }

    @Test
    fun `SpecialOfferEntity parseList and serializeList roundTrip preserves coupon and providerId`() {
        val offers = listOf(
            SpecialOfferEntity(
                id = "off_1",
                providerId = "store_abc",
                title = "خصم العيد",
                description = "خصم على جميع الأصناف",
                discountPercent = 20,
                couponCode = "EID20",
                isEnabled = true
            )
        )
        val json = SpecialOfferEntity.serializeList(offers)
        val parsed = SpecialOfferEntity.parseList(json)

        assertEquals(1, parsed.size)
        assertEquals("store_abc", parsed.first().providerId)
        assertEquals("EID20", parsed.first().couponCode)
        assertEquals(20, parsed.first().discountPercent)
        assertTrue(parsed.first().isEnabled)
    }

    @Test
    fun `PasswordHasher hashes and verifies password accurately`() {
        val hash = PasswordHasher.hashPassword("Secret#123")
        assertTrue(PasswordHasher.verifyPassword("Secret#123", hash))
        assertFalse(PasswordHasher.verifyPassword("WrongPass", hash))
    }

    @Test
    fun `BookingStatus canonicalizes APPROVED alias to ACCEPTED`() {
        assertEquals(BookingStatus.ACCEPTED, BookingStatus.fromCode("APPROVED"))
        assertEquals(BookingStatus.ACCEPTED, BookingStatus.fromCode(BookingStatus.ACCEPTED.code))
    }

    @Test
    fun `UnifiedSettingsSection enforces name validation and triggers callbacks`() {
        var savedName = ""
        var savedAvailable = false
        var changedPassPair: Pair<String, String>? = null

        composeTestRule.setContent {
            androidx.compose.foundation.layout.Column(
                modifier = androidx.compose.ui.Modifier.verticalScroll(
                    androidx.compose.foundation.rememberScrollState()
                )
            ) {
                UnifiedSettingsSection(
                    name = "متجر النور",
                    phone = "777111222",
                    cityArea = "صنعاء - حدة",
                    description = "متجر إلكترونيات",
                    workingHours = "9 ص - 9 م",
                    isAvailable = true,
                    themeColors = testThemePalette(),
                    onSaveProfile = { name, _, _, _, _, available ->
                        savedName = name
                        savedAvailable = available
                    },
                    onChangePassword = { oldP, newP ->
                        changedPassPair = oldP to newP
                    }
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("save_profile_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertEquals("متجر النور", savedName)
        assertTrue(savedAvailable)

        composeTestRule.onNodeWithTag("change_password_button").performScrollTo().assertIsNotEnabled()

        composeTestRule.onNodeWithText("كلمة المرور الحالية").performScrollTo().performTextInput("123456")
        composeTestRule.onNodeWithText("كلمة المرور الجديدة (6 أحرف على الأقل)").performScrollTo().performTextInput("abcdef")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("change_password_button").performScrollTo().assertIsEnabled().performClick()
        composeTestRule.waitForIdle()
        assertEquals("123456" to "abcdef", changedPassPair)
    }

    @Test
    fun `UnifiedBookingsSection renders non-duplicate order controls in order mode vs booking mode`() {
        composeTestRule.setContent {
            UnifiedBookingsSection(
                bookingId = "ord_1",
                clientName = "خالد",
                clientPhone = "770123456",
                serviceTitle = "وجبة غداء",
                dateString = "اليوم",
                timeString = "02:00 م",
                status = BookingStatus.PENDING.code,
                themeColors = testThemePalette(),
                onAcceptClick = {},
                onRejectClick = {},
                onUpdateOrderStatus = {}
            )
        }
        composeTestRule.waitForIdle()

        // Order mode shows 'order_prepare_button' and NOT duplicate 'booking_accept_button'
        composeTestRule.onNodeWithTag("order_prepare_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("booking_reject_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("booking_accept_button").assertDoesNotExist()
    }

    @Test
    fun `UnifiedProductsServicesSection toggles availability and fires edit action`() {
        var toggledTo: Boolean? = null
        var editClicked = false

        composeTestRule.setContent {
            UnifiedProductsServicesSection(
                title = "صيانة مكيفات",
                description = "فحص وتعبئة غاز",
                price = "5000",
                isAvailable = true,
                themeColors = testThemePalette(),
                onEditClick = { editClicked = true },
                onDeleteClick = {},
                onToggleAvailability = { toggledTo = it }
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("product_availability_switch").performClick()
        composeTestRule.waitForIdle()
        assertEquals(false, toggledTo)

        composeTestRule.onNodeWithTag("product_edit_button").performClick()
        composeTestRule.waitForIdle()
        assertTrue(editClicked)
    }
}
