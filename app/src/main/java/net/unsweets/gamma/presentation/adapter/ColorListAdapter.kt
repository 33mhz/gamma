package net.unsweets.gamma.presentation.adapter

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import net.unsweets.gamma.R
import net.unsweets.gamma.databinding.ChoosePrimaryColorDialogListItemBinding
import net.unsweets.gamma.presentation.util.ThemeColorUtil

class ColorListAdapter(private val listener: Callback, currentColor: ThemeColorUtil.ThemeColor?) :
    RecyclerView.Adapter<ColorListAdapter.ColorListViewHolder>() {
    private val additionalThemes =
        ThemeColorUtil.ThemeColor.entries.filterNot { it == ThemeColorUtil.ThemeColor.Default }
    private var chooseColor: ThemeColorUtil.ThemeColor? = null
    private var prevPosition = additionalThemes.indexOf(currentColor)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.choose_primary_color_dialog_list_item, parent, false)
        return ColorListViewHolder(view, listenerInternal)
    }

    private val listenerInternal = object : CallbackInternal {
        override fun chooseThemeColor(themeColor: ThemeColorUtil.ThemeColor, position: Int) {
            chooseColor = themeColor
            listener.chooseThemeColor(themeColor)
            // Not working correctly
//            notifyItemChanged(position)
//            notifyItemChanged(prevPosition)
            notifyDataSetChanged()
            prevPosition = position
            if (prevPosition < 0) return
        }
    }

    init {
        setHasStableIds(true)
    }

    interface Callback {
        fun chooseThemeColor(themeColor: ThemeColorUtil.ThemeColor)
    }

    interface CallbackInternal {
        fun chooseThemeColor(themeColor: ThemeColorUtil.ThemeColor, position: Int)
    }

    override fun getItemCount(): Int = additionalThemes.size

    override fun onBindViewHolder(holder: ColorListViewHolder, position: Int) {
        val additionalTheme = additionalThemes[position]
        holder.bindTo(additionalTheme, prevPosition == position, position)
    }

    override fun getItemId(position: Int): Long {
        return additionalThemes[position].ordinal.toLong()
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
            theme.resolveAttribute(androidx.appcompat.R.attr.colorPrimary, tv, true)
            val color = tv.data
            itemView.setBackgroundColor(color)
            themeColorCheck.visibility = if (checked) View.VISIBLE else View.GONE
            itemView.setOnClickListener { listener.chooseThemeColor(themeColor, position) }
        }
    }
}
