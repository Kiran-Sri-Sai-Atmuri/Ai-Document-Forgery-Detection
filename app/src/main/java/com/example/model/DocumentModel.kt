package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.RiskHigh
import com.example.ui.theme.RiskHighBg
import com.example.ui.theme.RiskHighBorder
import com.example.ui.theme.RiskLow
import com.example.ui.theme.RiskLowBg
import com.example.ui.theme.RiskLowBorder
import com.example.ui.theme.RiskMedium
import com.example.ui.theme.RiskMediumBg
import com.example.ui.theme.RiskMediumBorder

enum class RiskLevel(
  val label: String,
  val badgeText: String,
  val textColor: Color,
  val backgroundColor: Color,
  val borderColor: Color,
  val headline: String
) {
  LOW(
    label = "Low Risk",
    badgeText = "LOW RISK",
    textColor = RiskLow,
    backgroundColor = RiskLowBg,
    borderColor = RiskLowBorder,
    headline = "Verified Authentic"
  ),
  MEDIUM(
    label = "Medium Risk",
    badgeText = "MEDIUM RISK",
    textColor = RiskMedium,
    backgroundColor = RiskMediumBg,
    borderColor = RiskMediumBorder,
    headline = "Suspicious Anomalies Detected"
  ),
  HIGH(
    label = "High Risk",
    badgeText = "HIGH RISK",
    textColor = RiskHigh,
    backgroundColor = RiskHighBg,
    borderColor = RiskHighBorder,
    headline = "Critical Forgery Flags Detected"
  )
}

enum class VerificationStage(
  val stepIndex: Int,
  val title: String,
  val operation: String,
  val telemetry: String
) {
  DETECT(
    stepIndex = 1,
    title = "Blocklist Check",
    operation = "Screening identity across Interpol, OFAC, and AML blocklists at first step",
    telemetry = "128 ms • Watchlist Database"
  ),
  READ(
    stepIndex = 2,
    title = "OCR & Postgres DB",
    operation = "Detecting document type and extracting OCR fields for PostgreSQL relational DB",
    telemetry = "240 ms • ICAO 9303 & ISO 7810"
  ),
  ANALYZE(
    stepIndex = 3,
    title = "Tampering Service",
    operation = "Inspecting text format, document security rules, and text/image modifications",
    telemetry = "380 ms • Micro-print & Font Kerning"
  ),
  VERIFY(
    stepIndex = 4,
    title = "Face Recognition",
    operation = "Comparing photo in document with live photo taken from camera",
    telemetry = "490 ms • 68-Point Landmark Mesh"
  ),
  TRUST(
    stepIndex = 5,
    title = "Overall Score",
    operation = "Synthesizing combined score and preparing officer accept/reject console",
    telemetry = "610 ms • Officer Action Ready"
  )
}

enum class DocumentType(val displayName: String, val code: String) {
  PASSPORT("Passport", "TD3"),
  DRIVERS_LICENSE("Driver's License", "DL-A2"),
  NATIONAL_ID("National Identity Card", "TD1")
}

data class DocumentDna(
  val dnaHash: String,
  val seedValue: Long,
  val arcFrequencies: List<Float>,
  val nodeCount: Int,
  val entropyMetric: Float,
  val structuralParity: String
)

data class ForensicRegion(
  val xNorm: Float,
  val yNorm: Float,
  val widthNorm: Float,
  val heightNorm: Float,
  val tagLabel: String
)

enum class FlagSeverity {
  CRITICAL,
  WARNING,
  PASS
}

data class ForensicFlag(
  val id: String,
  val category: String,
  val severity: FlagSeverity,
  val title: String,
  val reason: String,
  val technicalEvidence: String,
  val region: ForensicRegion,
  val confidenceScore: Float
)

enum class OfficerDecisionStatus(val label: String, val badgeColorName: String) {
  PENDING("Pending Officer Review", "Navy"),
  ACCEPTED("Accepted by Officer", "Green"),
  REJECTED("Rejected by Officer", "Red")
}

data class BlocklistCheckResult(
  val isBlocked: Boolean,
  val matchedName: String? = null,
  val matchedDocNumber: String? = null,
  val reason: String? = null,
  val watchlistCategory: String? = null,
  val timestamp: Long = System.currentTimeMillis()
)

data class OcrExtractionData(
  val detectedType: DocumentType,
  val holderName: String,
  val documentNumber: String,
  val dateOfBirth: String,
  val expiryDate: String,
  val issueDate: String,
  val issuingCountry: String,
  val mrzCode: String,
  val mrzValid: Boolean,
  val ocrConfidence: Int,
  val extractedFields: Map<String, String>,
  val rulesChecked: List<String>
)

data class TamperingCheckResult(
  val tamperingScore: Int,
  val isModified: Boolean,
  val isTextModified: Boolean,
  val isImageModified: Boolean,
  val formatRuleStatus: String,
  val messages: List<String>
)

data class FaceRecognitionCheckResult(
  val faceMatchScore: Int,
  val isMatch: Boolean,
  val confidence: Float,
  val landmarkCorrelation: Float,
  val livenessVerified: Boolean,
  val messages: List<String>,
  val documentFaceCrop: String? = null,
  val cameraSelfieCrop: String? = null
)

data class DocumentSpecimen(
  val id: String,
  val title: String,
  val subtitle: String,
  val documentType: DocumentType,
  val holderName: String,
  val documentNumber: String,
  val dateOfBirth: String,
  val expiryDate: String,
  val countryCode: String,
  val trustScore: Int,
  val riskLevel: RiskLevel,
  val dna: DocumentDna,
  val flags: List<ForensicFlag>,
  val authenticityIndex: Int,
  val biometricCoherence: Int,
  val tamperResistance: Int,
  val dataConsistency: Int,
  val auditDigest: String,
  val investigationSummary: String,
  val imageUri: String? = null,
  // New pipeline fields
  val isBlocklisted: Boolean = false,
  val blocklistReason: String? = null,
  val blocklistResult: BlocklistCheckResult? = null,
  val ocrData: OcrExtractionData? = null,
  val tamperingResult: TamperingCheckResult? = null,
  val faceRecognitionResult: FaceRecognitionCheckResult? = null,
  val officerDecision: OfficerDecisionStatus = OfficerDecisionStatus.PENDING,
  val officerNotes: String = "",
  val officerTimestamp: Long? = null,
  val officerId: String = "OFFICER-4821"
) {
  val isTextModified: Boolean
    get() = tamperingResult?.isTextModified ?: (tamperResistance < 70)

  val isImageModified: Boolean
    get() = tamperingResult?.isImageModified ?: (biometricCoherence < 70)

  val tamperScore: Int
    get() = tamperingResult?.tamperingScore ?: tamperResistance

  val tamperingMessages: List<String>
    get() = tamperingResult?.messages ?: listOf(
      "Document substrate micro-pattern verification complete",
      if (tamperResistance >= 70) "Font kerning and text baseline within standard tolerances" else "Detected font baseline anomalies and ink bleed variance"
    )

  val faceMatchScore: Int
    get() = faceRecognitionResult?.faceMatchScore ?: biometricCoherence

  val isFaceMatched: Boolean
    get() = faceRecognitionResult?.isMatch ?: (biometricCoherence >= 70)

  val faceMatchMessages: List<String>
    get() = faceRecognitionResult?.messages ?: listOf(
      "68-point facial landmark geometry correlated",
      if (biometricCoherence >= 70) "Biometric facial mesh matches document portrait" else "Biometric mismatch detected between document portrait and sensor capture"
    )
}
