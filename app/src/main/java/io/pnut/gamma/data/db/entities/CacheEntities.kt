package io.pnut.gamma.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_posts", primaryKeys = ["id", "category", "userId"])
data class CachedPostEntity(
    val id: String,
    val category: String,
    val userId: String,
    val json: String,
    val paginationId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val displayIndex: Int = 0
)

@Entity(tableName = "cached_users", primaryKeys = ["id", "category", "userId"])
data class CachedUserEntity(
    val id: String,
    val category: String,
    val userId: String,
    val json: String,
    val paginationId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val displayIndex: Int = 0
)

@Entity(tableName = "cached_interactions", primaryKeys = ["id", "category", "userId"])
data class CachedInteractionEntity(
    val id: String,
    val category: String,
    val userId: String,
    val json: String,
    val paginationId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val displayIndex: Int = 0
)

@Entity(tableName = "user_suggestions")
data class UserSuggestionEntity(
    @PrimaryKey val id: String,
    val username: String,
    val name: String?,
    val youFollow: Boolean,
)
