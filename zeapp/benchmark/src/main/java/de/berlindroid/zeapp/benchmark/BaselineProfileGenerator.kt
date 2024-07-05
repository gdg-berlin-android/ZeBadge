package de.berlindroid.zeapp.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    companion object {
        const val MENU_BUTTON_DESCRIPTION = "Menu button"
        const val SAVE_ALL_PAGE_TO_BADGE_ITEM_TEXT = "Save all pages to badge"
        const val CONTRIBUTORS_ITEM_TEXT = "Contributors"
        const val OPEN_SOURCE_ITEM_TEXT = "Open Source"
        const val OPEN_RELEASE_PAGE_ITEM_TEXT = "Open release page"
        const val MEDIUM_DELAY = 5000L
        const val LARGE_DELAY = 10_000L
        const val SMALL_DELAY = 2_000L
    }

    @get:Rule
    val baselineRule = BaselineProfileRule()

    @Test
    fun startup() = baselineRule.collect(
        packageName = "de.berlindroid.zeapp",
        includeInStartupProfile = true,
    ) {
        pressHome()
        startActivityAndWait()
        device.wakeUp()
        device.openDrawer()

        device.openDrawerItems()
    }

    private fun UiDevice.openDrawer() {
        wait(Until.hasObject(By.desc(MENU_BUTTON_DESCRIPTION)), MEDIUM_DELAY)
        findObject(By.desc(MENU_BUTTON_DESCRIPTION))?.click()
    }

    private fun UiDevice.openDrawerItems() {
        wait(Until.hasObject(By.text(SAVE_ALL_PAGE_TO_BADGE_ITEM_TEXT)), MEDIUM_DELAY)
        findObject(By.desc(SAVE_ALL_PAGE_TO_BADGE_ITEM_TEXT)).click()
        waitForIdle(SMALL_DELAY)

        openDrawer()

        waitForIdle(SMALL_DELAY)
        wait(Until.hasObject(By.text(CONTRIBUTORS_ITEM_TEXT)), MEDIUM_DELAY)
        findObject(By.text(CONTRIBUTORS_ITEM_TEXT)).click()
        waitForIdle(SMALL_DELAY)

        openDrawer()

        waitForIdle(SMALL_DELAY)
        wait(Until.hasObject(By.text(OPEN_SOURCE_ITEM_TEXT)), LARGE_DELAY)
        findObject(By.text(OPEN_SOURCE_ITEM_TEXT)).click()
        waitForIdle(SMALL_DELAY)

        openDrawer()

        waitForIdle(SMALL_DELAY)
        wait(Until.hasObject(By.text(OPEN_RELEASE_PAGE_ITEM_TEXT)), MEDIUM_DELAY)
        findObject(By.text(OPEN_RELEASE_PAGE_ITEM_TEXT)).click()
        waitForIdle(SMALL_DELAY)
    }
}
