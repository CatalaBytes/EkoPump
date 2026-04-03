# EKOPUMP — Resumen Completo de Sesión
> Documento de traspaso para nuevo proyecto Claude Pro
> Fecha: Abril 2026 | GitHub: CatalaBytes/EkoPump

---

## 🎯 QUÉ ES EKOPUMP

App Android nativa para encontrar gasolineras baratas en España en tiempo real.
- **Package:** `com.catalabytes.ekopump`
- **Repo:** `/home/edeb13/github/EkoPump`
- **Dominio:** ekopump.org (pendiente)
- **Modelo:** Freemium sin anuncios

---

## 📊 ANÁLISIS DE MERCADO (realizado en sesión)

### Competencia y descargas reales
| App | Descargas | Valoración |
|---|---|---|
| Gasolineras de España | +500.000 | ⭐ 4.2 |
| Gasolina y Diesel España | +100.000 | ⭐ 4.5 |
| Gasolineras RACE | +50.000 | ⭐ 3.9 |
| GasAll | ~50.000 | ⭐ 4.1 |

### Mercado total
- 34,7 millones de vehículos en España (2025)
- ~25 millones de conductores con smartphone
- Apps líderes tienen máximo 2% de penetración → mercado vastamente subexplotado

### Ventajas diferenciales EKOPUMP vs competencia
1. **Euskera** — ÚNICO en el mercado. Ninguna app tiene euskera
2. **Widget pantalla inicio** — nadie lo tiene en este nicho
3. **IA predictiva de precios** — ningún competidor
4. **Sin anuncios** — todos los competidores tienen anuncios
5. **5 idiomas nativos** — ES, CA, EU, GL, EN

---

## 🏗️ STACK TÉCNICO DEFINIDO

| Capa | Tecnología | Coste |
|---|---|---|
| Lenguaje | Kotlin | Gratis |
| IDE | Android Studio | Gratis |
| Mapas | MapLibre SDK | Gratis |
| API combustible | MINETUR (Ministerio) | Gratis/Pública |
| GPS | FusedLocationProvider | Gratis |
| IA on-device | Google ML Kit | Gratis |
| Backend futuro | Firebase Firestore | Free tier |
| Notificaciones | Firebase Cloud Messaging | Gratis |
| Pagos | Google Play Billing v6 | Estándar |
| CI/CD | GitHub Actions | Gratis |

---

## 🌐 API MINETUR

**Base URL:**
```
https://sedeaplicaciones.minetur.gob.es/ServiciosRESTCarburantes/PreciosCarburantes/
```

**Endpoints:**
- `EstacionesTerrestres/` → todas las gasolineras de España
- Actualización cada 20 minutos
- Sin API key necesaria

**Particularidades críticas del JSON:**
- Coordenadas con COMA decimal: `"39,469187"` → hay que hacer `.replace(",", ".")`
- Precios con COMA decimal: `"1,559"` → mismo tratamiento
- Campos con tildes: `"Rótulo"`, `"Dirección"`, `"Longitud (WGS84)"`
- Precio vacío `""` = combustible no disponible

---

## 📁 ESTRUCTURA DEL PROYECTO (estado actual)

```
/home/edeb13/github/EkoPump/
├── app/
│   ├── build.gradle.kts
│   └── src/main/java/com/catalabytes/ekopump/
│       ├── MainActivity.kt
│       ├── data/
│       │   ├── api/
│       │   │   ├── MineturApiService.kt   ✅ creado
│       │   │   ├── MineturModels.kt       ✅ creado
│       │   │   └── RetrofitClient.kt      ✅ creado
│       │   ├── db/
│       │   │   └── GasolineraEntity.kt    ✅ creado
│       │   ├── mapper/
│       │   │   └── GasolineraMapper.kt    ✅ creado
│       │   └── repository/               (vacío — pendiente)
│       ├── domain/
│       │   └── model/
│       │       └── Gasolinera.kt          ✅ creado
│       ├── ui/
│       │   ├── theme/                     ✅ existe
│       │   └── common/
│       │       └── UiState.kt             ✅ creado
│       └── viewmodel/
│           └── GasolinerasViewModel.kt    ✅ creado
├── gradle/
│   └── libs.versions.toml                ✅ configurado
├── build.gradle.kts                       ✅ configurado
└── settings.gradle.kts                    ✅ configurado
```

---

## ✅ ESTADO ACTUAL DEL BUILD

```
BUILD SUCCESSFUL in 28s
42 actionable tasks: 7 executed, 35 up-to-date
```

**La app compila.** El APK debug se genera correctamente.

### Problema pendiente: CRASH al arrancar
Error: `S'ha aturat EkoPump` (crash en inicio)
**Causa probable:** MainActivity usa Hilt pero falta la clase Application configurada.

**Próximo paso inmediato:**
1. Ver log del crash: `adb logcat -d | grep -E "FATAL|ekopump|AndroidRuntime" | tail -30`
2. Crear `EkoPumpApplication.kt` con `@HiltAndroidApp`
3. Registrarla en `AndroidManifest.xml`

---

## 🔧 PROBLEMAS RESUELTOS EN SESIÓN

| Problema | Solución |
|---|---|
| TOML no acepta `//` como comentario | Solo `#` en TOML |
| `Gasolinera.kt` con múltiples `package` | Archivo corrupto — reescrito desde terminal |
| `android.useAndroidX` no configurado | Añadir a `gradle.properties` |
| `EPERM` en red | Faltaba `<uses-permission INTERNET/>` en Manifest |
| Firebase sin `google-services.json` | Plugin comentado temporalmente |
| Java no encontrado | `sudo apt install default-jdk` |
| `JAVA_HOME` incorrecto | `export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64` |
| `_gasolineras.value` en hilo IO | Cambiar a `.postValue()` |

---

## 📱 MONETIZACIÓN PLANIFICADA

| Función | Gratis | Premium (1,99€/mes · 14,99€/año) |
|---|---|---|
| Mapa + precios tiempo real | ✅ | ✅ |
| Alertas de precio | 2 máx | Ilimitadas |
| Favoritos | 5 máx | Ilimitados |
| Historial precios | 30 días | 12 meses |
| Widget pantalla inicio | ❌ | ✅ |
| Modo viaje IA | ❌ | ✅ |

---

## 🗺️ ASO (App Store Optimization)

### Título Play Store
`EKOPUMP: Gasolineras Baratas` (28 chars)

### Keywords prioritarias
- Long-tail: "app gasolineras sin anuncios", "widget precio gasolina android"
- Por idioma: gasolineres barates (CA), gasolindegia merke (EU)
- Euskera = monopolio absoluto, cero competencia

### Screenshots planificadas (8)
1. Mapa con precios → "La gasolinera más barata, ahora mismo"
2. Lista por precio → "Ahorra hasta X€ cada mes"
3. Alerta de precio → "Te avisamos cuando baje"
4. Historial gráfico → "Elige el mejor momento"
5. Calculadora ahorro → "Calcula exactamente cuánto ahorras"
6. Widget → "El precio siempre a la vista"
7. Selector idioma → "ES · CA · EU · GL · EN"
8. Sin anuncios → "Cero anuncios. Siempre."

---

## 📈 PROYECCIÓN ECONÓMICA

| Plazo | Usuarios | % Premium | Ingresos/mes |
|---|---|---|---|
| 0–6 meses | 1.000–5.000 | 2% | 200–800€ |
| 6–18 meses | 20.000–50.000 | 3–5% | 1.500–4.000€ |
| 18–36 meses | 100.000+ | 4–6% | 8.000–20.000€ |

---

## 🚀 FASES DEL PROYECTO

### ✅ FASE 0 — Completada
- Arquitectura definida
- Gradle configurado
- Modelos de datos creados
- BUILD SUCCESSFUL

### 🔄 FASE 1 — En curso (semanas 1–4)
- [ ] Resolver crash inicial (Application Hilt)
- [ ] Conectar API MINETUR → datos reales en pantalla
- [ ] Mapa MapLibre con marcadores
- [ ] GPS → gasolineras cercanas
- [ ] Filtros por combustible
- [ ] 5 idiomas (strings.xml)

### ⏳ FASE 2 — Pendiente (semanas 5–8)
- [ ] Historial de precios + gráfico
- [ ] Alertas push (Firebase)
- [ ] Predicción IA (ML Kit)
- [ ] Cálculo ahorro real

### ⏳ FASE 3 — Pendiente (semanas 9–12)
- [ ] Sistema freemium (Google Play Billing)
- [ ] Widget pantalla inicio
- [ ] Donaciones Ko-fi

### ⏳ FASE 4 — Pendiente (semanas 13–14)
- [ ] UI/UX refinado
- [ ] Beta cerrada
- [ ] Publicación Play Store

---

## ⚡ PRÓXIMOS 3 PASOS INMEDIATOS

**1. Resolver crash (hacer YA):**
```bash
adb logcat -d | grep -E "FATAL|ekopump|AndroidRuntime" | tail -30
```

**2. Crear EkoPumpApplication.kt:**
```kotlin
package com.catalabytes.ekopump

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EkoPumpApplication : Application()
```

**3. Registrar en AndroidManifest.xml:**
```xml
<application
    android:name=".EkoPumpApplication"
    ...>
```

---

## 🛠️ COMANDOS ÚTILES

```bash
# Compilar
cd ~/github/EkoPump && ./gradlew :app:assembleDebug

# Instalar en móvil
./gradlew :app:installDebug

# Ver logs de crash
adb logcat -d | grep -E "FATAL|ekopump|AndroidRuntime" | tail -30

# Limpiar build
./gradlew clean

# Ver todos los archivos Kotlin
find ~/github/EkoPump -name "*.kt" | sort
```

---

## 📝 NOTAS IMPORTANTES PARA CONTINUACIÓN

1. **Firebase desactivado** temporalmente — reactivar en Fase 2 con `google-services.json`
2. **El mapper** usa `.replace(",", ".")` para parsear precios y coordenadas de MINETUR
3. **Package correcto:** `com.catalabytes.ekopump` (NO `org.catalabytes`)
4. **Java:** OpenJDK 21 instalado en `/usr/lib/jvm/java-21-openjdk-amd64`
5. **Android Studio** tiene bug de caché — ante errores extraños: `File → Invalidate Caches → Restart`
6. **TOML:** Solo comentarios con `#`, nunca `//`

---

*Generado: Abril 2026 | Sesión inicial EKOPUMP | CatalaBytes*
