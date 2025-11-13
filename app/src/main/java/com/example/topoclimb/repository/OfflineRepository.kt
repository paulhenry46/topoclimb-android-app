package com.example.topoclimb.repository

import android.content.Context
import com.example.topoclimb.data.Area
import com.example.topoclimb.data.Contest
import com.example.topoclimb.data.Federated
import com.example.topoclimb.data.Route
import com.example.topoclimb.data.Site
import com.example.topoclimb.data.offline.OfflineAreaEntity
import com.example.topoclimb.data.offline.OfflineContestEntity
import com.example.topoclimb.data.offline.OfflineDatabase
import com.example.topoclimb.data.offline.OfflineRouteEntity
import com.example.topoclimb.data.offline.OfflineSiteEntity

/**
 * Repository for managing offline cached data
 */
class OfflineRepository(context: Context) {
    
    private val database = OfflineDatabase.getDatabase(context)
    private val siteDao = database.offlineSiteDao()
    private val areaDao = database.offlineAreaDao()
    private val routeDao = database.offlineRouteDao()
    private val contestDao = database.offlineContestDao()
    
    // Sites
    suspend fun cacheSite(site: Site, backendId: String, backendName: String) {
        val entity = OfflineSiteEntity.fromSite(site, backendId, backendName)
        siteDao.insertSite(entity)
    }
    
    suspend fun getCachedSites(): List<Federated<Site>> {
        return siteDao.getAllSites().map { entity ->
            Federated(
                data = entity.toSite(),
                backend = com.example.topoclimb.data.BackendMetadata(
                    backendId = entity.backendId,
                    backendName = entity.backendName,
                    baseUrl = ""
                )
            )
        }
    }
    
    suspend fun getCachedSite(siteId: Int): Site? {
        return siteDao.getSiteById(siteId)?.toSite()
    }
    
    suspend fun removeCachedSite(siteId: Int) {
        siteDao.deleteSite(siteId)
        areaDao.deleteAreasBySite(siteId)
        routeDao.deleteRoutesBySite(siteId)
        contestDao.deleteContestsBySite(siteId)
    }
    
    suspend fun isSiteCached(siteId: Int): Boolean {
        return siteDao.isSiteCached(siteId) > 0
    }
    
    // Areas
    suspend fun cacheAreas(areas: List<Area>) {
        val entities = areas.map { OfflineAreaEntity.fromArea(it) }
        areaDao.insertAreas(entities)
    }
    
    suspend fun getCachedAreas(siteId: Int): List<Area> {
        return areaDao.getAreasBySite(siteId).map { it.toArea() }
    }
    
    suspend fun getCachedArea(areaId: Int): Area? {
        return areaDao.getAreaById(areaId)?.toArea()
    }
    
    // Routes
    suspend fun cacheRoutes(routes: List<Route>) {
        val entities = routes.map { OfflineRouteEntity.fromRoute(it) }
        routeDao.insertRoutes(entities)
    }
    
    suspend fun getCachedRoutes(siteId: Int): List<Route> {
        return routeDao.getRoutesBySite(siteId).map { it.toRoute() }
    }
    
    suspend fun getCachedRoute(routeId: Int): Route? {
        return routeDao.getRouteById(routeId)?.toRoute()
    }
    
    // Contests
    suspend fun cacheContests(contests: List<Contest>) {
        val entities = contests.map { OfflineContestEntity.fromContest(it) }
        contestDao.insertContests(entities)
    }
    
    suspend fun getCachedContests(siteId: Int): List<Contest> {
        return contestDao.getContestsBySite(siteId).map { it.toContest() }
    }
    
    suspend fun getCachedContest(contestId: Int): Contest? {
        return contestDao.getContestById(contestId)?.toContest()
    }
    
    // Sync a complete site with all its data
    suspend fun syncSiteData(
        site: Site,
        areas: List<Area>,
        routes: List<Route>,
        contests: List<Contest>,
        backendId: String,
        backendName: String
    ) {
        cacheSite(site, backendId, backendName)
        cacheAreas(areas)
        cacheRoutes(routes)
        cacheContests(contests)
    }
    
    // Clear all offline data
    suspend fun clearAllOfflineData() {
        siteDao.deleteAllSites()
        areaDao.deleteAllAreas()
        routeDao.deleteAllRoutes()
        contestDao.deleteAllContests()
    }
}
