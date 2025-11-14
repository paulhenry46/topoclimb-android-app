package com.example.topoclimb.cache.dao

import androidx.room.*
import com.example.topoclimb.cache.entity.RouteEntity

@Dao
interface RouteDao {
    @Query("SELECT * FROM routes WHERE id = :id AND backendId = :backendId")
    suspend fun getRouteById(id: Int, backendId: String): RouteEntity?

    @Query("SELECT * FROM routes WHERE siteId = :siteId AND backendId = :backendId")
    suspend fun getRoutesBySite(siteId: Int, backendId: String): List<RouteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: RouteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutes(routes: List<RouteEntity>)

    @Query("DELETE FROM routes WHERE backendId = :backendId")
    suspend fun deleteAllRoutes(backendId: String)

    @Query("DELETE FROM routes WHERE id = :id AND backendId = :backendId")
    suspend fun deleteRoute(id: Int, backendId: String)

    @Query("DELETE FROM routes")
    suspend fun deleteAll()
}
