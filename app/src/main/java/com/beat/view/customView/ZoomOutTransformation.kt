package com.beat.view.customView

import android.view.View
import androidx.viewpager.widget.ViewPager
import kotlin.math.abs
import kotlin.math.max

class ZoomOutTransformation : ViewPager.PageTransformer {
    override fun transformPage(page: View, position: Float) {
        when {
            position < -1 -> {  // [-Infinity,-1)
                // This page is way off-screen to the left.
                page.alpha = 0F
            }
            position <= 1 -> { // [-1,1]
                page.scaleX = max(MIN_SCALE, 1 - abs(position))
                page.scaleY = max(MIN_SCALE, 1 - abs(position))
                page.alpha = max(MIN_ALPHA, 1 - abs(position))
            }
            else -> {  // (1,+Infinity]
                // This page is way off-screen to the right.
                page.alpha = 0F
            }
        }
    }

    companion object {
        private const val MIN_SCALE = 0.95f
        private const val MIN_ALPHA = 0.8f
    }
}