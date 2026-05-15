package com.cleverson.smsmanager.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.cleverson.smsmanager.data.model.HistoricoSMS

class SmsViewModel : ViewModel() {

    val historico =
        mutableStateListOf<HistoricoSMS>()
}