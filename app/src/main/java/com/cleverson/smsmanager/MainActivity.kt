package com.cleverson.smsmanager

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.mutableStateListOf
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

class MainActivity : ComponentActivity() {

    companion object {

        private const val TAG =
            "SMS_MANAGER"

        private const val SMS_SENT =
            "SMS_SENT"

        private const val SMS_DELIVERED =
            "SMS_DELIVERED"
    }

    // HISTÓRICO GLOBAL
    private val historicoState =
        mutableStateListOf<HistoricoSMS>()

    // RECEIVER ENVIO
    private val sentReceiver =
        object : BroadcastReceiver() {

            override fun onReceive(
                context: Context?,
                intent: Intent?
            ) {

                val smsId =
                    intent?.getLongExtra(
                        "sms_id",
                        -1
                    ) ?: -1

                val index =
                    historicoState.indexOfFirst {
                        it.id == smsId
                    }

                if (index != -1) {

                    val item =
                        historicoState[index]

                    val novoStatus =
                        when (resultCode) {

                            Activity.RESULT_OK -> {
                                "✅ ENVIADO"
                            }

                            SmsManager.RESULT_ERROR_GENERIC_FAILURE -> {
                                "❌ FALHA"
                            }

                            SmsManager.RESULT_ERROR_NO_SERVICE -> {
                                "📡 SEM SINAL"
                            }

                            SmsManager.RESULT_ERROR_NULL_PDU -> {
                                "⚠️ PDU NULO"
                            }

                            SmsManager.RESULT_ERROR_RADIO_OFF -> {
                                "✈️ MODO AVIÃO"
                            }

                            else -> {
                                "❌ ERRO"
                            }
                        }

                    historicoState[index] =
                        item.copy(
                            status = novoStatus
                        )
                }
            }
        }

    // RECEIVER ENTREGA
    private val deliveredReceiver =
        object : BroadcastReceiver() {

            override fun onReceive(
                context: Context?,
                intent: Intent?
            ) {

                val smsId =
                    intent?.getLongExtra(
                        "sms_id",
                        -1
                    ) ?: -1

                val index =
                    historicoState.indexOfFirst {
                        it.id == smsId
                    }

                if (index != -1) {

                    val item =
                        historicoState[index]

                    val novoStatus =
                        when (resultCode) {

                            Activity.RESULT_OK -> {
                                "📬 ENTREGUE"
                            }

                            Activity.RESULT_CANCELED -> {
                                "❌ NÃO ENTREGUE"
                            }

                            else -> {
                                "⚠️ FALHA ENTREGA"
                            }
                        }

                    historicoState[index] =
                        item.copy(
                            status = novoStatus
                        )
                }
            }
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

        // REGISTRA RECEIVERS
        if (android.os.Build.VERSION.SDK_INT >= 33) {

            registerReceiver(
                sentReceiver,
                IntentFilter(SMS_SENT),
                Context.RECEIVER_NOT_EXPORTED
            )

            registerReceiver(
                deliveredReceiver,
                IntentFilter(SMS_DELIVERED),
                Context.RECEIVER_NOT_EXPORTED
            )

        } else {

            registerReceiver(
                sentReceiver,
                IntentFilter(SMS_SENT)
            )

            registerReceiver(
                deliveredReceiver,
                IntentFilter(SMS_DELIVERED)
            )
        }

        setContent {

            SMSManagerTheme {

                Scaffold(
                    modifier =
                        Modifier.fillMaxSize()
                ) { innerPadding ->

                    TelaSMS(
                        modifier =
                            Modifier.padding(innerPadding),

                        historico =
                            historicoState,

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

    override fun onDestroy() {

        super.onDestroy()

        unregisterReceiver(sentReceiver)
        unregisterReceiver(deliveredReceiver)
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
                getSystemService(
                    SmsManager::class.java
                )

            // ID ÚNICO
            val smsId =
                System.currentTimeMillis()

            // STATUS INICIAL
            val horarioAtual =
                SimpleDateFormat(
                    "HH:mm:ss",
                    Locale.getDefault()
                ).format(Date())

            historicoState.add(
                0,
                HistoricoSMS(
                    id = smsId,
                    numero = numero,
                    mensagem = mensagem,
                    horario = horarioAtual,
                    status = "⏳ ENVIANDO"
                )
            )

            // INTENT ENVIO
            val sentIntent =
                Intent(SMS_SENT).apply {

                    putExtra(
                        "sms_id",
                        smsId
                    )
                }
            // FALLBACK DE STATUS
            android.os.Handler(
                mainLooper
            ).postDelayed({

                val index =
                    historicoState.indexOfFirst {
                        it.id == smsId &&
                                it.status == "⏳ ENVIANDO"
                    }

                if (index != -1) {

                    val item =
                        historicoState[index]

                    historicoState[index] =
                        item.copy(
                            status = "⚠️ SEM RETORNO"
                        )
                }

            }, 10000)
            // INTENT ENTREGA
            val deliveredIntent =
                Intent(SMS_DELIVERED).apply {

                    putExtra(
                        "sms_id",
                        smsId
                    )
                }

            // PENDING ENVIO
            val sentPI =
                PendingIntent.getBroadcast(
                    this,
                    smsId.toInt(),
                    sentIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or
                            PendingIntent.FLAG_IMMUTABLE
                )

            // PENDING ENTREGA
            val deliveredPI =
                PendingIntent.getBroadcast(
                    this,
                    smsId.toInt() + 1,
                    deliveredIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or
                            PendingIntent.FLAG_IMMUTABLE
                )

            // DIVIDIR MENSAGEM
            val partes =
                smsManager.divideMessage(
                    mensagem
                )

            // CALLBACKS
            val sentIntents =
                ArrayList<PendingIntent>()

            val deliveredIntents =
                ArrayList<PendingIntent>()

            partes.forEach {

                sentIntents.add(sentPI)
                deliveredIntents.add(deliveredPI)
            }

            // ENVIAR
            if (partes.size > 1) {

                smsManager.sendMultipartTextMessage(
                    numero,
                    null,
                    partes,
                    sentIntents,
                    deliveredIntents
                )

            } else {

                smsManager.sendTextMessage(
                    numero,
                    null,
                    mensagem,
                    sentPI,
                    deliveredPI
                )
            }

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Erro ao enviar SMS",
                e
            )

            historicoState.add(
                0,
                HistoricoSMS(
                    id = System.currentTimeMillis(),
                    numero = numero,
                    mensagem = mensagem,
                    horario = SimpleDateFormat(
                        "HH:mm:ss",
                        Locale.getDefault()
                    ).format(Date()),
                    status = "❌ ERRO"
                )
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
    val id: Long,
    val numero: String,
    val mensagem: String,
    val horario: String,
    val status: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaSMS(
    modifier: Modifier = Modifier,
    historico: SnapshotStateList<HistoricoSMS>,
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
        modifier =
            modifier.fillMaxSize(),

        color =
            MaterialTheme
                .colorScheme
                .background
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(24.dp)
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
                    Modifier.height(16.dp)
            )

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
                            "Número de telefone"
                    )

                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )

                    Row(
                        horizontalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {

                        ExposedDropdownMenuBox(
                            expanded =
                                expandirPaises,

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
                                }
                            )

                            ExposedDropdownMenu(
                                expanded =
                                    expandirPaises,

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

                            modifier =
                                Modifier.weight(1f),

                            placeholder = {
                                Text("43999999999")
                            },

                            keyboardOptions =
                                KeyboardOptions(
                                    keyboardType =
                                        KeyboardType.Phone
                                )
                        )
                    }
                }
            }

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            ElevatedCard(
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier =
                        Modifier.padding(16.dp)
                ) {

                    Text(
                        text = "Mensagem"
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

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(100.dp),

                        placeholder = {
                            Text(
                                "Digite sua mensagem..."
                            )
                        }
                    )

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
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
                            "${mensagem.length}/160 caracteres"
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(20.dp)
            )

            Button(
                onClick = {

                    focusManager.clearFocus()

                    if (
                        numero.isBlank() ||
                        mensagem.isBlank()
                    ) {
                        return@Button
                    }

                    loading = true

                    val numeroCompleto =
                        "${paisSelecionado.codigo}$numero"

                    onEnviarSMS(
                        numeroCompleto,
                        mensagem
                    )

                    numero = ""
                    mensagem = ""

                    loading = false
                },

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(60.dp)
            ) {

                if (loading) {

                    CircularProgressIndicator(
                        modifier =
                            Modifier.size(24.dp),

                        strokeWidth = 2.dp
                    )

                } else {

                    Text(
                        text =
                            "Enviar SMS"
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(24.dp)
            )

            Text(
                text =
                    "Histórico de SMS",

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
                                    item.mensagem
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(8.dp)
                            )

                            Text(
                                text =
                                    "🕒 ${item.horario}"
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(4.dp)
                            )

                            Text(
                                text =
                                    item.status,

                                color =
                                    when {

                                        item.status.contains("ENVIADO") ->
                                            MaterialTheme.colorScheme.primary

                                        item.status.contains("ENTREGUE") ->
                                            MaterialTheme.colorScheme.primary

                                        item.status.contains("FALHA") ->
                                            MaterialTheme.colorScheme.error

                                        item.status.contains("MODO AVIÃO") ->
                                            MaterialTheme.colorScheme.error

                                        else ->
                                            MaterialTheme.colorScheme.secondary
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}