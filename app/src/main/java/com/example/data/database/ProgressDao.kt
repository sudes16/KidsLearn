package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {
    @Query("SELECT * FROM letter_progress ORDER BY letter ASC")
    fun getAllProgress(): Flow<List<LetterProgress>>

    @Query("SELECT * FROM letter_progress WHERE letter = :letter LIMIT 1")
    suspend fun getProgressForLetter(letter: String): LetterProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: LetterProgress)

    @Query("SELECT * FROM game_scores ORDER BY score DESC LIMIT 10")
    fun getHighScores(): Flow<List<GameScore>>

    @Query("SELECT * FROM game_scores WHERE gameType = :gameType ORDER BY score DESC LIMIT 1")
    fun getHighScoreForGame(gameType: String): Flow<GameScore?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: GameScore)

    @Query("DELETE FROM letter_progress")
    suspend fun resetProgress()
}
