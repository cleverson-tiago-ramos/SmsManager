package com.cleverson.smsmanager.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cleverson.smsmanager.data.model.HistoricoSMS

@Composable
fun ItemHistorico(
    item: HistoricoSMS
) {

    var pinDigitado by remember {
        mutableStateOf("")
    }

    var statusValidacao by remember {
        mutableStateOf("")
    }

    ElevatedCard(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Column(
            modifier =
                Modifier.padding(16.dp)
        ) {

            Text(
                text =
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


            // INPUT PIN
            OutlinedTextField(

                value = pinDigitado,

                onValueChange = {

                    pinDigitado =
                        it.filter { c ->
                            c.isDigit()
                        }
                },

                label = {

                    Text("Validar PIN")
                },

                singleLine = true,

                modifier =
                    Modifier.fillMaxWidth()
            )

            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )

            Button(

                onClick = {

                    statusValidacao =

                        if (
                            pinDigitado == item.pin
                        ) {

                            "✅ PIN VÁLIDO"

                        } else {

                            "❌ PIN INVÁLIDO"
                        }
                },

                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text("Validar PIN")
            }

            if (
                statusValidacao.isNotEmpty()
            ) {

                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )

                Text(
                    text = statusValidacao
                )
            }

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            Text(item.status)

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            Text(
                "🕒 ${item.horario}"
            )
        }
    }
}