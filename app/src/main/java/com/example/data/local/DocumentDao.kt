package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
  @Query("SELECT * FROM verified_documents ORDER BY timestamp DESC")
  fun getAllDocuments(): Flow<List<DocumentEntity>>

  @Query("SELECT * FROM verified_documents WHERE id = :id LIMIT 1")
  suspend fun getDocumentById(id: String): DocumentEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDocument(document: DocumentEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(documents: List<DocumentEntity>)

  @Query("DELETE FROM verified_documents WHERE id = :id")
  suspend fun deleteDocumentById(id: String)

  @Query("DELETE FROM verified_documents")
  suspend fun clearAll()

  @Query("SELECT COUNT(*) FROM verified_documents")
  suspend fun getCount(): Int
}
