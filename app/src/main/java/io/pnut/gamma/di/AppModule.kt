package io.pnut.gamma.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
    fun providePnutCacheRepository(@ApplicationContext context: Context, accountRepository: IAccountRepository): IPnutCacheRepository =
        PnutCacheRepository(accountRepository.getDefaultAccount()?.id, context)
}
