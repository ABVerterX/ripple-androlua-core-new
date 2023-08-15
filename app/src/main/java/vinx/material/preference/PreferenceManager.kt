package vinx.material.preference

import vinx.material.preference.widget.BasePreference

class PreferenceManager(val dataStore: PreferenceDataStore) {
    private val preferenceSet = mutableSetOf<BasePreference>()

    fun findPreferenceByKey(key: String): BasePreference? {
        for (v in preferenceSet) {
            if (v.key == key) return v
        }
        return null
    }

    fun add(preference: BasePreference): PreferenceManager {
        preference.manager = this
        preferenceSet.add(preference)
        return this
    }

    fun remove(preference: BasePreference): PreferenceManager {
        preference.manager = null
        preferenceSet.remove(preference)
        return this
    }

    fun commit() {
        for (v in preferenceSet) {
//            v.commit()
        }
    }
}