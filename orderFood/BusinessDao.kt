package com.example.orderfood.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.orderfood.entity.Business
// BusinessDao.kt
@Dao
interface BusinessDao {
    @Query("SELECT * FROM businesses WHERE business_id = :businessId")
    suspend fun getBusinessById(businessId: String): Business?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(business: Business)

    @Query("SELECT COUNT(*) FROM businesses WHERE business_id = :businessId")
    suspend fun businessIdExists(businessId: String): Int
    @Update
    suspend fun update(business: Business)
    @Delete
    suspend fun delete(business: Business): Int

    // 新增：按 ID 删除
    @Query("DELETE FROM businesses WHERE business_id = :businessId")
    suspend fun deleteByBusinessId(businessId: String): Int
}