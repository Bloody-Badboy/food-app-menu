package dev.arpan.food.menu.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import dev.arpan.food.menu.data.MenuItem
import dev.arpan.food.menu.data.SubMenuCategory
import dev.arpan.food.menu.databinding.ItemSubMenuCategoryBinding
import java.util.Locale

class SubMenuCategoryAdapter(
    private val menuItem: MenuItem,
    private val onMenuItemUpdated: () -> Unit
) :
    RecyclerView.Adapter<SubMenuCategoryViewHolder>() {

    private var data: List<SubMenuCategory> = menuItem.subMenuCategory

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubMenuCategoryViewHolder {
        return SubMenuCategoryViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: SubMenuCategoryViewHolder, position: Int) {
        holder.bind(data[position], menuItem, onMenuItemUpdated)
    }

    override fun getItemCount(): Int = data.size
}

class SubMenuCategoryViewHolder private constructor(val binding: ItemSubMenuCategoryBinding) :
    RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun create(parent: ViewGroup): SubMenuCategoryViewHolder {
            return SubMenuCategoryViewHolder(
                binding = ItemSubMenuCategoryBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    fun bind(categoryName: SubMenuCategory, menuItem: MenuItem, onMenuItemUpdated: () -> Unit) {
        binding.tvTitle.text = categoryName.categoryName
        binding.tvDesc.text = categoryName.description
        binding.divider.isVisible = adapterPosition != 0

        when (categoryName.sCategoryType.toLowerCase(Locale.ROOT)) {
            "selection" -> {
                binding.rv.adapter = SingleChoiceListAdapter { position ->
                    categoryName.subMenuItem.forEachIndexed { index, subMenuItem ->
                        if (index == position) {
                            if (!subMenuItem.isSelected) {
                                subMenuItem.isSelected = true
                                menuItem.finalPrice += subMenuItem.price
                            }
                        } else {
                            if (subMenuItem.isSelected) {
                                menuItem.finalPrice -= subMenuItem.price
                                subMenuItem.isSelected = false
                            }
                        }
                    }
                    onMenuItemUpdated()
                }.apply {
                    data = categoryName.subMenuItem
                }
            }
            "addon" -> {
                binding.rv.adapter = MultipleChoiceListAdapter { positions ->
                    categoryName.subMenuItem.forEachIndexed { index, subMenuItem ->
                        if (index in positions) {
                            if (!subMenuItem.isSelected) {
                                subMenuItem.isSelected = true
                                menuItem.finalPrice += subMenuItem.price
                            }
                        } else {
                            if (subMenuItem.isSelected) {
                                menuItem.finalPrice -= subMenuItem.price
                                subMenuItem.isSelected = false
                            }
                        }
                    }
                    onMenuItemUpdated()
                }.apply {
                    data = categoryName.subMenuItem
                }
            }
        }
    }
}
