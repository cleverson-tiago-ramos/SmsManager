package com.cleverson.smsmanager

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.cleverson.smsmanager.ui.theme.SMSManagerTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "SMS_MANAGER"
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->

            if (isGranted) {

                Toast.makeText(
                    this,
                    "Permissão concedida!",
                    Toast.LENGTH_SHORT
                ).show()

                Log.d(TAG, "Permissão SEND_SMS concedida")

            } else {

                Toast.makeText(
                    this,
                    "Permissão negada!",
                    Toast.LENGTH_SHORT
                ).show()

                Log.e(TAG, "Permissão SEND_SMS negada")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        verificarPermissaoSMS()

        setContent {

            SMSManagerTheme {

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->

                    TelaSMS(
                        modifier = Modifier.padding(innerPadding),
                        onEnviarSMS = { numero, mensagem ->

                            enviarSMS(
                                numero,
                                mensagem
                            )
                        }
                    )
                }
            }
        }
    }

    private fun verificarPermissaoSMS() {

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            requestPermissionLauncher.launch(
                Manifest.permission.SEND_SMS
            )
        }
    }

    private fun enviarSMS(
        numero: String,
        mensagem: String
    ) {

        try {

            Log.d(TAG, "Enviando SMS para: $numero")

            val smsManager = SmsManager.getDefault()

            val partes =
                smsManager.divideMessage(mensagem)

            smsManager.sendMultipartTextMessage(
                numero,
                null,
                partes,
                null,
                null
            )

            Log.d(TAG, "SMS enviado com sucesso")

            Toast.makeText(
                this,
                "SMS enviado com sucesso!",
                Toast.LENGTH_LONG
            ).show()

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Erro ao enviar SMS",
                e
            )

            Toast.makeText(
                this,
                "Erro ao enviar SMS",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaSMS(
    modifier: Modifier = Modifier,
    onEnviarSMS: (
        String,
        String
    ) -> Unit
) {

    var numero by remember {
        mutableStateOf("")
    }

    var mensagem by remember {
        mutableStateOf("")
    }

    var loading by remember {
        mutableStateOf(false)
    }

    val focusManager =
        LocalFocusManager.current

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp),

            verticalArrangement =
                Arrangement.spacedBy(20.dp)
        ) {

            Text(
                text = "SMS Manager",
                style =
                    MaterialTheme.typography.headlineLarge
            )

            Text(
                text =
                    "Envie mensagens SMS diretamente pelo dispositivo.",
                style =
                    MaterialTheme.typography.bodyMedium,

                color =
                    MaterialTheme.colorScheme.onSurfaceVariant
            )

            ElevatedCard(
                modifier =
                    Modifier.fillMaxWidth(),

                elevation =
                    CardDefaults.elevatedCardElevation(
                        defaultElevation = 6.dp
                    )
            ) {

                Column(
                    modifier =
                        Modifier.padding(18.dp)
                ) {

                    Text(
                        text = "Número de telefone"
                    )

                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )

                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically,

                        horizontalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {

                        Surface(
                            shape =
                                RoundedCornerShape(12.dp),

                            tonalElevation = 4.dp,

                            color =
                                MaterialTheme.colorScheme.primaryContainer
                        ) {

                            Row(
                                modifier =
                                    Modifier.padding(
                                        horizontal = 14.dp,
                                        vertical = 16.dp
                                    ),

                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {

                                Text(
                                    text = "🇧🇷",

                                    style =
                                        MaterialTheme.typography.titleLarge
                                )

                                Spacer(
                                    modifier =
                                        Modifier.width(6.dp)
                                )

                                Text(
                                    text = "+55",

                                    style =
                                        MaterialTheme.typography.bodyLarge
                                )
                            }
                        }

                        OutlinedTextField(
                            value = numero,

                            onValueChange = {

                                numero =
                                    it.filter { c ->
                                        c.isDigit()
                                    }
                            },

                            placeholder = {
                                Text("(43) 99999-9999")
                            },

                            keyboardOptions =
                                KeyboardOptions(
                                    keyboardType =
                                        KeyboardType.Phone
                                ),

                            modifier =
                                Modifier.weight(1f),

                            singleLine = true,

                            isError =
                                numero.isNotEmpty() &&
                                        numero.length < 11,

                            supportingText = {

                                if (
                                    numero.isNotEmpty() &&
                                    numero.length < 11
                                ) {

                                    Text(
                                        text =
                                            "Número inválido"
                                    )
                                }
                            }
                        )
                    }
                }
            }

            ElevatedCard(
                modifier =
                    Modifier.fillMaxWidth(),

                elevation =
                    CardDefaults.elevatedCardElevation(
                        defaultElevation = 6.dp
                    )
            ) {

                Column(
                    modifier =
                        Modifier.padding(18.dp)
                ) {

                    Text(
                        text = "Mensagem"
                    )

                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )

                    OutlinedTextField(
                        value = mensagem,

                        onValueChange = {

                            if (it.length <= 160) {
                                mensagem = it
                            }
                        },

                        placeholder = {
                            Text(
                                "Digite sua mensagem..."
                            )
                        },

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(180.dp),

                        maxLines = 8
                    )

                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )

                    LinearProgressIndicator(
                        progress = {
                            mensagem.length / 160f
                        },

                        modifier =
                            Modifier.fillMaxWidth()
                    )

                    Spacer(
                        modifier =
                            Modifier.height(6.dp)
                    )

                    Text(
                        text =
                            "${mensagem.length}/160 caracteres",

                        style =
                            MaterialTheme.typography.bodySmall,

                        color =
                            MaterialTheme
                                .colorScheme
                                .primary
                    )
                }
            }

            Button(
                onClick = {

                    focusManager.clearFocus()

                    when {

                        numero.isEmpty() -> {
                            return@Button
                        }

                        numero.length < 10 -> {
                            return@Button
                        }

                        mensagem.isEmpty() -> {
                            return@Button
                        }

                        else -> {

                            loading = true

                            onEnviarSMS(
                                numero,
                                mensagem
                            )

                            numero = ""
                            mensagem = ""

                            loading = false
                        }
                    }
                },

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(60.dp),

                shape =
                    MaterialTheme.shapes.extraLarge,

                enabled = !loading
            ) {

                if (loading) {

                    CircularProgressIndicator(
                        modifier =
                            Modifier.size(24.dp),

                        strokeWidth = 2.dp
                    )

                } else {

                    Text(
                        text = "Enviar SMS"
                    )
                }
            }
        }
    }
}