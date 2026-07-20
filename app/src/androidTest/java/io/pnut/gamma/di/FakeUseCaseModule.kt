package io.pnut.gamma.di

import dagger.Module
import dagger.Provides
import io.pnut.gamma.domain.usecases.CacheInteractionUseCase
import io.pnut.gamma.domain.usecases.CachePostUseCase
import io.pnut.gamma.domain.usecases.CacheUserUseCase
import io.pnut.gamma.domain.usecases.CreatePollUseCase
import io.pnut.gamma.domain.usecases.DeletePostUseCase
import io.pnut.gamma.domain.usecases.GetAccountListUseCase
import io.pnut.gamma.domain.usecases.GetAuthenticatedUserUseCase
import io.pnut.gamma.domain.usecases.GetCachedInteractionListUseCase
import io.pnut.gamma.domain.usecases.GetCachedPostListUseCase
import io.pnut.gamma.domain.usecases.GetCachedUserListUseCase
import io.pnut.gamma.domain.usecases.GetCurrentAccountUseCase
import io.pnut.gamma.domain.usecases.GetFilesUseCase
import io.pnut.gamma.domain.usecases.GetInteractionUseCase
import io.pnut.gamma.domain.usecases.GetPollUseCase
import io.pnut.gamma.domain.usecases.GetPostUseCase
import io.pnut.gamma.domain.usecases.GetProfileUseCase
import io.pnut.gamma.domain.usecases.GetUsersUseCase
import io.pnut.gamma.domain.usecases.LogoutUseCase
import io.pnut.gamma.domain.usecases.PostUseCase
import io.pnut.gamma.domain.usecases.RepostUseCase
import io.pnut.gamma.domain.usecases.SetupTokenUseCase
import io.pnut.gamma.domain.usecases.StarUseCase
import io.pnut.gamma.domain.usecases.UpdateDefaultAccountUseCase
import io.pnut.gamma.domain.usecases.UpdateProfileUseCase
import io.pnut.gamma.domain.usecases.UpdateRelationshipUseCase
import io.pnut.gamma.domain.usecases.UpdateUserImageUseCase
import io.pnut.gamma.domain.usecases.UploadFileUseCase
import io.pnut.gamma.domain.usecases.VerifyTokenUseCase
import io.pnut.gamma.domain.usecases.VoteUseCase
import org.mockito.Mockito

@Module
class FakeUseCaseModule {
  lateinit var setupTokenUseCase: SetupTokenUseCase
  lateinit var getAccountListUseCase: GetAccountListUseCase
  lateinit var verifyTokenUseCase: VerifyTokenUseCase
  lateinit var getCachedPostListUseCase: GetCachedPostListUseCase
  lateinit var getCurrentAccountUseCase: GetCurrentAccountUseCase

  @Provides
  fun provideSetupTokenUseCase() = setupTokenUseCase

  @Provides
  fun provideTokenUseCase(): VerifyTokenUseCase = verifyTokenUseCase

  @Provides
  fun provideGetPostUseCase(): GetPostUseCase =
    Mockito.mock(GetPostUseCase::class.java)

  @Provides
  fun provideGetInteractionUseCase(): GetInteractionUseCase =
    Mockito.mock(GetInteractionUseCase::class.java)

  @Provides
  fun provideGetAuthenticatedUserUseCase(): GetAuthenticatedUserUseCase =
    Mockito.mock(GetAuthenticatedUserUseCase::class.java)

  @Provides
  fun provideGetFilesUseCase(): GetFilesUseCase = Mockito.mock(GetFilesUseCase::class.java)

  @Provides
  fun provideGetProfileUseCase(): GetProfileUseCase = Mockito.mock(GetProfileUseCase::class.java)

  @Provides
  fun provideGetUsersUseCase(): GetUsersUseCase = Mockito.mock(GetUsersUseCase::class.java)

  @Provides
  fun providePostUseCase(): PostUseCase = Mockito.mock(PostUseCase::class.java)

  @Provides
  fun provideStarUseCase(): StarUseCase = Mockito.mock(StarUseCase::class.java)

  @Provides
  fun provideRepostUseCase(): RepostUseCase = Mockito.mock(RepostUseCase::class.java)

  @Provides
  fun provideGetCurrentAccountUseCase(): GetCurrentAccountUseCase = getCurrentAccountUseCase

  @Provides
  fun provideUpdateProfileUseCase(): UpdateProfileUseCase =
    Mockito.mock(UpdateProfileUseCase::class.java)

  @Provides
  fun provideFollowUseCase(): UpdateRelationshipUseCase =
    Mockito.mock(UpdateRelationshipUseCase::class.java)

  @Provides
  fun provideGetAccountListUseCase(): GetAccountListUseCase = getAccountListUseCase

  @Provides
  fun provideUpdateDefaultAccountUseCase(): UpdateDefaultAccountUseCase =
    Mockito.mock(UpdateDefaultAccountUseCase::class.java)

  @Provides
  fun provideLogoutUseCase(): LogoutUseCase = Mockito.mock(LogoutUseCase::class.java)

  @Provides
  fun provideUploadFileUseCase(): UploadFileUseCase = Mockito.mock(UploadFileUseCase::class.java)

  @Provides
  fun provideDeletePostUseCase(): DeletePostUseCase = Mockito.mock(DeletePostUseCase::class.java)

  @Provides
  fun provideUpdateUserImageUseCase(): UpdateUserImageUseCase =
    Mockito.mock(UpdateUserImageUseCase::class.java)

  @Provides
  fun provideGetCachedPostListUseCase(): GetCachedPostListUseCase = getCachedPostListUseCase

  @Provides
  fun provideGetCachedUserListUseCase(): GetCachedUserListUseCase =
    Mockito.mock(GetCachedUserListUseCase::class.java)

  @Provides
  fun provideGetCachedInteractionListUseCase(): GetCachedInteractionListUseCase =
    Mockito.mock(GetCachedInteractionListUseCase::class.java)

  @Provides
  fun provideCachePostUseCase(): CachePostUseCase =
    Mockito.mock(CachePostUseCase::class.java)

  @Provides
  fun provideCacheUserUseCase(): CacheUserUseCase =
    Mockito.mock(CacheUserUseCase::class.java)

  @Provides
  fun provideCacheInteractionUseCase(): CacheInteractionUseCase =
    Mockito.mock(CacheInteractionUseCase::class.java)

  @Provides
  fun provideCreatePollUseCase(): CreatePollUseCase = Mockito.mock(CreatePollUseCase::class.java)

  @Provides
  fun provideGetPollUseCase(): GetPollUseCase = Mockito.mock(GetPollUseCase::class.java)

  @Provides
  fun provideVoteUseCase(): VoteUseCase = Mockito.mock(VoteUseCase::class.java)
}