package com.serviseyem.security

import android.content.Context
import android.net.Uri
import android.webkit.WebResourceRequest
import androidx.test.core.app.ApplicationProvider
import com.serviseyem.data.LocalAppCacheManager
import com.serviseyem.navigation.ValhallaRoutingEngine
import com.serviseyem.utils.AdminSecurityManager
import com.serviseyem.utils.OfflineTileInterceptor
import com.serviseyem.utils.SecurityCryptoUtils
import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SecurityAndAuditRegressionTest {

    @Test
    fun `resolveRoleFromFirebaseClaims enforces strict claim verification`() {
        assertNull(AdminSecurityManager.resolveRoleFromFirebaseClaims(null))
        assertNull(AdminSecurityManager.resolveRoleFromFirebaseClaims(emptyMap()))
        assertNull(AdminSecurityManager.resolveRoleFromFirebaseClaims(mapOf("role" to "user", "vip" to true)))

        assertEquals("OWNER", AdminSecurityManager.resolveRoleFromFirebaseClaims(mapOf("isOwner" to true)))
        assertEquals("OWNER", AdminSecurityManager.resolveRoleFromFirebaseClaims(mapOf("role" to "super_admin")))
        assertEquals("ADMIN", AdminSecurityManager.resolveRoleFromFirebaseClaims(mapOf("admin" to true)))
        assertEquals("ADMIN", AdminSecurityManager.resolveRoleFromFirebaseClaims(mapOf("role" to "ADMIN")))
        assertEquals("SUPERVISOR", AdminSecurityManager.resolveRoleFromFirebaseClaims(mapOf("role" to "supervisor")))
    }

    @Test
    fun `validatePasswordPolicy rejects weak or short passwords and accepts strong ones`() {
        assertFalse(SecurityCryptoUtils.validatePasswordPolicy("123456").first)
        assertFalse(SecurityCryptoUtils.validatePasswordPolicy("1234567").first)
        assertFalse(SecurityCryptoUtils.validatePasswordPolicy("7777777").first)
        assertFalse(SecurityCryptoUtils.validatePasswordPolicy("password").first)

        val strongResult = SecurityCryptoUtils.validatePasswordPolicy("Yemen@2026")
        assertTrue(strongResult.first)
        assertNull(strongResult.second)
    }

    @Test
    fun `booking security helper tracks 3 failed attempts triggers lockout and resets cleanly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val bookingId = "test_booking_lockout_101"

        BookingSecurityHelper.resetAttempts(context, bookingId)
        assertEquals(3, BookingSecurityHelper.getRemainingAttempts(context, bookingId))
        assertFalse(BookingSecurityHelper.isBookingLocked(context, bookingId))

        assertEquals(2, BookingSecurityHelper.recordFailedAttempt(context, bookingId))
        assertEquals(2, BookingSecurityHelper.getRemainingAttempts(context, bookingId))
        assertFalse(BookingSecurityHelper.isBookingLocked(context, bookingId))

        assertEquals(1, BookingSecurityHelper.recordFailedAttempt(context, bookingId))
        assertEquals(1, BookingSecurityHelper.getRemainingAttempts(context, bookingId))
        assertFalse(BookingSecurityHelper.isBookingLocked(context, bookingId))

        assertEquals(0, BookingSecurityHelper.recordFailedAttempt(context, bookingId))
        assertEquals(0, BookingSecurityHelper.getRemainingAttempts(context, bookingId))
        assertTrue(BookingSecurityHelper.isBookingLocked(context, bookingId))

        BookingSecurityHelper.resetAttempts(context, bookingId)
        assertEquals(3, BookingSecurityHelper.getRemainingAttempts(context, bookingId))
        assertFalse(BookingSecurityHelper.isBookingLocked(context, bookingId))
    }

    @Test
    fun `decodePolyline6 decodes valid routes and safely rejects malformed input`() {
        val validPoints = ValhallaRoutingEngine.decodePolyline6("_ibE_ibE_seK_seK")
        assertTrue(validPoints.isNotEmpty())
        assertEquals(2, validPoints.size)

        assertTrue(ValhallaRoutingEngine.decodePolyline6("").isEmpty())
        assertTrue(ValhallaRoutingEngine.decodePolyline6("   ").isEmpty())
        assertTrue(ValhallaRoutingEngine.decodePolyline6("abc123").isEmpty())
    }

    @Test
    fun `offline tile interceptor blocks carto urls and prunes corrupt expired and excess tiles`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val blockedRequest = object : WebResourceRequest {
            override fun getUrl(): Uri = Uri.parse("https://a.basemaps.cartocdn.com/light_all/10/638/466.png")
            override fun isForMainFrame(): Boolean = false
            override fun isRedirect(): Boolean = false
            override fun hasGesture(): Boolean = false
            override fun getMethod(): String = "GET"
            override fun getRequestHeaders(): MutableMap<String, String> = mutableMapOf()
        }

        assertNull(OfflineTileInterceptor.intercept(context, blockedRequest))

        val cacheDir = File(context.applicationContext.cacheDir, "tile_cache_v${OfflineTileInterceptor.CACHE_VERSION}/aa")
        cacheDir.deleteRecursively()
        cacheDir.mkdirs()

        val now = System.currentTimeMillis()
        val corruptTile = File(cacheDir, "corrupt.tile").apply {
            writeBytes(ByteArray(16))
        }
        val expiredTile = File(cacheDir, "expired.tile").apply {
            writeBytes(ByteArray(80))
            setLastModified(now - 10L * 24L * 60L * 60L * 1000L)
        }
        val validOldTile = File(cacheDir, "valid_old.tile").apply {
            writeBytes(ByteArray(80))
            setLastModified(now - 2L * 24L * 60L * 60L * 1000L)
        }
        val validNewTile = File(cacheDir, "valid_new.tile").apply {
            writeBytes(ByteArray(80))
            setLastModified(now)
        }

        OfflineTileInterceptor.pruneCacheIfNeeded(context, maxBytes = 100L)

        assertFalse(corruptTile.exists())
        assertFalse(expiredTile.exists())
        assertFalse(validOldTile.exists())
        assertTrue(validNewTile.exists())

        cacheDir.deleteRecursively()
    }

    @Test
    fun `local app cache manager caps offline queue at 100 newest actions`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val cacheManager = LocalAppCacheManager(context)
        cacheManager.clearOfflineQueue()

        for (i in 1..105) {
            cacheManager.queueOfflineAction(
                LocalAppCacheManager.OfflineSyncAction(
                    id = "action_$i",
                    type = "CREATE_BOOKING",
                    payloadJson = "{\"index\":$i}",
                    createdAt = 1_000L + i
                )
            )
        }

        val queue = JSONArray(cacheManager.getOfflineQueueRaw())
        assertEquals(100, queue.length())
        assertEquals("action_6", queue.getJSONObject(0).getString("id"))
        assertEquals("action_105", queue.getJSONObject(99).getString("id"))

        cacheManager.clearOfflineQueue()
    }
}
