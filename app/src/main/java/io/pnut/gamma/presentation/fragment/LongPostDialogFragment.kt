package io.pnut.gamma.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BundleCompat
import io.pnut.gamma.domain.entity.raw.LongPost
import io.pnut.gamma.R
import io.pnut.gamma.databinding.FragmentLongPostDialogBinding

class LongPostDialogFragment : BaseBottomSheetDialogFragment() {
    private val longPost: LongPost by lazy {
        arguments?.let { BundleCompat.getParcelable(it, BundleKey.LongPost.name, LongPost::class.java) }
            ?: throw NullPointerException("You must set LongPost")
    }

    private var _binding: FragmentLongPostDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLongPostDialogBinding.inflate(inflater, container, false)
        val title = longPost.title
        binding.longPostViewToolbar.setNavigationOnClickListener { dismiss() }
        binding.longPostViewToolbar.title =
            if (title?.isNotEmpty() == true) title else getString(R.string.long_post_no_title)
        binding.longPostBodyTextView.text = longPost.body
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private enum class BundleKey { LongPost }

    companion object {
        fun newInstance(longPost: LongPost) = LongPostDialogFragment().apply {
            arguments = Bundle().also {
                it.putParcelable(BundleKey.LongPost.name, longPost)
            }
        }
    }
}
