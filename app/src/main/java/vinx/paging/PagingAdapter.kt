package vinx.paging

import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import vinx.widget.recyclerview.RecyclerViewHolder

class PagingAdapter(private val creator: Creator) : PagingDataAdapter<Any, RecyclerViewHolder>(
    DiffUtilsCallback(creator)
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RecyclerViewHolder(creator.onCreateViewHolder(parent, viewType))

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) =
        creator.onBindViewHolder(
            holder.view.tag, getItem(position), holder, position
        )

    interface Creator {
        fun getItemViewType(position: Int): Number
        fun onCreateViewHolder(
            parent: ViewGroup, viewType: Int
        ): View

        fun onBindViewHolder(
            views: Any?, data: Any?, holder: RecyclerViewHolder, position: Int
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