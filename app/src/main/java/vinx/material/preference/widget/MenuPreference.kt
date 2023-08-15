package vinx.material.preference.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.appcompat.view.ContextThemeWrapper
import vinx.abverterx.ripplelua.core.R
import vinx.material.preference.widget.simplemenu.SimpleMenuPopupWindow

@SuppressLint("CustomViewStyleable")
class MenuPreference @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
) : Preference(context, attrs, defStyleAttr) {

    private var value = -1
    private var popup: SimpleMenuPopupWindow

    var items = arrayOf<CharSequence>()
        set(value) {
            field = value
            popup.entries = value
        }

    init {
        val typed: TypedArray = context.obtainStyledAttributes(
            attrs, R.styleable.SimpleMenuPreference, defStyleAttr, defStyleRes
        )
        val popupTheme = typed.getResourceId(
            R.styleable.SimpleMenuPreference_android_popupTheme,
            R.style.ThemeOverlay_Preference_SimpleMenuPreference_PopupMenu
        )
        typed.recycle()
        val popupContext = if (popupTheme != 0) {
            ContextThemeWrapper(context, popupTheme)
        } else {
            context
        }
        popup = SimpleMenuPopupWindow(popupContext)
        popup.onItemClickListener = object : SimpleMenuPopupWindow.OnItemClickListener {
            override fun onClick(i: Int) {
                value = i
                save()
            }
        }
    }

    override fun onClick() {
        popup.selectedIndex = value
        popup.requestMeasure()
        this.parent.let { popup.show(this, it as View, iconFrame.width) }
    }

    override fun save() {
        key?.let { openDataStore()?.putInt(it, value) }
    }

    override fun update() {
        key?.let { openDataStore()?.getInt(it) }?.let {
            value = it
////            Log.i("ssssssssssssssss", it.toString())
        }
    }
}