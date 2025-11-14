package com.example.topoclimb.cache.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class StringListConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.let {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(it, type)
        }
    }
}

class StringMapConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromStringMap(value: Map<String, Int>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toStringMap(value: String?): Map<String, Int>? {
        return value?.let {
            val type = object : TypeToken<Map<String, Int>>() {}.type
            gson.fromJson(it, type)
        }
    }
}
