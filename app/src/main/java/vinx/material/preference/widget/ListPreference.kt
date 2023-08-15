package vinx.material.preference.widget

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ListPreference @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleId: Int = 0
) : Preference(context, attrs, defStyleId) {

    private val dialog = MaterialAlertDialogBuilder(context)
    var onSelectedListener: OnSelectedListener? = null
    var items = arrayOf<String>()
        set(value) {
            dialog.setItems(value) { _, i ->
                onSelectedListener?.onSelected(i, items[i])
            }
            field = value
        }

    override fun onClick() {
        dialog.setTitle(title).show()
    }

    override fun save() {}

    override fun update() {}

    interface OnSelectedListener {
        fun onSelected(pos: Int, value: String)
    }
}