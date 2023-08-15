package vinx.material.preference.widget

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.LinearLayoutCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import vinx.material.textfield.MaterialTextField


class TextFieldPreference @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleId: Int = 0
) : Preference(context, attrs, defStyleId) {

    private var value: String = ""
    private lateinit var editText: MaterialTextField

    private fun showKeyboard(editText: TextInputEditText) {
        (editText.parent as View).isFocusable = true
        (editText.parent as View).isFocusableInTouchMode = true
        editText.requestFocus()
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(
            editText, InputMethodManager.SHOW_FORCED
        )
    }

    override fun onClick() {
        editText = MaterialTextField(context)
        editText.setText(value)
        val dialog = MaterialAlertDialogBuilder(context).setTitle(title)
            .setView(LinearLayoutCompat(context).apply { addView(editText) })
            .setPositiveButton(android.R.string.ok) { _, _ ->
                value = editText.getText().toString()
                save()
                (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
                    editText.windowToken, 0
                )
            }.setNegativeButton(android.R.string.cancel) { _, _ ->
                (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
                    editText.windowToken, 0
                )
            }.create()

        editText.setText(value)
        editText.setSelection(value.length)
        editText.requestFocus()

        dialog.setOnShowListener {
            Thread {
                SystemClock.sleep(25)
                (context as Activity).runOnUiThread {
                    showKeyboard(editText.editText as TextInputEditText)
                }
            }.start()
        }
        dialog.show()

        editText.layoutParams =
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                leftMargin = 24.dp
                rightMargin = 24.dp
                topMargin = 8.dp
            }
    }

    override fun save() {
        key?.let { openDataStore()?.putString(it, value) }
    }

    override fun update() {
        key?.let { openDataStore()?.getString(it) }?.let {
            value = it
//            editText.setText(it)
        }
    }
}