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
import androidx.compose.material3.MenuAnchorType

import androidx.compose.ui.Alignment
import androidx.compose.runtime.saveable.rememberSaveable
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
data class Pais(
    val nome: String,
    val codigo: String,
    val bandeira: String
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaSMS(
    modifier: Modifier = Modifier,
    onEnviarSMS: (
        String,
        String
    ) -> Unit
) {



    val paises = listOf(
        Pais("Brasil", "+55", "🇧🇷"),
        Pais("Estados Unidos", "+1", "🇺🇸"),
        Pais("Portugal", "+351", "🇵🇹"),
        Pais("Argentina", "+54", "🇦🇷"),
        Pais("França", "+33", "🇫🇷"),
        Pais("Alemanha", "+49", "🇩🇪")
    )

    var paisSelecionado by remember {
        mutableStateOf(paises[0])
    }

    var expandirPaises by remember {
        mutableStateOf(false)
    }

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

            // HEADER
            Column {

                Text(
                    text = "SMS Manager",
                    style =
                        MaterialTheme.typography.headlineLarge
                )

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )

                Text(
                    text =
                        "Envie mensagens SMS diretamente pelo dispositivo.",

                    style =
                        MaterialTheme.typography.bodyMedium,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )
            }

            // CARD TELEFONE
            ElevatedCard(
                modifier =
                    Modifier.fillMaxWidth(),

                elevation =
                    CardDefaults
                        .elevatedCardElevation(
                            defaultElevation = 6.dp
                        )
            ) {

                Column(
                    modifier =
                        Modifier.padding(18.dp)
                ) {

                    Text(
                        text = "Número de telefone",

                        style =
                            MaterialTheme
                                .typography
                                .titleMedium
                    )

                    Spacer(
                        modifier =
                            Modifier.height(14.dp)
                    )

                    Row(
                        verticalAlignment =
                            Alignment.Top,

                        horizontalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {

                        // SELECT PAÍS
                        ExposedDropdownMenuBox(
                            expanded = expandirPaises,

                            onExpandedChange = {
                                expandirPaises =
                                    !expandirPaises
                            }
                        ) {

                            OutlinedTextField(
                                value =
                                    "${paisSelecionado.bandeira} ${paisSelecionado.codigo}",

                                onValueChange = {},

                                readOnly = true,

                                trailingIcon = {

                                    ExposedDropdownMenuDefaults
                                        .TrailingIcon(
                                            expanded =
                                                expandirPaises
                                        )
                                },

                                modifier =
                                    Modifier
                                        .width(145.dp)
                                        .menuAnchor(
                                            MenuAnchorType.PrimaryNotEditable,
                                            true
                                        ),

                                singleLine = true
                            )

                            ExposedDropdownMenu(
                                expanded = expandirPaises,

                                onDismissRequest = {
                                    expandirPaises = false
                                }
                            ) {

                                paises.forEach { pais ->

                                    DropdownMenuItem(

                                        text = {

                                            Text(
                                                "${pais.bandeira} ${pais.nome} ${pais.codigo}"
                                            )
                                        },

                                        onClick = {

                                            paisSelecionado = pais
                                            expandirPaises = false
                                        }
                                    )
                                }
                            }
                        }

                        // TELEFONE
                        OutlinedTextField(
                            value = numero,

                            onValueChange = {

                                numero =
                                    it.filter { c ->
                                        c.isDigit()
                                    }
                            },

                            placeholder = {
                                Text("43999999999")
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
                                        numero.length < 10,

                            supportingText = {

                                when {

                                    numero.isEmpty() -> {

                                        Text(
                                            text =
                                                "Digite o telefone"
                                        )
                                    }

                                    numero.length < 10 -> {

                                        Text(
                                            text =
                                                "Número inválido"
                                        )
                                    }

                                    else -> {

                                        Text(
                                            text =
                                                "Número válido"
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }

            // CARD MENSAGEM
            ElevatedCard(
                modifier =
                    Modifier.fillMaxWidth(),

                elevation =
                    CardDefaults
                        .elevatedCardElevation(
                            defaultElevation = 6.dp
                        )
            ) {

                Column(
                    modifier =
                        Modifier.padding(18.dp)
                ) {

                    Text(
                        text = "Mensagem",

                        style =
                            MaterialTheme
                                .typography
                                .titleMedium
                    )

                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
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
                            Modifier.height(14.dp)
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
                            Modifier.height(8.dp)
                    )

                    Text(
                        text =
                            "${mensagem.length}/160 caracteres",

                        style =
                            MaterialTheme
                                .typography
                                .bodySmall,

                        color =
                            MaterialTheme
                                .colorScheme
                                .primary
                    )
                }
            }

            // BOTÃO
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
                                "${paisSelecionado.codigo}$numero",
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
                    MaterialTheme
                        .shapes
                        .extraLarge,

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
                        text = "Enviar SMS",

                        style =
                            MaterialTheme
                                .typography
                                .titleMedium
                    )
                }
            }
        }
    }
}