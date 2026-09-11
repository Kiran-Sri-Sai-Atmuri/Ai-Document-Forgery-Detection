package com.example.data.services

import com.example.model.DocumentSpecimen
import com.example.model.OfficerDecisionStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class PostgresDocumentRecord(
  val id: String,
  val docType: String,
  val holderName: String,
  val docNumber: String,
  val dob: String,
  val expiryDate: String,
  val country: String,
  val mrzCode: String,
  val ocrConfidence: Int,
  val tamperingScore: Int,
  val isModified: Boolean,
  val faceMatchScore: Int,
  val isFaceMatch: Boolean,
  val isBlocklisted: Boolean,
  val blocklistReason: String?,
  val overallScore: Int,
  val riskLevel: String,
  val officerDecision: String,
  val officerId: String,
  val createdAt: String
)

/**
 * Service 3: PostgreSQL Relational Database Service
 * Provides complete PostgreSQL DDL schema definition, relational mapping,
 * exportable SQL statements, and synchronizes with local Room persistence.
 */
object PostgresDatabaseService {

  val POSTGRES_TABLE_NAME = "kyc_verified_documents"

  val DDL_SCHEMA = """
    -- PostgreSQL Schema for KYC Document Verification Pipeline
    CREATE TABLE IF NOT EXISTS $POSTGRES_TABLE_NAME (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        doc_type VARCHAR(50) NOT NULL,
        holder_name VARCHAR(150) NOT NULL,
        doc_number VARCHAR(50) NOT NULL,
        dob VARCHAR(30),
        expiry_date VARCHAR(30),
        issuing_country VARCHAR(10),
        mrz_code TEXT,
        ocr_confidence INT CHECK (ocr_confidence BETWEEN 0 AND 100),
        tampering_score INT CHECK (tampering_score BETWEEN 0 AND 100),
        is_modified BOOLEAN DEFAULT FALSE,
        face_match_score INT CHECK (face_match_score BETWEEN 0 AND 100),
        is_face_match BOOLEAN DEFAULT FALSE,
        is_blocklisted BOOLEAN DEFAULT FALSE,
        blocklist_reason TEXT,
        overall_score INT CHECK (overall_score BETWEEN 0 AND 100),
        risk_level VARCHAR(20) NOT NULL,
        officer_decision VARCHAR(30) DEFAULT 'PENDING',
        officer_id VARCHAR(50),
        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
    );

    CREATE INDEX IF NOT EXISTS idx_kyc_holder_name ON $POSTGRES_TABLE_NAME (holder_name);
    CREATE INDEX IF NOT EXISTS idx_kyc_doc_number ON $POSTGRES_TABLE_NAME (doc_number);
    CREATE INDEX IF NOT EXISTS idx_kyc_decision ON $POSTGRES_TABLE_NAME (officer_decision);
  """.trimIndent()

  fun toPostgresRecord(specimen: DocumentSpecimen): PostgresDocumentRecord {
    val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(
      Date(specimen.officerTimestamp ?: System.currentTimeMillis())
    )

    return PostgresDocumentRecord(
      id = try {
        UUID.nameUUIDFromBytes(specimen.id.toByteArray()).toString()
      } catch (e: Exception) {
        UUID.randomUUID().toString()
      },
      docType = specimen.documentType.name,
      holderName = specimen.holderName,
      docNumber = specimen.documentNumber,
      dob = specimen.dateOfBirth,
      expiryDate = specimen.expiryDate,
      country = specimen.countryCode,
      mrzCode = specimen.ocrData?.mrzCode ?: "",
      ocrConfidence = specimen.ocrData?.ocrConfidence ?: 95,
      tamperingScore = specimen.tamperingResult?.tamperingScore ?: specimen.tamperResistance,
      isModified = specimen.tamperingResult?.isModified ?: (specimen.trustScore < 70),
      faceMatchScore = specimen.faceRecognitionResult?.faceMatchScore ?: specimen.biometricCoherence,
      isFaceMatch = specimen.faceRecognitionResult?.isMatch ?: (specimen.biometricCoherence >= 75),
      isBlocklisted = specimen.isBlocklisted,
      blocklistReason = specimen.blocklistReason,
      overallScore = specimen.trustScore,
      riskLevel = specimen.riskLevel.name,
      officerDecision = specimen.officerDecision.name,
      officerId = specimen.officerId,
      createdAt = dateStr
    )
  }

  fun generateInsertSql(specimen: DocumentSpecimen): String {
    val rec = toPostgresRecord(specimen)
    val escapedName = rec.holderName.replace("'", "''")
    val escapedReason = rec.blocklistReason?.replace("'", "''") ?: "NULL"
    val reasonSql = if (rec.blocklistReason != null) "'$escapedReason'" else "NULL"
    val escapedMrz = rec.mrzCode.replace("'", "''").replace("\n", "\\n")

    val baseSql = """
      -- 1. Insert/Update User Record in PostgreSQL
      INSERT INTO $POSTGRES_TABLE_NAME (
        id, doc_type, holder_name, doc_number, dob, expiry_date,
        issuing_country, mrz_code, ocr_confidence, tampering_score,
        is_modified, face_match_score, is_face_match, is_blocklisted,
        blocklist_reason, overall_score, risk_level, officer_decision,
        officer_id, created_at
      ) VALUES (
        '${rec.id}',
        '${rec.docType}',
        '$escapedName',
        '${rec.docNumber}',
        '${rec.dob}',
        '${rec.expiryDate}',
        '${rec.country}',
        '$escapedMrz',
        ${rec.ocrConfidence},
        ${rec.tamperingScore},
        ${rec.isModified},
        ${rec.faceMatchScore},
        ${rec.isFaceMatch},
        ${rec.isBlocklisted},
        $reasonSql,
        ${rec.overallScore},
        '${rec.riskLevel}',
        '${rec.officerDecision}',
        '${rec.officerId}',
        NOW()
      )
      ON CONFLICT (id) DO UPDATE SET
        officer_decision = EXCLUDED.officer_decision,
        is_blocklisted = EXCLUDED.is_blocklisted,
        blocklist_reason = EXCLUDED.blocklist_reason;
    """.trimIndent()

    if (rec.isBlocklisted || rec.officerDecision == "REJECTED") {
      val blReason = (rec.blocklistReason ?: "Officer Rejection").replace("'", "''")
      return baseSql + "\n\n" + """
        -- 2. User Flagged: Insert into PostgreSQL Enterprise Blocklist Table
        INSERT INTO blocklist_records (
          id, full_name, document_number, agency, reason, added_by_officer, created_at
        ) VALUES (
          'BLK-${rec.id.take(8)}',
          '$escapedName',
          '${rec.docNumber}',
          'Screening Officer Review',
          '$blReason',
          '${rec.officerId}',
          NOW()
        )
        ON CONFLICT (id) DO NOTHING;
      """.trimIndent()
    }

    return baseSql
  }
}
