package dev.arpan.food.menu.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlin.properties.Delegates

@JsonClass(generateAdapter = true)
data class SubMenuCategory(

    @Json(name = "menuCategoryName")
    val categoryName: String,

    @Json(name = "sCategoryType")
    val sCategoryType: String,

    @Json(name = "itemMaxCount")
    val itemMaxCount: String,

    @Json(name = "tDescription")
    val description: String,

    @Json(name = "MenuItem")
    val subMenuItem: List<SubMenuItem>,

    @Json(name = "subMenuCategoryID")
    val subMenuCategoryID: Long,

    @Json(name = "eStatus")
    val eStatus: String
)

@JsonClass(generateAdapter = true)
data class SubMenuItem(

    @Json(name = "itemID")
    val id: Long,

    @Json(name = "subMenuItemID")
    val subId: Long,

    @Json(name = "tDescription")
    val description: String,

    @Json(name = "menuItemName")
    val name: String,

    @Json(name = "subItemPrice")
    val price: Float,

    @Json(name = "defaultSelect")
    @YesNoBoolean
    val isDefaultSelect: Boolean,

    @Json(name = "subMenuCategoryID")
    val categoryID: String,

    @Json(name = "eStatus")
    val status: String
) {
    var isSelected: Boolean = isDefaultSelect
}

@JsonClass(generateAdapter = true)
data class MenuCategory(

    @Json(name = "MenuList")
    val menu: List<MenuItem>,

    @Json(name = "MenuCategory")
    val categoryName: String
)

@JsonClass(generateAdapter = true)
data class MenuItem(

    @Json(name = "itemID")
    val id: Long,

    @Json(name = "itemName")
    val name: String,

    @Json(name = "isVeg")
    @YesNoBoolean
    val isVeg: Boolean,

    @Json(name = "tDescription")
    val description: String,

    @Json(name = "discountedItemPrice")
    val discountedPrice: Int,

    @Json(name = "itemStatus")
    val status: String,

    @Json(name = "isB1G1Applicable")
    @YesNoBoolean
    val isB1G1Applicable: Boolean,

    @Json(name = "itemPrice")
    val price: Float,

    @Json(name = "isBestSeller")
    @YesNoBoolean
    val isBestSeller: Boolean,

    @Json(name = "itemImage")
    val image: String,

    @Json(name = "MenuCategory")
    val subMenuCategory: List<SubMenuCategory> = emptyList()
) {
    val isCustomizationAvailable: Boolean = subMenuCategory.isNotEmpty()

    var categoryName: String = ""

    var quantity: Int by Delegates.vetoable(0) { _, _, newValue ->
        newValue >= 0
    }

    // price of the item after adding submenu item to it
    var finalPrice: Float = price

    val totalPrice: Float
        get() = (finalPrice * quantity).coerceAtLeast(price)

    var isAddedToCart: Boolean = false

    val tags: List<String>
        get() {
            val list = mutableListOf<String>()
            if (isBestSeller) {
                list.add("Bestseller")
            }
            if (isB1G1Applicable) {
                list.add("Buy 1 Get 1")
            }
            return list
        }

    fun reset() {
        quantity = 0
        finalPrice = price
        subMenuCategory.forEach { menuCategory ->
            menuCategory.subMenuItem.forEach { subMenuItem ->
                subMenuItem.isSelected = subMenuItem.isDefaultSelect
            }
        }
    }
}
