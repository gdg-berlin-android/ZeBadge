package de.berlindroid.zekompanion.server.zepass

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileNotFoundException
import java.util.UUID

private const val POSTS_FILENAME = "./zepass.db"

@Serializable
data class Post(
    val uuid: String,
    val posterUUID: String,
    val message: String,
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
        val repo = load()
        posts.add(post)
        save(repo)

        return post.uuid
    }

    fun getPosts(): List<Post> {
        return posts.toList()
    }
}
