package io.pnut.gamma.di

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.pnut.gamma.domain.usecases.*
import org.mockito.Mockito
import javax.inject.Singleton

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [UseCaseModule::class])
object TestUseCaseModule {

    @Provides
    @Singleton
    fun provideTokenUseCase(): VerifyTokenUseCase = Mockito.mock(VerifyTokenUseCase::class.java)

    @Provides
    @Singleton
    fun provideGetPostUseCase(): GetPostUseCase = Mockito.mock(GetPostUseCase::class.java)

    @Provides
    @Singleton
    fun provideGetInteractionUseCase(): GetInteractionUseCase = Mockito.mock(GetInteractionUseCase::class.java)

    @Provides
    @Singleton
    fun provideSetUpTokenUseCase(): SetupTokenUseCase = Mockito.mock(SetupTokenUseCase::class.java)

    @Provides
    @Singleton
    fun provideGetAuthenticatedUserUseCase(): GetAuthenticatedUserUseCase = Mockito.mock(GetAuthenticatedUserUseCase::class.java)

    @Provides
    @Singleton
    fun provideGetFilesUseCase(): GetFilesUseCase = Mockito.mock(GetFilesUseCase::class.java)

    @Provides
    @Singleton
    fun provideGetProfileUseCase(): GetProfileUseCase = Mockito.mock(GetProfileUseCase::class.java)

    @Provides
    @Singleton
    fun provideGetUsersUseCase(): GetUsersUseCase = Mockito.mock(GetUsersUseCase::class.java)

    @Provides
    @Singleton
    fun providePostUseCase(): PostUseCase = Mockito.mock(PostUseCase::class.java)

    @Provides
    @Singleton
    fun provideStarUseCase(): StarUseCase = Mockito.mock(StarUseCase::class.java)

    @Provides
    @Singleton
    fun provideRepostUseCase(): RepostUseCase = Mockito.mock(RepostUseCase::class.java)

    @Provides
    @Singleton
    fun provideGetCurrentAccountUseCase(): GetCurrentAccountUseCase = Mockito.mock(GetCurrentAccountUseCase::class.java)

    @Provides
    @Singleton
    fun provideUpdateProfileUseCase(): UpdateProfileUseCase = Mockito.mock(UpdateProfileUseCase::class.java)

    @Provides
    @Singleton
    fun provideFollowUseCase(): UpdateRelationshipUseCase = Mockito.mock(UpdateRelationshipUseCase::class.java)

    @Provides
    @Singleton
    fun provideGetAccountListUseCase(): GetAccountListUseCase = Mockito.mock(GetAccountListUseCase::class.java)

    @Provides
    @Singleton
    fun provideUpdateDefaultAccountUseCase(): UpdateDefaultAccountUseCase = Mockito.mock(UpdateDefaultAccountUseCase::class.java)

    @Provides
    @Singleton
    fun provideLogoutUseCase(): LogoutUseCase = Mockito.mock(LogoutUseCase::class.java)

    @Provides
    @Singleton
    fun provideUploadFileUseCase(): UploadFileUseCase = Mockito.mock(UploadFileUseCase::class.java)

    @Provides
    @Singleton
    fun provideDeletePostUseCase(): DeletePostUseCase = Mockito.mock(DeletePostUseCase::class.java)

    @Provides
    @Singleton
    fun provideUpdateUserImageUseCase(): UpdateUserImageUseCase = Mockito.mock(UpdateUserImageUseCase::class.java)

    @Provides
    @Singleton
    fun provideGetCachedPostListUseCase(): GetCachedPostListUseCase = Mockito.mock(GetCachedPostListUseCase::class.java)

    @Provides
    @Singleton
    fun provideGetCachedUserListUseCase(): GetCachedUserListUseCase = Mockito.mock(GetCachedUserListUseCase::class.java)

    @Provides
    @Singleton
    fun provideGetCachedInteractionListUseCase(): GetCachedInteractionListUseCase = Mockito.mock(GetCachedInteractionListUseCase::class.java)

    @Provides
    @Singleton
    fun provideCachePostUseCase(): CachePostUseCase = Mockito.mock(CachePostUseCase::class.java)

    @Provides
    @Singleton
    fun provideCacheUserUseCase(): CacheUserUseCase = Mockito.mock(CacheUserUseCase::class.java)

    @Provides
    @Singleton
    fun provideCacheInteractionUseCase(): CacheInteractionUseCase = Mockito.mock(CacheInteractionUseCase::class.java)

    @Provides
    @Singleton
    fun provideCreatePollUseCase(): CreatePollUseCase = Mockito.mock(CreatePollUseCase::class.java)

    @Provides
    @Singleton
    fun provideGetPollUseCase(): GetPollUseCase = Mockito.mock(GetPollUseCase::class.java)

    @Provides
    @Singleton
    fun provideVoteUseCase(): VoteUseCase = Mockito.mock(VoteUseCase::class.java)

    @Provides
    @Singleton
    fun provideGetChannelsUseCase(): GetChannelsUseCase = Mockito.mock(GetChannelsUseCase::class.java)
}
