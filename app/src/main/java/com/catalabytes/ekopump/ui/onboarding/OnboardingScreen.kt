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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catalabytes.ekopump.R
import com.catalabytes.ekopump.ui.theme.EkoAmber40
import com.catalabytes.ekopump.ui.theme.EkoGreen40

data class OnboardingPage(
    val emoji: String,
    val titulo: String,
    val descripcion: String,
    val color: Color
)

@Composable
fun getOnboardingPages(): List<OnboardingPage> = listOf(
    OnboardingPage(
        emoji       = "\u26fd",
        titulo      = stringResource(R.string.onboarding_1_titulo),
        descripcion = stringResource(R.string.onboarding_1_desc),
        color       = EkoGreen40
    ),
    OnboardingPage(
        emoji       = "\ud83d\udcb6",
        titulo      = stringResource(R.string.onboarding_2_titulo),
        descripcion = stringResource(R.string.onboarding_2_desc),
        color       = Color(0xFF2E7D32)
    ),
    OnboardingPage(
        emoji       = "\ud83d\udee2\ufe0f",
        titulo      = stringResource(R.string.onboarding_3_titulo),
        descripcion = stringResource(R.string.onboarding_3_desc),
        color       = EkoAmber40
    ),
    OnboardingPage(
        emoji       = "\ud83d\ude80",
        titulo      = stringResource(R.string.onboarding_4_titulo),
        descripcion = stringResource(R.string.onboarding_4_desc),
        color       = EkoGreen40
    )
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val onboardingPages = getOnboardingPages()
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
                    Text(
                        if (esUltima) stringResource(R.string.onboarding_boton_empezar)
                        else stringResource(R.string.onboarding_boton_siguiente),
                        fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White
                    )
                }
                if (!esUltima) {
                    TextButton(onClick = onFinish) {
                        Text(stringResource(R.string.onboarding_boton_omitir),
                            color = Color(0xFF6B8F72), fontSize = 14.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
