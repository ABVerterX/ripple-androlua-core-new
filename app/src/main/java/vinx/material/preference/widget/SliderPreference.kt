package vinx.material.preference.widget

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.slider.Slider

class SliderPreference @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleId: Int = 0
) : Preference(context, attrs, defStyleId) {

    val slider = Slider(context)
    var value = 0f
        set(value) {
            field = value
            slider.value = field
        }

    init {
        setPadding(16.dp, 0, 0, 0)
        labelRootView.addView(slider)
        labelRootView.setPadding(0, 16.dp, 0, 0)
        (titleView.layoutParams as LayoutParams).setMargins(14.dp, 0, 0, 0)
        (summaryView.layoutParams as LayoutParams).setMargins(14.dp, 0, 0, 0)
        (slider.layoutParams as LayoutParams).setMargins(0, (-8).dp, (-2).dp, 0)
        (labelRootView.layoutParams as LayoutParams).setMargins((-14).dp, 0, 0, 0)
        clipChildren = false
        labelRootView.clipChildren = false
    }

    override fun save() {
        key?.let { openDataStore()?.putFloat(it, value) }
    }

    override fun update() {
        key?.let { openDataStore()?.getFloat(it) }?.let {
            value = it
        }
    }
}