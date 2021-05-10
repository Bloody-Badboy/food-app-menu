package dev.arpan.food.menu.utils

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import androidx.annotation.AttrRes
import dev.arpan.food.menu.R
import java.text.DecimalFormat

object Utils {
    private val decimalFormat = DecimalFormat("#.00")

    @JvmStatic
    fun formatDecimalPoint(value: Float): String {
        return decimalFormat.format(value)
    }
}

fun Context.getThemeColorAttribute(@AttrRes resId: Int): Int {
    val typedValue = TypedValue()
    val typedArray = obtainStyledAttributes(typedValue.data, intArrayOf(resId))
    val color = typedArray.getColor(0, 0)
    typedArray.recycle()
    return color
}

val Context.themePrimaryColor: Int
    get() = getThemeColorAttribute(R.attr.colorPrimary)

val Int.dp get() = (this * Resources.getSystem().displayMetrics.density).toInt()
val Int.px get() = (this / Resources.getSystem().displayMetrics.density).toInt()
