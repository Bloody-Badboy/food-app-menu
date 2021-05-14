package dev.arpan.food.menu.adapters

import android.transition.TransitionInflater
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import dev.arpan.food.menu.R
import dev.arpan.food.menu.data.MenuItem
import dev.arpan.food.menu.databinding.ItemMenuBinding
import dev.arpan.food.menu.utils.executeAfter

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
        holder.binding.content.layoutStepper.onAdd = {
            onAddClick.invoke(item)
        }
        holder.binding.content.layoutStepper.onRemove = {
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

    private val transition =
        TransitionInflater.from(itemView.context).inflateTransition(R.transition.item_menu)

    private val start = ConstraintSet().apply {
        clone(binding.content.constraintLayout)
    }

    private val end = ConstraintSet().apply {
        clone(itemView.context, R.layout.include_menu_item_content_alt)
    }

    fun bind(
        isFirstItemOfCategory: Boolean,
        item: MenuItem
    ) {
        binding.apply {

            tvCategory.isVisible = isFirstItemOfCategory

            if (item.isExpanded) {
                end.applyTo(binding.content.constraintLayout)
            } else {
                start.applyTo(binding.content.constraintLayout)
            }

            executeAfter {
                menuItem = item
            }

            binding.content.ivDishImage.setOnClickListener {
                TransitionManager.beginDelayedTransition(itemView.parent as ViewGroup, transition)
                if (item.isExpanded) {
                    start.applyTo(binding.content.constraintLayout)
                } else {
                    end.applyTo(binding.content.constraintLayout)
                }
                item.isExpanded = !item.isExpanded
                executeAfter {
                    menuItem = item
                }
            }
        }
    }
}
