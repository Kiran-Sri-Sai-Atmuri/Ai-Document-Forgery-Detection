package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun CameraCaptureView(
  imageCapture: ImageCapture,
  isTorchEnabled: Boolean,
  cameraLensFacing: Int = CameraSelector.LENS_FACING_BACK,
  modifier: Modifier = Modifier,
  onCameraReady: () -> Unit = {}
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  var cameraInstance by remember { mutableStateOf<Camera?>(null) }
  var cameraError by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(isTorchEnabled, cameraInstance) {
    cameraInstance?.let { cam ->
      if (cam.cameraInfo.hasFlashUnit()) {
        cam.cameraControl.enableTorch(isTorchEnabled)
      }
    }
  }

  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    if (cameraError != null) {
      Text(
        text = cameraError ?: "Camera not available",
        color = Color.White
      )
    } else {
      AndroidView(
        factory = { ctx ->
          val previewView = PreviewView(ctx).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
          }

          val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
          cameraProviderFuture.addListener({
            try {
              val cameraProvider = cameraProviderFuture.get()
              val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
              }

              val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(cameraLensFacing)
                .build()

              cameraProvider.unbindAll()
              val camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
              )
              cameraInstance = camera
              onCameraReady()
            } catch (exc: Exception) {
              Log.e("CameraCaptureView", "Use case binding failed", exc)
              cameraError = "Camera sensor init: ${exc.localizedMessage}"
            }
          }, ContextCompat.getMainExecutor(ctx))

          previewView
        },
        update = { previewView ->
          // When cameraLensFacing changes, rebind
          val cameraProviderFuture = ProcessCameraProvider.getInstance(previewView.context)
          cameraProviderFuture.addListener({
            try {
              val cameraProvider = cameraProviderFuture.get()
              val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
              }
              val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(cameraLensFacing)
                .build()
              cameraProvider.unbindAll()
              val camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
              )
              cameraInstance = camera
            } catch (exc: Exception) {
              Log.e("CameraCaptureView", "Rebind camera failed", exc)
            }
          }, ContextCompat.getMainExecutor(previewView.context))
        },
        modifier = Modifier.fillMaxSize()
      )
    }
  }
}

fun capturePhotoFromCamera(
  context: Context,
  imageCapture: ImageCapture,
  onSuccess: (Bitmap) -> Unit,
  onError: (Exception) -> Unit
) {
  val executor = ContextCompat.getMainExecutor(context)
  imageCapture.takePicture(
    executor,
    object : ImageCapture.OnImageCapturedCallback() {
      override fun onCaptureSuccess(imageProxy: ImageProxy) {
        try {
          val rawBitmap = imageProxy.toBitmap()
          val rotation = imageProxy.imageInfo.rotationDegrees
          val rotatedBitmap = if (rotation != 0) {
            val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
            Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
          } else {
            rawBitmap
          }
          imageProxy.close()

          // Downscale to max 1280px and ensure software ARGB_8888 bitmap to prevent OOM and Config.HARDWARE getPixel crash
          val maxDim = 1280
          val w = rotatedBitmap.width
          val h = rotatedBitmap.height
          val scaledBitmap = if (w > maxDim || h > maxDim) {
            val scale = maxDim.toFloat() / maxOf(w, h)
            Bitmap.createScaledBitmap(rotatedBitmap, (w * scale).toInt(), (h * scale).toInt(), true)
          } else {
            rotatedBitmap
          }

          val softwareBitmap = if (scaledBitmap.config == Bitmap.Config.HARDWARE || !scaledBitmap.isMutable) {
            scaledBitmap.copy(Bitmap.Config.ARGB_8888, false) ?: scaledBitmap
          } else {
            scaledBitmap
          }

          onSuccess(softwareBitmap)
        } catch (e: Exception) {
          imageProxy.close()
          onError(e)
        }
      }

      override fun onError(exception: ImageCaptureException) {
        onError(exception)
      }
    }
  )
}
