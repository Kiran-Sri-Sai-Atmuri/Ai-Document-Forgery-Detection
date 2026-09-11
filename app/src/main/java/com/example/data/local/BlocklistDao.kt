package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BlocklistDao {
  @Query("SELECT * FROM blocklist_records ORDER BY timestamp DESC")
  fun getAllBlocklistEntries(): Flow<List<BlocklistEntity>>

  @Query("SELECT * FROM blocklist_records")
  suspend fun getAllBlocklistList(): List<BlocklistEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBlocklistEntry(entry: BlocklistEntity)

  @Query("SELECT * FROM blocklist_records WHERE UPPER(fullName) = UPPER(:name) OR (documentNumber IS NOT NULL AND documentNumber != '' AND UPPER(documentNumber) = UPPER(:docNum)) LIMIT 1")
  suspend fun findMatch(name: String, docNum: String): BlocklistEntity?

  @Query("DELETE FROM blocklist_records WHERE id = :id")
  suspend fun deleteEntry(id: String)

  @Query("SELECT COUNT(*) FROM blocklist_records")
  suspend fun getCount(): Int

  @Query("DELETE FROM blocklist_records")
  suspend fun clearAll()
}
