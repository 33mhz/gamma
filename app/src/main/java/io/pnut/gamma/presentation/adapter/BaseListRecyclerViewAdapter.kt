package io.pnut.gamma.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.pnut.gamma.R
import io.pnut.gamma.databinding.SegmentItemBinding
import io.pnut.gamma.domain.entity.PnutResponse
import io.pnut.gamma.domain.entity.UniquePageable
import io.pnut.gamma.domain.model.PageableItemWrapper
import java.util.Locale

class PageableItemWrapperDiffCallback<T : UniquePageable> : DiffUtil.ItemCallback<PageableItemWrapper<T>>() {
    override fun areItemsTheSame(oldItem: PageableItemWrapper<T>, newItem: PageableItemWrapper<T>): Boolean {
        return when (oldItem) {
            is PageableItemWrapper.Item if newItem is PageableItemWrapper.Item -> {
                oldItem.item.uniqueKey == newItem.item.uniqueKey
            }

            is PageableItemWrapper.Pager if newItem is PageableItemWrapper.Pager -> {
                oldItem.maxId == newItem.maxId && oldItem.minId == newItem.minId
            }

            else -> {
                false
            }
        }
    }

    override fun areContentsTheSame(oldItem: PageableItemWrapper<T>, newItem: PageableItemWrapper<T>): Boolean {
        return oldItem == newItem
    }
}

class BaseListRecyclerViewAdapter<T : UniquePageable, V : RecyclerView.ViewHolder>(
    private val options: BaseListRecyclerViewAdapterOptions<T, V>
) : ListAdapter<PageableItemWrapper<T>, V>(PageableItemWrapperDiffCallback()) {
    data class BaseListRecyclerViewAdapterOptions<TT : UniquePageable, VV>(
        val itemList: ArrayList<PageableItemWrapper<TT>>,
        var listener: IBaseList<TT, VV>,
        val reverse: Boolean = false,
        var mainItemId: String = ""
    )

    init {
        submitList(ArrayList(options.itemList))
    }

    var recyclerView: RecyclerView? = null

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
    }

    private enum class ViewType { Body, Segment }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is PageableItemWrapper.Pager -> ViewType.Segment
            else -> ViewType.Body
        }.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): V {
        val inflater = LayoutInflater.from(parent.context)
        @Suppress("UNCHECKED_CAST")
        return when (ViewType.entries[viewType]) {
            ViewType.Body -> {
                val view = inflater
                    .inflate(options.listener.getItemLayout(), parent, false)
                options.listener.createViewHolder(view, viewType)
            }
            ViewType.Segment -> {
                val view = inflater
                    .inflate(R.layout.segment_item, parent, false)
                val viewHolder = SegmentViewHolder(view)
                viewHolder as V
            }
        }
    }

    interface IBaseList<T : UniquePageable, V> {
        fun createViewHolder(mView: View, viewType: Int = 0): V
        fun onClickItemListener(viewHolder: V, item: T, itemWrapper: PageableItemWrapper<T>)
        fun onBindViewHolder(item: T, viewHolder: V, position: Int, isMainItem: Boolean)
        fun getItemLayout(): Int
        val itemNameRes: Int
        fun onClickSegmentListener(
            viewHolder: SegmentViewHolder,
            itemWrapper: PageableItemWrapper.Pager<T>
        )
    }

    override fun onBindViewHolder(holder: V, position: Int) {
        val itemWrapper = getItem(position)
        when (ViewType.entries[getItemViewType(position)]) {
            ViewType.Body -> {
                val item = (itemWrapper as? PageableItemWrapper.Item)?.item ?: return
                holder.itemView.setOnClickListener {
                    options.listener.onClickItemListener(
                        holder,
                        item,
                        itemWrapper
                    )
                }
                options.listener.onBindViewHolder(
                    item,
                    holder,
                    position,
                    options.mainItemId == item.uniqueKey
                )
            }
            ViewType.Segment -> {
                val pager = itemWrapper as? PageableItemWrapper.Pager ?: return
                val viewHolder = holder as? SegmentViewHolder ?: return
                when {
                    pager.state == PageableItemWrapper.Pager.State.Error -> {
                        viewHolder.binding.retryButton.visibility = View.VISIBLE
                        viewHolder.binding.noItemsMessageTextView.visibility = View.GONE
                        viewHolder.binding.loadingIndicatorProgressBar.visibility = View.GONE
                        viewHolder.binding.endOfListImageView.visibility = View.GONE
                        viewHolder.binding.segmentImageView.visibility = View.GONE
                        setClickableSegment(viewHolder, pager) {
                            viewHolder.binding.retryButton.visibility = View.GONE
                            viewHolder.binding.loadingIndicatorProgressBar.visibility = View.VISIBLE
                            val newPager = pager.copy(state = PageableItemWrapper.Pager.State.Loading)
                            val idx = options.itemList.indexOf(pager)
                            if (idx >= 0) {
                                options.itemList[idx] = newPager
                                submitList(ArrayList(options.itemList))
                            }
                        }
                    }
                    pager.state == PageableItemWrapper.Pager.State.Loading -> {
                        viewHolder.binding.retryButton.visibility = View.GONE
                        viewHolder.binding.noItemsMessageTextView.visibility = View.GONE
                        viewHolder.binding.loadingIndicatorProgressBar.visibility = View.VISIBLE
                        viewHolder.binding.endOfListImageView.visibility = View.GONE
                        viewHolder.binding.segmentImageView.visibility = View.GONE
                        setClickableSegment(viewHolder, pager)
                    }
                    pager.more -> {
                        // remain items
                        viewHolder.binding.retryButton.visibility = View.GONE
                        viewHolder.binding.noItemsMessageTextView.visibility = View.GONE
                        viewHolder.binding.loadingIndicatorProgressBar.visibility = View.GONE
                        viewHolder.binding.endOfListImageView.visibility = View.GONE
                        viewHolder.binding.segmentImageView.visibility = View.VISIBLE
                        setClickableSegment(viewHolder, pager) {
                            viewHolder.binding.segmentImageView.visibility = View.GONE
                            viewHolder.binding.loadingIndicatorProgressBar.visibility = View.VISIBLE
                            val newPager = pager.copy(state = PageableItemWrapper.Pager.State.Loading)
                            val idx = options.itemList.indexOf(pager)
                            if (idx >= 0) {
                                options.itemList[idx] = newPager
                                submitList(ArrayList(options.itemList))
                            }
                        }
                    }
                    !pager.more && position == 0 -> {
                        // empty
                        viewHolder.binding.retryButton.visibility = View.GONE
                        viewHolder.binding.loadingIndicatorProgressBar.visibility = View.GONE
                        viewHolder.binding.noItemsMessageTextView.visibility = View.VISIBLE
                        viewHolder.binding.endOfListImageView.visibility = View.GONE
                        viewHolder.binding.segmentImageView.visibility = View.GONE
                        val context = holder.itemView.context
                        val itemName = context.getString(options.listener.itemNameRes)
                            .lowercase(Locale.ENGLISH)
                        viewHolder.binding.noItemsMessageTextView.text =
                            context.getString(R.string.no_items_template, itemName)
                        disableSegment(viewHolder)
                    }
                    else -> {
                        // loaded all items
                        viewHolder.binding.retryButton.visibility = View.GONE
                        viewHolder.binding.noItemsMessageTextView.visibility = View.GONE
                        viewHolder.binding.loadingIndicatorProgressBar.visibility = View.GONE
                        viewHolder.binding.endOfListImageView.visibility = View.VISIBLE
                        viewHolder.binding.segmentImageView.visibility = View.GONE
                        disableSegment(viewHolder)
                    }
                }
            }
        }
    }

    private fun setClickableSegment(
        viewHolder: SegmentViewHolder,
        pager: PageableItemWrapper.Pager<T>,
        onClick: () -> Unit = {}
    ) {
        viewHolder.itemView.isEnabled = true
        viewHolder.itemView.setOnClickListener {
            options.listener.onClickSegmentListener(viewHolder, pager)
            it.isEnabled = false
            onClick()
        }
    }

    private fun disableSegment(viewHolder: SegmentViewHolder) {
        viewHolder.itemView.setOnClickListener(null)
    }

    fun updateItem(item: PageableItemWrapper<T>) {
        val index = options.itemList.indexOfFirst {
            it.uniqueKey == item.uniqueKey
        }
        if (index < 0) return
        options.itemList[index] = item
        submitList(ArrayList(options.itemList))
    }

    fun removeItem(item: PageableItemWrapper<T>) {
        val index = options.itemList.indexOf(item)
        if (index < 0) return
        options.itemList.removeAt(index)
        submitList(ArrayList(options.itemList))
    }

    fun showRetryMessage() {
        val pagerIndex = options.itemList.indexOfLast { it is PageableItemWrapper.Pager }
        if (pagerIndex >= 0) {
            val pager = options.itemList[pagerIndex] as PageableItemWrapper.Pager<T>
            options.itemList[pagerIndex] = pager.copy(state = PageableItemWrapper.Pager.State.Error)
            submitList(ArrayList(options.itemList))
        }
    }

    fun updateMainItemId(id: String) {
        if (options.mainItemId == id) return

        val oldId = options.mainItemId
        options.mainItemId = id

        options.itemList.forEachIndexed { index, wrapper ->
            if (wrapper.uniqueKey == oldId || wrapper.uniqueKey == id) {
                notifyItemChanged(index)
            }
        }
    }

    fun removeSegmentIfNeed(requestPager: PageableItemWrapper<T>?): Int {
        if (requestPager == null) return 0
        val willRemoveSegmentIndex = options.itemList.indexOf(requestPager)
        if (willRemoveSegmentIndex < 0) return 0
        options.itemList.removeAt(willRemoveSegmentIndex)
        return willRemoveSegmentIndex
    }

    private fun addSegmentIfNeed(
        index: Int,
        requestPager: PageableItemWrapper<T>?,
        response: PnutResponse<List<T>>
    ) {
        when (response.meta.more) {
            true -> {
                val segment = PageableItemWrapper.Pager.createFromMeta(response.meta, requestPager)
                options.itemList.add(index, segment)
            }
            false if index == options.itemList.size -> {
                options.itemList.add(
                    index,
                    PageableItemWrapper.Pager.createFromMeta(response.meta, requestPager)
                )
            }
            false if 0 == index && options.itemList.isEmpty() -> {
                options.itemList.add(
                    PageableItemWrapper.Pager.createFromMeta(
                        response.meta,
                        requestPager
                    )
                )
            }

            else -> {}
        }
    }

    fun updateItems(
        requestPager: PageableItemWrapper<T>?,
        response: PnutResponse<List<T>>
    ) {
        var insertIndex = removeSegmentIfNeed(requestPager)
        val items = response.data
        val newKeys = items.map { it.uniqueKey }.toSet()

        val iterator = options.itemList.iterator()
        var i = 0
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (item.uniqueKey in newKeys) {
                iterator.remove()
                if (i < insertIndex) {
                    insertIndex--
                }
            } else {
                i++
            }
        }

        val pageableItemWrapperItems = items.map { PageableItemWrapper.Item(it) }

        options.itemList.addAll(insertIndex, pageableItemWrapperItems)
        addSegmentIfNeed(insertIndex + pageableItemWrapperItems.size, requestPager, response)
        submitList(ArrayList(options.itemList))
    }

    class SegmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = SegmentItemBinding.bind(itemView)
    }
}
