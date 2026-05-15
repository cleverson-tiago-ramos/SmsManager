package com.cleverson.smsmanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.cleverson.smsmanager.data.model.HistoricoSMS
import com.cleverson.smsmanager.data.model.Pais
import com.cleverson.smsmanager.ui.components.ItemHistorico

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaSMS(
    historico: List<HistoricoSMS>,
    onEnviarSMS: (
        String,
        String
    ) -> Unit
) {

    val paises = listOf(

        Pais(
            nome = "Brasil",
            codigo = "+55",
            bandeira = "🇧🇷"
        ),

        Pais(
            nome = "Estados Unidos",
            codigo = "+1",
            bandeira = "🇺🇸"
        ),

        Pais(
            nome = "Portugal",
            codigo = "+351",
            bandeira = "🇵🇹"
        ),

        Pais(
            nome = "Argentina",
            codigo = "+54",
            bandeira = "🇦🇷"
        )
    )

    var paisSelecionado by remember {
        mutableStateOf(paises[0])
    }

    var expandirPaises by remember {
        mutableStateOf(false)
    }

    var numero by remember {
        mutableStateOf("")
    }

    var mensagem by remember {
        mutableStateOf("")
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp)
    ) {

        Text(
            text = "SMS Manager",

            style =
                MaterialTheme
                    .typography
                    .headlineLarge
        )

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )

        // TELEFONE + PAÍS
        Row(

            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            // PAÍS
            ExposedDropdownMenuBox(

                expanded =
                    expandirPaises,

                onExpandedChange = {

                    expandirPaises =
                        !expandirPaises
                },

                modifier =
                    Modifier.width(120.dp)
            ) {

                OutlinedTextField(

                    value =
                        "${paisSelecionado.bandeira} ${paisSelecionado.codigo}",

                    onValueChange = {},

                    readOnly = true,

                    singleLine = true,

                    label = {

                        Text("País")
                    },

                    modifier =
                        Modifier
                            .menuAnchor()
                            .fillMaxWidth(),

                    trailingIcon = {

                        ExposedDropdownMenuDefaults
                            .TrailingIcon(
                                expanded =
                                    expandirPaises
                            )
                    }
                )
                ExposedDropdownMenu(

                    expanded =
                        expandirPaises,

                    onDismissRequest = {

                        expandirPaises = false
                    }
                ) {

                    paises.forEach { pais ->

                        DropdownMenuItem(

                            text = {

                                Text(
                                    "${pais.bandeira} ${pais.nome}"
                                )
                            },

                            onClick = {

                                paisSelecionado = pais
                                expandirPaises = false
                            }
                        )
                    }
                }
            }

            // TELEFONE
            OutlinedTextField(

                value = numero,

                onValueChange = {

                    numero =
                        it.filter { c ->
                            c.isDigit()
                        }
                },

                label = {

                    Text("Telefone")
                },

                singleLine = true,

                modifier =
                    Modifier.weight(1f),

                keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                            KeyboardType.Phone
                    )
            )
        }

        Spacer(
            modifier =
                Modifier.height(20.dp)
        )

        // MENSAGEM
        OutlinedTextField(

            value = mensagem,

            onValueChange = {

                if (it.length <= 160) {

                    mensagem = it
                }
            },

            label = {

                Text("Mensagem")
            },

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(120.dp),

            maxLines = 6
        )

        Spacer(
            modifier =
                Modifier.height(8.dp)
        )

        LinearProgressIndicator(

            progress = {
                mensagem.length / 160f
            },

            modifier =
                Modifier.fillMaxWidth()
        )

        Spacer(
            modifier =
                Modifier.height(4.dp)
        )

        Text(
            text =
                "${mensagem.length}/160 caracteres",

            style =
                MaterialTheme
                    .typography
                    .bodySmall
        )

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )

        // BOTÃO
        Button(

            onClick = {

                if (
                    numero.isNotEmpty() &&
                    mensagem.isNotEmpty()
                ) {

                    val numeroCompleto =
                        "${paisSelecionado.codigo}$numero"

                    onEnviarSMS(
                        numeroCompleto,
                        mensagem
                    )

                    numero = ""
                    mensagem = ""
                }
            },

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
        ) {

            Text(
                text = "Enviar SMS"
            )
        }

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )

        Text(
            text = "Histórico de SMS",

            style =
                MaterialTheme
                    .typography
                    .headlineSmall
        )

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        LazyColumn(

            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            items(historico) { item ->

                ItemHistorico(item)
            }
        }
    }
}