package com.example.orderfood.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.orderfood.entity.Business
import com.example.orderfood.entity.Food
import com.example.orderfood.entity.Order
import com.example.orderfood.entity.OrderDetail
import com.example.orderfood.entity.User
import com.example.orderfood.entity.Comment
import com.example.orderfood.entity.Reply

@Database(
    entities = [
        Business::class,
        User::class,
        Food::class,
        Order::class,
        OrderDetail::class,
        Comment::class,
        Reply::class
    ],
    version = 13, // 升级版本号到10
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun businessDao(): BusinessDao
    abstract fun userDao(): UserDao
    abstract fun foodDao(): FoodDao
    abstract fun orderDao(): OrderDao
    abstract fun orderDetailDao(): OrderDetailDao
    abstract fun commentDao(): CommentDao
    abstract fun replyDao():ReplyDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // 版本9到10的迁移：更新表结构
        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 1. 更新User表 - 添加代理主键
                database.execSQL("""
                    CREATE TABLE new_user_table (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id TEXT NOT NULL,
                        password TEXT NOT NULL,
                        name TEXT,
                        sex TEXT,
                        address TEXT,
                        phone TEXT,
                        image_path TEXT,
                        UNIQUE(user_id)
                    )
                """)

                // 迁移旧用户数据
                database.execSQL("""
                    INSERT INTO new_user_table (user_id, password, name, sex, address, phone, image_path)
                    SELECT user_id, password, name, sex, address, phone, image_path 
                    FROM user_table
                """)

                database.execSQL("DROP TABLE user_table")
                database.execSQL("ALTER TABLE new_user_table RENAME TO user_table")

                // 2. 更新Business表 - 添加代理主键
                database.execSQL("""
                    CREATE TABLE new_businesses (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        business_id TEXT NOT NULL,
                        password TEXT NOT NULL,
                        name TEXT,
                        description TEXT,
                        type TEXT,
                        image_path TEXT,
                        UNIQUE(business_id)
                    )
                """)

                // 迁移旧商家数据
                database.execSQL("""
                    INSERT INTO new_businesses (business_id, password, name, description, type, image_path)
                    SELECT business_id, password, name, description, type, image_path 
                    FROM businesses
                """)

                database.execSQL("DROP TABLE businesses")
                database.execSQL("ALTER TABLE new_businesses RENAME TO businesses")

                // 3. 更新Comment表 - 使用新的外键关系
                database.execSQL("""
                    CREATE TABLE new_comments (
                        commentId INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER NOT NULL,
                        business_id INTEGER NOT NULL,
                        content TEXT NOT NULL,
                        rating INTEGER NOT NULL CHECK(rating BETWEEN 1 AND 5),
                        commentTime INTEGER NOT NULL,
                        imagePath TEXT,
                        FOREIGN KEY (user_id) REFERENCES user_table(id) ON DELETE CASCADE,
                        FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE
                    )
                """)

                // 迁移旧评论数据（需要处理外键映射）
                database.execSQL("""
                    INSERT INTO new_comments (commentId, user_id, business_id, content, rating, commentTime, imagePath)
                    SELECT 
                        c.commentId,
                        u.id AS user_id,
                        b.id AS business_id,
                        c.content,
                        c.rating,
                        c.commentTime,
                        c.imagePath
                    FROM comments c
                    JOIN user_table u ON c.userId = u.user_id
                    JOIN businesses bu ON c.businessId = bu.business_id
                """)

                database.execSQL("DROP TABLE comments")
                database.execSQL("ALTER TABLE new_comments RENAME TO comments")

                // 4. 添加索引
                database.execSQL("CREATE INDEX idx_comments_business ON comments(business_id)")
                database.execSQL("CREATE INDEX idx_comments_user ON comments(user_id)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "order_food_database"
                )
                    .addMigrations(MIGRATION_9_10) // 使用新的迁移
                    .fallbackToDestructiveMigration() // 对于其他版本使用破坏性迁移
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}