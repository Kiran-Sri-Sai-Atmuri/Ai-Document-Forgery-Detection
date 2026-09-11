package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DocumentSpecimen
import com.example.model.RiskLevel
import com.example.model.SampleData
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
 * Screen 1: Home
 * Centered on one dominant "Verify Document" action, with generous whitespace,
 * high-contrast typography, and real-time Room database & AI telemetry stats.
 */
@Composable
fun HomeScreen(
  onStartVerification: () -> Unit,
  onInspectSpecimen: (DocumentSpecimen) -> Unit,
  modifier: Modifier = Modifier,
  verifiedDocuments: List<DocumentSpecimen> = emptyList()
) {
  val totalScreened = verifiedDocuments.size
  val lowRiskCount = verifiedDocuments.count { it.riskLevel == RiskLevel.LOW }
  val passRate = if (totalScreened > 0) (lowRiskCount * 100) / totalScreened else 0
  val highRiskCount = verifiedDocuments.count { it.riskLevel == RiskLevel.HIGH }
  val avgTrust = if (totalScreened > 0) verifiedDocuments.map { it.trustScore }.average().toInt() else 0
  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(VeriBackground)
      .padding(horizontal = 24.dp),
    contentPadding = PaddingValues(top = 28.dp, bottom = 40.dp),
    verticalArrangement = Arrangement.spacedBy(28.dp)
  ) {
    // 1. Minimal Header & System Status
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "VERITRUST",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.6.sp,
              color = VeriNavy950
            )
          )
          Text(
            text = "Forensic Identity Screening",
            style = MaterialTheme.typography.bodySmall.copy(
              color = VeriNavy600
            )
          )
        }

        // Live System Operational Pill
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(VeriWhite)
            .border(1.dp, VeriBorder, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Box(
            modifier = Modifier
              .size(6.dp)
              .clip(CircleShape)
              .background(Color(0xFF10B981)) // Active green
          )
          Text(
            text = "AI ENGINE ACTIVE",
            style = MaterialTheme.typography.labelSmall.copy(
              fontFamily = FontFamily.Monospace,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              color = VeriNavy800
            )
          )
        }
      }
    }

    // 2. Hero Centerpiece with Dominant Action
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp))
          .background(VeriWhite)
          .border(1.dp, VeriBorder, RoundedCornerShape(20.dp))
          .padding(26.dp),
        horizontalAlignment = Alignment.Start
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Outlined.Shield,
            contentDescription = null,
            tint = VeriAccent,
            modifier = Modifier.size(18.dp)
          )
          Text(
            text = "INTELLIGENT CREDENTIAL VERIFICATION",
            style = MaterialTheme.typography.labelSmall.copy(
              fontSize = 10.sp,
              letterSpacing = 1.0.sp,
              fontWeight = FontWeight.Bold,
              color = VeriAccent
            )
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Authenticate documents with AI forensic rigor.",
          style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.SemiBold,
            color = VeriNavy950,
            lineHeight = 32.sp
          )
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "Inspects physical security patterns, font raster alterations, ICAO MRZ checksums, and facial biometric congruence in real time.",
          style = MaterialTheme.typography.bodyMedium.copy(
            color = VeriNavy600,
            lineHeight = 22.sp
          )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Real-Time System Metrics Strip
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(VeriBackground)
            .border(1.dp, VeriBorderSubtle, RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp, horizontal = 8.dp),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "$totalScreened",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = VeriNavy950
              )
            )
            Text(
              text = "SCREENED",
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 8.sp,
                letterSpacing = 0.8.sp,
                color = VeriNavy600
              )
            )
          }

          Box(modifier = Modifier.width(1.dp).height(24.dp).background(VeriBorderSubtle))

          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "$passRate%",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = RiskLow
              )
            )
            Text(
              text = "PASS RATE",
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 8.sp,
                letterSpacing = 0.8.sp,
                color = VeriNavy600
              )
            )
          }

          Box(modifier = Modifier.width(1.dp).height(24.dp).background(VeriBorderSubtle))

          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "$avgTrust%",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = VeriAccent
              )
            )
            Text(
              text = "AVG TRUST",
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 8.sp,
                letterSpacing = 0.8.sp,
                color = VeriNavy600
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // DOMINANT ACTION BUTTON
        Button(
          onClick = onStartVerification,
          modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .testTag("verify_document_button"),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = VeriNavy950,
            contentColor = VeriWhite
          ),
          elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(
              imageVector = Icons.Outlined.Security,
              contentDescription = null,
              modifier = Modifier.size(20.dp)
            )
            Text(
              text = "Verify Document",
              style = MaterialTheme.typography.titleMedium.copy(
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
      }
    }

    // 3. Quick Forensic Audit Logs / Room Database Audit Trail
    item {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "RECENT INVESTIGATIONS",
            style = MaterialTheme.typography.labelSmall.copy(
              fontSize = 10.sp,
              letterSpacing = 1.2.sp,
              fontWeight = FontWeight.Bold,
              color = VeriNavy400
            )
          )
          Text(
            text = "${verifiedDocuments.size} AUDITED",
            style = MaterialTheme.typography.labelSmall.copy(
              fontFamily = FontFamily.Monospace,
              color = VeriNavy400
            )
          )
        }

        // List of recent specimens
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(VeriWhite)
            .border(1.dp, VeriBorder, RoundedCornerShape(16.dp))
        ) {
          if (verifiedDocuments.isEmpty()) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "No documents scanned yet. Tap Verify Document to begin.",
                style = MaterialTheme.typography.bodySmall.copy(color = VeriNavy600)
              )
            }
          } else {
            verifiedDocuments.forEachIndexed { index, specimen ->
              SpecimenAuditItem(
                specimen = specimen,
                onClick = { onInspectSpecimen(specimen) }
              )
              if (index < verifiedDocuments.size - 1) {
                HorizontalDivider(color = VeriBorderSubtle, thickness = 1.dp)
              }
            }
          }
        }
      }
    }

    // 4. Trust Architecture Attributes (Minimal bottom indicators)
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(VeriWhite)
          .border(1.dp, VeriBorder, RoundedCornerShape(14.dp))
          .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        TrustFeaturePill(title = "Zero-Knowledge", caption = "Privacy Preserving")
        Box(modifier = Modifier.width(1.dp).height(24.dp).background(VeriBorder))
        TrustFeaturePill(title = "Explainable AI", caption = "Audit Grade XAI")
        Box(modifier = Modifier.width(1.dp).height(24.dp).background(VeriBorder))
        TrustFeaturePill(title = "Document DNA", caption = "Cryptographic Hash")
      }
    }
  }
}

@Composable
private fun SpecimenAuditItem(
  specimen: DocumentSpecimen,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(horizontal = 18.dp, vertical = 14.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Text(
          text = specimen.title,
          style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.SemiBold,
            color = VeriNavy950,
            fontSize = 14.sp
          )
        )
      }
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = "${specimen.holderName} • ${specimen.documentNumber}",
        style = MaterialTheme.typography.bodySmall.copy(
          fontFamily = FontFamily.Monospace,
          fontSize = 11.sp,
          color = VeriNavy600
        )
      )
    }

    // Risk Pill & Score
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(6.dp))
          .background(specimen.riskLevel.backgroundColor)
          .border(1.dp, specimen.riskLevel.borderColor, RoundedCornerShape(6.dp))
          .padding(horizontal = 8.dp, vertical = 3.dp)
      ) {
        Text(
          text = specimen.riskLevel.badgeText,
          style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = specimen.riskLevel.textColor
          )
        )
      }

      Text(
        text = "${specimen.trustScore}%",
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          color = VeriNavy950
        )
      )

      Icon(
        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
        contentDescription = null,
        tint = VeriNavy400,
        modifier = Modifier.size(16.dp)
      )
    }
  }
}

@Composable
private fun TrustFeaturePill(title: String, caption: String) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = title,
      style = MaterialTheme.typography.labelSmall.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        color = VeriNavy950
      )
    )
    Text(
      text = caption,
      style = MaterialTheme.typography.labelSmall.copy(
        fontSize = 9.sp,
        color = VeriNavy400
      )
    )
  }
}
