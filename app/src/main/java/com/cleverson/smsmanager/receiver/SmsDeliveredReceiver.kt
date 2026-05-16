package com.cleverson.smsmanager.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.cleverson.smsmanager.store.SmsStatusStore

class SmsDeliveredReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context?,
        intent: Intent?
    ) {

        Log.d(
            "SMS_STATUS",
            "DeliveredReceiver resultCode=$resultCode"
        )

        val smsId =
            intent?.getLongExtra(
                "sms_id",
                -1
            ) ?: -1

        when (resultCode) {

            // ENTREGUE
            Activity.RESULT_OK -> {

                SmsStatusStore.statusMap[smsId] =
                    "📬 ENTREGUE"

                Log.d(
                    "SMS_STATUS",
                    "📬 SMS ENTREGUE"
                )
            }

            // NÃO ENTREGUE
            Activity.RESULT_CANCELED -> {

                SmsStatusStore.statusMap[smsId] =
                    "⚠️ NÃO ENTREGUE"

                Log.e(
                    "SMS_STATUS",
                    "⚠️ SMS NÃO ENTREGUE"
                )
            }

            // OUTROS
            else -> {

                SmsStatusStore.statusMap[smsId] =
                    "⚠️ ENTREGA DESCONHECIDA"

                Log.e(
                    "SMS_STATUS",
                    "⚠️ ENTREGA DESCONHECIDA: $resultCode"
                )
            }
        }
    }
}