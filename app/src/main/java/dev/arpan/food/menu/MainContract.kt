package dev.arpan.food.menu

import dev.arpan.food.menu.base.UiEffect
import dev.arpan.food.menu.base.UiEvent
import dev.arpan.food.menu.base.UiState
import dev.arpan.food.menu.data.MenuItem

sealed class Event : UiEvent {
    object LoadMenuData : Event()
    data class AddItem(val menuItem: MenuItem) : Event()
    data class RemoveItem(val menuItem: MenuItem) : Event()
    data class CustomizationDialogDismissed(val menuItem: MenuItem) : Event()
}

data class State(val menuState: MenuState, val cartState: CartState) : UiState

sealed class MenuState {
    object Initial : MenuState()
    object Loading : MenuState()
    data class MenuLoaded(
        val categories: List<String>,
        val items: List<MenuItem>
    ) : MenuState()
}

sealed class CartState {
    object Initial : CartState()
    data class HasItems(
        val itemCount: Int,
        val amount: Float,
        val discountItemCount: Int,
        val discountAmount: Float
    ) : CartState()
}

sealed class Effect : UiEffect {
    data class MenuItemUpdated(val position: Int) : Effect()
    data class ShowCustomization(val menuItem: MenuItem) : Effect()
}
