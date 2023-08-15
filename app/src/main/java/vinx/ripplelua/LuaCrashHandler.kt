package vinx.ripplelua

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION
import android.os.Environment
import android.util.Log
import com.androlua.LuaActivity
import com.androlua.LuaApplication
import com.androlua.Main
import com.androlua.Welcome
import java.io.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class LuaCrashHandler private constructor() : Thread.UncaughtExceptionHandler {
    private var mDefaultHandler: Thread.UncaughtExceptionHandler? = null
    private var mContext: Context? = null
    private val info: MutableMap<String, String> = LinkedHashMap()
    private val formatter: DateFormat = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.PRC)

    fun init(context: Context?) {
        mContext = context
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        //设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(getInstance())
    }

    override fun uncaughtException(thread: Thread, ex: Throwable) {
        if (!handleException(ex) && mDefaultHandler != null) {
            //如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler!!.uncaughtException(thread, ex)
        } else {
            /*try
			{
				Thread.sleep(3000);
			}
			catch (InterruptedException e)
			{
				Log.e(TAG, "error : ", e);
			}
			//退出程序
//			android.os.Process.killProcess(android.os.Process.myPid());
//			System.exit(0);*/
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param throwable
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private fun handleException(throwable: Throwable?): Boolean {
        if (throwable == null) {
            return false
        }
        collectDeviceInfo()

        val crashHandlerLuaActivityFile = File((mContext as LuaApplication).luaDir + "/crash.lua")
        if (crashHandlerLuaActivityFile.exists()) {
            val intent = Intent(mContext, LuaActivity::class.java)
            intent.putExtra("crashReport", saveCrashReport(throwable))
            intent.putExtra("name", crashHandlerLuaActivityFile.path)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            intent.data = Uri.fromFile(crashHandlerLuaActivityFile)
//            for (i in (mContext as LuaApplication).activities) {
//                i.finishAffinity()
//            }
            mContext?.startActivity(intent)
//            saveCrashReport(throwable)
            android.os.Process.killProcess(android.os.Process.myPid())
        } else {
            saveCrashReport(throwable)
        }
        return true
    }

    private fun collectDeviceInfo() {
        try {
            val manager = mContext!!.packageManager
            val packageInfo =
                manager.getPackageInfo(mContext!!.packageName, PackageManager.GET_ACTIVITIES)
            if (packageInfo != null) {
                val versionName = packageInfo.versionName
                val versionCode = packageInfo.versionCode.toString()
                info["versionName"] = versionName
                info["versionCode"] = versionCode
            }
        } catch (exception: PackageManager.NameNotFoundException) {
            Log.e(TAG, "An error occurred when collect package info", exception)
        }

        var fields = Build::class.java.declaredFields
        for (field in fields) {
            try {
                field.isAccessible = true
                val name = field.name
                val obj = field[null]
                if (obj is Array<*>) {
                    info[name] = obj.contentToString()
                } else if (obj != null) {
                    info[name] = obj.toString()
                }
                Log.d(TAG, "$name: $obj")

            } catch (e: Exception) {
                Log.e(TAG, "An error occurred when collect crash info", e)
            }
        }

        fields = VERSION::class.java.declaredFields
        for (field in fields) {
            try {
                field.isAccessible = true
                val name = field.name
                val obj = field[null]
                if (obj is Array<*>) {
                    info[name] = obj.contentToString()
                } else if (obj != null) {
                    info[name] = obj.toString()
                }
                Log.d(TAG, "$name: $obj")

            } catch (e: Exception) {
                Log.e(TAG, "An error occurred when collect crash info", e)
            }
        }
    }

    private fun saveCrashReport(throwable: Throwable): String? {
        val stringBuffer = StringBuffer()
        for ((key, value) in info) {
            stringBuffer.append("$key=$value\n")
        }

        val writer = StringWriter()
        val printWriter = PrintWriter(writer)
        throwable.printStackTrace(printWriter)

        var cause = throwable.cause
        while (cause != null) {
            cause.printStackTrace(printWriter)
            cause = cause.cause
        }
        printWriter.close()

        val result = writer.toString()
        stringBuffer.append(result)

        try {
            val timestamp = System.currentTimeMillis()
            val time = formatter.format(Date())
            val fileName = "crash-$time-$timestamp.log"

            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                val dirFile = mContext?.getExternalFilesDir("crash")
                if (dirFile != null) {
                    if (!dirFile.exists()) dirFile.mkdirs()
                }

                val file = File((dirFile?.absolutePath) + "/" + fileName)
                if (!file.exists()) file.createNewFile()

                val outputStream = FileOutputStream(file)
                outputStream.write(stringBuffer.toString().toByteArray())
                outputStream.close()
                Log.e("Crash", stringBuffer.toString())
            }
            return fileName
        } catch (e: Exception) {
            Log.e(TAG, "An error occurred while writing file...", e)
        }
        return null
    }

    companion object {
        const val TAG = "LuaCrashHandler"

        @SuppressLint("StaticFieldLeak")
        val INSTANCE = LuaCrashHandler()

        @JvmStatic
        fun getInstance(): LuaCrashHandler {
            return INSTANCE
        }
    }
}