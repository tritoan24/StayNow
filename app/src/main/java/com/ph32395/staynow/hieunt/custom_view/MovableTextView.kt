package com.ph32395.staynow.hieunt.custom_view

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class MovableTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {
    init {
        this.ellipsize = TextUtils.TruncateAt.MARQUEE
        this.isSingleLine = true
        this.marqueeRepeatLimit = -1
        this.isSelected = true
    }
}