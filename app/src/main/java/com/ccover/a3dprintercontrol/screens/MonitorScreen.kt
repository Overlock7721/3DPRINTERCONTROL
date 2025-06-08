package com.ccover.a3dprintercontrol.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ccover.a3dprintercontrol.R
import com.ccover.a3dprintercontrol.api.models.PrinterInfo
import com.ccover.a3dprintercontrol.utils.NotificationHelper
import com.vicochart.vico.compose.Chart
import com.vicochart.vico.compose.axis.horizontal.rememberBottomAxis
import com.vicochart.vico.compose.axis.vertical.rememberStartAxis
import com.vicochart.vico.compose.chart.line.lineChart
import com.vicochart.vico.core.entry.ChartEntryModelProducer
import com.vicochart.vico.core.entry.FloatEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitorScreen(
    printer: PrinterInfo,
    timeRange: TimeRange = TimeRange.ALL,
    onTimeRangeChange: (TimeRange) -> Unit,
    onEmergencyStop: () -> Unit,
    onSettings: () -> Unit,
    temperatureHistory: List<FloatEntry>
) {
    val context = LocalContext.current
    var showConfirmDialog by remember { mutableStateOf(false) }

    // Создаём канал уведомлений при запуске экрана
    LaunchedEffect(Unit) {
        NotificationHelper.createChannel(context)
    }

    // Проверка критических событий и вызов уведомления
    LaunchedEffect(printer) {
        if (printer.nozzleTemp > 250) {
            NotificationHelper.showNotification(
                context,
                title = "Перегрев сопла",
                message = "Температура сопла превысила 250°C на принтере ${printer.name}!"
            )
        }
        if (printer.printStatus == "Ошибка" || printer.printStatus == "Печать остановлена" || printer.printStatus == "Авария") {
            NotificationHelper.showNotification(
                context,
                title = "Сбой печати",
                message = "На принтере ${printer.name} печать остановлена или возникла ошибка."
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Заголовок экрана с именем принтера
        Text(
            text = printer.name,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Температура стола и сопла
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TemperatureCard(
                title = stringResource(R.string.bed_temp),
                value = "${printer.bedTemp} °C"
            )
            TemperatureCard(
                title = stringResource(R.string.nozzle_temp),
                value = "${printer.nozzleTemp} °C"
            )
        }

        // Время печати
        Text(
            text = stringResource(R.string.print_time, printer.printTime),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Выбор временного интервала для графика
        TimeRangeSelector(
            selectedRange = timeRange,
            onRangeSelected = onTimeRangeChange
        )

        // График температуры сопла
        if (temperatureHistory.isNotEmpty()) {
            TemperatureChart(
                entries = temperatureHistory,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(vertical = 8.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_data),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Статус печати с цветовой индикацией
        val statusColor = when (printer.printStatus) {
            "Идёт печать" -> Color(0xFF00C853)
            "Ожидание" -> Color(0xFFFFC107)
            "Печать остановлена", "Ошибка", "Авария" -> Color(0xFFD32F2F)
            else -> MaterialTheme.colorScheme.primary
        }
        Text(
            text = stringResource(R.string.print_status, printer.printStatus),
            color = statusColor,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Кнопка экстренной остановки с двойным подтверждением
        Button(
            onClick = { showConfirmDialog = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(stringResource(R.string.emergency_stop))
        }

        // Кнопка настройки параметров
        Button(
            onClick = onSettings,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.settings))
        }
    }

    // Диалог подтверждения экстренной остановки
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Подтвердите остановку") },
            text = { Text("Вы действительно хотите экстренно остановить печать на принтере ${printer.name}?") },
            confirmButton = {
                Button(
                    onClick = {
                        onEmergencyStop()
                        showConfirmDialog = false
                        Toast.makeText(
                            LocalContext.current,
                            "Печать остановлена",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                ) {
                    Text("Остановить")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

// Карточка для отображения температуры.
@Composable
fun TemperatureCard(
    title: String,
    value: String
) {
    Card(
        modifier = Modifier.width(150.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}

// Селектор временного интервала для графика.
@Composable
fun TimeRangeSelector(
    selectedRange: TimeRange,
    onRangeSelected: (TimeRange) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TimeRange.values().forEach { range ->
            FilterChip(
                selected = selectedRange == range,
                onClick = { onRangeSelected(range) },
                label = { Text(range.displayName) },
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

// График температуры сопла (Vico Chart).
@Composable
fun TemperatureChart(
    entries: List<FloatEntry>,
    modifier: Modifier = Modifier
) {
    val entryModelProducer = remember { ChartEntryModelProducer(entries) }
    val bottomAxis = rememberBottomAxis()
    val startAxis = rememberStartAxis()
    Chart(
        chart = lineChart(),
        chartModelProducer = entryModelProducer,
        modifier = modifier,
        bottomAxis = bottomAxis,
        startAxis = startAxis
    )
}

// Перечисление для выбора временного интервала графика.
enum class TimeRange(val displayName: String) {
    ALL("За всё время"),
    LAST_30_MIN("30 мин"),
    LAST_HOUR("1 час"),
    LAST_12_HOURS("12 часов")
}
