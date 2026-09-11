package com.example

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.DocumentSpecimen
import com.example.model.SampleData
import com.example.ui.screens.ExplainScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ResultScreen
import com.example.ui.screens.ScanUploadScreen
import com.example.ui.screens.VerificationScreen
import com.example.ui.theme.VeriBackground
import com.example.ui.viewmodel.MainViewModel

sealed class Screen {
  object Home : Screen()
  object ScanUpload : Screen()
  data class Verification(
    val specimen: DocumentSpecimen,
    val bitmap: Bitmap? = null,
    val selfieBitmap: Bitmap? = null
  ) : Screen()
  data class Result(val specimen: DocumentSpecimen) : Screen()
  data class Explain(val specimen: DocumentSpecimen) : Screen()
}

@Composable
fun VeriTrustApp(
  viewModel: MainViewModel = viewModel()
) {
  var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
  val verifiedDocuments by viewModel.verifiedDocuments.collectAsStateWithLifecycle()

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = VeriBackground
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "screen_transition"
      ) { screen ->
        when (screen) {
          is Screen.Home -> {
            HomeScreen(
              onStartVerification = { currentScreen = Screen.ScanUpload },
              onInspectSpecimen = { specimen -> currentScreen = Screen.Result(specimen) },
              verifiedDocuments = verifiedDocuments
            )
          }

          is Screen.ScanUpload -> {
            ScanUploadScreen(
              onNavigateBack = { currentScreen = Screen.Home },
              onProceedToVerification = { specimen, docBitmap, selfieBitmap ->
                currentScreen = Screen.Verification(specimen, docBitmap, selfieBitmap)
              }
            )
          }

          is Screen.Verification -> {
            VerificationScreen(
              specimen = screen.specimen,
              capturedBitmap = screen.bitmap,
              selfieBitmap = screen.selfieBitmap,
              onCancel = { currentScreen = Screen.ScanUpload },
              onVerificationCompleted = { verifiedSpecimen ->
                viewModel.saveVerifiedDocument(verifiedSpecimen) { persistedSpecimen ->
                  currentScreen = Screen.Result(persistedSpecimen)
                }
              }
            )
          }

          is Screen.Result -> {
            ResultScreen(
              specimen = screen.specimen,
              onExplainReasons = {
                currentScreen = Screen.Explain(screen.specimen)
              },
              onVerifyAnother = {
                currentScreen = Screen.Home
              },
              onRecordOfficerDecision = { status, notes ->
                viewModel.recordOfficerDecision(screen.specimen.id, status, notes)
              }
            )
          }

          is Screen.Explain -> {
            ExplainScreen(
              specimen = screen.specimen,
              onNavigateBack = {
                currentScreen = Screen.Result(screen.specimen)
              },
              onDone = {
                currentScreen = Screen.Result(screen.specimen)
              }
            )
          }
        }
      }
    }
  }
}

