package com.example.safeqr

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// El correo es único: no puede haber dos cuentas con el mismo email
@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String = "",
    val email: String,
    // Ya no se guarda la contraseña en texto plano: aquí va el hash PBKDF2
    val password: String,
    // Sal aleatoria (Base64) usada para generar el hash
    val salt: String = ""
)
