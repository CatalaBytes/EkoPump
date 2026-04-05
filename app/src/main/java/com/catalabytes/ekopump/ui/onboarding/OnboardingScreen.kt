package com.catalabytes.ekopump.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catalabytes.ekopump.ui.theme.EkoAmber40
import com.catalabytes.ekopump.ui.theme.EkoGreen40

data class OnboardingPage(
    val emoji: String,
    val titulo: String,
    val descripcion: String,
    val color: Color
)

val onboardingPages = listOf(
    OnboardingPage("⛽", "Bienvenido a EkoPump", "Encuentra la gasolina más barata cerca de ti con datos oficiales del Gobierno de España. Actualizado cada hora.", EkoGreen40),
    OnboardingPage("💶", "Calcula tu ahorro real", "Dinos el consumo de tu coche y cuántos litros quieres repostar. EkoPump calcula exactamente cuánto ahorras en cada gasolinera.", Color(0xFF2E7D32)),
    OnboardingPage("🛢️", "Sigue el precio del Brent", "El precio del petróleo sube antes de que lo notes en el surtidor. EkoPump te avisa para que repostes en el momento justo.", EkoAmber40),
    OnboardingPage("🚀", "¡Todo listo!", "EkoPump es gratis, sin anuncios y funciona en español, català, euskera, galego e inglés.", EkoGreen40)
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    var paginaActual by remember { mutableStateOf(0) }
    val pagina = onboardingPages[paginaActual]
    val esUltima = paginaActual == onboardingPages.size - 1

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF070F09), Color(0xFF0D1A0F))))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.height(48.dp))

            Box(
                modifier = Modifier.size(120.dp).clip(CircleShape)
                    .background(pagina.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(pagina.emoji, fontSize = 56.sp)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(pagina.titulo, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold,
                    color = Color.White, textAlign = TextAlign.Center, lineHeight = 34.sp)
                Text(pagina.descripcion, fontSize = 16.sp, color = Color(0xFF6B8F72),
                    textAlign = TextAlign.Center, lineHeight = 24.sp)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                onboardingPages.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(50))
                            .background(if (index == paginaActual) EkoGreen40 else Color(0xFF2D4A31))
                            .size(width = if (index == paginaActual) 24.dp else 8.dp, height = 8.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = { if (esUltima) onFinish() else paginaActual++ },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EkoGreen40)
                ) {
                    Text(if (esUltima) "¡Empezar a ahorrar!" else "Siguiente",
                        fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                if (!esUltima) {
                    TextButton(onClick = onFinish) {
                        Text("Omitir", color = Color(0xFF6B8F72), fontSize = 14.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
