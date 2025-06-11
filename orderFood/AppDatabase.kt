package com.example.orderfood.dao


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.orderfood.entity.Business
import com.example.orderfood.entity.Food
import com.example.orderfood.entity.Order
import com.example.orderfood.entity.OrderDetail
import com.example.orderfood.entity.User

@Database(
    entities = [
        Business::class,
        User::class,
        Food::class,
        Order::class,          // 新增订单表
        OrderDetail::class     // 新增订单详情表
    ],
    version = 6,               // 版本号升级到4
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun businessDao(): BusinessDao
    abstract fun userDao(): UserDao
    abstract fun foodDao(): FoodDao
    abstract fun orderDao(): OrderDao          // 新增订单DAO
    abstract fun orderDetailDao(): OrderDetailDao // 新增订单详情DAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "order_food_database"
                )
                    .fallbackToDestructiveMigration()  // 处理版本升级
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}