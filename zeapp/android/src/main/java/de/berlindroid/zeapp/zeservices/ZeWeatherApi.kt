package de.berlindroid.zeapp.zeservices

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET

interface ZeWeatherApi {

    @Serializable
    data class Weather(
        @SerialName(value = "hourly")
        val hourly: Hourly,
    )

    @Serializable
    data class Hourly(
        @SerialName(value = "time")
        val time: List<String>,

        @SerialName(value = "temperature_2m")
        val temperature: List<Double>,
    )

    @GET("v1/forecast?latitude=52.5244&longitude=13.4105&hourly=temperature_2m&forecast_days=16")
    suspend fun getWeather(): Weather
}
