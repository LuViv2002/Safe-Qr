package com.example.safeqr

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MedicalRecordDao {
    @Insert
    suspend fun insertRecord(record: MedicalRecord): Long

    @Query("SELECT * FROM medical_records ORDER BY id DESC")
    suspend fun getAllRecords(): List<MedicalRecord>

    // Nuevo: obtener un solo perfil para QrDisplayActivity
    @Query("SELECT * FROM medical_records WHERE id = :id LIMIT 1")
    suspend fun getRecordById(id: Int): MedicalRecord?

    @Delete
    suspend fun deleteRecord(record: MedicalRecord)
}
