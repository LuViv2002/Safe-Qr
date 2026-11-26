package com.example.safeqr

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object QrGenerator {

    fun buildEmergencyPayload(record: MedicalRecord): String {
        val tipo = if (record.profileType == "GENERAL") "Perfil médico" else "Perfil médico infantil"
        val contactos = buildList {
            if (record.emergencyContact1.isNotBlank()) add("Contacto 1: ${record.emergencyContact1}")
            if (record.emergencyContact2.isNotBlank()) add("Contacto 2: ${record.emergencyContact2}")
        }.joinToString(" | ")

        return buildString {
            appendLine("SAFE QR - EMERGENCIA")
            appendLine("Tipo: $tipo")
            appendLine("Nombre: ${record.fullName}")
            if (record.birthDate.isNotBlank()) appendLine("Nacimiento: ${record.birthDate}")
            appendLine("Tipo de sangre: ${record.bloodType}")
            appendLine("Alergias: ${record.allergies}")
            if (contactos.isNotEmpty()) append(contactos)
        }.trim()
    }

    // QR 1: datos de identificación y contacto (registro infantil)
    fun buildRegisterPayload(record: MedicalRecord): String = buildString {
        appendLine("SAFE QR - REGISTRO")
        appendLine("Nombre: ${record.fullName}")
        if (record.birthDate.isNotBlank()) appendLine("Nacimiento: ${record.birthDate}")
        appendLine("Contacto 1: ${record.emergencyContact1}")
        if (record.emergencyContact2.isNotBlank()) appendLine("Contacto 2: ${record.emergencyContact2}")
    }.trim()

    // QR 2: datos clínicos de emergencia (perfil médico)
    fun buildMedicalPayload(record: MedicalRecord): String = buildString {
        appendLine("SAFE QR - PERFIL MÉDICO")
        appendLine("Nombre: ${record.fullName}")
        appendLine("Tipo de sangre: ${record.bloodType}")
        appendLine("Alergias: ${record.allergies}")
    }.trim()

    suspend fun generateBitmap(
        content: String,
        size: Int = 512
    ): Bitmap? = withContext(Dispatchers.Default) {
        if (content.isBlank() || size <= 0) return@withContext null

        return@withContext try {
            val hints = mapOf(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.MARGIN to 1
            )
            val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (_: Exception) {
            null
        }
    }
}