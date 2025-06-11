package com.example.orderfood.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.orderfood.entity.Food

@Dao
interface FoodDao {
    @Insert
    suspend fun insert(food: Food)
    @Query("SELECT * FROM food_table WHERE name LIKE :query OR description LIKE :query")
    suspend fun searchFoods(query: String): List<Food>
    @Query("SELECT * FROM food_table WHERE businessId = :businessId")
    suspend fun getFoodsByBusinessId(businessId: String): List<Food>

    @Query("SELECT * FROM food_table WHERE foodId = :foodId")
    suspend fun getFoodById(foodId: Long): Food?
    @Query("SELECT * FROM food_table")
    suspend fun getAllFoods(): List<Food>

    @Query("SELECT COUNT(*) FROM food_table")
    suspend fun getFoodCount(): Int
    // 添加更新方法
    @Update
    suspend fun update(food: Food)
    @Query("SELECT * FROM food_table WHERE businessId = :businessId AND name LIKE :namePattern")
    suspend fun getFoodsByBusinessIdAndName(businessId: String, namePattern: String): List<Food>
    @Query("DELETE FROM food_table WHERE foodId = :id")
    fun deleteById(id: Long)
}