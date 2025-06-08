package com.ccover.a3dprintercontrol.api.models

data class PrinterInfo(
    val id: String,
    val name: String,
    val bedTemp: Float,
    val nozzleTemp: Float,
    val printTime: String,
    val printStatus: String
)
