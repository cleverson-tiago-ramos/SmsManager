package com.cleverson.smsmanager.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cleverson.smsmanager.data.model.HistoricoSMS

class SmsDeliveredReceiver(
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
                        "📬 ENTREGUE"

                    Activity.RESULT_CANCELED ->
                        "❌ NÃO ENTREGUE"

                    else ->
                        "⚠️ FALHA ENTREGA"
                }

            historico[index] =
                item.copy(
                    status = novoStatus
                )
        }
    }
}