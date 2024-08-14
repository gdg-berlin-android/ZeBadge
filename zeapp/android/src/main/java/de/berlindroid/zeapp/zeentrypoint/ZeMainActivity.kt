package de.berlindroid.zeapp.zeentrypoint

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import de.berlindroid.zeapp.zevm.ZeBadgeViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Main View entrance for the app
 */
class ZeMainActivity : AppCompatActivity() {
    internal val vm: ZeBadgeViewModel by viewModel()

    /**
     * Once created, use the main view composable.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        handleIntent()

        drawsUi()
    }
}

const val ROUTE_HOME = "home"
const val ROUTE_ABOUT = "about"
const val ROUTE_ALTER_EGOS = "alter egos"
const val ROUTE_OPENSOURCE = "opensource"
const val ROUTE_SETTINGS = "settings"
const val ROUTE_ZEPASS = "zepass"
const val ROUTE_LANGUAGES = "languages"
