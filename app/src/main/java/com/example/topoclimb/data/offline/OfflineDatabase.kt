package com.example.topoclimb.data.offline

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        OfflineSiteEntity::class,
        OfflineAreaEntity::class,
        OfflineRouteEntity::class,
        OfflineContestEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class OfflineDatabase : RoomDatabase() {
    
    abstract fun offlineSiteDao(): OfflineSiteDao
    abstract fun offlineAreaDao(): OfflineAreaDao
    abstract fun offlineRouteDao(): OfflineRouteDao
    abstract fun offlineContestDao(): OfflineContestDao
    
    companion object {
        @Volatile
        private var INSTANCE: OfflineDatabase? = null
        
        fun getDatabase(context: Context): OfflineDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OfflineDatabase::class.java,
                    "topoclimb_offline_database"
                )
                .fallbackToDestructiveMigration() // Allow database migration
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
