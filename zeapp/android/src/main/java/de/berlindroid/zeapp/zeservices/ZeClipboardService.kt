package de.berlindroid.zeapp.zeservices

import android.content.ClipData
import android.content.Context
import de.berlindroid.zeapp.R

class ZeClipboardService(
        private val context: Context,
    ) {
        fun copyToClipboard(text: String) {
            val manager = context.getSystemService(android.content.ClipboardManager::class.java)
            manager.setPrimaryClip(ClipData.newPlainText(context.getString(R.string.app_name), text))
        }
    }
