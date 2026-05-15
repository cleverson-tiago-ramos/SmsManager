package com.cleverson.smsmanager.repository

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.util.Log
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.cleverson.smsmanager.data.model.HistoricoSMS
import com.cleverson.smsmanager.utils.SMS_DELIVERED
import com.cleverson.smsmanager.utils.SMS_SENT
import com.cleverson.smsmanager.utils.TAG
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SmsRepository(
    private val context: Context
) {

    fun enviarSMS(
        numero: String,
        mensagem: String,
        historico: SnapshotStateList<HistoricoSMS>
    ) {

        try {

            val smsManager =
                SmsManager.getDefault()

            val smsId =
                System.currentTimeMillis()

            val pin =
                (1000..9999).random()

            val mensagemFinal =
                "$mensagem\nPIN: $pin"

            val horarioAtual =
                SimpleDateFormat(
                    "HH:mm:ss",
                    Locale.getDefault()
                ).format(Date())

            historico.add(
                0,
                HistoricoSMS(
                    id = smsId,
                    numero = numero,
                    mensagem = mensagemFinal,
                    horario = horarioAtual,
                    status = "⏳ ENVIANDO",
                    pin = pin.toString()
                )
            )

            val sentIntent =
                Intent(SMS_SENT).apply {

                    putExtra(
                        "sms_id",
                        smsId
                    )
                }

            val deliveredIntent =
                Intent(SMS_DELIVERED).apply {

                    putExtra(
                        "sms_id",
                        smsId
                    )
                }

            val sentPI =
                PendingIntent.getBroadcast(
                    context,
                    smsId.toInt(),
                    sentIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or
                            PendingIntent.FLAG_IMMUTABLE
                )

            val deliveredPI =
                PendingIntent.getBroadcast(
                    context,
                    smsId.toInt() + 1,
                    deliveredIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or
                            PendingIntent.FLAG_IMMUTABLE
                )

            val partes =
                smsManager.divideMessage(
                    mensagemFinal
                )

            val sentIntents =
                ArrayList<PendingIntent>()

            val deliveredIntents =
                ArrayList<PendingIntent>()

            repeat(partes.size) {

                sentIntents.add(sentPI)
                deliveredIntents.add(deliveredPI)
            }

            smsManager.sendMultipartTextMessage(
                numero,
                null,
                partes,
                sentIntents,
                deliveredIntents
            )

            Handler(
                Looper.getMainLooper()
            ).postDelayed({

                val index =
                    historico.indexOfFirst {

                        it.id == smsId &&
                                it.status == "⏳ ENVIANDO"
                    }

                if (index != -1) {

                    historico[index] =
                        historico[index].copy(
                            status = "⚠️ SEM RETORNO"
                        )
                }

            }, 10000)

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Erro ao enviar SMS",
                e
            )
        }
    }
}