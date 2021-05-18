package dev.arpan.food.menu

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import dev.arpan.food.menu.adapters.MenuCategoryIndicatorAdapter
import dev.arpan.food.menu.adapters.MenuItemAdapter
import dev.arpan.food.menu.adapters.SubMenuCategoryAdapter
import dev.arpan.food.menu.data.MenuItem
import dev.arpan.food.menu.databinding.ActivityMainBinding
import dev.arpan.food.menu.databinding.DialogMenuCustomizationBinding
import dev.arpan.food.menu.utils.JumpSmoothScroller
import dev.arpan.food.menu.utils.Utils
import kotlinx.coroutines.flow.collect

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    private lateinit var binding: ActivityMainBinding

    private lateinit var indexer: MenuCategoryIndexer
    private lateinit var categoryIndicatorAdapter: MenuCategoryIndicatorAdapter
    private lateinit var menuItemAdapter: MenuItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.initViews()

        lifecycleScope.launchWhenStarted {
            viewModel.uiState.collect { state ->
                binding.handleMenuState(state.menuState)
                binding.handleCartState(state.cartState)
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.effect.collect { effect ->
                when (effect) {
                    is Effect.MenuItemUpdated -> {
                        menuItemAdapter.notifyItemChanged(effect.position)
                    }
                    is Effect.ShowCustomization -> {
                        showCustomizationSheet(effect.menuItem)
                    }
                }
            }
        }
    }

    private fun ActivityMainBinding.handleMenuState(menuState: MenuState) {
        when (menuState) {
            is MenuState.Initial, MenuState.Loading -> {
                pb.isVisible = true
                content.isVisible = false
            }
            is MenuState.MenuLoaded -> {
                pb.isVisible = false
                content.isVisible = true

                indexer = MenuCategoryIndexer(menuState.categories, menuState.items)
                (rvCategoryIndicator.adapter as MenuCategoryIndicatorAdapter).data =
                    menuState.categories
                (rv.adapter as MenuItemAdapter).data = menuState.items
            }
        }
    }

    private fun ActivityMainBinding.handleCartState(cartState: CartState) {
        when (cartState) {
            is CartState.Initial -> {
                layoutViewCart.isVisible = false
            }
            is CartState.HasItems -> {
                layoutViewCart.isVisible = true
                viewCart.tvCartItems.text = resources.getQuantityString(
                    R.plurals.item_quantity,
                    cartState.itemCount,
                    cartState.itemCount
                )

                val finalPrice = cartState.amount - cartState.discountAmount

                viewCart.tvFinalPrice.text = "$${Utils.formatDecimalPoint(finalPrice)}"
                viewCart.tvTotalPrice.text = "$${Utils.formatDecimalPoint(cartState.amount)}"

                viewCart.tvB1g1DiscountInfo.isVisible = cartState.discountItemCount > 0
                viewCart.tvTotalPrice.isVisible = cartState.discountItemCount >= 2

                when {
                    cartState.discountItemCount == 1 -> {
                        viewCart.tvB1g1DiscountInfo.text = getString(R.string.b1g1_not_applied)
                    }
                    cartState.discountItemCount == 2 || cartState.discountItemCount == 3 -> {
                        viewCart.tvB1g1DiscountInfo.text = getString(R.string.b1g1_applied_one_item)
                    }
                    cartState.discountAmount >= 4 -> {
                        viewCart.tvB1g1DiscountInfo.text = getString(R.string.b1g1_applied_two_item)
                    }
                }
            }
        }
    }

    private fun ActivityMainBinding.initViews() {

        val itemsScroller = JumpSmoothScroller(this@MainActivity)

        var fromUserClick = false
        categoryIndicatorAdapter = MenuCategoryIndicatorAdapter { category ->
            val position = indexer.positionForCategory(category)
            itemsScroller.targetPosition = position
            (rv.layoutManager as LinearLayoutManager).startSmoothScroll(itemsScroller)
            rvCategoryIndicator.smoothScrollToPosition(indexer.categories.indexOf(category))

            fromUserClick = true
        }
        menuItemAdapter = MenuItemAdapter(
            onAddClick = { item ->
                viewModel.setEvent(Event.AddItem(item))
            },
            onRemoveClick = { item ->
                viewModel.setEvent(Event.RemoveItem(item))
            }
        )

        rvCategoryIndicator.adapter = categoryIndicatorAdapter
        rv.adapter = menuItemAdapter
        rvCategoryIndicator.itemAnimator = null
        rv.itemAnimator = null

        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var prevIndex: Int? = -1

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (fromUserClick) fromUserClick = false
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (fromUserClick) return

                val first =
                    (rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                if (first < 0) {
                    return
                }

                val firstCategory = indexer.categoryForPosition(first) ?: return
                val firstCategoryIndex = indexer.categories.indexOf(firstCategory)

                if (firstCategoryIndex != prevIndex) {
                    prevIndex = firstCategoryIndex
                    categoryIndicatorAdapter.selectedPosition = firstCategoryIndex
                    rvCategoryIndicator.smoothScrollToPosition(firstCategoryIndex)
                }
            }
        })
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
            viewModel.setEvent(Event.CustomizationDialogDismissed(menuItem))
        }
        dialog.show()
    }
}
