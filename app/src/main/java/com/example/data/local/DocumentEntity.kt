package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.DocumentDna
import com.example.model.DocumentSpecimen
import com.example.model.DocumentType
import com.example.model.FlagSeverity
import com.example.model.ForensicFlag
import com.example.model.ForensicRegion
import com.example.model.RiskLevel
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "verified_documents")
data class DocumentEntity(
  @PrimaryKey val id: String,
  val title: String,
  val subtitle: String,
  val documentType: String,
  val holderName: String,
  val documentNumber: String,
  val dateOfBirth: String,
  val expiryDate: String,
  val countryCode: String,
  val trustScore: Int,
  val riskLevel: String,
  val authenticityIndex: Int,
  val biometricCoherence: Int,
  val tamperResistance: Int,
  val dataConsistency: Int,
  val auditDigest: String,
  val investigationSummary: String,
  val flagsJson: String,
  val dnaHash: String,
  val dnaSeed: Long,
  val dnaArcFreqs: String,
  val dnaNodeCount: Int,
  val dnaEntropy: Float,
  val dnaParity: String,
  val imageUri: String? = null,
  val isBlocklisted: Boolean = false,
  val blocklistReason: String? = null,
  val tamperingScore: Int = 90,
  val isModified: Boolean = false,
  val faceMatchScore: Int = 90,
  val isFaceMatch: Boolean = true,
  val officerDecision: String = "PENDING",
  val officerNotes: String = "",
  val officerId: String = "OFFICER-4821",
  val timestamp: Long = System.currentTimeMillis()
) {
  fun toSpecimen(): DocumentSpecimen {
    val type = try {
      DocumentType.valueOf(documentType)
    } catch (e: Exception) {
      DocumentType.PASSPORT
    }

    val risk = try {
      RiskLevel.valueOf(riskLevel)
    } catch (e: Exception) {
      RiskLevel.LOW
    }

    val decision = try {
      com.example.model.OfficerDecisionStatus.valueOf(officerDecision)
    } catch (e: Exception) {
      com.example.model.OfficerDecisionStatus.PENDING
    }

    val arcList = dnaArcFreqs.split(",").mapNotNull { it.trim().toFloatOrNull() }
      .ifEmpty { listOf(0.9f, 0.85f, 0.95f, 0.88f) }

    val dna = DocumentDna(
      dnaHash = dnaHash,
      seedValue = dnaSeed,
      arcFrequencies = arcList,
      nodeCount = dnaNodeCount,
      entropyMetric = dnaEntropy,
      structuralParity = dnaParity
    )

    val flags = parseFlags(flagsJson)

    // Build synthesized check results
    val tamperingRes = com.example.model.TamperingCheckResult(
      tamperingScore = tamperingScore,
      isModified = isModified,
      isTextModified = isModified && (trustScore < 50),
      isImageModified = isModified,
      formatRuleStatus = if (!isModified) "AUTHENTIC & COMPLIANT" else "SUSPICIOUS / TAMPERED",
      messages = listOf(
        if (isModified) "❌ [MODIFICATION DETECTED] Text kerning or photo boundary failed inspection."
        else "✅ [AUTHENTIC] Document format rules and typography verified compliant."
      )
    )

    val faceRes = com.example.model.FaceRecognitionCheckResult(
      faceMatchScore = faceMatchScore,
      isMatch = isFaceMatch,
      confidence = 0.95f,
      landmarkCorrelation = faceMatchScore / 100f,
      livenessVerified = isFaceMatch,
      messages = listOf(
        if (isFaceMatch) "✅ [BIOMETRIC MATCH] Face in document and camera selfie match (${faceMatchScore}% similarity)."
        else "❌ [BIOMETRIC MISMATCH] Photo in document does not match live camera selfie (${faceMatchScore}% similarity)."
      )
    )

    val ocr = com.example.data.services.OcrExtractionService.extractFromSpecimenOrImage(
      specimen = DocumentSpecimen(
        id = id,
        title = title,
        subtitle = subtitle,
        documentType = type,
        holderName = holderName,
        documentNumber = documentNumber,
        dateOfBirth = dateOfBirth,
        expiryDate = expiryDate,
        countryCode = countryCode,
        trustScore = trustScore,
        riskLevel = risk,
        dna = dna,
        flags = flags,
        authenticityIndex = authenticityIndex,
        biometricCoherence = biometricCoherence,
        tamperResistance = tamperResistance,
        dataConsistency = dataConsistency,
        auditDigest = auditDigest,
        investigationSummary = investigationSummary
      ),
      bitmap = null
    )

    return DocumentSpecimen(
      id = id,
      title = title,
      subtitle = subtitle,
      documentType = type,
      holderName = holderName,
      documentNumber = documentNumber,
      dateOfBirth = dateOfBirth,
      expiryDate = expiryDate,
      countryCode = countryCode,
      trustScore = trustScore,
      riskLevel = risk,
      dna = dna,
      flags = flags,
      authenticityIndex = authenticityIndex,
      biometricCoherence = biometricCoherence,
      tamperResistance = tamperResistance,
      dataConsistency = dataConsistency,
      auditDigest = auditDigest,
      investigationSummary = investigationSummary,
      imageUri = imageUri,
      isBlocklisted = isBlocklisted,
      blocklistReason = blocklistReason,
      ocrData = ocr,
      tamperingResult = tamperingRes,
      faceRecognitionResult = faceRes,
      officerDecision = decision,
      officerNotes = officerNotes,
      officerId = officerId,
      officerTimestamp = timestamp
    )
  }

  companion object {
    fun fromSpecimen(specimen: DocumentSpecimen, imageUri: String? = specimen.imageUri): DocumentEntity {
      val flagsArray = JSONArray()
      specimen.flags.forEach { flag ->
        val obj = JSONObject().apply {
          put("id", flag.id)
          put("category", flag.category)
          put("severity", flag.severity.name)
          put("title", flag.title)
          put("reason", flag.reason)
          put("technicalEvidence", flag.technicalEvidence)
          put("confidenceScore", flag.confidenceScore.toDouble())
          val reg = JSONObject().apply {
            put("x", flag.region.xNorm.toDouble())
            put("y", flag.region.yNorm.toDouble())
            put("w", flag.region.widthNorm.toDouble())
            put("h", flag.region.heightNorm.toDouble())
            put("tag", flag.region.tagLabel)
          }
          put("region", reg)
        }
        flagsArray.put(obj)
      }

      val tamperScore = specimen.tamperingResult?.tamperingScore ?: specimen.tamperResistance
      val isMod = specimen.tamperingResult?.isModified ?: (specimen.trustScore < 70)
      val faceScore = specimen.faceRecognitionResult?.faceMatchScore ?: specimen.biometricCoherence
      val isFace = specimen.faceRecognitionResult?.isMatch ?: (faceScore >= 75)

      return DocumentEntity(
        id = specimen.id,
        title = specimen.title,
        subtitle = specimen.subtitle,
        documentType = specimen.documentType.name,
        holderName = specimen.holderName,
        documentNumber = specimen.documentNumber,
        dateOfBirth = specimen.dateOfBirth,
        expiryDate = specimen.expiryDate,
        countryCode = specimen.countryCode,
        trustScore = specimen.trustScore,
        riskLevel = specimen.riskLevel.name,
        authenticityIndex = specimen.authenticityIndex,
        biometricCoherence = specimen.biometricCoherence,
        tamperResistance = specimen.tamperResistance,
        dataConsistency = specimen.dataConsistency,
        auditDigest = specimen.auditDigest,
        investigationSummary = specimen.investigationSummary,
        flagsJson = flagsArray.toString(),
        dnaHash = specimen.dna.dnaHash,
        dnaSeed = specimen.dna.seedValue,
        dnaArcFreqs = specimen.dna.arcFrequencies.joinToString(","),
        dnaNodeCount = specimen.dna.nodeCount,
        dnaEntropy = specimen.dna.entropyMetric,
        dnaParity = specimen.dna.structuralParity,
        imageUri = imageUri,
        isBlocklisted = specimen.isBlocklisted,
        blocklistReason = specimen.blocklistReason,
        tamperingScore = tamperScore,
        isModified = isMod,
        faceMatchScore = faceScore,
        isFaceMatch = isFace,
        officerDecision = specimen.officerDecision.name,
        officerNotes = specimen.officerNotes,
        officerId = specimen.officerId,
        timestamp = specimen.officerTimestamp ?: System.currentTimeMillis()
      )
    }

    private fun parseFlags(jsonStr: String): List<ForensicFlag> {
      val list = mutableListOf<ForensicFlag>()
      try {
        val array = JSONArray(jsonStr)
        for (i in 0 until array.length()) {
          val obj = array.getJSONObject(i)
          val id = obj.optString("id", "F-$i")
          val category = obj.optString("category", "General")
          val severityStr = obj.optString("severity", "PASS")
          val severity = try {
            FlagSeverity.valueOf(severityStr)
          } catch (e: Exception) {
            FlagSeverity.PASS
          }
          val title = obj.optString("title", "Check")
          val reason = obj.optString("reason", "Verified.")
          val technicalEvidence = obj.optString("technicalEvidence", "No anomaly detected.")
          val conf = obj.optDouble("confidenceScore", 0.95).toFloat()

          val regObj = obj.optJSONObject("region")
          val region = if (regObj != null) {
            ForensicRegion(
              xNorm = regObj.optDouble("x", 0.1).toFloat(),
              yNorm = regObj.optDouble("y", 0.1).toFloat(),
              widthNorm = regObj.optDouble("w", 0.3).toFloat(),
              heightNorm = regObj.optDouble("h", 0.3).toFloat(),
              tagLabel = regObj.optString("tag", "Zone")
            )
          } else {
            ForensicRegion(0.1f, 0.1f, 0.4f, 0.3f, "Document Area")
          }

          list.add(
            ForensicFlag(
              id = id,
              category = category,
              severity = severity,
              title = title,
              reason = reason,
              technicalEvidence = technicalEvidence,
              region = region,
              confidenceScore = conf
            )
          )
        }
      } catch (e: Exception) {
        // Fallback flag
      }
      return list
    }
  }
}
