package de.berlindroid.zeapp.zeservices

import de.berlindroid.zeapp.zeui.zeabout.Contributor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import javax.inject.Inject

class ZeContributorsService @Inject constructor() {
    fun contributors(): Flow<List<Contributor>> = flow {
        val contributors = githubApiService.getContributors()

        emit(
            contributors.map { Contributor(it.login, it.url, it.imageUrl, it.contributions) },
        )
    }.flowOn(Dispatchers.IO)
}

private val json = Json {
    ignoreUnknownKeys = true
}

private val retrofit = Retrofit.Builder()
    .baseUrl("https://api.github.com/repos/gdg-berlin-android/zebadge/")
    .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
    .build()

private val githubApiService = retrofit.create(GithubApi::class.java)

private interface GithubApi {

    @Serializable
    data class Contributor(
        @SerialName(value = "login")
        val login: String,

        @SerialName(value = "contributions")
        val contributions: Int,

        @SerialName(value = "html_url")
        val url: String,

        @SerialName(value = "avatar_url")
        val imageUrl: String,
    )

    @GET("contributors")
    suspend fun getContributors(): List<Contributor>
}
