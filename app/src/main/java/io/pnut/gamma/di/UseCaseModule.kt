package io.pnut.gamma.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.pnut.gamma.domain.repository.IAccountRepository
import io.pnut.gamma.domain.repository.IPnutCacheRepository
import io.pnut.gamma.domain.repository.IPnutRepository
import io.pnut.gamma.domain.repository.IPreferenceRepository
import io.pnut.gamma.domain.usecases.CacheInteractionUseCase
import io.pnut.gamma.domain.usecases.CachePostUseCase
import io.pnut.gamma.domain.usecases.CacheUserUseCase
import io.pnut.gamma.domain.usecases.CreateMessageUseCase
import io.pnut.gamma.domain.usecases.CreatePmMessageUseCase
import io.pnut.gamma.domain.usecases.GetExistingPmUseCase
import io.pnut.gamma.domain.usecases.SubscribeChannelUseCase
import io.pnut.gamma.domain.usecases.MuteChannelUseCase
import io.pnut.gamma.domain.usecases.CreatePollUseCase
import io.pnut.gamma.domain.usecases.DeleteMessageUseCase
import io.pnut.gamma.domain.usecases.DeletePostUseCase
import io.pnut.gamma.domain.usecases.GetAccountListUseCase
import io.pnut.gamma.domain.usecases.GetAuthenticatedUserUseCase
import io.pnut.gamma.domain.usecases.GetCachedInteractionListUseCase
import io.pnut.gamma.domain.usecases.GetCachedPostListUseCase
import io.pnut.gamma.domain.usecases.GetCachedUserListUseCase
import io.pnut.gamma.domain.usecases.GetChannelUseCase
import io.pnut.gamma.domain.usecases.GetChannelsUseCase
import io.pnut.gamma.domain.usecases.GetMessagesUseCase
import io.pnut.gamma.domain.usecases.GetMessageThreadUseCase
import io.pnut.gamma.domain.usecases.GetCurrentAccountUseCase
import io.pnut.gamma.domain.usecases.GetFilesUseCase
import io.pnut.gamma.domain.usecases.GetInteractionUseCase
import io.pnut.gamma.domain.usecases.GetPollUseCase
import io.pnut.gamma.domain.usecases.GetPostUseCase
import io.pnut.gamma.domain.usecases.GetProfileUseCase
import io.pnut.gamma.domain.usecases.GetUsersUseCase
import io.pnut.gamma.domain.usecases.LogoutUseCase
import io.pnut.gamma.domain.usecases.PostUseCase
import io.pnut.gamma.domain.usecases.ReportPostUseCase
import io.pnut.gamma.domain.usecases.RepostUseCase
import io.pnut.gamma.domain.usecases.SetupTokenUseCase
import io.pnut.gamma.domain.usecases.StarUseCase
import io.pnut.gamma.domain.usecases.UpdateDefaultAccountUseCase
import io.pnut.gamma.domain.usecases.UpdateProfileUseCase
import io.pnut.gamma.domain.usecases.UpdateRelationshipUseCase
import io.pnut.gamma.domain.usecases.UpdateUserImageUseCase
import io.pnut.gamma.domain.usecases.UploadFileUseCase
import io.pnut.gamma.domain.usecases.UpdateMarkerUseCase
import io.pnut.gamma.domain.usecases.VerifyTokenUseCase
import io.pnut.gamma.domain.usecases.VoteUseCase

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Provides
    fun provideTokenUseCase(
        accountRepository: IAccountRepository,
        pnutRepository: IPnutRepository
    ): VerifyTokenUseCase = VerifyTokenUseCase(accountRepository, pnutRepository)

    @Provides
    fun provideGetPostUseCase(
        pnutRepository: IPnutRepository,
        preferenceRepository: IPreferenceRepository
    ): GetPostUseCase =
        GetPostUseCase(pnutRepository, preferenceRepository)

    @Provides
    fun provideGetInteractionUseCase(pnutRepository: IPnutRepository): GetInteractionUseCase =
        GetInteractionUseCase(pnutRepository)

    @Provides
    fun provideSetUpTokenUseCase(
        pnutRepository: IPnutRepository,
        accountRepository: IAccountRepository
    ): SetupTokenUseCase =
        SetupTokenUseCase(pnutRepository, accountRepository)

    @Provides
    fun provideGetAuthenticatedUserUseCase(
        pnutRepository: IPnutRepository,
        pnutCacheRepository: IPnutCacheRepository
    ): GetAuthenticatedUserUseCase =
        GetAuthenticatedUserUseCase(pnutRepository, pnutCacheRepository)

    @Provides
    fun provideGetFilesUseCase(
        pnutRepository: IPnutRepository
    ): GetFilesUseCase = GetFilesUseCase(pnutRepository)

    @Provides
    fun provideGetProfileUseCase(
        pnutRepository: IPnutRepository
    ): GetProfileUseCase = GetProfileUseCase(pnutRepository)

    @Provides
    fun provideGetUsersUseCase(
        pnutRepository: IPnutRepository
    ): GetUsersUseCase = GetUsersUseCase(pnutRepository)

    @Provides
    fun providePostUseCase(
        pnutRepository: IPnutRepository,
        accountRepository: IAccountRepository
    ): PostUseCase = PostUseCase(pnutRepository, accountRepository)

    @Provides
    fun provideStarUseCase(
        pnutRepository: IPnutRepository
    ): StarUseCase = StarUseCase(pnutRepository)

    @Provides
    fun provideRepostUseCase(
        pnutRepository: IPnutRepository
    ): RepostUseCase = RepostUseCase(pnutRepository)

    @Provides
    fun provideGetCurrentAccountUseCase(
        accountRepository: IAccountRepository
    ): GetCurrentAccountUseCase = GetCurrentAccountUseCase(accountRepository)

    @Provides
    fun provideUpdateProfileUseCase(
        pnutRepository: IPnutRepository
    ): UpdateProfileUseCase = UpdateProfileUseCase(pnutRepository)

    @Provides
    fun provideFollowUseCase(
        pnutRepository: IPnutRepository
    ): UpdateRelationshipUseCase = UpdateRelationshipUseCase(pnutRepository)

    @Provides
    fun provideGetAccountListUseCase(
        accountRepository: IAccountRepository
    ): GetAccountListUseCase = GetAccountListUseCase(accountRepository)

    @Provides
    fun provideUpdateDefaultAccountUseCase(
        accountRepository: IAccountRepository,
        pnutRepository: IPnutRepository
    ): UpdateDefaultAccountUseCase = UpdateDefaultAccountUseCase(accountRepository, pnutRepository)

    @Provides
    fun provideLogoutUseCase(
        accountRepository: IAccountRepository,
        pnutRepository: IPnutRepository
    ): LogoutUseCase = LogoutUseCase(accountRepository, pnutRepository)

    @Provides
    fun provideUploadFileUseCase(
        pnutRepository: IPnutRepository
    ): UploadFileUseCase = UploadFileUseCase(pnutRepository)

    @Provides
    fun provideDeletePostUseCase(
        pnutRepository: IPnutRepository
    ): DeletePostUseCase = DeletePostUseCase(pnutRepository)

    @Provides
    fun provideReportPostUseCase(
        pnutRepository: IPnutRepository,
        accountRepository: IAccountRepository
    ): ReportPostUseCase = ReportPostUseCase(pnutRepository, accountRepository)

    @Provides
    fun provideUpdateUserImageUseCase(
        pnutRepository: IPnutRepository
    ): UpdateUserImageUseCase = UpdateUserImageUseCase(pnutRepository)

    @Provides
    fun provideGetCachedPostListUseCase(
        pnutCacheRepository: IPnutCacheRepository
    ): GetCachedPostListUseCase = GetCachedPostListUseCase(pnutCacheRepository)

    @Provides
    fun provideGetCachedUserListUseCase(
        pnutCacheRepository: IPnutCacheRepository
    ): GetCachedUserListUseCase = GetCachedUserListUseCase(pnutCacheRepository)

    @Provides
    fun provideGetCachedInteractionListUseCase(
        pnutCacheRepository: IPnutCacheRepository
    ): GetCachedInteractionListUseCase = GetCachedInteractionListUseCase(pnutCacheRepository)

    @Provides
    fun provideCachePostUseCase(
        pnutCacheRepository: IPnutCacheRepository,
        preferenceRepository: IPreferenceRepository
    ): CachePostUseCase = CachePostUseCase(pnutCacheRepository, preferenceRepository)

    @Provides
    fun provideCacheUserUseCase(
        pnutCacheRepository: IPnutCacheRepository,
        preferenceRepository: IPreferenceRepository
    ): CacheUserUseCase = CacheUserUseCase(pnutCacheRepository, preferenceRepository)

    @Provides
    fun provideCacheInteractionUseCase(
        pnutCacheRepository: IPnutCacheRepository,
        preferenceRepository: IPreferenceRepository
    ): CacheInteractionUseCase = CacheInteractionUseCase(pnutCacheRepository, preferenceRepository)

    @Provides
    fun provideCreatePollUseCase(
        pnutRepository: IPnutRepository
    ): CreatePollUseCase = CreatePollUseCase(pnutRepository)

    @Provides
    fun provideGetPollUseCase(
        pnutRepository: IPnutRepository
    ): GetPollUseCase = GetPollUseCase(pnutRepository)

    @Provides
    fun provideVoteUseCase(
        pnutRepository: IPnutRepository
    ): VoteUseCase = VoteUseCase(pnutRepository)

    @Provides
    fun provideGetChannelsUseCase(
        pnutRepository: IPnutRepository
    ): GetChannelsUseCase = GetChannelsUseCase(pnutRepository)

    @Provides
    fun provideGetChannelUseCase(
        pnutRepository: IPnutRepository
    ): GetChannelUseCase = GetChannelUseCase(pnutRepository)

    @Provides
    fun provideGetMessagesUseCase(
        pnutRepository: IPnutRepository
    ): GetMessagesUseCase = GetMessagesUseCase(pnutRepository)

    @Provides
    fun provideUpdateMarkerUseCase(
        pnutRepository: IPnutRepository
    ): UpdateMarkerUseCase = UpdateMarkerUseCase(pnutRepository)

    @Provides
    fun provideCreateMessageUseCase(
        pnutRepository: IPnutRepository
    ): CreateMessageUseCase = CreateMessageUseCase(pnutRepository)

    @Provides
    fun provideDeleteMessageUseCase(
        pnutRepository: IPnutRepository
    ): DeleteMessageUseCase = DeleteMessageUseCase(pnutRepository)

    @Provides
    fun provideGetMessageThreadUseCase(
        pnutRepository: IPnutRepository
    ): GetMessageThreadUseCase = GetMessageThreadUseCase(pnutRepository)

    @Provides
    fun provideCreatePmMessageUseCase(
        pnutRepository: IPnutRepository
    ): CreatePmMessageUseCase = CreatePmMessageUseCase(pnutRepository)

    @Provides
    fun provideGetExistingPmUseCase(
        pnutRepository: IPnutRepository
    ): GetExistingPmUseCase = GetExistingPmUseCase(pnutRepository)

    @Provides
    fun provideSubscribeChannelUseCase(
        pnutRepository: IPnutRepository
    ): SubscribeChannelUseCase = SubscribeChannelUseCase(pnutRepository)

    @Provides
    fun provideMuteChannelUseCase(
        pnutRepository: IPnutRepository
    ): MuteChannelUseCase = MuteChannelUseCase(pnutRepository)
}