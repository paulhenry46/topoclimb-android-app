package com.example.topoclimb.data.offline

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface OfflineRouteDao {
    
    @Query("SELECT * FROM offline_routes WHERE siteId = :siteId")
    suspend fun getRoutesBySite(siteId: Int): List<OfflineRouteEntity>
    
    @Query("SELECT * FROM offline_routes WHERE id = :routeId")
    suspend fun getRouteById(routeId: Int): OfflineRouteEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: OfflineRouteEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutes(routes: List<OfflineRouteEntity>)
    
    @Query("DELETE FROM offline_routes WHERE siteId = :siteId")
    suspend fun deleteRoutesBySite(siteId: Int)
    
    @Query("DELETE FROM offline_routes")
    suspend fun deleteAllRoutes()
}
