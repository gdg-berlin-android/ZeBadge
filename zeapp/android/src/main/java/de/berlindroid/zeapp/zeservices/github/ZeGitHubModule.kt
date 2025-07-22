package de.berlindroid.zeapp.zeservices.github

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object ZeGitHubModule {
    private val json =
        Json {
            ignoreUnknownKeys = true
        }

    private val retrofit =
        Retrofit
            .Builder()
            .baseUrl("https://api.github.com/repos/gdg-berlin-android/zebadge/")
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    fun gitHubApiService(): GitHubApi = retrofit.create(GitHubApi::class.java)
}
