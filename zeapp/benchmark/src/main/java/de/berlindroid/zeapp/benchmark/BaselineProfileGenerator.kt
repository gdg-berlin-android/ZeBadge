package de.berlindroid.zeapp.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val baselineRule = BaselineProfileRule()

    @Test
    fun startup() = baselineRule.collect(
        packageName = "de.berlindroid.zeapp",
        includeInStartupProfile = true
    ) {
        pressHome()
        startActivityAndWait()
    }
}
