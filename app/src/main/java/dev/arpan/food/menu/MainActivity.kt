package dev.arpan.food.menu

import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
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
import dev.arpan.food.menu.utils.JumpSmoothScroller
import okio.Buffer

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var menuItemAdapter: MenuItemAdapter

    private var categories: List<String> = emptyList()
    private var menuItems: MutableList<MenuItem> = mutableListOf()

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initMenuData()
        initAdapter()

        bottomSheetBehavior = BottomSheetBehavior.from(binding.layoutCustomization)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        binding.sheet.ibClose.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
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

        var handleUserCategoryClick = false
        val categoryIndicatorAdapter = MenuCategoryIndicatorAdapter { category ->
            val position = indexer.positionForCategory(category)
            itemsScroller.targetPosition = position
            (binding.rv.layoutManager as LinearLayoutManager).startSmoothScroll(itemsScroller)
            binding.rvCategoryIndicator.smoothScrollToPosition(categories.indexOf(category))

            handleUserCategoryClick = true
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
                    if (handleUserCategoryClick) handleUserCategoryClick = false
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (handleUserCategoryClick) return

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
        val index = menuItems.indexOfFirst { it.id == item.id }
        if (index >= 0) {
            if (addItem) {
                menuItems[index].quantity++
            } else {
                menuItems[index].quantity--
            }
            menuItemAdapter.notifyItemChanged(index)
        }
    }

    private fun showCustomizationSheet(menuItem: MenuItem) {

        if (menuItem.quantity == 0) {
            menuItem.quantity = 1
        }

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        binding.sheet.apply {
            tvCustomizationSubTitle.text = "Customize you ${menuItem.name}"

            rv.adapter = SubMenuCategoryAdapter(menuItem) {
                buttonAddToCart.text = "ADD $${menuItem.totalPrice}"
            }

            buttonAddToCart.text = "ADD $${menuItem.totalPrice}"

            tvCount.text = menuItem.quantity.toString()

            buttonAdd.setOnClickListener {
                menuItem.quantity++
                tvCount.text = menuItem.quantity.toString()
                buttonAddToCart.text = "ADD $${menuItem.totalPrice}"
            }
            buttonRemove.setOnClickListener {
                menuItem.quantity--
                tvCount.text = menuItem.quantity.toString()
                buttonAddToCart.text = "ADD $${menuItem.totalPrice}"

                if (menuItem.quantity == 0) {
                    menuItem.isAddedToCart = false
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                }
            }
            buttonAddToCart.setOnClickListener {
                menuItem.isAddedToCart = true
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    bottomSheetBehavior.removeBottomSheetCallback(this)
                    if (!menuItem.isAddedToCart) {
                        menuItem.reset()
                    }
                    val index = menuItems.indexOfFirst { it.id == menuItem.id }
                    if (index >= 0) {
                        menuItems[index] = menuItem
                        menuItemAdapter.notifyItemChanged(index)
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                val outRect = Rect()
                binding.sheet.root.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }
}
