package com.dingyi.myluaapp.build

import android.app.Activity
import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import kotlin.math.roundToInt


fun Context.getDefaultFontSizes(): IntArray {
    val dipInPixels = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        1f,
        this.resources.displayMetrics
    )

    val sizes = IntArray(3)

    // This is a bit arbitrary and sub-optimal. We want to give a sensible default for minimum font size
    // to prevent invisible text due to zoom be mistake:
    sizes[1] = (4f * dipInPixels).toInt() // min

    // http://www.google.com/design/spec/style/typography.html#typography-line-height
    var defaultFontSize = (12 * dipInPixels).roundToInt()
    // Make it divisible by 2 since that is the minimal adjustment step:
    if (defaultFontSize % 2 == 1) defaultFontSize--
    sizes[0] = defaultFontSize // default
    sizes[2] = 256 // max
    return sizes
}

/**
 * If value is not in the range [min, max], set it to either min or max.
 */
fun Int.clamp(min: Int, max: Int): Int {
    return this.coerceAtLeast(min).coerceAtMost(max)
}

class FontSize(defaultSizes: IntArray) {

    private val defaultSize = defaultSizes[0]
    private val minSize = defaultSizes[1]
    private val maxSize = defaultSizes[2]
    private var currentSize = defaultSize

    fun getFontSize(): Int {
        return currentSize.clamp(minSize, maxSize)
    }

    fun setFontSize(size: Int) {
        currentSize = size
    }

    fun changeFontSize(increase: Boolean) {
        var currentFontSize = getFontSize()
        currentFontSize += (if (increase) 1 else -1) * 2
        currentFontSize = minSize.coerceAtLeast(currentFontSize.coerceAtMost(maxSize))
        setFontSize(currentFontSize)
    }

}

fun Activity.areDisableSoftKeyboardFlagsSet(): Boolean {
    return if (window == null) false else
        window.attributes.flags and WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM !== 0
}

fun Activity.showSoftKeyboard(view: View) {
    view.requestFocus()
    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.showSoftInput(view, 0)
}