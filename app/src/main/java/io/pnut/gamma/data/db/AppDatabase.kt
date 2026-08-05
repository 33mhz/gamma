package io.pnut.gamma.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import io.pnut.gamma.data.db.dao.CacheDao
import io.pnut.gamma.data.db.entities.CachedInteractionEntity
import io.pnut.gamma.data.db.entities.CachedPostEntity
import io.pnut.gamma.data.db.entities.CachedUserEntity

@Database(
    entities = [
        CachedPostEntity::class,
        CachedUserEntity::class,
        CachedInteractionEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cacheDao(): CacheDao
}
