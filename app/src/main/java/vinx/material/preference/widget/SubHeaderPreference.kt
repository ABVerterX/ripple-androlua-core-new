package vinx.material.preference.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import com.google.android.material.checkbox.MaterialCheckBox

class SubHeaderPreference @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleId: Int = 0
) : Preference(context, attrs, defStyleId) {

    override var summary: String? = null
        set(value) {
            field = null
        }

    private var isBlankEnabled = false

    init {
        isClickable = false
        titleView.apply {
            textAppearance = android.R.attr.textAppearanceSearchResultTitle
            setTextColor(android.R.attr.colorPrimary.toAttrColor())
            textSize = 14F
        }
        labelRootView.apply {
            setPadding(0.dp, 24.dp, 0.dp, 8.dp)
        }

        iconFrame.visibility = GONE
    }

    fun isBlankEnabled() = isBlankEnabled
    fun setBlankEnabled(enabled: Boolean) {
        isBlankEnabled = enabled
        iconFrame.visibility = if (enabled) VISIBLE else GONE
    }

    override fun save() {}
    override fun update() {}
}