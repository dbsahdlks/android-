package com.example.orderfood.activity.user.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.orderfood.R
import com.example.orderfood.entity.Business

class BusinessAdapter(
    private var businessList: MutableList<Business>,
    private val onItemClick: (Business) -> Unit
) : RecyclerView.Adapter<BusinessAdapter.BusinessViewHolder>() {

    inner class BusinessViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.business_name)
        val description: TextView = itemView.findViewById(R.id.business_description)
        val type: TextView = itemView.findViewById(R.id.business_type)
        val image: ImageView = itemView.findViewById(R.id.business_image)

        init {
            itemView.setOnClickListener {
                onItemClick(businessList[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusinessViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_business, parent, false)
        return BusinessViewHolder(view)
    }

    override fun onBindViewHolder(holder: BusinessViewHolder, position: Int) {
        val business = businessList[position]
        holder.name.text = business.name
        holder.description.text = business.description
        holder.type.text = business.type

        // 加载图片（关键修改：拼接完整路径并添加file://前缀）
        if (!business.imagePath.isNullOrEmpty()) {
            val context = holder.itemView.context
            val fullPath = "file://${context.filesDir}/${business.imagePath}"

            Glide.with(context)
                .load(fullPath)
                .placeholder(R.drawable.upimg)  // 加载中显示的占位图
                .error(R.drawable.cannottupain)   // 加载失败显示的错误图（可选）
                .into(holder.image)
        }
    }

    override fun getItemCount() = businessList.size

    fun updateList(newList: List<Business>) {
        businessList.clear()
        businessList.addAll(newList)
        notifyDataSetChanged()
    }
}