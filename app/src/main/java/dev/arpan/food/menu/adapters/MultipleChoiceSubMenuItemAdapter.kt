package dev.arpan.food.menu.adapters

import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.arpan.food.menu.data.SubMenuItem
import dev.arpan.food.menu.databinding.ItemSubMenuItemBinding

class MultipleChoiceListAdapter(private val listener: (positions: List<Int>) -> Unit) :
    RecyclerView.Adapter<MultipleChoiceViewHolder>() {

    private val selections = SparseBooleanArray()
    private val selectedPositions: List<Int>
        get() {
            return mutableListOf<Int>().apply {
                for (i in 0 until selections.size()) {
                    if (selections.valueAt(i)) {
                        add(selections.keyAt(i))
                    }
                }
            }
        }

    var data: List<SubMenuItem> = emptyList()
        set(value) {
            field = value
            value.forEachIndexed { index, subMenuItem ->
                selections.put(index, subMenuItem.isSelected)
            }
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MultipleChoiceViewHolder {
        return MultipleChoiceViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: MultipleChoiceViewHolder, position: Int) {
        holder.binding.item = data[position]
        holder.binding.checked = selections.get(position)
        holder.binding.executePendingBindings()

        holder.itemView.setOnClickListener {
            selections.put(position, !holder.binding.checkBox.isChecked)
            notifyItemChanged(position)

            listener.invoke(selectedPositions)
        }
    }

    override fun getItemCount() = data.size
}

class MultipleChoiceViewHolder private constructor(val binding: ItemSubMenuItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    companion object {

        fun create(parent: ViewGroup): MultipleChoiceViewHolder {
            return MultipleChoiceViewHolder(
                binding = ItemSubMenuItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }
}
