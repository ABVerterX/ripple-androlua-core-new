package vinx.material.preference.widget

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.checkbox.MaterialCheckBox

class CheckBoxPreference @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleId: Int = 0
) : Preference(context, attrs, defStyleId) {

    private val checkBox = MaterialCheckBox(context)
    private var value: Boolean = false

    init {
        checkBox.isClickable = false
        widgetFrame.addView(checkBox)
        setPadding(16.dp, 16.dp, 6.dp, 16.dp)
    }

    override fun onClick() {
        checkBox.toggle()
        value = checkBox.isChecked
    }

    fun setChecked(checked: Boolean) {
        value = checked
        checkBox.isChecked = checked
        save()
    }

    fun isChecked(): Boolean {
        return value
    }

    fun submit() {
        key?.let { openDataStore()?.putBoolean(it, value) }
    }

    override fun save() {}

    override fun update() {
        key?.let { openDataStore()?.getBoolean(it) }?.let {
            value = it
            setChecked(it)
        }
    }
}