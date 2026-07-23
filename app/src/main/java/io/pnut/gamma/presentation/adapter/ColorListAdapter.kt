package io.pnut.gamma.presentation.adapter

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.pnut.gamma.R
import io.pnut.gamma.databinding.ChoosePrimaryColorDialogListItemBinding
import io.pnut.gamma.presentation.util.ThemeColorUtil
import androidx.appcompat.R as Rc

class ColorListAdapter(private val listener: Callback, currentColor: ThemeColorUtil.ThemeColor?) :
    ListAdapter<ThemeColorUtil.ThemeColor, ColorListAdapter.ColorListViewHolder>(ColorDiffCallback) {

    private var prevPosition = RecyclerView.NO_POSITION

    private val listenerInternal = object : CallbackInternal {
        override fun chooseThemeColor(themeColor: ThemeColorUtil.ThemeColor, position: Int) {
            val oldPosition = prevPosition
            prevPosition = position
            listener.chooseThemeColor(themeColor)

            if (oldPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(oldPosition)
            }
            if (prevPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(prevPosition)
            }
        }
    }

    init {
        setHasStableIds(true)
        val additionalThemes = ThemeColorUtil.ThemeColor.entries.filterNot { it == ThemeColorUtil.ThemeColor.Default }
        prevPosition = additionalThemes.indexOf(currentColor)
        submitList(additionalThemes)
    }

    interface Callback {
        fun chooseThemeColor(themeColor: ThemeColorUtil.ThemeColor)
    }

    interface CallbackInternal {
        fun chooseThemeColor(themeColor: ThemeColorUtil.ThemeColor, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.choose_primary_color_dialog_list_item, parent, false)
        return ColorListViewHolder(view, listenerInternal)
    }

    override fun onBindViewHolder(holder: ColorListViewHolder, position: Int) {
        val additionalTheme = getItem(position)
        holder.bindTo(additionalTheme, prevPosition == position, position)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).ordinal.toLong()
    }

    class ColorListViewHolder(itemView: View, private val listener: CallbackInternal) :
        RecyclerView.ViewHolder(itemView) {
        private val binding = ChoosePrimaryColorDialogListItemBinding.bind(itemView)
        private val themeColorCheck: ImageView = binding.themeColorCheck
        fun bindTo(
            themeColor: ThemeColorUtil.ThemeColor,
            checked: Boolean,
            position: Int
        ) {
            val theme = itemView.resources.newTheme().also {
                it.applyStyle(themeColor.themeResource, true)
            }
            val tv = TypedValue()
            theme.resolveAttribute(Rc.attr.colorPrimary, tv, true)
            val color = tv.data
            itemView.setBackgroundColor(color)
            themeColorCheck.visibility = if (checked) View.VISIBLE else View.GONE
            itemView.setOnClickListener { listener.chooseThemeColor(themeColor, position) }
        }
    }

    private object ColorDiffCallback : DiffUtil.ItemCallback<ThemeColorUtil.ThemeColor>() {
        override fun areItemsTheSame(
            oldItem: ThemeColorUtil.ThemeColor,
            newItem: ThemeColorUtil.ThemeColor
        ): Boolean = oldItem == newItem

        override fun areContentsTheSame(
            oldItem: ThemeColorUtil.ThemeColor,
            newItem: ThemeColorUtil.ThemeColor
        ): Boolean = oldItem == newItem
    }
}
