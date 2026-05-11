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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.cleverson.smsmanager.ui.theme.SMSManagerTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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

            } else {

                Toast.makeText(
                    this,
                    "Permissão negada!",
                    Toast.LENGTH_SHORT
                ).show()
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

            Log.d(
                TAG,
                "Enviando SMS para: $numero"
            )

            val smsManager =
                SmsManager.getDefault()

            // ACTIONS
            val SMS_SENT =
                "SMS_SENT"

            val SMS_DELIVERED =
                "SMS_DELIVERED"

            // PENDING INTENTS
            val sentPI =
                PendingIntent.getBroadcast(
                    this,
                    0,
                    Intent(SMS_SENT),
                    PendingIntent.FLAG_IMMUTABLE
                )

            val deliveredPI =
                PendingIntent.getBroadcast(
                    this,
                    0,
                    Intent(SMS_DELIVERED),
                    PendingIntent.FLAG_IMMUTABLE
                )

            // RECEIVER ENVIO
            registerReceiver(

                object : BroadcastReceiver() {

                    override fun onReceive(
                        context: Context?,
                        intent: Intent?
                    ) {

                        when (resultCode) {

                            RESULT_OK -> {

                                Toast.makeText(
                                    this@MainActivity,
                                    "SMS enviado com sucesso!",
                                    Toast.LENGTH_LONG
                                ).show()

                                Log.d(
                                    TAG,
                                    "SMS enviado"
                                )
                            }

                            SmsManager.RESULT_ERROR_GENERIC_FAILURE -> {

                                Toast.makeText(
                                    this@MainActivity,
                                    "Falha genérica",
                                    Toast.LENGTH_LONG
                                ).show()

                                Log.e(
                                    TAG,
                                    "Falha genérica"
                                )
                            }

                            SmsManager.RESULT_ERROR_NO_SERVICE -> {

                                Toast.makeText(
                                    this@MainActivity,
                                    "Sem serviço",
                                    Toast.LENGTH_LONG
                                ).show()

                                Log.e(
                                    TAG,
                                    "Sem serviço"
                                )
                            }

                            SmsManager.RESULT_ERROR_NULL_PDU -> {

                                Toast.makeText(
                                    this@MainActivity,
                                    "PDU nulo",
                                    Toast.LENGTH_LONG
                                ).show()

                                Log.e(
                                    TAG,
                                    "PDU nulo"
                                )
                            }

                            SmsManager.RESULT_ERROR_RADIO_OFF -> {

                                Toast.makeText(
                                    this@MainActivity,
                                    "Modo avião ativo",
                                    Toast.LENGTH_LONG
                                ).show()

                                Log.e(
                                    TAG,
                                    "Rádio desligado"
                                )
                            }
                        }
                    }

                },

                IntentFilter(SMS_SENT),
                Context.RECEIVER_NOT_EXPORTED
            )

            // RECEIVER ENTREGA
            registerReceiver(

                object : BroadcastReceiver() {

                    override fun onReceive(
                        context: Context?,
                        intent: Intent?
                    ) {

                        when (resultCode) {

                            RESULT_OK -> {

                                Toast.makeText(
                                    this@MainActivity,
                                    "SMS entregue!",
                                    Toast.LENGTH_LONG
                                ).show()

                                Log.d(
                                    TAG,
                                    "SMS entregue"
                                )
                            }

                            RESULT_CANCELED -> {

                                Toast.makeText(
                                    this@MainActivity,
                                    "SMS não entregue",
                                    Toast.LENGTH_LONG
                                ).show()

                                Log.e(
                                    TAG,
                                    "Falha entrega SMS"
                                )
                            }
                        }
                    }

                },

                IntentFilter(SMS_DELIVERED),
                Context.RECEIVER_NOT_EXPORTED
            )

            // DIVIDIR MENSAGEM
            val partes =
                smsManager.divideMessage(mensagem)

            // LISTAS DE CALLBACKS
            val sentIntents =
                ArrayList<PendingIntent>()

            val deliveredIntents =
                ArrayList<PendingIntent>()

            partes.forEach {

                sentIntents.add(sentPI)
                deliveredIntents.add(deliveredPI)
            }

            // ENVIO MULTIPART
            smsManager.sendMultipartTextMessage(
                numero,
                null,
                partes,
                sentIntents,
                deliveredIntents
            )

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

data class HistoricoSMS(
    val numero: String,
    val mensagem: String,
    val horario: String
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

    var historico by remember {
        mutableStateOf(
            listOf<HistoricoSMS>()
        )
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
                .padding(top = 48.dp)
        ) {

            Text(
                text = "SMS Manager",

                style =
                    MaterialTheme
                        .typography
                        .headlineLarge
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )



            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            // CARD TELEFONE
            ElevatedCard(
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier =
                        Modifier.padding(18.dp)
                ) {

                    Text(
                        text =
                            "Número de telefone",

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
                        horizontalArrangement =
                            Arrangement.spacedBy(12.dp),

                        verticalAlignment =
                            Alignment.Top
                    ) {

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

                                modifier =
                                    Modifier.width(140.dp),

                                trailingIcon = {

                                    ExposedDropdownMenuDefaults
                                        .TrailingIcon(
                                            expanded =
                                                expandirPaises
                                        )
                                },

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
                                                "${pais.bandeira} ${pais.nome}"
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
                                            "Digite o telefone"
                                        )
                                    }

                                    numero.length < 10 -> {

                                        Text(
                                            "Número inválido"
                                        )
                                    }

                                    else -> {

                                        Text(
                                            "Número válido"
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }

            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )

            // CARD MENSAGEM
            ElevatedCard(
                modifier =
                    Modifier.fillMaxWidth()
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
                                .height(100.dp),

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
                            Modifier.height(8.dp)
                    )

                    Text(
                        text =
                            "${mensagem.length}/160 caracteres",

                        style =
                            MaterialTheme
                                .typography
                                .bodySmall
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(20.dp)
            )

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

                            val numeroCompleto =
                                "${paisSelecionado.codigo}$numero"

                            onEnviarSMS(
                                numeroCompleto,
                                mensagem
                            )

                            val horarioAtual =
                                SimpleDateFormat(
                                    "HH:mm:ss",
                                    Locale.getDefault()
                                ).format(Date())

                            historico =
                                listOf(
                                    HistoricoSMS(
                                        numero =
                                            numeroCompleto,

                                        mensagem =
                                            mensagem,

                                        horario =
                                            horarioAtual
                                    )
                                ) + historico

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
                        .extraLarge
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

            Spacer(
                modifier =
                    Modifier.height(24.dp)
            )

            Text(
                text = "Histórico de SMS",

                style =
                    MaterialTheme
                        .typography
                        .headlineSmall
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            LazyColumn(

                modifier =
                    Modifier.weight(1f),

                verticalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {

                items(historico) { item ->

                    ElevatedCard(
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Column(
                            modifier =
                                Modifier.padding(16.dp)
                        ) {

                            Text(
                                text =
                                    "📱 ${item.numero}",

                                style =
                                    MaterialTheme
                                        .typography
                                        .titleMedium
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(6.dp)
                            )

                            Text(
                                text =
                                    item.mensagem,

                                style =
                                    MaterialTheme
                                        .typography
                                        .bodyLarge
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(8.dp)
                            )

                            Text(
                                text =
                                    "🕒 ${item.horario}",

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
                }
            }
        }
    }
}