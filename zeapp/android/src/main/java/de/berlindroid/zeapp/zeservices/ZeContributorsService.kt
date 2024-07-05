package de.berlindroid.zeapp.zeservices

import de.berlindroid.zeapp.zeui.zeabout.Contributor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okio.IOException
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import timber.log.Timber
import javax.inject.Inject

class ZeContributorsService @Inject constructor() {

    private val _contributors = MutableStateFlow<List<Contributor>>(emptyList())
    val contributors = _contributors.asStateFlow()
    private var lastPageLoaded = false

    // For some reason GitHub's pagination starts at 1
    private var currentPage = 1

    suspend fun loadMore() {
        if (lastPageLoaded) return
        try {
            val contributors = githubApiService.getContributors(currentPage)
                .map { Contributor(it.login, it.url, it.imageUrl, it.contributions) }
            if (contributors.isEmpty()) lastPageLoaded = true
            currentPage += 1
            _contributors.update { (it + contributors) }
        } catch (ioException: IOException) {
            Timber.w(ioException, "Failed to load contributors")
        } catch (httpException: HttpException) {
            Timber.w(httpException, "Failed to load contributors")
        }
    }
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
    suspend fun getContributors(@Query("page") page: Int, @Query("per_page") pageSize: Int = 30): List<Contributor>
}
