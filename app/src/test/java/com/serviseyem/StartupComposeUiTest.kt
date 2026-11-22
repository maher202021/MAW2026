package com.serviseyem

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import com.serviseyem.data.AdminSettingsEntity
import com.serviseyem.data.BannerEntity
import com.serviseyem.data.helpers.DefaultDemoData
import com.serviseyem.ui.MainViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class StartupComposeUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `first frames splash and home browser compose and measure cleanly`() {
        composeRule.mainClock.autoAdvance = false
        // Initial frame with YemenSplashScreen + AppNavigator
        composeRule.mainClock.advanceTimeByFrame()

        composeRule.activityRule.scenario.onActivity { activity ->
            val vm = ViewModelProvider(activity)[MainViewModel::class.java]
            vm.appState._providers.value = DefaultDemoData.getDemoProviders()
            vm.appState._stores.value = DefaultDemoData.getDemoStores()
            vm.appState._properties.value = DefaultDemoData.getDemoProperties()
            vm.appState._banners.value = listOf(
                BannerEntity(id = "b1", title = "Test Banner", url = "", type = "TEXT", duration = 5)
            )
            vm.appState._settings.value = AdminSettingsEntity(bannerEnabled = true, bannerContent = "Banner Msg")
        }

        // Advance past splash delay (2200ms + 400ms fadeOut)
        composeRule.mainClock.advanceTimeBy(3000L)
        composeRule.mainClock.advanceTimeByFrame()
    }
}
