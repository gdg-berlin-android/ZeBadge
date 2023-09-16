package de.berlindroid.zeapp.zeservices

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal suspend fun fetchWeather(date: String): WeatherData {
    try {
        val weather = weatherApiService.getWeather()

        val tempIndex = weather.hourly.time.indexOfFirst {
            it.contains("${date}T12:00")
        }
        if (tempIndex == -1) {
            return WeatherData(
                day = null,
                temperature = -1.0,
            )
        }
        val temperature = weather.hourly.temperature[tempIndex]
        val day = weather.hourly.time[tempIndex]
        return WeatherData(
            day = day,
            temperature = temperature,
        )
    } catch (e: Exception) {
        return WeatherData(
            day = null,
            temperature = -42.0,
        )
    }
}

private val retrofit = Retrofit.Builder()
    .baseUrl("https://api.open-meteo.com")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

private val weatherApiService = retrofit.create(WeatherApi::class.java)

private interface WeatherApi {

    data class Weather(
        @SerializedName("hourly")
        val hourly: Hourly,
    )

    data class Hourly(
        @SerializedName("time")
        val time: List<String>,

        @SerializedName("temperature_2m")
        val temperature: List<Double>,
    )

    @GET("v1/forecast?latitude=52.5244&longitude=13.4105&hourly=temperature_2m&forecast_days=16")
    suspend fun getWeather(): Weather
}

data class WeatherData(
    val day: String?,
    val temperature: Double,
) {

    val formattedTemperature: String
        get() = "${temperature}C"

    fun formattedDate(): String {
        return if (day == null) {
            "N/A"
        } else {
            val dateTime = LocalDateTime.parse(day, DateTimeFormatter.ISO_DATE_TIME)
            dateTime.format(DateTimeFormatter.ofPattern("d MMM uuuu"))
        }
    }
}
