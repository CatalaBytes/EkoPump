package com.catalabytes.ekopump.ui.brent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catalabytes.ekopump.viewmodel.BrentViewModel

@Composable
fun BrentWidget(viewModel: BrentViewModel) {
    val brent by viewModel.brent.collectAsState()

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.Black.copy(alpha = 0.25f),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (brent == null) {
                Text("🛢 Brent: —", color = Color.White, fontSize = 11.sp)
            } else {
                val b = brent!!
                val subiendo = b.variacion >= 0
                val color = if (subiendo) Color(0xFFFF6B35) else Color(0xFF4CAF50)
                val flecha = if (subiendo) "▲" else "▼"
                Text("🛢 Brent", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Text("${"%.2f".format(b.precio)}$", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("$flecha ${"%.2f".format(b.variacionPct)}%", color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
