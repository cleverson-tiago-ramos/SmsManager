package com.cleverson.smsmanager

import android.Manifest
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.cleverson.smsmanager.receiver.SmsDeliveredReceiver
import com.cleverson.smsmanager.receiver.SmsSentReceiver
import com.cleverson.smsmanager.repository.SmsRepository
import com.cleverson.smsmanager.ui.screens.TelaSMS
import com.cleverson.smsmanager.ui.theme.SMSManagerTheme
import com.cleverson.smsmanager.utils.SMS_DELIVERED
import com.cleverson.smsmanager.utils.SMS_SENT
import com.cleverson.smsmanager.viewmodel.SmsViewModel
import android.content.Context
class MainActivity : ComponentActivity() {

    private val viewModel =
        SmsViewModel()

    private lateinit var repository:
            SmsRepository

    private lateinit var sentReceiver:
            SmsSentReceiver

    private lateinit var deliveredReceiver:
            SmsDeliveredReceiver

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {}

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        repository =
            SmsRepository(this)

        sentReceiver =
            SmsSentReceiver(
                viewModel.historico
            )

        deliveredReceiver =
            SmsDeliveredReceiver(
                viewModel.historico
            )

        verificarPermissaoSMS()

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

        setContent {

            SMSManagerTheme {

                TelaSMS(
                    historico =
                        viewModel.historico,

                    onEnviarSMS = {
                            numero,
                            mensagem ->

                        repository.enviarSMS(
                            numero,
                            mensagem,
                            viewModel.historico
                        )
                    }
                )
            }
        }
    }

    override fun onDestroy() {

        super.onDestroy()

        unregisterReceiver(
            sentReceiver
        )

        unregisterReceiver(
            deliveredReceiver
        )
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