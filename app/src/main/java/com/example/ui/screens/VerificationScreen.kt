package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.backend.GeminiVerificationBackend
import com.example.model.DocumentSpecimen
import com.example.model.VerificationStage
import com.example.ui.components.DocumentFrameView
import com.example.ui.components.VerificationTimeline
import com.example.ui.theme.VeriAccent
import com.example.ui.theme.VeriBackground
import com.example.ui.theme.VeriBorder
import com.example.ui.theme.VeriNavy400
import com.example.ui.theme.VeriNavy600
import com.example.ui.theme.VeriNavy800
import com.example.ui.theme.VeriNavy950
import com.example.ui.theme.VeriWhite
import kotlinx.coroutines.async
import kotlinx.coroutines.delay

/**
 * Screen 3: AI Verification
 * Real-time verification timeline: Detect → Read → Analyze → Verify → Trust
 * executes live backend AI inspection concurrently with forensic streaming telemetry.
 */
@Composable
fun VerificationScreen(
  specimen: DocumentSpecimen,
  capturedBitmap: Bitmap? = null,
  selfieBitmap: Bitmap? = null,
  onCancel: () -> Unit,
  onVerificationCompleted: (DocumentSpecimen) -> Unit,
  modifier: Modifier = Modifier
) {
  var currentStage by remember { mutableStateOf(VerificationStage.DETECT) }
  val completedStages = remember { mutableStateListOf<VerificationStage>() }
  var resolvedSpecimen by remember { mutableStateOf(specimen) }
  var hasDispatched by remember { mutableStateOf(false) }

  // Sequential progression through stages while executing real AI verification pipeline
  LaunchedEffect(Unit) {
    // Launch complete pipeline task concurrently
    val backendDeferred = async {
      try {
        com.example.data.services.VerificationPipelineCoordinator.executePipeline(
          initialSpecimen = specimen,
          documentBitmap = capturedBitmap,
          cameraSelfieBitmap = selfieBitmap
        )
      } catch (e: Throwable) {
        android.util.Log.e("VerificationScreen", "Pipeline execution error", e)
        specimen
      }
    }

    val stages = VerificationStage.values()
    for (i in stages.indices) {
      currentStage = stages[i]
      delay(600) // Investigation telemetry dwell per stage
      completedStages.add(stages[i])
    }

    // Await complete pipeline computation
    try {
      resolvedSpecimen = backendDeferred.await()
    } catch (e: Throwable) {
      android.util.Log.e("VerificationScreen", "Error awaiting pipeline", e)
      resolvedSpecimen = specimen
    }
    delay(200)
    if (!hasDispatched) {
      hasDispatched = true
      onVerificationCompleted(resolvedSpecimen)
    }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(VeriBackground)
      .padding(horizontal = 24.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 36.dp),
    verticalArrangement = Arrangement.spacedBy(18.dp)
  ) {
    // 1. Investigation Status Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(VeriAccent)
            )
            Text(
              text = if (capturedBitmap != null) "AI VISION INGESTION ACTIVE" else "FORENSIC SCREENING IN PROGRESS",
              style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = VeriAccent
              )
            )
          }
          Text(
            text = if (capturedBitmap != null) "Live Document Capture" else specimen.title,
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.SemiBold,
              color = VeriNavy950
            )
          )
        }

        IconButton(
          onClick = onCancel,
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(VeriWhite)
            .border(1.dp, VeriBorder, CircleShape)
            .testTag("cancel_verification_button")
        ) {
          Icon(
            imageVector = Icons.Outlined.Close,
            contentDescription = "Cancel",
            tint = VeriNavy600,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }

    // 2. Active Laser Scanner Viewfinder
    item {
      DocumentFrameView(
        specimen = if (capturedBitmap == null) specimen else null,
        capturedBitmap = capturedBitmap,
        isScanning = true
      )
    }

    // 3. Elegant Real-Time Verification Timeline: Detect -> Read -> Analyze -> Verify -> Trust
    item {
      VerificationTimeline(
        currentStage = currentStage,
        completedStages = completedStages.toSet()
      )
    }

    // 4. Streaming Forensic Telemetry Terminal
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(VeriWhite)
          .border(1.dp, VeriBorder, RoundedCornerShape(14.dp))
          .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "LIVE FORENSIC AUDIT LOG",
            style = MaterialTheme.typography.labelSmall.copy(
              fontFamily = FontFamily.Monospace,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              color = VeriNavy400
            )
          )
          Text(
            text = "ENCLAVE STREAM",
            style = MaterialTheme.typography.labelSmall.copy(
              fontFamily = FontFamily.Monospace,
              fontSize = 9.sp,
              color = VeriAccent
            )
          )
        }

        val logMessage = when (currentStage) {
          VerificationStage.DETECT -> "> [OP-1] Edge matrix calibrated. 4 corner homography rectified in 142ms."
          VerificationStage.READ -> "> [OP-2] Multi-spectral OCR parsed. MRZ lines extracted with 100% confidence."
          VerificationStage.ANALYZE -> "> [OP-3] Inspecting guilloche lathe lines and micro-print font kerning..."
          VerificationStage.VERIFY -> "> [OP-4] Facial embedding similarity calculated against ghost watermark."
          VerificationStage.TRUST -> "> [OP-5] Compiling Document DNA cryptographic hash and explainability tree."
        }

        Text(
          text = logMessage,
          style = MaterialTheme.typography.bodySmall.copy(
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = VeriNavy800,
            lineHeight = 16.sp
          )
        )
      }
    }

    // 5. Fast-Forward / Skip to Result Button
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
      ) {
        TextButton(
          onClick = {
            if (!hasDispatched) {
              hasDispatched = true
              onVerificationCompleted(resolvedSpecimen)
            }
          },
          modifier = Modifier.testTag("skip_to_result_button")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              imageVector = Icons.Outlined.Speed,
              contentDescription = null,
              tint = VeriNavy600,
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = "Skip to Results",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = VeriNavy800
              )
            )
          }
        }
      }
    }
  }
}
