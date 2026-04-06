# EkoPump — Guía para Claude

App Android para encontrar la gasolinera más barata cerca del usuario, calcular ahorro real
y seguir el precio del Brent. Datos oficiales del Ministerio de Energía de España (MINETUR).
Pública en ekopump.es. Sin anuncios, sin registro.

---

## Stack tecnológico

| Capa | Tecnología | Versión |
|---|---|---|
| Lenguaje | Kotlin | 2.1.0 |
| UI | Jetpack Compose + Material3 | BOM 2025.02.00 |
| DI | Hilt | 2.54 |
| Red | Retrofit + OkHttp + Moshi | 2.11.0 / 4.12.0 / 1.15.2 |
| BBDD local | Room | 2.6.1 |
| Preferencias | DataStore Preferences | 1.1.2 |
| Mapas | MapLibre Android | 11.8.2 |
| Localización | Play Services Location | 21.3.0 |
| Gráficos | Vico (compose-m3) | 2.1.1 |
| Firebase | BOM 33.10.0 | Firestore, Auth, Messaging, Analytics |
| Billing | Play Billing | 7.1.1 |
| Imágenes | Coil | 2.7.0 |
| Build | AGP | 8.13.2 |

---

## Arquitectura

Patrón MVVM limpio con una sola actividad (`MainActivity`).

```
MainActivity (Compose)
├── EkoPumpApp()             — gestiona onboarding vs app principal
└── GasolinerasScreen()      — pantalla raíz con Scaffold + BottomNavBar
    ├── Tab 0: ListaGasolineras + GasolineraItem
    ├── Tab 1: MapScreen (MapLibre)
    ├── Tab 2: HistoryScreen
    └── Tab 3: PerfilScreen
```

### Capas

```
ui/                          — Composables (sin lógica de negocio)
viewmodel/                   — StateFlow + coroutines, coordinan datos
domain/                      — Calculador de ahorro, modelos puros
data/
  ├── api/                   — Retrofit (Minetur), Moshi adapters
  ├── brent/                 — Alpha Vantage API (precio petróleo)
  ├── local/                 — Room DB (historial repostajes)
  ├── prefs/                 — DataStore (configuración usuario)
  ├── repository/            — GasolinerasRepository, RefuelRepository
  ├── location/              — LocationProvider (FusedLocationProvider)
  └── mapper/                — EstacionDto → Gasolinera
```

---

## Fuentes de datos externas

### MINETUR (gasolineras)
- URL: `https://sedeaplicaciones.minetur.gob.es/ServiciosRESTCarburantes/PreciosCarburantes/EstacionesTerrestres/`
- Actualización: cada hora
- Sin clave API
- Campos parseados: Gasolina 95 E5, Gasolina 98 E5, Gasoleo A, Gasoleo B, Gasoleo Premium, GLP
- Importante: latitud/longitud usan coma decimal → se reemplazan con `.` en el mapper

### Alpha Vantage (Brent)
- URL: `https://www.alphavantage.co/query?function=BRENT&interval=daily&apikey=...`
- API key hardcoded en `BrentRepository.kt` (plan gratuito, 25 req/día)
- Devuelve historial diario → se usan últimos 30 días para gráfico

---

## Modelos de dominio clave

### `Gasolinera`
Campos de precio todos `Double?` (null = no disponible en esa estación).

### `Combustible` (enum en GasolinerasRepository.kt)
`GASOLINA_95`, `GASOLINA_98`, `GASOLEO_A`, `GASOLEO_PREMIUM`, `GLP`
Cada entrada tiene `label: String` y `precio: (Gasolinera) -> Double?`.

### `VehicleType` (enum)
`TURISMO`, `FURGONETA`, `CAMION`, `AUTOBUS`
Cada entrada define: emoji, labelEs, consumoDefault/Min/Max, litrosDefault/Min/Max,
quickConsumos, quickLitros.

### `EnergyType` (enum) — añadido en Sesión 7
`GNC`, `GNL`, `ADBLUE`, `EV`
Selector de energía alternativa en PerfilScreen. Se persiste en DataStore.
Es independiente del filtro de combustible (no filtra la lista aún).

### `RefuelEntity` (Room)
Tabla `refuel_history`. Campos: stationName, stationAddress, fuelType, pricePerLiter,
liters, totalCost, savedAmount, latitude, longitude, timestamp.

---

## Persistencia (DataStore)

`CalculadorPrefs` — archivo `calculador_prefs`:
- `consumo_l100km` (Float) — defecto 7f
- `litros_repostar` (Float) — defecto 40f
- `vehicle_type` (String) — defecto "TURISMO"
- `energy_type` (String?) — null si no seleccionado

`LanguagePreferences` — archivo `ekopump_lang`:
- `language` (String) — "system" | "es" | "ca" | "eu" | "gl" | "en"

Onboarding: `ekopump_onboarding` SharedPreferences, clave `completado` (Boolean).

---

## ViewModel principal: `GasolinerasViewModel`

StateFlows expuestos:
- `uiState: UiState<List<GasolineraConDistancia>>`
- `combustible: Combustible` — filtro activo en la lista
- `userLat / userLon: Double`
- `tendencias: Map<String, TendenciaPrecio>` — compara precios vs. sesión anterior
- `consumo / litros: Float` — de DataStore
- `vehicleType: VehicleType` — de DataStore
- `energyType: EnergyType?` — de DataStore

Métodos: `cargar()`, `setCombustible()`, `setConsumo()`, `setLitros()`,
`setVehicleType()`, `setEnergyType()`.

---

## Colores y tema

Definidos en `ui/theme/Color.kt`:
- `EkoGreen40` — verde principal (header gradient, botones)
- `EkoAmber40` — ámbar (header gradient derecha)

Paleta oscura usada inline en los composables:
- `darkBg = Color(0xFF0D1F0D)` — fondo principal
- `darkCard = Color(0xFF162916)` — fondo tarjetas
- `verde = Color(0xFF69F0AE)` — acento neón (Perfil, Historial)
- `grayText = Color(0xFFB0BEC5)` — texto secundario

Energía alternativa (EnergyType):
- GNC → `Color(0xFF29B6F6)` azul cielo
- GNL → `Color(0xFFAB47BC)` violeta
- AdBlue → `Color(0xFF1E88E5)` azul
- EV → `Color(0xFFFFD600)` amarillo

---

## Convenciones de código

- Todo en **español** (variables, comentarios, labels de UI)
- Composables en el mismo fichero que los usan si son pequeños (excepto pantallas propias)
- `PerfilScreen`, `ListaGasolineras`, `GasolineraItem` viven en `MainActivity.kt` (refactor pendiente)
- `CalculadorDialog` es un Dialog legacy que duplica parte de PerfilScreen — conviven
- No hay Navigation Compose real; el routing es un `when (tabActual)` en `GasolinerasScreen`
- Imports de enums se hacen con nombre completo cualificado dentro de los composables
  cuando no hay import explícito (p. ej. `VehicleType.entries` sin import en MainActivity)
- `@OptIn(ExperimentalMaterial3Api::class)` necesario para `PullToRefreshBox` y `ModalBottomSheet`

---

## Features completadas por sesión

### Sesión 1 — Base
- Lista de gasolineras desde MINETUR API
- Filtros por tipo de combustible (chips)
- Distancia al usuario con FusedLocationProvider

### Sesión 2 — Calculador y Brent
- Widget precio Brent (Alpha Vantage) en header
- Pantalla historial Brent con gráfico Vico
- Calculador ahorro real (consumo × litros × diferencia de precio)
- Referencia = gasolinera más cercana (no la más barata)

### Sesión 3 — UI y lanzamiento web
- Landing page ekopump.es (docs/index.html)
- Capturas reales de la app
- Icono definitivo

### Sesión 4 — Onboarding
- Pantalla onboarding 4 pasos (se muestra sólo la primera vez)
- SharedPreferences `ekopump_onboarding` para recordar si se completó

### Sesión 5 — Selector vehículo + tendencias de precio
- `VehicleType` enum con ranges de consumo/litros por tipo
- `CalculadorDialog` futurista con animaciones
- Indicadores tendencia precio ↑ ↓ → por gasolinera (compara con sesión anterior via `PriceHistoryPrefs`)

### Sesión 6 — Bottom nav + Historial + Pull-to-refresh
- Bottom Navigation Bar: Lista / Mapa / Historial / Perfil
- `HistoryScreen`: stats (repostajes, litros, ahorro, gasto total) + lista con delete
- `AddRefuelSheet`: BottomSheet para registrar repostajes desde el ítem de lista
- `HistoryViewModel` + `RefuelRepository` + Room (`RefuelEntity`)
- Pull-to-refresh en ListaGasolineras con `PullToRefreshBox`

### Sesión 7 — EnergyType selector en Perfil
- `EnergyType` enum: GNC, GNL, AdBlue, EV
- `PerfilScreen` ahora muestra selector visual de energía alternativa
- Persiste en DataStore (`energy_type`), toggle (toca para deseleccionar)
- Colores diferenciados por tipo; no filtra la lista aún (step futuro)

---

## Roadmap pendiente

### Próximas sesiones planificadas

- **Eléctricos**: integrar puntos de carga (Electromaps API o similar) como capa en el mapa
- **Transportista / Pro**: modo camión con soporte GNC/GNL en filtros + AdBlue (precio y disponibilidad)
- **Conectar EnergyType al filtro de lista**: si el usuario selecciona GNC → preseleccionar GLP en Combustible
- **Notificaciones push**: alertar cuando el Brent baje de umbral o precio local cambie
- **Compartir repostaje**: captura/share de la tarjeta de ahorro
- **Modo oscuro explícito**: ya usa fondo oscuro fijo, pero no respeta sistema
- **Refactor navegación**: mover a Navigation Compose real, sacar `PerfilScreen` y `ListaGasolineras` fuera de `MainActivity.kt`
- **Widget pantalla inicio**: ya existe `EkoPumpWidget.kt` (incompleto)
- **Internacionalización completa**: strings en `res/values` para soporte CA/EU/GL/EN

---

## Notas importantes

- La API de Minetur devuelve **todas** las estaciones de España sin paginación (~10.000 registros)
  → se filtra a 10 km del usuario antes de mostrar
- El mapa usa estilo público de MapLibre (`demotiles.maplibre.org`) — sin clave, sin coste
- `BrentRepository` tiene la API key hardcoded — el plan gratuito de Alpha Vantage
  tiene límite de 25 llamadas/día; si falla silenciosamente es por eso
- `CalculadorDialog` y `PerfilScreen` comparten la misma lógica (consumo/litros/vehicleType)
  pero son composables independientes — hay duplicación de UI
- El tab "Perfil" usa el icono `Icons.Default.Settings` (igual que "Historial" antes) —
  pendiente cambiarlo a `Icons.Default.Person`
- Google Search Console verificado en `docs/google0b4d0f9696759e73.html`
- Package: `com.catalabytes.ekopump`
- applicationId: `com.catalabytes.ekopump`
