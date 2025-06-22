package com.example.orderfood.activity.user.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.orderfood.R
import com.example.orderfood.entity.Food

class FoodAdapter(
    private var foodList: MutableList<Food> = mutableListOf(),
    // 将回调改为可空类型，避免lateinit问题
    private var onAddClick: ((Food) -> Unit)? = null
) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    inner class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.food_name)
        val price: TextView = itemView.findViewById(R.id.food_price)
        val description: TextView = itemView.findViewById(R.id.food_description)
        val image: ImageView = itemView.findViewById(R.id.food_image)
        val addButton: View = itemView.findViewById(R.id.add_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_food, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val food = foodList[position]
        holder.name.text = food.name
        holder.price.text = "¥%.2f".format(food.price)
        holder.description.text = food.description

        // 改进的图片加载逻辑
        if (!food.imageUrl.isNullOrEmpty()) {
            val context = holder.itemView.context
            val fullPath = "file://${context.filesDir}/${food.imageUrl}"

            Glide.with(context)
                .load(fullPath)
                .placeholder(R.drawable.upimg)
                .error(R.drawable.cannottupain)
                .into(holder.image)
        } else {
            holder.image.setImageResource(R.drawable.cannottupain)
        }

        holder.addButton.setOnClickListener {
            // 使用安全调用，避免空指针异常
            onAddClick?.invoke(food)
        }
    }

    override fun getItemCount() = foodList.size

    // 优化列表更新
    fun updateList(newList: List<Food>) {
        foodList.clear()
        foodList.addAll(newList)
        notifyDataSetChanged()
    }

    // 添加数据的方法
    fun addData(newData: List<Food>) {
        val startPosition = foodList.size
        foodList.addAll(newData)
        notifyItemRangeInserted(startPosition, newData.size)
    }

    // 清空数据的方法
    fun clearData() {
        val size = foodList.size
        foodList.clear()
        notifyItemRangeRemoved(0, size)
    }

    // 提供设置回调的公共方法
    fun setOnAddClickListener(listener: (Food) -> Unit) {
        onAddClick = listener
    }
}