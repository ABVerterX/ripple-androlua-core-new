package vinx.material.preference.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.Px
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.imageview.ShapeableImageView

class EndIconPreference @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleId: Int = 0
) : Preference(context, attrs, defStyleId) {

    private val checkBox = MaterialCheckBox(context)
    private var value: Boolean = false
    private var endIconView = ShapeableImageView(context)
    var endIcon: Drawable? = null
        set(value) {
            field = value
            endIconView.setImageDrawable(value)
        }

    init {
        widgetFrame.addView(endIconView)
        setEndIconSize(24.dp, 24.dp)
        setEndIconColor(android.R.attr.colorControlNormal.toAttrColor())
    }

    fun setEndIconSize(@Px width: Int, @Px height: Int) {
        endIconView.layoutParams = LayoutParams(width, height)
    }

    fun setEndIconColor(@ColorInt color: Int) {
        endIconView.setColorFilter(color)
    }

    override fun save() {}
    override fun update() {}
}