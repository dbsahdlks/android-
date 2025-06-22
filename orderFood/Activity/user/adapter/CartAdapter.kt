package com.example.orderfood.activity.user.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.orderfood.R
import com.example.orderfood.model.CartItem

class CartAdapter(
    private val cartItems: MutableList<CartItem>,
    private val onIncrease: (Int) -> Unit,
    private val onDecrease: (Int) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.cart_item_name)
        val price: TextView = itemView.findViewById(R.id.cart_item_price)
        val quantity: TextView = itemView.findViewById(R.id.cart_item_quantity)
        val image: ImageView = itemView.findViewById(R.id.cart_item_image)
        val decreaseButton: View = itemView.findViewById(R.id.decrease_button)
        val increaseButton: View = itemView.findViewById(R.id.increase_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItem = cartItems[position]
        val food = cartItem.food

        holder.name.text = food.name
        holder.price.text = "¥%.2f x ${cartItem.quantity}".format(food.price)
        holder.quantity.text = cartItem.quantity.toString()

        // 加载图片
        if (!food.imageUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(food.imageUrl)
                .placeholder(R.drawable.upimg)
                .into(holder.image)
        }

        holder.increaseButton.setOnClickListener {
            onIncrease(position)
        }

        holder.decreaseButton.setOnClickListener {
            onDecrease(position)
        }
    }

    override fun getItemCount() = cartItems.size
}