package dev.arpan.food.menu.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import androidx.databinding.BindingAdapter
import dev.arpan.food.menu.databinding.StepperViewBinding

@BindingAdapter("count")
fun StepperView.stepCount(count: Int?) {
    this.count = count ?: return
}

class StepperView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = StepperViewBinding.inflate(LayoutInflater.from(context), this, true)

    var count: Int = 0
        set(value) {
            field = value
            binding.apply {
                tvCount.text = value.toString()
                if (value >= 1) {
                    tvCount.visibility = VISIBLE
                    buttonRemove.visibility = VISIBLE
                    tvAdd.visibility = GONE
                } else {
                    tvCount.visibility = GONE
                    buttonRemove.visibility = GONE
                    tvAdd.visibility = VISIBLE
                }
            }
        }

    var onAdd: (() -> Unit)? = null
    var onRemove: (() -> Unit)? = null

    init {

        binding.apply {
            buttonAdd.setOnClickListener {
                onAdd?.invoke()
            }

            buttonRemove.setOnClickListener {
                onRemove?.invoke()
            }
        }
    }
}
