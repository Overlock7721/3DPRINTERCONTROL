package com.ccover.a3dprintercontrol.utils

import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.widget.Toast
import java.io.BufferedReader
import java.io.InputStreamReader

object VpnHelper {

    /**
     * Проверяет, активен ли VPN (WireGuard) на устройстве.
     */
    fun isVpnActive(context: Context): Boolean {
        val vpnServiceIntent = Intent(context, VpnService::class.java)
        val vpnService = context.getSystemService(VpnService::class.java)
        return vpnService != null
    }

    /**
     * Запускает VPN-туннель через shell-скрипт
     */
    fun startVpn(context: Context, configPath: String = "/data/data/com.ccover.a3dprintercontrol/files/wireguard.conf") {
        try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", "wg-quick up $configPath"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readLines().joinToString("\n")
            process.waitFor()
            Toast.makeText(context, "VPN запущен: $output", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка запуска VPN: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Останавливает VPN-туннель через shell-скрипт.
     */
    fun stopVpn(context: Context, configPath: String = "/data/data/com.ccover.a3dprintercontrol/files/wireguard.conf") {
        try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", "wg-quick down $configPath"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readLines().joinToString("\n")
            process.waitFor()
            Toast.makeText(context, "VPN остановлен: $output", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка остановки VPN: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
