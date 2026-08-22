package io.pnut.gamma.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import io.pnut.gamma.R
import io.pnut.gamma.databinding.ReactionUserItemBinding
import io.pnut.gamma.domain.entity.User
import io.pnut.gamma.presentation.util.BindingUtil

class ReactionUsersAdapter(
    private val reactionUsers: List<User>,
    val listener: Listener,
//    private val shapeOfAvatar: ShapeOfAvatar
) :
    RecyclerView.Adapter<ReactionUsersAdapter.ViewHolder>() {
    interface Listener {
        fun onUserClick(user: User)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.reaction_user_item, parent, false)
        return ViewHolder(view)
//            .also {
            //            it.avatarView.setShape(shapeOfAvatar)
//        }
    }

    override fun getItemCount(): Int = reactionUsers.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = reactionUsers[position]
        BindingUtil.loadAvatar(holder.avatarView, user, User.AvatarSize.Normal)
        holder.avatarView.setOnClickListener {
            listener.onUserClick(user)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ReactionUserItemBinding.bind(itemView)
        val avatarView: ImageView = binding.reactionUserIconImageView
    }

}
