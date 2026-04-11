package com.catalabytes.ekopump.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.catalabytes.ekopump.R
import com.catalabytes.ekopump.data.prefs.Idioma
import com.catalabytes.ekopump.data.prefs.IDIOMAS
import com.catalabytes.ekopump.ui.theme.EkoGreen40
import com.catalabytes.ekopump.viewmodel.LanguageViewModel

@Composable
fun BanderaIdioma(idioma: Idioma, modifier: Modifier = Modifier) {
    if (idioma.banderaRes != null) {
        Image(
            painter = painterResource(id = idioma.banderaRes),
            contentDescription = idioma.nombre,
            modifier = modifier.size(24.dp),
            contentScale = ContentScale.Fit
        )
    } else {
        Text(text = idioma.bandera, fontSize = 20.sp, modifier = modifier)
    }
}

@Composable
fun LanguageSelectorDialog(
    onDismiss: () -> Unit,
    viewModel: LanguageViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val currentLang by viewModel.language.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "\uD83C\uDF0D Idioma / Language",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                LazyColumn {
                    items(IDIOMAS) { idioma ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    context.getSharedPreferences("ekopump_lang", Context.MODE_PRIVATE)
                                        .edit()
                                        .putString("language", idioma.codigo)
                                        .commit()
                                    viewModel.setLanguage(idioma.codigo)
                                    onDismiss()
                                    val msg = context.getString(R.string.idioma_reiniciando)
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        val intent = (context as Activity).intent
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                        context.startActivity(intent)
                                        (context as Activity).finish()
                                    }, 400)
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                BanderaIdioma(idioma)
                                Text(idioma.nombre, fontSize = 16.sp)
                            }
                            if (currentLang == idioma.codigo) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = EkoGreen40
                                )
                            }
                        }
                        if (idioma != IDIOMAS.last()) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                        }
                    }
                }
            }
        }
    }
}
