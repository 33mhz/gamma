package io.pnut.gamma.di

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.migration.DisableInstallInCheck
import io.pnut.gamma.domain.repository.AccountRepository
import io.pnut.gamma.domain.repository.IAccountRepository
import io.pnut.gamma.domain.repository.IPnutCacheRepository
import io.pnut.gamma.domain.repository.IPnutRepository
import io.pnut.gamma.domain.repository.IPreferenceRepository
import io.pnut.gamma.domain.repository.PnutCacheRepository
import io.pnut.gamma.domain.repository.PnutRepository
import io.pnut.gamma.domain.repository.PreferenceRepository
import org.mockito.Mockito
import javax.inject.Singleton

@DisableInstallInCheck
@Module(
  subcomponents = [
//    FakeUseCaseComponent::class
  ]
)
class FakeAppModule(private val application: Application) {
  private val accountRepository = AccountRepository(application)
  private val pnutRepository =
    PnutRepository(application, accountRepository.getDefaultAccount()?.token)
  private val preferenceRepository = PreferenceRepository(application)

  @Provides
  fun provideContext(): Context = application

  @Provides
  @Singleton
  fun providePreferenceRepository(): IPreferenceRepository = preferenceRepository

  @Provides
  @Singleton
  fun providePnutRepository(): IPnutRepository = pnutRepository

  @Provides
  fun providePnutCacheRepository(): IPnutCacheRepository =
    PnutCacheRepository(accountRepository.getDefaultAccount()?.id, application)

  @Provides
  @Singleton
  fun provideAccountRepository(): IAccountRepository = accountRepository

  @Provides
  fun provideHiltWorkerFactory(): HiltWorkerFactory = Mockito.mock(HiltWorkerFactory::class.java)
}