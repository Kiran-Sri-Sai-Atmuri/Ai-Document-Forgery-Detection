package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DocumentSpecimen
import com.example.model.FlagSeverity
import com.example.model.ForensicFlag
import com.example.ui.theme.RiskHigh
import com.example.ui.theme.RiskLow
import com.example.ui.theme.RiskMedium
import com.example.ui.theme.VeriAccent
import com.example.ui.theme.VeriBorder
import com.example.ui.theme.VeriNavy400
import com.example.ui.theme.VeriNavy600
import com.example.ui.theme.VeriNavy800
import com.example.ui.theme.VeriNavy950
import com.example.ui.theme.VeriWhite

/**
 * Visual Evidence Inspection View
 * Renders the document preview with interactive bounding boxes and forensic pins
 * directly targeting detected anomalies.
 */
@Composable
fun EvidenceHighlightView(
  specimen: DocumentSpecimen,
  selectedFlag: ForensicFlag?,
  onSelectFlag: (ForensicFlag) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // Document canvas with overlay bounding regions
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(1.586f)
        .clip(RoundedCornerShape(16.dp))
        .background(Color(0xFFF1F5F9))
        .border(1.dp, VeriBorder, RoundedCornerShape(16.dp))
        .testTag("evidence_document_viewport")
    ) {
      // Underneath: Stylized Document Card
      DocumentSpecimenCard(specimen = specimen)

      // Overlay: Forensic Bounding Boxes and Pins
      Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        specimen.flags.forEach { flag ->
          val isSelected = selectedFlag?.id == flag.id
          val region = flag.region
          val rx = region.xNorm * w
          val ry = region.yNorm * h
          val rw = region.widthNorm * w
          val rh = region.heightNorm * h

          val flagColor = when (flag.severity) {
            FlagSeverity.CRITICAL -> RiskHigh
            FlagSeverity.WARNING -> RiskMedium
            FlagSeverity.PASS -> RiskLow
          }

          val boxColor = if (isSelected) flagColor else flagColor.copy(alpha = 0.5f)
          val strokeWidth = if (isSelected) 2.5f else 1.2f

          // Draw dashed or solid bounding rect
          drawRect(
            color = boxColor,
            topLeft = Offset(rx, ry),
            size = Size(rw, rh),
            style = Stroke(
              width = strokeWidth,
              pathEffect = if (isSelected) null else PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
            )
          )

          // Draw corner anchor crosshairs
          val anchorL = 6f
          drawLine(boxColor, Offset(rx, ry), Offset(rx + anchorL, ry), strokeWidth)
          drawLine(boxColor, Offset(rx, ry), Offset(rx, ry + anchorL), strokeWidth)
          drawLine(boxColor, Offset(rx + rw, ry + rh), Offset(rx + rw - anchorL, ry + rh), strokeWidth)
          drawLine(boxColor, Offset(rx + rw, ry + rh), Offset(rx + rw, ry + rh - anchorL), strokeWidth)

          // Halo fill when selected
          if (isSelected) {
            drawRect(
              color = flagColor.copy(alpha = 0.08f),
              topLeft = Offset(rx, ry),
              size = Size(rw, rh)
            )
          }
        }
      }

      // Interactive Click Overlays
      specimen.flags.forEach { flag ->
        val isSelected = selectedFlag?.id == flag.id
        val region = flag.region

        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(
              start = (region.xNorm * 340).dp,
              top = (region.yNorm * 215).dp
            )
        ) {
          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(
                if (isSelected) VeriNavy950 else VeriWhite
              )
              .border(
                1.dp,
                if (isSelected) VeriNavy950 else VeriBorder,
                RoundedCornerShape(4.dp)
              )
              .clickable { onSelectFlag(flag) }
              .padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(
                  when (flag.severity) {
                    FlagSeverity.CRITICAL -> RiskHigh
                    FlagSeverity.WARNING -> RiskMedium
                    FlagSeverity.PASS -> RiskLow
                  }
                )
            )
            Text(
              text = region.tagLabel,
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) VeriWhite else VeriNavy950
              )
            )
          }
        }
      }
    }

    // Explanatory badge below viewer
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Tap highlighted region to inspect optical evidence",
        style = MaterialTheme.typography.bodySmall.copy(
          fontSize = 11.sp,
          color = VeriNavy600
        )
      )
      Text(
        text = "${specimen.flags.size} EVIDENCE ZONES",
        style = MaterialTheme.typography.labelSmall.copy(
          fontFamily = FontFamily.Monospace,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = VeriNavy400
        )
      )
    }
  }
}
