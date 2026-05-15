package com.cleverson.smsmanager.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import com.cleverson.smsmanager.data.model.HistoricoSMS

class SmsSentReceiver(
    private val historico: MutableList<HistoricoSMS>
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

        if (index != -1) {

            val item =
                historico[index]

            val novoStatus =
                when (resultCode) {

                    Activity.RESULT_OK ->
                        "✅ ENVIADO"

                    SmsManager.RESULT_ERROR_GENERIC_FAILURE ->
                        "❌ FALHA"

                    SmsManager.RESULT_ERROR_NO_SERVICE ->
                        "📡 SEM SINAL"

                    SmsManager.RESULT_ERROR_RADIO_OFF ->
                        "✈️ MODO AVIÃO"

                    else ->
                        "❌ ERRO"
                }

            historico[index] =
                item.copy(
                    status = novoStatus
                )
        }
    }
}