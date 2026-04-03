package com.catalabytes.ekopump.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MineturResponse(
    @Json(name = "Fecha") val fecha: String,
    @Json(name = "ListaEESSPrecio") val listaEESSPrecio: List<EstacionDto>,
    @Json(name = "ResultadoConsulta") val resultadoConsulta: String
)

@JsonClass(generateAdapter = true)
data class EstacionDto(
    @Json(name = "IDEESS") val id: String,
    @Json(name = "Rótulo") val rotulo: String,
    @Json(name = "Dirección") val direccion: String,
    @Json(name = "Localidad") val localidad: String,
    @Json(name = "Provincia") val provincia: String,
    @Json(name = "C.P.") val codigoPostal: String,
    @Json(name = "Latitud") val latitud: String,
    @Json(name = "Longitud (WGS84)") val longitud: String,
    @Json(name = "Horario") val horario: String,
    @Json(name = "Precio Gasolina 95 E5") val precioGasolina95: String,
    @Json(name = "Precio Gasolina 98 E5") val precioGasolina98: String,
    @Json(name = "Precio Gasoleo A") val precioGasoleoA: String,
    @Json(name = "Precio Gasoleo B") val precioGasoleoB: String,
    @Json(name = "Precio Gasoleo Premium") val precioGasoleoPremium: String,
    @Json(name = "Precio Gases licuados del petróleo") val precioGLP: String,
    @Json(name = "IDProvincia") val idProvincia: String,
    @Json(name = "IDMunicipio") val idMunicipio: String
)
