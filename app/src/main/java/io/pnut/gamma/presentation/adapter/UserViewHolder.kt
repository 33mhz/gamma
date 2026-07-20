package io.pnut.gamma.presentation.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.button.MaterialButton
import io.pnut.gamma.R
import io.pnut.gamma.databinding.FragmentUserItemBinding
import io.pnut.gamma.domain.Relationship
import io.pnut.gamma.domain.entity.User
import io.pnut.gamma.presentation.util.ColorResource
import io.pnut.gamma.presentation.util.EntityOnTouchListener
import com.bumptech.glide.Glide
import io.pnut.gamma.presentation.util.Util


class UserViewHolder(mView: View) :
    RecyclerView.ViewHolder(mView) {
    private val binding = FragmentUserItemBinding.bind(mView)
    private val avatarView: ImageView = binding.avatarImageView
    private val screenNameTextView: TextView = binding.screenNameTextView
    private val handleNameTextView: TextView = binding.handleNameTextView
    private val bodyTextView: TextView = binding.bodyTextView
    private val relationshipTextView: TextView = binding.relationshipTextView
    private val actionButton: MaterialButton = binding.actionButton
    private val entityListener: View.OnTouchListener = EntityOnTouchListener()
    private val context = itemView.context

    interface Callback {
        fun onActionButtonClick(user: User)
    }

    fun bind(user: User, listener: Callback) {
        Glide.with(itemView.context)
            .load(user.content.avatarImage.url)
            .apply(RequestOptions.circleCropTransform())
            .into(avatarView)
        screenNameTextView.text = user.username
        handleNameTextView.text = user.name
        bodyTextView.apply {
            text = user.content.getSpannableStringBuilder(itemView.context)
            setOnTouchListener(entityListener)
        }
        relationshipTextView.visibility = Util.getVisibility(!user.me && user.followsYou)
        val relationshipText = user.relationshipTextRes?.let { context.getString(it) }
        actionButton.text = relationshipText
        actionButton.setOnClickListener {
            listener.onActionButtonClick(user)
        }
        applyStyle(user)
    }

    private fun applyStyle(user: User) {
        val relationship = Relationship.getRelationship(user)
        val style = actionButtonStyleMap[relationship] ?: return
        style.outlineColorRes.getColor(context)?.let {
            actionButton.strokeColor = it

        }
        style.bgColorRes.getColor(context)?.let {
            actionButton.backgroundTintList = it
        }
        style.textColorRes.getColor(context)?.let {
            actionButton.setTextColor(it)
        }
    }

    private val actionButtonStyleMap = mapOf(
        Relationship.Follow to ActionButtonStyle(
            ColorResource.Color(R.color.stroke_button),
            ColorResource.Color(R.color.bg_button_active),
            ColorResource.Color(R.color.text_button_active)
        ),
        Relationship.UnFollow to ActionButtonStyle(
            ColorResource.Color(R.color.stroke_button),
            ColorResource.Color(R.color.bg_button_inactive),
            ColorResource.Color(R.color.text_button_inactive)
        ),
        Relationship.Block to ActionButtonStyle(
            ColorResource.Color(R.color.colorError),
            ColorResource.Color(R.color.bg_button_inactive),
            ColorResource.Color(R.color.colorError)
        )
    )


    private data class ActionButtonStyle(
        val outlineColorRes: ColorResource,
        val bgColorRes: ColorResource,
        val textColorRes: ColorResource
    )

}
