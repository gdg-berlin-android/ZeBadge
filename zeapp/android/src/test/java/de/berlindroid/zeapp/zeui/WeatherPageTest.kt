package de.berlindroid.zeapp.zeui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.berlindroid.zeapp.zeui.zepages.WeatherPage
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33])
class WeatherPageTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    @Ignore("Robolectric configuration issue with ComponentActivity resolution")
    fun `It renders the WeatherPage`() {
        composeTestRule.setContent {
            WeatherPage()
        }
    }
}
