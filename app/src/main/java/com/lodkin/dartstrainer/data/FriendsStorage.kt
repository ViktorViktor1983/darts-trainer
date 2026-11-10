package com.lodkin.dartstrainer.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Friend(
    val nickname: String,
    val note: String
)

object FriendsStorage {
    private const val PREFS_NAME = "friends_prefs"
    private const val KEY_FRIENDS = "friends_list"

    fun getFriends(context: Context): List<Friend> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_FRIENDS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<Friend>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(Friend(obj.getString("nickname"), obj.getString("note")))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveFriends(context: Context, friends: List<Friend>) {
        val arr = JSONArray()
        for (f in friends) {
            val obj = JSONObject().apply {
                put("nickname", f.nickname)
                put("note", f.note)
            }
            arr.put(obj)
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_FRIENDS, arr.toString()).apply()
    }
}
