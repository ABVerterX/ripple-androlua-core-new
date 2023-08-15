package vinx.material.preference.widget

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class SingleChoicePreference @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleId: Int = 0
) : Preference(context, attrs, defStyleId) {

    private var value = -1
    private val dialog = MaterialAlertDialogBuilder(context)
    var items = arrayOf<String>()

    override fun onClick() {
        var pos = -1
        dialog
            .setTitle(title)
            .setSingleChoiceItems(items, value) { _, i -> pos = i }
            .setPositiveButton(android.R.string.ok) { _, _ ->
                value = pos
                save()
            }.setNegativeButton(android.R.string.cancel, null)
            .show()
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