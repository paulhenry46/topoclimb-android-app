package com.example.topoclimb.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.topoclimb.database.entities.LineFetchMetadataEntity

@Dao
interface LineFetchMetadataDao {
    @Query("SELECT * FROM line_fetch_metadata WHERE lineId = :lineId AND backendId = :backendId")
    suspend fun getLineFetchMetadata(lineId: Int, backendId: String): LineFetchMetadataEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLineFetchMetadata(metadata: LineFetchMetadataEntity)
    
    @Query("DELETE FROM line_fetch_metadata WHERE lineId = :lineId AND backendId = :backendId")
    suspend fun deleteLineFetchMetadata(lineId: Int, backendId: String)
    
    @Query("DELETE FROM line_fetch_metadata WHERE backendId = :backendId")
    suspend fun deleteAllLineFetchMetadata(backendId: String)
}
