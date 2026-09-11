package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.FindInPage
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.services.PostgresDatabaseService
import com.example.model.DocumentSpecimen
import com.example.model.OfficerDecisionStatus
import com.example.model.RiskLevel
import com.example.ui.components.DocumentDnaView
import com.example.ui.theme.RiskHigh
import com.example.ui.theme.RiskLow
import com.example.ui.theme.RiskMedium
import com.example.ui.theme.VeriAccent
import com.example.ui.theme.VeriBackground
import com.example.ui.theme.VeriBorder
import com.example.ui.theme.VeriBorderSubtle
import com.example.ui.theme.VeriNavy400
import com.example.ui.theme.VeriNavy600
import com.example.ui.theme.VeriNavy800
import com.example.ui.theme.VeriNavy950
import com.example.ui.theme.VeriWhite

/**
 * Screen 4: Result & Officer Decision Console
 * Integrates:
 * 1. Step 1: Blocklist Screening Alert (Sanctions & Interpol check)
 * 2. Overall Combined Trust Score & Risk Level
 * 3. Officer Decision Station: Accept or Reject based on score
 * 4. Step 2: Document Type Detection, OCR Details & PostgreSQL DB persistence
 * 5. Step 3: Tampering Inspection Service (real vs fake, text/image modifications, rules & score)
 * 6. Step 4: Face Recognition Service (document photo vs live camera photo comparison & score)
 */
@Composable
fun ResultScreen(
  specimen: DocumentSpecimen,
  onExplainReasons: () -> Unit,
  onVerifyAnother: () -> Unit,
  onRecordOfficerDecision: (OfficerDecisionStatus, String) -> Unit = { _, _ -> },
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current
  var currentDecision by remember { mutableStateOf(specimen.officerDecision) }
  var showDecisionDialog by remember { mutableStateOf<OfficerDecisionStatus?>(null) }
  var officerNotesInput by remember { mutableStateOf("") }
  var showPostgresSchemaModal by remember { mutableStateOf(false) }

  val isBlocklisted = specimen.isBlocklisted

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(VeriBackground)
      .padding(horizontal = 24.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp)
  ) {
    // 1. Navigation & Specimen Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onVerifyAnother,
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(VeriWhite)
            .border(1.dp, VeriBorder, CircleShape)
            .testTag("result_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
            contentDescription = "Back",
            tint = VeriNavy950,
            modifier = Modifier.size(18.dp)
          )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "VERIFICATION DOSSIER",
            style = MaterialTheme.typography.labelSmall.copy(
              fontFamily = FontFamily.Monospace,
              letterSpacing = 1.2.sp,
              fontWeight = FontWeight.Bold,
              color = VeriNavy400
            )
          )
          Text(
            text = specimen.documentNumber,
            style = MaterialTheme.typography.bodySmall.copy(
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.SemiBold,
              color = VeriNavy950
            )
          )
        }

        IconButton(
          onClick = onVerifyAnother,
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(VeriWhite)
            .border(1.dp, VeriBorder, CircleShape)
            .testTag("new_scan_icon_button")
        ) {
          Icon(
            imageVector = Icons.Outlined.Refresh,
            contentDescription = "New Scan",
            tint = VeriNavy950,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }

    // 2. STEP 1: BLOCKLIST SCREENING STATUS BANNER
    if (isBlocklisted) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFEF2F2))
            .border(1.5.dp, RiskHigh, RoundedCornerShape(16.dp))
            .padding(18.dp)
            .testTag("blocklist_alert_banner")
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Outlined.Block,
                contentDescription = "Blocklist Hit",
                tint = RiskHigh,
                modifier = Modifier.size(22.dp)
              )
              Text(
                text = "BLOCKLIST CRITICAL ALERT",
                style = MaterialTheme.typography.titleSmall.copy(
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.8.sp,
                  color = RiskHigh
                )
              )
            }

            Text(
              text = "The user in this document is listed in the enforcement blocklist.",
              style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF991B1B)
              )
            )

            Text(
              text = "Reason: ${specimen.blocklistReason ?: "Matched sanctions & identity watch records"}",
              style = MaterialTheme.typography.bodySmall.copy(
                color = Color(0xFF7F1D1D),
                fontSize = 12.sp
              )
            )

            Text(
              text = "Official Protocol: Immediate rejection required under AML compliance rules.",
              style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = Color(0xFF991B1B)
              )
            )
          }
        }
      }
    } else {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF0FDF4))
            .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(16.dp))
            .padding(14.dp)
            .testTag("blocklist_cleared_banner")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFFDCFCE7)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = "Watchlist Cleared",
                tint = Color(0xFF16A34A),
                modifier = Modifier.size(18.dp)
              )
            }
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "IDENTITY WATCHLIST CLEARED",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.8.sp,
                  color = Color(0xFF15803D)
                )
              )
              Text(
                text = "Clean record: Passed Interpol Red Notice, OFAC SDN, and international AML sanctions screening.",
                style = MaterialTheme.typography.bodySmall.copy(
                  fontSize = 11.sp,
                  color = Color(0xFF166534)
                )
              )
            }
          }
        }
      }
    }

    // 3. HERO OVERALL COMBINED SCORE CARD
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp))
          .background(VeriWhite)
          .border(1.dp, VeriBorder, RoundedCornerShape(20.dp))
          .padding(22.dp)
          .testTag("result_hero_card"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Top status pill
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(specimen.riskLevel.backgroundColor)
              .border(1.dp, specimen.riskLevel.borderColor, RoundedCornerShape(8.dp))
              .padding(horizontal = 12.dp, vertical = 6.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = if (specimen.riskLevel == RiskLevel.LOW) Icons.Outlined.CheckCircle else Icons.Outlined.ReportProblem,
                contentDescription = null,
                tint = specimen.riskLevel.textColor,
                modifier = Modifier.size(14.dp)
              )
              Text(
                text = specimen.riskLevel.badgeText,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontFamily = FontFamily.Monospace,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.5.sp,
                  color = specimen.riskLevel.textColor
                )
              )
            }
          }

          Text(
            text = "${specimen.documentType.displayName} (${specimen.documentType.code})",
            style = MaterialTheme.typography.labelSmall.copy(
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium,
              color = VeriNavy600
            )
          )
        }

        // Score display and DNA view
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "OVERALL COMPOSITE SCORE",
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Bold,
                color = VeriNavy400
              )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
              Text(
                text = "${specimen.trustScore}",
                style = MaterialTheme.typography.displayLarge.copy(
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.SansSerif,
                  color = if (isBlocklisted) RiskHigh else VeriNavy950,
                  fontSize = 54.sp,
                  lineHeight = 54.sp
                )
              )
              Text(
                text = " /100",
                modifier = Modifier.padding(bottom = 6.dp),
                style = MaterialTheme.typography.titleMedium.copy(
                  color = VeriNavy400,
                  fontWeight = FontWeight.Medium
                )
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = if (isBlocklisted) "BLOCKED: Watchlist Match" else specimen.riskLevel.headline,
              style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = if (isBlocklisted) RiskHigh else specimen.riskLevel.textColor
              )
            )
          }

          DocumentDnaView(
            dna = specimen.dna,
            size = 96.dp,
            showLabel = true,
            interactive = true
          )
        }

        HorizontalDivider(color = VeriBorderSubtle, thickness = 1.dp)

        Text(
          text = specimen.investigationSummary,
          style = MaterialTheme.typography.bodySmall.copy(
            color = VeriNavy800,
            lineHeight = 18.sp
          )
        )
      }
    }

    // 4. OFFICER DECISION WORKFLOW STATION (ACCEPT OR REJECT BASED ON SCORE)
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(18.dp))
          .background(VeriWhite)
          .border(1.5.dp, if (currentDecision == OfficerDecisionStatus.ACCEPTED) RiskLow else if (currentDecision == OfficerDecisionStatus.REJECTED) RiskHigh else VeriNavy950, RoundedCornerShape(18.dp))
          .padding(18.dp)
          .testTag("officer_decision_station"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Outlined.Policy,
              contentDescription = null,
              tint = VeriNavy950,
              modifier = Modifier.size(20.dp)
            )
            Text(
              text = "OFFICER REVIEW CONSOLE",
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.0.sp,
                color = VeriNavy950
              )
            )
          }

          // Decision Status Badge
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(
                when (currentDecision) {
                  OfficerDecisionStatus.ACCEPTED -> Color(0xFFECFDF5)
                  OfficerDecisionStatus.REJECTED -> Color(0xFFFEF2F2)
                  OfficerDecisionStatus.PENDING -> VeriBackground
                }
              )
              .border(
                1.dp,
                when (currentDecision) {
                  OfficerDecisionStatus.ACCEPTED -> RiskLow
                  OfficerDecisionStatus.REJECTED -> RiskHigh
                  OfficerDecisionStatus.PENDING -> VeriBorder
                },
                RoundedCornerShape(6.dp)
              )
              .padding(horizontal = 10.dp, vertical = 4.dp)
          ) {
            Text(
              text = currentDecision.name,
              style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = when (currentDecision) {
                  OfficerDecisionStatus.ACCEPTED -> RiskLow
                  OfficerDecisionStatus.REJECTED -> RiskHigh
                  OfficerDecisionStatus.PENDING -> VeriNavy600
                }
              )
            )
          }
        }

        Text(
          text = when (currentDecision) {
            OfficerDecisionStatus.ACCEPTED -> "Document accepted by Officer (ID: 4821). Identity cleared."
            OfficerDecisionStatus.REJECTED -> "Document rejected by Officer. Identity blocked and flagged."
            OfficerDecisionStatus.PENDING -> "Based on the overall score of ${specimen.trustScore}/100 and forensic checks below, accept or reject this document:"
          },
          style = MaterialTheme.typography.bodySmall.copy(
            color = VeriNavy800,
            lineHeight = 17.sp
          )
        )

        // Officer Actions Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // ACCEPT BUTTON
          Button(
            onClick = {
              showDecisionDialog = OfficerDecisionStatus.ACCEPTED
              officerNotesInput = "Verified authentic. Score: ${specimen.trustScore}/100."
            },
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("officer_accept_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = if (currentDecision == OfficerDecisionStatus.ACCEPTED) Color(0xFF047857) else Color(0xFF059669),
              contentColor = VeriWhite
            )
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
              )
              Text("Accept", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
          }

          // REJECT BUTTON
          Button(
            onClick = {
              showDecisionDialog = OfficerDecisionStatus.REJECTED
              officerNotesInput = if (isBlocklisted) {
                "User is in blocklist. Rejection mandatory."
              } else if (specimen.isTextModified || specimen.isImageModified) {
                "Document tampered: Text/Image modifications detected."
              } else {
                "Low overall trust score (${specimen.trustScore}/100)."
              }
            },
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("officer_reject_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = if (currentDecision == OfficerDecisionStatus.REJECTED) Color(0xFFB91C1C) else Color(0xFFDC2626),
              contentColor = VeriWhite
            )
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = Icons.Outlined.Block,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
              )
              Text("Reject", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
          }
        }
      }
    }

    // 5. SERVICE 3: FACE RECOGNITION COMPARISON CARD
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .background(VeriWhite)
          .border(1.dp, VeriBorder, RoundedCornerShape(16.dp))
          .padding(18.dp)
          .testTag("face_recognition_service_card"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Outlined.Person,
              contentDescription = null,
              tint = VeriNavy950,
              modifier = Modifier.size(20.dp)
            )
            Text(
              text = "FACE RECOGNITION SERVICE",
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.0.sp,
                color = VeriNavy950
              )
            )
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(if (specimen.faceMatchScore >= 75) Color(0xFFECFDF5) else Color(0xFFFEF2F2))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = "${specimen.faceMatchScore}% SIMILARITY",
              style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = if (specimen.faceMatchScore >= 75) RiskLow else RiskHigh
              )
            )
          }
        }

        Text(
          text = "Compares the photo on the document against the live photo taken from the camera.",
          style = MaterialTheme.typography.bodySmall.copy(
            color = VeriNavy600,
            fontSize = 11.sp
          )
        )

        // Visual Comparison Mock Frames
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(VeriBackground)
            .padding(12.dp),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
              modifier = Modifier
                .size(width = 60.dp, height = 75.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(VeriWhite)
                .border(1.dp, VeriBorder, RoundedCornerShape(8.dp)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                tint = VeriNavy800,
                modifier = Modifier.size(36.dp)
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Document Photo", fontSize = 10.sp, color = VeriNavy600)
          }

          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = if (specimen.isFaceMatched) Icons.Outlined.CheckCircle else Icons.Outlined.WarningAmber,
              contentDescription = null,
              tint = if (specimen.isFaceMatched) RiskLow else RiskHigh,
              modifier = Modifier.size(24.dp)
            )
            Text(
              text = if (specimen.isFaceMatched) "MATCH" else "MISMATCH",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              color = if (specimen.isFaceMatched) RiskLow else RiskHigh
            )
          }

          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
              modifier = Modifier
                .size(width = 60.dp, height = 75.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(VeriWhite)
                .border(1.dp, VeriBorder, RoundedCornerShape(8.dp)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                tint = VeriNavy950,
                modifier = Modifier.size(36.dp)
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Camera Photo", fontSize = 10.sp, color = VeriNavy600)
          }
        }

        // Service Messages
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = "Service Diagnostic Messages:",
            style = MaterialTheme.typography.labelSmall.copy(
              fontSize = 10.sp,
              fontWeight = FontWeight.SemiBold,
              color = VeriNavy800
            )
          )
          MessageBulletList(messages = specimen.faceMatchMessages)
        }
      }
    }

    // 6. SERVICE 2: TAMPERING INSPECTION SERVICE (REAL VS FAKE & MODIFICATIONS)
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .background(VeriWhite)
          .border(1.dp, VeriBorder, RoundedCornerShape(16.dp))
          .padding(18.dp)
          .testTag("tampering_service_card"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Outlined.FindInPage,
              contentDescription = null,
              tint = VeriNavy950,
              modifier = Modifier.size(20.dp)
            )
            Text(
              text = "TAMPERING INSPECTION SERVICE",
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.0.sp,
                color = VeriNavy950
              )
            )
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(if (specimen.tamperScore >= 75) Color(0xFFECFDF5) else Color(0xFFFEF2F2))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = "${specimen.tamperScore}% AUTHENTIC",
              style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = if (specimen.tamperScore >= 75) RiskLow else RiskHigh
              )
            )
          }
        }

        Text(
          text = "Inspects if document is real or fake by text format, document rules, and text/image modifications.",
          style = MaterialTheme.typography.bodySmall.copy(
            color = VeriNavy600,
            fontSize = 11.sp
          )
        )

        // Text & Image Modification Status Badges
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(if (!specimen.isTextModified) Color(0xFFECFDF5) else Color(0xFFFEF2F2))
              .border(1.dp, if (!specimen.isTextModified) RiskLow else RiskHigh, RoundedCornerShape(8.dp))
              .padding(8.dp)
          ) {
            Column {
              Text(
                text = "TEXT FORMAT",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = VeriNavy600
              )
              Text(
                text = if (!specimen.isTextModified) "✓ Unaltered" else "⚠ Modified Text",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (!specimen.isTextModified) RiskLow else RiskHigh
              )
            }
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(if (!specimen.isImageModified) Color(0xFFECFDF5) else Color(0xFFFEF2F2))
              .border(1.dp, if (!specimen.isImageModified) RiskLow else RiskHigh, RoundedCornerShape(8.dp))
              .padding(8.dp)
          ) {
            Column {
              Text(
                text = "IMAGE INTEGRITY",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = VeriNavy600
              )
              Text(
                text = if (!specimen.isImageModified) "✓ Original Photo" else "⚠ Modified Image",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (!specimen.isImageModified) RiskLow else RiskHigh
              )
            }
          }
        }

        // Tampering Diagnostic Messages
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = "Inspection Diagnostic Messages:",
            style = MaterialTheme.typography.labelSmall.copy(
              fontSize = 10.sp,
              fontWeight = FontWeight.SemiBold,
              color = VeriNavy800
            )
          )
          MessageBulletList(messages = specimen.tamperingMessages)
        }
      }
    }

    // 7. SERVICE 1: DOCUMENT TYPE DETECTION, OCR SERVICE & POSTGRESQL DB
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .background(VeriWhite)
          .border(1.dp, VeriBorder, RoundedCornerShape(16.dp))
          .padding(18.dp)
          .testTag("ocr_postgres_card"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Outlined.Storage,
              contentDescription = null,
              tint = VeriNavy950,
              modifier = Modifier.size(20.dp)
            )
            Text(
              text = "OCR SERVICE & POSTGRESQL DB",
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.0.sp,
                color = VeriNavy950
              )
            )
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(VeriBackground)
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = "STORED IN DB",
              style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = VeriAccent
              )
            )
          }
        }

        Text(
          text = "Detected document type and extracted OCR details persisted to PostgreSQL database schema.",
          style = MaterialTheme.typography.bodySmall.copy(
            color = VeriNavy600,
            fontSize = 11.sp
          )
        )

        // Extracted Key Fields Table
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(VeriBackground)
            .padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          ExtractedFieldRow(label = "Document Type", value = specimen.documentType.displayName)
          ExtractedFieldRow(label = "Full Name", value = specimen.holderName)
          ExtractedFieldRow(label = "Document #", value = specimen.documentNumber)
          ExtractedFieldRow(label = "Date of Birth", value = specimen.dateOfBirth)
          ExtractedFieldRow(label = "Expiry Date", value = specimen.expiryDate)
          ExtractedFieldRow(label = "Nationality", value = specimen.countryCode)
        }

        // View PostgreSQL Schema & Query Button
        OutlinedButton(
          onClick = { showPostgresSchemaModal = true },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, VeriBorder)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              imageVector = Icons.Outlined.Storage,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
              tint = VeriNavy950
            )
            Text(
              text = "Inspect PostgreSQL DDL & Stored Row",
              style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = VeriNavy950
              )
            )
          }
        }
      }
    }

    // 8. Bottom Action Buttons
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(
          onClick = onExplainReasons,
          modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .testTag("inspect_evidence_button"),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = VeriNavy950,
            contentColor = VeriWhite
          )
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Outlined.Info,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Text(
              text = "Explain Forensic Evidence Tree",
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = VeriWhite
              )
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
              imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
          }
        }

        OutlinedButton(
          onClick = onVerifyAnother,
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("verify_another_button"),
          shape = RoundedCornerShape(14.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, VeriBorder),
          colors = ButtonDefaults.outlinedButtonColors(
            containerColor = VeriWhite,
            contentColor = VeriNavy950
          )
        ) {
          Text(
            text = "Verify Another Document",
            style = MaterialTheme.typography.bodyMedium.copy(
              fontWeight = FontWeight.Medium,
              color = VeriNavy950
            )
          )
        }
      }
    }
  }

  // OFFICER DECISION CONFIRMATION DIALOG
  if (showDecisionDialog != null) {
    val decision = showDecisionDialog!!
    AlertDialog(
      onDismissRequest = { showDecisionDialog = null },
      title = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = if (decision == OfficerDecisionStatus.ACCEPTED) Icons.Outlined.CheckCircle else Icons.Outlined.Block,
            contentDescription = null,
            tint = if (decision == OfficerDecisionStatus.ACCEPTED) RiskLow else RiskHigh
          )
          Text(
            text = if (decision == OfficerDecisionStatus.ACCEPTED) "Confirm Clearance" else "Confirm Rejection",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "You are recording official determination for document #${specimen.documentNumber} (${specimen.holderName}).",
            fontSize = 13.sp
          )
          if (decision == OfficerDecisionStatus.REJECTED) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFFEF2F2))
                .padding(10.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(
                  imageVector = Icons.Outlined.Block,
                  contentDescription = null,
                  tint = RiskHigh,
                  modifier = Modifier.size(16.dp)
                )
                Text(
                  text = "User will be automatically kept under the PostgreSQL and local Blocklist DB. Future verifications for this identity will be rejected.",
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = RiskHigh
                  )
                )
              }
            }
          }
          OutlinedTextField(
            value = officerNotesInput,
            onValueChange = { officerNotesInput = it },
            label = { Text("Officer Audit Notes") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            currentDecision = decision
            onRecordOfficerDecision(decision, officerNotesInput)
            showDecisionDialog = null
            val message = if (decision == OfficerDecisionStatus.REJECTED) {
              "Determination Recorded: User placed under Blocklist in DB"
            } else {
              "Determination Recorded: Identity cleared"
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = if (decision == OfficerDecisionStatus.ACCEPTED) RiskLow else RiskHigh,
            contentColor = VeriWhite
          )
        ) {
          Text(if (decision == OfficerDecisionStatus.REJECTED) "Reject & Blocklist" else "Submit Clearance")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDecisionDialog = null }) {
          Text("Cancel", color = VeriNavy600)
        }
      }
    )
  }

  // POSTGRESQL SCHEMA & QUERY MODAL
  if (showPostgresSchemaModal) {
    AlertDialog(
      onDismissRequest = { showPostgresSchemaModal = false },
      title = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Outlined.Storage,
            contentDescription = null,
            tint = VeriAccent
          )
          Text(
            text = "PostgreSQL DB Storage Schema",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        }
      },
      text = {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .height(340.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(
            text = "Target PostgreSQL Table: kyc_verified_documents",
            style = MaterialTheme.typography.labelSmall.copy(
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              color = VeriNavy800
            )
          )

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(VeriNavy950)
              .padding(12.dp)
          ) {
            LazyColumn {
              item {
                Text(
                  text = PostgresDatabaseService.generateInsertSql(specimen),
                  color = Color(0xFFE2E8F0),
                  fontFamily = FontFamily.Monospace,
                  fontSize = 11.sp,
                  lineHeight = 16.sp
                )
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val sql = PostgresDatabaseService.generateInsertSql(specimen)
            clipboardManager.setText(AnnotatedString(sql))
            Toast.makeText(context, "Postgres SQL copied to clipboard", Toast.LENGTH_SHORT).show()
          },
          colors = ButtonDefaults.buttonColors(containerColor = VeriNavy950)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(imageVector = Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
            Text("Copy SQL")
          }
        }
      },
      dismissButton = {
        TextButton(onClick = { showPostgresSchemaModal = false }) {
          Text("Close", color = VeriNavy600)
        }
      }
    )
  }
}

@Composable
private fun ExtractedFieldRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      fontSize = 11.sp,
      color = VeriNavy600,
      fontWeight = FontWeight.Medium
    )
    Text(
      text = value,
      fontSize = 12.sp,
      color = VeriNavy950,
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.SemiBold
    )
  }
}

@Composable
private fun MessageBulletList(messages: List<String>) {
  Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    for (msg in messages) {
      Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Text("•", color = VeriNavy600, fontSize = 12.sp)
        Text(
          text = msg,
          style = MaterialTheme.typography.bodySmall.copy(
            fontSize = 11.sp,
            color = VeriNavy800
          )
        )
      }
    }
  }
}
