package vinx.material.preference.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import com.google.android.material.checkbox.MaterialCheckBox

class TipPreference @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleId: Int = 0
) : Preference(context, attrs, defStyleId) {


    init {
//        icon = android.R.drawable.ic_info
        removeView(titleView)
        iconFrame.translationY = 16.dp.toFloat()
        iconFrame.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        gravity = Gravity.START
    }

    override fun save() {}
    override fun update() {}
}