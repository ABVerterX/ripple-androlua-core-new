package vinx.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

open class RatioLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleId: Int = 0
) : FrameLayout(context, attrs, defStyleId) {

    var ratio = 1f
        set(value) {
            field = value
            requestLayout()
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) {
            val newHeightMeasureSpec = MeasureSpec.makeMeasureSpec((width * ratio).toInt(), MeasureSpec.EXACTLY)
            super.onMeasure(widthMeasureSpec, newHeightMeasureSpec)
        } else {
            val newWidthMeasureSpec = MeasureSpec.makeMeasureSpec((height / ratio).toInt(), MeasureSpec.EXACTLY)
            super.onMeasure(newWidthMeasureSpec, heightMeasureSpec)
        }
    }

}