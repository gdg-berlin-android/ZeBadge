package de.berlindroid.zeapp.zedi

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import de.berlindroid.zeapp.zeservices.ZePassApi
import de.berlindroid.zeapp.zeservices.ZePassService
import de.berlindroid.zeapp.zeservices.ZeUserApi
import de.berlindroid.zeapp.zeservices.ZeUserService
import de.berlindroid.zeapp.zeservices.ZeWeatherApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

data class ZeServerBaseUrl(
    val value: String,
)

private val BASE_URL = ZeServerBaseUrl("https://zebadge.app/api/")
private val METEO_URL = ZeServerBaseUrl("https://api.open-meteo.com")

@InstallIn(ViewModelComponent::class)
@Module
object ApiModule {
    @Provides
    fun provideSerialization(): Json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

    @Provides
    fun provideZePassService(
        baseUrl: ZeServerBaseUrl,
        json: Json,
    ): ZePassService =
        Retrofit
            .Builder()
            .baseUrl(baseUrl.value)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ZePassService::class.java)

    @Provides
    fun provideBaseUrl(): ZeServerBaseUrl = BASE_URL

    @Provides
    fun provideZePassApi(service: ZePassService): ZePassApi =
        ZePassApi(
            service,
        )

    @Provides
    fun provideZeUserApi(
        service: ZeUserService,
        baseUrl: ZeServerBaseUrl,
    ): ZeUserApi =
        ZeUserApi(
            service,
            baseUrl,
        )

    @Provides
    fun provideUserService(
        baseUrl: ZeServerBaseUrl,
        json: Json,
    ): ZeUserService =
        Retrofit
            .Builder()
            .baseUrl(baseUrl.value)
            .client(
                OkHttpClient()
                    .newBuilder()
                    .build(),
            ).addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(
                ZeUserService::class.java,
            )

    @Provides
    fun provideZeWeatherApi(json: Json): ZeWeatherApi =
        Retrofit
            .Builder()
            .baseUrl(METEO_URL.value)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ZeWeatherApi::class.java)
}
