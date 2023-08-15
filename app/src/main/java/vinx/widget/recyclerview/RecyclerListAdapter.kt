package vinx.widget.recyclerview

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

class RecyclerListAdapter constructor(private val creator: Creator) :
    ListAdapter<Any, RecyclerViewHolder>(DiffUtilsCallback(creator)) {

    override fun getItemViewType(position: Int) = creator.getItemViewType(getItem(position), position).toInt()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RecyclerViewHolder(creator.onCreateViewHolder(parent, viewType))

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) =
        creator.onBindViewHolder(
            holder.view.tag, getItem(position), holder, position
        )

    interface Creator {
        fun getItemViewType(
//            self: RecyclerListAdapterDelegate,
            data: Any?,
            position: Int
        ): Number

        fun onCreateViewHolder(
//            self: RecyclerListAdapterDelegate,
            parent: ViewGroup, viewType: Int
        ): View

        fun onBindViewHolder(
//            self: RecyclerListAdapterDelegate,
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