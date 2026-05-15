package com.cleverson.smsmanager.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.cleverson.smsmanager.data.model.HistoricoSMS
import com.cleverson.smsmanager.utils.SMS_DELIVERED

class SmsDeliveredReceiver(

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

                // ENTREGUE
                Activity.RESULT_OK -> {

                    "📬 ENTREGUE"
                }

                // NÃO ENTREGUE
                Activity.RESULT_CANCELED -> {

                    "⚠️ NÃO ENTREGUE"
                }

                // OUTROS
                else -> {

                    "⚠️ ENTREGA DESCONHECIDA"
                }
            }

        historico[index] =
            historico[index].copy(
                status = novoStatus
            )
    }

    companion object {

        fun intentFilter() =
            IntentFilter(SMS_DELIVERED)
    }
}