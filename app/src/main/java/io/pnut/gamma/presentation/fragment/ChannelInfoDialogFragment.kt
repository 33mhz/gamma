package io.pnut.gamma.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import android.text.format.DateFormat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import io.pnut.gamma.R
import io.pnut.gamma.databinding.FragmentChannelInfoBinding
import io.pnut.gamma.domain.entity.Channel
import io.pnut.gamma.domain.entity.raw.ChatSettings
import io.pnut.gamma.domain.usecases.GetChannelUseCase
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class ChannelInfoDialogFragment : BottomSheetDialogFragment() {

    @Inject
    lateinit var getChannelUseCase: GetChannelUseCase

    private var _binding: FragmentChannelInfoBinding? = null
    private val binding get() = _binding!!

    private val channelId by lazy { arguments?.getString(BUNDLE_KEY_CHANNEL_ID) ?: "" }
    private val isPm by lazy { arguments?.getBoolean(BUNDLE_KEY_IS_PM) ?: false }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChannelInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchChannelInfo()
    }

    private fun fetchChannelInfo() {
        lifecycleScope.launch {
            try {
                val response = getChannelUseCase.run(channelId)
                updateUi(response.data)
            } catch (_: Exception) {
            }
        }
    }

    private fun updateUi(channel: Channel) {
        val chatSettings = ChatSettings.getChatSettings(channel.raw)
        binding.titleTextView.text = getString(R.string.room, channel.id)

        if (!isPm && chatSettings?.categories != null && chatSettings.categories.isNotEmpty()) {
            binding.categoriesChipGroup.isVisible = true
            binding.categoriesChipGroup.removeAllViews()
            chatSettings.categories.forEach { category ->
                val chip = Chip(requireContext())
                chip.text = category.value.replaceFirstChar { it.uppercase() }
                binding.categoriesChipGroup.addView(chip)
            }
        }

        binding.privacyContainer.isVisible = !isPm
        if (!isPm) {
            binding.privacyIcon.text = getString(if (channel.acl.read.public) R.string.public_room else R.string.private_room)
            binding.privacyIcon.setCompoundDrawablesWithIntrinsicBounds(
                if (channel.acl.read.public) R.drawable.ic_public_black_24dp else R.drawable.ic_person_black_24dp,
                0, 0, 0
            )
        }

        binding.subscribersTextView.text = channel.counts.subscribers?.toString() ?: "0"
        binding.messagesTextView.text = String.format(Locale.US, "%,d", channel.counts.messages)
        binding.createdTextView.text = DateFormat.format(getString(R.string.date_format_yyyy_mm_dd), channel.createdAt)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val BUNDLE_KEY_CHANNEL_ID = "channel_id"
        private const val BUNDLE_KEY_IS_PM = "is_pm"

        fun newInstance(channelId: String, isPm: Boolean) = ChannelInfoDialogFragment().apply {
            arguments = Bundle().apply {
                putString(BUNDLE_KEY_CHANNEL_ID, channelId)
                putBoolean(BUNDLE_KEY_IS_PM, isPm)
            }
        }
    }
}
