package com.example.safeqr

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChildRegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: SafeQrViewModel = hiltViewModel()
            val records by viewModel.records.collectAsStateWithLifecycle()

            MaterialTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("register_scaffold")
                ) { innerPadding ->
                    ChildRegisterScreen(
                        modifier = Modifier.padding(innerPadding),
                        totalRecords = records.size,
                        onBack = { finish() },
                        onSubmitForm = { tipoPerfil, nombre, fecha, alergias, sangre, contacto1, contacto2 ->
                            val newRecord = MedicalRecord(
                                profileType = tipoPerfil,
                                fullName = nombre,
                                birthDate = fecha,
                                allergies = alergias,
                                bloodType = sangre,
                                emergencyContact1 = contacto1,
                                emergencyContact2 = contacto2
                            )
                            viewModel.addRecord(newRecord)
                            newRecord
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildRegisterScreen(
    modifier: Modifier = Modifier,
    totalRecords: Int = 0,
    onBack: () -> Unit = {},
    onSubmitForm: ((
        tipoPerfil: String,
        nombre: String,
        fecha: String,
        alergias: String,
        sangre: String,
        contacto1: String,
        contacto2: String
    ) -> MedicalRecord?)? = null
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    var selectedProfileType by remember { mutableStateOf("INFANTIL") }
    var nombre by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var alergias by remember { mutableStateOf("") }
    var sangre by remember { mutableStateOf("") }
    var contacto1 by remember { mutableStateOf("") }
    var contacto2 by remember { mutableStateOf("") }
    var qrPreview by remember { mutableStateOf<Bitmap?>(null) }

    val screenTitle = if (selectedProfileType == "INFANTIL") {
        "Registro Médico Infantil"
    } else {
        "Registro Médico"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(20.dp)
            .testTag("register_form_container"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = screenTitle,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.testTag("label_register_title")
        )

        if (totalRecords > 0) {
            Text(
                text = "Perfiles guardados: $totalRecords",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Text(
            text = "Selecciona el tipo de perfil",
            style = MaterialTheme.typography.labelLarge
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedProfileType == "INFANTIL",
                onClick = { selectedProfileType = "INFANTIL" },
                label = { Text("Perfil médico infantil") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chip_profile_child")
            )
            FilterChip(
                selected = selectedProfileType == "GENERAL",
                onClick = { selectedProfileType = "GENERAL" },
                label = { Text("Perfil médico") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chip_profile_general")
            )
        }

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre completo") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_full_name")
        )

        OutlinedTextField(
            value = fecha,
            onValueChange = { fecha = it },
            label = { Text("Fecha de nacimiento") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_birth_date")
        )

        OutlinedTextField(
            value = alergias,
            onValueChange = { alergias = it },
            label = { Text("Alergias") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_allergies")
        )

        OutlinedTextField(
            value = sangre,
            onValueChange = { sangre = it },
            label = { Text("Tipo de sangre") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_blood_type")
        )

        OutlinedTextField(
            value = contacto1,
            onValueChange = { contacto1 = it },
            label = { Text("Contacto de emergencia 1") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_emergency_contact_1")
        )

        OutlinedTextField(
            value = contacto2,
            onValueChange = { contacto2 = it },
            label = { Text("Contacto de emergencia 2") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_emergency_contact_2")
        )

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = {
                val cleanNombre = nombre.trim()
                val cleanContacto1 = contacto1.trim()

                if (cleanNombre.isEmpty() || cleanContacto1.isEmpty()) {
                    Toast.makeText(
                        context,
                        "Ingresa al menos el nombre y un contacto de emergencia",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }

                val cleanFecha = fecha.trim()
                val cleanAlergias = alergias.trim().ifEmpty { "Ninguna" }
                val cleanSangre = sangre.trim().ifEmpty { "No especificado" }
                val cleanContacto2 = contacto2.trim()

                val savedRecord = onSubmitForm?.invoke(
                    selectedProfileType,
                    cleanNombre,
                    cleanFecha,
                    cleanAlergias,
                    cleanSangre,
                    cleanContacto1,
                    cleanContacto2
                ) ?: MedicalRecord(
                    profileType = selectedProfileType,
                    fullName = cleanNombre,
                    birthDate = cleanFecha,
                    allergies = cleanAlergias,
                    bloodType = cleanSangre,
                    emergencyContact1 = cleanContacto1,
                    emergencyContact2 = cleanContacto2
                )

                Toast.makeText(
                    context,
                    "Perfil guardado",
                    Toast.LENGTH_SHORT
                ).show()

                coroutineScope.launch {
                    val payload = QrGenerator.buildEmergencyPayload(savedRecord)
                    qrPreview = QrGenerator.generateBitmap(payload)
                    scrollState.animateScrollTo(scrollState.maxValue)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_save_profile")
        ) {
            Text("Guardar perfil")
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_cancel_register")
        ) {
            Text("Volver")
        }

        qrPreview?.let { bitmap ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Código QR generado",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Código QR de emergencia",
                modifier = Modifier
                    .size(220.dp)
                    .align(Alignment.CenterHorizontally)
                    .testTag("img_qr_preview")
            )
        }
    }
}

@Preview(showBackground = true, name = "ChildRegisterScreen Preview")
@Composable
fun ChildRegisterScreenPreview() {
    MaterialTheme {
        ChildRegisterScreen()
    }
}