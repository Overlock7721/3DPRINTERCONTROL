package com.ccover.a3dprintercontrol.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import com.ccover.a3dprintercontrol.api.models.ObjectsQueryResponse

class MoonrakerApi(
    private val baseUrl: String,
    private val apiKey: String
) {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; explicitNulls = false })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 7000
            connectTimeoutMillis = 5000
            socketTimeoutMillis = 5000
        }
        expectSuccess = true
    }

    // Получение статуса и телеметрии принтера
    suspend fun getPrinterStatus(printerId: String): ObjectsQueryResponse {
        val url = "$baseUrl/printer/objects/query?heater_bed,toolhead,print_stats"
        return client.get(url) {
            header("X-Api-Key", apiKey)
            contentType(ContentType.Application.Json)
        }.body()
    }

    // Экстренная остановка печати
    suspend fun emergencyStop(printerId: String): Boolean {
        val url = "$baseUrl/printer/print/cancel"
        return try {
            client.post(url) {
                header("X-Api-Key", apiKey)
                contentType(ContentType.Application.Json)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    // Изменение настроек печати
    suspend fun setPrinterSettings(
        printerId: String,
        bedTemp: Int,
        nozzleTemp: Int,
        printSpeed: Int
    ): Boolean {
        val url = "$baseUrl/printer/gcode/script"
        val script = "SET_HEATER_BED_TEMPERATURE TARGET=$bedTemp\nSET_EXTRUDER_TEMPERATURE TARGET=$nozzleTemp\nSET_PRINT_SPEED SPEED=$printSpeed"
        return try {
            client.post(url) {
                header("X-Api-Key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(mapOf("script" to script))
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    // Сброс настроек принтера к заводским
    suspend fun resetPrinterSettings(printerId: String): Boolean {
        val url = "$baseUrl/printer/gcode/script"
        val script = "RESTORE_DEFAULTS"
        return try {
            client.post(url) {
                header("X-Api-Key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(mapOf("script" to script))
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    // Получение истории температуры для графика
    suspend fun getTemperatureHistory(printerId: String, interval: String): List<Float> {
        val url = "$baseUrl/printer/temperature/history?interval=$interval"
        return try {
            client.get(url) {
                header("X-Api-Key", apiKey)
                contentType(ContentType.Application.Json)
            }.body<List<Float>>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Получение списка принтеров
    suspend fun getPrinters(): Map<String, String> {
        val url = "$baseUrl/printers"
        return try {
            client.get(url) {
                header("X-Api-Key", apiKey)
                contentType(ContentType.Application.Json)
            }.body<Map<String, String>>()
        } catch (e: Exception) {
            mapOf(
                "1" to "CCOVER_Printer_01",
                "2" to "CCOVER_Printer_02",
                "3" to "CCOVER_Printer_03"
            )
        }
    }
}
