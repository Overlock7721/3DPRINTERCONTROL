package com.ccover.a3dprintercontrol.api.models

import kotlinx.serialization.Serializable

@Serializable
data class ObjectsQueryResponse(
    val result: Result
) {
    @Serializable
    data class Result(
        val status: String,
        val heater_bed: HeaterBed,
        val toolhead: Toolhead,
        val print_stats: PrintStats
    )

    @Serializable
    data class HeaterBed(val temperature: Float, val target: Float)
    @Serializable
    data class Toolhead(val position: List<Float>, val print_time: Float)
    @Serializable
    data class PrintStats(val filename: String, val progress: Float)
}
