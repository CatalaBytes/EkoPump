package com.catalabytes.ekopump.ui.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catalabytes.ekopump.R
import com.catalabytes.ekopump.data.repository.Combustible
import com.catalabytes.ekopump.data.repository.GasolineraConDistancia

private val EkoGreen  = Color(0xFF00C853)
private val EkoDark   = Color(0xFF0D1A0F)
private val EkoCard   = Color(0xFF1B2E1C)

@Composable
fun FavoritasScreen(
    gasolineras: List<GasolineraConDistancia>,
    combustible: Combustible,
    alertIds: Set<String> = emptySet(),
    onGasolineraClick: (GasolineraConDistancia) -> Unit
) {
    val context = LocalContext.current

    var favIds by remember { mutableStateOf(FavoritasPrefs.getIds(context)) }
    LaunchedEffect(Unit) { favIds = FavoritasPrefs.getIds(context) }

    val favoritas = gasolineras.filter { favIds.contains(it.gasolinera.id) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EkoDark)
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.fav_titulo),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = EkoGreen
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (favoritas.isEmpty())
                stringResource(R.string.fav_guardadas_0)
            else
                stringResource(R.string.fav_guardadas, favoritas.size),
            fontSize = 13.sp,
            color = Color(0xFF6B8F72)
        )
        Spacer(Modifier.height(16.dp))

        if (favoritas.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.fav_sin_datos),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF0FDF4)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.fav_instruccion),
                        fontSize = 14.sp,
                        color = Color(0xFF6B8F72),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(favoritas) { item ->
                    val g = item.gasolinera
                    val precio = combustible.precio(g)
                    Card(
                        onClick = { onGasolineraClick(item) },
                        colors = CardDefaults.cardColors(containerColor = EkoCard),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = g.nombre,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF0FDF4)
                                    )
                                    if (alertIds.contains(g.id)) {
                                        Spacer(Modifier.width(6.dp))
                                        Icon(
                                            Icons.Default.Notifications,
                                            contentDescription = stringResource(R.string.detail_alerta_titulo),
                                            tint = Color(0xFFFFB300),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = g.localidad,
                                    fontSize = 13.sp,
                                    color = Color(0xFF6B8F72)
                                )
                                item.distanciaKm?.let {
                                    Text(
                                        text = "${"%.1f".format(it)} km",
                                        fontSize = 12.sp,
                                        color = Color(0xFF6B8F72)
                                    )
                                }
                            }
                            precio?.let {
                                Box(
                                    modifier = Modifier
                                        .background(EkoGreen, androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "${"%.3f".format(it)}€",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF052E16)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
