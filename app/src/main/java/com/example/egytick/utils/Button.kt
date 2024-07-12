package com.example.egytick.utils

import android.content.Context
import android.util.AttributeSet
import android.graphics.Typeface
import androidx.appcompat.widget.AppCompatButton

class Button(context: Context, attrs: AttributeSet) : AppCompatButton(context, attrs) {
    init {
        applyFont()
    }

    private fun applyFont() {
        val typeface: Typeface = Typeface.createFromAsset(context.assets, "Montserrat-Bold.ttf")
        setTypeface(typeface)
    }
}
