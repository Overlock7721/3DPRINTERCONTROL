package com.ccover.a3dprintercontrol.api.models

/**
 * Модель данных для уведомлений о событиях в системе мониторинга 3D-принтеров.
 */
data class Notification(
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: NotificationType = NotificationType.INFO
)

/**
 * Типы уведомлений для дифференциации важности и оформления.
 */
enum class NotificationType {
    INFO,       // Информационное сообщение (например, успешное завершение задания)
    WARNING,    // Предупреждение (например, высокая температура)
    ERROR,      // Ошибка или авария (например, обрыв нити, перегрев)
    SUCCESS     // Успешное действие (например, параметры сохранены)
}
