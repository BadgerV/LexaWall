package com.lexawall.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey
    val id: String,
    val word: String,
    val partOfSpeech: String,
    val cefrLevel: String,
    val definition: String,
    val exampleSentence: String,
    val notificationLine1: String,
    val notificationLine2: String,
    val pronunciationIpa: String?,
    val etymology: String?,
    val lexaSays: String? = null
)
