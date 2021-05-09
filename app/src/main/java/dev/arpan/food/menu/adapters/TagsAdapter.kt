package dev.arpan.food.menu.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.arpan.food.menu.R

class TagsAdapter : RecyclerView.Adapter<TagsAdapter.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    var data: List<String> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_tag, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount() = data.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: String) {
            (itemView as TextView).text = item
        }
    }
}

@BindingAdapter("tags")
fun RecyclerView.bindTags(tags: List<String>?) {
    var pagerAdapter = adapter as TagsAdapter?
    if (pagerAdapter == null) {
        pagerAdapter = TagsAdapter()
        adapter = pagerAdapter
        setHasFixedSize(true)
    }
    tags?.let {
        pagerAdapter.data = tags
    }
}
