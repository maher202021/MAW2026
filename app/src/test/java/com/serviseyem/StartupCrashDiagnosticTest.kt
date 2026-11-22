package com.serviseyem

import android.content.Context
import android.os.Looper
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.serviseyem.data.AdminSettingsEntity
import com.serviseyem.data.helpers.DefaultDemoData
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.FullSyncManager
import com.serviseyem.utils.OfflineQueueManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class StartupCrashDiagnosticTest {

    @Test
    fun `application and firebase initialize cleanly`() {
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        assertNotNull(appContext)
        assertTrue(FirebaseApp.getApps(appContext).isNotEmpty())
        assertNotNull(FirebaseFirestore.getInstance())
    }

    @Test
    fun `main activity launch and sync initialization succeed without crashing`() {
        val controller = Robolectric.buildActivity(MainActivity::class.java)
            .create()
            .start()
            .resume()

        val activity = controller.get()
        assertNotNull(activity)

        val mainViewModel = ViewModelProvider(activity)[MainViewModel::class.java]
        assertNotNull(mainViewModel)

        mainViewModel.initializeUserIdentity(activity)
        mainViewModel.setupRealtimeFirestoreListeners()
        mainViewModel.settingsViewModel.loadCardSettings()
        mainViewModel.adminViewModel.loadPendingTechnicians()

        // Simulate synced demo data arriving from Firestore
        mainViewModel.appState._providers.value = DefaultDemoData.getDemoProviders()
        mainViewModel.appState._stores.value = DefaultDemoData.getDemoStores()
        mainViewModel.appState._properties.value = DefaultDemoData.getDemoProperties()
        mainViewModel.appState._settings.value = AdminSettingsEntity(bannerEnabled = true, bannerContent = "Test")

        Shadows.shadowOf(Looper.getMainLooper()).runToEndOfTasks()

        controller.pause().stop().destroy()
    }

    @Test
    fun `sync managers work offline without throwing`() = runBlocking {
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        val fullSyncManager = FullSyncManager(appContext)
        val offlineQueue = OfflineQueueManager(appContext)
        fullSyncManager.isSyncRequired()
        val res = offlineQueue.processQueueSuspend()
        assertTrue(res)
    }
}
