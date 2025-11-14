package com.example.topoclimb.cache

import android.content.Context
import com.example.topoclimb.cache.database.AppDatabase
import com.example.topoclimb.cache.entity.*
import com.example.topoclimb.data.*

/**
 * Manages cache operations and expiration policies
 * 
 * Expiration policies:
 * - Sites: 1 week (604800000 ms)
 * - Site details (areas, contests): 1 week (604800000 ms)
 * - Areas (sectors, lines, schemas): 3 days (259200000 ms)
 * - Routes: 3 days for list (259200000 ms), 1 week for cached route data (604800000 ms)
 */
class CacheManager(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    
    companion object {
        private const val ONE_WEEK_MS = 7 * 24 * 60 * 60 * 1000L // 604800000 ms
        private const val THREE_DAYS_MS = 3 * 24 * 60 * 60 * 1000L // 259200000 ms
    }

    // Check if cached data is still valid
    private fun isValid(cachedAt: Long, expirationMs: Long): Boolean {
        return (System.currentTimeMillis() - cachedAt) < expirationMs
    }

    // Sites
    suspend fun getCachedSites(backendId: String): List<Site>? {
        val sites = db.siteDao().getAllSites(backendId)
        if (sites.isEmpty()) return null
        
        // Check if any site has expired
        val allValid = sites.all { isValid(it.cachedAt, ONE_WEEK_MS) }
        return if (allValid) sites.map { it.toSite() } else null
    }
    
    suspend fun getCachedSitesIgnoreExpiration(backendId: String): List<Site>? {
        val sites = db.siteDao().getAllSites(backendId)
        if (sites.isEmpty()) return null
        return sites.map { it.toSite() }
    }

    suspend fun cacheSites(sites: List<Site>, backendId: String) {
        val entities = sites.map { SiteEntity.fromSite(it, backendId) }
        db.siteDao().insertSites(entities)
    }

    suspend fun getCachedSite(siteId: Int, backendId: String): Site? {
        val site = db.siteDao().getSiteById(siteId, backendId) ?: return null
        return if (isValid(site.cachedAt, ONE_WEEK_MS)) site.toSite() else null
    }
    
    suspend fun getCachedSiteIgnoreExpiration(siteId: Int, backendId: String): Site? {
        val site = db.siteDao().getSiteById(siteId, backendId) ?: return null
        return site.toSite()
    }

    suspend fun cacheSite(site: Site, backendId: String) {
        db.siteDao().insertSite(SiteEntity.fromSite(site, backendId))
    }

    // Areas
    suspend fun getCachedAreas(backendId: String): List<Area>? {
        val areas = db.areaDao().getAllAreas(backendId)
        if (areas.isEmpty()) return null
        
        val allValid = areas.all { isValid(it.cachedAt, THREE_DAYS_MS) }
        return if (allValid) areas.map { it.toArea() } else null
    }

    suspend fun getCachedAreasBySite(siteId: Int, backendId: String): List<Area>? {
        val areas = db.areaDao().getAreasBySite(siteId, backendId)
        if (areas.isEmpty()) return null
        
        val allValid = areas.all { isValid(it.cachedAt, ONE_WEEK_MS) }
        return if (allValid) areas.map { it.toArea() } else null
    }
    
    suspend fun getCachedAreasBySiteIgnoreExpiration(siteId: Int, backendId: String): List<Area>? {
        val areas = db.areaDao().getAreasBySite(siteId, backendId)
        if (areas.isEmpty()) return null
        return areas.map { it.toArea() }
    }

    suspend fun cacheAreas(areas: List<Area>, backendId: String) {
        val entities = areas.map { AreaEntity.fromArea(it, backendId) }
        db.areaDao().insertAreas(entities)
    }

    suspend fun getCachedArea(areaId: Int, backendId: String): Area? {
        val area = db.areaDao().getAreaById(areaId, backendId) ?: return null
        return if (isValid(area.cachedAt, THREE_DAYS_MS)) area.toArea() else null
    }

    suspend fun cacheArea(area: Area, backendId: String) {
        db.areaDao().insertArea(AreaEntity.fromArea(area, backendId))
    }

    // Routes
    suspend fun getCachedRoute(routeId: Int, backendId: String): Route? {
        val route = db.routeDao().getRouteById(routeId, backendId) ?: return null
        // Individual route data: 1 week expiration
        return if (isValid(route.cachedAt, ONE_WEEK_MS)) route.toRoute() else null
    }

    suspend fun getCachedRoutesBySite(siteId: Int, backendId: String): List<Route>? {
        val routes = db.routeDao().getRoutesBySite(siteId, backendId)
        if (routes.isEmpty()) return null
        
        // Route list: 3 days expiration
        val allValid = routes.all { isValid(it.cachedAt, THREE_DAYS_MS) }
        return if (allValid) routes.map { it.toRoute() } else null
    }

    suspend fun cacheRoute(route: Route, backendId: String) {
        db.routeDao().insertRoute(RouteEntity.fromRoute(route, backendId))
    }

    suspend fun cacheRoutes(routes: List<Route>, backendId: String) {
        val entities = routes.map { RouteEntity.fromRoute(it, backendId) }
        db.routeDao().insertRoutes(entities)
    }

    // Sectors
    suspend fun getCachedSectorsByArea(areaId: Int, backendId: String): List<Sector>? {
        val sectors = db.sectorDao().getSectorsByArea(areaId, backendId)
        if (sectors.isEmpty()) return null
        
        val allValid = sectors.all { isValid(it.cachedAt, THREE_DAYS_MS) }
        return if (allValid) sectors.map { it.toSector() } else null
    }

    suspend fun cacheSectors(sectors: List<Sector>, backendId: String) {
        val entities = sectors.map { SectorEntity.fromSector(it, backendId) }
        db.sectorDao().insertSectors(entities)
    }

    // Lines
    suspend fun getCachedLinesBySector(sectorId: Int, backendId: String): List<Line>? {
        val lines = db.lineDao().getLinesBySector(sectorId, backendId)
        if (lines.isEmpty()) return null
        
        val allValid = lines.all { isValid(it.cachedAt, THREE_DAYS_MS) }
        return if (allValid) lines.map { it.toLine() } else null
    }

    suspend fun cacheLines(lines: List<Line>, backendId: String) {
        val entities = lines.map { LineEntity.fromLine(it, backendId) }
        db.lineDao().insertLines(entities)
    }

    // Sector Schemas
    suspend fun getCachedSchemasByArea(areaId: Int, backendId: String): List<SectorSchema>? {
        val schemas = db.sectorSchemaDao().getSchemasByArea(areaId, backendId)
        if (schemas.isEmpty()) return null
        
        val allValid = schemas.all { isValid(it.cachedAt, THREE_DAYS_MS) }
        return if (allValid) schemas.map { it.toSectorSchema() } else null
    }

    suspend fun cacheSchemas(schemas: List<SectorSchema>, areaId: Int, backendId: String) {
        val entities = schemas.map { SectorSchemaEntity.fromSectorSchema(it, backendId, areaId) }
        db.sectorSchemaDao().insertSchemas(entities)
    }

    // Contests
    suspend fun getCachedContestsBySite(siteId: Int, backendId: String): List<Contest>? {
        val contests = db.contestDao().getContestsBySite(siteId, backendId)
        if (contests.isEmpty()) return null
        
        val allValid = contests.all { isValid(it.cachedAt, ONE_WEEK_MS) }
        return if (allValid) contests.map { it.toContest() } else null
    }
    
    suspend fun getCachedContestsBySiteIgnoreExpiration(siteId: Int, backendId: String): List<Contest>? {
        val contests = db.contestDao().getContestsBySite(siteId, backendId)
        if (contests.isEmpty()) return null
        return contests.map { it.toContest() }
    }

    suspend fun cacheContests(contests: List<Contest>, backendId: String) {
        val entities = contests.map { ContestEntity.fromContest(it, backendId) }
        db.contestDao().insertContests(entities)
    }

    // Clear all cache
    suspend fun clearAllCache() {
        db.siteDao().deleteAll()
        db.areaDao().deleteAll()
        db.routeDao().deleteAll()
        db.sectorDao().deleteAll()
        db.lineDao().deleteAll()
        db.sectorSchemaDao().deleteAll()
        db.contestDao().deleteAll()
    }

    // Clear cache for a specific backend
    suspend fun clearBackendCache(backendId: String) {
        db.siteDao().deleteAllSites(backendId)
        db.areaDao().deleteAllAreas(backendId)
        db.routeDao().deleteAllRoutes(backendId)
        db.sectorDao().deleteAllSectors(backendId)
        db.lineDao().deleteAllLines(backendId)
        db.sectorSchemaDao().deleteAllSchemas(backendId)
        db.contestDao().deleteAllContests(backendId)
    }
}
