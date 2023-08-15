package vinx.ripplelua

import android.util.Log
import com.luajava.LuaTable

class LuaEx {
}

fun LuaTable<Int, Any?>.toList(): List<Any> {
    return run {
        val list = arrayListOf<Any>()
        for (i in 1..this.length()) {
            this[i]?.let { list.add(it) }
        }
        list
    }
}