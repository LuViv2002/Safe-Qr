package com.example.safeqr

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// Actividad principal de la aplicación SAFE QR (hereda de ComponentActivity para usar Compose)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Permite dibujar contenido de borde a borde en la pantalla
        enableEdgeToEdge()
        // Define la interfaz de usuario con Jetpack Compose
        setContent {
            MaterialTheme {
                // Scaffold proporciona la estructura base de Material 3 (respetando márgenes del sistema)
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("main_scaffold")
                ) { innerPadding ->
                    // Llama a la pantalla principal pasando el padding interno del Scaffold
                    MainScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

// Composable que representa el menú principal de la aplicación
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    // Callback opcional para navegar al registro (si es null, abre ChildRegisterActivity con Intent)
    onNavigateToRegister: (() -> Unit)? = null,
    // Callback opcional para navegar a perfiles (si es null, abre MedicalProfileActivity con Intent)
    onNavigateToProfiles: (() -> Unit)? = null
) {
    // Obtiene el contexto actual para lanzar actividades o mostrar avisos
    val context = LocalContext.current

    // Contenedor vertical centrado con espacio interno
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .testTag("main_menu_container"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título principal de la app
        Text(
            text = "SAFE QR",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("label_app_title")
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Subtítulo descriptivo del propósito de la app
        Text(
            text = "Información médica y códigos QR de emergencia",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.testTag("label_app_subtitle")
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Tarjeta informativa de acceso rápido
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_quick_access")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Acceso rápido",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.testTag("label_card_header")
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Registra datos médicos infantiles o consulta los códigos QR generados.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("label_card_description")
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón principal para ir a la pantalla de registro médico infantil
        Button(
            onClick = {
                if (onNavigateToRegister != null) {
                    onNavigateToRegister()
                } else {
                    context.startActivity(Intent(context, ChildRegisterActivity::class.java))
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_go_register")
        ) {
            Text("Registrar perfil médico")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Botón secundario para ir a la pantalla de perfiles guardados y códigos QR
        OutlinedButton(
            onClick = {
                if (onNavigateToProfiles != null) {
                    onNavigateToProfiles()
                } else {
                    context.startActivity(Intent(context, MedicalProfileActivity::class.java))
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_go_profiles")
        ) {
            Text("Ver perfiles y QR")
        }
    }
}

// Vista previa en Android Studio para revisar el diseño sin ejecutar la app
@Preview(showBackground = true, name = "MainScreen Preview")
@Composable
fun MainScreenPreview() {
    MaterialTheme {
        MainScreen()
    }
}