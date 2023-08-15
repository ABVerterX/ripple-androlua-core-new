package vinx.paging

import android.util.Log
import androidx.paging.PagingState
import vinx.ripplelua.toList

internal class PagingSource(private val creator: Paging.Creator, var lastPage: Int) : androidx.paging.PagingSource<Int, Any>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Any> {
        return try {
            val page = params.key ?: 1
            LoadResult.Page(
                creator.load(this, page).toList(), null, if (page >= lastPage) null else (page + 1)
            )
        } catch (e: Exception) {
            creator.onFailure(this, e) ?: LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Any>): Int? {
        return null
//        return state.anchorPosition?.let { anchorPosition ->
//            state.closestItemToPosition(anchorPosition) as Int
//        }
    }

}