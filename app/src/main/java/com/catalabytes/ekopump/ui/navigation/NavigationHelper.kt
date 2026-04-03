package com.catalabytes.ekopump.ui.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri

fun navegarAGasolinera(context: Context, lat: Double, lon: Double, nombre: String) {
    val uri = Uri.parse("google.navigation:q=$lat,$lon&title=${Uri.encode(nombre)}")
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.google.android.apps.maps")
    }
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lon")
        context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
    }
}
