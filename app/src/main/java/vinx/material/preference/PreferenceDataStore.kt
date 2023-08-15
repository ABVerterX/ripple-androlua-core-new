package vinx.material.preference

interface PreferenceDataStore {
    fun putString(key: String, value: String)
    fun getString(key: String): String

    fun putInt(key: String, value: Int)
    fun getInt(key: String): Int

    fun putFloat(key: String, value: Float)
    fun getFloat(key: String): Float

    fun putLong(key: String, value: Long)
    fun getLong(key: String): Long

//    fun putMap(key: String, value: Map<*, *>)
//    fun getMap(key: String): Map<*, *>
//
//    fun putArray(key: String, value: Array<*>)
//    fun getArray(key: String): Array<*>

    fun putBoolean(key: String, value: Boolean)
    fun getBoolean(key: String): Boolean

    fun putSerializableObject(key: String, value: java.io.Serializable)
    fun getSerializableObject(key: String): java.io.Serializable
}