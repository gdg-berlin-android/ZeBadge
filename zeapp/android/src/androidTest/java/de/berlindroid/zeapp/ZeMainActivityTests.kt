package de.berlindroid.zeapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.junit.Rule
import org.junit.Test

class ZeMainActivityTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ZeMainActivity>()

    @get:Rule
    val activityScenarioRule = ActivityScenarioRule(ZeMainActivity::class.java)

    @Test
    fun testSendRandomPageButtonWithNoDeviceConnected() {
        composeTestRule.onNodeWithContentDescription("Send random page to badge")
            .performClick()

        composeTestRule.onNodeWithContentDescription("Copy info bar message")
            .assertIsDisplayed()
    }
}
