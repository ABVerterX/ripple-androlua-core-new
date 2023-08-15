package vinx.material.preference.widget.simplemenu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import androidx.recyclerview.widget.RecyclerView
import vinx.abverterx.ripplelua.core.R

class SimpleMenuListAdapter(private val mWindow: SimpleMenuPopupWindow) :
    RecyclerView.Adapter<SimpleMenuListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.simple_menu_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mWindow, position)
    }

    override fun getItemCount(): Int {
        return mWindow.entries.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var mCheckedTextView: CheckedTextView
        private var mWindow: SimpleMenuPopupWindow? = null

        init {
            mCheckedTextView = itemView.findViewById(android.R.id.text1)
            itemView.setOnClickListener(this)
        }

        fun bind(window: SimpleMenuPopupWindow?, position: Int) {
            mWindow = window
            mCheckedTextView.text = mWindow!!.entries[position]
            mCheckedTextView.isChecked = position == mWindow!!.selectedIndex
            mCheckedTextView.maxLines =
                if (mWindow!!.mode == SimpleMenuPopupWindow.DIALOG) Int.MAX_VALUE else 1
            val padding = mWindow!!.listPadding[mWindow!!.mode][SimpleMenuPopupWindow.HORIZONTAL]
            val paddingVertical = mCheckedTextView.paddingTop
            mCheckedTextView.setPadding(padding, paddingVertical, padding, paddingVertical)
        }

        override fun onClick(view: View) {
            if (mWindow!!.onItemClickListener != null) {
                mWindow!!.onItemClickListener?.onClick(adapterPosition)
            }
            if (mWindow!!.isShowing) {
                mWindow!!.dismiss()
            }
        }
    }
}