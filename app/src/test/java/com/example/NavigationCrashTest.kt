package com.example

import androidx.lifecycle.ViewModelProvider
import com.example.ui.viewmodel.BrowserViewModel
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NavigationCrashTest {

    @Test
    fun testNavigateToKeywordDoesNotCrash() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()
        assertNotNull(activity)

        val viewModel = ViewModelProvider(activity)[BrowserViewModel::class.java]
        viewModel.navigateTo("google")
        ShadowLooper.idleMainLooper()

        val tab = viewModel.tabManager.currentTab
        assertNotNull(tab)
        assertFalse(tab!!.isNewTab)
    }

    @Test
    fun testNavigateToUrlDoesNotCrash() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()
        assertNotNull(activity)

        val viewModel = ViewModelProvider(activity)[BrowserViewModel::class.java]
        viewModel.navigateTo("https://example.com")
        ShadowLooper.idleMainLooper()

        val tab = viewModel.tabManager.currentTab
        assertNotNull(tab)
        assertFalse(tab!!.isNewTab)
    }
}
