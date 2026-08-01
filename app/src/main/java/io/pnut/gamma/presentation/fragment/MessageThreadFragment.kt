package io.pnut.gamma.presentation.fragment

import android.os.Bundle
import android.view.View
import androidx.core.os.BundleCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import io.pnut.gamma.R
import io.pnut.gamma.databinding.ListWithToolbarBinding
import io.pnut.gamma.domain.entity.Message
import io.pnut.gamma.domain.entity.PnutResponse
import io.pnut.gamma.domain.model.PageableItemWrapper
import io.pnut.gamma.domain.model.io.GetMessageThreadInputData
import io.pnut.gamma.domain.usecases.GetMessageThreadUseCase
import javax.inject.Inject

@AndroidEntryPoint
class MessageThreadFragment : ChannelMessagesFragment() {

    private val message by lazy {
        arguments?.let { BundleCompat.getParcelable(it, BundleKey.Message.name, Message::class.java) } ?: throw NullPointerException("Must set Message")
    }

    @Inject
    lateinit var getMessageThreadUseCase: GetMessageThreadUseCase

    override val reverse = true

    override val viewModel: BaseListViewModel<Message> by lazy {
        ViewModelProvider(
            this, MessageThreadViewModel.Factory(channelId, message.id, getMessageThreadUseCase)
        )[MessageThreadViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = ListWithToolbarBinding.bind(view)
        binding.toolbar.setTitle(R.string.thread)
    }

    class MessageThreadViewModel(
        private val channelId: String,
        private val messageId: String,
        private val getMessageThreadUseCase: GetMessageThreadUseCase
    ) : BaseListViewModel<Message>() {
        override suspend fun getItems(requestPager: PageableItemWrapper.Pager<Message>?): PnutResponse<List<Message>> {
            // For threads, we usually get all items at once or minimal pagination
            return getMessageThreadUseCase.run(GetMessageThreadInputData(channelId, messageId)).res
        }

        class Factory(
            private val channelId: String,
            private val messageId: String,
            private val getMessageThreadUseCase: GetMessageThreadUseCase
        ) : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MessageThreadViewModel(channelId, messageId, getMessageThreadUseCase) as T
            }
        }
    }

    private enum class BundleKey { Message }

    companion object {
        fun newInstance(channelId: String, message: Message) = MessageThreadFragment().apply {
            arguments = Bundle().apply {
                putString("ChannelId", channelId)
                putParcelable(BundleKey.Message.name, message)
            }
        }
    }
}
