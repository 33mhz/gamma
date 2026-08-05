package io.pnut.gamma.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import androidx.room.Room
import io.pnut.gamma.data.db.AppDatabase
import io.pnut.gamma.data.db.dao.CacheDao
import io.pnut.gamma.domain.repository.AccountRepository
import io.pnut.gamma.domain.repository.IAccountRepository
import io.pnut.gamma.domain.repository.IPnutCacheRepository
import io.pnut.gamma.domain.repository.IPnutRepository
import io.pnut.gamma.domain.repository.IPreferenceRepository
import io.pnut.gamma.domain.repository.PnutCacheRepository
import io.pnut.gamma.domain.repository.PnutRepository
import io.pnut.gamma.domain.repository.PreferenceRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAccountRepository(@ApplicationContext context: Context): IAccountRepository =
        AccountRepository(context)

    @Provides
    @Singleton
    fun providePnutRepository(@ApplicationContext context: Context, accountRepository: IAccountRepository): IPnutRepository =
        PnutRepository(context, accountRepository.getDefaultAccount()?.token)

    @Provides
    @Singleton
    fun providePreferenceRepository(@ApplicationContext context: Context): IPreferenceRepository =
        PreferenceRepository(context)

    @Provides
    fun providePnutCacheRepository(
        @ApplicationContext context: Context,
        accountRepository: IAccountRepository,
        cacheDao: CacheDao
    ): IPnutCacheRepository =
        PnutCacheRepository(accountRepository.getDefaultAccount()?.id, context, cacheDao)

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "gamma-db")
            .fallbackToDestructiveMigration(false)
            .build()

    @Provides
    fun provideCacheDao(database: AppDatabase): CacheDao = database.cacheDao()
}
