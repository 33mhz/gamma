package io.pnut.gamma.data.db.dao

import androidx.room.*
import io.pnut.gamma.data.db.entities.CachedInteractionEntity
import io.pnut.gamma.data.db.entities.CachedPostEntity
import io.pnut.gamma.data.db.entities.CachedUserEntity
import io.pnut.gamma.data.db.entities.UserSuggestionEntity

@Dao
interface CacheDao {
    @Query("SELECT * FROM cached_posts WHERE category = :category AND userId = :userId ORDER BY displayIndex ASC")
    suspend fun getPosts(category: String, userId: String): List<CachedPostEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<CachedPostEntity>)

    @Query("DELETE FROM cached_posts WHERE category = :category AND userId = :userId")
    suspend fun deletePosts(category: String, userId: String)

    @Transaction
    suspend fun replacePosts(category: String, userId: String, posts: List<CachedPostEntity>) {
        deletePosts(category, userId)
        insertPosts(posts)
    }

    @Query("SELECT * FROM cached_users WHERE category = :category AND userId = :userId ORDER BY displayIndex ASC")
    suspend fun getUsers(category: String, userId: String): List<CachedUserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<CachedUserEntity>)

    @Query("DELETE FROM cached_users WHERE category = :category AND userId = :userId")
    suspend fun deleteUsers(category: String, userId: String)

    @Transaction
    suspend fun replaceUsers(category: String, userId: String, users: List<CachedUserEntity>) {
        deleteUsers(category, userId)
        insertUsers(users)
    }

    @Query("SELECT * FROM cached_interactions WHERE category = :category AND userId = :userId ORDER BY displayIndex ASC")
    suspend fun getInteractions(category: String, userId: String): List<CachedInteractionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInteractions(interactions: List<CachedInteractionEntity>)

    @Query("DELETE FROM cached_interactions WHERE category = :category AND userId = :userId")
    suspend fun deleteInteractions(category: String, userId: String)

    @Transaction
    suspend fun replaceInteractions(category: String, userId: String, interactions: List<CachedInteractionEntity>) {
        deleteInteractions(category, userId)
        insertInteractions(interactions)
    }

    @Query("DELETE FROM cached_posts")
    suspend fun deleteAllPosts()

    @Query("DELETE FROM cached_users")
    suspend fun deleteAllUsers()

    @Query("DELETE FROM cached_interactions")
    suspend fun deleteAllInteractions()

    @Query("SELECT * FROM user_suggestions WHERE username LIKE :query || '%' OR name LIKE '%' || :query || '%' ORDER BY youFollow DESC, username ASC LIMIT 8")
    suspend fun searchSuggestions(query: String): List<UserSuggestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuggestions(suggestions: List<UserSuggestionEntity>)

    @Query("DELETE FROM user_suggestions")
    suspend fun clearSuggestions()

    @Transaction
    suspend fun clearAll() {
        deleteAllPosts()
        deleteAllUsers()
        deleteAllInteractions()
        clearSuggestions()
    }
}
