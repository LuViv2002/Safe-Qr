package com.example.safeqr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

// Estados posibles del inicio de sesión / registro
sealed interface AuthState {
    data object Idle : AuthState
    data object Loading : AuthState
    data class Success(val user: User) : AuthState
    data class Error(val message: String) : AuthState
}

@HiltViewModel
class SafeQrViewModel @Inject constructor(
    private val medicalRecordDao: MedicalRecordDao,
    private val userDao: UserDao
) : ViewModel() {

    // ---------- Perfiles médicos ----------
    private val _records = MutableStateFlow<List<MedicalRecord>>(emptyList())
    val records: StateFlow<List<MedicalRecord>> = _records.asStateFlow()

    // Perfil seleccionado para QrDisplayActivity
    private val _selectedRecord = MutableStateFlow<MedicalRecord?>(null)
    val selectedRecord: StateFlow<MedicalRecord?> = _selectedRecord.asStateFlow()

    // ---------- Autenticación ----------
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        loadRecords()
    }

    fun loadRecords() {
        viewModelScope.launch {
            _records.value = medicalRecordDao.getAllRecords()
        }
    }

    fun loadRecord(id: Int) {
        viewModelScope.launch {
            _selectedRecord.value = medicalRecordDao.getRecordById(id)
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

    // Crea una cuenta nueva guardando solo el hash de la contraseña
    fun register(email: String, password: String) {
        val cleanEmail = email.trim().lowercase()
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            _authState.value = AuthState.Error("Correo electrónico no válido")
            return
        }
        if (password.length < 6) {
            _authState.value = AuthState.Error("La contraseña debe tener al menos 6 caracteres")
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            if (userDao.findByEmail(cleanEmail) != null) {
                _authState.value = AuthState.Error("Ya existe una cuenta con ese correo")
                return@launch
            }
            // PBKDF2 es costoso a propósito: se calcula fuera del hilo principal
            val salt = PasswordHasher.newSalt()
            val hash = withContext(Dispatchers.Default) { PasswordHasher.hash(password, salt) }
            val user = User(
                name = cleanEmail.substringBefore("@"),
                email = cleanEmail,
                password = hash,
                salt = salt
            )
            val newId = userDao.insertUser(user)
            _authState.value = AuthState.Success(user.copy(id = newId.toInt()))
        }
    }

    // Inicia sesión comparando el hash de la contraseña escrita con el guardado
    fun login(email: String, password: String) {
        val cleanEmail = email.trim().lowercase()
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val user = userDao.findByEmail(cleanEmail)
            val ok = user != null && withContext(Dispatchers.Default) {
                PasswordHasher.verify(password, user.salt, user.password)
            }
            _authState.value = if (ok) {
                AuthState.Success(user!!)
            } else {
                // Mismo mensaje en ambos casos para no revelar si el correo existe
                AuthState.Error("Correo o contraseña incorrectos")
            }
        }
    }

    fun clearAuthError() {
        if (_authState.value is AuthState.Error) _authState.value = AuthState.Idle
    }
}
