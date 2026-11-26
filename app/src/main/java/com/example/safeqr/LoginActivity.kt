package com.example.safeqr

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint

// Actividad encargada del inicio de sesión (y registro de cuenta) en SAFE QR
@AndroidEntryPoint
class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Habilita diseño de borde a borde
        enableEdgeToEdge()
        setContent {
            // ViewModel inyectado por Hilt (usa UserDao por dentro)
            val viewModel: SafeQrViewModel = hiltViewModel()
            val authState by viewModel.authState.collectAsStateWithLifecycle()

            // Reacciona a los cambios de estado: éxito -> menú principal, error -> aviso
            LaunchedEffect(authState) {
                when (val state = authState) {
                    is AuthState.Success -> {
                        Toast.makeText(
                            this@LoginActivity,
                            "Bienvenido(a), ${state.user.name}",
                            Toast.LENGTH_SHORT
                        ).show()
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    }
                    is AuthState.Error -> {
                        Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_SHORT).show()
                        viewModel.clearAuthError()
                    }
                    else -> Unit
                }
            }

            MaterialTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("login_scaffold")
                ) { innerPadding ->
                    LoginScreen(
                        modifier = Modifier.padding(innerPadding),
                        isLoading = authState is AuthState.Loading,
                        onBack = { finish() },
                        // Conexión con la base de datos a través del ViewModel
                        onAuthenticate = { email, password, isRegisterMode ->
                            if (isRegisterMode) viewModel.register(email, password)
                            else viewModel.login(email, password)
                        }
                    )
                }
            }
        }
    }
}

// Composable de inicio de sesión con soporte para alternar entre login y registro de usuario
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLoginSuccess: () -> Unit = {},
    onBack: () -> Unit = {},
    // Deshabilita el botón mientras se valida contra la base de datos
    isLoading: Boolean = false,
    // Callback opcional para validar credenciales contra base de datos / ViewModel
    onAuthenticate: ((email: String, password: String, isRegisterMode: Boolean) -> Unit)? = null
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Estados de los campos de texto y modo de pantalla (false = Iniciar sesión, true = Crear cuenta)
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp)
            .testTag("login_form_container"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título principal de la marca
        Text(
            text = "SAFE QR",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("label_login_brand")
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Subtítulo que cambia según si el usuario inicia sesión o crea una cuenta
        Text(
            text = if (isRegisterMode) {
                "Crea una cuenta para administrar perfiles médicos"
            } else {
                "Inicia sesión para acceder a tus perfiles médicos"
            },
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.testTag("label_login_subtitle")
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Campo: Correo electrónico
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_login_email")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Campo: Contraseña oculta con PasswordVisualTransformation
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_login_password")
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Botón principal de acción (Iniciar sesión o Registrar cuenta)
        Button(
            onClick = {
                val cleanEmail = email.trim()
                // La contraseña no se recorta: los espacios forman parte de ella
                val cleanPassword = password

                // Validación básica de campos vacíos
                if (cleanEmail.isEmpty() || cleanPassword.isEmpty()) {
                    Toast.makeText(
                        context,
                        "Completa el correo y la contraseña",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }

                // Si ya conectaste ViewModel / Room, delega ahí; de lo contrario muestra aviso y continúa
                if (onAuthenticate != null) {
                    onAuthenticate(cleanEmail, cleanPassword, isRegisterMode)
                } else {
                    Toast.makeText(
                        context,
                        if (isRegisterMode) "Cuenta preparada" else "Acceso concedido",
                        Toast.LENGTH_SHORT
                    ).show()
                    onLoginSuccess()
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_submit_auth")
        ) {
            Text(
                when {
                    isLoading -> "Validando..."
                    isRegisterMode -> "Crear cuenta"
                    else -> "Iniciar sesión"
                }
            )
      }

        Spacer(modifier = Modifier.height(8.dp))

        // Botón de texto para alternar entre "Iniciar sesión" y "Crear cuenta"
        TextButton(
            onClick = { isRegisterMode = !isRegisterMode },
            modifier = Modifier.testTag("btn_toggle_auth_mode")
        ) {
            Text(
                if (isRegisterMode) {
                    "¿Ya tienes cuenta? Inicia sesión"
                } else {
                    "¿No tienes cuenta? Regístrate aquí"
                }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Botón secundario para volver atrás
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_back_from_login")
        ) {
            Text("Volver")
        }
    }
}

// Vista previa de la pantalla de inicio de sesión en Android Studio
@Preview(showBackground = true, name = "LoginScreen Preview")
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreen()
    }
}
