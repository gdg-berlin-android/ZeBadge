package de.berlindroid.zeapp.zeservices

import android.util.Base64
import java.util.UUID
import javax.inject.Inject

private const val SPACE_ESCAPED = "\$SPACE#"
private val CONFIG_REGEX = Regex("""([^\s]+?)=([^\s]+?)(?:\s+|$)""")

/**
 * Parses the badge configuration using the following format:
 *
 * ```
 * wifi_attached=False user.uuid=4d3f6ca7‑d256‑4f84‑a6c6‑099a26055d4c \
 * user.description=Edward$SPACE#Bernard,$SPACE#a$SPACE#veteran \
 * user.name=Edward$SPACE#Bernard developer_mode=True \
 * user.iconB64=eNpjYGBgUJnkqaIg6MDAAmTX/+U+WGf//399OwNjYfv/gk1AQ==
 * ```
 */
class ZeBadgeConfigParser
@Inject
constructor() {
    fun parse(configString: String): ParseResult {
        val configMap =
            CONFIG_REGEX.findAll(configString)
                .map { it.groupValues }
                .associate { it[1] to it[2] }
                .mapValues { it.value.replace(SPACE_ESCAPED, " ") }

        val userId = configMap["user.uuid"]?.let { UUID.fromString(it) }
        val userName = configMap["user.name"]
        val userDescription = configMap["user.description"]
        val userProfilePhoto = configMap["user.iconB64"]?.let { Base64.decode(it, Base64.DEFAULT) }

        val isWiFiAttached = configMap["wifi_attached"]?.toBoolean() ?: false
        val isDeveloperMode = configMap["developer_mode"]?.toBoolean() ?: false

        val userInfo =
            if (
                userId != null && userName != null && userDescription != null && userProfilePhoto != null
            ) {
                UserInfo(userId, userName, userDescription, userProfilePhoto)
            } else {
                null
            }

        return ParseResult(
            userInfo,
            isWiFiAttached,
            isDeveloperMode,
        )
    }
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
        if (other !is UserInfo) return false

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
