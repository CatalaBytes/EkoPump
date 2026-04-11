package com.catalabytes.ekopump.domain.model

import androidx.annotation.StringRes
import com.catalabytes.ekopump.R

enum class VehicleType(
    val emoji: String,
    @get:StringRes val labelRes: Int,
    val consumoDefault: Float,
    val consumoMin: Float,
    val consumoMax: Float,
    val litrosDefault: Float,
    val litrosMin: Float,
    val litrosMax: Float,
    val quickConsumos: List<Float>,
    val quickLitros: List<Float>
) {
    TURISMO(
        emoji = "\ud83d\ude97", labelRes = R.string.vehicle_turismo,
        consumoDefault = 7f, consumoMin = 4f, consumoMax = 18f,
        litrosDefault = 40f, litrosMin = 10f, litrosMax = 80f,
        quickConsumos = listOf(5f, 7f, 9f, 12f),
        quickLitros   = listOf(20f, 30f, 40f, 50f)
    ),
    MOTO(
        emoji = "\ud83c\udfc5", labelRes = R.string.vehicle_moto,
        consumoDefault = 5f, consumoMin = 3f, consumoMax = 12f,
        litrosDefault = 15f, litrosMin = 5f, litrosMax = 25f,
        quickConsumos = listOf(3f, 5f, 7f, 10f),
        quickLitros   = listOf(8f, 12f, 15f, 20f)
    ),
    FURGONETA(
        emoji = "\ud83d\ude90", labelRes = R.string.vehicle_furgoneta,
        consumoDefault = 12f, consumoMin = 7f, consumoMax = 22f,
        litrosDefault = 80f, litrosMin = 40f, litrosMax = 120f,
        quickConsumos = listOf(9f, 12f, 15f, 18f),
        quickLitros   = listOf(50f, 70f, 90f, 110f)
    ),
    CAMION(
        emoji = "\ud83d\ude9b", labelRes = R.string.vehicle_camion,
        consumoDefault = 30f, consumoMin = 18f, consumoMax = 50f,
        litrosDefault  = 400f, litrosMin = 100f, litrosMax = 800f,
        quickConsumos = listOf(22f, 28f, 35f, 42f),
        quickLitros   = listOf(200f, 300f, 500f, 700f)
    ),
    AUTOBUS(
        emoji = "\ud83d\ude8c", labelRes = R.string.vehicle_autobus,
        consumoDefault = 25f, consumoMin = 15f, consumoMax = 45f,
        litrosDefault  = 250f, litrosMin = 100f, litrosMax = 500f,
        quickConsumos = listOf(18f, 22f, 28f, 35f),
        quickLitros   = listOf(150f, 200f, 300f, 400f)
    ),
    ELECTRICO(
        emoji = "\u26a1", labelRes = R.string.vehicle_electrico,
        consumoDefault = 20f, consumoMin = 10f, consumoMax = 30f,
        litrosDefault = 50f, litrosMin = 20f, litrosMax = 100f,
        quickConsumos = listOf(14f, 18f, 22f, 28f),
        quickLitros   = listOf(30f, 40f, 60f, 80f)
    )
}
