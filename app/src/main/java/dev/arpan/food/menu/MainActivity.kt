package dev.arpan.food.menu

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dev.arpan.food.menu.adapters.MenuCategoryIndicatorAdapter
import dev.arpan.food.menu.adapters.MenuItemAdapter
import dev.arpan.food.menu.adapters.SubMenuCategoryAdapter
import dev.arpan.food.menu.data.MenuCategory
import dev.arpan.food.menu.data.MenuItem
import dev.arpan.food.menu.data.YesNoBooleanAdapter
import dev.arpan.food.menu.databinding.ActivityMainBinding
import dev.arpan.food.menu.databinding.DialogMenuCustomizationBinding
import dev.arpan.food.menu.utils.JumpSmoothScroller
import dev.arpan.food.menu.utils.Utils
import okio.Buffer

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var menuItemAdapter: MenuItemAdapter

    private var categories: List<String> = emptyList()
    private var menuItems: MutableList<MenuItem> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initMenuData()
        initAdapter()
    }

    private fun initMenuData() {
        val moshi = Moshi.Builder()
            .add(YesNoBooleanAdapter())
            .build()
        val adapter = moshi.adapter<List<MenuCategory>>(
            Types.newParameterizedType(
                List::class.java,
                MenuCategory::class.java
            )
        )

        val buffer = Buffer().readFrom(assets.open("menu.json"))
        val reader = JsonReader.of(buffer)

        val data = adapter.fromJson(reader)!!

        categories = data.map { it.categoryName }
        menuItems = data.flatMap { menuCategory ->
            menuCategory.menu.map { menuItem ->
                menuItem.apply {
                    categoryName = menuCategory.categoryName
                }
            }
        }.toMutableList()
    }

    private fun initAdapter() {

        val indexer = MenuCategoryIndexer(categories, menuItems)
        val itemsScroller = JumpSmoothScroller(this)

        var fromUserClick = false
        val categoryIndicatorAdapter = MenuCategoryIndicatorAdapter { category ->
            val position = indexer.positionForCategory(category)
            itemsScroller.targetPosition = position
            (binding.rv.layoutManager as LinearLayoutManager).startSmoothScroll(itemsScroller)
            binding.rvCategoryIndicator.smoothScrollToPosition(categories.indexOf(category))

            fromUserClick = true
        }.apply {
            data = categories
        }
        menuItemAdapter = MenuItemAdapter(
            onAddClick = { item ->
                if (!item.isCustomizationAvailable) {
                    updateItemQuantity(item, true)
                } else {
                    showCustomizationSheet(item)
                }
            },
            onRemoveClick = { item ->
                if (!item.isCustomizationAvailable) {
                    updateItemQuantity(item, false)
                } else {
                    showCustomizationSheet(item)
                }
            }
        ).apply {
            data = menuItems
        }

        binding.rvCategoryIndicator.adapter = categoryIndicatorAdapter
        binding.rv.adapter = menuItemAdapter
        binding.rvCategoryIndicator.itemAnimator = null
        binding.rv.itemAnimator = null

        binding.rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var prevIndex: Int? = -1

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (fromUserClick) fromUserClick = false
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (fromUserClick) return

                val first =
                    (binding.rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                if (first < 0) {
                    return
                }

                val firstCategory = indexer.categoryForPosition(first) ?: return

                val firstCategoryIndex = categories.indexOf(firstCategory)

                if (firstCategoryIndex != prevIndex) {
                    prevIndex = firstCategoryIndex
                    categoryIndicatorAdapter.selectedPosition = firstCategoryIndex
                    binding.rvCategoryIndicator.smoothScrollToPosition(firstCategoryIndex)
                }
            }
        })
    }

    private fun updateItemQuantity(item: MenuItem, addItem: Boolean) {
        if (addItem) {
            item.quantity++
            item.isAddedToCart = true
        } else {
            item.quantity--
            if (item.quantity == 0) {
                item.isAddedToCart = false
            }
        }
        updateMenuItem(item)
    }

    private fun updateMenuItem(item: MenuItem) {
        val index = menuItems.indexOfFirst { it.id == item.id }
        if (index >= 0) {
            menuItems[index] = item
            menuItemAdapter.notifyItemChanged(index)
            calculateCartPrice()
        }
    }

    private fun calculateCartPrice() {
        var cartValue = 0f
        var cartItemCount = 0
        var discountAmount = 0f

        val cartItems = menuItems.filter { it.isAddedToCart }
        cartItems.forEach {
            cartItemCount += it.quantity
            cartValue += it.totalPrice
        }

        val b1g1Items = cartItems.filter { it.isB1G1Applicable }.sortedBy { it.finalPrice }
        val discountItemCount = b1g1Items.sumBy { it.quantity }

        if (discountItemCount >= 2) {
            val item = b1g1Items[0]
            discountAmount += item.finalPrice
        }

        if (discountItemCount >= 4) {
            // if first item's quantity is four or more the list size would be 1
            val item = if (b1g1Items.size >= 2) b1g1Items[1] else b1g1Items[0]
            discountAmount += item.finalPrice
        }

        binding.apply {
            if (cartValue > 0f) {
                layoutViewCart.isVisible = true
                viewCart.tvCartItems.text = resources.getQuantityString(
                    R.plurals.item_quantity,
                    cartItemCount,
                    cartItemCount
                )

                val finalPrice = cartValue - discountAmount

                viewCart.tvFinalPrice.text = "$${Utils.formatDecimalPoint(finalPrice)}"
                viewCart.tvTotalPrice.text = "$${Utils.formatDecimalPoint(cartValue)}"

                viewCart.tvB1g1DiscountInfo.isVisible = discountItemCount > 0
                viewCart.tvTotalPrice.isVisible = discountItemCount >= 2

                when {
                    discountItemCount == 1 -> {
                        viewCart.tvB1g1DiscountInfo.text = getString(R.string.b1g1_not_applied)
                    }
                    discountItemCount == 2 || discountItemCount == 3 -> {
                        viewCart.tvB1g1DiscountInfo.text = getString(R.string.b1g1_applied_one_item)
                    }
                    discountAmount >= 4 -> {
                        viewCart.tvB1g1DiscountInfo.text = getString(R.string.b1g1_applied_two_item)
                    }
                }
            } else {
                layoutViewCart.isVisible = false
            }
        }
    }

    private fun showCustomizationSheet(menuItem: MenuItem) {

        if (menuItem.quantity == 0) {
            menuItem.quantity = 1
        }

        val dialog = BottomSheetDialog(this)
        dialog.setContentView(
            DialogMenuCustomizationBinding.inflate(layoutInflater).apply {
                tvCustomizationSubTitle.text = "Customize you ${menuItem.name}"

                rv.adapter = SubMenuCategoryAdapter(menuItem) {
                    buttonAddToCart.text = "ADD $${Utils.formatDecimalPoint(menuItem.totalPrice)}"
                }

                buttonAddToCart.text = "ADD $${Utils.formatDecimalPoint(menuItem.totalPrice)}"

                tvCount.text = menuItem.quantity.toString()

                buttonAdd.setOnClickListener {
                    menuItem.quantity++
                    tvCount.text = menuItem.quantity.toString()
                    buttonAddToCart.text = "ADD $${Utils.formatDecimalPoint(menuItem.totalPrice)}"
                }
                buttonRemove.setOnClickListener {
                    menuItem.quantity--
                    tvCount.text = menuItem.quantity.toString()
                    buttonAddToCart.text = "ADD $${Utils.formatDecimalPoint(menuItem.totalPrice)}"

                    if (menuItem.quantity == 0) {
                        menuItem.isAddedToCart = false
                        dialog.dismiss()
                    }
                }
                buttonAddToCart.setOnClickListener {
                    menuItem.isAddedToCart = true
                    dialog.dismiss()
                }
                ibClose.setOnClickListener {
                    dialog.dismiss()
                }
            }.root
        )
        dialog.setOnDismissListener {
            if (!menuItem.isAddedToCart) {
                menuItem.reset()
            }
            updateMenuItem(menuItem)
        }
        dialog.show()
    }
}
