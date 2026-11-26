package com.example.safeqr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SafeQrViewModel @Inject constructor(
    private val medicalRecordDao: MedicalRecordDao,
    private val userDao: UserDao
) : ViewModel() {

    private val _records = MutableStateFlow<List<MedicalRecord>>(emptyList())
    val records: StateFlow<List<MedicalRecord>> = _records.asStateFlow()

    init {
        loadRecords()
    }

    fun loadRecords() {
        viewModelScope.launch {
            _records.value = medicalRecordDao.getAllRecords()
        }
    }

    fun addRecord(record: MedicalRecord) {
        viewModelScope.launch {
            medicalRecordDao.insertRecord(record)
            loadRecords()
        }
    }

    fun deleteRecord(record: MedicalRecord) {
        viewModelScope.launch {
            medicalRecordDao.deleteRecord(record)
            loadRecords()
        }
    }
}