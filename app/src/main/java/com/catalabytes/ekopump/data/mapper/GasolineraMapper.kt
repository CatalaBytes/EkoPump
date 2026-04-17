package com.catalabytes.ekopump.data.mapper

import com.catalabytes.ekopump.data.api.EstacionDto
import com.catalabytes.ekopump.data.local.entity.GasolineraEntity
import com.catalabytes.ekopump.domain.model.Gasolinera

fun EstacionDto.toDomain(): Gasolinera = Gasolinera(
    id = id,
    nombre = rotulo,
    direccion = direccion,
    localidad = localidad,
    provincia = provincia,
    latitud = latitud.replace(",", ".").toDoubleOrNull() ?: 0.0,
    longitud = longitud.replace(",", ".").toDoubleOrNull() ?: 0.0,
    horario = horario,
    gasolina95 = precioGasolina95.replace(",", ".").toDoubleOrNull(),
    gasolina98 = precioGasolina98.replace(",", ".").toDoubleOrNull(),
    gasoleoA = precioGasoleoA.replace(",", ".").toDoubleOrNull(),
    gasoleoB = precioGasoleoB.replace(",", ".").toDoubleOrNull(),
    gasoleoPremium = precioGasoleoPremium.replace(",", ".").toDoubleOrNull(),
    glp = precioGLP.replace(",", ".").toDoubleOrNull(),
    gnc = precioGNC.replace(",", ".").toDoubleOrNull(),
    gnl = precioGNL.replace(",", ".").toDoubleOrNull()
)

fun Gasolinera.toEntity(cachedAt: Long): GasolineraEntity = GasolineraEntity(
    id = id,
    nombre = nombre,
    direccion = direccion,
    localidad = localidad,
    provincia = provincia,
    latitud = latitud,
    longitud = longitud,
    horario = horario,
    gasolina95 = gasolina95,
    gasolina98 = gasolina98,
    gasoleoA = gasoleoA,
    gasoleoB = gasoleoB,
    gasoleoPremium = gasoleoPremium,
    glp = glp,
    gnc = gnc,
    gnl = gnl,
    cachedAt = cachedAt
)

fun GasolineraEntity.toDomain(): Gasolinera = Gasolinera(
    id = id,
    nombre = nombre,
    direccion = direccion,
    localidad = localidad,
    provincia = provincia,
    latitud = latitud,
    longitud = longitud,
    horario = horario,
    gasolina95 = gasolina95,
    gasolina98 = gasolina98,
    gasoleoA = gasoleoA,
    gasoleoB = gasoleoB,
    gasoleoPremium = gasoleoPremium,
    glp = glp,
    gnc = gnc,
    gnl = gnl
)
