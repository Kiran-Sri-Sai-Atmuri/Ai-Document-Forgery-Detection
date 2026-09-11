package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DocumentDna
import com.example.ui.theme.VeriAccent
import com.example.ui.theme.VeriBorder
import com.example.ui.theme.VeriNavy400
import com.example.ui.theme.VeriNavy600
import com.example.ui.theme.VeriNavy800
import com.example.ui.theme.VeriNavy950
import com.example.ui.theme.VeriWhite
import kotlin.math.cos
import kotlin.math.sin

/**
 * Signature "Document DNA" Visual:
 * A proprietary abstract biometric/cryptographic fingerprint synthesized
 * directly from document structural features, glyph entropy, and OCR geometry.
 */
@Composable
fun DocumentDnaView(
  dna: DocumentDna,
  modifier: Modifier = Modifier,
  size: Dp = 96.dp,
  showLabel: Boolean = true,
  interactive: Boolean = true
) {
  var showDetailsDialog by remember { mutableStateOf(false) }

  val infiniteTransition = rememberInfiniteTransition(label = "dna_shimmer")
  val pulseProgress by infiniteTransition.animateFloat(
    initialValue = 0.97f,
    targetValue = 1.03f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "dna_pulse"
  )

  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier = Modifier
        .size(size)
        .clip(RoundedCornerShape(18.dp))
        .background(VeriWhite)
        .border(1.dp, VeriBorder, RoundedCornerShape(18.dp))
        .testTag("document_dna_visual")
        .then(
          if (interactive) {
            Modifier.clickable { showDetailsDialog = true }
          } else Modifier
        )
        .padding(8.dp),
      contentAlignment = Alignment.Center
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = this.size.width / 2f
        val centerY = this.size.height / 2f
        val maxRadius = (minOf(this.size.width, this.size.height) / 2f) * 0.88f * pulseProgress

        // Draw central core glyph
        drawCircle(
          color = VeriNavy950,
          radius = 3.5f,
          center = Offset(centerX, centerY)
        )

        // Draw concentric biometric ridges with gaps calculated from DNA frequencies
        val arcLayers = dna.arcFrequencies.size.coerceAtLeast(5)
        for (i in 0 until arcLayers) {
          val layerRatio = (i + 1).toFloat() / (arcLayers + 0.5f)
          val arcRadius = maxRadius * layerRatio
          val frequency = dna.arcFrequencies.getOrElse(i) { 0.5f }

          val strokeWidth = when {
            i == 0 -> 2.2f
            i % 2 == 0 -> 1.8f
            else -> 1.4f
          }

          // Cryptographic arc angles
          val startAngle1 = (dna.seedValue % 60 + (i * 47)) % 360f
          val sweepAngle1 = 80f + (frequency * 70f)

          val startAngle2 = (startAngle1 + sweepAngle1 + 25f) % 360f
          val sweepAngle2 = 60f + ((1f - frequency) * 80f)

          val arcColor = when {
            i == arcLayers - 2 -> VeriAccent
            i % 2 == 0 -> VeriNavy950
            else -> VeriNavy800.copy(alpha = 0.85f)
          }

          // Arc 1
          drawArc(
            color = arcColor,
            startAngle = startAngle1,
            sweepAngle = sweepAngle1.coerceAtMost(160f),
            useCenter = false,
            topLeft = Offset(centerX - arcRadius, centerY - arcRadius),
            size = Size(arcRadius * 2, arcRadius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
          )

          // Arc 2
          drawArc(
            color = arcColor.copy(alpha = 0.75f),
            startAngle = startAngle2,
            sweepAngle = sweepAngle2.coerceAtMost(140f),
            useCenter = false,
            topLeft = Offset(centerX - arcRadius, centerY - arcRadius),
            size = Size(arcRadius * 2, arcRadius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
          )

          // Micro anchor nodes on key radii
          if (i % 2 == 1) {
            val nodeAngleRad = Math.toRadians((startAngle1 + sweepAngle1).toDouble())
            val nx = centerX + (arcRadius * cos(nodeAngleRad)).toFloat()
            val ny = centerY + (arcRadius * sin(nodeAngleRad)).toFloat()
            drawCircle(
              color = VeriAccent,
              radius = 2.2f,
              center = Offset(nx, ny)
            )
          }
        }

        // Sub-micro crosshair ticks on perimeter
        for (angle in 0 until 360 step 45) {
          val rad = Math.toRadians(angle.toDouble())
          val innerR = maxRadius * 0.95f
          val outerR = maxRadius * 1.02f
          val x1 = centerX + (innerR * cos(rad)).toFloat()
          val y1 = centerY + (innerR * sin(rad)).toFloat()
          val x2 = centerX + (outerR * cos(rad)).toFloat()
          val y2 = centerY + (outerR * sin(rad)).toFloat()

          drawLine(
            color = VeriNavy400.copy(alpha = 0.5f),
            start = Offset(x1, y1),
            end = Offset(x2, y2),
            strokeWidth = 1f
          )
        }
      }
    }

    if (showLabel) {
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "DOCUMENT DNA",
        style = MaterialTheme.typography.labelSmall.copy(
          fontSize = 9.sp,
          letterSpacing = 1.2.sp,
          fontWeight = FontWeight.Bold,
          color = VeriNavy400
        )
      )
      Text(
        text = dna.dnaHash,
        style = MaterialTheme.typography.bodySmall.copy(
          fontFamily = FontFamily.Monospace,
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium,
          color = VeriNavy950
        )
      )
    }
  }

  if (showDetailsDialog) {
    AlertDialog(
      onDismissRequest = { showDetailsDialog = false },
      title = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Outlined.Fingerprint,
            contentDescription = null,
            tint = VeriNavy950,
            modifier = Modifier.size(22.dp)
          )
          Text(
            text = "Document DNA Forensics",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
          )
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "The Document DNA is a proprietary structural fingerprint synthesized from multi-spectral scanning:",
            style = MaterialTheme.typography.bodyMedium
          )
          Column(
            modifier = Modifier
              .background(VeriWhite, RoundedCornerShape(10.dp))
              .border(1.dp, VeriBorder, RoundedCornerShape(10.dp))
              .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            DnaMetricRow(label = "Signature Hash", value = dna.dnaHash)
            DnaMetricRow(label = "Entropy Metric", value = "%.3f (High)".format(dna.entropyMetric))
            DnaMetricRow(label = "Forensic Nodes", value = "${dna.nodeCount} Anchor Points")
            DnaMetricRow(label = "Integrity Status", value = dna.structuralParity)
          }
          Text(
            text = "Any digital alteration to typography, security thread geometry, or MRZ parity fundamentally alters the resulting biometric arcs.",
            style = MaterialTheme.typography.bodySmall.copy(color = VeriNavy600)
          )
        }
      },
      confirmButton = {
        TextButton(onClick = { showDetailsDialog = false }) {
          Text("Close", color = VeriNavy950, fontWeight = FontWeight.SemiBold)
        }
      },
      shape = RoundedCornerShape(16.dp),
      containerColor = VeriWhite
    )
  }
}

@Composable
private fun DnaMetricRow(label: String, value: String) {
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
        color = VeriNavy950
      )
    )
  }
}
