package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VerificationStage
import com.example.ui.theme.VeriAccent
import com.example.ui.theme.VeriAccentLight
import com.example.ui.theme.VeriBorder
import com.example.ui.theme.VeriNavy400
import com.example.ui.theme.VeriNavy600
import com.example.ui.theme.VeriNavy800
import com.example.ui.theme.VeriNavy950
import com.example.ui.theme.VeriWhite

/**
 * AI Verification Timeline:
 * Real-time progression: Detect → Read → Analyze → Verify → Trust
 * Clean, minimal, high-precision forensic timeline with micro-telemetry.
 */
@Composable
fun VerificationTimeline(
  currentStage: VerificationStage,
  completedStages: Set<VerificationStage>,
  modifier: Modifier = Modifier
) {
  val stages = VerificationStage.values()

  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .background(VeriWhite)
      .border(1.dp, VeriBorder, RoundedCornerShape(16.dp))
      .padding(20.dp)
      .testTag("verification_timeline"),
    verticalArrangement = Arrangement.spacedBy(0.dp)
  ) {
    // Header for timeline
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "FORENSIC INVESTIGATION PIPELINE",
        style = MaterialTheme.typography.labelSmall.copy(
          fontSize = 10.sp,
          letterSpacing = 1.2.sp,
          fontWeight = FontWeight.Bold,
          color = VeriNavy400
        )
      )
      Text(
        text = "STAGE ${currentStage.stepIndex} OF 5",
        style = MaterialTheme.typography.labelSmall.copy(
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.SemiBold,
          color = VeriAccent
        )
      )
    }

    Spacer(modifier = Modifier.height(18.dp))

    stages.forEachIndexed { index, stage ->
      val isCompleted = completedStages.contains(stage)
      val isCurrent = currentStage == stage
      val isPending = !isCompleted && !isCurrent
      val isLast = index == stages.size - 1

      TimelineStepItem(
        stage = stage,
        isCompleted = isCompleted,
        isCurrent = isCurrent,
        isPending = isPending,
        isLast = isLast
      )
    }
  }
}

@Composable
private fun TimelineStepItem(
  stage: VerificationStage,
  isCompleted: Boolean,
  isCurrent: Boolean,
  isPending: Boolean,
  isLast: Boolean
) {
  val infiniteTransition = rememberInfiniteTransition(label = "pulse_step")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1.0f,
    targetValue = 1.25f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "step_pulse_scale"
  )

  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.Top
  ) {
    // Left Indicator Column (Node + Vertical connecting line)
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.width(28.dp)
    ) {
      // Step Node
      Box(
        modifier = Modifier
          .size(20.dp),
        contentAlignment = Alignment.Center
      ) {
        when {
          isCompleted -> {
            Box(
              modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(VeriNavy950),
              contentAlignment = Alignment.Center
            ) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(VeriWhite)
              )
            }
          }
          isCurrent -> {
            // Pulsing active node
            Box(
              modifier = Modifier
                .size(18.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(VeriAccentLight)
            )
            Box(
              modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(VeriAccent)
            )
          }
          else -> {
            // Pending node
            Box(
              modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(Color(0xFFF1F5F9))
                .border(1.dp, VeriBorder, CircleShape)
            )
          }
        }
      }

      // Vertical connector line
      if (!isLast) {
        Box(
          modifier = Modifier
            .width(1.5.dp)
            .height(44.dp)
            .background(if (isCompleted) VeriNavy950 else VeriBorder)
        )
      }
    }

    Spacer(modifier = Modifier.width(12.dp))

    // Right Content Column
    Column(
      modifier = Modifier
        .weight(1f)
        .padding(bottom = if (isLast) 0.dp else 16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = stage.title,
          style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = if (isCurrent || isCompleted) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 15.sp,
            color = when {
              isCurrent -> VeriAccent
              isCompleted -> VeriNavy950
              else -> VeriNavy400
            }
          )
        )

        // Status or Telemetry
        when {
          isCompleted -> {
            Text(
              text = "PASSED",
              style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = VeriNavy600
              )
            )
          }
          isCurrent -> {
            Text(
              text = "ANALYZING...",
              style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = VeriAccent
              )
            )
          }
          else -> {
            Text(
              text = "QUEUED",
              style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = VeriNavy400
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(2.dp))

      Text(
        text = stage.operation,
        style = MaterialTheme.typography.bodySmall.copy(
          fontSize = 12.sp,
          lineHeight = 16.sp,
          color = if (isCurrent) VeriNavy800 else VeriNavy600
        )
      )

      if (isCompleted || isCurrent) {
        Spacer(modifier = Modifier.height(3.dp))
        Text(
          text = stage.telemetry,
          style = MaterialTheme.typography.labelSmall.copy(
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = if (isCurrent) VeriAccent else VeriNavy400
          )
        )
      }
    }
  }
}
