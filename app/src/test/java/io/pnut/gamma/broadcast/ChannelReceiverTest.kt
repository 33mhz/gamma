package io.pnut.gamma.broadcast

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
class ChannelReceiverTest {

    @Test
    fun testBroadcastAndReceiveChannelRead() {
        var readChannelId: String? = null

        val receiver = ChannelReceiver(object : ChannelReceiver.Callback {
            override fun onChannelRead(channelId: String) {
                readChannelId = channelId
            }
        })

        val scope = CoroutineScope(Dispatchers.Main)
        receiver.register(scope)
        ShadowLooper.idleMainLooper()

        ChannelReceiver.broadcastRead("channel_123")
        ShadowLooper.idleMainLooper()

        assertThat(readChannelId).isEqualTo("channel_123")

        receiver.unregister()
    }
}
