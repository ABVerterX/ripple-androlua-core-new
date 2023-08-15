package vinx.material.preference.widget

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.switchmaterial.SwitchMaterial

class SwitchPreference @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleId: Int = 0
) : Preference(context, attrs, defStyleId) {

    private lateinit var switchCompat: SwitchCompat
    private var value: Boolean = false

    companion object {
        private val STYLE_CLASS = Class.forName("com.google.android.material.R${'$'}style")
//        private var M3_THEME = ATTR_CLASS.getDeclaredField("Theme_Material3_DayNight").getInt(ATTR_CLASS)
    }

    init {
        setMaterial3StyleEnabled(false)
//        com.google.android.material.R.style.
    }

    fun setChecked(checked: Boolean) {
        value = checked
        switchCompat.isChecked = checked
        save()
    }

    fun isChecked(): Boolean {
        return value
    }

    fun setMaterial3StyleEnabled(enabled: Boolean) {
        widgetFrame.removeAllViews()
        if (!enabled) {
            widgetFrame.addView(SwitchMaterial(context))
        } else if (Class.forName("com.google.android.material.materialswitch.MaterialSwitch") != null) {
            widgetFrame.addView(
                MaterialSwitch(
                    ContextThemeWrapper(
                        context,
                        STYLE_CLASS.getDeclaredField("Theme_Material3_DayNight").getInt(STYLE_CLASS)
                    )
                )
            )
        } else {
            throw UnsupportedOperationException("The MDC library doesn't include MaterialSwitch! ")
        }

        switchCompat = widgetFrame.getChildAt(0) as SwitchCompat
        switchCompat.isClickable = false
    }

    override fun onClick() {
        switchCompat.toggle()
        value = switchCompat.isChecked
        save()
    }

    override fun save() {
        key?.let { openDataStore()?.putBoolean(it, value) }
    }

    override fun update() {
        key?.let { openDataStore()?.getBoolean(it) }?.let {
            value = it
            setChecked(it)
        }
    }
}