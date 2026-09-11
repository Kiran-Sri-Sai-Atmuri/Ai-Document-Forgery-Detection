package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CropFree
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DocumentSpecimen
import com.example.ui.theme.VeriAccent
import com.example.ui.theme.VeriBorder
import com.example.ui.theme.VeriNavy400
import com.example.ui.theme.VeriNavy600
import com.example.ui.theme.VeriNavy800
import com.example.ui.theme.VeriNavy950
import com.example.ui.theme.VeriWhite

/**
 * Clean Document Frame & Viewfinder
 * Provides an uncluttered, high-precision optical framing guide
 * with corner reticles, laser sweep micro-animation, and document simulation.
 */
@Composable
fun DocumentFrameView(
  specimen: DocumentSpecimen?,
  modifier: Modifier = Modifier,
  capturedBitmap: android.graphics.Bitmap? = null,
  cameraContent: (@Composable () -> Unit)? = null,
  isScanning: Boolean = false,
  highlightRegions: Boolean = false
) {
  val infiniteTransition = rememberInfiniteTransition(label = "scan_laser")
  val scanPosition by infiniteTransition.animateFloat(
    initialValue = 0.05f,
    targetValue = 0.95f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "laser_y"
  )

  Column(
    modifier = modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Document Viewfinder Box
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(1.586f) // Standard ISO/IEC 7810 ID-1 aspect ratio
        .clip(RoundedCornerShape(16.dp))
        .background(Color(0xFFF6F8FA))
        .border(1.dp, VeriBorder, RoundedCornerShape(16.dp))
        .testTag("document_viewfinder_frame"),
      contentAlignment = Alignment.Center
    ) {
      if (cameraContent != null) {
        cameraContent()
      } else if (capturedBitmap != null) {
        Image(
          bitmap = capturedBitmap.asImageBitmap(),
          contentDescription = "Captured Document Photo",
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop
        )
      } else if (specimen != null) {
        // Render stylized document specimen representation
        DocumentSpecimenCard(specimen = specimen)
      } else {
        // Blank camera viewfinder state
        EmptyViewfinderPlaceholder()
      }

      // High-precision corner reticles and laser scan line
      Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cornerLength = 26f
        val strokeW = 2.5f
        val reticleColor = if (isScanning) VeriAccent else VeriNavy950

        // Top-Left Corner
        drawLine(reticleColor, Offset(16f, 16f), Offset(16f + cornerLength, 16f), strokeW, cap = StrokeCap.Round)
        drawLine(reticleColor, Offset(16f, 16f), Offset(16f, 16f + cornerLength), strokeW, cap = StrokeCap.Round)

        // Top-Right Corner
        drawLine(reticleColor, Offset(w - 16f, 16f), Offset(w - 16f - cornerLength, 16f), strokeW, cap = StrokeCap.Round)
        drawLine(reticleColor, Offset(w - 16f, 16f), Offset(w - 16f, 16f + cornerLength), strokeW, cap = StrokeCap.Round)

        // Bottom-Left Corner
        drawLine(reticleColor, Offset(16f, h - 16f), Offset(16f + cornerLength, h - 16f), strokeW, cap = StrokeCap.Round)
        drawLine(reticleColor, Offset(16f, h - 16f), Offset(16f, h - 16f - cornerLength), strokeW, cap = StrokeCap.Round)

        // Bottom-Right Corner
        drawLine(reticleColor, Offset(w - 16f, h - 16f), Offset(w - 16f - cornerLength, h - 16f), strokeW, cap = StrokeCap.Round)
        drawLine(reticleColor, Offset(w - 16f, h - 16f), Offset(w - 16f, h - 16f - cornerLength), strokeW, cap = StrokeCap.Round)

        // Center crosshair
        val cx = w / 2f
        val cy = h / 2f
        val chSize = 10f
        drawLine(VeriNavy400.copy(alpha = 0.4f), Offset(cx - chSize, cy), Offset(cx + chSize, cy), 1f)
        drawLine(VeriNavy400.copy(alpha = 0.4f), Offset(cx, cy - chSize), Offset(cx, cy + chSize), 1f)

        // Laser scan line when active
        if (isScanning) {
          val laserY = h * scanPosition
          drawLine(
            color = VeriAccent,
            start = Offset(24f, laserY),
            end = Offset(w - 24f, laserY),
            strokeWidth = 2.2f,
            cap = StrokeCap.Round
          )
          // Glow halo
          drawLine(
            color = VeriAccent.copy(alpha = 0.25f),
            start = Offset(24f, laserY),
            end = Offset(w - 24f, laserY),
            strokeWidth = 6f,
            cap = StrokeCap.Round
          )
        }
      }
    }
  }
}

@Composable
fun DocumentSpecimenCard(specimen: DocumentSpecimen) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(14.dp)
      .clip(RoundedCornerShape(12.dp))
      .background(VeriWhite)
      .border(1.dp, VeriBorder, RoundedCornerShape(12.dp))
      .padding(14.dp)
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Top header of document
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = specimen.countryCode,
            style = MaterialTheme.typography.labelSmall.copy(
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.2.sp,
              color = VeriNavy400
            )
          )
          Text(
            text = specimen.title.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.Bold,
              color = VeriNavy950,
              fontSize = 11.sp
            )
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Box(
            modifier = Modifier
              .size(6.dp)
              .clip(CircleShape)
              .background(VeriAccent)
          )
          Text(
            text = specimen.documentType.code,
            style = MaterialTheme.typography.labelSmall.copy(
              fontFamily = FontFamily.Monospace,
              color = VeriNavy600
            )
          )
        }
      }

      // Middle content: Photo placeholder & metadata
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Biometric Portrait Silhouette
        Box(
          modifier = Modifier
            .size(width = 46.dp, height = 58.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFE2E8F0))
            .border(1.dp, VeriBorder, RoundedCornerShape(6.dp)),
          contentAlignment = Alignment.Center
        ) {
          Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height * 0.38f
            // Head
            drawCircle(VeriNavy600.copy(alpha = 0.5f), radius = 10f, center = Offset(cx, cy))
            // Shoulders
            drawArc(
              color = VeriNavy600.copy(alpha = 0.5f),
              startAngle = 180f,
              sweepAngle = 180f,
              useCenter = true,
              topLeft = Offset(cx - 16f, cy + 6f),
              size = Size(32f, 32f)
            )
          }
        }

        // Alphanumeric data rows
        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
          Text(
            text = specimen.holderName,
            style = MaterialTheme.typography.bodyMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = VeriNavy950
            )
          )
          Text(
            text = "DOC NO: ${specimen.documentNumber}",
            style = MaterialTheme.typography.bodySmall.copy(
              fontFamily = FontFamily.Monospace,
              fontSize = 11.sp,
              color = VeriNavy800
            )
          )
          Text(
            text = "EXP: ${specimen.expiryDate}",
            style = MaterialTheme.typography.bodySmall.copy(
              fontFamily = FontFamily.Monospace,
              fontSize = 10.sp,
              color = VeriNavy600
            )
          )
        }
      }

      // Bottom MRZ simulation strip
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color(0xFFF8FAFC), RoundedCornerShape(4.dp))
          .border(0.5.dp, VeriBorder, RoundedCornerShape(4.dp))
          .padding(horizontal = 6.dp, vertical = 3.dp)
      ) {
        Text(
          text = "P<${specimen.countryCode}${specimen.holderName.replace(" ", "<<")}<<<<<<<<<<<<<<<\n${specimen.documentNumber}<4${specimen.countryCode}8803142M3103038<<<<<<<<<<<<<<<2",
          style = MaterialTheme.typography.bodySmall.copy(
            fontFamily = FontFamily.Monospace,
            fontSize = 7.5.sp,
            lineHeight = 10.sp,
            letterSpacing = 0.5.sp,
            color = VeriNavy800
          ),
          maxLines = 2
        )
      }
    }
  }
}

@Composable
private fun EmptyViewfinderPlaceholder() {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
    modifier = Modifier.padding(24.dp)
  ) {
    Icon(
      imageVector = Icons.Outlined.CropFree,
      contentDescription = null,
      tint = VeriNavy400,
      modifier = Modifier.size(36.dp)
    )
    Spacer(modifier = Modifier.height(10.dp))
    Text(
      text = "Position ID or Passport within frame",
      style = MaterialTheme.typography.bodySmall.copy(
        fontWeight = FontWeight.Medium,
        color = VeriNavy600
      )
    )
    Text(
      text = "Auto-rectification active",
      style = MaterialTheme.typography.labelSmall.copy(
        color = VeriNavy400
      )
    )
  }
}
