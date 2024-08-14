package de.berlindroid.zeapp

import android.app.Application
import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import com.github.takahirom.roborazzi.RoborazziRule
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.After
import org.junit.Rule
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.robolectric.Shadows.shadowOf

// see: https://github.com/bitPogo/keather/blob/45cc973d016f5b8c08906121f321a19353b74a75/android/src/test/kotlin/tech/antibytes/keather/android/app/RoborazziTest.kt

abstract class RoborazziTest(
    captureType: RoborazziRule.CaptureType = RoborazziRule.CaptureType.None,
) {
    // see: https://github.com/robolectric/robolectric/pull/4736#issuecomment-1831034882
    @get:Rule(order = 1)
    val addActivityToRobolectricRule =
        object : TestWatcher() {
            override fun starting(description: Description?) {
                super.starting(description)
                val appContext: Application = ApplicationProvider.getApplicationContext()
                val activityInfo =
                    ActivityInfo().apply {
                        name = ComponentActivity::class.java.name
                        packageName = appContext.packageName
                    }
                shadowOf(appContext.packageManager).addOrUpdateActivity(activityInfo)
            }
        }

    @get:Rule(order = 2)
    val subjectUnderTest = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val roborazziRule = roborazziOf(subjectUnderTest, captureType)

    @After
    fun capture() {
        subjectUnderTest.onRoot().captureRoboImage()
        subjectUnderTest.activityRule.scenario.recreate()
    }
}
