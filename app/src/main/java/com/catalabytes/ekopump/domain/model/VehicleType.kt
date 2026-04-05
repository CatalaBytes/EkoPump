package com.catalabytes.ekopump.domain.model

enum class VehicleType(
    val emoji: String,
    val labelEs: String,
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
        emoji = "🚗", labelEs = "Turismo",
        consumoDefault = 7f, consumoMin = 4f, consumoMax = 18f,
        litrosDefault = 40f, litrosMin = 10f, litrosMax = 80f,
        quickConsumos = listOf(5f, 7f, 9f, 12f),
        quickLitros   = listOf(20f, 30f, 40f, 50f)
    ),
    FURGONETA(
        emoji = "🚐", labelEs = "Furgoneta",
        consumoDefault = 12f, consumoMin = 7f, consumoMax = 22f,
        litrosDefault = 80f, litrosMin = 40f, litrosMax = 120f,
        quickConsumos = listOf(9f, 12f, 15f, 18f),
        quickLitros   = listOf(50f, 70f, 90f, 110f)
    ),
    CAMION(
        emoji = "🚛", labelEs = "Camión",
        consumoDefault = 30f, consumoMin = 18f, consumoMax = 50f,
        litrosDefault  = 400f, litrosMin = 100f, litrosMax = 800f,
        quickConsumos = listOf(22f, 28f, 35f, 42f),
        quickLitros   = listOf(200f, 300f, 500f, 700f)
    ),
    AUTOBUS(
        emoji = "🚌", labelEs = "Autobús",
        consumoDefault = 25f, consumoMin = 15f, consumoMax = 45f,
        litrosDefault  = 250f, litrosMin = 100f, litrosMax = 500f,
        quickConsumos = listOf(18f, 22f, 28f, 35f),
        quickLitros   = listOf(150f, 200f, 300f, 400f)
    )
}
