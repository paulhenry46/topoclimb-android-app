package com.example.topoclimb.database.converters

import androidx.room.TypeConverter
import com.example.topoclimb.data.GradingSystem
import com.google.gson.Gson

class GradingSystemConverter {
    private val gson = Gson()
    
    @TypeConverter
    fun fromGradingSystem(value: GradingSystem?): String? {
        return value?.let { gson.toJson(it) }
    }
    
    @TypeConverter
    fun toGradingSystem(value: String?): GradingSystem? {
        return value?.let { gson.fromJson(it, GradingSystem::class.java) }
    }
}
