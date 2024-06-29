package de.berlindroid.zeapp.zeservices

import android.util.Base64
import java.util.UUID
import javax.inject.Inject

private const val SPACE_ESCAPED = "\$SPACE#"

/**
 * Parses the badge configuration using the following format:
 *
 * ```
 * wifi_attached=False user.uuid=4d3f6ca7‑d256‑4f84‑a6c6‑099a26055d4c \
 * user.description=Edward$SPACE#Bernard,$SPACE#a$SPACE#veteran \
 * user.name=Edward$SPACE#Bernard developer_mode=True \
 * user.iconB64=eNpjYGBgUJnkqaIg6MDAAmTX/+U+WGf//399OwNjYfv/gk1AQ=="
 * ```
 */
class ZeBadgeConfigParser
    @Inject
    constructor() {
        fun parse(configString: String): ParseResult {
            val configMap =
                configString
                    .split("\\s+".toRegex())
                    .map { it.split("=", limit = 2) }
                    .filter { it.size == 2 }
                    .associate { it[0] to it[1] }

            val userId = parseUserId(configMap)
            val userName = parseUserName(configMap)
            val userDescription = parseUserDescription(configMap)
            val userProfilePhoto = parseUserProfilePhoto(configMap)
            val userInfo =
                if (
                    userId != null && userName != null && userDescription != null && userProfilePhoto != null
                ) {
                    UserInfo(userId, userName, userDescription, userProfilePhoto)
                } else {
                    null
                }

            return ParseResult(
                userInfo = userInfo,
                isWiFiAttached = parseWiFiAttached(configMap),
                isDeveloperMode = parseDeveloperMode(configMap),
            )
        }

        private fun parseWiFiAttached(configMap: Map<String, String>): Boolean = configMap["wifi_attached"]?.toBoolean() ?: false

        private fun parseDeveloperMode(configMap: Map<String, String>): Boolean = configMap["developer_mode"]?.toBoolean() ?: false

        private fun parseUserId(configMap: Map<String, String>): UUID? = configMap["user.uuid"]?.let { UUID.fromString(it) }

        private fun parseUserProfilePhoto(configMap: Map<String, String>): ByteArray? =
            configMap["user.iconB64"]?.let { Base64.decode(it, Base64.DEFAULT) }

        private fun parseUserName(configMap: Map<String, String>): String? = configMap["user.name"]?.unescape()

        private fun parseUserDescription(configMap: Map<String, String>): String? = configMap["user.description"]?.unescape()

        private fun String.unescape() = replace(SPACE_ESCAPED, " ")
    }

data class ParseResult(
    val userInfo: UserInfo?,
    val isWiFiAttached: Boolean,
    val isDeveloperMode: Boolean,
) {
    fun flatten(): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        userInfo?.flatten()?.forEach { (key, value) ->
            map["user.$key"] = value
        }
        map["isWiFiAttached"] = isWiFiAttached
        map["isDeveloperMode"] = isDeveloperMode
        return map
    }
}

data class UserInfo(
    val id: UUID,
    val name: String,
    val description: String,
    val profilePhoto: ByteArray,
) {
    fun flatten() =
        mapOf<String, Any?>(
            "id" to id.toString(),
            "name" to name,
            "description" to description,
            "profilePhoto" to Base64.encode(profilePhoto, Base64.DEFAULT),
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserInfo

        if (id != other.id) return false
        if (name != other.name) return false
        if (description != other.description) return false
        if (!profilePhoto.contentEquals(other.profilePhoto)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + profilePhoto.contentHashCode()
        return result
    }
}
