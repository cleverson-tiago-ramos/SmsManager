package com.cleverson.smsmanager.repository

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
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
import com.cleverson.smsmanager.store.SmsStatusStore
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
                (System.currentTimeMillis() % Int.MAX_VALUE).toLong()

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
                Intent(
                    context,
                    com.cleverson.smsmanager.receiver.SmsSentReceiver::class.java
                ).apply {

                    action = SMS_SENT

                    putExtra(
                        "sms_id",
                        smsId
                    )
                }

            val deliveredIntent =
                Intent(
                    context,
                    com.cleverson.smsmanager.receiver.SmsDeliveredReceiver::class.java
                ).apply {

                    action = SMS_DELIVERED

                    putExtra(
                        "sms_id",
                        smsId
                    )
                }
            val flags =
                if (
                    Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.S
                ) {

                    PendingIntent.FLAG_UPDATE_CURRENT or
                            PendingIntent.FLAG_IMMUTABLE

                } else {

                    PendingIntent.FLAG_UPDATE_CURRENT
                }

            val sentPI =
                PendingIntent.getBroadcast(
                    context,
                    smsId.toInt(),
                    sentIntent,
                    flags
                )

            val deliveredPI =
                PendingIntent.getBroadcast(
                    context,
                    smsId.toInt() + 1,
                    deliveredIntent,
                    flags
                )

            // =========================
            // ENVIO SMS
            // =========================

            if (mensagemFinal.length > 160) {

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

            } else {
                Log.d(
                    "SMS_STATUS",
                    "Tentando enviar SMS para $numero"
                )
                smsManager.sendTextMessage(
                    numero,
                    null,
                    mensagemFinal,
                    sentPI,
                    deliveredPI
                )
            }

            // =========================
            // TIMEOUT
            // =========================

            Handler(
                Looper.getMainLooper()
            ).postDelayed({

                val index =
                    historico.indexOfFirst {

                        it.id == smsId
                    }

                if (index != -1) {

                    val novoStatus =
                        SmsStatusStore.statusMap[smsId]

                    historico[index] =
                        historico[index].copy(

                            status =
                                novoStatus
                                    ?: "⚠️ SEM RETORNO"
                        )
                }

            }, 10000)

        } catch (e: Exception) {


            Log.e(
                "SMS_STATUS",
                "Erro ao enviar SMS",
                e
            )
        }
    }
}