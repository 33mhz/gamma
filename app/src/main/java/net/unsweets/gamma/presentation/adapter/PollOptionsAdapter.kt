package net.unsweets.gamma.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import net.unsweets.gamma.R
import net.unsweets.gamma.databinding.PollItemViewBinding
import net.unsweets.gamma.domain.entity.Poll
import net.unsweets.gamma.domain.entity.PollLikeValue
import net.unsweets.gamma.util.LogUtil

class PollOptionsAdapter(private val pollLikeValue: PollLikeValue, private var poll: Poll? = null) :
    RecyclerView.Adapter<PollOptionsAdapter.OptionsViewHolder>() {
    val reachedLimit: Boolean
        get() = (poll?.maxOptions ?: 1) < chosenPositions.size
    val votable
        get() = !reachedLimit && chosenPositions.isNotEmpty()
    private val options
        get() = poll?.options ?: pollLikeValue.options

    interface Callback {
        fun onUpdateChoiceState(votable: Boolean)
    }

    var listener: Callback? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.poll_item_view, parent, false)
        return OptionsViewHolder(view)
    }

    fun setPollDetail(poll: Poll) {
        this.poll = poll
        chosenPositions.clear()
        LogUtil.e("poll $poll")
        val positions =
            poll.options.withIndex().filter { it.value.isYourResponse == true }.map { it.index }
        LogUtil.e("positions $positions")
        chosenPositions.addAll(positions)
        notifyDataSetChanged()
    }

    private val chosenPositions = mutableSetOf<Int>()
    val getChosenPositions
        get() = chosenPositions.toSet()

    override fun getItemCount(): Int = options.size

    override fun onBindViewHolder(holder: OptionsViewHolder, position: Int) {
        val option = options[position]
        val pollLocal = poll
        holder.bindTo(option, chosenPositions, pollLocal) {
            listener?.onUpdateChoiceState(votable)

        }

    }

    class OptionsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = PollItemViewBinding.bind(itemView)
        private val pollOptionProgressBar: ProgressBar = binding.pollOptionProgressBar
        private val pollOptionCheckBox: MaterialCheckBox = binding.pollOptionCheckBox
        private val pollOptionCountTextView: TextView = binding.pollOptionCountTextView
        private val pollOptionLayout: ViewGroup = binding.pollOptionLayout
        fun bindTo(
            option: Poll.PollOption,
            chosenPositions: MutableSet<Int>,
            poll: Poll?,
            callback: () -> Unit
        ) {
            val value = option.getPercent(poll?.total)
            LogUtil.e("total ${poll?.total} value $value ${option.respondents}")
            pollOptionProgressBar.progress = value
            pollOptionCheckBox.text = option.text
            pollOptionCountTextView.text =
                itemView.context.getString(R.string.poll_percent_template, value)
            LogUtil.e("alreadyClosed ${poll?.alreadyClosed}")
            val alreadyClosed = poll?.alreadyClosed ?: false
            pollOptionCheckBox.isEnabled = !alreadyClosed
            pollOptionCheckBox.isChecked =
                if (poll?.alreadyClosed == false) chosenPositions.contains(bindingAdapterPosition) else option.isYourResponse == true
            pollOptionLayout.setOnClickListener { if (!alreadyClosed) pollOptionCheckBox.performClick() }
            pollOptionCheckBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    chosenPositions.add(bindingAdapterPosition)
                } else {
                    chosenPositions.remove(bindingAdapterPosition)
                }
                callback()
            }
        }

    }

}
