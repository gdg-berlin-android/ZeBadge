package de.berlindroid.zekompanion.desktop.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runDesktopComposeUiTest
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziOptions
import io.github.takahirom.roborazzi.captureRoboImage
import org.junit.Ignore
import org.junit.Test

class ImageManipulatorsTest {
    @Test
    @Ignore("Desktop UI tests temporarily disabled due to scene initialization issues")
    @OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class)
    fun `It render ImageManipulators`() = runDesktopComposeUiTest {
        setContent {
            MaterialTheme {
                ImageManipulators(sendToBadge = {})
            }
        }

        val roborazziOptions = RoborazziOptions(
            recordOptions = RoborazziOptions.RecordOptions(
                resizeScale = 0.5
            ),
            compareOptions = RoborazziOptions.CompareOptions(
                changeThreshold = 0F
            )
        )
        onRoot().captureRoboImage(roborazziOptions = roborazziOptions)
    }
}
