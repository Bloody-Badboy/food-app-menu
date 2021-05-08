package dev.arpan.food.menu.utils

import java.text.DecimalFormat

object Utils {
    private val decimalFormat = DecimalFormat("#.00")

    @JvmStatic
    fun formatDecimalPoint(value: Float): String {
        return decimalFormat.format(value)
    }
}
