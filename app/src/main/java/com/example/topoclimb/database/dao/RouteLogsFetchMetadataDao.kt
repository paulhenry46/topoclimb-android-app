package com.example.topoclimb.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.topoclimb.database.entities.RouteLogsFetchMetadataEntity

@Dao
interface RouteLogsFetchMetadataDao {
    @Query("SELECT * FROM route_logs_fetch_metadata WHERE routeId = :routeId AND backendId = :backendId")
    suspend fun getRouteLogsFetchMetadata(routeId: Int, backendId: String): RouteLogsFetchMetadataEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRouteLogsFetchMetadata(metadata: RouteLogsFetchMetadataEntity)
    
    @Query("DELETE FROM route_logs_fetch_metadata WHERE routeId = :routeId AND backendId = :backendId")
    suspend fun deleteRouteLogsFetchMetadata(routeId: Int, backendId: String)
    
    @Query("DELETE FROM route_logs_fetch_metadata WHERE backendId = :backendId")
    suspend fun deleteAllRouteLogsFetchMetadata(backendId: String)
}
