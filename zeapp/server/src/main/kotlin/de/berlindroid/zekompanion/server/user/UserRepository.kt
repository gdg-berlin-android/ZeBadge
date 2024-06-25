package de.berlindroid.zekompanion.server.user

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileNotFoundException
import java.util.UUID

private const val DB_FILENAME = "./user.db"

@Serializable
data class User(
    val name: String? = null,
    val iconUrl: String? = null,
    val uuid: String? = null,
)

class UserRepository private constructor(
    private val users: MutableList<User> = mutableListOf(),
) {
    companion object {
        fun load(): UserRepository = try {
            UserRepository(
                users = Json.decodeFromString(File(DB_FILENAME).readText()),
            )
        } catch (notFound: FileNotFoundException) {
            UserRepository()
        }

        fun save(repo: UserRepository) = File(DB_FILENAME).writer().use {
            it.write(Json.encodeToString(repo.users))
        }
    }

    fun createUser(user: User): String? {
        val existingUser = users.find { it.uuid == user.uuid }
        if (existingUser != null || user.uuid != null) {
            return null
        }

        val uuid = UUID.randomUUID().toString()
        users.add(user.copy(uuid = uuid))

        save(this)

        return uuid
    }

    fun getUser(uuid: String): User? {
        return users.find { it.uuid == uuid }
    }

    fun getUsers(): List<User> {
        return users.toList()
    }

    fun updateUser(newUser: User): Boolean {
        val index = users.indexOfFirst { it.uuid == newUser.uuid }
        if (index < 0) {
            return false
        }

        if (newUser.uuid == null) {
            return false
        } else {
            users[index] = newUser
        }

        save(this)

        return true
    }

    fun deleteUser(uuid: String): Boolean {
        val user = users.find { it.uuid == uuid }
        if (user == null) {
            return false
        }

        users.remove(user)

        save(this)

        return true
    }
}
