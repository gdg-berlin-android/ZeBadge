package de.berlindroid.zekompanion.desktop.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runDesktopComposeUiTest
import com.github.takahirom.roborazzi.RoborazziOptions
import io.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test

class ImageManipulatorsTest {
    @Test
    @OptIn(ExperimentalTestApi::class)
    fun `It render ImageManipulators`() = runDesktopComposeUiTest {
        setContent {
            ImageManipulators {}
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
