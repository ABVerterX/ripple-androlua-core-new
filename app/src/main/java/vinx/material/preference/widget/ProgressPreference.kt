package vinx.material.preference.widget

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.progressindicator.LinearProgressIndicator

class ProgressPreference @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleId: Int = 0
) : Preference(context, attrs, defStyleId) {

    val slider = LinearProgressIndicator(context)
    var value = 0
        set(value) {
            field = value
            slider.progress = field
        }

    init {
        setPadding(16.dp, 16.dp, 0, 16.dp)
        labelRootView.addView(slider)
        (slider.layoutParams as LayoutParams).setMargins(0, 8.dp, 0, 0)
    }

    override fun save() {
        key?.let { openDataStore()?.putInt(it, value) }
    }

    override fun update() {
        key?.let { openDataStore()?.getInt(it) }?.let {
            value = it
        }
    }
}