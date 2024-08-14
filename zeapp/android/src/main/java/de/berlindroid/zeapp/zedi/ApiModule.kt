package de.berlindroid.zeapp.zedi

import de.berlindroid.zeapp.zeservices.ZePassApi
import de.berlindroid.zeapp.zeservices.ZePassService
import de.berlindroid.zeapp.zeservices.ZeUserApi
import de.berlindroid.zeapp.zeservices.ZeUserService
import de.berlindroid.zeapp.zeservices.ZeWeatherApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

data class ZeServerBaseUrl(
    val value: String,
)

private val BASE_URL = ZeServerBaseUrl("https://zebadge.app/api/")
private val METEO_URL = ZeServerBaseUrl("https://api.open-meteo.com")

val apiModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    single<ZePassService> {
        Retrofit.Builder()
            .baseUrl(get<ZeServerBaseUrl>().value)
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ZePassService::class.java)
    }

    single<ZeServerBaseUrl> {
        BASE_URL
    }

    single<ZePassApi> {
        ZePassApi(
            get(),
        )
    }

    single<ZeUserApi> {
        ZeUserApi(
            get(),
            get(),
        )
    }

    single<ZeUserService> {
        Retrofit.Builder()
            .baseUrl(get<ZeServerBaseUrl>().value)
            .client(
                OkHttpClient()
                    .newBuilder()
                    .build(),
            )
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build()
            .create(
                ZeUserService::class.java,
            )
    }

    single<ZeWeatherApi> {
        Retrofit.Builder()
            .baseUrl(METEO_URL.value)
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ZeWeatherApi::class.java)
    }
}
