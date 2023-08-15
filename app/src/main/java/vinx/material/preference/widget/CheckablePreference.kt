package vinx.material.preference.widget

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.checkbox.MaterialCheckBox

@Deprecated("")
class CheckablePreference @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleId: Int = 0
) : Preference(context, attrs, defStyleId) {

    private val checkBox = MaterialCheckBox(context)
    private var value: Boolean = false

    init {
        setOnClickListener {
            checkBox.toggle()
        }
        checkBox.setOnCheckedChangeListener { _, checked ->
            value = checked
        }
        widgetFrame.addView(checkBox)
    }


    fun setChecked(checked: Boolean) {
        value = checked
    }

    fun isChecked(): Boolean {
        return value
    }

    override fun save() {}
    override fun update() {}
}