package com.example.safeqr

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint

// Actividad dedicada a visualizar los códigos QR generados
@AndroidEntryPoint
class QrDisplayActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_RECORD_ID = "extra_record_id"

        // Forma segura de abrir esta pantalla con el id del perfil
        fun newIntent(context: Context, recordId: Int): Intent =
            Intent(context, QrDisplayActivity::class.java)
                .putExtra(EXTRA_RECORD_ID, recordId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Habilita diseño de borde a borde
        enableEdgeToEdge()

        val recordId = intent.getIntExtra(EXTRA_RECORD_ID, -1)

        setContent {
            val viewModel: SafeQrViewModel = hiltViewModel()
            val record by viewModel.selectedRecord.collectAsStateWithLifecycle()

            var registerQr by remember { mutableStateOf<Bitmap?>(null) }
            var medicalQr by remember { mutableStateOf<Bitmap?>(null) }

            // 1) Pide el perfil a Room usando el id recibido
            LaunchedEffect(recordId) {
                if (recordId != -1) viewModel.loadRecord(recordId)
            }

            // 2) Cuando llega el perfil, genera los dos QR en segundo plano
            LaunchedEffect(record) {
                record?.let {
                    registerQr = QrGenerator.generateBitmap(QrGenerator.buildRegisterPayload(it))
                    medicalQr = QrGenerator.generateBitmap(QrGenerator.buildMedicalPayload(it))
                }
            }

            MaterialTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("qr_display_scaffold")
                ) { innerPadding ->
                    QrDisplayScreen(
                        modifier = Modifier.padding(innerPadding),
                        childName = record?.fullName ?: "Sin perfil seleccionado",
                        childRegisterQrBitmap = registerQr,
                        medicalProfileQrBitmap = medicalQr,
                        onBack = { finish() }
                    )
                }
            }
        }
    }
}

// Composable que muestra ambos códigos QR: Registro Infantil y Perfil Médico
@Composable
fun QrDisplayScreen(
    modifier: Modifier = Modifier,
    childName: String = "Sin perfil seleccionado",
    childRegisterQrBitmap: Bitmap? = null,
    medicalProfileQrBitmap: Bitmap? = null,
    onBack: () -> Unit = {}
) {
    // Permite desplazar verticalmente si la pantalla es pequeña
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(20.dp)
            .testTag("qr_display_container"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Encabezado superior con título y botón para regresar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Códigos QR",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("label_qr_screen_title")
            )
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.testTag("btn_back_from_qr_display")
            ) {
                Text("Volver")
            }
        }

        // Nombre del menor al que pertenecen ambos códigos QR
        Text(
            text = "Menor: $childName",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.testTag("label_qr_child_name")
        )

        // Tarjeta 1: Código QR del Registro Infantil (datos de identificación y contacto)
        QrCodeSectionCard(
            title = "QR - Registro Infantil",
            subtitle = "Contiene nombre, fecha de nacimiento y contactos de emergencia.",
            qrBitmap = childRegisterQrBitmap,
            cardTag = "card_child_register_qr",
            imageTag = "img_child_register_qr"
        )

        // Tarjeta 2: Código QR del Perfil Médico (alergias, tipo de sangre y alertas clínicas)
        QrCodeSectionCard(
            title = "QR - Perfil Médico",
            subtitle = "Contiene tipo de sangre, alergias e información clínica de emergencia.",
            qrBitmap = medicalProfileQrBitmap,
            cardTag = "card_medical_profile_qr",
            imageTag = "img_medical_profile_qr"
        )
    }
}

// Componente reutilizable para cada tarjeta que contiene un título, descripción y su imagen QR
@Composable
private fun QrCodeSectionCard(
    title: String,
    subtitle: String,
    qrBitmap: Bitmap?,
    cardTag: String,
    imageTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(cardTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título de la tarjeta QR
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Descripción de qué información incluye ese QR
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Muestra el Bitmap del QR si ya fue generado, o un recuadro de espera si es null
            if (qrBitmap != null) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = title,
                    modifier = Modifier
                        .size(200.dp)
                        .testTag(imageTag)
                )
            } else {
                Surface(
                    tonalElevation = 2.dp,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .size(200.dp)
                        .testTag("${imageTag}_placeholder")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "QR pendiente",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

// Vista previa en Android Studio de la pantalla de códigos QR
@Preview(showBackground = true, name = "QrDisplayScreen Preview")
@Composable
fun QrDisplayScreenPreview() {
    MaterialTheme {
        QrDisplayScreen(childName = "Mateo Hernández")
    }
}