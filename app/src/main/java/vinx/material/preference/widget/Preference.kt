package vinx.material.preference.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.marginLeft
import androidx.core.view.setPadding
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView

open class Preference @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleId: Int = 0
) : BasePreference(context, attrs, defStyleId) {
    protected val labelRootView = LinearLayoutCompat(context)
    protected val titleView = MaterialTextView(context)
    protected val summaryView = MaterialTextView(context)
    protected var iconFrame = LinearLayoutCompat(context)
    protected var iconView = ShapeableImageView(context)
    protected var widgetFrame = LinearLayoutCompat(context)

    var title: String? = null
        set(value) {
            titleView.setTextWithVisibility(value)
            field = value
        }

    open var summary: String? = null
        set(value) {
            summaryView.setTextWithVisibility(value)
            field = value
        }

    open var icon: Drawable? = null
        set(value) {
            iconFrame.visibility = if (value != null) VISIBLE else GONE
            iconView.setImageDrawable(value)
            field = value
        }

    init {
        gravity = Gravity.CENTER_VERTICAL
        orientation = HORIZONTAL
        isClickable = true
        setIconSize(24.dp, 24.dp)
        setIconColor(android.R.attr.colorControlNormal.toAttrColor())
        super.setPadding(16.dp, 0.dp, 16.dp, 0.dp)
        addView(iconFrame)
        addView(labelRootView, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
        addView(widgetFrame, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT))
        setRippleBackground()

        labelRootView.apply {
            orientation = VERTICAL
            setPadding(0.dp, 16.dp, 0.dp, 16.dp)
            addView(titleView)
            addView(summaryView)
            (layoutParams as LayoutParams).weight = 1F
        }

        titleView.apply {
            visibility = GONE
            textAppearance = android.R.attr.textAppearanceListItem
            ellipsize = TextUtils.TruncateAt.MARQUEE
            setSingleLine()
        }

        summaryView.visibility = GONE

        iconFrame.apply {
            minimumWidth = 56.dp
            visibility = GONE
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
            setPadding(0.dp, 4.dp, 8.dp, 4.dp)
            addView(iconView)
        }

        widgetFrame.gravity = Gravity.END or Gravity.CENTER_VERTICAL
        widgetFrame.setPadding(16.dp, 0, 0, 0)
    }

    open fun setIconSize(@Px width: Int, @Px height: Int) {
        iconView.layoutParams = LayoutParams(width, height)
    }

    open fun setIconColor(@ColorInt color: Int) {
        iconView.setColorFilter(color)
    }

    fun setTitleColor(@ColorInt color: Int) {
        titleView.setTextColor(color)
    }

    fun setSummaryColor(@ColorInt color: Int) {
        summaryView.setTextColor(color)
    }

    fun setTitleTypeface(typeface: Typeface) {
        titleView.typeface = typeface
    }

    fun setSummaryTypeface(typeface: Typeface) {
        summaryView.typeface = typeface
    }

    fun setTitleTextSize(textSize: Float) {
        titleView.textSize = textSize
    }

    fun setSummaryTextSize(textSize: Float) {
        summaryView.textSize = textSize
    }

    fun setBlankIcon(isBlankIcon: Boolean) {
        icon = if (isBlankIcon) {
            BlankIconDrawable
        } else {
            null
        }
    }

    override fun setPadding(@Px left: Int, @Px top: Int, @Px right: Int, @Px bottom: Int) {
        super.setPadding(left, 0.dp, right, 0.dp)
        labelRootView.setPadding(0.dp, top, 0.dp, bottom)
    }

    open fun setImageBitmap(bitmap: Bitmap) {
        icon = BitmapDrawable(context.resources, bitmap)
    }

    override fun save() {}
    override fun update() {}

}