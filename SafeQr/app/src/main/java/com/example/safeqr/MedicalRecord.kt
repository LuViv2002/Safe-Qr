package com.example.safeqr

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medical_records")
data class MedicalRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val profileType: String = "INFANTIL", // "INFANTIL" o "GENERAL"
    val fullName: String,
    val birthDate: String,
    val allergies: String,
    val bloodType: String,
    val emergencyContact1: String,
    val emergencyContact2: String = ""
)