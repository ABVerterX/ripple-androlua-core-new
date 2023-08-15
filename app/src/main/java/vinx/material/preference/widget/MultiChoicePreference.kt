package vinx.material.preference.widget

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.util.SparseBooleanArray
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MultiChoicePreference @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleId: Int = 0
) : Preference(context, attrs, defStyleId) {

    private var value = booleanArrayOf()
//    private var selection = booleanArrayOf()
    private lateinit var selection: SparseBooleanArray

    var items = arrayOf<String>()
        set(value) {
            this.value = BooleanArray(value.size) { false }
//            this.selection = BooleanArray(value.size) { false }
            field = value
        }

    override fun onClick() {
//        update()
        MaterialAlertDialogBuilder(context).setTitle(title)
            .setMultiChoiceItems(items, value, null)
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                selection = (dialog as AlertDialog).listView.checkedItemPositions
                for (i in value.indices) {
                    value[i] = selection.get(i)
                }
                save()
            }.setNegativeButton(android.R.string.cancel, null)
            .setOnDismissListener(null)
            .show()
    }

    override fun save() {
        var data = "["
        for (i in value.indices)
            if (value[i])
                data = "$data$i,"
        data = data.removeSuffix(",")
        data += "]"
        key?.let { openDataStore()?.putString(it, data) }
    }

    override fun update() {
        key?.let { openDataStore()?.getString(it) }?.let { data ->
            val list = data.removePrefix("[").removeSuffix("]").split(",")
            if (list[0] != "")
                for (i in list) value[i.toInt()] = true
        }
    }
}