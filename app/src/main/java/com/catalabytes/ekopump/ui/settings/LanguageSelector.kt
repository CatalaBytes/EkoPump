package com.catalabytes.ekopump.ui.settings

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.catalabytes.ekopump.data.prefs.IDIOMAS
import com.catalabytes.ekopump.ui.theme.EkoGreen40
import com.catalabytes.ekopump.viewmodel.LanguageViewModel

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
                    "🌍 Idioma / Language",
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
                                    // Guardar síncronamente
                                    context.getSharedPreferences("ekopump_lang", Context.MODE_PRIVATE)
                                        .edit()
                                        .putString("language", idioma.codigo)
                                        .commit()
                                    viewModel.setLanguage(idioma.codigo)
                                    onDismiss()
                                    // Matar proceso completamente — reinicio total garantizado
                                    android.os.Process.killProcess(android.os.Process.myPid())
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(idioma.bandera, fontSize = 20.sp)
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
