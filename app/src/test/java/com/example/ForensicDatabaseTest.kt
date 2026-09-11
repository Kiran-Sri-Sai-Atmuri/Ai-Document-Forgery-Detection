package com.example

import android.content.Context
import android.graphics.Bitmap
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.backend.ForensicEngine
import com.example.data.local.AppDatabase
import com.example.data.local.DocumentEntity
import com.example.model.DocumentType
import com.example.model.SampleData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ForensicDatabaseTest {

  private lateinit var database: AppDatabase

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun testForensicEngineAnalysis() {
    val bitmap = Bitmap.createBitmap(800, 500, Bitmap.Config.ARGB_8888)
    val specimen = ForensicEngine.analyzeImage(bitmap, "Driver License")

    assertNotNull(specimen)
    assertEquals(DocumentType.DRIVERS_LICENSE, specimen.documentType)
    assertTrue(specimen.trustScore in 1..100)
    assertTrue(specimen.flags.isNotEmpty())
    assertNotNull(specimen.dna.dnaHash)
  }

  @Test
  fun testRoomEntityMappingAndStorage() = runBlocking {
    val dao = database.documentDao()
    val specimen = SampleData.genuineDriversLicense

    val entity = DocumentEntity.fromSpecimen(specimen)
    dao.insertDocument(entity)

    val stored = dao.getDocumentById(specimen.id)
    assertNotNull(stored)
    val mappedSpecimen = stored!!.toSpecimen()

    assertEquals(specimen.id, mappedSpecimen.id)
    assertEquals(specimen.trustScore, mappedSpecimen.trustScore)
    assertEquals(specimen.riskLevel, mappedSpecimen.riskLevel)
    assertEquals(specimen.flags.size, mappedSpecimen.flags.size)

    val all = dao.getAllDocuments().first()
    assertEquals(1, all.size)
  }
}
