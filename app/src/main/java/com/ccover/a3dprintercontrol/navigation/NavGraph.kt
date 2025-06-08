package com.ccover.a3dprintercontrol.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ccover.a3dprintercontrol.screens.AuthScreen
import com.ccover.a3dprintercontrol.screens.PrintersScreen
import com.ccover.a3dprintercontrol.screens.MonitorScreen
import com.ccover.a3dprintercontrol.screens.SettingsScreen
import com.ccover.a3dprintercontrol.api.models.PrinterInfo
import com.ccover.a3dprintercontrol.api.MoonrakerApi
import com.ccover.a3dprintercontrol.utils.Preferences
import com.vicochart.vico.core.entry.FloatEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun formatPrintTime(seconds: Float): String {
    val totalSeconds = seconds.toInt()
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val secs = totalSeconds % 60
    return "${hours} ч ${minutes} мин ${secs} сек"
}


@Composable
fun NavGraph(navController: NavHostController) {
    val context = LocalContext.current
    val prefs = remember { Preferences(context) }

    // Состояния авторизации и API
    var isAuthorized by remember { mutableStateOf(false) }
    var serverIp by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }
    var moonrakerApi by remember { mutableStateOf<MoonrakerApi?>(null) }

    // Список принтеров и выбранный принтер
    var printers by remember { mutableStateOf(listOf<PrinterInfo>()) }
    var selectedPrinter by remember { mutableStateOf<PrinterInfo?>(null) }

    // Телеметрия и история температур
    var printerTelemetry by remember { mutableStateOf<PrinterInfo?>(null) }
    var temperatureHistory by remember { mutableStateOf<List<FloatEntry>>(emptyList()) }
    var timeRange by remember { mutableStateOf(MonitorScreen.TimeRange.ALL) }

    // Загрузка принтеров из Preferences
    LaunchedEffect(isAuthorized) {
        if (isAuthorized) {
            prefs.printers.collect { list ->
                printers = list
            }
        }
    }

    NavHost(navController, startDestination = if (isAuthorized) "printers" else "auth") {
        composable("auth") {
            AuthScreen(
                onAuthSuccess = { inputIp, inputKey ->
                    serverIp = inputIp
                    apiKey = inputKey
                    moonrakerApi = MoonrakerApi(serverIp, apiKey)
                    isAuthorized = true
                    CoroutineScope(Dispatchers.IO).launch {
                        prefs.saveServerIp(serverIp)
                        prefs.saveApiKey(apiKey)
                    }
                    navController.navigate("printers") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }
        composable("printers") {
            PrintersScreen(
                printers = printers,
                onAddPrinter = { newPrinter ->
                    CoroutineScope(Dispatchers.IO).launch {
                        prefs.addPrinter(newPrinter)
                    }
                },
                onSelectPrinter = { printer ->
                    selectedPrinter = printer
                    navController.navigate("monitor")
                }
            )
        }
        composable("monitor") {
            val printer = selectedPrinter
            if (printer != null && moonrakerApi != null) {
                // Загружаем телеметрию и историю температур
                LaunchedEffect(printer.id, timeRange) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val telemetry = moonrakerApi!!.getPrinterStatus(printer.id)
                        printerTelemetry = PrinterInfo(
                            id = printer.id,
                            name = printer.name,
                            bedTemp = telemetry.result.heater_bed.temperature,
                            nozzleTemp = telemetry.result.toolhead.temperature,
                            printTime = formatPrintTime(telemetry.result.toolhead.print_time),
                            printStatus = telemetry.result.status
                        )
                        val tempList = moonrakerApi!!.getTemperatureHistory(printer.id, timeRange.name)
                        temperatureHistory = tempList.mapIndexed { idx, temp -> FloatEntry(idx.toFloat(), temp) }
                    }
                }
                MonitorScreen(
                    printer = printerTelemetry ?: printer,
                    timeRange = timeRange,
                    onTimeRangeChange = { newRange -> timeRange = newRange },
                    onEmergencyStop = {
                        CoroutineScope(Dispatchers.IO).launch {
                            moonrakerApi!!.emergencyStop(printer.id)
                        }
                    },
                    onSettings = { navController.navigate("settings") },
                    temperatureHistory = temperatureHistory
                )
            }
        }
        composable("settings") {
            val printer = selectedPrinter
            SettingsScreen(
                onSave = { bedTemp, nozzleTemp, printSpeed ->
                    if (printer != null && moonrakerApi != null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            moonrakerApi!!.setPrinterSettings(printer.id, bedTemp, nozzleTemp, printSpeed)
                        }
                    }
                    navController.popBackStack()
                },
                onReset = {
                    if (printer != null && moonrakerApi != null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            moonrakerApi!!.resetPrinterSettings(printer.id)
                        }
                    }
                }
            )
        }
    }
}
