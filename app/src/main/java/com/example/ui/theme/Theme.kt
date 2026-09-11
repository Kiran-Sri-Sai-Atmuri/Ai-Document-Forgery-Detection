package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val VeriTrustColorScheme = lightColorScheme(
  primary = VeriNavy950,
  onPrimary = VeriWhite,
  primaryContainer = VeriAccentLight,
  onPrimaryContainer = VeriAccent,
  secondary = VeriNavy800,
  onSecondary = VeriWhite,
  secondaryContainer = VeriSurfaceVariant,
  onSecondaryContainer = VeriNavy950,
  tertiary = VeriAccent,
  onTertiary = VeriWhite,
  background = VeriBackground,
  onBackground = VeriNavy950,
  surface = VeriCardBackground,
  onSurface = VeriNavy950,
  surfaceVariant = VeriSurfaceVariant,
  onSurfaceVariant = VeriNavy800,
  outline = VeriBorder,
  outlineVariant = VeriBorderSubtle
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = VeriTrustColorScheme,
    typography = Typography,
    content = content
  )
}
