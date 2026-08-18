package io.pnut.gamma.presentation.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.pnut.gamma.R
import io.pnut.gamma.databinding.ComposeThumbnailImageBinding
import io.pnut.gamma.domain.model.UriInfo

class ComposeThumbnailAdapter(
    private val items: MutableList<UriInfo> = mutableListOf(),
    private val listener: Callback
) : RecyclerView.Adapter<ComposeThumbnailAdapter.ViewHolder>() {

    interface Callback {
        fun onRemove()
        fun onClick(uri: Uri, index: Int)
        fun updateList(list: List<UriInfo>)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.compose_thumbnail_image, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val uriInfo = items[position]
        Glide
            .with(holder.binding.thumbnail)
            .load(uriInfo.uri)
            .sizeMultiplier(.7f)
            .into(holder.binding.thumbnail)

        holder.binding.removeButton.setOnClickListener { remove(holder.bindingAdapterPosition) }
        holder.binding.thumbnail.setOnClickListener {
            listener.onClick(uriInfo.uri, holder.bindingAdapterPosition)
        }
    }

    private fun remove(index: Int) {
        if (index == RecyclerView.NO_POSITION) return
        items.removeAt(index)
        listener.onRemove()
        listener.updateList(items)
        notifyItemRemoved(index)
    }

    fun addAll(uriList: List<UriInfo>) {
        val start = items.size
        items.addAll(uriList)
        listener.updateList(items)
        notifyItemRangeInserted(start, uriList.size)
    }

    fun add(uriInfo: UriInfo) {
        val index = items.size
        items.add(index, uriInfo)
        listener.updateList(items)
        notifyItemInserted(index)
    }

    fun replace(uriInfo: UriInfo, index: Int) {
        if (index == RecyclerView.NO_POSITION) return
        items[index] = uriInfo
        listener.updateList(items)
        notifyItemChanged(index)
    }

    fun getItems() = items

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ComposeThumbnailImageBinding.bind(view)
    }
}
