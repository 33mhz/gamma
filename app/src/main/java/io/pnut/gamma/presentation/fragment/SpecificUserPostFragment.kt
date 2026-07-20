package io.pnut.gamma.presentation.fragment

import android.os.Bundle
import io.pnut.gamma.domain.model.StreamType
import dagger.hilt.android.AndroidEntryPoint

abstract class SpecificUserPostFragment: PostItemFragment() {
    override val streamType: StreamType by lazy {
        val userId = arguments?.getString(BundleKey.UserId.name, "me") ?: "me"
        setStreamTypeWithUserId(userId)
    }
    private enum class BundleKey { UserId }

    abstract fun setStreamTypeWithUserId(userId: String): StreamType

    @AndroidEntryPoint
    class UserPostFragment : SpecificUserPostFragment() {
        override fun setStreamTypeWithUserId(userId: String): StreamType = StreamType.User(userId)
        companion object {
            fun newInstance(userId: String) = UserPostFragment().apply {
                arguments = Bundle().apply {
                    putString(BundleKey.UserId.name, userId)
                }
            }
        }
    }

    @AndroidEntryPoint
    class StarsPostFragment: SpecificUserPostFragment() {
        override fun setStreamTypeWithUserId(userId: String): StreamType =  StreamType.Stars(userId)
        companion object {
            fun newInstance(userId: String) = StarsPostFragment().apply {
                arguments = Bundle().apply {
                    putString(BundleKey.UserId.name, userId)
                }
            }
        }
    }
}