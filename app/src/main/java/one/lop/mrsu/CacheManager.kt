import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import one.lop.mrsu.model.Group
import java.text.SimpleDateFormat
import java.util.*

class CacheManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("schedule_cache", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveGroupsForDate(date: String, groups: List<Group>) {
        if (groups.isNotEmpty() && groups.any { it.group != null && it.timeTable != null }) {
            val json = gson.toJson(groups)
            sharedPreferences.edit().putString("cached_$date", json).apply()
            Log.d("CacheManager", "Saved groups for date $date: ${groups.size} groups")
        } else {
            Log.w("CacheManager", "Attempted to save invalid group list for date $date: ${groups}")
        }
        logFullCache()
    }

    fun getGroupsForDate(date: String): List<Group>? {
        val json = sharedPreferences.getString("cached_$date", null)
        if (json != null) {
            try {
                val type = object : TypeToken<List<Group>>() {}.type
                val groups: List<Group> = gson.fromJson(json, type)
                if (groups.any { it.group != null && it.timeTable != null }) {
                    Log.d("CacheManager", "Retrieved groups for date $date: ${groups.size} groups")
                    groups.forEach { group ->
                        Log.d("CacheManager", "Group: $group")
                    }
                    return groups
                } else {
                    Log.w("CacheManager", "Invalid data retrieved from cache for date $date. Groups: $groups")
                }
            } catch (e: Exception) {
                Log.e("CacheManager", "Failed to deserialize groups for date $date: ${e.message}")
            }
        } else {
            Log.w("CacheManager", "No cached data for date $date")
        }
        logFullCache()
        return null
    }

    fun clearOldCache() {
        val validDates = getValidDateKeys()
        val allKeys = sharedPreferences.all.keys
        val keysToRemove = allKeys - validDates
        val editor = sharedPreferences.edit()
        keysToRemove.forEach {
            Log.d("CacheManager", "Removed outdated cache for key: $it")
            editor.remove(it)
        }
        editor.apply()
    }

    private fun getValidDateKeys(): Set<String> {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return (0..6).map {
            calendar.add(Calendar.DAY_OF_YEAR, if (it < 3) -it else it - 3)
            "cached_${dateFormat.format(calendar.time)}"
        }.toSet()
    }

    private fun logFullCache() {
        val allEntries = sharedPreferences.all
        Log.d("CacheManager", "Full cache content:")
        for ((key, value) in allEntries) {
            val entrySize = value.toString().length
            Log.d("CacheManager", "Key: $key, Size: $entrySize, Value (first 100 chars): ${value.toString().take(100)}")
        }
    }
}
