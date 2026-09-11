package com.example.model

object SampleData {

  // Specimen 1: Authentic Driver's License (LOW RISK, 97% Trust Score)
  val genuineDriversLicense = DocumentSpecimen(
    id = "SPEC-US-DL-9821",
    title = "California Driver License",
    subtitle = "REAL ID Compliant • Issued 2024",
    documentType = DocumentType.DRIVERS_LICENSE,
    holderName = "ELENA ROSTOVA",
    documentNumber = "D8492019",
    dateOfBirth = "14 NOV 1991",
    expiryDate = "14 NOV 2029",
    countryCode = "USA",
    trustScore = 97,
    riskLevel = RiskLevel.LOW,
    dna = DocumentDna(
      dnaHash = "7F1A·4C82·B901·2E88",
      seedValue = 0x7F1A4C82L,
      arcFrequencies = listOf(0.92f, 0.88f, 0.95f, 0.91f, 0.89f, 0.94f, 0.97f),
      nodeCount = 18,
      entropyMetric = 0.962f,
      structuralParity = "ECC-256 VALIDATED"
    ),
    flags = listOf(
      ForensicFlag(
        id = "FLAG-101",
        category = "Guilloche Micro-pattern",
        severity = FlagSeverity.PASS,
        title = "Continuous Security Lines",
        reason = "Micro-wave security lathe lines show 0.03mm precision with continuous vector geometry and zero digital rasterization artifacts.",
        technicalEvidence = "Sub-pixel spline variance < 0.012 px; spectral resolution matched to state treasury standards.",
        region = ForensicRegion(0.08f, 0.15f, 0.35f, 0.25f, "Security Grid"),
        confidenceScore = 0.994f
      ),
      ForensicFlag(
        id = "FLAG-102",
        category = "Facial Biometrics & Liveness",
        severity = FlagSeverity.PASS,
        title = "Natural Depth Gradient",
        reason = "Facial portrait shows genuine 3D biometric shadow falloff, laser-engraved micro-perforations, and ghost image congruence.",
        technicalEvidence = "Secondary ghost image embedding cosine similarity is 0.968 relative to primary portrait.",
        region = ForensicRegion(0.68f, 0.22f, 0.24f, 0.42f, "Ghost Portrait"),
        confidenceScore = 0.988f
      ),
      ForensicFlag(
        id = "FLAG-103",
        category = "Typography & Standards",
        severity = FlagSeverity.PASS,
        title = "Compliant OCR-B & State Glyphs",
        reason = "Character kerning, ink bleed morphology, and stroke weight match authentic state bureau templates exactly.",
        technicalEvidence = "Typeface stem kerning deviation 0.00% across all 24 alphanumeric fields.",
        region = ForensicRegion(0.08f, 0.52f, 0.55f, 0.30f, "Alphanumeric Fields"),
        confidenceScore = 0.991f
      )
    ),
    authenticityIndex = 98,
    biometricCoherence = 96,
    tamperResistance = 99,
    dataConsistency = 95,
    auditDigest = "SHA256: 4f8a92b...e019c4",
    investigationSummary = "Specimen exhibits no forensic discrepancies. Optical security patterns, ghost portrait alignment, and tactile laser engraving meet all federal REAL ID screening specifications."
  )

  // Specimen 2: Altered Passport with Font Tampering (HIGH RISK, 34% Trust Score)
  val alteredPassport = DocumentSpecimen(
    id = "SPEC-EU-PASS-4410",
    title = "Federal Republic Passport",
    subtitle = "Altered Expiry & Forged Typeface",
    documentType = DocumentType.PASSPORT,
    holderName = "MAXIMILIAN VANCE",
    documentNumber = "C01X9842M",
    dateOfBirth = "03 MAR 1988",
    expiryDate = "03 MAR 2031",
    countryCode = "DEU",
    trustScore = 34,
    riskLevel = RiskLevel.HIGH,
    dna = DocumentDna(
      dnaHash = "3B9E·11A4·F900·81C2",
      seedValue = 0x3B9E11A4L,
      arcFrequencies = listOf(0.34f, 0.41f, 0.29f, 0.85f, 0.38f, 0.42f, 0.31f),
      nodeCount = 9,
      entropyMetric = 0.385f,
      structuralParity = "PARITY BREACH DETECTED"
    ),
    flags = listOf(
      ForensicFlag(
        id = "FLAG-201",
        category = "Typography & Glyph Forensics",
        severity = FlagSeverity.CRITICAL,
        title = "Inconsistent Font Stem Weight in Expiry Date",
        reason = "The expiration numeral '2031' was rendered in an Arial variant rather than official OCR-B. Ink diffusion shows digital toner overlay instead of intaglio press printing.",
        technicalEvidence = "Stem thickness variance: +23.4% above standard threshold. Edge feathering gradient exhibits 150 DPI desktop printer raster artifacts.",
        region = ForensicRegion(0.48f, 0.42f, 0.32f, 0.12f, "Forged Expiry Date"),
        confidenceScore = 0.998f
      ),
      ForensicFlag(
        id = "FLAG-202",
        category = "ICAO 9303 Checksum",
        severity = FlagSeverity.CRITICAL,
        title = "Machine Readable Zone (MRZ) Parity Failure",
        reason = "The check digit computed for line 2 does not match the printed optical number 'C01X9842M'. Mathematical verification failed mod-7 check.",
        technicalEvidence = "Computed check digit: 7 | Printed check digit: 2. Delta breach indicates manual alphanumeric alteration.",
        region = ForensicRegion(0.05f, 0.78f, 0.90f, 0.18f, "MRZ Lines 1 & 2"),
        confidenceScore = 1.000f
      ),
      ForensicFlag(
        id = "FLAG-203",
        category = "Micro-print & Security Thread",
        severity = FlagSeverity.WARNING,
        title = "Discontinuous Guilloche Waveform",
        reason = "The rainbow background pattern abruptly truncates behind the date box, revealing evidence of image cloning/healing tool manipulation.",
        technicalEvidence = "Laplacian edge variance peak at boundary [x:482, y:391]. Background texture frequency interrupted.",
        region = ForensicRegion(0.44f, 0.38f, 0.40f, 0.20f, "Texture Infill Zone"),
        confidenceScore = 0.965f
      )
    ),
    authenticityIndex = 28,
    biometricCoherence = 54,
    tamperResistance = 19,
    dataConsistency = 35,
    auditDigest = "SHA256: a910cb4...719ef2",
    investigationSummary = "High-confidence forgery detected. Mathematical checksum invalidation and digital font tampering across expiration fields confirm intentional physical credential manipulation."
  )

  // Specimen 3: National Identity Card with Replay / Screen Capture Artifacts (MEDIUM RISK, 62% Trust Score)
  val nationalIdReplay = DocumentSpecimen(
    id = "SPEC-SG-NID-7104",
    title = "National Identity Card",
    subtitle = "Replay Screen Glare & Hologram Anomaly",
    documentType = DocumentType.NATIONAL_ID,
    holderName = "SARAH LIN WEI",
    documentNumber = "S9382104G",
    dateOfBirth = "22 AUG 1995",
    expiryDate = "PERMANENT",
    countryCode = "SGP",
    trustScore = 62,
    riskLevel = RiskLevel.MEDIUM,
    dna = DocumentDna(
      dnaHash = "5E22·90FD·6A13·48B9",
      seedValue = 0x5E2290FDL,
      arcFrequencies = listOf(0.65f, 0.58f, 0.72f, 0.60f, 0.59f, 0.68f, 0.61f),
      nodeCount = 13,
      entropyMetric = 0.640f,
      structuralParity = "PARTIAL PARITY (SUSPECT)"
    ),
    flags = listOf(
      ForensicFlag(
        id = "FLAG-301",
        category = "Optically Variable Ink (OVI)",
        severity = FlagSeverity.WARNING,
        title = "Static Specular Reflection / Lack of Hologram Iridescence",
        reason = "Hologram patch lacks dynamic optical shift under variable lighting angles, indicating a flat photographic reprint or high-res monitor display playback.",
        technicalEvidence = "Chromatic aberration dispersion score 0.42 (Expected > 0.85 for micro-embossed kinegram).",
        region = ForensicRegion(0.55f, 0.12f, 0.32f, 0.28f, "Holographic Kinegram"),
        confidenceScore = 0.912f
      ),
      ForensicFlag(
        id = "FLAG-302",
        category = "Sensor & Moire Pattern",
        severity = FlagSeverity.WARNING,
        title = "Sub-pixel Screen Moire Lattice",
        reason = "High-frequency periodic interference grid detected across the card surface, typical of capturing a secondary electronic OLED screen.",
        technicalEvidence = "2D Fast Fourier Transform (FFT) reveals discrete 60Hz pixel pitch harmonics in green channel.",
        region = ForensicRegion(0.12f, 0.30f, 0.45f, 0.35f, "Moire Interference"),
        confidenceScore = 0.884f
      ),
      ForensicFlag(
        id = "FLAG-303",
        category = "Data Consistency",
        severity = FlagSeverity.PASS,
        title = "Alphanumeric Field Cross-Check",
        reason = "Name, birthdate, and registration serial numbers correspond to valid format patterns and logical checksums.",
        technicalEvidence = "Card serial formula valid. Zero checksum failures detected in plaintext metadata.",
        region = ForensicRegion(0.08f, 0.65f, 0.80f, 0.22f, "Identity Fields"),
        confidenceScore = 0.970f
      )
    ),
    authenticityIndex = 58,
    biometricCoherence = 65,
    tamperResistance = 60,
    dataConsistency = 82,
    auditDigest = "SHA256: d83b771...335ca9",
    investigationSummary = "Suspicious presentation attack. Document metadata is syntactically coherent, but surface optical characteristics strongly suggest screen replay or color reprint rather than a physical original card."
  )

  val allSpecimens = listOf(genuineDriversLicense, alteredPassport, nationalIdReplay)
}
