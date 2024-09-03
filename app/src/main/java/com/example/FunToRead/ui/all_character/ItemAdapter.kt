//ui/all_character/ItemAdapter
package com.example.FunToRead.ui.all_character

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.FunToRead.R
import com.example.FunToRead.data.model.Item
import com.example.FunToRead.databinding.ItemLayoutBinding

// Adapter class for managing a list of items in a RecyclerView
class ItemAdapter(private var items: List<Item>, private val callBack: ItemListener) :
    RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    // Interface to handle item click and long click events
    interface ItemListener {
        fun onItemClicked(index: Int)
        fun onItemLongClicked(index: Int): Boolean
    }

    // ViewHolder class to represent each item view in the RecyclerView
    inner class ItemViewHolder(private val binding: ItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener, View.OnLongClickListener {

        init {
            // Set click listeners for the root view of the item layout
            binding.root.setOnClickListener(this)
            binding.root.setOnLongClickListener(this)
        }

        // Handle click event
        override fun onClick(v: View?) {
            callBack.onItemClicked(adapterPosition)
        }

        // Handle long click event
        override fun onLongClick(v: View?): Boolean {
            return callBack.onItemLongClicked(adapterPosition)
        }

        // Bind the item data to the views in the layout
        fun bind(item: Item) {
            binding.itemTitle.text = item.title

            // Load the item image using Glide, with a default image if none is provided
            if (item.photo == "default") {
                Glide.with(binding.root).load(R.drawable.default_pic).circleCrop().into(binding.itemImage)
            } else {
                Glide.with(binding.root).load(item.photo).circleCrop().into(binding.itemImage)
            }
        }
    }

    // Update the list of items and notify the adapter
    fun setItems(newItems: List<Item>) {
        items = newItems
        notifyDataSetChanged()
    }

    // Get the item at the specified position
    fun itemAt(position: Int): Item = items[position]

    // Inflate the item layout and create a ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder =
        ItemViewHolder(ItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    // Bind the item data to the ViewHolder
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    // Return the total number of items
    override fun getItemCount(): Int = items.size
}
