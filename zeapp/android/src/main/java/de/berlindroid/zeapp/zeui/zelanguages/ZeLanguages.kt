package de.berlindroid.zeapp.zeui.zelanguages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ZeLanguages(
    paddingValues: PaddingValues,
    onLocaleChange: (String?) -> Unit,
) {
    val appLanguages = SUPPORTED_LANGUAGES

    Column(
        modifier = Modifier
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
    ) {
        Text(text = "App Language", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            item {
                Button(onClick = { onLocaleChange(null) }, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Set default")
                }
            }
            items(appLanguages) {
                Button(onClick = { onLocaleChange(it) }, modifier = Modifier.fillMaxWidth()) {
                    Text(text = it)
                }
            }
        }
    }
}

private val SUPPORTED_LANGUAGES = listOf(
    "ar-EG", // Arabic (Egypt)
    "bs", // Bosnian
    "de-DE", // German (Germany)
    "en-GB", // English (United Kingdom)
    "es", // Spanish
    "et", // Estonian
    "fr", // French
    "hi", // Hindi
    "hr-HR", // Croatian (Croatia)
    "it", // Italian
    "ja", // Japanese
    "lt", // Lithuanian
    "mr", // Marathi
    "nl", // Dutch
    "pl", // Polish
    "pt-BR", // Portuguese (Brazil)
    "ru", // Russian
    "sq", // Albanian
    "tr", // Turkish
    "uk", // Ukrainian
    "ur", // Urdu
)
