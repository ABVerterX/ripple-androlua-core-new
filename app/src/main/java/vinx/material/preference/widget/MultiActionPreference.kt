package vinx.material.preference.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.LinearLayoutCompat
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.divider.MaterialDivider
import vinx.material.button.IconButton

class MultiActionPreference @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleId: Int = 0
) : Preference(context, attrs, defStyleId) {

    private val checkBox = MaterialCheckBox(context)
    private var value: Boolean = false
    private val divider = MaterialDivider(context)
    var onActionClickedListener: OnActionClickedListener? = null
    val actionView = IconButton(
        context,
        null,
        com.google.android.material.R.style.Widget_MaterialComponents_Button_TextButton_Icon
    )
    var actionIcon: Drawable? = null
        set(value) {
            field = value
            value?.let { actionView.setIcon(it) }
        }

    init {
        val newLayout = LinearLayoutCompat(context)
        newLayout.apply {
            gravity = Gravity.CENTER_VERTICAL
            orientation = HORIZONTAL
            isClickable = true
            setPadding(16.dp, 16.dp, 16.dp, 16.dp)
            setRippleBackground()
        }
        removeView(iconFrame)
        newLayout.addView(iconFrame)
        removeView(labelRootView)
        newLayout.addView(labelRootView)
        addView(
            newLayout,
            0,
            LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply { weight = 1f }
        )
        addView(MaterialDivider(context).apply {
            layoutParams = LayoutParams(1.dp, LayoutParams.MATCH_PARENT).apply {
                setMargins(0, 10.dp, 0, 10.dp)
            }
        }, 1)
//        widgetFrame.addView(divider)
        background = null
        widgetFrame.addView(actionView)
        widgetFrame.setPadding(4.dp, 0, 0, 0)
        setPadding(0.dp, 0.dp, 4.dp, 0.dp)
        actionView.setIconSize(24.dp)
        actionView.setOnClickListener {
            onActionClickedListener?.onClick(this)

        }
    }

    interface OnActionClickedListener {
        fun onClick(v: MultiActionPreference)
    }

    override fun save() {}
    override fun update() {}
}