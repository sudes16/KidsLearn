package com.example.data.database

import kotlinx.coroutines.flow.Flow

class ProgressRepository(private val progressDao: ProgressDao) {
    val allProgress: Flow<List<LetterProgress>> = progressDao.getAllProgress()
    val highScores: Flow<List<GameScore>> = progressDao.getHighScores()

    fun getHighScoreForGame(gameType: String): Flow<GameScore?> {
        return progressDao.getHighScoreForGame(gameType)
    }

    suspend fun getProgressForLetter(letter: String): LetterProgress? {
        return progressDao.getProgressForLetter(letter)
    }

    suspend fun updateProgress(progress: LetterProgress) {
        progressDao.insertProgress(progress)
    }

    suspend fun insertScore(gameType: String, score: Int) {
        progressDao.insertScore(GameScore(gameType = gameType, score = score))
    }

    suspend fun resetAllProgress() {
        progressDao.resetProgress()
    }
}
