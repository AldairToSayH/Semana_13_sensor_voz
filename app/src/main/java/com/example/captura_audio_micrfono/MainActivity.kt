package com.example.captura_audio_micrfono

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.captura_audio_micrfono.ui.theme.Captura_Audio_MicrófonoTheme
import java.text.Normalizer
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Captura_Audio_MicrófonoTheme {
                Microfono()
            }
        }
    }
}

@Composable
fun Microfono(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var texto by remember { mutableStateOf("") }
    var colorFondo by remember { mutableStateOf(Color.White) }
    var mensaje by remember {
        mutableStateOf("Pulsa Hablar y di un color, por ejemplo: rojo, amarillo o azul.")
    }

    val reconocimientoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        if (resultado.resultCode == Activity.RESULT_OK) {
            val resultados = resultado.data?.getStringArrayListExtra(
                RecognizerIntent.EXTRA_RESULTS
            )
            texto = resultados?.firstOrNull().orEmpty()
            val colorReconocido = buscarColor(texto)

            if (colorReconocido != null) {
                colorFondo = colorReconocido.color
                mensaje = "Fondo cambiado a ${colorReconocido.nombre}."
            } else if (texto.isNotBlank()) {
                mensaje = "No reconocí un color en la orden. Prueba con rojo, amarillo o azul."
            } else {
                mensaje = "No se pudo reconocer lo que dijiste. Inténtalo otra vez."
            }
        } else {
            mensaje = "El reconocimiento de voz fue cancelado."
        }
    }

    fun iniciarReconocimiento() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-PE")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Di el color del fondo")
        }

        try {
            reconocimientoLauncher.launch(intent)
        } catch (_: ActivityNotFoundException) {
            mensaje = "Este dispositivo no tiene un servicio de reconocimiento de voz."
        }
    }

    val permisoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            iniciarReconocimiento()
        } else {
            mensaje = "Se necesita permiso para usar el micrófono."
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colorFondo
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Luz y color",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Consulta la iluminación y cambia el fondo usando tu voz.",
                style = MaterialTheme.typography.bodyLarge
            )

            SensorLuz()

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Control por voz",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = mensaje,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Button(
                        onClick = {
                            if (
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                iniciarReconocimiento()
                            } else {
                                permisoLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "Hablar",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    if (texto.isNotBlank()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Orden reconocida: $texto",
                                modifier = Modifier.padding(14.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SensorLuz(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    val sensorLuz = remember {
        sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    }
    var lux by remember { mutableStateOf(0f) }
    val sensorDisponible = sensorLuz != null
    val ambiente = when {
        lux < 50f -> AmbienteLuz("Anochecer", R.drawable.anochecer)
        lux < 500f -> AmbienteLuz("Atardecer", R.drawable.atardecer)
        else -> AmbienteLuz("Amanecer", R.drawable.amanecer)
    }

    DisposableEffect(sensorDisponible) {
        if (!sensorDisponible) {
            return@DisposableEffect onDispose { }
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                lux = event.values[0]
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        sensorManager.registerListener(
            listener,
            sensorLuz,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sensor de luz",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (sensorDisponible) {
                    "Luz: $lux lux"
                } else {
                    "No hay sensor de luz"
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium
            )

            if (sensorDisponible) {
                Spacer(modifier = Modifier.height(14.dp))
                Image(
                    painter = painterResource(id = ambiente.imagen),
                    contentDescription = ambiente.nombre,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = ambiente.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

private data class AmbienteLuz(val nombre: String, val imagen: Int)

private data class ColorVoz(val nombre: String, val color: Color)

private fun buscarColor(orden: String): ColorVoz? {
    val textoNormalizado = Normalizer.normalize(
        orden.lowercase(Locale.getDefault()),
        Normalizer.Form.NFD
    ).replace("\\p{M}+".toRegex(), "")

    val colores = listOf(
        "amarillo" to Color(0xFFFFEB3B),
        "naranja" to Color(0xFFFF9800),
        "morado" to Color(0xFF9C27B0),
        "violeta" to Color(0xFF9C27B0),
        "rosado" to Color(0xFFE91E63),
        "rosa" to Color(0xFFE91E63),
        "blanco" to Color.White,
        "negro" to Color.Black,
        "verde" to Color(0xFF4CAF50),
        "azul" to Color(0xFF2196F3),
        "rojo" to Color(0xFFF44336),
        "gris" to Color.Gray
    )

    return colores.firstNotNullOfOrNull { (nombre, color) ->
        if (Regex("\\b$nombre\\b").containsMatchIn(textoNormalizado)) {
            ColorVoz(nombre, color)
        } else {
            null
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MicrofonoPreview() {
    Captura_Audio_MicrófonoTheme {
        Microfono()
    }
}
