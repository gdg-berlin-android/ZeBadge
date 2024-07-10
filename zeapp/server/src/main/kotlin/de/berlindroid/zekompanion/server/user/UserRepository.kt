package de.berlindroid.zekompanion.server.user

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileNotFoundException

private const val DB_FILENAME = "./user.db"

@Serializable
data class User(
    val uuid: String,
    val name: String,
    val description: String,
    val profileB64: String?,
    val chatPhrase: String?,
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

        users.add(user)

        save(this)

        return user.uuid
    }

    fun getUser(uuid: String): User? {
        return users.find { it.uuid == uuid }
    }

    fun getUserByIndex(index: Int): User? {
        return users.getOrNull(index)?.copy(uuid = "$index")
    }

    fun getUsers(): List<User> {
        return users.toList()
    }

    fun getIndexedUsers(): List<User> {
        return users.mapIndexed { index, user -> user.copy(uuid = "$index") }
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

    fun updateUserByIndex(index: Int, newUser: User): Boolean {
        val oldUser = users.getOrNull(index) ?: return false
        users[index] = newUser.copy(uuid = oldUser.uuid)

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
