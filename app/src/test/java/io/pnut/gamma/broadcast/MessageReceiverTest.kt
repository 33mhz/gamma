package io.pnut.gamma.broadcast

import android.content.Context
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.common.truth.Truth.assertThat
import io.pnut.gamma.domain.entity.Client
import io.pnut.gamma.domain.entity.Message
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.shadows.ShadowLooper
import java.util.Date

@RunWith(RobolectricTestRunner::class)
class MessageReceiverTest {

    @Test
    fun testBroadcastAndReceiveMessage() {
        val context: Context = RuntimeEnvironment.getApplication()
        var receivedMessage: Message? = null

        val receiver = MessageReceiver(object : MessageReceiver.Callback {
            override fun onMessageReceive(message: Message) {
                receivedMessage = message
            }
        })

        LocalBroadcastManager.getInstance(context)
            .registerReceiver(receiver, MessageReceiver.getIntentFilter())

        val testMessage = Message(
            counts = Message.MessageCount(0),
            createdAt = Date(),
            id = "m123",
            isDeleted = false,
            isSticky = false,
            source = Client("client_id", "TestApp", "https://example.com"),
            channelId = "c456",
            threadId = "m123",
            user = null,
            content = null
        )

        MessageReceiver.broadcast(context, testMessage)
        ShadowLooper.idleMainLooper()

        assertThat(receivedMessage).isNotNull()
        assertThat(receivedMessage?.id).isEqualTo("m123")
        assertThat(receivedMessage?.channelId).isEqualTo("c456")

        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
    }
}
