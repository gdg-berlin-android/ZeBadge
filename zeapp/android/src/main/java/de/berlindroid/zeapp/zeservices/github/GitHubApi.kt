package de.berlindroid.zeapp.zeservices.github

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

interface GitHubApi {

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
    suspend fun getContributors(@Query("page") page: Int): List<Contributor>
}
