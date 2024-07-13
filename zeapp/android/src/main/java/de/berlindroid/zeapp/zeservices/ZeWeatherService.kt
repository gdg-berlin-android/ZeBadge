package de.berlindroid.zeapp.zeservices

import de.berlindroid.zeapp.zemodels.WeatherData
import javax.inject.Inject

/**
 * Service to fetch the weather data from the API by the provided date
 */
class ZeWeatherService
    @Inject
    constructor(private val zeWeatherApi: ZeWeatherApi) {
        internal suspend fun fetchWeather(date: String): WeatherData {
            try {
                val weather = zeWeatherApi.getWeather()

                val tempIndex =
                    weather.hourly.time.indexOfFirst {
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
    }
