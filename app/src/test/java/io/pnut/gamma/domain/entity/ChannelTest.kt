package io.pnut.gamma.domain.entity

import com.google.common.truth.Truth.assertThat
import io.pnut.gamma.domain.model.ChannelType
import org.junit.Test
import java.util.Date

class ChannelTest {

    private fun createChannel(
        type: String,
        writeUserIds: List<String> = emptyList(),
        fullUserIds: List<String> = emptyList(),
        readUserIds: List<String> = emptyList()
    ): Channel {
        val full = Channel.Acl.Full(immutable = false, you = true, userIds = fullUserIds)
        val write = Channel.Acl.Write(anyUser = false, immutable = false, userIds = writeUserIds, you = true)
        val read = Channel.Acl.Read(anyUser = false, immutable = false, public = false, userIds = readUserIds, you = true)
        val acl = Channel.Acl(full = full, write = write, read = read)
        return Channel(
            acl = acl,
            counts = Channel.ChannelCount(messages = 0),
            createdAt = Date(),
            hasStickyMessages = false,
            hasUnread = false,
            id = "100",
            type = type,
            youMuted = false,
            youSubscribed = true
        )
    }

    @Test
    fun testIsTwoPersonPmForTwoUserPmChannel() {
        val channel = createChannel(
            type = ChannelType.PM.value,
            writeUserIds = listOf("user1", "user2")
        )
        assertThat(channel.isTwoPersonPm).isTrue()
    }

    @Test
    fun testIsTwoPersonPmForSelfPmChannel() {
        val channel = createChannel(
            type = ChannelType.PM.value,
            writeUserIds = listOf("user1")
        )
        assertThat(channel.isTwoPersonPm).isTrue()
    }

    @Test
    fun testIsTwoPersonPmForGroupPmChannel() {
        val channel = createChannel(
            type = ChannelType.PM.value,
            writeUserIds = listOf("user1", "user2", "user3")
        )
        assertThat(channel.isTwoPersonPm).isFalse()
    }

    @Test
    fun testIsTwoPersonPmForChatChannel() {
        val channel = createChannel(
            type = ChannelType.Chat.value,
            writeUserIds = listOf("user1", "user2")
        )
        assertThat(channel.isTwoPersonPm).isFalse()
    }
}
