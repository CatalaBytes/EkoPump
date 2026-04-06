package com.catalabytes.ekopump.domain.model

enum class EnergyType(
    val emoji: String,
    val labelEs: String,
    val descripcion: String
) {
    GNC("🟦", "GNC", "Gas Natural\nComprimido"),
    GNL("🟪", "GNL", "Gas Natural\nLicuado"),
    ADBLUE("🔵", "AdBlue", "Aditivo\nDiésel"),
    EV("⚡", "Eléctrico", "Vehículo\nEléctrico")
}
