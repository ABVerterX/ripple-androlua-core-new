package vinx.paging

import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import androidx.paging.PagingSource
import com.androlua.LuaActivity
import com.luajava.LuaTable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class Paging @JvmOverloads constructor(
    private val activity: LuaActivity,
    size: Int,
    lastPage: Int = Int.MAX_VALUE,
    private val creator: Creator,
) {
    var flow: Flow<PagingData<Any>>

    init {
        flow = Pager(PagingConfig(size)) {
            PagingSource(creator, Int.MAX_VALUE)
        }.flow.cachedIn(activity.viewModel.viewModelScope)
    }

    interface Creator {
        fun load(self: PagingSource<Int, Any>, page: Int): LuaTable<Int, Any?>
        fun onFailure(self: PagingSource<Int, Any>, e: Exception): PagingSource.LoadResult.Error<Int, Any>?
    }

    fun submitTo(adapter: PagingAdapter) {
        activity.lifecycleScope.launch {
            flow.collect { pagingData ->
                adapter.submitData(pagingData)
            }
        }
    }
}