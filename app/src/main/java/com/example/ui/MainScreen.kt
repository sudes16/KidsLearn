package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke as CanvasStroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.LetterItem
import com.example.data.LetterTemplates
import com.example.ui.viewmodel.LetterViewModel
import com.example.ui.viewmodel.FeedState
import com.example.ui.viewmodel.PopState
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class AppTab(val icon: String, val label: String) {
    TRACE("✏️", "Trace & Learn"),
    FEED("🍲", "Feed Safari"),
    BALLOON("🎈", "Balloon Pop"),
    STICKERS("🏆", "Stickers")
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen(
    viewModel: LetterViewModel = viewModel()
) {
    var activeTab by remember { mutableStateOf(AppTab.TRACE) }
    val progressList by viewModel.allProgress.collectAsState()
    
    // Quick progress lookup map
    val progressMap = remember(progressList) {
        progressList.associateBy { it.letter }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            CustomBottomNavigationBar(
                currentTab = activeTab,
                onTabSelected = { activeTab = it }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(com.example.ui.theme.GeoBg)
                .padding(innerPadding)
        ) {
            // Elegant top branding bar
            HeaderSection()

            // Animated tab switches
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) with fadeOut(animationSpec = tween(220))
                },
                modifier = Modifier.weight(1f)
            ) { tab ->
                when (tab) {
                    AppTab.TRACE -> TraceTabContent(viewModel, progressMap)
                    AppTab.FEED -> FeedTabContent(viewModel)
                    AppTab.BALLOON -> BalloonTabContent(viewModel)
                    AppTab.STICKERS -> StickersTabContent(viewModel, progressMap)
                }
            }
        }
    }
}

@Composable
fun HeaderSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(45.dp)
                .background(com.example.ui.theme.GeoTertiary, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("🔤", fontSize = 24.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = "Alphabet Match & Trace",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = com.example.ui.theme.GeoOnBg
            )
            Text(
                text = "Learn pairs with animal friends!",
                fontSize = 12.sp,
                color = com.example.ui.theme.GeoMuted,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun CustomBottomNavigationBar(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .shadow(12.dp, shape = RoundedCornerShape(24.dp))
            .border(2.dp, com.example.ui.theme.GeoSecondary, RoundedCornerShape(24.dp)),
        color = com.example.ui.theme.GeoBottomNavBg,
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppTab.values().forEach { tab ->
                val isSelected = currentTab == tab
                val bgActive = if (isSelected) com.example.ui.theme.GeoSecondary else Color.Transparent
                val textColor = if (isSelected) com.example.ui.theme.GeoOnBg else com.example.ui.theme.GeoMuted
                
                Box(
                    modifier = Modifier
                        .testTag("nav_tab_${tab.name.lowercase()}")
                        .clip(RoundedCornerShape(18.dp))
                        .background(bgActive)
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = tab.icon, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        AnimatedVisibility(visible = isSelected) {
                            Text(
                                  text = tab.label,
                                  fontSize = 12.sp,
                                  fontWeight = FontWeight.Bold,
                                  color = textColor
                            )
                        }
                    }
                }
            }
        }
    }
}

// ------------------- TRACE TAB -------------------

@Composable
fun TraceTabContent(
    viewModel: LetterViewModel,
    progressMap: Map<String, com.example.data.database.LetterProgress>
) {
    val selectedLetter by viewModel.selectedLetter.collectAsState()
    val isUppercaseMode by viewModel.isUppercaseMode.collectAsState()
    val traceCompleted by viewModel.traceCompleted.collectAsState()
    val showConfetti by viewModel.showConfetti.collectAsState()

    val currentProgress = progressMap[selectedLetter.char.toString()]
    val starsCount = currentProgress?.stars ?: 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Horizontal letters slider
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(LetterTemplates.list) { item ->
                val isCurrent = item.char == selectedLetter.char
                val progress = progressMap[item.char.toString()]
                val starCount = progress?.stars ?: 0

                Card(
                    modifier = Modifier
                        .testTag("letter_scroller_${item.char}")
                        .width(60.dp)
                        .height(75.dp)
                        .clickable { viewModel.selectLetter(item) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrent) Color(item.themeColor) else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrent) 6.dp else 2.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        width = if (isCurrent) 3.dp else 1.dp,
                        color = if (isCurrent) Color.White else com.example.ui.theme.GeoSecondary
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${item.char}${item.char.lowercase()}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isCurrent) Color.White else com.example.ui.theme.GeoOnBg
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = item.animalEmoji, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Row {
                            repeat(3) { index ->
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = if (index < starCount) Color(0xFFFBBF24) else Color(0xFFE5E7EB),
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Active animal mascot card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.GeoTertiary),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp, com.example.ui.theme.GeoSecondary)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .shadow(1.dp, shape = RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = selectedLetter.animalEmoji, fontSize = 36.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "${selectedLetter.char} is for ${selectedLetter.animalName}!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = com.example.ui.theme.GeoPrimary
                    )
                    Text(
                        text = selectedLetter.animalFact,
                        fontSize = 12.sp,
                        color = com.example.ui.theme.GeoMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Toggle tracing mode (Capital Letter vs Small Letter)
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(com.example.ui.theme.GeoTertiary)
                .border(1.dp, com.example.ui.theme.GeoSecondary, RoundedCornerShape(16.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { viewModel.toggleUppercaseMode(true) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isUppercaseMode) com.example.ui.theme.GeoPrimary else Color.Transparent,
                    contentColor = if (isUppercaseMode) Color.White else com.example.ui.theme.GeoMuted
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = null,
                modifier = Modifier.testTag("toggle_uppercase")
            ) {
                Text("Capital: ${selectedLetter.char}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Button(
                onClick = { viewModel.toggleUppercaseMode(false) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isUppercaseMode) com.example.ui.theme.GeoPrimary else Color.Transparent,
                    contentColor = if (!isUppercaseMode) Color.White else com.example.ui.theme.GeoMuted
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = null,
                modifier = Modifier.testTag("toggle_lowercase")
            ) {
                Text("Small: ${selectedLetter.char.lowercase()}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tracing Board Canvas Box
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .shadow(12.dp, shape = RoundedCornerShape(28.dp))
                .border(4.dp, com.example.ui.theme.GeoPrimary, RoundedCornerShape(28.dp))
                .background(Color(0xFF1E3F20), RoundedCornerShape(28.dp)), // Deep forest green chalkboard
            contentAlignment = Alignment.Center
        ) {
            TracingCanvas(viewModel = viewModel)

            // Confetti explosion overlay
            ConfettiBurst(active = showConfetti)

            // Traced state celebratory pop
            if (traceCompleted) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "⭐️ STUPENDOUS! ⭐️",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = com.example.ui.theme.GeoTertiary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "You completed '${if (isUppercaseMode) selectedLetter.char else selectedLetter.char.lowercase()}'!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = selectedLetter.animalEmoji,
                            fontSize = 72.sp,
                            modifier = Modifier.scale(
                                rememberInfiniteTransition().animateFloat(
                                    initialValue = 0.9f,
                                    targetValue = 1.2f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(800, easing = FastOutSlowInEasing),
                                        repeatMode = RepeatMode.Reverse
                                    )
                                ).value
                            )
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { viewModel.resetTracing() },
                                colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.GeoMuted),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.testTag("trace_again_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Refresh, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Try Again", fontWeight = FontWeight.Bold)
                                }
                            }
                            Button(
                                onClick = {
                                    // Move to next letter automatically
                                    val currentIdx = LetterTemplates.list.indexOf(selectedLetter)
                                    val nextIdx = (currentIdx + 1) % LetterTemplates.list.size
                                    viewModel.selectLetter(LetterTemplates.list[nextIdx])
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.GeoPrimary),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.testTag("next_letter_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Next Letter", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.Filled.ArrowForward, contentDescription = null)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TracingCanvas(viewModel: LetterViewModel) {
    val activeLetter by viewModel.selectedLetter.collectAsState()
    val isUppercaseMode by viewModel.isUppercaseMode.collectAsState()
    
    val currentStrokeIndex by viewModel.currentStrokeIndex.collectAsState()
    val currentPointIndex by viewModel.currentPointIndex.collectAsState()
    val tracedPoints by viewModel.tracedPoints.collectAsState()
    val touchTrail by viewModel.touchTrail.collectAsState()
    val traceCompleted by viewModel.traceCompleted.collectAsState()

    val activeStrokes = if (isUppercaseMode) activeLetter.uppercaseStrokes else activeLetter.lowercaseStrokes

    // Pulse animation for upcoming target point of the stroke
    val infiniteTransition = rememberInfiniteTransition()
    val pulsingScale by infiniteTransition.animateFloat(
        initialValue = 11f,
        targetValue = 24f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    var canvasWidth by remember { mutableStateOf(0f) }
    var canvasHeight by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(isUppercaseMode, activeLetter, traceCompleted) {
                detectDragGestures(
                    onDragStart = { offset ->
                        viewModel.onTracingTouch(offset, canvasWidth, canvasHeight)
                    },
                    onDrag = { change, _ ->
                        viewModel.onTracingTouch(change.position, canvasWidth, canvasHeight)
                    },
                    onDragEnd = {
                        viewModel.onTracingTouchEnded()
                    },
                    onDragCancel = {
                        viewModel.onTracingTouchEnded()
                    }
                )
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("interactive_chalkboard_canvas")
        ) {
            canvasWidth = size.width
            canvasHeight = size.height

            // 1. Draw ALL template strokes of the current letter as dotted gray outlines underneath
            activeStrokes.forEachIndexed { sIdx, stroke ->
                // Draw template stroke
                val strokePath = androidx.compose.ui.graphics.Path().apply {
                    if (stroke.points.isNotEmpty()) {
                        moveTo(stroke.points[0].x * canvasWidth, stroke.points[0].y * canvasHeight)
                        for (i in 1 until stroke.points.size) {
                            lineTo(stroke.points[i].x * canvasWidth, stroke.points[i].y * canvasHeight)
                        }
                    }
                }
                
                // Uncompleted stroke colors: semi-transparent white/gray
                val strokeColor = if (sIdx < currentStrokeIndex) {
                    Color(activeLetter.themeColor) // Already traced paths gets colored!
                } else if (sIdx == currentStrokeIndex) {
                    Color.White.copy(alpha = 0.5f)
                } else {
                    Color.White.copy(alpha = 0.25f)
                }

                drawPath(
                    path = strokePath,
                    color = strokeColor,
                    style = CanvasStroke(
                        width = 24.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 25f), 0f)
                    )
                )

                // Optional: Draw guidelines/arrows for current stroke if not traced
                if (sIdx == currentStrokeIndex && stroke.points.size >= 2) {
                    // Draw guide arrows
                    for (i in 0 until stroke.points.size - 1) {
                        val p1 = stroke.points[i]
                        val p2 = stroke.points[i + 1]
                        val dx = (p2.x - p1.x) * canvasWidth
                        val dy = (p2.y - p1.y) * canvasHeight
                        
                        // Draw helper dots to teach direction
                        drawCircle(
                            color = Color(0xFFFDE68A).copy(alpha = 0.6f),
                            radius = 6.dp.toPx(),
                            center = Offset(p1.x * canvasWidth, p1.y * canvasHeight)
                        )
                    }
                }
            }

            // 2. Draw user traced points (Neon magic Crayon trail)
            if (tracedPoints.size >= 2) {
                val userPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(tracedPoints[0].x, tracedPoints[0].y)
                    for (i in 1 until tracedPoints.size) {
                        lineTo(tracedPoints[i].x, tracedPoints[i].y)
                    }
                }
                // Glowing rainbow candy glow
                drawPath(
                    path = userPath,
                    color = Color(0xFF10B981), // Solid cute emerald green trace
                    style = CanvasStroke(
                        width = 26.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )
            }

            // 3. Draw child finger touch trails slightly
            if (touchTrail.size >= 2) {
                val trailPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(touchTrail[0].x, touchTrail[0].y)
                    for (i in 1 until touchTrail.size) {
                        lineTo(touchTrail[i].x, touchTrail[i].y)
                    }
                }
                drawPath(
                    path = trailPath,
                    color = Color(0xFFA7F3D0).copy(alpha = 0.4f), // Soft brush feedback
                    style = CanvasStroke(
                        width = 20.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )
            }

            // 4. Highlight the CURRENT expected Target Dot to trace next
            if (currentStrokeIndex < activeStrokes.size) {
                val currentStroke = activeStrokes[currentStrokeIndex]
                if (currentPointIndex < currentStroke.points.size) {
                    val targetPt = currentStroke.points[currentPointIndex]
                    val pxX = targetPt.x * canvasWidth
                    val pxY = targetPt.y * canvasHeight

                    // Pulsing start helper
                    drawCircle(
                        color = Color(0xFFF59E0B).copy(alpha = 0.4f),
                        radius = pulsingScale.dp.toPx(),
                        center = Offset(pxX, pxY)
                    )
                    drawCircle(
                        color = Color(0xFFFBBF24), // High visibility guiding yellow
                        radius = 12.dp.toPx(),
                        center = Offset(pxX, pxY)
                    )
                    // Draw little hand/pencil inside target dot
                    drawCircle(
                        color = Color.White,
                        radius = 5.dp.toPx(),
                        center = Offset(pxX, pxY)
                    )
                }
            }
        }
        
        // Dynamic labels overlay
        if (!traceCompleted) {
            Text(
                text = "Trace carefully along the dots!",
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(14.dp)
            )
        }
    }
}

// ------------------- FEED GAME TAB -------------------

@Composable
fun FeedTabContent(viewModel: LetterViewModel) {
    val targetLetter by viewModel.feedAnimalTarget.collectAsState()
    val options by viewModel.feedOptions.collectAsState()
    val feedState by viewModel.feedGameState.collectAsState()
    val score by viewModel.feedScore.collectAsState()
    val highScore by viewModel.feedHighScore.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // High score header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.GeoSecondary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💯 Score: ", fontWeight = FontWeight.Bold, color = com.example.ui.theme.GeoOnBg)
                    Text(
                        text = "$score",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = com.example.ui.theme.GeoPrimary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🏆 Top Score: ", color = com.example.ui.theme.GeoMuted, fontSize = 12.sp)
                    Text(
                        text = "$highScore",
                        fontWeight = FontWeight.Bold,
                        color = com.example.ui.theme.GeoOnBg,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Large Mascot Panel (Animating response states)
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(2.dp, com.example.ui.theme.GeoSecondary),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Mascot animation bounce scale
                val animalScale = remember { Animatable(1f) }
                LaunchedEffect(feedState) {
                    if (feedState == FeedState.Correct) {
                        animalScale.animateTo(1.3f, animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy))
                        animalScale.animateTo(1f, animationSpec = spring())
                    }
                }

                Text(
                    text = when (feedState) {
                        FeedState.Question -> "Feed ${targetLetter.animalName}!"
                        FeedState.Correct -> "YUM! Delicious!"
                        is FeedState.Wrong -> "No, that's not it! Try again!"
                    },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = when (feedState) {
                        FeedState.Question -> com.example.ui.theme.GeoPrimary
                        FeedState.Correct -> com.example.ui.theme.GeoPrimary
                        is FeedState.Wrong -> Color(0xFFEF4444)
                    },
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Huge animated animal sticker
                Text(
                    text = targetLetter.animalEmoji,
                    fontSize = 110.sp,
                    modifier = Modifier.scale(animalScale.value)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .background(com.example.ui.theme.GeoTertiary, RoundedCornerShape(16.dp))
                        .border(1.dp, com.example.ui.theme.GeoSecondary, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = when (feedState) {
                            FeedState.Question -> "Give the hungry ${targetLetter.animalName} the small letter bowl matching CAPITAL '${targetLetter.char}'!"
                            FeedState.Correct -> "Splendid! You matched '${targetLetter.char}' with '${targetLetter.char.lowercase()}'!"
                            is FeedState.Wrong -> "Oops! That was bowl '${(feedState as FeedState.Wrong).letterChosen}'!"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = com.example.ui.theme.GeoMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Food matching letter bowls choices
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            options.forEach { optionChar ->
                Card(
                    modifier = Modifier
                        .testTag("feed_option_$optionChar")
                        .weight(1f)
                        .height(115.dp)
                        .clickable { viewModel.selectFeedOption(optionChar) },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(3.dp, com.example.ui.theme.GeoSecondary),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("🍲", fontSize = 28.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$optionChar",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = com.example.ui.theme.GeoOnBg
                        )
                    }
                }
            }
        }

        // Action controls
        Button(
            onClick = { viewModel.resetFeedGame() },
            colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.GeoMuted),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.testTag("reset_feed_game_button")
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Restart Points", fontWeight = FontWeight.Bold)
        }
    }
}

// ------------------- BALLOON POP GAME TAB -------------------

@Composable
fun BalloonTabContent(viewModel: LetterViewModel) {
    val targetLetter by viewModel.popAnimalTarget.collectAsState()
    val options by viewModel.balloonOptions.collectAsState()
    val poppedBalloons by viewModel.poppedBalloons.collectAsState()
    val popState by viewModel.popGameState.collectAsState()
    val score by viewModel.popScore.collectAsState()
    val highScore by viewModel.popHighScore.collectAsState()

    // Balloon distinct color templates
    val balloonColors = listOf(
        Color(0xFFFF8B94), Color(0xFFFFD3B6), Color(0xFFA8E6CF),
        Color(0xFFDCEDC1), Color(0xFFDCD6F7)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // High scores panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.GeoSecondary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎈 Pops: ", fontWeight = FontWeight.Bold, color = com.example.ui.theme.GeoOnBg)
                    Text(
                        text = "$score",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = com.example.ui.theme.GeoPrimary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🏆 Top Pops: ", color = com.example.ui.theme.GeoMuted, fontSize = 12.sp)
                    Text(
                        text = "$highScore",
                        fontWeight = FontWeight.Bold,
                        color = com.example.ui.theme.GeoOnBg,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Safari visual field (where balloons float and animal asks details)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            com.example.ui.theme.GeoBg, // Soft warm beige
                            com.example.ui.theme.GeoTertiary, // Soft light sage green
                        )
                    )
                )
                .border(2.dp, com.example.ui.theme.GeoSecondary, RoundedCornerShape(24.dp))
        ) {
            // Sun & Clouds background illustrations
            Text(
                text = "☀️",
                fontSize = 40.sp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            )
            Text(
                text = "☁️",
                fontSize = 32.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            )

            // Dynamic Helium option balloon floats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                options.forEachIndexed { sIdx, optionChar ->
                    val isPopped = poppedBalloons.contains(sIdx)

                    // Infinite Bobbing Floating animation
                    val infiniteTransition = rememberInfiniteTransition()
                    val bobOffset by infiniteTransition.animateFloat(
                        initialValue = -15f,
                        targetValue = 15f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200 + sIdx * 150, easing = SineIntensityEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )

                    // Popping scale shrinks to zero rapidly
                    val balloonScale = animateFloatAsState(
                        targetValue = if (isPopped) 0f else 1f,
                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                    )

                    if (balloonScale.value > 0.05f) {
                        Column(
                            modifier = Modifier
                                .testTag("balloon_$sIdx")
                                .offset(y = bobOffset.dp)
                                .scale(balloonScale.value)
                                .clickable { viewModel.popBalloon(sIdx, optionChar) },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(balloonColors[sIdx % balloonColors.size], CircleShape)
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$optionChar",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF1E293B)
                                )
                            }
                            // Balloon string
                            Canvas(modifier = Modifier.size(width = 2.dp, height = 24.dp)) {
                                drawLine(
                                    color = Color.White,
                                    start = Offset(size.width / 2, 0f),
                                    end = Offset(size.width / 2, size.height),
                                    strokeWidth = 2.dp.toPx()
                                )
                            }
                        }
                    } else {
                        // Explosion Splatters indicator
                        Box(
                            modifier = Modifier.size(60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("💥", fontSize = 36.sp)
                        }
                    }
                }
            }

            // Animal mascot asking at the bottom right
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    modifier = Modifier.widthIn(max = 240.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(2.dp, com.example.ui.theme.GeoSecondary)
                ) {
                    Text(
                        text = when (popState) {
                            PopState.Question -> "Find Capital '${targetLetter.char}' balloons for my small '${targetLetter.char.lowercase()}'!"
                            PopState.Correct -> "HOORAY! POP!"
                            is PopState.Wrong -> "No, that's letter '${(popState as PopState.Wrong).letterChosen}'!"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = com.example.ui.theme.GeoPrimary,
                        modifier = Modifier.padding(10.dp),
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = targetLetter.animalEmoji,
                    fontSize = 72.sp,
                    modifier = Modifier.scale(
                        if (popState == PopState.Correct) 1.25f else 1f
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Actions
        Button(
            onClick = { viewModel.resetPopGame() },
            colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.GeoMuted),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.testTag("reset_pop_game_button")
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Restart Balloon Pops", fontWeight = FontWeight.Bold)
        }
    }
}

// Sine wave easing for bobbings
val SineIntensityEasing = androidx.compose.animation.core.Easing { fraction ->
    sin(fraction * PI.toFloat() * 1.5f)
}

// ------------------- STICKERS COLLECTION TAB -------------------

@Composable
fun StickersTabContent(
    viewModel: LetterViewModel,
    progressMap: Map<String, com.example.data.database.LetterProgress>
) {
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Stats Banner
        val masteredLettersCount = remember(progressMap) {
            progressMap.values.count { it.uppercaseTraced && it.lowercaseTraced }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.GeoTertiary),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, com.example.ui.theme.GeoSecondary),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "My Sticker Book",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = com.example.ui.theme.GeoPrimary
                    )
                    Text(
                        text = "Master letter pairs to unlock stickers!",
                        fontSize = 11.sp,
                        color = com.example.ui.theme.GeoMuted
                    )
                }
                Text(
                    text = "$masteredLettersCount / 26 🏆",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = com.example.ui.theme.GeoPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Grid of 26 letters (unlocked vs locked stickers)
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(LetterTemplates.list) { item ->
                val progress = progressMap[item.char.toString()]
                val isUnlocked = progress != null && (progress.uppercaseTraced || progress.lowercaseTraced)
                val isFullyMastered = progress != null && progress.uppercaseTraced && progress.lowercaseTraced

                Card(
                    modifier = Modifier
                        .testTag("badge_item_${item.char}")
                        .aspectRatio(0.82f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUnlocked) Color.White else com.example.ui.theme.GeoBottomNavBg
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 2.dp else 0.dp),
                    border = BorderStroke(
                        width = if (isFullyMastered) 3.dp else 1.dp,
                        color = if (isFullyMastered) com.example.ui.theme.GeoPrimary else com.example.ui.theme.GeoSecondary
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (isUnlocked) {
                            // Display beautiful unlocked animal mascot sticker!
                            Text(text = item.animalEmoji, fontSize = 42.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.animalName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = com.example.ui.theme.GeoOnBg,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${item.char}${item.char.lowercase()}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = com.example.ui.theme.GeoPrimary
                            )
                            // Star rate indicators
                            Row {
                                repeat(3) { index ->
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = if (index < (progress?.stars ?: 0)) Color(0xFFFBBF24) else Color(0xFFE2E8F0),
                                        modifier = Modifier.size(11.dp)
                                    )
                                }
                            }
                        } else {
                            // Silhouette locked state
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = "Locked",
                                tint = com.example.ui.theme.GeoMuted,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${item.char} ${item.char.lowercase()}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = com.example.ui.theme.GeoMuted
                            )
                            Text(
                                text = "Locked",
                                fontSize = 10.sp,
                                color = com.example.ui.theme.GeoMuted
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Reset all progress action
        Button(
            onClick = { showResetDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.GeoMuted),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .testTag("reset_progress_system_button")
        ) {
            Icon(Icons.Filled.Delete, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Clear My Progress", fontWeight = FontWeight.Bold, color = Color.White)
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Sticker Book?", fontWeight = FontWeight.Black) },
            text = { Text("This will clean out all your earned master stars, high scores, and sticker unlocks. Are you sure?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllProgress()
                        showResetDialog = false
                    }
                ) {
                    Text("Yes, Start Over", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("No, Keep Progress")
                }
            }
        )
    }
}

// ------------------- CONFETTI BURST DRAWING -------------------

class ConfettiParticle(
    val x: Float,
    val y: Float,
    val color: Color,
    val radius: Float,
    val angle: Float,
    val speed: Float
)

@Composable
fun ConfettiBurst(active: Boolean) {
    if (!active) return

    val infiniteTransition = rememberInfiniteTransition()
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Generate fixed random particles
    val particles = remember {
        val list = mutableListOf<ConfettiParticle>()
        val colors = listOf(
            Color(0xFF60A5FA), Color(0xFFF87171), Color(0xFF34D399),
            Color(0xFFFBBF24), Color(0xFFC084FC), Color(0xFFF472B6)
        )
        repeat(40) {
            list.add(
                ConfettiParticle(
                    x = 0.5f,
                    y = 0.5f,
                    color = colors.random(),
                    radius = Random.nextInt(12, 28).toFloat(),
                    angle = Random.nextFloat() * 360f,
                    speed = Random.nextFloat() * 450f + 150f
                )
            )
        }
        list
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        particles.forEach { p ->
            val rad = p.angle * PI / 180f
            // Project offset outwards with speed * animated progress
            val dist = p.speed * progress
            val px = (p.x * w) + (cos(rad) * dist).toFloat()
            val py = (p.y * h) + (sin(rad) * dist).toFloat() + (progress * progress * 200f) // gravity pull downwards

            // Particle fades out near the end
            val alpha = (1f - progress).coerceIn(0f, 1f)
            
            drawCircle(
                color = p.color.copy(alpha = alpha),
                radius = p.radius,
                center = Offset(px, py)
            )
        }
    }
}
