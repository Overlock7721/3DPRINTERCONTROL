package com.ccover.a3dprintercontrol.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    onSave: (Int, Int, Int) -> Unit,
    onReset: () -> Unit
) {
    var bedTemp by remember { mutableStateOf("") }
    var nozzleTemp by remember { mutableStateOf("") }
    var printSpeed by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Настройки печати",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        OutlinedTextField(
            value = bedTemp,
            onValueChange = { bedTemp = it },
            label = { Text("Температура стола (°C)") },
            keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = nozzleTemp,
            onValueChange = { nozzleTemp = it },
            label = { Text("Температура сопла (°C)") },
            keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = printSpeed,
            onValueChange = { printSpeed = it },
            label = { Text("Скорость печати (%)") },
            keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (showError) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Проверьте корректность введённых данных",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                val bed = bedTemp.toIntOrNull()
                val nozzle = nozzleTemp.toIntOrNull()
                val speed = printSpeed.toIntOrNull()
                if (bed != null && nozzle != null && speed != null) {
                    onSave(bed, nozzle, speed)
                    showError = false
                } else {
                    showError = true
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сохранить")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = {
                bedTemp = ""
                nozzleTemp = ""
                printSpeed = ""
                onReset()
                showError = false
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сбросить к заводским")
        }
    }
}
