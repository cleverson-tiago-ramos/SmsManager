package com.cleverson.smsmanager.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.util.Log
import com.cleverson.smsmanager.store.SmsStatusStore

class SmsSentReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context?,
        intent: Intent?
    ) {

        Log.d(
            "SMS_STATUS",
            "SentReceiver resultCode=$resultCode"
        )

        val smsId =
            intent?.getLongExtra(
                "sms_id",
                -1
            ) ?: -1

        when (resultCode) {

            Activity.RESULT_OK -> {

                SmsStatusStore.statusMap[smsId] =
                    "✅ ENVIADO"

                Log.d(
                    "SMS_STATUS",
                    "✅ SMS ENVIADO"
                )
            }

            SmsManager.RESULT_ERROR_GENERIC_FAILURE -> {

                SmsStatusStore.statusMap[smsId] =
                    "❌ FALHA GENÉRICA"

                Log.e(
                    "SMS_STATUS",
                    "❌ FALHA GENÉRICA"
                )
            }

            SmsManager.RESULT_ERROR_NO_SERVICE -> {

                SmsStatusStore.statusMap[smsId] =
                    "📡 SEM SERVIÇO"

                Log.e(
                    "SMS_STATUS",
                    "📡 SEM SERVIÇO"
                )
            }

            SmsManager.RESULT_ERROR_NULL_PDU -> {

                SmsStatusStore.statusMap[smsId] =
                    "⚠️ PDU NULO"

                Log.e(
                    "SMS_STATUS",
                    "⚠️ PDU NULO"
                )
            }

            SmsManager.RESULT_ERROR_RADIO_OFF -> {

                SmsStatusStore.statusMap[smsId] =
                    "✈️ MODO AVIÃO"

                Log.e(
                    "SMS_STATUS",
                    "✈️ MODO AVIÃO"
                )
            }

            else -> {

                SmsStatusStore.statusMap[smsId] =
                    "⚠️ ERRO DESCONHECIDO"

                Log.e(
                    "SMS_STATUS",
                    "⚠️ ERRO DESCONHECIDO: $resultCode"
                )
            }
        }
    }
}