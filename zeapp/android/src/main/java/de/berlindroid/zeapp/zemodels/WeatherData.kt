package de.berlindroid.zeapp.zemodels

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class WeatherData(
    val day: String?,
    val temperature: Double,
) {
    val formattedTemperature: String
        get() = "${temperature}C"

    fun formattedDate(): String =
        if (day == null) {
            "N/A"
        } else {
            val dateTime = LocalDateTime.parse(day, DateTimeFormatter.ISO_DATE_TIME)
            dateTime.format(DateTimeFormatter.ofPattern("d MMM uuuu"))
        }
}
