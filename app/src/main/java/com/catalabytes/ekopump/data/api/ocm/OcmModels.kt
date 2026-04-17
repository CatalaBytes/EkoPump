package com.catalabytes.ekopump.data.api.ocm

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OcmPoi(
    @Json(name = "ID") val id: Int,
    @Json(name = "AddressInfo") val addressInfo: OcmAddressInfo,
    @Json(name = "Connections") val connections: List<OcmConnection>? = null,
    @Json(name = "NumberOfPoints") val numberOfPoints: Int? = null,
    @Json(name = "StatusType") val statusType: OcmStatusType? = null,
    @Json(name = "UsageType") val usageType: OcmUsageType? = null,
    @Json(name = "UsageCost") val usageCost: String? = null,
    @Json(name = "OperatorInfo") val operatorInfo: OcmOperatorInfo? = null
)

@JsonClass(generateAdapter = true)
data class OcmAddressInfo(
    @Json(name = "Title") val title: String,
    @Json(name = "AddressLine1") val addressLine1: String? = null,
    @Json(name = "Town") val town: String? = null,
    @Json(name = "Latitude") val latitude: Double,
    @Json(name = "Longitude") val longitude: Double,
    @Json(name = "Distance") val distance: Double? = null
)

@JsonClass(generateAdapter = true)
data class OcmConnection(
    @Json(name = "ConnectionType") val connectionType: OcmConnectionType? = null,
    @Json(name = "Level") val level: OcmLevel? = null,
    @Json(name = "PowerKW") val powerKw: Double? = null,
    @Json(name = "Quantity") val quantity: Int? = null
)

@JsonClass(generateAdapter = true)
data class OcmConnectionType(
    @Json(name = "Title") val title: String? = null
)

@JsonClass(generateAdapter = true)
data class OcmLevel(
    @Json(name = "IsFastChargeCapable") val isFastChargeCapable: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class OcmStatusType(
    @Json(name = "IsOperational") val isOperational: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class OcmUsageType(
    @Json(name = "ID") val id: Int? = null
)

@JsonClass(generateAdapter = true)
data class OcmOperatorInfo(
    @Json(name = "Title") val title: String? = null
)
