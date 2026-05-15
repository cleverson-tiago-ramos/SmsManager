package com.cleverson.smsmanager.data.model

data class HistoricoSMS(

    val id: Long,

    val numero: String,

    val mensagem: String,

    val horario: String,

    val status: String
)