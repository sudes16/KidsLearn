package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.LetterItem
import com.example.data.LetterTemplates
import com.example.data.database.AppDatabase
import com.example.data.database.LetterProgress
import com.example.data.database.ProgressRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.sqrt

class LetterViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ProgressRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ProgressRepository(database.progressDao())
    }

    // Reactively stream child progress and high scores from DB
    val allProgress: StateFlow<List<LetterProgress>> = repository.allProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val highScores: StateFlow<List<com.example.data.database.GameScore>> = repository.highScores
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // High Scores per game type
    val feedHighScore: StateFlow<Int> = repository.getHighScoreForGame("FEED_ANIMAL")
        .map { it?.score ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val popHighScore: StateFlow<Int> = repository.getHighScoreForGame("BALLOON_POP")
        .map { it?.score ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // --- Tracing Screen State ---
    private val _selectedLetter = MutableStateFlow<LetterItem>(LetterTemplates.list[0])
    val selectedLetter: StateFlow<LetterItem> = _selectedLetter.asStateFlow()

    private val _isUppercaseMode = MutableStateFlow(true)
    val isUppercaseMode: StateFlow<Boolean> = _isUppercaseMode.asStateFlow()

    private val _isNumbersMode = MutableStateFlow(false)
    val isNumbersMode: StateFlow<Boolean> = _isNumbersMode.asStateFlow()

    // Interactive Tracing progress
    private val _currentStrokeIndex = MutableStateFlow(0)
    val currentStrokeIndex: StateFlow<Int> = _currentStrokeIndex.asStateFlow()

    private val _currentPointIndex = MutableStateFlow(0) // Point index currently being targeted inside current stroke
    val currentPointIndex: StateFlow<Int> = _currentPointIndex.asStateFlow()

    // Trace path successfully drawn by the child so far (points they connected)
    private val _tracedPoints = MutableStateFlow<List<Offset>>(emptyList())
    val tracedPoints: StateFlow<List<Offset>> = _tracedPoints.asStateFlow()

    // Free drawing trail just for nice visual pointer feedback
    private val _touchTrail = MutableStateFlow<List<Offset>>(emptyList())
    val touchTrail: StateFlow<List<Offset>> = _touchTrail.asStateFlow()

    private val _traceCompleted = MutableStateFlow(false)
    val traceCompleted: StateFlow<Boolean> = _traceCompleted.asStateFlow()

    private val _showConfetti = MutableStateFlow(false)
    val showConfetti: StateFlow<Boolean> = _showConfetti.asStateFlow()

    // --- GAME 1: Feed the Animal Screen State ---
    private val _feedAnimalTarget = MutableStateFlow<LetterItem>(LetterTemplates.list[0])
    val feedAnimalTarget: StateFlow<LetterItem> = _feedAnimalTarget.asStateFlow()

    private val _feedOptions = MutableStateFlow<List<Char>>(emptyList())
    val feedOptions: StateFlow<List<Char>> = _feedOptions.asStateFlow()

    private val _feedGameState = MutableStateFlow<FeedState>(FeedState.Question)
    val feedGameState: StateFlow<FeedState> = _feedGameState.asStateFlow()

    private val _feedScore = MutableStateFlow(0)
    val feedScore: StateFlow<Int> = _feedScore.asStateFlow()

    // --- GAME 2: Safari Balloon Pop State ---
    private val _popAnimalTarget = MutableStateFlow<LetterItem>(LetterTemplates.list[0])
    val popAnimalTarget: StateFlow<LetterItem> = _popAnimalTarget.asStateFlow()

    private val _balloonOptions = MutableStateFlow<List<Char>>(emptyList())
    val balloonOptions: StateFlow<List<Char>> = _balloonOptions.asStateFlow()

    // Track which balloon index has been popped
    private val _poppedBalloons = MutableStateFlow<Set<Int>>(emptySet())
    val poppedBalloons: StateFlow<Set<Int>> = _poppedBalloons.asStateFlow()

    private val _popGameState = MutableStateFlow<PopState>(PopState.Question)
    val popGameState: StateFlow<PopState> = _popGameState.asStateFlow()

    private val _popScore = MutableStateFlow(0)
    val popScore: StateFlow<Int> = _popScore.asStateFlow()

    init {
        // Prepare initial states of the games
        generateNewFeedRound()
        generateNewPopRound()
    }

    // Set active letter for tracing
    fun selectLetter(item: LetterItem) {
        _selectedLetter.value = item
        resetTracing()
    }

    fun toggleUppercaseMode(upper: Boolean) {
        _isUppercaseMode.value = upper
        resetTracing()
    }

    fun toggleNumbersMode(enabled: Boolean) {
        _isNumbersMode.value = enabled
        _selectedLetter.value = if (enabled) LetterTemplates.numberList[0] else LetterTemplates.list[0]
        resetTracing()
    }

    fun resetTracing() {
        _currentStrokeIndex.value = 0
        _currentPointIndex.value = 0
        _tracedPoints.value = emptyList()
        _touchTrail.value = emptyList()
        _traceCompleted.value = false
        _showConfetti.value = false
    }

    // Tracing touch input handler
    fun onTracingTouch(touchOffset: Offset, canvasWidth: Float, canvasHeight: Float) {
        if (_traceCompleted.value || canvasWidth <= 0 || canvasHeight <= 0) return

        // Normalize touch offset
        val nx = touchOffset.x / canvasWidth
        val ny = touchOffset.y / canvasHeight
        val touchNorm = Offset(nx, ny)

        // Add to touch trail for soft drawing visual paths
        _touchTrail.value = _touchTrail.value + touchOffset

        // Get expected stroke points
        val activeLetter = _selectedLetter.value
        val activeStrokes = if (_isUppercaseMode.value) activeLetter.uppercaseStrokes else activeLetter.lowercaseStrokes
        if (_currentStrokeIndex.value >= activeStrokes.size) return

        val currentStroke = activeStrokes[_currentStrokeIndex.value]
        val targetPointIndex = _currentPointIndex.value
        if (targetPointIndex >= currentStroke.points.size) return

        val targetPoint = currentStroke.points[targetPointIndex]

        // Calculate Euclidean distance in normalized space
        val dist = sqrt((nx - targetPoint.x) * (nx - targetPoint.x) + (ny - targetPoint.y) * (ny - targetPoint.y))

        // Kid friendly range threshold (approx 14% of canvas coordinate distance, extremely forgiving and fun!)
        val hitThreshold = 0.14f
        if (dist <= hitThreshold) {
            // Child traced the targeted point!
            // Append target coordinate to success trail (re-mapped to pixels)
            val pxOffset = Offset(targetPoint.x * canvasWidth, targetPoint.y * canvasHeight)
            _tracedPoints.value = _tracedPoints.value + pxOffset

            _currentPointIndex.value = targetPointIndex + 1

            // Check if entire current stroke is done
            if (_currentPointIndex.value >= currentStroke.points.size) {
                // Move to next stroke
                val nextStrokeIdx = _currentStrokeIndex.value + 1
                if (nextStrokeIdx >= activeStrokes.size) {
                    // ALL STROKES COMPLETED!
                    _traceCompleted.value = true
                    _showConfetti.value = true
                    saveTracingProgress()
                } else {
                    _currentStrokeIndex.value = nextStrokeIdx
                    _currentPointIndex.value = 0
                }
            }
        }
    }

    fun onTracingTouchEnded() {
        // Clear touch pointer trail upon release, but preserve correct traced dots
        _touchTrail.value = emptyList()
    }

    private fun saveTracingProgress() {
        val letterStr = _selectedLetter.value.char.toString()
        val isUpper = _isUppercaseMode.value
        val isNumMode = _isNumbersMode.value
        viewModelScope.launch {
            val existing = repository.getProgressForLetter(letterStr)
            val newProgress = if (existing != null) {
                val upTraced = if (isNumMode || isUpper) true else existing.uppercaseTraced
                val lowTraced = if (isNumMode || !isUpper) true else existing.lowercaseTraced
                // Stars logic: 1 star if upper, 1 if lower, 3 stars if BOTH are mastered!
                val totalStars = if (upTraced && lowTraced) 3 else 1
                existing.copy(
                    uppercaseTraced = upTraced,
                    lowercaseTraced = lowTraced,
                    stars = totalStars
                )
            } else {
                LetterProgress(
                    letter = letterStr,
                    uppercaseTraced = isNumMode || isUpper,
                    lowercaseTraced = isNumMode || !isUpper,
                    stars = if (isNumMode) 3 else 1
                )
            }
            repository.updateProgress(newProgress)
        }
    }

    // --- GAME 1: Feed the Animal logic ---
    fun selectFeedOption(selectedChar: Char) {
        val targetChar = _feedAnimalTarget.value.char
        if (_feedGameState.value != FeedState.Question) return

        if (selectedChar.equals(targetChar, ignoreCase = true)) {
            // Correct matching answer!
            _feedGameState.value = FeedState.Correct
            _feedScore.value += 10
            viewModelScope.launch {
                delay(2000)
                generateNewFeedRound()
            }
        } else {
            // Wrong dish
            _feedGameState.value = FeedState.Wrong(selectedChar)
            viewModelScope.launch {
                delay(1200)
                _feedGameState.value = FeedState.Question // Reset so they can choose again
            }
        }
    }

    fun generateNewFeedRound() {
        // Save current high score if needed
        if (_feedScore.value > 0 && _feedGameState.value == FeedState.Correct) {
            // Save in parallel
            saveScore("FEED_ANIMAL", _feedScore.value)
        }

        val list = LetterTemplates.list
        val randomTarget = list.random()
        _feedAnimalTarget.value = randomTarget

        // Generate 3 choices (1 correct lowercase, 2 other unique random lowercase)
        val correctChoice = randomTarget.char.lowercaseChar()
        val wrongChoices = list.filter { it.char != randomTarget.char }
            .shuffled()
            .take(2)
            .map { it.char.lowercaseChar() }

        _feedOptions.value = (wrongChoices + correctChoice).shuffled()
        _feedGameState.value = FeedState.Question
    }

    fun resetFeedGame() {
        if (_feedScore.value > 0) {
            saveScore("FEED_ANIMAL", _feedScore.value)
        }
        _feedScore.value = 0
        generateNewFeedRound()
    }

    // --- GAME 2: Safari Balloon Pop logic ---
    fun popBalloon(index: Int, balloonChar: Char) {
        if (_popGameState.value != PopState.Question) return
        if (_poppedBalloons.value.contains(index)) return

        val targetChar = _popAnimalTarget.value.char

        if (balloonChar.equals(targetChar, ignoreCase = true)) {
            // Correct match balloon pop!
            _poppedBalloons.value = _poppedBalloons.value + index
            _popGameState.value = PopState.Correct
            _popScore.value += 15
            viewModelScope.launch {
                delay(2000)
                generateNewPopRound()
            }
        } else {
            // Pop wrong balloon: pops but gives mistake feedback
            _poppedBalloons.value = _poppedBalloons.value + index
            _popGameState.value = PopState.Wrong(balloonChar)
            viewModelScope.launch {
                delay(1200)
                _popGameState.value = PopState.Question
            }
        }
    }

    fun generateNewPopRound() {
        if (_popScore.value > 0 && _popGameState.value == PopState.Correct) {
            saveScore("BALLOON_POP", _popScore.value)
        }

        val list = LetterTemplates.list
        val randomTarget = list.random()
        _popAnimalTarget.value = randomTarget
        _poppedBalloons.value = emptySet()

        // Generate 5 balloons (one is the correct uppercase letter, others are random uppercase)
        val correctChoice = randomTarget.char.uppercaseChar()
        val wrongChoices = list.filter { it.char != randomTarget.char }
            .shuffled()
            .take(4)
            .map { it.char.uppercaseChar() }

        _balloonOptions.value = (wrongChoices + correctChoice).shuffled()
        _popGameState.value = PopState.Question
    }

    fun resetPopGame() {
        if (_popScore.value > 0) {
            saveScore("BALLOON_POP", _popScore.value)
        }
        _popScore.value = 0
        generateNewPopRound()
    }

    private fun saveScore(gameType: String, score: Int) {
        viewModelScope.launch {
            repository.insertScore(gameType, score)
        }
    }

    fun clearAllProgress() {
        viewModelScope.launch {
            repository.resetAllProgress()
            resetFeedGame()
            resetPopGame()
        }
    }
}

sealed interface FeedState {
    object Question : FeedState
    object Correct : FeedState
    data class Wrong(val letterChosen: Char) : FeedState
}

sealed interface PopState {
    object Question : PopState
    object Correct : PopState
    data class Wrong(val letterChosen: Char) : PopState
}
