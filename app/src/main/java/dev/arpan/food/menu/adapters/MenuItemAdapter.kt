package dev.arpan.food.menu.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import dev.arpan.food.menu.data.MenuItem
import dev.arpan.food.menu.databinding.ItemMenuBinding

class MenuItemAdapter(
    private val onAddClick: (MenuItem) -> Unit,
    private val onRemoveClick: (MenuItem) -> Unit
) : RecyclerView.Adapter<CategoryItemViewHolder>() {

    var data: List<MenuItem> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryItemViewHolder {
        return CategoryItemViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: CategoryItemViewHolder, position: Int) {
        val item = data[position]
        val isFirstItemOfCategory = if (position > 0) {
            item.categoryName != data[position - 1].categoryName
        } else {
            true
        }
        holder.bind(isFirstItemOfCategory, item)
        holder.binding.layoutStepper.onAdd = {
            onAddClick.invoke(item)
        }
        holder.binding.layoutStepper.onRemove = {
            onRemoveClick.invoke(item)
        }
    }

    override fun getItemCount(): Int = data.size
}

class CategoryItemViewHolder private constructor(val binding: ItemMenuBinding) :
    RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun create(parent: ViewGroup): CategoryItemViewHolder {
            return CategoryItemViewHolder(
                binding = ItemMenuBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    fun bind(
        isFirstItemOfCategory: Boolean,
        item: MenuItem
    ) {
        binding.apply {
            menuItem = item
            executePendingBindings()
            tvCategory.isVisible = isFirstItemOfCategory
        }
    }
}
