package dev.arpan.food.menu

import androidx.lifecycle.viewModelScope
import dev.arpan.food.menu.base.BaseViewModel
import dev.arpan.food.menu.data.MenuItem
import dev.arpan.food.menu.data.MenuRepository
import dev.arpan.food.menu.utils.sumByFloat
import kotlinx.coroutines.launch

class MainViewModel(private val repository: MenuRepository = MenuRepository.getInstance()) :
    BaseViewModel<Event, State, Effect>() {

    init {
        setEvent(Event.LoadMenuData)
    }

    private lateinit var categories: List<String>
    private lateinit var menuItems: MutableList<MenuItem>

    override fun createInitialState() =
        State(menuState = MenuState.Initial, cartState = CartState.Initial)

    override fun handleEvent(event: Event) {
        when (event) {
            is Event.LoadMenuData -> {
                viewModelScope.launch {

                    setState {
                        State(
                            menuState = MenuState.Loading,
                            cartState = CartState.Initial
                        )
                    }

                    val data = repository.getMenuMenuCategory()
                    categories = data.map { menuCategory -> menuCategory.categoryName }
                    menuItems = data.flatMap { menuCategory ->
                        menuCategory.menu.map { menuItem ->
                            menuItem.apply {
                                categoryName = menuCategory.categoryName
                            }
                        }
                    }.toMutableList()

                    setState {
                        State(
                            menuState = MenuState.MenuLoaded(
                                categories = categories,
                                items = menuItems,
                            ),
                            cartState = CartState.Initial
                        )
                    }
                }
            }
            is Event.AddItem -> {
                if (event.menuItem.isCustomizationAvailable) {
                    setEffect {
                        Effect.ShowCustomization(menuItem = event.menuItem)
                    }
                } else {
                    updateItemQuantity(item = event.menuItem, addItem = true)
                }
            }
            is Event.RemoveItem -> {
                if (event.menuItem.isCustomizationAvailable) {
                    setEffect {
                        Effect.ShowCustomization(menuItem = event.menuItem)
                    }
                } else {
                    updateItemQuantity(item = event.menuItem, addItem = false)
                }
            }
            is Event.CustomizationDialogDismissed -> {
                if (!event.menuItem.isAddedToCart) {
                    event.menuItem.reset()
                }
                updateMenuItem(item = event.menuItem)
            }
        }
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
            setEffect {
                Effect.MenuItemUpdated(position = index)
            }
            calculateCartPrice()
        }
    }

    private fun calculateCartPrice() {

        val cartItems = menuItems.filter { it.isAddedToCart }
        val cartValue = cartItems.sumByFloat { it.totalPrice }
        val cartItemCount = cartItems.sumBy { it.quantity }

        val b1g1Items = cartItems.filter { it.isB1G1Applicable }.sortedBy { it.finalPrice }
        val discountItems = mutableListOf<MenuItem>()
        val discountItemCount = b1g1Items.sumBy { it.quantity }

        if (discountItemCount >= 2) {
            val item = b1g1Items[0]
            discountItems.add(item)
        }

        if (discountItemCount >= 4) {
            // if first item's quantity is four or more the list size would be 1
            val item = if (b1g1Items.size >= 2) b1g1Items[1] else b1g1Items[0]
            discountItems.add(item)
        }

        val discountAmount = discountItems.sumByFloat { it.finalPrice }

        val cartState = if (cartItemCount >= 1) {
            CartState.HasItems(
                itemCount = cartItemCount,
                amount = cartValue,
                discountItemCount = discountItemCount,
                discountAmount = discountAmount
            )
        } else {
            CartState.Initial
        }
        setState {
            State(
                menuState = currentState.menuState,
                cartState = cartState
            )

        }
    }
}