package com.example.data

import androidx.compose.ui.geometry.Offset
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

data class Stroke(val points: List<Offset>)

data class LetterItem(
    val char: Char,
    val name: String,
    val uppercaseStrokes: List<Stroke>,
    val lowercaseStrokes: List<Stroke>,
    val animalName: String,
    val animalEmoji: String,
    val animalFact: String,
    val themeColor: Long // ARGB color
)

object LetterTemplates {
    // Generate linear interpolation between two offsets
    private fun line(x1: Float, y1: Float, x2: Float, y2: Float, count: Int = 6): List<Offset> {
        val points = mutableListOf<Offset>()
        for (i in 0 until count) {
            val t = i.toFloat() / (count - 1)
            points.add(Offset(x1 + t * (x2 - x1), y1 + t * (y2 - y1)))
        }
        return points
    }

    // Generate elliptical/circular path interpolation
    private fun arc(
        cx: Float, cy: Float, rx: Float, ry: Float,
        startDeg: Float, sweepDeg: Float, count: Int = 8
    ): List<Offset> {
        val points = mutableListOf<Offset>()
        for (i in 0 until count) {
            val t = i.toFloat() / (count - 1)
            val angleDeg = startDeg + t * sweepDeg
            val rad = angleDeg * PI / 180.0
            val x = cx + rx * cos(rad).toFloat()
            val y = cy + ry * sin(rad).toFloat()
            points.add(Offset(x, y))
        }
        return points
    }

    val list: List<LetterItem> = listOf(
        LetterItem(
            char = 'A', name = "Alligator", animalName = "Alligator", animalEmoji = "🐊",
            animalFact = "Alligators have 80 sharp teeth!", themeColor = 0xFF83C5BE,
            uppercaseStrokes = listOf(
                Stroke(line(0.5f, 0.15f, 0.2f, 0.85f)),
                Stroke(line(0.5f, 0.15f, 0.8f, 0.85f)),
                Stroke(line(0.32f, 0.58f, 0.68f, 0.58f))
            ),
            lowercaseStrokes = listOf(
                Stroke(arc(0.5f, 0.6f, 0.17f, 0.17f, 0f, -360f)),
                Stroke(line(0.67f, 0.43f, 0.67f, 0.82f))
            )
        ),
        LetterItem(
            char = 'B', name = "Bear", animalName = "Bear", animalEmoji = "🐻",
            animalFact = "Bears can run up to 35 miles per hour!", themeColor = 0xFFE07A5F,
            uppercaseStrokes = listOf(
                Stroke(line(0.28f, 0.15f, 0.28f, 0.85f)),
                Stroke(arc(0.28f, 0.32f, 0.22f, 0.17f, -90f, 180f)),
                Stroke(arc(0.28f, 0.67f, 0.25f, 0.18f, -90f, 180f))
            ),
            lowercaseStrokes = listOf(
                Stroke(line(0.3f, 0.15f, 0.3f, 0.85f)),
                Stroke(arc(0.3f, 0.62f, 0.22f, 0.2f, -90f, 180f))
            )
        ),
        LetterItem(
            char = 'C', name = "Cat", animalName = "Cat", animalEmoji = "🐱",
            animalFact = "Cats sleep for 12 to 16 hours a day!", themeColor = 0xFFF4A261,
            uppercaseStrokes = listOf(
                Stroke(arc(0.52f, 0.5f, 0.25f, 0.32f, 45f, 270f))
            ),
            lowercaseStrokes = listOf(
                Stroke(arc(0.52f, 0.62f, 0.18f, 0.2f, 45f, 270f))
            )
        ),
        LetterItem(
            char = 'D', name = "Dolphin", animalName = "Dolphin", animalEmoji = "🐬",
            animalFact = "Dolphins call each other by unique whistles!", themeColor = 0xFF2A9D8F,
            uppercaseStrokes = listOf(
                Stroke(line(0.3f, 0.15f, 0.3f, 0.85f)),
                Stroke(arc(0.3f, 0.5f, 0.32f, 0.35f, -90f, 180f))
            ),
            lowercaseStrokes = listOf(
                Stroke(line(0.7f, 0.15f, 0.7f, 0.85f)),
                Stroke(arc(0.7f, 0.62f, 0.22f, 0.2f, 90f, 180f))
            )
        ),
        LetterItem(
            char = 'E', name = "Elephant", animalName = "Elephant", animalEmoji = "🐘",
            animalFact = "Elephants are the largest land animals!", themeColor = 0xFF588157,
            uppercaseStrokes = listOf(
                Stroke(line(0.3f, 0.15f, 0.3f, 0.85f)),
                Stroke(line(0.3f, 0.15f, 0.72f, 0.15f)),
                Stroke(line(0.3f, 0.5f, 0.62f, 0.5f)),
                Stroke(line(0.3f, 0.85f, 0.72f, 0.85f))
            ),
            lowercaseStrokes = listOf(
                Stroke(line(0.32f, 0.6f, 0.68f, 0.6f)),
                Stroke(arc(0.5f, 0.57f, 0.18f, 0.18f, 0f, -280f))
            )
        ),
        LetterItem(
            char = 'F', name = "Fox", animalName = "Fox", animalEmoji = "🦊",
            animalFact = "Foxes use their bushy tails to stay warm!", themeColor = 0xFFE76F51,
            uppercaseStrokes = listOf(
                Stroke(line(0.32f, 0.15f, 0.32f, 0.85f)),
                Stroke(line(0.32f, 0.15f, 0.72f, 0.15f)),
                Stroke(line(0.32f, 0.48f, 0.62f, 0.48f))
            ),
            lowercaseStrokes = listOf(
                Stroke(arc(0.55f, 0.3f, 0.15f, 0.15f, 0f, 180f) + line(0.4f, 0.3f, 0.4f, 0.85f)),
                Stroke(line(0.28f, 0.45f, 0.58f, 0.45f))
            )
        ),
        LetterItem(
            char = 'G', name = "Giraffe", animalName = "Giraffe", animalEmoji = "🦒",
            animalFact = "Giraffes have blue-purple tongues!", themeColor = 0xFFE9C46A,
            uppercaseStrokes = listOf(
                Stroke(arc(0.52f, 0.5f, 0.24f, 0.32f, 45f, 260f)),
                Stroke(line(0.55f, 0.55f, 0.76f, 0.55f) + line(0.76f, 0.55f, 0.76f, 0.75f))
            ),
            lowercaseStrokes = listOf(
                Stroke(arc(0.5f, 0.48f, 0.18f, 0.16f, 0f, -360f)),
                Stroke(line(0.68f, 0.48f, 0.68f, 0.8f) + arc(0.52f, 0.8f, 0.16f, 0.12f, 0f, 150f))
            )
        ),
        LetterItem(
            char = 'H', name = "Hippo", animalName = "Hippo", animalEmoji = "🦛",
            animalFact = "Hippos run incredibly fast on land!", themeColor = 0xFF9B5DE5,
            uppercaseStrokes = listOf(
                Stroke(line(0.28f, 0.15f, 0.28f, 0.85f)),
                Stroke(line(0.72f, 0.15f, 0.72f, 0.85f)),
                Stroke(line(0.28f, 0.5f, 0.72f, 0.5f))
            ),
            lowercaseStrokes = listOf(
                Stroke(line(0.3f, 0.15f, 0.3f, 0.85f)),
                Stroke(arc(0.5f, 0.62f, 0.2f, 0.18f, 180f, -140f) + line(0.7f, 0.62f, 0.7f, 0.85f))
            )
        ),
        LetterItem(
            char = 'I', name = "Iguana", animalName = "Iguana", animalEmoji = "🦎",
            animalFact = "Iguanas can hold their breath underwater!", themeColor = 0xFF606C38,
            uppercaseStrokes = listOf(
                Stroke(line(0.5f, 0.15f, 0.5f, 0.85f)),
                Stroke(line(0.32f, 0.15f, 0.68f, 0.15f)),
                Stroke(line(0.32f, 0.85f, 0.68f, 0.85f))
            ),
            lowercaseStrokes = listOf(
                Stroke(line(0.5f, 0.18f, 0.5f, 0.24f)), // Dot represents as short stem or single
                Stroke(line(0.5f, 0.4f, 0.5f, 0.85f))
            )
        ),
        LetterItem(
            char = 'J', name = "Jaguar", animalName = "Jaguar", animalEmoji = "🐆",
            animalFact = "Jaguars are wonderful swimmers!", themeColor = 0xFFF39C12,
            uppercaseStrokes = listOf(
                Stroke(line(0.32f, 0.15f, 0.68f, 0.15f)),
                Stroke(line(0.5f, 0.15f, 0.5f, 0.75f) + arc(0.42f, 0.75f, 0.08f, 0.1f, 0f, 150f))
            ),
            lowercaseStrokes = listOf(
                Stroke(line(0.54f, 0.28f, 0.54f, 0.32f)), // Dot
                Stroke(line(0.54f, 0.46f, 0.54f, 0.8f) + arc(0.42f, 0.8f, 0.12f, 0.12f, 0f, 150f))
            )
        ),
        LetterItem(
            char = 'K', name = "Koala", animalName = "Koala", animalEmoji = "🐨",
            animalFact = "Koalas eat up to two pounds of leaves daily!", themeColor = 0xFF7F8C8D,
            uppercaseStrokes = listOf(
                Stroke(line(0.3f, 0.15f, 0.3f, 0.85f)),
                Stroke(line(0.7f, 0.15f, 0.32f, 0.5f)),
                Stroke(line(0.32f, 0.5f, 0.72f, 0.85f))
            ),
            lowercaseStrokes = listOf(
                Stroke(line(0.32f, 0.15f, 0.32f, 0.85f)),
                Stroke(line(0.64f, 0.45f, 0.34f, 0.62f)),
                Stroke(line(0.34f, 0.62f, 0.68f, 0.85f))
            )
        ),
        LetterItem(
            char = 'L', name = "Lion", animalName = "Lion", animalEmoji = "🦁",
            animalFact = "A lion's roar can be heard 5 miles away!", themeColor = 0xFFD35400,
            uppercaseStrokes = listOf(
                Stroke(line(0.34f, 0.15f, 0.34f, 0.85f)),
                Stroke(line(0.34f, 0.85f, 0.72f, 0.85f))
            ),
            lowercaseStrokes = listOf(
                Stroke(line(0.5f, 0.15f, 0.5f, 0.85f))
            )
        ),
        LetterItem(
            char = 'M', name = "Monkey", animalName = "Monkey", animalEmoji = "🐵",
            animalFact = "Monkeys use expressions and sounds to talk!", themeColor = 0xFF8D6E63,
            uppercaseStrokes = listOf(
                Stroke(line(0.2f, 0.85f, 0.2f, 0.15f)),
                Stroke(line(0.2f, 0.15f, 0.5f, 0.58f)),
                Stroke(line(0.5f, 0.58f, 0.8f, 0.15f)),
                Stroke(line(0.8f, 0.15f, 0.8f, 0.85f))
            ),
            lowercaseStrokes = listOf(
                Stroke(line(0.28f, 0.45f, 0.28f, 0.85f)),
                Stroke(arc(0.4f, 0.6f, 0.12f, 0.15f, 180f, -180f) + line(0.52f, 0.6f, 0.52f, 0.85f)),
                Stroke(arc(0.64f, 0.6f, 0.12f, 0.15f, 180f, -180f) + line(0.76f, 0.6f, 0.76f, 0.85f))
            )
        ),
        LetterItem(
            char = 'N', name = "Nightingale", animalName = "Nightingale", animalEmoji = "🐦",
            animalFact = "Nightingales have over 200 beautiful songs!", themeColor = 0xFF4A90E2,
            uppercaseStrokes = listOf(
                Stroke(line(0.26f, 0.85f, 0.26f, 0.15f)),
                Stroke(line(0.26f, 0.15f, 0.74f, 0.85f)),
                Stroke(line(0.74f, 0.85f, 0.74f, 0.15f))
            ),
            lowercaseStrokes = listOf(
                Stroke(line(0.32f, 0.45f, 0.32f, 0.85f)),
                Stroke(arc(0.51f, 0.62f, 0.19f, 0.17f, 180f, -180f) + line(0.7f, 0.62f, 0.7f, 0.85f))
            )
        ),
        LetterItem(
            char = 'O', name = "Owl", animalName = "Owl", animalEmoji = "🦉",
            animalFact = "Owls can rotate their heads 270 degrees!", themeColor = 0xFFE08D60,
            uppercaseStrokes = listOf(
                Stroke(arc(0.5f, 0.5f, 0.28f, 0.34f, 0f, 360f))
            ),
            lowercaseStrokes = listOf(
                Stroke(arc(0.5f, 0.63f, 0.2f, 0.2f, 0f, 360f))
            )
        ),
        LetterItem(
            char = 'P', name = "Panda", animalName = "Panda", animalEmoji = "🐼",
            animalFact = "Pandas eat bamboo for 12 hours every day!", themeColor = 0xFF3D3D3D,
            uppercaseStrokes = listOf(
                Stroke(line(0.28f, 0.15f, 0.28f, 0.85f)),
                Stroke(arc(0.28f, 0.34f, 0.26f, 0.19f, -90f, 180f))
            ),
            lowercaseStrokes = listOf(
                Stroke(line(0.31f, 0.42f, 0.31f, 0.95f)),
                Stroke(arc(0.31f, 0.62f, 0.23f, 0.19f, -90f, 180f))
            )
        ),
        LetterItem(
            char = 'Q', name = "Quail", animalName = "Quail", animalEmoji = "🐦",
            animalFact = "Quails are excellent runners in thick grass!", themeColor = 0xFF6C7A89,
            uppercaseStrokes = listOf(
                Stroke(arc(0.50f, 0.48f, 0.28f, 0.33f, 0f, 360f)),
                Stroke(line(0.61f, 0.65f, 0.82f, 0.85f))
            ),
            lowercaseStrokes = listOf(
                Stroke(arc(0.5f, 0.62f, 0.19f, 0.19f, 0f, -360f)),
                Stroke(line(0.69f, 0.43f, 0.69f, 0.94f) + line(0.69f, 0.94f, 0.79f, 0.94f))
            )
        ),
        LetterItem(
            char = 'R', name = "Rabbit", animalName = "Rabbit", animalEmoji = "🐰",
            animalFact = "Rabbits do a happy logic dance called a 'binky'!", themeColor = 0xFFF2A6B2,
            uppercaseStrokes = listOf(
                Stroke(line(0.28f, 0.15f, 0.28f, 0.85f)),
                Stroke(arc(0.28f, 0.35f, 0.26f, 0.2f, -90f, 180f)),
                Stroke(line(0.28f, 0.52f, 0.7f, 0.85f))
            ),
            lowercaseStrokes = listOf(
                Stroke(line(0.34f, 0.45f, 0.34f, 0.85f)),
                Stroke(arc(0.5f, 0.58f, 0.16f, 0.13f, 180f, -120f))
            )
        ),
        LetterItem(
            char = 'S', name = "Squirrel", animalName = "Squirrel", animalEmoji = "🐿️",
            animalFact = "Squirrels plant thousands of trees each year!", themeColor = 0xFFC68B59,
            uppercaseStrokes = listOf(
                Stroke(
                    arc(0.5f, 0.32f, 0.2f, 0.17f, 45f, 210f) +
                    arc(0.5f, 0.68f, 0.22f, 0.18f, -135f, -210f)
                )
            ),
            lowercaseStrokes = listOf(
                Stroke(
                    arc(0.51f, 0.54f, 0.15f, 0.12f, 45f, 210f) +
                    arc(0.51f, 0.76f, 0.17f, 0.13f, -135f, -210f)
                )
            )
        ),
        LetterItem(
            char = 'T', name = "Tiger", animalName = "Tiger", animalEmoji = "🐯",
            animalFact = "Tigers have unique stripes on their skin too!", themeColor = 0xFFF15A24,
            uppercaseStrokes = listOf(
                Stroke(line(0.25f, 0.16f, 0.75f, 0.16f)),
                Stroke(line(0.5f, 0.16f, 0.5f, 0.84f))
            ),
            lowercaseStrokes = listOf(
                Stroke(line(0.46f, 0.2f, 0.46f, 0.78f) + arc(0.55f, 0.78f, 0.09f, 0.06f, 180f, -90f)),
                Stroke(line(0.28f, 0.42f, 0.64f, 0.42f))
            )
        ),
        LetterItem(
            char = 'U', name = "Unicorn", animalName = "Unicorn", animalEmoji = "🦄",
            animalFact = "The unicorn is the national animal of Scotland!", themeColor = 0xFF9F86C0,
            uppercaseStrokes = listOf(
                Stroke(line(0.28f, 0.15f, 0.28f, 0.65f) + arc(0.5f, 0.65f, 0.22f, 0.2f, 180f, -180f) + line(0.72f, 0.65f, 0.72f, 0.15f))
            ),
            lowercaseStrokes = listOf(
                Stroke(line(0.32f, 0.45f, 0.32f, 0.75f) + arc(0.51f, 0.75f, 0.19f, 0.1f, 180f, -180f) + line(0.7f, 0.75f, 0.7f, 0.45f)),
                Stroke(line(0.7f, 0.45f, 0.7f, 0.85f))
            )
        ),
        LetterItem(
            char = 'V', name = "Vulture", animalName = "Vulture", animalEmoji = "🦅",
            animalFact = "Vultures protect nature by keeping it clean!", themeColor = 0xFF5D6D7E,
            uppercaseStrokes = listOf(
                Stroke(line(0.2f, 0.15f, 0.5f, 0.85f)),
                Stroke(line(0.5f, 0.85f, 0.8f, 0.15f))
            ),
            lowercaseStrokes = listOf(
                Stroke(line(0.32f, 0.46f, 0.5f, 0.84f)),
                Stroke(line(0.5f, 0.84f, 0.68f, 0.46f))
            )
        ),
        LetterItem(
            char = 'W', name = "Whale", animalName = "Whale", animalEmoji = "🐳",
            animalFact = "Whales are larger than any dinosaur ever was!", themeColor = 0xFF3E82B8,
            uppercaseStrokes = listOf(
                Stroke(line(0.18f, 0.15f, 0.34f, 0.85f)),
                Stroke(line(0.34f, 0.85f, 0.5f, 0.38f)),
                Stroke(line(0.5f, 0.38f, 0.66f, 0.85f)),
                Stroke(line(0.66f, 0.85f, 0.82f, 0.15f))
            ),
            lowercaseStrokes = listOf(
                Stroke(line(0.24f, 0.46f, 0.36f, 0.84f)),
                Stroke(line(0.36f, 0.84f, 0.48f, 0.56f)),
                Stroke(line(0.48f, 0.56f, 0.6f, 0.84f)),
                Stroke(line(0.6f, 0.84f, 0.72f, 0.46f))
            )
        ),
        LetterItem(
            char = 'X', name = "X-ray Fish", animalName = "X-ray Fish", animalEmoji = "🐟",
            animalFact = "X-ray Fish are completely clear inside!", themeColor = 0xFF3FE2C9,
            uppercaseStrokes = listOf(
                Stroke(line(0.22f, 0.15f, 0.78f, 0.85f)),
                Stroke(line(0.78f, 0.15f, 0.22f, 0.85f))
            ),
            lowercaseStrokes = listOf(
                Stroke(line(0.32f, 0.45f, 0.68f, 0.85f)),
                Stroke(line(0.68f, 0.45f, 0.32f, 0.85f))
            )
        ),
        LetterItem(
            char = 'Y', name = "Yak", animalName = "Yak", animalEmoji = "🦬",
            animalFact = "Yaks live in snow and have extremely warm fur!", themeColor = 0xFF7E6B5A,
            uppercaseStrokes = listOf(
                Stroke(line(0.22f, 0.15f, 0.5f, 0.5f)),
                Stroke(line(0.78f, 0.15f, 0.5f, 0.5f)),
                Stroke(line(0.5f, 0.5f, 0.5f, 0.85f))
            ),
            lowercaseStrokes = listOf(
                Stroke(line(0.31f, 0.45f, 0.5f, 0.75f)),
                Stroke(line(0.69f, 0.45f, 0.31f, 0.95f))
            )
        ),
        LetterItem(
            char = 'Z', name = "Zebra", animalName = "Zebra", animalEmoji = "🦓",
            animalFact = "No two zebras have the exact same stripes!", themeColor = 0xFF2C3E50,
            uppercaseStrokes = listOf(
                Stroke(line(0.25f, 0.18f, 0.75f, 0.18f)),
                Stroke(line(0.75f, 0.18f, 0.25f, 0.82f)),
                Stroke(line(0.25f, 0.82f, 0.75f, 0.82f))
            ),
            lowercaseStrokes = listOf(
                Stroke(line(0.32f, 0.46f, 0.68f, 0.46f)),
                Stroke(line(0.68f, 0.46f, 0.32f, 0.82f)),
                Stroke(line(0.32f, 0.82f, 0.68f, 0.82f))
            )
        )
    )

    val numberList: List<LetterItem> = listOf(
        LetterItem(
            char = '0', name = "Nebula Zero", animalName = "Infinity Black Hole", animalEmoji = "🌌",
            animalFact = "A cosmic wormhole that loops forever, swallowing old stars and birthing newborn supernovas!", themeColor = 0xFF8338EC,
            uppercaseStrokes = listOf(
                Stroke(arc(0.5f, 0.5f, 0.22f, 0.32f, -90f, 360f, count = 18))
            ),
            lowercaseStrokes = listOf(
                Stroke(arc(0.5f, 0.5f, 0.22f, 0.32f, -90f, 360f, count = 18))
            )
        ),
        LetterItem(
            char = '1', name = "Laser One", animalName = "Galactic Rocket", animalEmoji = "🚀",
            animalFact = "A hyper-speed star-cruiser that launches perpendicular to the galactic plane!", themeColor = 0xFF3A86C8,
            uppercaseStrokes = listOf(
                Stroke(line(0.38f, 0.25f, 0.5f, 0.15f) + line(0.5f, 0.15f, 0.5f, 0.85f)),
                Stroke(line(0.32f, 0.85f, 0.68f, 0.85f))
            ),
            lowercaseStrokes = listOf(
                Stroke(line(0.38f, 0.25f, 0.5f, 0.15f) + line(0.5f, 0.15f, 0.5f, 0.85f)),
                Stroke(line(0.32f, 0.85f, 0.68f, 0.85f))
            )
        ),
        LetterItem(
            char = '2', name = "Cosmic Two", animalName = "Cyber Swan-Dragon", animalEmoji = "🐉",
            animalFact = "Glows with the power of twin binary stars, orbiting safely in the Sagittarius Arm!", themeColor = 0xFFFB5607,
            uppercaseStrokes = listOf(
                Stroke(arc(0.48f, 0.32f, 0.22f, 0.18f, -180f, 200f) + line(0.68f, 0.42f, 0.28f, 0.85f)),
                Stroke(line(0.28f, 0.85f, 0.72f, 0.85f))
            ),
            lowercaseStrokes = listOf(
                Stroke(arc(0.48f, 0.32f, 0.22f, 0.18f, -180f, 200f) + line(0.68f, 0.42f, 0.28f, 0.85f)),
                Stroke(line(0.28f, 0.85f, 0.72f, 0.85f))
            )
        ),
        LetterItem(
            char = '3', name = "Warp Three", animalName = "Alien UFO Butterfly", animalEmoji = "🛸",
            animalFact = "Traces a path through 3 dimensional warping hyper-jump points in deep space!", themeColor = 0xFF2A9D8F,
            uppercaseStrokes = listOf(
                Stroke(arc(0.5f, 0.35f, 0.22f, 0.18f, -90f, 210f, count = 10) + arc(0.5f, 0.65f, 0.25f, 0.20f, -90f, 210f, count = 10))
            ),
            lowercaseStrokes = listOf(
                Stroke(arc(0.5f, 0.35f, 0.22f, 0.18f, -90f, 210f, count = 10) + arc(0.5f, 0.65f, 0.25f, 0.20f, -90f, 210f, count = 10))
            )
        ),
        LetterItem(
            char = '4', name = "Spark Four", animalName = "Hyper Charge Robot", animalEmoji = "🤖",
            animalFact = "A cute android companion that uses its four laser antennas to chat with satellites!", themeColor = 0xFFF77F00,
            uppercaseStrokes = listOf(
                Stroke(line(0.55f, 0.15f, 0.25f, 0.62f) + line(0.25f, 0.62f, 0.75f, 0.62f)),
                Stroke(line(0.58f, 0.35f, 0.58f, 0.85f))
            ),
            lowercaseStrokes = listOf(
                Stroke(line(0.55f, 0.15f, 0.25f, 0.62f) + line(0.25f, 0.62f, 0.75f, 0.62f)),
                Stroke(line(0.58f, 0.35f, 0.58f, 0.85f))
            )
        ),
        LetterItem(
            char = '5', name = "Stellar Five", animalName = "Cosmic Pegasus", animalEmoji = "🦄",
            animalFact = "A magical flying celestial horse whose five horn crystals glow in beautiful rainbow hues!", themeColor = 0xFFFF006E,
            uppercaseStrokes = listOf(
                Stroke(line(0.35f, 0.18f, 0.35f, 0.45f) + arc(0.48f, 0.62f, 0.22f, 0.2f, -90f, 210f, count = 12)),
                Stroke(line(0.35f, 0.18f, 0.68f, 0.18f))
            ),
            lowercaseStrokes = listOf(
                Stroke(line(0.35f, 0.18f, 0.35f, 0.45f) + arc(0.48f, 0.62f, 0.22f, 0.2f, -90f, 210f, count = 12)),
                Stroke(line(0.35f, 0.18f, 0.68f, 0.18f))
            )
        ),
        LetterItem(
            char = '6', name = "Cyclone Six", animalName = "Multidimensional Vortex", animalEmoji = "🌀",
            animalFact = "A swirl of stardust spinning 6 times faster than a pulsar star!", themeColor = 0xFF00B4D8,
            uppercaseStrokes = listOf(
                Stroke(line(0.62f, 0.18f, 0.35f, 0.5f) + arc(0.5f, 0.65f, 0.2f, 0.2f, 0f, 360f, count = 14))
            ),
            lowercaseStrokes = listOf(
                Stroke(line(0.62f, 0.18f, 0.35f, 0.5f) + arc(0.5f, 0.65f, 0.2f, 0.2f, 0f, 360f, count = 14))
            )
        ),
        LetterItem(
            char = '7', name = "Bolt Seven", animalName = "Quantum Chrono-Boomerang", animalEmoji = "⚡",
            animalFact = "A stellar boomerang carrying golden lightning bolts across 7 dimensions!", themeColor = 0xFFFFB703,
            uppercaseStrokes = listOf(
                Stroke(line(0.28f, 0.18f, 0.72f, 0.18f)),
                Stroke(line(0.72f, 0.18f, 0.38f, 0.85f))
            ),
            lowercaseStrokes = listOf(
                Stroke(line(0.28f, 0.18f, 0.72f, 0.18f)),
                Stroke(line(0.72f, 0.18f, 0.38f, 0.85f))
            )
        ),
        LetterItem(
            char = '8', name = "Epoch Eight", animalName = "Infinity Warp Gate", animalEmoji = "♾️",
            animalFact = "A loop of infinite possibility allowing instant travel across millions of light-years!", themeColor = 0xFFE76F51,
            uppercaseStrokes = listOf(
                Stroke(arc(0.5f, 0.33f, 0.18f, 0.18f, 0f, 360f, count = 12)),
                Stroke(arc(0.5f, 0.67f, 0.22f, 0.22f, 0f, -360f, count = 12))
            ),
            lowercaseStrokes = listOf(
                Stroke(arc(0.5f, 0.33f, 0.18f, 0.18f, 0f, 360f, count = 12)),
                Stroke(arc(0.5f, 0.67f, 0.22f, 0.22f, 0f, -360f, count = 12))
            )
        ),
        LetterItem(
            char = '9', name = "Astro Nine", animalName = "Space Balloon Octopus", animalEmoji = "🐙",
            animalFact = "A floaty alien octopus with 9 jelly tentacles that plays cosmic harp melodies on starbeams!", themeColor = 0xFFD81159,
            uppercaseStrokes = listOf(
                Stroke(arc(0.5f, 0.35f, 0.2f, 0.2f, 0f, -360f, count = 12)),
                Stroke(line(0.7f, 0.35f, 0.7f, 0.85f))
            ),
            lowercaseStrokes = listOf(
                Stroke(arc(0.5f, 0.35f, 0.2f, 0.2f, 0f, -360f, count = 12)),
                Stroke(line(0.7f, 0.35f, 0.7f, 0.85f))
            )
        )
    )
}
