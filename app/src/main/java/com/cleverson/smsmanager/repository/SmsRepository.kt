package com.cleverson.smsmanager.repository

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.util.Log
import com.cleverson.smsmanager.data.model.HistoricoSMS
import com.cleverson.smsmanager.utils.SMS_DELIVERED
import com.cleverson.smsmanager.utils.SMS_SENT
import com.cleverson.smsmanager.utils.TAG
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

class SmsRepository(
    private val context: Context
) {

    private val requestCodeCounter =
        AtomicInteger(0)

    fun enviarSMS(
        numero: String,
        mensagem: String,
        historico: MutableList<HistoricoSMS>
    ) {

        try {

            Log.d(
                TAG,
                "Enviando SMS para: $numero"
            )

            val smsManager =
                context.getSystemService(
                    SmsManager::class.java
                )

            val smsId =
                System.currentTimeMillis()

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
                    mensagem = mensagem,
                    horario = horarioAtual,
                    status = "⏳ ENVIANDO"
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
                    requestCodeCounter.incrementAndGet(),
                    sentIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or
                            PendingIntent.FLAG_IMMUTABLE
                )

            val deliveredPI =
                PendingIntent.getBroadcast(
                    context,
                    requestCodeCounter.incrementAndGet(),
                    deliveredIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or
                            PendingIntent.FLAG_IMMUTABLE
                )

            val partes =
                smsManager.divideMessage(
                    mensagem
                )

            if (partes.size > 1) {

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

            } else {

                smsManager.sendTextMessage(
                    numero,
                    null,
                    mensagem,
                    sentPI,
                    deliveredPI
                )
            }

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