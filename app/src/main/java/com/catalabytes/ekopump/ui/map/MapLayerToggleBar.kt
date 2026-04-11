package com.catalabytes.ekopump.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catalabytes.ekopump.domain.model.MapLayer
import com.catalabytes.ekopump.ui.theme.EkoGreen40

@Composable
fun MapLayerToggleBar(
    capaActiva: MapLayer,
    onCapaSelected: (MapLayer) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.65f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(MapLayer.entries) { layer ->
            val isSelected  = layer == capaActiva
            val isAvailable = layer.activo
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        when {
                            isSelected && isAvailable  -> EkoGreen40
                            isSelected && !isAvailable -> Color(0xFF37474F)
                            else                       -> Color.White.copy(alpha = 0.15f)
                        }
                    )
                    .border(
                        width = if (isSelected) 0.dp else 1.dp,
                        color = if (isAvailable) Color.White.copy(alpha = 0.3f)
                                else Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onCapaSelected(layer) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(layer.emoji, fontSize = 16.sp)
                    if (isSelected) {
                        Text(
                            stringResource(layer.labelRes),
                            fontSize = 11.sp,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (!isAvailable) {
                        Text(
                            "·",
                            fontSize = 8.sp,
                            color = Color(0xFFFFA726)
                        )
                    }
                }
            }
        }
    }
}
