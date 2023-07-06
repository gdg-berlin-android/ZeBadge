import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import de.berlindroid.zeapp.ZeMainActivity
import org.junit.Rule
import org.junit.Test

class ZeMainActivityTest {

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
