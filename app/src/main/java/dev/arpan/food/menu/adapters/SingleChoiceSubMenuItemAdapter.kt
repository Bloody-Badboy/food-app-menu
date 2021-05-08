package dev.arpan.food.menu.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.arpan.food.menu.data.SubMenuItem
import dev.arpan.food.menu.databinding.ItemSubMenuItemBinding
import kotlin.properties.Delegates

class SingleChoiceListAdapter(
    private val itemListener: (position: Int) -> Unit
) : RecyclerView.Adapter<SingleChoiceViewHolder>() {

    var data: List<SubMenuItem> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()

            val index = value.indexOfFirst { it.isSelected }
            if (index >= 0) {
                selectedPosition = index
            }
        }

    var selectedPosition by Delegates.observable(-1) { _, oldPos, newPos ->
        if (newPos in data.indices) {
            notifyItemChanged(oldPos)
            notifyItemChanged(newPos)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingleChoiceViewHolder {
        return SingleChoiceViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: SingleChoiceViewHolder, position: Int) {
        holder.bind(data[position], position == selectedPosition)
        holder.itemView.setOnClickListener {
            selectedPosition = position
            itemListener.invoke(position)
        }
    }

    override fun getItemCount() = data.size
}

class SingleChoiceViewHolder(val binding: ItemSubMenuItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(subMenuItem: SubMenuItem, selected: Boolean) {
        binding.item = subMenuItem
        binding.checked = selected
        binding.executePendingBindings()
    }

    companion object {
        fun create(parent: ViewGroup): SingleChoiceViewHolder {
            return SingleChoiceViewHolder(
                binding = ItemSubMenuItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }
}
