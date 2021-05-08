package dev.arpan.food.menu.utils

import android.graphics.Typeface
import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("boldText")
fun TextView.bold(bold: Boolean) {
    if (bold) {
        setTypeface(null, Typeface.BOLD)
    } else {
        setTypeface(null, Typeface.NORMAL)
    }
}
