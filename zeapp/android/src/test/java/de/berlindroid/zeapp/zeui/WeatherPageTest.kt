package de.berlindroid.zeapp.zeui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.berlindroid.zeapp.RoborazziTest
import de.berlindroid.zeapp.zeui.zepages.WeatherPage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33])
class WeatherPageTest : RoborazziTest() {
    @Test
    fun `It renders the WeatherPage`() {
        subjectUnderTest.setContent {
            WeatherPage()
        }
    }
}
