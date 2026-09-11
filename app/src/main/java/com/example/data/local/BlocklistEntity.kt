package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocklist_records")
data class BlocklistEntity(
  @PrimaryKey val id: String,
  val fullName: String,
  val documentNumber: String?,
  val agency: String,
  val reason: String,
  val addedByOfficer: String = "OFFICER-4821",
  val timestamp: Long = System.currentTimeMillis()
)
