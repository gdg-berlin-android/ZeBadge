package de.berlindroid.zekompanion.server.zepass

import de.berlindroid.zekompanion.server.user.UserRepository
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileNotFoundException

private const val POSTS_FILENAME = "./zepass.db"

@Serializable
data class Post(
    val uuid: String,
    val posterUUID: String,
    val message: String,
)

@Serializable
data class OptimizedPosts(
    val message: String,
    val profileB64: String?,
)

class ZePassRepository private constructor(
    private val posts: MutableList<Post> = mutableListOf(),
) {
    companion object {
        fun load(): ZePassRepository = try {
            ZePassRepository(
                posts = Json.decodeFromString(File(POSTS_FILENAME).readText()),
            )
        } catch (notFound: FileNotFoundException) {
            println("Couldn't find '$POSTS_FILENAME'.")
            ZePassRepository()
        }

        fun save(repo: ZePassRepository) = File(POSTS_FILENAME).writer().use {
            it.write(Json.encodeToString(repo.posts))
        }
    }

    fun newPost(post: Post): String {
        posts.add(post)
        save(this)

        return post.uuid
    }

    fun getPosts(): List<Post> {
        return posts.toList()
    }

    fun getOptimizedPosts(users: UserRepository): List<OptimizedPosts> {
        return posts.map {
            OptimizedPosts(
                message = it.message,
                profileB64 = users.getUser(it.posterUUID)?.profileB64,
            )
        }
    }
}
