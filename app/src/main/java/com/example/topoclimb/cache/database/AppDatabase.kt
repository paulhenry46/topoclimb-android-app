package com.example.topoclimb.cache.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.topoclimb.cache.dao.*
import com.example.topoclimb.cache.entity.*

@Database(
    entities = [
        SiteEntity::class,
        AreaEntity::class,
        RouteEntity::class,
        SectorEntity::class,
        LineEntity::class,
        SectorSchemaEntity::class,
        ContestEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(StringListConverter::class, StringMapConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun siteDao(): SiteDao
    abstract fun areaDao(): AreaDao
    abstract fun routeDao(): RouteDao
    abstract fun sectorDao(): SectorDao
    abstract fun lineDao(): LineDao
    abstract fun sectorSchemaDao(): SectorSchemaDao
    abstract fun contestDao(): ContestDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "topoclimb_cache_database"
                )
                .fallbackToDestructiveMigration() // For development - will clear cache on schema change
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
