package vinx.widget.recyclerview

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil

class ListAdapter constructor(private val creator: Creator) :
    androidx.recyclerview.widget.ListAdapter<Any, RecyclerViewHolder>(DiffUtilsCallback(creator)) {

    override fun getItemViewType(position: Int) = creator.getItemViewType(this, position).toInt()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RecyclerViewHolder(creator.onCreateViewHolder(this, parent, viewType))

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) =
        creator.onBindViewHolder(
            this, holder.tag, getItem(position), holder, position
        )

    interface Creator {
        fun getItemViewType(self: ListAdapter, position: Int): Number
        fun onCreateViewHolder(
            self: ListAdapter, parent: ViewGroup, viewType: Int
        ): View

        fun onBindViewHolder(
            self: ListAdapter,
            views: Any?,
            data: Any?,
            holder: RecyclerViewHolder,
            position: Int
        )

        fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean
        fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean
    }

    companion object {
        class DiffUtilsCallback constructor(private val creator: Creator) :
            DiffUtil.ItemCallback<Any>() {
            override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean =
                creator.areItemsTheSame(oldItem, newItem)

            override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean =
                creator.areContentsTheSame(oldItem, newItem)
        }
    }
}