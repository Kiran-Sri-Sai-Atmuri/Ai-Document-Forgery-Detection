package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Cameraswitch
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.DocumentDna
import com.example.model.DocumentSpecimen
import com.example.model.DocumentType
import com.example.model.RiskLevel
import com.example.model.SampleData
import com.example.ui.components.CameraCaptureView
import com.example.ui.components.DocumentFrameView
import com.example.ui.components.capturePhotoFromCamera
import com.example.ui.theme.RiskLow
import com.example.ui.theme.VeriAccent
import com.example.ui.theme.VeriBackground
import com.example.ui.theme.VeriBorder
import com.example.ui.theme.VeriNavy400
import com.example.ui.theme.VeriNavy600
import com.example.ui.theme.VeriNavy800
import com.example.ui.theme.VeriNavy950
import com.example.ui.theme.VeriWhite

enum class CaptureMode {
  CAMERA,
  SPECIMEN
}

enum class ScanStep {
  DOCUMENT_CAPTURE,
  FACE_SELFIE_CAPTURE
}

/**
 * Screen 2: Scan / Upload
 * 2-Stage Optical Acquisition:
 * Step 1: Document Optical Capture / Specimen Selection
 * Step 2: Live Face Selfie Biometric Capture with Front/Back Camera
 */
@Composable
fun ScanUploadScreen(
  onNavigateBack: () -> Unit,
  onProceedToVerification: (DocumentSpecimen, Bitmap?, Bitmap?) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var currentStep by remember { mutableStateOf(ScanStep.DOCUMENT_CAPTURE) }
  var captureMode by remember { mutableStateOf(CaptureMode.CAMERA) }
  val initialLiveSpecimen = remember {
    DocumentSpecimen(
      id = "DOC-${System.currentTimeMillis() % 100000}",
      title = "Scanned Identity Credential",
      subtitle = "Live Optical Acquisition",
      documentType = DocumentType.DRIVERS_LICENSE,
      holderName = "Scanned Document User",
      documentNumber = "DOC-PENDING",
      dateOfBirth = "1993-05-18",
      expiryDate = "2030-11-20",
      countryCode = "USA",
      trustScore = 95,
      riskLevel = RiskLevel.LOW,
      dna = DocumentDna("0x9F4C1B0A", 1001L, listOf(0.5f, 0.6f, 0.5f, 0.7f, 0.5f), 10, 0.4f, "MATCHED"),
      flags = emptyList(),
      authenticityIndex = 95,
      biometricCoherence = 95,
      tamperResistance = 95,
      dataConsistency = 95,
      auditDigest = "SHA256:LIVE_CAPTURE",
      investigationSummary = "Live optical document capture initialized for extraction."
    )
  }
  var selectedSpecimen by remember { mutableStateOf(initialLiveSpecimen) }
  var isFlashActive by remember { mutableStateOf(false) }
  var isCapturing by remember { mutableStateOf(false) }
  var cameraLensFacing by remember { mutableStateOf<Int>(CameraSelector.LENS_FACING_BACK) }

  var capturedDocumentBitmap by remember { mutableStateOf<Bitmap?>(null) }
  var capturedSelfieBitmap by remember { mutableStateOf<Bitmap?>(null) }

  // Check camera permission state
  var hasCameraPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
      ) == PackageManager.PERMISSION_GRANTED
    )
  }

  // Camera permission launcher
  val cameraPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    hasCameraPermission = isGranted
    if (!isGranted) {
      Toast.makeText(context, "Camera permission required for live scanning", Toast.LENGTH_SHORT).show()
    }
  }

  // Auto request permission on entering screen if in camera mode
  LaunchedEffect(Unit) {
    if (!hasCameraPermission) {
      cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }
  }

  // Photo Picker launcher for zero-permission document image upload
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      try {
        val rawBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
          ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, _, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            decoder.isMutableRequired = true
          }
        } else {
          @Suppress("DEPRECATION")
          MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }

        val maxDim = 1280
        val w = rawBitmap.width
        val h = rawBitmap.height
        val scaledBitmap = if (w > maxDim || h > maxDim) {
          val scale = maxDim.toFloat() / maxOf(w, h)
          Bitmap.createScaledBitmap(rawBitmap, (w * scale).toInt(), (h * scale).toInt(), true)
        } else {
          rawBitmap
        }

        val safeBitmap = if (scaledBitmap.config == Bitmap.Config.HARDWARE || !scaledBitmap.isMutable) {
          scaledBitmap.copy(Bitmap.Config.ARGB_8888, false) ?: scaledBitmap
        } else {
          scaledBitmap
        }

        capturedDocumentBitmap = safeBitmap
        // Proceed to Face Biometric stage
        currentStep = ScanStep.FACE_SELFIE_CAPTURE
        cameraLensFacing = CameraSelector.LENS_FACING_FRONT
      } catch (e: Exception) {
        Log.e("ScanUploadScreen", "Failed to load picked image", e)
        Toast.makeText(context, "Failed to load image: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
      }
    }
  }

  val docImageCapture = remember {
    ImageCapture.Builder()
      .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
      .build()
  }

  val selfieImageCapture = remember {
    ImageCapture.Builder()
      .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
      .build()
  }

  if (currentStep == ScanStep.FACE_SELFIE_CAPTURE) {
    // Step 2: Live Face Selfie Biometric Capture Screen
    LazyColumn(
      modifier = modifier
        .fillMaxSize()
        .background(VeriBackground)
        .padding(horizontal = 24.dp),
      contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
      verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = { currentStep = ScanStep.DOCUMENT_CAPTURE },
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(VeriWhite)
              .border(1.dp, VeriBorder, CircleShape)
              .testTag("selfie_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
              contentDescription = "Back",
              tint = VeriNavy950,
              modifier = Modifier.size(18.dp)
            )
          }

          Text(
            text = "STEP 2: LIVE FACE COMPARISON",
            style = MaterialTheme.typography.labelSmall.copy(
              fontFamily = FontFamily.Monospace,
              letterSpacing = 1.2.sp,
              fontWeight = FontWeight.Bold,
              color = VeriNavy600
            )
          )

          IconButton(
            onClick = {
              cameraLensFacing = if (cameraLensFacing == CameraSelector.LENS_FACING_FRONT) {
                CameraSelector.LENS_FACING_BACK
              } else {
                CameraSelector.LENS_FACING_FRONT
              }
            },
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(VeriWhite)
              .border(1.dp, VeriBorder, CircleShape)
              .testTag("flip_camera_button")
          ) {
            Icon(
              imageVector = Icons.Outlined.Cameraswitch,
              contentDescription = "Flip Camera",
              tint = VeriNavy950,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      item {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = "Live Face Verification",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              color = VeriNavy950
            )
          )
          Text(
            text = "Take a live camera photo of the person presenting the document. The biometric service will compare the facial geometry against the photo in the document.",
            style = MaterialTheme.typography.bodySmall.copy(
              color = VeriNavy600,
              lineHeight = 18.sp
            )
          )
        }
      }

      // Camera Selfie Viewfinder with Oval Guide
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(VeriNavy950)
            .border(1.dp, VeriBorder, RoundedCornerShape(20.dp)),
          contentAlignment = Alignment.Center
        ) {
          if (hasCameraPermission) {
            CameraCaptureView(
              imageCapture = selfieImageCapture,
              isTorchEnabled = isFlashActive,
              cameraLensFacing = cameraLensFacing,
              modifier = Modifier.fillMaxSize()
            )
          } else {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Outlined.PhotoCamera,
                contentDescription = null,
                tint = VeriWhite,
                modifier = Modifier.size(36.dp)
              )
              Text("Camera Offline", color = VeriWhite, fontSize = 13.sp)
            }
          }

          // Oval Biometric Face Reticle
          Box(
            modifier = Modifier
              .size(width = 170.dp, height = 220.dp)
              .clip(RoundedCornerShape(90.dp))
              .border(2.dp, VeriAccent.copy(alpha = 0.8f), RoundedCornerShape(90.dp))
          )

          // Center Reticle Prompt
          Box(
            modifier = Modifier
              .align(Alignment.BottomCenter)
              .padding(bottom = 12.dp)
              .clip(RoundedCornerShape(20.dp))
              .background(VeriNavy950.copy(alpha = 0.85f))
              .padding(horizontal = 14.dp, vertical = 6.dp)
          ) {
            Text(
              text = if (capturedSelfieBitmap != null) "✓ Selfie Acquired" else "Position face within oval",
              color = if (capturedSelfieBitmap != null) VeriAccent else VeriWhite,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Medium
            )
          }
        }
      }

      // Action Buttons
      item {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Button(
            onClick = {
              if (hasCameraPermission) {
                isCapturing = true
                capturePhotoFromCamera(
                  context = context,
                  imageCapture = selfieImageCapture,
                  onSuccess = { selfieBitmap ->
                    isCapturing = false
                    capturedSelfieBitmap = selfieBitmap
                    onProceedToVerification(selectedSpecimen, capturedDocumentBitmap, selfieBitmap)
                  },
                  onError = {
                    isCapturing = false
                    // Fallback to verification with specimen biometric
                    onProceedToVerification(selectedSpecimen, capturedDocumentBitmap, null)
                  }
                )
              } else {
                onProceedToVerification(selectedSpecimen, capturedDocumentBitmap, null)
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(54.dp)
              .testTag("snap_selfie_button"),
            enabled = !isCapturing,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = VeriNavy950,
              contentColor = VeriWhite
            )
          ) {
            if (isCapturing) {
              CircularProgressIndicator(
                color = VeriWhite,
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
              )
            } else {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(
                  imageVector = Icons.Outlined.Person,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp)
                )
                Text(
                  text = "Capture Face & Run Verification",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
              }
            }
          }

          OutlinedButton(
            onClick = {
              // Proceed using specimen baseline biometric portrait
              onProceedToVerification(selectedSpecimen, capturedDocumentBitmap, null)
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("use_specimen_face_button"),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, VeriBorder),
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = VeriWhite,
              contentColor = VeriNavy950
            )
          ) {
            Text(
              text = "Use Document Photo Only (Skip Live Camera)",
              style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                color = VeriNavy800
              )
            )
          }
        }
      }
    }
    return
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(VeriBackground)
      .padding(horizontal = 24.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
    verticalArrangement = Arrangement.spacedBy(18.dp)
  ) {
    // 1. Navigation & Optical Controls Header
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
            .testTag("scan_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
            contentDescription = "Back",
            tint = VeriNavy950,
            modifier = Modifier.size(18.dp)
          )
        }

        Text(
          text = "DOCUMENT CAPTURE",
          style = MaterialTheme.typography.labelSmall.copy(
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.2.sp,
            fontWeight = FontWeight.Bold,
            color = VeriNavy600
          )
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          // Switch between Live Camera and Specimen simulation
          IconButton(
            onClick = {
              captureMode = if (captureMode == CaptureMode.CAMERA) CaptureMode.SPECIMEN else CaptureMode.CAMERA
            },
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(VeriWhite)
              .border(1.dp, VeriBorder, CircleShape)
              .testTag("mode_toggle_button")
          ) {
            Icon(
              imageVector = Icons.Outlined.Tune,
              contentDescription = "Toggle Mode",
              tint = VeriNavy950,
              modifier = Modifier.size(18.dp)
            )
          }

          IconButton(
            onClick = { isFlashActive = !isFlashActive },
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(if (isFlashActive) VeriNavy950 else VeriWhite)
              .border(1.dp, VeriBorder, CircleShape)
              .testTag("flash_toggle_button")
          ) {
            Icon(
              imageVector = Icons.Outlined.FlashOn,
              contentDescription = "Flash Toggle",
              tint = if (isFlashActive) VeriWhite else VeriNavy950,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }
    }

    // 2. Optical Document Frame Viewfinder (Camera or Specimen)
    item {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        if (captureMode == CaptureMode.CAMERA && hasCameraPermission) {
          DocumentFrameView(
            specimen = null,
            isScanning = false,
            cameraContent = {
              CameraCaptureView(
                imageCapture = docImageCapture,
                isTorchEnabled = isFlashActive,
                cameraLensFacing = CameraSelector.LENS_FACING_BACK,
                modifier = Modifier.fillMaxSize()
              )
            }
          )
        } else if (captureMode == CaptureMode.CAMERA && !hasCameraPermission) {
          // Camera Permission Prompt inside Frame
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(220.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(VeriWhite)
              .border(1.dp, VeriBorder, RoundedCornerShape(16.dp))
              .padding(20.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Icon(
                imageVector = Icons.Outlined.PhotoCamera,
                contentDescription = null,
                tint = VeriNavy600,
                modifier = Modifier.size(36.dp)
              )
              Text(
                text = "Camera Permission Required",
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.SemiBold,
                  color = VeriNavy950
                )
              )
              Text(
                text = "Grant camera access to enable real-time document frame scanning and edge calibration.",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = VeriNavy600,
                  fontSize = 12.sp,
                  lineHeight = 16.sp
                ),
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
              Button(
                onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                colors = ButtonDefaults.buttonColors(containerColor = VeriNavy950),
                shape = RoundedCornerShape(10.dp)
              ) {
                Text("Enable Camera", fontSize = 12.sp, color = VeriWhite)
              }
            }
          }
        } else {
          DocumentFrameView(
            specimen = selectedSpecimen,
            isScanning = false
          )
        }

        // Alignment Status Bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(VeriWhite)
            .border(1.dp, VeriBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              imageVector = Icons.Outlined.Check,
              contentDescription = null,
              tint = VeriAccent,
              modifier = Modifier.size(14.dp)
            )
            Text(
              text = if (captureMode == CaptureMode.CAMERA && hasCameraPermission) {
                "Live Camera Active • Center Document"
              } else if (captureMode == CaptureMode.CAMERA) {
                "Camera Offline • Standby Mode"
              } else {
                "Simulated Specimen Frame (ISO 7810)"
              },
              style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = VeriNavy800
              )
            )
          }

          Text(
            text = if (captureMode == CaptureMode.CAMERA) "OPTICAL SENSOR" else "SPECIMEN TEST",
            style = MaterialTheme.typography.labelSmall.copy(
              fontFamily = FontFamily.Monospace,
              fontSize = 9.sp,
              color = VeriNavy400
            )
          )
        }
      }
    }

    // 3. Primary Actions: Capture vs Upload
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(
          onClick = {
            if (captureMode == CaptureMode.CAMERA && hasCameraPermission) {
              isCapturing = true
              capturePhotoFromCamera(
                context = context,
                imageCapture = docImageCapture,
                onSuccess = { capturedBitmap ->
                  isCapturing = false
                  capturedDocumentBitmap = capturedBitmap
                  currentStep = ScanStep.FACE_SELFIE_CAPTURE
                  cameraLensFacing = CameraSelector.LENS_FACING_FRONT
                },
                onError = { exc ->
                  isCapturing = false
                  Log.e("ScanUploadScreen", "Capture failed, proceeding to Step 2 with target specimen", exc)
                  capturedDocumentBitmap = null
                  currentStep = ScanStep.FACE_SELFIE_CAPTURE
                  cameraLensFacing = CameraSelector.LENS_FACING_FRONT
                }
              )
            } else {
              capturedDocumentBitmap = null
              currentStep = ScanStep.FACE_SELFIE_CAPTURE
              cameraLensFacing = CameraSelector.LENS_FACING_FRONT
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .testTag("capture_analyze_button"),
          enabled = !isCapturing,
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = VeriNavy950,
            contentColor = VeriWhite
          ),
          elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
          if (isCapturing) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              CircularProgressIndicator(
                color = VeriWhite,
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp
              )
              Text("Capturing Sensor Frame...", color = VeriWhite)
            }
          } else {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Outlined.PhotoCamera,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
              )
              Text(
                text = if (captureMode == CaptureMode.CAMERA && hasCameraPermission) {
                  "Capture & Screen Document"
                } else {
                  "Screen Selected Document"
                },
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.SemiBold,
                  color = VeriWhite
                )
              )
            }
          }
        }

        OutlinedButton(
          onClick = {
            photoPickerLauncher.launch(
              PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("upload_image_button"),
          shape = RoundedCornerShape(14.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, VeriBorder),
          colors = ButtonDefaults.outlinedButtonColors(
            containerColor = VeriWhite,
            contentColor = VeriNavy950
          )
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Outlined.FileUpload,
              contentDescription = null,
              tint = VeriNavy950,
              modifier = Modifier.size(18.dp)
            )
            Text(
              text = "Upload Photo from Gallery",
              style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                color = VeriNavy950
              )
            )
          }
        }
      }
    }

    // 4. Live Optical Acquisition & Database Sync Guidance
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .background(VeriWhite)
          .border(1.dp, VeriBorder, RoundedCornerShape(16.dp))
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
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
              modifier = Modifier.size(18.dp)
            )
            Text(
              text = "LIVE DATA EXTRACTION & POSTGRESQL",
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                letterSpacing = 1.0.sp,
                fontWeight = FontWeight.Bold,
                color = VeriNavy950
              )
            )
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(Color(0xFFECFDF5))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = "DB ACTIVE",
              style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = RiskLow
              )
            )
          }
        }

        Text(
          text = "Every document photo captured or uploaded is parsed via multi-spectral OCR. Real-time details (Name, Document Number, Expiry, Type) are stored directly into PostgreSQL. If an officer rejects the identity, the user is permanently recorded into the PostgreSQL blocklist table.",
          style = MaterialTheme.typography.bodySmall.copy(
            fontSize = 11.sp,
            lineHeight = 16.sp,
            color = VeriNavy600
          )
        )
      }
    }
  }
}
