package de.berlindroid.zekompanion.server.user

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileNotFoundException
import java.util.UUID

private const val DB_FILENAME = "./user.db"

@Serializable
data class User(
    val name: String,
    val iconB64: String,
    val description: String,
    val uuid: String,
)

class UserRepository(
    private val users: MutableList<User> = mutableListOf(),
) {
    companion object {
        fun load(): UserRepository = try {
            val users = File(DB_FILENAME).readText()

            try {
                UserRepository(
                    users = Json.decodeFromString(users),
                )
            } catch (e: SerializationException) {
                println("Couldn't read users file. Creating a new one.")
                UserRepository()
            }
        } catch (notFound: FileNotFoundException) {
            UserRepository()
        }

        fun save(repo: UserRepository) = File(DB_FILENAME).writer().use {
            it.write(Json.encodeToString(repo.users))
        }
    }

    fun createUser(user: User): String? {
        val existingUser = users.find { it.uuid == user.uuid }
        if (existingUser != null) {
            println("User '${user.uuid}' already exists.")
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

        users[index] = newUser

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
