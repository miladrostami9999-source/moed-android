package com.miladrostami.moed

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** A category the user created once and can pick again for future payments. */
data class CustomCategory(val name: String, val iconKey: String)

/**
 * Keeps the user's custom categories on this phone.
 * They live next to the app settings, not in the encrypted payment database, so adding
 * them never needs a database migration.
 */
class CustomCategoryStore(context: Context) {
    private val prefs = context.getSharedPreferences("mento_categories", Context.MODE_PRIVATE)

    fun load(): List<CustomCategory> = runCatching {
        val array = JSONArray(prefs.getString(KEY, "[]"))
        (0 until array.length()).mapNotNull { index ->
            val item = array.optJSONObject(index) ?: return@mapNotNull null
            val name = item.optString("name").trim()
            if (name.isEmpty()) null else CustomCategory(name, item.optString("icon").ifEmpty { "more" })
        }
    }.getOrDefault(emptyList())

    fun save(list: List<CustomCategory>) {
        val array = JSONArray()
        list.forEach { array.put(JSONObject().put("name", it.name).put("icon", it.iconKey)) }
        prefs.edit().putString(KEY, array.toString()).apply()
    }

    private companion object { const val KEY = "custom" }
}

/** Adds [name] or, if it already exists (ignoring case), updates its icon. Built-in and blank names are ignored. */
internal fun upsertCustomCategory(
    list: List<CustomCategory>,
    name: String,
    iconKey: String,
    reserved: Collection<String>
): List<CustomCategory> {
    val clean = name.trim()
    if (clean.isEmpty() || reserved.any { it.equals(clean, ignoreCase = true) }) return list
    val index = list.indexOfFirst { it.name.equals(clean, ignoreCase = true) }
    return if (index >= 0) list.mapIndexed { i, c -> if (i == index) c.copy(iconKey = iconKey) else c }
    else list + CustomCategory(clean, iconKey)
}

/**
 * Categories already used by payments (name to icon) that are not saved yet,
 * for example after restoring a backup or updating from an older version.
 */
internal fun missingCustomCategories(
    saved: List<CustomCategory>,
    used: List<Pair<String, String>>,
    reserved: Collection<String>
): List<CustomCategory> {
    val known = (saved.map { it.name } + reserved).map { it.lowercase() }.toMutableSet()
    return used.mapNotNull { (name, icon) ->
        val clean = name.trim()
        if (clean.isEmpty() || !known.add(clean.lowercase())) null else CustomCategory(clean, icon)
    }
}
