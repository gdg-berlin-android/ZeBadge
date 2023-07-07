package de.berlindroid.zeapp.zeui.zepages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

@Composable
fun CustomPhrasePage(
    phrase: String = "Your phrase here",
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2E6D31)),
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = phrase,
            color = Color.White,
            maxLines = 2,
            fontSize = 8.sp,
        )
    }
}
