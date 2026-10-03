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
@Suppress("unused")
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
    fun provideUpdatePostUseCase(): UpdatePostUseCase = Mockito.mock(UpdatePostUseCase::class.java)

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
    fun provideSearchMessagesUseCase(): SearchMessagesUseCase = Mockito.mock(SearchMessagesUseCase::class.java)

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
    fun provideReportPostUseCase(): ReportPostUseCase = Mockito.mock(ReportPostUseCase::class.java)

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

    @Provides
    @Singleton
    fun provideGetChannelUseCase(): GetChannelUseCase = Mockito.mock(GetChannelUseCase::class.java)

    @Provides
    @Singleton
    fun provideGetMessagesUseCase(): GetMessagesUseCase = Mockito.mock(GetMessagesUseCase::class.java)

    @Provides
    @Singleton
    fun provideUpdateMarkerUseCase(): UpdateMarkerUseCase = Mockito.mock(UpdateMarkerUseCase::class.java)

    @Provides
    @Singleton
    fun provideCreateMessageUseCase(): CreateMessageUseCase = Mockito.mock(CreateMessageUseCase::class.java)

    @Provides
    @Singleton
    fun provideDeleteMessageUseCase(): DeleteMessageUseCase = Mockito.mock(DeleteMessageUseCase::class.java)

    @Provides
    @Singleton
    fun provideGetMessageThreadUseCase(): GetMessageThreadUseCase = Mockito.mock(GetMessageThreadUseCase::class.java)

    @Provides
    @Singleton
    fun provideCreatePmMessageUseCase(): CreatePmMessageUseCase = Mockito.mock(CreatePmMessageUseCase::class.java)

    @Provides
    @Singleton
    fun provideGetExistingPmUseCase(): GetExistingPmUseCase = Mockito.mock(GetExistingPmUseCase::class.java)

    @Provides
    @Singleton
    fun provideSubscribeChannelUseCase(): SubscribeChannelUseCase = Mockito.mock(SubscribeChannelUseCase::class.java)

    @Provides
    @Singleton
    fun provideMuteChannelUseCase(): MuteChannelUseCase = Mockito.mock(MuteChannelUseCase::class.java)
}
