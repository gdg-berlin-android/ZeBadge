package de.berlindroid.zeapp

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RoborazziRule
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.After
import org.junit.Rule

// see: https://github.com/bitPogo/keather/blob/45cc973d016f5b8c08906121f321a19353b74a75/android/src/test/kotlin/tech/antibytes/keather/android/app/RoborazziTest.kt

abstract class RoborazziTest(
    captureType: RoborazziRule.CaptureType = RoborazziRule.CaptureType.None,
) {
    @get:Rule
    val subjectUnderTest = createComposeRule()

    @get:Rule
    val roborazziRule =
        RoborazziRule(
            composeRule = subjectUnderTest,
            captureRoot = subjectUnderTest.onRoot(),
            options =
                RoborazziRule.Options(
                    captureType = captureType,
                ),
        )

    @After
    fun capture() {
        subjectUnderTest.onRoot().captureRoboImage()
    }
}
