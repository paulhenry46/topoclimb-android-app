package com.example.topoclimb.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.topoclimb.database.converters.GradingSystemConverter
import com.example.topoclimb.database.converters.StringListConverter
import com.example.topoclimb.database.dao.AreaDao
import com.example.topoclimb.database.dao.ContestDao
import com.example.topoclimb.database.dao.LineDao
import com.example.topoclimb.database.dao.LineFetchMetadataDao
import com.example.topoclimb.database.dao.LogDao
import com.example.topoclimb.database.dao.RouteDao
import com.example.topoclimb.database.dao.RouteLogsFetchMetadataDao
import com.example.topoclimb.database.dao.SectorDao
import com.example.topoclimb.database.dao.SiteDao
import com.example.topoclimb.database.dao.SvgMapCacheDao
import com.example.topoclimb.database.entities.AreaEntity
import com.example.topoclimb.database.entities.ContestEntity
import com.example.topoclimb.database.entities.LineEntity
import com.example.topoclimb.database.entities.LineFetchMetadataEntity
import com.example.topoclimb.database.entities.LogEntity
import com.example.topoclimb.database.entities.RouteEntity
import com.example.topoclimb.database.entities.RouteLogsFetchMetadataEntity
import com.example.topoclimb.database.entities.SectorEntity
import com.example.topoclimb.database.entities.SiteEntity
import com.example.topoclimb.database.entities.SvgMapCacheEntity

@Database(
    entities = [
        SiteEntity::class,
        AreaEntity::class,
        RouteEntity::class,
        ContestEntity::class,
        SectorEntity::class,
        LineEntity::class,
        SvgMapCacheEntity::class,
        LineFetchMetadataEntity::class,
        LogEntity::class,
        RouteLogsFetchMetadataEntity::class
    ],
    version = 8,
    exportSchema = false
)
@TypeConverters(
    StringListConverter::class,
    GradingSystemConverter::class
)
abstract class TopoClimbDatabase : RoomDatabase() {
    
    abstract fun siteDao(): SiteDao
    abstract fun areaDao(): AreaDao
    abstract fun routeDao(): RouteDao
    abstract fun contestDao(): ContestDao
    abstract fun sectorDao(): SectorDao
    abstract fun lineDao(): LineDao
    abstract fun svgMapCacheDao(): SvgMapCacheDao
    abstract fun lineFetchMetadataDao(): LineFetchMetadataDao
    abstract fun logDao(): LogDao
    abstract fun routeLogsFetchMetadataDao(): RouteLogsFetchMetadataDao
    
    companion object {
        @Volatile
        private var INSTANCE: TopoClimbDatabase? = null
        
        fun getDatabase(context: Context): TopoClimbDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TopoClimbDatabase::class.java,
                    "topoclimb_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                android.util.Log.d("OfflineFirst", "TopoClimbDatabase initialized (version 8 with route logs caching)")
                INSTANCE = instance
                instance
            }
        }
    }
}
