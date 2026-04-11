package com.catalabytes.ekopump.ui.map

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catalabytes.ekopump.R
import com.catalabytes.ekopump.domain.model.MapLayer
import com.catalabytes.ekopump.ui.theme.EkoGreen40

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComingSoonSheet(
    layer: MapLayer,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("ekopump_interests", Context.MODE_PRIVATE)
    val keyInterest = "interested_${layer.name.lowercase()}"
    var yaAvisado by remember {
        mutableStateOf(prefs.getBoolean(keyInterest, false))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0D1A0F)
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(layer.emoji, fontSize = 48.sp)
            Text(
                stringResource(layer.labelRes),
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFFA726).copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    stringResource(R.string.layer_en_desarrollo),
                    fontSize = 12.sp,
                    color = Color(0xFFFFA726),
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                stringResource(layer.descripcionRes),
                fontSize = 15.sp,
                color = Color(0xFF6B8F72),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    if (!yaAvisado) {
                        prefs.edit().putBoolean(keyInterest, true).apply()
                        yaAvisado = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (yaAvisado) Color(0xFF2D4A31) else EkoGreen40
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    stringResource(
                        if (yaAvisado) R.string.layer_avisado else R.string.layer_avisar
                    ),
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }

            TextButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ekopump.es/roadmap"))
                    context.startActivity(intent)
                }
            ) {
                Text(
                    stringResource(R.string.layer_ver_roadmap),
                    color = EkoGreen40,
                    fontSize = 14.sp
                )
            }
        }
    }
}
