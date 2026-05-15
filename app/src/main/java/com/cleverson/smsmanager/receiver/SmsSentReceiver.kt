package com.cleverson.smsmanager.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.telephony.SmsManager
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.cleverson.smsmanager.data.model.HistoricoSMS
import com.cleverson.smsmanager.utils.SMS_SENT

class SmsSentReceiver(

    private val historico:
    SnapshotStateList<HistoricoSMS>

) : BroadcastReceiver() {

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
            historico.indexOfFirst {

                it.id == smsId
            }

        if (index == -1) {
            return
        }

        val novoStatus =

            when (resultCode) {

                // ENVIADO
                Activity.RESULT_OK -> {

                    "✅ ENVIADO"
                }

                // FALHA GENÉRICA
                SmsManager.RESULT_ERROR_GENERIC_FAILURE -> {

                    "❌ FALHA GENÉRICA"
                }

                // SEM SERVIÇO
                SmsManager.RESULT_ERROR_NO_SERVICE -> {

                    "📡 SEM SERVIÇO"
                }

                // PDU INVÁLIDO
                SmsManager.RESULT_ERROR_NULL_PDU -> {

                    "⚠️ PDU NULO"
                }

                // MODO AVIÃO
                SmsManager.RESULT_ERROR_RADIO_OFF -> {

                    "✈️ MODO AVIÃO"
                }

                // DESCONHECIDO
                else -> {

                    "⚠️ ERRO DESCONHECIDO"
                }
            }

        historico[index] =
            historico[index].copy(
                status = novoStatus
            )
    }

    companion object {

        fun intentFilter() =
            IntentFilter(SMS_SENT)
    }
}