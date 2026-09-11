package com.example.data.repository

import com.example.data.local.BlocklistDao
import com.example.data.local.BlocklistEntity
import com.example.data.local.DocumentDao
import com.example.data.local.DocumentEntity
import com.example.data.postgres.PostgresDatabaseManager
import com.example.data.services.BlocklistService
import com.example.model.DocumentSpecimen
import com.example.model.OfficerDecisionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DocumentRepository(
  private val documentDao: DocumentDao,
  private val blocklistDao: BlocklistDao? = null
) {

  val allDocuments: Flow<List<DocumentSpecimen>> = documentDao.getAllDocuments().map { entities ->
    entities.map { it.toSpecimen() }
  }

  suspend fun insertDocument(specimen: DocumentSpecimen, imageUri: String? = specimen.imageUri) {
    documentDao.insertDocument(DocumentEntity.fromSpecimen(specimen, imageUri))
    // Automatically replicate to PostgreSQL
    PostgresDatabaseManager.storeVerifiedUser(specimen)
  }

  suspend fun getDocumentById(id: String): DocumentSpecimen? {
    return documentDao.getDocumentById(id)?.toSpecimen()
  }

  suspend fun deleteDocument(id: String) {
    documentDao.deleteDocumentById(id)
  }

  suspend fun getAllBlocklistEntries(): List<BlocklistEntity> {
    return blocklistDao?.getAllBlocklistList() ?: emptyList()
  }

  suspend fun updateOfficerDecision(
    id: String,
    decision: OfficerDecisionStatus,
    notes: String,
    officerId: String = "OFFICER-4821"
  ): DocumentSpecimen? {
    val existing = getDocumentById(id) ?: return null

    val isRejected = decision == OfficerDecisionStatus.REJECTED
    val updated = existing.copy(
      officerDecision = decision,
      officerNotes = notes,
      officerTimestamp = System.currentTimeMillis(),
      officerId = officerId,
      isBlocklisted = existing.isBlocklisted || isRejected,
      blocklistReason = if (isRejected) "Officer Rejection: ${notes.ifBlank { "Failed compliance and security check" }}" else existing.blocklistReason
    )

    insertDocument(updated)

    // If officer rejects, keep him under blocklist in db (both Room and PostgreSQL)
    if (isRejected) {
      val blocklistEntry = BlocklistEntity(
        id = "BLK-${System.currentTimeMillis() % 100000}",
        fullName = updated.holderName,
        documentNumber = updated.documentNumber,
        agency = "Law Enforcement Officer Review",
        reason = notes.ifBlank { "Rejected during forensic examination by $officerId" },
        addedByOfficer = officerId,
        timestamp = System.currentTimeMillis()
      )
      blocklistDao?.insertBlocklistEntry(blocklistEntry)
      PostgresDatabaseManager.addBlocklistRecord(blocklistEntry)
      BlocklistService.addToBlocklist(
        name = updated.holderName,
        docNumber = updated.documentNumber,
        reason = blocklistEntry.reason,
        agency = blocklistEntry.agency
      )
    }

    // Update PostgreSQL record
    PostgresDatabaseManager.updateOfficerDecision(id, decision.name, notes)

    return updated
  }

  /**
   * Cleans out any legacy specimen dummy data so only actual scanned/verified users are stored.
   */
  suspend fun removeDummyData() {
    val dummyIds = listOf("SPEC-US-DL-9821", "SPEC-EU-PASS-4410", "SPEC-UK-ID-2209")
    dummyIds.forEach { dummyId ->
      documentDao.deleteDocumentById(dummyId)
    }
  }
}

