package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.backend.GeminiVerificationBackend
import com.example.data.local.AppDatabase
import com.example.data.repository.DocumentRepository
import com.example.model.DocumentSpecimen
import com.example.model.SampleData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

  private val database = AppDatabase.getDatabase(application)
  private val repository = DocumentRepository(database.documentDao(), database.blocklistDao())

  val verifiedDocuments: StateFlow<List<DocumentSpecimen>> = repository.allDocuments
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  val blocklistEntries = database.blocklistDao().getAllBlocklistEntries()
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  val postgresUsers = com.example.data.postgres.PostgresDatabaseManager.verifiedUsers
  val postgresBlocklist = com.example.data.postgres.PostgresDatabaseManager.blocklistRecords

  private val _selectedSpecimen = MutableStateFlow<DocumentSpecimen>(SampleData.genuineDriversLicense)
  val selectedSpecimen: StateFlow<DocumentSpecimen> = _selectedSpecimen.asStateFlow()

  private val _capturedBitmap = MutableStateFlow<Bitmap?>(null)
  val capturedBitmap: StateFlow<Bitmap?> = _capturedBitmap.asStateFlow()

  private val _cameraSelfieBitmap = MutableStateFlow<Bitmap?>(null)
  val cameraSelfieBitmap: StateFlow<Bitmap?> = _cameraSelfieBitmap.asStateFlow()

  private val _isVerifying = MutableStateFlow(false)
  val isVerifying: StateFlow<Boolean> = _isVerifying.asStateFlow()

  init {
    viewModelScope.launch {
      // Purge any sample/dummy data so only real user extractions exist in database
      repository.removeDummyData()
    }
  }

  fun setCapturedBitmap(bitmap: Bitmap?) {
    _capturedBitmap.value = bitmap
  }

  fun setCameraSelfieBitmap(bitmap: Bitmap?) {
    _cameraSelfieBitmap.value = bitmap
  }

  fun setSelectedSpecimen(specimen: DocumentSpecimen) {
    _selectedSpecimen.value = specimen
    _capturedBitmap.value = null
    _cameraSelfieBitmap.value = null
  }

  /**
   * Persists the already-verified document directly to Room database and PostgreSQL
   * without re-executing the entire verification pipeline, preventing duplicate processing and crashes.
   */
  fun saveVerifiedDocument(
    verifiedSpecimen: DocumentSpecimen,
    onCompleted: (DocumentSpecimen) -> Unit
  ) {
    viewModelScope.launch {
      try {
        repository.insertDocument(verifiedSpecimen)
        _selectedSpecimen.value = verifiedSpecimen
        onCompleted(verifiedSpecimen)
      } catch (e: Exception) {
        android.util.Log.e("MainViewModel", "Failed to save verified document", e)
        _selectedSpecimen.value = verifiedSpecimen
        onCompleted(verifiedSpecimen)
      }
    }
  }

  fun runVerificationPipeline(
    fallbackSpecimen: DocumentSpecimen,
    documentBitmap: Bitmap?,
    selfieBitmap: Bitmap?,
    onCompleted: (DocumentSpecimen) -> Unit
  ) {
    viewModelScope.launch {
      _isVerifying.value = true
      try {
        val extraBlocklist = repository.getAllBlocklistEntries()
        val verifiedResult = com.example.data.services.VerificationPipelineCoordinator.executePipeline(
          initialSpecimen = fallbackSpecimen,
          documentBitmap = documentBitmap,
          cameraSelfieBitmap = selfieBitmap,
          extraDbEntries = extraBlocklist
        )

        // Persist in real-time to Room and PostgreSQL
        repository.insertDocument(verifiedResult)
        _selectedSpecimen.value = verifiedResult
        _isVerifying.value = false
        onCompleted(verifiedResult)
      } catch (e: Exception) {
        android.util.Log.e("MainViewModel", "Pipeline execution error in ViewModel", e)
        _selectedSpecimen.value = fallbackSpecimen
        _isVerifying.value = false
        onCompleted(fallbackSpecimen)
      }
    }
  }

  fun recordOfficerDecision(
    documentId: String,
    decision: com.example.model.OfficerDecisionStatus,
    notes: String
  ) {
    viewModelScope.launch {
      val updated = repository.updateOfficerDecision(documentId, decision, notes)
      if (updated != null && _selectedSpecimen.value.id == documentId) {
        _selectedSpecimen.value = updated
      }
    }
  }

  fun deleteDocument(id: String) {
    viewModelScope.launch {
      repository.deleteDocument(id)
    }
  }
}
