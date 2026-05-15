package com.cleverson.smsmanager

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateListOf
import androidx.core.content.ContextCompat
import com.cleverson.smsmanager.data.model.HistoricoSMS
import com.cleverson.smsmanager.receiver.SmsDeliveredReceiver
import com.cleverson.smsmanager.receiver.SmsSentReceiver
import com.cleverson.smsmanager.repository.SmsRepository
import com.cleverson.smsmanager.ui.screens.TelaSMS
import com.cleverson.smsmanager.ui.theme.SMSManagerTheme

class MainActivity : ComponentActivity() {

    private lateinit var smsRepository: SmsRepository

    private val historico =
        mutableStateListOf<HistoricoSMS>()

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->

            if (!isGranted) {

                finish()
            }
        }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        verificarPermissaoSMS()

        smsRepository =
            SmsRepository(this)

        // RECEIVER ENVIO
        registerReceiver(
            SmsSentReceiver(historico),
            SmsSentReceiver.intentFilter(),
            RECEIVER_NOT_EXPORTED
        )

        // RECEIVER ENTREGA
        registerReceiver(
            SmsDeliveredReceiver(historico),
            SmsDeliveredReceiver.intentFilter(),
            RECEIVER_NOT_EXPORTED
        )

        setContent {

            SMSManagerTheme {

                TelaSMS(

                    historico = historico,

                    onEnviarSMS = {
                            numero,
                            mensagem ->

                        smsRepository.enviarSMS(
                            numero = numero,
                            mensagem = mensagem,
                            historico = historico
                        )
                    }
                )
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
}