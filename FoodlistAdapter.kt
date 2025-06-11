package com.example.orderfood.activity.man.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.orderfood.R
import com.example.orderfood.activity.man.ManageManUpdateFoodActivity
import com.example.orderfood.model.FoodItem
import java.io.File

class FoodListAdapter(
    private val foodList: List<FoodItem>,
    private val onItemClick: (FoodItem) -> Unit
) : RecyclerView.Adapter<FoodListAdapter.FoodViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_food, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val foodItem = foodList[position]
        holder.bind(foodItem)

        // 设置点击事件
        holder.itemView.setOnClickListener {
            // 启动修改商品信息的 Activity
            val context = holder.itemView.context
            val intent = Intent(context, ManageManUpdateFoodActivity::class.java).apply {
                // 传递商品信息
                putExtra("food_id", foodItem.id)
                putExtra("food_name", foodItem.name)
                putExtra("food_price", foodItem.price)
                putExtra("food_description", foodItem.description)
                putExtra("food_imageUrl", foodItem.imageUrl)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = foodList.size

    inner class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val foodImage: ImageView = itemView.findViewById(R.id.ivFoodImage)
        private val foodName: TextView = itemView.findViewById(R.id.tvFoodName)
        private val foodDescription: TextView = itemView.findViewById(R.id.tvFoodDescription)
        private val foodPrice: TextView = itemView.findViewById(R.id.tvFoodPrice)

        fun bind(foodItem: FoodItem) {
            // 获取当前视图的上下文
            val context = itemView.context

            // 处理图片路径
            val imagePath = resolveImagePath(foodItem.imageUrl, context)

            // 使用 Glide 加载图片
            loadImage(imagePath, context)

            // 设置文本内容
            foodName.text = foodItem.name
            foodDescription.text = foodItem.description
            foodPrice.text = "¥${foodItem.price}"
        }

        private fun resolveImagePath(imageUrl: String?, context: Context): Any? {
            return when {
                imageUrl == null -> null
                imageUrl.startsWith("http") -> imageUrl
                imageUrl.startsWith("content://") -> imageUrl
                else -> {
                    val file = File(context.filesDir, imageUrl)
                    if (file.exists()) file else null
                }
            }
        }

        private fun loadImage(imagePath: Any?, context: Context) {
            Glide.with(context)
                .load(imagePath ?: R.drawable.cannottupain) // 如果路径无效，使用错误图片
                .placeholder(R.drawable.k)
                .error(R.drawable.cannottupain)
                .into(foodImage)
        }
    }
}