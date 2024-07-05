package de.berlindroid.zeapp.zeservices

import de.berlindroid.zeapp.zedi.ZeServerBaseUrl
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path
import javax.inject.Inject

@Serializable
data class User(
    val name: String,
    val uuid: String,
    val profileB64: String,
)

interface ZeUserService {
    @GET("user/{uuid}")
    suspend fun getOneUser(
        @Path("uuid") uuid: String,
    ): User?
}

class ZeUserApi
@Inject constructor(
    private val service: ZeUserService,
    private val baseUrl: ZeServerBaseUrl,
) {
    fun getUserProfilePng(uuid: String): String = "${baseUrl.value}/user/$uuid/png"

    suspend fun getOneUser(uuid: String): User? = service.getOneUser(uuid)
}
