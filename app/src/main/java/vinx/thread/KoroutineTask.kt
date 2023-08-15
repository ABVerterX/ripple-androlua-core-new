package vinx.thread

import com.androlua.*
import com.luajava.*
import kotlinx.coroutines.*

@OptIn(DelicateCoroutinesApi::class)
class KoroutineTask(
    private val luaContext: LuaContext,
    luaFunction: LuaObject,
    private val luaCallback: LuaObject,
    private val delay: Long,
    arguments: Array<Any>
): LuaGcable {
    private val luaState: LuaState = LuaStateFactory.newLuaState()
    private var loaded: Array<Any>? = null
    private var buffer: ByteArray? = null
    private var gc = false

    init {
        luaContext.regGc(this)
        buffer = luaFunction.dump()
        val l = luaContext.luaState
        val g = l.getLuaObject("luajava")
        val loaded = g.getField("imported")
        if (!loaded.isNil) {
            this.loaded = loaded.asArray()
        }

        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                Thread.sleep(delay)
            }
            val args = run(arguments)
            luaCallback.call(args)
        }
    }

    private fun run(arguments: Array<Any>): Any? {
        with(luaState) {
            openLibs()
            pushJavaObject(luaContext)
            setGlobal(
                when (luaContext) {
                    is LuaActivity -> "activity"
                    is LuaService -> "service"
                    else -> {
                        throw UnsupportedOperationException()
                    }
                }
            )

            pushJavaObject(this)
            setGlobal("this")

            pushContext(luaContext)

            getGlobal("luajava")
            pushString(luaContext.luaDir)
            setField(-2, "luadir")
            pop(1)
        }

        try {
            val print: JavaFunction = LuaPrint(luaContext, luaState)
            print.register("print")

            val update: JavaFunction = object : JavaFunction(luaState) {
                @Throws(LuaException::class)
                override fun execute(): Int {
                    update(L.toJavaObject(2))
                    return 0
                }
            }
            update.register("update")

            with(luaState) {
                getGlobal("package")

                pushString(luaContext.luaLPath)
                setField(-2, "path")

                pushString(luaContext.luaCpath)
                setField(-2, "cpath")

                pop(1)
            }
        } catch (e: LuaException) {
            luaContext.sendError("AsyncTask", e)
        }

        if (loaded != null) {
            val require = luaState.getLuaObject("require")
            try {
                require.call("import")
                val import: LuaObject = luaState.getLuaObject("import")
                for (s in loaded!!) import.call(s.toString())
            } catch (_: LuaException) {
            }
        }

        try {
            luaState.top = 0
            var ok: Int = luaState.LloadBuffer(buffer, "LuaAsyncTask")
            if (ok == 0) {
                luaState.getGlobal("debug")
                luaState.getField(-1, "traceback")
                luaState.remove(-2)
                luaState.insert(-2)
                val l: Int = arguments.size
                for (arg in arguments) {
                    luaState.pushObjectValue(arg)
                }
                ok = luaState.pcall(l, LuaState.LUA_MULTRET, -2 - l)
                if (ok == 0) {
                    val n: Int = luaState.top - 1
                    val ret = arrayOfNulls<Any>(n)
                    for (i in 0 until n) ret[i] = luaState.toJavaObject(i + 2)
                    return ret
                }
            }
            throw LuaException(errorReason(ok) + ": " + luaState.toString(-1))
        } catch (e: Exception) {
            luaContext.sendError("doInBackground", e)
        }

        return null
    }

    fun update(msg: Any?) {
//        publishProgress(msg)
    }

    fun update(msg: String?) {
//        publishProgress(msg)
    }

    fun update(msg: Int) {
//        publishProgress(msg)
    }

    private fun errorReason(error: Int): String {
        when (error) {
            6 -> return "error error"
            5 -> return "GC error"
            4 -> return "Out of memory"
            3 -> return "Syntax error"
            2 -> return "Runtime error"
            1 -> return "Yield error"
        }
        return "Unknown error $error"
    }

    override fun gc() {
//        if (getStatus() == AsyncTaskX.Status.RUNNING) cancel(true)
        gc = true
    }

    override fun isGc(): Boolean {
        return gc
    }
}