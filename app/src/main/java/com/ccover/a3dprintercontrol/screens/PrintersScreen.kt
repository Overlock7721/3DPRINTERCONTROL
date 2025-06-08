package com.ccover.a3dprintercontrol.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ccover.a3dprintercontrol.api.models.PrinterInfo

@Composable
fun PrintersScreen(
    printers: List<PrinterInfo>,
    onAddPrinter: (PrinterInfo) -> Unit,
    onSelectPrinter: (PrinterInfo) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var newPrinterIp by remember { mutableStateOf("") }
    var newPrinterName by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Text("+")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Список принтеров",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            if (printers.isEmpty()) {
                Text(
                    text = "Нет доступных принтеров",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                printers.forEach { printer ->
                    PrinterCard(
                        printer = printer,
                        onClick = { onSelectPrinter(printer) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        if (showAddDialog) {
            Dialog(onDismissRequest = { showAddDialog = false }) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 8.dp,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Добавить принтер", style = MaterialTheme.typography.titleLarge)
                        OutlinedTextField(
                            value = newPrinterIp,
                            onValueChange = { newPrinterIp = it },
                            label = { Text("IP-адрес") },
                            keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(keyboardType = KeyboardType.Uri),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newPrinterName,
                            onValueChange = { newPrinterName = it },
                            label = { Text("Имя принтера") },
                            singleLine = true
                        )
                        if (showError) {
                            Text(
                                text = "Введите корректные данные",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showAddDialog = false }) {
                                Text("Отмена")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = {
                                if (newPrinterIp.isNotBlank() && newPrinterName.isNotBlank()) {
                                    val newPrinter = PrinterInfo(
                                        id = System.currentTimeMillis().toString(),
                                        name = newPrinterName,
                                        bedTemp = 0f,
                                        nozzleTemp = 0f,
                                        printTime = "0 ч 00 мин",
                                        printStatus = "Ожидание"
                                    )
                                    onAddPrinter(newPrinter)
                                    newPrinterIp = ""
                                    newPrinterName = ""
                                    showError = false
                                    showAddDialog = false
                                } else {
                                    showError = true
                                }
                            }) {
                                Text("Добавить")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Карточка принтера с цветовой индикацией статуса.
 */
@Composable
fun PrinterCard(
    printer: PrinterInfo,
    onClick: () -> Unit
) {
    val statusColor = when (printer.printStatus) {
        "Идёт печать" -> Color(0xFF00C853)
        "Ожидание" -> Color(0xFFFFC107)
        "Печать остановлена", "Ошибка", "Авария" -> Color(0xFFD32F2F)
        else -> MaterialTheme.colorScheme.primary
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = statusColor,
                        shape = CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = printer.name,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = printer.printStatus,
                color = statusColor,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
