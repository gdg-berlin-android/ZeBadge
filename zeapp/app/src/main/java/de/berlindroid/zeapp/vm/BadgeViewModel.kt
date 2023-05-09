package de.berlindroid.zeapp.vm

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.graphics.scale
import androidx.lifecycle.AndroidViewModel
import de.berlindroid.zeapp.PAGE_HEIGHT
import de.berlindroid.zeapp.PAGE_WIDTH
import de.berlindroid.zeapp.R
import de.berlindroid.zeapp.bits.isBinary
import de.berlindroid.zeapp.hardware.Badge

class BadgeViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val sharedPreferences =
        getApplication<Application>()
            .getSharedPreferences(
                "defaults",
                Application.MODE_PRIVATE
            )

    private val badge = Badge()

    fun sendPageToDevice(name: String, page: Bitmap) {
        if (page.isBinary()) {
            badge.sendPage(
                getApplication<Application>().applicationContext,
                name,
                page
            )
        } else {
            Toast.makeText(
                getApplication(),
                "Please send binary image.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun resetNamePage() {
        namePage.value = initialNamePage()
    }

    fun simulatorButtonPressed(button: Char) {
        currentPage.value = button
    }

    fun currentPageCharToPageBitmap(): Bitmap {
        return when (currentPage.value) {
            'a' -> namePage.value
            'b' -> firstSponsorPage.value
            'c' -> secondSponsorPage.value
            'u' -> firstCustomPage.value
            'd' -> secondCustomPage.value
            else -> namePage.value
        }
    }

    private fun initialNamePage(): Bitmap =
        BitmapFactory.decodeResource(
            getApplication<Application>().resources,
            R.drawable.sample_badge,
        ).scale(PAGE_WIDTH, PAGE_HEIGHT)

    var name = mutableStateOf("Your Name")
    var contact = mutableStateOf("Your Contact")
    var nameEditorDialog = mutableStateOf(false)


    var namePage = mutableStateOf(initialNamePage())

    var firstSponsorPage = mutableStateOf(
        BitmapFactory.decodeResource(
            getApplication<Application>().resources,
            R.drawable.page_google,
        ).scale(PAGE_WIDTH, PAGE_HEIGHT)
    )

    var secondSponsorPage = mutableStateOf(
        BitmapFactory.decodeResource(
            getApplication<Application>().resources,
            R.drawable.page_telekom,
        ).scale(PAGE_WIDTH, PAGE_HEIGHT)
    )

    var firstCustomPage =
        mutableStateOf(
            BitmapFactory.decodeResource(
                getApplication<Application>().resources,
                R.drawable.soon,
            ).scale(PAGE_WIDTH, PAGE_HEIGHT)
        )

    var secondCustomPage =
        mutableStateOf(
            BitmapFactory.decodeResource(
                getApplication<Application>().resources,
                R.drawable.soon,
            ).scale(PAGE_WIDTH, PAGE_HEIGHT)
        )

    var currentPage = mutableStateOf('a')
}