package vinx.graphics;

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.BitmapDrawable
import com.androlua.LuaBitmap
import com.androlua.LuaContext

class IconDrawable private constructor(resources: Resources, bitmap: Bitmap) :
    BitmapDrawable(resources, bitmap) {

    companion object {
        @JvmStatic
        fun create(
            context: LuaContext, filePath: String, color: Int?, size: Int
        ): IconDrawable {
            return createFromBitmap(
                context as Context, LuaBitmap.getBitmap(context, filePath), color, size
            )
        }

        @JvmStatic
        fun createFromBitmap(
            context: Context, bitmap: Bitmap, color: Int?, size: Int
        ): IconDrawable {
            val scale = (size / bitmap.width).toFloat()
            val matrix = Matrix()
            matrix.postScale(scale, scale)
            return IconDrawable(
                context.resources, Bitmap.createBitmap(bitmap, 0, 0, size, size, matrix, true)
            ).setColor(color)
        }
    }

    fun setColor(color: Int?): IconDrawable {
        if (color != null && color != 0) {
            colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        }
        return this
    }
}
