package com.contest.vezdekod.model

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap

class VKUser {
    var photo_200: String? = null
    var bitmapPhoto: ImageBitmap? = null
    var first_name: String? = null
    var last_name: String? = null
    var counters: Counters? = null
}

class Counters {
    var friends: Int? = null
    var groups: Int? = null
}