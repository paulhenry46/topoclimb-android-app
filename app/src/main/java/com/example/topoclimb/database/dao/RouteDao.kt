package com.example.topoclimb.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.topoclimb.database.entities.RouteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteDao {
    @Query("SELECT * FROM routes WHERE backendId = :backendId ORDER BY name")
    fun getAllRoutesFlow(backendId: String): Flow<List<RouteEntity>>
    
    @Query("SELECT * FROM routes WHERE backendId = :backendId ORDER BY name")
    suspend fun getAllRoutes(backendId: String): List<RouteEntity>
    
    @Query("SELECT * FROM routes WHERE id = :id AND backendId = :backendId")
    fun getRouteFlow(id: Int, backendId: String): Flow<RouteEntity?>
    
    @Query("SELECT * FROM routes WHERE id = :id AND backendId = :backendId")
    suspend fun getRoute(id: Int, backendId: String): RouteEntity?
    
    @Query("SELECT * FROM routes WHERE siteId = :siteId AND backendId = :backendId ORDER BY name")
    fun getRoutesBySiteFlow(siteId: Int, backendId: String): Flow<List<RouteEntity>>
    
    @Query("SELECT * FROM routes WHERE siteId = :siteId AND backendId = :backendId ORDER BY name")
    suspend fun getRoutesBySite(siteId: Int, backendId: String): List<RouteEntity>
    
    @Query("SELECT * FROM routes WHERE lineId = :lineId AND backendId = :backendId ORDER BY name")
    fun getRoutesByLineFlow(lineId: Int, backendId: String): Flow<List<RouteEntity>>
    
    @Query("SELECT * FROM routes WHERE lineId = :lineId AND backendId = :backendId ORDER BY name")
    suspend fun getRoutesByLine(lineId: Int, backendId: String): List<RouteEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutes(routes: List<RouteEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: RouteEntity)
    
    @Query("DELETE FROM routes WHERE backendId = :backendId")
    suspend fun deleteAllRoutes(backendId: String)
}
