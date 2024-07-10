package de.berlindroid.zeapp.zeservices

import de.berlindroid.zeapp.zedi.ZeServerBaseUrl
import javax.inject.Inject
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path

@Serializable
data class User(
    val name: String,
    val uuid: String,
    val description: String,
    val chatPhrase: String,
    val profileB64: String?,
)

interface ZeUserService {
    @GET("user/{uuid}")
    suspend fun getOneUser(
        @Path("uuid") uuid: String,
    ): User?

    @GET("user")
    suspend fun getUsers(): List<User>?
}

class ZeUserApi
@Inject constructor(
    private val service: ZeUserService,
    private val baseUrl: ZeServerBaseUrl,
) {
    fun getUserProfilePng(uuid: String): String = "${baseUrl.value}/user/$uuid/png"
    fun getSmallUserProfilePng(uuid: String): String = "${baseUrl.value}/user/$uuid/256x256/png"

    suspend fun getOneUser(uuid: String): User? = service.getOneUser(uuid)
    suspend fun getUsers(): List<User>? = service.getUsers()
}
