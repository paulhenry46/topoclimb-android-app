package com.example.topoclimb.data.offline

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        OfflineSiteEntity::class,
        OfflineAreaEntity::class,
        OfflineRouteEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class OfflineDatabase : RoomDatabase() {
    
    abstract fun offlineSiteDao(): OfflineSiteDao
    abstract fun offlineAreaDao(): OfflineAreaDao
    abstract fun offlineRouteDao(): OfflineRouteDao
    
    companion object {
        @Volatile
        private var INSTANCE: OfflineDatabase? = null
        
        fun getDatabase(context: Context): OfflineDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OfflineDatabase::class.java,
                    "topoclimb_offline_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
