package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testSpecimensValidity() {
    val specimens = com.example.model.SampleData.allSpecimens
    assertEquals(3, specimens.size)

    val genuine = com.example.model.SampleData.genuineDriversLicense
    assertEquals(com.example.model.RiskLevel.LOW, genuine.riskLevel)
    assertTrue(genuine.trustScore >= 80)

    val altered = com.example.model.SampleData.alteredPassport
    assertEquals(com.example.model.RiskLevel.HIGH, altered.riskLevel)
    assertTrue(altered.trustScore < 50)
    assertTrue(altered.flags.any { it.severity == com.example.model.FlagSeverity.CRITICAL })
  }
}
