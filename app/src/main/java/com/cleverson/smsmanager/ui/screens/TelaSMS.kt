package com.cleverson.smsmanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cleverson.smsmanager.data.model.HistoricoSMS
import com.cleverson.smsmanager.ui.components.ItemHistorico

data class Pais(

    val nome: String,

    val codigo: String,

    val bandeira: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaSMS(

    historico:
    SnapshotStateList<HistoricoSMS>,

    onEnviarSMS: (
        String,
        String
    ) -> Unit
) {

    // PAÍSES
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

    // ESTADOS
    var paisSelecionado by remember {

        mutableStateOf(
            paises[0]
        )
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

        // PAÍS + TELEFONE
        Row(

            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            // SELECT PAÍS
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
                    Modifier.weight(1f)
            )
        }

        Spacer(
            modifier =
                Modifier.height(12.dp)
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
                    .height(120.dp)
        )

        Spacer(
            modifier =
                Modifier.height(8.dp)
        )

        Text(
            text =
                "${mensagem.length}/160 caracteres"
        )

        Spacer(
            modifier =
                Modifier.height(20.dp)
        )

        // BOTÃO
        Button(

            onClick = {

                val numeroCompleto =
                    "${paisSelecionado.codigo}$numero"

                onEnviarSMS(
                    numeroCompleto,
                    mensagem
                )

                numero = ""
                mensagem = ""
            },

            modifier =
                Modifier.fillMaxWidth()
        ) {

            Text("Enviar SMS")
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