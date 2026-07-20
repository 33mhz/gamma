package net.unsweets.gamma.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import net.unsweets.gamma.R
import net.unsweets.gamma.databinding.ThumbnailItemBinding
import net.unsweets.gamma.domain.entity.raw.OEmbed

class ThumbnailViewPagerAdapter(
    private val photos: List<OEmbed.Photo>,
    private val listener: Listener
) :
    RecyclerView.Adapter<ThumbnailViewPagerAdapter.ThumbnailViewHolder>() {
    interface Listener {
        fun onClick(path: String, position: Int, items: List<String>)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThumbnailViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.thumbnail_item, parent, false)
        return ThumbnailViewHolder(view)
    }

    override fun getItemCount(): Int = photos.size

    override fun onBindViewHolder(holder: ThumbnailViewHolder, position: Int) {
        val context = holder.itemView.context
        val item = photos[position]
        val url = item.thumbnailUrl ?: item.url
        Glide.with(context).load(url).into(holder.thumbnailImageView)
        holder.itemView.setOnClickListener {
            listener.onClick(item.url, position, photos.map { it.url })
        }
    }

    class ThumbnailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ThumbnailItemBinding.bind(itemView)
        val thumbnailImageView: ImageView = binding.thumbnailImageView
    }
}
