package io.pnut.gamma.presentation.viewmodel

import com.google.common.truth.Truth.assertThat
import io.pnut.gamma.domain.entity.Channel
import io.pnut.gamma.domain.entity.Client
import io.pnut.gamma.domain.entity.Message
import io.pnut.gamma.domain.model.ChannelType
import io.pnut.gamma.domain.model.PageableItemWrapper
import io.pnut.gamma.domain.usecases.GetChannelsUseCase
import io.pnut.gamma.mock.AccountRepositoryMock
import io.pnut.gamma.presentation.fragment.ChannelListFragment.ChannelListViewModel
import io.pnut.gamma.sample.Users
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import java.util.Date

@RunWith(RobolectricTestRunner::class)
class ChannelListViewModelTest {

    private fun createChannel(id: String, hasUnread: Boolean, recentMessage: Message? = null): Channel {
        val user = Users.me.copy(id = "u1", username = "testuser", name = "Test User")
        val acl = Channel.Acl(
            full = Channel.Acl.Full(false, true, userIds = listOf("u1")),
            write = Channel.Acl.Write(true, false, userIds = listOf("u1"), you = true),
            read = Channel.Acl.Read(true, false, true, userIds = listOf("u1"), you = true)
        )
        return Channel(
            acl = acl,
            counts = Channel.ChannelCount(messages = 1),
            createdAt = Date(),
            hasStickyMessages = false,
            hasUnread = hasUnread,
            id = id,
            recentMessage = recentMessage,
            recentMessageId = recentMessage?.id,
            type = "io.pnut.core.chat",
            youMuted = false,
            youSubscribed = true,
            user = user
        )
    }

    private fun createMessage(id: String, channelId: String, userId: String): Message {
        val user = Users.others.copy(id = userId, username = "user_$userId", name = "User $userId")
        return Message(
            counts = Message.MessageCount(0),
            createdAt = Date(),
            id = id,
            isDeleted = false,
            isSticky = false,
            source = Client("client_id", "TestApp", "https://example.com"),
            channelId = channelId,
            threadId = id,
            user = user,
            content = null
        )
    }

    @Test
    fun testMarkChannelAsRead() {
        val getChannelsUseCase = mock(GetChannelsUseCase::class.java)
        val accountRepo = object : AccountRepositoryMock() {
            override fun getStoredIds(): List<String> = listOf("my_user_id")
        }

        val viewModel = ChannelListViewModel(
            ChannelType.Chat,
            null,
            null,
            getChannelsUseCase,
            accountRepo
        )

        val channel = createChannel("c1", hasUnread = true)
        viewModel.items.add(PageableItemWrapper.Item(channel))

        viewModel.markChannelAsRead("c1")

        val updatedChannel = (viewModel.items.first() as PageableItemWrapper.Item).item
        assertThat(updatedChannel.hasUnread).isFalse()
    }

    @Test
    fun testUpdateRecentMessageFromOtherUser() {
        val getChannelsUseCase = mock(GetChannelsUseCase::class.java)
        val accountRepo = object : AccountRepositoryMock() {
            override fun getStoredIds(): List<String> = listOf("my_user_id")
        }

        val viewModel = ChannelListViewModel(
            ChannelType.Chat,
            null,
            null,
            getChannelsUseCase,
            accountRepo
        )

        val channel = createChannel("c1", hasUnread = false)
        viewModel.items.add(PageableItemWrapper.Item(channel))

        val newMessage = createMessage("m100", "c1", "other_user_id")
        viewModel.updateRecentMessage(newMessage)

        val updatedChannel = (viewModel.items.first() as PageableItemWrapper.Item).item
        assertThat(updatedChannel.recentMessageId).isEqualTo("m100")
        assertThat(updatedChannel.recentMessage).isEqualTo(newMessage)
        assertThat(updatedChannel.hasUnread).isTrue()
    }

    @Test
    fun testUpdateRecentMessageFromSelf() {
        val getChannelsUseCase = mock(GetChannelsUseCase::class.java)
        val accountRepo = object : AccountRepositoryMock() {
            override fun getStoredIds(): List<String> = listOf("my_user_id")
        }

        val viewModel = ChannelListViewModel(
            ChannelType.Chat,
            null,
            null,
            getChannelsUseCase,
            accountRepo
        )

        val channel = createChannel("c1", hasUnread = true)
        viewModel.items.add(PageableItemWrapper.Item(channel))

        val newMessage = createMessage("m101", "c1", "my_user_id")
        viewModel.updateRecentMessage(newMessage)

        val updatedChannel = (viewModel.items.first() as PageableItemWrapper.Item).item
        assertThat(updatedChannel.recentMessageId).isEqualTo("m101")
        assertThat(updatedChannel.recentMessage).isEqualTo(newMessage)
        assertThat(updatedChannel.hasUnread).isFalse()
    }
}
