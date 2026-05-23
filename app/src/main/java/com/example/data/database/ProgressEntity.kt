package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "letter_progress")
data class LetterProgress(
    @PrimaryKey val letter: String, // String representation of the letter, e.g., "A", "B"
    val uppercaseTraced: Boolean = false,
    val lowercaseTraced: Boolean = false,
    val stars: Int = 0 // Star rating (0 to 3)
)

@Entity(tableName = "game_scores")
data class GameScore(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val gameType: String, // "FEED_ANIMAL" or "BALLOON_POP"
    val score: Int,
    val timestamp: Long = System.currentTimeMillis()
)
