package com.lexawall.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lexawall.data.local.entities.WordEntity

@Dao
interface WordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(words: List<WordEntity>)

    @Query("SELECT * FROM words WHERE id = :id LIMIT 1")
    suspend fun getWordById(id: String): WordEntity?

    @Query("SELECT * FROM words WHERE cefrLevel = :cefrLevel")
    suspend fun getWordsByLevel(cefrLevel: String): List<WordEntity>

    @Query("SELECT COUNT(*) FROM words")
    suspend fun getWordCount(): Int

    @Query("SELECT * FROM words")
    suspend fun getAllWords(): List<WordEntity>

    @Query("SELECT * FROM words ORDER BY RANDOM() LIMIT :count")
    suspend fun getRandomWords(count: Int): List<WordEntity>

    @Query("SELECT * FROM words WHERE id IN (:ids)")
    suspend fun getWordsByIds(ids: List<String>): List<WordEntity>

    @Query("SELECT * FROM words WHERE id NOT IN (:excludeIds) ORDER BY RANDOM() LIMIT :count")
    suspend fun getWanderWords(excludeIds: List<String>, count: Int): List<WordEntity>

    @Query("DELETE FROM words")
    suspend fun clearAll()
}
