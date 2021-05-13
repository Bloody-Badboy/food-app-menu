package dev.arpan.food.menu.utils

import android.graphics.Paint
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

@BindingAdapter("strikeThrough")
fun TextView.showStrikeThrough(show: Boolean) {
    paintFlags = if (show)
        paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
    else
        paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
}
