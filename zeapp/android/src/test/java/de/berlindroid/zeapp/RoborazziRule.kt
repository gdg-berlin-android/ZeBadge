package de.berlindroid.zeapp

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.github.takahirom.roborazzi.RoborazziRule

// see: https://github.com/bitPogo/keather/blob/45cc973d016f5b8c08906121f321a19353b74a75/android/src/test/kotlin/tech/antibytes/keather/android/app/RoborazziRule.kt

fun roborazziOf(
    szenario: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity>,
    captureType: RoborazziRule.CaptureType = RoborazziRule.CaptureType.None,
): RoborazziRule =
    RoborazziRule(
        composeRule = szenario,
        captureRoot = szenario.onRoot(),
        options =
            RoborazziRule.Options(
                captureType = captureType,
            ),
    )
