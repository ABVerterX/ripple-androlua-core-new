package vinx.material.preference.widget

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.textview.MaterialTextView

class EndTextPreference @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleId: Int = 0
) : Preference(context, attrs, defStyleId) {

    private val textView = MaterialTextView(context)
    private var value: Boolean = false
    var endText: String? = null
        set(value) {
            textView.text = value
            field = value
        }

    init {
        setOnClickListener {
        }
        widgetFrame.addView(textView)
    }

    override fun save() {}
    override fun update() {}
}