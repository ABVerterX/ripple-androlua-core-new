package vinx.material.preference.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.Space
import androidx.annotation.ColorInt
import androidx.annotation.Px

@Deprecated("No longer need it. ", ReplaceWith("Preference.setBlankIcon(true); "))
class BlankIconPreference @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleId: Int = 0
) : Preference(context, attrs, defStyleId) {

    override var icon: Drawable? = null
        set(value) {
            field = null
        }

    override fun save() {}
    override fun update() {}

    init {
        iconFrame.removeView(iconView)
        iconFrame.visibility = VISIBLE
    }

//    override fun setIconSize(@Px width: Int, @Px height: Int) {
//        throw UnsupportedOperationException()
//    }

//    override fun setIconColor(@ColorInt color: Int) {
//        throw UnsupportedOperationException()
//    }
//
//    override fun setImageBitmap(bitmap: Bitmap) {
//        throw UnsupportedOperationException()
//    }
}