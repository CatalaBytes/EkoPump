package com.catalabytes.ekopump.data.api.ocm

import com.catalabytes.ekopump.data.local.entity.EvChargerEntity
import com.catalabytes.ekopump.domain.model.EvCharger
import com.catalabytes.ekopump.domain.model.EvConexion

fun OcmPoi.toDomain(): EvCharger = EvCharger(
    id = id,
    nombre = addressInfo.title,
    direccion = addressInfo.addressLine1,
    localidad = addressInfo.town,
    latitud = addressInfo.latitude,
    longitud = addressInfo.longitude,
    distanciaKm = addressInfo.distance,
    operador = operatorInfo?.title,
    totalPuntos = numberOfPoints,
    esOperacional = statusType?.isOperational ?: false,
    esPublico = usageType?.id == 1,
    coste = usageCost,
    conexiones = connections?.map { it.toDomain() } ?: emptyList()
)

private fun OcmConnection.toDomain(): EvConexion = EvConexion(
    tipoConector = connectionType?.title ?: "Desconocido",
    potenciaKw = powerKw,
    cantidad = quantity,
    esCargaRapida = level?.isFastChargeCapable ?: false
)

fun EvCharger.toEntity(cachedAt: Long): EvChargerEntity = EvChargerEntity(
    id = id,
    nombre = nombre,
    direccion = direccion,
    localidad = localidad,
    latitud = latitud,
    longitud = longitud,
    distanciaKm = distanciaKm,
    operador = operador,
    totalPuntos = totalPuntos,
    esOperacional = esOperacional,
    esPublico = esPublico,
    coste = coste,
    conexiones = conexiones,
    cachedAt = cachedAt
)

fun EvChargerEntity.toDomain(): EvCharger = EvCharger(
    id = id,
    nombre = nombre,
    direccion = direccion,
    localidad = localidad,
    latitud = latitud,
    longitud = longitud,
    distanciaKm = distanciaKm,
    operador = operador,
    totalPuntos = totalPuntos,
    esOperacional = esOperacional,
    esPublico = esPublico,
    coste = coste,
    conexiones = conexiones
)
