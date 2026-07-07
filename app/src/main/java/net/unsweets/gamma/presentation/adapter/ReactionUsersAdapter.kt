package net.unsweets.gamma.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import net.unsweets.gamma.R
import net.unsweets.gamma.databinding.ReactionUserItemBinding
import net.unsweets.gamma.domain.entity.User
import net.unsweets.gamma.domain.model.preference.ShapeOfAvatar
import com.bumptech.glide.Glide

class ReactionUsersAdapter(
    private val reactionUsers: List<User>,
    val listener: Listener,
    private val shapeOfAvatar: ShapeOfAvatar
) :
    RecyclerView.Adapter<ReactionUsersAdapter.ViewHolder>() {
    interface Listener {
        fun onUserClick(user: User)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.reaction_user_item, parent, false)
        return ViewHolder(view).also {
            //            it.avatarView.setShape(shapeOfAvatar)
        }
    }

    override fun getItemCount(): Int = reactionUsers.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = reactionUsers[position]
        Glide.with(holder.itemView).load(user.getAvatarUrl(User.AvatarSize.Normal))
            .apply(RequestOptions.circleCropTransform())
            .into(holder.avatarView)
        holder.avatarView.setOnClickListener {
            listener.onUserClick(user)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ReactionUserItemBinding.bind(itemView)
        val avatarView: ImageView = binding.reactionUserIconImageView
    }

}
