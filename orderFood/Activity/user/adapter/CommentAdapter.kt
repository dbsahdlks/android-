package com.example.orderfood.activity.user.adapter
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.orderfood.R
import com.example.orderfood.dao.UserDao
import com.example.orderfood.entity.Comment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.util.Locale
import java.text.SimpleDateFormat
class CommentAdapter(
    private val context: Context,
    private val userDao: UserDao
) : ListAdapter<Comment, CommentAdapter.CommentViewHolder>(CommentDiffCallback()) {

    // 事件监听器接口
    interface OnCommentActionListener {
        fun onDeleteComment(position: Int, commentId: Long)
        fun onReplyComment(position: Int, commentId: Long)
        fun onImageClick(imageUrl: String) // 新增图片点击事件
    }

    private var listener: OnCommentActionListener? = null
    private val userAvatarMap = mutableMapOf<String, String>() // 缓存用户头像
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    fun setOnCommentActionListener(listener: OnCommentActionListener) {
        this.listener = listener
    }
    // 定义点击事件接口
    private var onItemClickListener: ((Comment) -> Unit)? = null

    // 设置点击监听器的方法
    fun setOnItemClickListener(listener: (Comment) -> Unit) {
        onItemClickListener = listener
    }
    // ViewHolder 类
    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val avatarImg = itemView.findViewById<ImageView>(R.id.man_my_comment_tx)
        val nameTxt = itemView.findViewById<TextView>(R.id.man_my_comment_name)
        val timeTxt = itemView.findViewById<TextView>(R.id.man_my_comment_time)
        val contentTxt = itemView.findViewById<TextView>(R.id.man_my_comment_content)
        val imageView = itemView.findViewById<ImageView>(R.id.man_my_comment_img)
        val deleteBtn = itemView.findViewById<Button>(R.id.man_my_comment_btn_delete)

        val starViews = arrayOf(
            itemView.findViewById<ImageView>(R.id.star1),
            itemView.findViewById<ImageView>(R.id.star2),
            itemView.findViewById<ImageView>(R.id.star3),
            itemView.findViewById<ImageView>(R.id.star4),
            itemView.findViewById<ImageView>(R.id.star5)
        )

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = getItem(position)

        // 绑定数据
        holder.nameTxt.text = comment.userName ?: "匿名用户"
        holder.timeTxt.text = formatCommentTime(comment.commentTime)
        holder.contentTxt.text = comment.content

        // 设置评分
        setRatingStars(holder.starViews, comment.rating)

        // 设置用户头像
        loadUserAvatar(comment.userId, holder.avatarImg)

        // 设置评论图片
        if (!comment.imagePath.isNullOrEmpty()) {
            Glide.with(context)
                .load(comment.imagePath)
                .placeholder(R.drawable.upimg)
                .error(R.drawable.upimg)
                .centerCrop()
                .into(holder.imageView)
            holder.imageView.visibility = View.VISIBLE

            // 添加图片点击事件
            holder.imageView.setOnClickListener {
                listener?.onImageClick(comment.imagePath)
            }
        } else {
            holder.imageView.visibility = View.GONE
        }

        // 设置按钮点击事件
        holder.deleteBtn.setOnClickListener {
            listener?.onDeleteComment(position, comment.commentId)
        }

    }

    private fun loadUserAvatar(userId: String, avatarImageView: ImageView) {
        // 先检查缓存
        if (userAvatarMap.containsKey(userId)) {
            Glide.with(context)
                .load(userAvatarMap[userId])
                .circleCrop() // 圆形头像
                .placeholder(R.drawable.upimg)
                .error(R.drawable.cannottupain)
                .into(avatarImageView)
            return
        }

        // 异步获取用户头像
        coroutineScope.launch {
            val user = withContext(Dispatchers.IO) {
                userDao.getUserById(userId)
            }

            if (user != null) {
                userAvatarMap[userId] = user.imagePath // 缓存头像URL
                Glide.with(context)
                    .load(user.imagePath)
                    .circleCrop() // 圆形头像
                    .placeholder(R.drawable.upimg)
                    .error(R.drawable.cannottupain)
                    .into(avatarImageView)
            }
        }
    }

    private fun setRatingStars(starViews: Array<ImageView>, rating: Int) {
        starViews.forEach { it.setImageResource(R.drawable.wx) }
        for (i in 0 until rating.coerceAtMost(starViews.size)) {
            starViews[i].setImageResource(R.drawable.fullstar)
        }
    }

    private fun formatCommentTime(timestamp: Long): String {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return format.format(Date(timestamp))
    }
}

// DiffUtil 回调，用于高效更新列表
class CommentDiffCallback : DiffUtil.ItemCallback<Comment>() {
    override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
        return oldItem.commentId == newItem.commentId
    }

    override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
        return oldItem == newItem
    }
}