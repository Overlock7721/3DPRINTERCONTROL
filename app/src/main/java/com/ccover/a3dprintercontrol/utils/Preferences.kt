package com.ccover.a3dprintercontrol.utils

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

object PreferencesKeys {
    val SERVER_IP = stringPreferencesKey("server_ip")
    val API_KEY = stringPreferencesKey("api_key")
    val PRINTERS = stringSetPreferencesKey("printers") // Список принтеров в формате "id|ip|name"
    val SELECTED_PRINTER_ID = stringPreferencesKey("selected_printer_id")
}

data class PrinterPrefs(
    val id: String,
    val ip: String,
    val name: String
)

class Preferences(private val context: Context) {

    // Получение IP сервера
    val serverIp: Flow<String> = context.dataStore.data
        .map { it[PreferencesKeys.SERVER_IP] ?: "" }

    // Получение API-ключа
    val apiKey: Flow<String> = context.dataStore.data
        .map { it[PreferencesKeys.API_KEY] ?: "" }

    // Получение выбранного принтера
    val selectedPrinterId: Flow<String> = context.dataStore.data
        .map { it[PreferencesKeys.SELECTED_PRINTER_ID] ?: "" }

    // Получение списка принтеров
    val printers: Flow<List<PrinterPrefs>> = context.dataStore.data
        .map { prefs ->
            prefs[PreferencesKeys.PRINTERS]?.mapNotNull { entry ->
                val parts = entry.split("|")
                if (parts.size == 3) PrinterPrefs(parts[0], parts[1], parts[2]) else null
            } ?: emptyList()
        }

    // Сохранить IP сервера
    suspend fun saveServerIp(ip: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.SERVER_IP] = ip
        }
    }

    // Сохранить API-ключ
    suspend fun saveApiKey(key: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.API_KEY] = key
        }
    }

    // Сохранить выбранный принтер
    suspend fun saveSelectedPrinterId(printerId: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.SELECTED_PRINTER_ID] = printerId
        }
    }

    // Добавить принтер в список
    suspend fun addPrinter(printer: PrinterPrefs) {
        context.dataStore.edit { prefs ->
            val current = prefs[PreferencesKeys.PRINTERS]?.toMutableSet() ?: mutableSetOf()
            current.add("${printer.id}|${printer.ip}|${printer.name}")
            prefs[PreferencesKeys.PRINTERS] = current
        }
    }

    // Удалить принтер из списка
    suspend fun removePrinter(printerId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[PreferencesKeys.PRINTERS]?.toMutableSet() ?: mutableSetOf()
            val updated = current.filterNot { it.startsWith("$printerId|") }.toSet()
            prefs[PreferencesKeys.PRINTERS] = updated
        }
    }

    // Очистить все настройки (например, при выходе пользователя)
    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
