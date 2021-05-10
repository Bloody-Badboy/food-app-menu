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
import dev.arpan.food.menu.utils.dp

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

    private val transition =
        TransitionInflater.from(itemView.context).inflateTransition(R.transition.item_menu)

    private val start = ConstraintSet().apply {
        clone(binding.constraintLayout)

        clear(R.id.iv_dish_image, ConstraintSet.START)
        connect(
            R.id.iv_dish_image,
            ConstraintSet.END,
            ConstraintSet.PARENT_ID,
            ConstraintSet.END,
            16.dp
        )
        constrainWidth(R.id.iv_dish_image, 96.dp)
        constrainHeight(R.id.iv_dish_image, 96.dp)

        connect(
            R.id.iv_veg_non_veg,
            ConstraintSet.TOP,
            ConstraintSet.PARENT_ID,
            ConstraintSet.TOP,
            16.dp
        )
    }

    private val end = ConstraintSet().apply {
        clone(binding.constraintLayout)

        connect(
            R.id.iv_dish_image,
            ConstraintSet.START,
            ConstraintSet.PARENT_ID,
            ConstraintSet.START,
            16.dp
        )
        constrainWidth(R.id.iv_dish_image, ConstraintSet.MATCH_CONSTRAINT)
        constrainHeight(R.id.iv_dish_image, 200.dp)

        connect(
            R.id.iv_veg_non_veg,
            ConstraintSet.TOP,
            R.id.iv_dish_image,
            ConstraintSet.BOTTOM,
            16.dp
        )
    }

    fun bind(
        isFirstItemOfCategory: Boolean,
        item: MenuItem
    ) {
        binding.apply {
            menuItem = item
            executePendingBindings()
            tvCategory.isVisible = isFirstItemOfCategory

            if (item.isExpanded) {
                end.applyTo(constraintLayout)
            }else{
                start.applyTo(constraintLayout)
            }

            ivDishImage.setOnClickListener {
                TransitionManager.beginDelayedTransition(itemView.parent as ViewGroup, transition)
                if (item.isExpanded) {
                    start.applyTo(constraintLayout)
                } else {
                    end.applyTo(constraintLayout)
                }
                item.isExpanded = !item.isExpanded
            }
        }
    }
}
