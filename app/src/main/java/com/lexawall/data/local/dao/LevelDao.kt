package com.lexawall.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.lexawall.data.local.entities.LevelEntity

@Dao
interface LevelDao {
    @Query("SELECT * FROM level_table WHERE id = 1 LIMIT 1")
    suspend fun getLevelOrNull(): LevelEntity?

    @Transaction
    suspend fun getLevel(): LevelEntity {
        return getLevelOrNull() ?: LevelEntity().also { insertOrReplace(it) }
    }

    @Update
    suspend fun updateLevel(entity: LevelEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(entity: LevelEntity)
}
