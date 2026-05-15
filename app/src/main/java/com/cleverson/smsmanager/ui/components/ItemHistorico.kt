package com.cleverson.smsmanager.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cleverson.smsmanager.data.model.HistoricoSMS

@Composable
fun ItemHistorico(
    item: HistoricoSMS
) {

    ElevatedCard(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Column(
            modifier =
                Modifier.padding(16.dp)
        ) {

            Text(
                "📱 ${item.numero}",
                style =
                    MaterialTheme
                        .typography
                        .titleMedium
            )

            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )

            Text(item.mensagem)

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            Text("🕒 ${item.horario}")

            Spacer(
                modifier =
                    Modifier.height(4.dp)
            )

            Text(item.status)
        }
    }
}