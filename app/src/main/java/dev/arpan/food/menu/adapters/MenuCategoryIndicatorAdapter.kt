package dev.arpan.food.menu.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import dev.arpan.food.menu.databinding.ItemMenuCategoryIndicatorBinding
import kotlin.properties.Delegates

class MenuCategoryIndicatorAdapter(private val onItemClick: (String) -> Unit) :
    RecyclerView.Adapter<CategoryViewHolder>() {

    var data: List<String> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
            if (value.isNotEmpty()) {
                selectedPosition = 0
            }
        }

    var selectedPosition by Delegates.observable(-1) { _, oldPos, newPos ->
        if (newPos in data.indices) {
            notifyItemChanged(oldPos)
            notifyItemChanged(newPos)
        }
    }

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item, position == selectedPosition)
        holder.itemView.setOnClickListener {
            selectedPosition = position
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = data.size
}

class CategoryViewHolder private constructor(val binding: ItemMenuCategoryIndicatorBinding) :
    RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun create(parent: ViewGroup): CategoryViewHolder {
            return CategoryViewHolder(
                binding = ItemMenuCategoryIndicatorBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    fun bind(categoryName: String, selected: Boolean) {
        (itemView as Chip).apply {
            isChecked = selected
            text = categoryName
        }
    }
}
