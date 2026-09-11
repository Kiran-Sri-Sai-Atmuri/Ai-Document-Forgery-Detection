package com.example.data.services

import com.example.model.BlocklistCheckResult
import java.util.concurrent.CopyOnWriteArrayList

data class BlocklistEntry(
  val id: String,
  val fullName: String,
  val documentNumber: String? = null,
  val reason: String,
  val agency: String,
  val riskLevel: String = "CRITICAL"
)

/**
 * Service 1: Blocklist Screening Service
 * Evaluates whether the identity in the document matches international sanctions,
 * Interpol Red Notices, or AML/KYC blocklists at the very first step.
 */
object BlocklistService {

  private val entries = CopyOnWriteArrayList<BlocklistEntry>().apply {
    addAll(
      listOf(
        BlocklistEntry(
          id = "BLK-1001",
          fullName = "MAXIMILIAN VANCE",
          documentNumber = "C01X9842M",
          reason = "FATF Global Sanctions: Multi-jurisdictional financial fraud and synthetic identity laundering.",
          agency = "Interpol Red Notice #A-4820/6-2023"
        ),
        BlocklistEntry(
          id = "BLK-1002",
          fullName = "VIKTOR PETROV",
          documentNumber = "P89201948",
          reason = "OFAC Sanctioned List: Wire fraud syndicate and forged travel document distribution.",
          agency = "OFAC Specially Designated Nationals (SDN)"
        ),
        BlocklistEntry(
          id = "BLK-1003",
          fullName = "ALEXEI VOLKOV",
          documentNumber = "DL-904182",
          reason = "Border Security Watchlist: Identity cloning and altered biometrics record.",
          agency = "EU Border Security Taskforce"
        ),
        BlocklistEntry(
          id = "BLK-1004",
          fullName = "CARLOS MENDEZ",
          documentNumber = "PAS-552194",
          reason = "Interpol Watchlist Tier-1: Impersonation and stolen passport ring.",
          agency = "INTERPOL"
        )
      )
    )
  }

  fun getBlocklist(): List<BlocklistEntry> = entries.toList()

  fun addToBlocklist(name: String, docNumber: String?, reason: String, agency: String) {
    entries.add(
      BlocklistEntry(
        id = "BLK-${System.currentTimeMillis() % 10000}",
        fullName = name.trim().uppercase(),
        documentNumber = docNumber?.trim()?.uppercase(),
        reason = reason,
        agency = agency
      )
    )
  }

  fun removeFromBlocklist(id: String) {
    entries.removeAll { it.id == id }
  }

  /**
   * First step: Check if user in document is in blocklist.
   * Uses strict sanctions screening logic to prevent false positives:
   * Requires non-empty exact name match, exact multi-token match, or exact document number match.
   * Checks both international watchlists and officer-rejected database blocklist entries.
   */
  fun checkBlocklist(
    holderName: String,
    documentNumber: String,
    extraDbEntries: List<com.example.data.local.BlocklistEntity> = emptyList()
  ): BlocklistCheckResult {
    val normName = holderName.trim().uppercase()
    val normDoc = documentNumber.trim().uppercase()

    // If no name and no document number provided, treat as unlisted clean record
    if (normName.isBlank() && normDoc.isBlank()) {
      return BlocklistCheckResult(
        isBlocked = false,
        reason = "Clean record. Identity not listed on Interpol, OFAC, or global watchlists."
      )
    }

    // Combine static entries with database blocklist entries
    val combinedEntries = entries.toList() + extraDbEntries.map { db ->
      BlocklistEntry(
        id = db.id,
        fullName = db.fullName,
        documentNumber = db.documentNumber,
        reason = db.reason,
        agency = db.agency
      )
    }

    val match = combinedEntries.firstOrNull { entry ->
      val entryName = entry.fullName.trim().uppercase()
      val entryDoc = entry.documentNumber?.trim()?.uppercase()

      // Exact document number match (must be at least 4 characters to avoid false matches)
      val docMatch = !entryDoc.isNullOrEmpty() && normDoc.length >= 4 && normDoc == entryDoc

      // Exact full name match
      val exactNameMatch = normName.isNotBlank() && normName == entryName

      // Multi-word name token match (e.g. "MAXIMILIAN VANCE")
      // Only matches if all non-trivial tokens of the blocklisted entry are contained in normName
      val entryTokens = entryName.split("\\s+".toRegex()).filter { it.length >= 3 }
      val tokenMatch = normName.isNotBlank() && entryTokens.size >= 2 && entryTokens.all { token ->
        normName.contains(token)
      }

      docMatch || exactNameMatch || tokenMatch
    }

    return if (match != null) {
      BlocklistCheckResult(
        isBlocked = true,
        matchedName = match.fullName,
        matchedDocNumber = match.documentNumber,
        reason = "The user is in blocklist: ${match.reason} [Authority: ${match.agency}]",
        watchlistCategory = match.agency
      )
    } else {
      BlocklistCheckResult(
        isBlocked = false,
        reason = "Clean record. Identity not listed on Interpol, OFAC, or global watchlists."
      )
    }
  }
}
