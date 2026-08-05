package io.pnut.gamma.domain.repository

import android.content.Context
import io.pnut.gamma.data.db.dao.CacheDao
import io.pnut.gamma.data.db.entities.CachedInteractionEntity
import io.pnut.gamma.data.db.entities.CachedPostEntity
import io.pnut.gamma.data.db.entities.CachedUserEntity
import io.pnut.gamma.domain.entity.Interaction
import io.pnut.gamma.domain.entity.Post
import io.pnut.gamma.domain.entity.Token
import io.pnut.gamma.domain.entity.TokenJsonAdapter
import io.pnut.gamma.domain.entity.User
import io.pnut.gamma.domain.model.CachedList
import io.pnut.gamma.domain.model.PageableItemWrapper
import io.pnut.gamma.domain.model.StreamType
import io.pnut.gamma.domain.model.UserListType
import io.pnut.gamma.presentation.util.PageableItemWrapperConverter
import io.pnut.gamma.util.LogUtil
import io.pnut.gamma.util.MoshiSingleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import kotlin.math.min


class PnutCacheRepository(
    private val currentUserId: String?,
    context: Context,
    private val cacheDao: CacheDao
) : IPnutCacheRepository {
    private val baseCacheDir =
        File(getUserCacheDir(context), currentUserId.orEmpty()).apply {
            mkdirs()
        }

    private val safeUserId: String
        get() = currentUserId ?: ""

    private sealed class CachePath {
        open val name: String = this::class.java.simpleName
        object Token : CachePath()
    }

    override suspend fun getToken(): Token? = withContext(Dispatchers.IO) {
        return@withContext try {
            val file = CachePath.Token.getFile()
            if (!file.exists()) return@withContext null
            val inputStream = FileInputStream(file)
            inputStream.reader().use {
                val json = it.readText()
                TokenJsonAdapter(MoshiSingleton.moshi).fromJson(json)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun CachePath.getFile(): File {
        val file = File("$baseCacheDir/$name")
        LogUtil.e("file $file, ${file.parentFile}")
        file.parentFile?.mkdirs()
        file.createNewFile()
        return file
    }

    override suspend fun storeToken(token: Token) = withContext(Dispatchers.IO) {
        val file = CachePath.Token.getFile()
        val json = TokenJsonAdapter(MoshiSingleton.moshi).toJson(token)
        file.writer().use {
            it.write(json)
        }
    }

    override suspend fun storePosts(
        posts: List<PageableItemWrapper<Post>>,
        streamType: StreamType,
        cacheSize: Int
    ) = withContext(Dispatchers.IO) {
        val category = streamType.categoryName
        val resizedList = if (cacheSize > 0) {
            posts.subList(0, min(posts.size, cacheSize))
        } else {
            posts
        }

        val adapter = MoshiSingleton.moshi.adapter(PageableItemWrapperConverter.StorablePost::class.java)
        val entities = resizedList.mapIndexed { index, item ->
            val storable = when (item) {
                is PageableItemWrapper.Pager -> PageableItemWrapperConverter.StorablePost.Pager(item)
                is PageableItemWrapper.Item -> PageableItemWrapperConverter.StorablePost.Item(item)
            }
            CachedPostEntity(
                id = item.uniqueKey,
                category = category,
                userId = safeUserId,
                json = adapter.toJson(storable),
                paginationId = item.getSinceId(),
                displayIndex = index
            )
        }
        cacheDao.replacePosts(category, safeUserId, entities)
    }

    override suspend fun storeInteractions(
        interactions: List<PageableItemWrapper<Interaction>>,
        cacheSize: Int
    ) = withContext(Dispatchers.IO) {
        val category = "Interaction"
        val resizedList = if (cacheSize > 0) {
            interactions.subList(0, min(interactions.size, cacheSize))
        } else {
            interactions
        }

        val adapter = MoshiSingleton.moshi.adapter(PageableItemWrapperConverter.StorableInteraction::class.java)
        val entities = resizedList.mapIndexed { index, item ->
            val storable = when (item) {
                is PageableItemWrapper.Pager -> PageableItemWrapperConverter.StorableInteraction.Pager(item)
                is PageableItemWrapper.Item -> PageableItemWrapperConverter.StorableInteraction.Item(item)
            }
            CachedInteractionEntity(
                id = item.uniqueKey,
                category = category,
                userId = safeUserId,
                json = adapter.toJson(storable),
                paginationId = item.getSinceId(),
                displayIndex = index
            )
        }
        cacheDao.replaceInteractions(category, safeUserId, entities)
    }

    override suspend fun storeUsers(
        users: List<PageableItemWrapper<User>>,
        userListType: UserListType,
        cacheSize: Int
    ) = withContext(Dispatchers.IO) {
        val category = userListType.categoryName
        val resizedList = if (cacheSize > 0) {
            users.subList(0, min(users.size, cacheSize))
        } else {
            users
        }

        val adapter = MoshiSingleton.moshi.adapter(PageableItemWrapperConverter.StorableUser::class.java)
        val entities = resizedList.mapIndexed { index, item ->
            val storable = when (item) {
                is PageableItemWrapper.Pager -> PageableItemWrapperConverter.StorableUser.Pager(item)
                is PageableItemWrapper.Item -> PageableItemWrapperConverter.StorableUser.Item(item)
            }
            CachedUserEntity(
                id = item.uniqueKey,
                category = category,
                userId = safeUserId,
                json = adapter.toJson(storable),
                paginationId = item.getSinceId(),
                displayIndex = index
            )
        }
        cacheDao.replaceUsers(category, safeUserId, entities)
    }

    override suspend fun getPosts(streamType: StreamType): CachedList<Post> = withContext(Dispatchers.IO) {
        val category = streamType.categoryName
        val entities = cacheDao.getPosts(category, safeUserId)
        val adapter = MoshiSingleton.moshi.adapter(PageableItemWrapperConverter.StorablePost::class.java)
        val list = entities.mapNotNull { entity ->
            when (val storable = adapter.fromJson(entity.json)) {
                is PageableItemWrapperConverter.StorablePost.Item -> storable.pageableItemWrapper
                is PageableItemWrapperConverter.StorablePost.Pager -> storable.pager
                else -> null
            }
        }
        CachedList(list)
    }

    override suspend fun getInteractions(): CachedList<Interaction> = withContext(Dispatchers.IO) {
        val category = "Interaction"
        val entities = cacheDao.getInteractions(category, safeUserId)
        val adapter = MoshiSingleton.moshi.adapter(PageableItemWrapperConverter.StorableInteraction::class.java)
        val list = entities.mapNotNull { entity ->
            when (val storable = adapter.fromJson(entity.json)) {
                is PageableItemWrapperConverter.StorableInteraction.Item -> storable.pageableItemWrapper
                is PageableItemWrapperConverter.StorableInteraction.Pager -> storable.pager
                else -> null
            }
        }
        CachedList(list)
    }

    override suspend fun getUsers(userListType: UserListType): CachedList<User> = withContext(Dispatchers.IO) {
        val category = userListType.categoryName
        val entities = cacheDao.getUsers(category, safeUserId)
        val adapter = MoshiSingleton.moshi.adapter(PageableItemWrapperConverter.StorableUser::class.java)
        val list = entities.mapNotNull { entity ->
            when (val storable = adapter.fromJson(entity.json)) {
                is PageableItemWrapperConverter.StorableUser.Item -> storable.pageableItemWrapper
                is PageableItemWrapperConverter.StorableUser.Pager -> storable.pager
                else -> null
            }
        }
        CachedList(list)
    }

    override suspend fun clearAll() = withContext(Dispatchers.IO) {
        cacheDao.clearAll()
    }

    companion object {
        private const val USER_CACHE_DIR_NAME = "userCache"
        fun getUserCacheDir(context: Context): File {
            return File(context.cacheDir, USER_CACHE_DIR_NAME)
        }
    }
}
