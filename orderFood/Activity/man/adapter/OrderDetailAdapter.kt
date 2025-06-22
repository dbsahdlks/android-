package com.example.orderfood.activity.man.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.orderfood.R
import com.example.orderfood.entity.OrderDetail

class OrderDetailAdapter(
    private val details: List<OrderDetail>
) : RecyclerView.Adapter<OrderDetailAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val foodName: TextView = view.findViewById(R.id.foodName)
        val foodQuantity: TextView = view.findViewById(R.id.foodQuantity)
        val foodPrice: TextView = view.findViewById(R.id.foodPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_detail, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val detail = details[position]
        holder.foodName.text = detail.foodName
        holder.foodQuantity.text = "x${detail.quantity}"
        holder.foodPrice.text = "¥${detail.foodPrice}"
    }

    override fun getItemCount() = details.size
}