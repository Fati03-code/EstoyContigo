# Matriz de pruebas

Entorno: Android Emulator API 35 (Pixel, x86_64), APK debug y release (R8, firmado). Firebase Emulator Suite (Auth, Firestore, Storage) con las reglas reales de `firebase/`. iOS: compilación Kotlin/Native de `iosArm64` y `iosSimulatorArm64` (enlace y ejecución en Xcode pendientes de Mac).

## Pruebas automatizadas

| Suite | Casos | Qué valida | Resultado |
|---|---|---|---|
| `ValidatorsTest` | 7 | Intensidad 1–5, nota ≤ 500, correo y contraseña, teléfono del contacto | ✅ |
| `RiskDetectorTest` | 4 | Frases de crisis sin acentos, no alarmar por estrés escolar, umbral 3/5, prioridad de crisis | ✅ |
| `CatalogAndGeoTest` | 4 | Pausas de 1–3 min, recomendación por emoción, Haversine, directorio offline con líneas 24 h | ✅ |
| `ShakeDetectorTest` | 4 | Tres picos = agitar, caminar no cuenta, ventana de 900 ms, enfriamiento | ✅ (tras corrección, ver F-1) |
| `MappersTest` | 3 | Pendiente/sincronizado, UUID, etiquetas, emoción desconocida | ✅ |
| `FirebaseEmulatorTest` | 4 | Registro/inicio/renovación de token, CRUD de ánimo, **otra cuenta no puede leer ni escribir**, regla rechaza intensidad 9, foto privada en Storage | ✅ (con emulador) |

Total: 26 pruebas, 0 fallos.

## Pruebas funcionales (manuales)

| ID | Caso | Pasos | Resultado esperado | Resultado |
|---|---|---|---|---|
| CP-01 | Onboarding con aviso | Instalar, avanzar 3 pantallas | "Comenzar" deshabilitado hasta marcar la casilla de aviso y privacidad | ✅ |
| CP-02 | Uso sin cuenta | En "Respaldo en la nube" elegir "Usar sin cuenta" | Llega a Inicio; todas las funciones disponibles | ✅ |
| CP-03 | Registro offline-first | Registrar Ansiedad, motivo "Entregas" y guardar | Aparece en el Diario con etiqueta naranja "Pendiente" | ✅ (debug y release) |
| CP-04 | Sincronización | Con cuenta en emulador de Firebase, registrar en modo avión y reconectar | Al volver la red, WorkManager sube el registro y cambia a verde | ✅ (API probada con `FirebaseEmulatorTest`) |
| CP-05 | SOS sin internet | Modo avión → botón SOS → Línea de la Vida | Abre el marcador con 8009112000 | ✅ |
| CP-06 | Permiso de cámara denegado | Foto del día → Denegar dos veces | Explicación + botón "Abrir Ajustes"; el registro se guarda sin foto | ✅ |
| CP-07 | Directorio sin GPS | Denegar ubicación | Lista completa por categoría, sin distancias | ✅ |
| CP-08 | Rotación / proceso | Escribir nota, rotar; luego enviar la app a segundo plano y matar el proceso (`adb shell am kill`) | La nota y la emoción se conservan (SavedStateHandle) | ✅ |
| CP-09 | Crisis en nota | Escribir "ya no quiero vivir" y guardar | Pantalla de apoyo con 911 y líneas de crisis | ✅ |
| CP-10 | Release R8 | Instalar APK release firmado y repetir CP-01..CP-03 | Sin cierres; Room y serialización funcionan | ✅ |

## Fallos encontrados y corregidos

| ID | Prueba | Fallo | Causa | Corrección |
|---|---|---|---|---|
| F-1 | `ShakeDetectorTest` | 2 de 4 casos fallaban: nunca detectaba la primera sacudida | `lastPeakAt = Long.MIN_VALUE`; `now - lastPeakAt` se desbordaba y el enfriamiento parecía activo | Inicializar con `Long.MIN_VALUE / 2` |
| F-2 | Reglas de Firestore | Las escrituras desde la app eran rechazadas | La regla esperaba `tags` como lista y un campo `id`; la app envía `tags` como texto y el id va en la ruta | Validar `tags is string` y quitar `id` de las claves requeridas |
| F-3 | `FirebaseEmulatorTest` | JUnit: "Method … should be void" | `= runBlocking { … }` devolvía el último valor | `runBlocking<Unit>` |
| F-4 | iOS (Kotlin/Native) | "Mixing Kotlin and Objective-C supertypes is not supported" | Una clase implementaba una interfaz Kotlin y `CLLocationManagerDelegateProtocol` | Delegado separado `LocationAuthDelegate : NSObject()` |
