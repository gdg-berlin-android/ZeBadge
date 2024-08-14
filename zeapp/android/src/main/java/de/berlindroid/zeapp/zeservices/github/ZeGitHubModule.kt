package de.berlindroid.zeapp.zeservices.github

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

val zeGitHubModule = module {
    single<GitHubApi> {
        Retrofit.Builder()
            .baseUrl("https://api.github.com/repos/gdg-berlin-android/zebadge/")
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build().create(GitHubApi::class.java)
    }
}
