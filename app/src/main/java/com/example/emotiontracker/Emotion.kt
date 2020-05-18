package com.example.emotiontracker

import androidx.annotation.ColorRes
import org.threeten.bp.LocalDateTime

data class Emotion(val time: LocalDateTime, val emotionCat: String, @ColorRes val color: Int)
