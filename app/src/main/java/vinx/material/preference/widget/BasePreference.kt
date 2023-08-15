package vinx.material.preference.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.widget.TextViewCompat
import com.google.android.material.textview.MaterialTextView
import vinx.material.preference.PreferenceDataStore
import vinx.material.preference.PreferenceManager
import kotlin.math.round

abstract class BasePreference @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleId: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleId) {

    var key: String? = null
    var manager: PreferenceManager? = null

    var onPreferenceClickListener: OnPreferenceClickListener? = null

    init {
        setOnClickListener {
            onClick()
            onPreferenceClickListener?.onClick(this)
        }
    }

    protected val Number.dp get() = round(toFloat() * context.resources.displayMetrics.density).toInt()

    protected var MaterialTextView.textAppearance: Int
        get() {
            throw UnsupportedOperationException()
        }
        set(value) = run {
            val attrs = context.obtainStyledAttributes(intArrayOf(value))
            val resId = attrs.getResourceId(0, 0)
            TextViewCompat.setTextAppearance(this, resId)
            attrs.recycle()
        }

    protected fun Int.toAttrColor(): Int {
        val attrs = context.obtainStyledAttributes(intArrayOf(this))
        val color = attrs.getColor(0, Color.TRANSPARENT)
        attrs.recycle()
        return color
    }

    protected fun View.setRippleBackground(isBorderless: Boolean? = false) {
        val attrId =
            if (isBorderless == true) android.R.attr.selectableItemBackgroundBorderless else android.R.attr.selectableItemBackground
        val styled = context.obtainStyledAttributes(intArrayOf(attrId))
        this.setBackgroundResource(styled.getResourceId(0, 0))
        styled.recycle()
    }

    protected fun MaterialTextView.setTextWithVisibility(charSequence: CharSequence?) = this.apply {
        text = charSequence
        visibility = when (charSequence) {
            null -> View.GONE
            else -> View.VISIBLE
        }
    }

    protected fun openDataStore(): PreferenceDataStore? {
        return if (key == null) null else manager?.dataStore
    }

    protected open fun onClick() {}

    abstract fun save()
    abstract fun update()

    interface OnPreferenceClickListener {
        fun onClick(view: BasePreference)
    }
}