package com.example.safeqr

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MedicalProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: SafeQrViewModel = hiltViewModel()
            val records by viewModel.records.collectAsStateWithLifecycle()

            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MedicalProfileScreen(
                        records = records,
                        modifier = Modifier.padding(innerPadding),
                        onDeleteRecord = { record -> viewModel.deleteRecord(record) },
                        // Abre QrDisplayActivity con el id del perfil
                        onOpenQrScreen = { record ->
                            startActivity(QrDisplayActivity.newIntent(this, record.id))
                        },
                        onBack = { finish() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalProfileScreen(
    records: List<MedicalRecord>,
    modifier: Modifier = Modifier,
    onDeleteRecord: (MedicalRecord) -> Unit = {},
    onOpenQrScreen: (MedicalRecord) -> Unit = {},
    onBack: () -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf("TODOS") }
    var selectedRecordForQr by remember { mutableStateOf<MedicalRecord?>(null) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(selectedRecordForQr) {
        val target = selectedRecordForQr
        qrBitmap = if (target != null) {
            QrGenerator.generateBitmap(QrGenerator.buildEmergencyPayload(target))
        } else {
            null
        }
    }

    val filteredRecords = remember(records, selectedFilter) {
        when (selectedFilter) {
            "INFANTIL" -> records.filter { it.profileType == "INFANTIL" }
            "GENERAL" -> records.filter { it.profileType == "GENERAL" }
            else -> records
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Perfiles Médicos Registrados",
            style = MaterialTheme.typography.headlineSmall
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == "TODOS",
                onClick = { selectedFilter = "TODOS" },
                label = { Text("Todos") }
            )
            FilterChip(
                selected = selectedFilter == "INFANTIL",
                onClick = { selectedFilter = "INFANTIL" },
                label = { Text("Infantil") }
            )
            FilterChip(
                selected = selectedFilter == "GENERAL",
                onClick = { selectedFilter = "GENERAL" },
                label = { Text("Médico") }
            )
        }

        if (filteredRecords.isEmpty()) {
            val emptyMessage = when (selectedFilter) {
                "INFANTIL" -> "No hay perfiles médicos infantiles registrados."
                "GENERAL" -> "No hay perfiles médicos registrados."
                else -> "Aún no hay perfiles guardados."
            }
            Text(
                text = emptyMessage,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredRecords, key = { it.id }) { record ->
                    val typeLabel = if (record.profileType == "GENERAL") {
                        "Perfil médico"
                    } else {
                        "Perfil médico infantil"
                    }

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = typeLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = record.fullName,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text("Nacimiento: ${record.birthDate.ifBlank { "No especificada" }}")
                            Text("Tipo de sangre: ${record.bloodType}")
                            Text("Alergias: ${record.allergies}")
                            Text("Contacto 1: ${record.emergencyContact1}")
                            if (record.emergencyContact2.isNotBlank()) {
                                Text("Contacto 2: ${record.emergencyContact2}")
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                            ) {
                                OutlinedButton(
                                    onClick = { selectedRecordForQr = record }
                                ) {
                                    Text("Ver QR")
                                }
                                Button(
                                    onClick = { onDeleteRecord(record) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Eliminar")
                                }
                            }
                        }
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }

    selectedRecordForQr?.let { record ->
        AlertDialog(
            onDismissRequest = { selectedRecordForQr = null },
            title = { Text("QR de ${record.fullName}") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    qrBitmap?.let { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Código QR de ${record.fullName}",
                            modifier = Modifier.size(220.dp)
                        )
                    } ?: Text("Generando código QR...")

                    Text(
                        text = QrGenerator.buildEmergencyPayload(record),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(onClick = { selectedRecordForQr = null }) {
                    Text("Cerrar")
                }
            },
            // Nuevo botón: ver los dos QR (registro y perfil médico) en su propia pantalla
            dismissButton = {
                OutlinedButton(onClick = {
                    selectedRecordForQr = null
                    onOpenQrScreen(record)
                }) {
                    Text("Ver QR por separado")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MedicalProfileScreenPreview() {
    MaterialTheme {
        MedicalProfileScreen(
            records = listOf(
                MedicalRecord(
                    id = 1,
                    profileType = "INFANTIL",
                    fullName = "Mateo Pérez",
                    birthDate = "14/05/2021",
                    allergies = "Penicilina",
                    bloodType = "O+",
                    emergencyContact1 = "Ana Pérez - 5512345678"
                ),
                MedicalRecord(
                    id = 2,
                    profileType = "GENERAL",
                    fullName = "Carlos López",
                    birthDate = "02/11/1998",
                    allergies = "Ninguna",
                    bloodType = "A-",
                    emergencyContact1 = "Laura López - 5587654321"
                )
            )
        )
    }
}