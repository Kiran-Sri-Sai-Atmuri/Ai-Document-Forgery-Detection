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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.FactCheck
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.model.FlagSeverity
import com.example.model.ForensicFlag
import com.example.model.RiskLevel
import com.example.ui.components.EvidenceHighlightView
import com.example.ui.theme.RiskHigh
import com.example.ui.theme.RiskHighBg
import com.example.ui.theme.RiskHighBorder
import com.example.ui.theme.RiskLow
import com.example.ui.theme.RiskLowBg
import com.example.ui.theme.RiskLowBorder
import com.example.ui.theme.RiskMedium
import com.example.ui.theme.RiskMediumBg
import com.example.ui.theme.RiskMediumBorder
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
 * Screen 5: Explain
 * "Why was this flagged?" with concise reasons and visual evidence highlights.
 * Deep forensic investigation breakdown with interactive document regions.
 */
@Composable
fun ExplainScreen(
  specimen: DocumentSpecimen,
  onNavigateBack: () -> Unit,
  onDone: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedFlag by remember {
    mutableStateOf(
      specimen.flags.firstOrNull { it.severity == FlagSeverity.CRITICAL }
        ?: specimen.flags.firstOrNull { it.severity == FlagSeverity.WARNING }
        ?: specimen.flags.firstOrNull()
    )
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(VeriBackground)
      .padding(horizontal = 24.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp)
  ) {
    // 1. Top Bar
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onNavigateBack,
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(VeriWhite)
            .border(1.dp, VeriBorder, CircleShape)
            .testTag("explain_back_button")
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
            text = "FORENSIC EXPLAINABILITY",
            style = MaterialTheme.typography.labelSmall.copy(
              fontFamily = FontFamily.Monospace,
              letterSpacing = 1.2.sp,
              fontWeight = FontWeight.Bold,
              color = VeriNavy400
            )
          )
          Text(
            text = "Why was this flagged?",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.SemiBold,
              color = VeriNavy950
            )
          )
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(specimen.riskLevel.backgroundColor)
            .border(1.dp, specimen.riskLevel.borderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = "${specimen.trustScore}%",
            style = MaterialTheme.typography.labelSmall.copy(
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              color = specimen.riskLevel.textColor
            )
          )
        }
      }
    }

    // 2. Visual Evidence Highlights (Interactive Document View)
    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "OPTICAL EVIDENCE OVERLAY",
          style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 10.sp,
            letterSpacing = 1.2.sp,
            fontWeight = FontWeight.Bold,
            color = VeriNavy400
          )
        )

        EvidenceHighlightView(
          specimen = specimen,
          selectedFlag = selectedFlag,
          onSelectFlag = { selectedFlag = it }
        )
      }
    }

    // 3. Flagged Reasons List
    item {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "CONCISE FINDINGS & EVIDENCE",
            style = MaterialTheme.typography.labelSmall.copy(
              fontSize = 10.sp,
              letterSpacing = 1.2.sp,
              fontWeight = FontWeight.Bold,
              color = VeriNavy400
            )
          )
          Text(
            text = "${specimen.flags.size} CHECKS",
            style = MaterialTheme.typography.labelSmall.copy(
              fontFamily = FontFamily.Monospace,
              color = VeriNavy400
            )
          )
        }

        specimen.flags.forEach { flag ->
          ForensicFlagCard(
            flag = flag,
            isSelected = selectedFlag?.id == flag.id,
            onSelect = { selectedFlag = flag }
          )
        }
      }
    }

    // 4. Forensic Metadata & Cryptographic Audit Digest
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .background(VeriWhite)
          .border(1.dp, VeriBorder, RoundedCornerShape(16.dp))
          .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          text = "VERIFICATION AUDIT METADATA",
          style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 10.sp,
            letterSpacing = 1.0.sp,
            fontWeight = FontWeight.Bold,
            color = VeriNavy400
          )
        )

        AuditDetailRow(label = "Audit Digest", value = specimen.auditDigest)
        AuditDetailRow(label = "Document Standard", value = "${specimen.countryCode} · ${specimen.documentType.code}")
        AuditDetailRow(label = "Model Inference", value = "628 ms (FIPS 140-3 Enclave)")
        AuditDetailRow(label = "DNA Parity", value = specimen.dna.structuralParity)
      }
    }

    // 5. Done / Return CTA
    item {
      Button(
        onClick = onDone,
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("explain_done_button"),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = VeriNavy950,
          contentColor = VeriWhite
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
      ) {
        Text(
          text = "Return to Verification Overview",
          style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.SemiBold,
            color = VeriWhite
          )
        )
      }
    }
  }
}

@Composable
private fun ForensicFlagCard(
  flag: ForensicFlag,
  isSelected: Boolean,
  onSelect: () -> Unit
) {
  val severityColor = when (flag.severity) {
    FlagSeverity.CRITICAL -> RiskHigh
    FlagSeverity.WARNING -> RiskMedium
    FlagSeverity.PASS -> RiskLow
  }

  val severityBg = when (flag.severity) {
    FlagSeverity.CRITICAL -> RiskHighBg
    FlagSeverity.WARNING -> RiskMediumBg
    FlagSeverity.PASS -> RiskLowBg
  }

  val severityBorder = when (flag.severity) {
    FlagSeverity.CRITICAL -> RiskHighBorder
    FlagSeverity.WARNING -> RiskMediumBorder
    FlagSeverity.PASS -> RiskLowBorder
  }

  val severityLabel = when (flag.severity) {
    FlagSeverity.CRITICAL -> "CRITICAL FLAG"
    FlagSeverity.WARNING -> "SUSPICIOUS ANOMALY"
    FlagSeverity.PASS -> "VERIFIED PASS"
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .background(VeriWhite)
      .border(
        width = if (isSelected) 1.8.dp else 1.dp,
        color = if (isSelected) VeriNavy950 else VeriBorder,
        shape = RoundedCornerShape(14.dp)
      )
      .clickable(onClick = onSelect)
      .padding(18.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // Top line: Category and Severity Badge
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = flag.category.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
          fontSize = 10.sp,
          letterSpacing = 0.8.sp,
          fontWeight = FontWeight.Bold,
          color = VeriNavy600
        )
      )

      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(6.dp))
          .background(severityBg)
          .border(1.dp, severityBorder, RoundedCornerShape(6.dp))
          .padding(horizontal = 8.dp, vertical = 2.dp)
      ) {
        Text(
          text = severityLabel,
          style = MaterialTheme.typography.labelSmall.copy(
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = severityColor
          )
        )
      }
    }

    // Title
    Text(
      text = flag.title,
      style = MaterialTheme.typography.titleSmall.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        color = VeriNavy950
      )
    )

    // Human-Friendly Concise Reason
    Text(
      text = flag.reason,
      style = MaterialTheme.typography.bodyMedium.copy(
        color = VeriNavy800,
        fontSize = 13.5.sp,
        lineHeight = 19.sp
      )
    )

    // Technical Evidence Snippet
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .background(Color(0xFFF8FAFC))
        .border(0.5.dp, VeriBorder, RoundedCornerShape(8.dp))
        .padding(10.dp)
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
          text = "TECHNICAL EVIDENCE",
          style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            color = VeriNavy400
          )
        )
        Text(
          text = flag.technicalEvidence,
          style = MaterialTheme.typography.bodySmall.copy(
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = VeriNavy950,
            lineHeight = 15.sp
          )
        )
      }
    }

    // Bottom Confidence & Region Tag
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Zone: ${flag.region.tagLabel}",
        style = MaterialTheme.typography.labelSmall.copy(
          fontFamily = FontFamily.Monospace,
          fontSize = 10.sp,
          color = VeriNavy600
        )
      )

      Text(
        text = "AI Confidence: ${(flag.confidenceScore * 100).toInt()}%",
        style = MaterialTheme.typography.labelSmall.copy(
          fontFamily = FontFamily.Monospace,
          fontSize = 10.sp,
          fontWeight = FontWeight.SemiBold,
          color = VeriAccent
        )
      )
    }
  }
}

@Composable
private fun AuditDetailRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall.copy(color = VeriNavy600)
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodySmall.copy(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        color = VeriNavy950
      )
    )
  }
}
