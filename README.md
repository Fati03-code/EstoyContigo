# EstoyContigo

App de acompañamiento emocional para estudiantes de UPIICSA-IPN, para **Android e iOS**. Hecha con Kotlin Multiplatform + Compose Multiplatform en Android Studio.

> EstoyContigo **no sustituye la atención psicológica o médica profesional**. Si estás en peligro, llama al **911** o a la **Línea de la Vida 800 911 2000** (24 h, gratuita).

## Funciones

| Área | Qué hace |
|---|---|
| Onboarding | Presenta a Michi (mascota gato), muestra el aviso de que la app no sustituye atención profesional y pide aceptar la privacidad. |
| SOS | Botón visible en todas las pantallas: 911, Línea de la Vida, SAPTEL, LOCATEL, Consejo Ciudadano, contacto de confianza y Orientación Juvenil UPIICSA. Funciona sin internet. |
| Registro de ánimo | 6 emociones con colores neutros, intensidad 1–5, motivos estudiantiles (exámenes, entregas, transporte, dinero…), nota de hasta 500 caracteres y foto opcional. |
| Agita para registrar | El acelerómetro detecta tres sacudidas y abre el registro. |
| Diario | Historial con indicador **naranja (pendiente)** / **verde (en la nube)**, resumen de 30 días y borrado. |
| Micro-pausas (1–3 min) | Suspiro fisiológico, respiración cuadrada, 4-7-8, coherente, anclaje 5-4-3-2-1, soltar hombros y autocompasión. Se pueden hacer entre clases o en el transporte; el acelerómetro detecta si vas en movimiento. |
| Directorio de apoyo | Precargado en Room (offline). Con GPS se ordena por cercanía; botones para llamar, escribir y abrir el mapa. |
| Notificaciones | Recordatorio diario "¿Cómo te sientes hoy?" y modo época de exámenes. Sin datos emocionales en la pantalla de bloqueo. |
| Detección de riesgo | Si una nota contiene frases de crisis, o hay 3 registros difíciles de intensidad alta entre los últimos 5, la app muestra apoyo y líneas de crisis. No diagnostica. |
| Cuenta y nube (opcional) | Firebase Auth + Firestore + Storage con reglas por usuario. Sin cuenta, la app funciona completa en el teléfono. |
| Ajustes | Tema claro/oscuro/sistema, vibración, contacto de confianza, recordatorios, borrar todos mis datos y borrar cuenta. |
| Accesibilidad | Descripciones para TalkBack/VoiceOver, texto escalable, contraste AA y áreas táctiles de 48 dp. |

## Arquitectura

MVVM + Repository, con **Room como única fuente de verdad (SSOT)**. Ver [docs/ARQUITECTURA.md](docs/ARQUITECTURA.md).

```
composeApp/src/
  commonMain/   UI Compose, ViewModels, dominio, Room, DataStore, Firebase REST (Ktor), SyncManager
  androidMain/  CameraX/ActivityResult, FusedLocation, SensorManager, WorkManager, notificaciones
  iosMain/      UIImagePicker, CoreLocation, CoreMotion, UserNotifications, Network
  commonTest/   Pruebas unitarias multiplataforma
  androidUnitTest/  Pruebas E2E con Firebase Emulator
iosApp/         Proyecto Xcode (SwiftUI host)
firebase/       Reglas de Firestore y Storage
docs/           Privacidad, Seguridad de los datos, matriz de pruebas, bitácora de IA
```

| Tema | Tecnología |
|---|---|
| Lenguaje / UI | Kotlin 2.2.21, Compose Multiplatform 1.9.3, Material 3 |
| Persistencia | Room 2.8.4 (KMP), DataStore Preferences |
| Segundo plano | WorkManager 2.10.5 (Android); al abrir y al reconectar (iOS) |
| Nube | Firebase Auth, Firestore y Storage vía REST con Ktor |
| DI | Koin |
| Android | minSdk 26, target/compileSdk 36, R8 activado |

## Compilar

Requisitos: Android Studio (Narwhal o superior), JDK 17, y para iOS una Mac con Xcode 16.

```bash
./gradlew :composeApp:assembleDebug          # APK debug
./gradlew :composeApp:testDebugUnitTest      # pruebas unitarias
./gradlew :composeApp:assembleRelease        # APK release (R8)
./gradlew :composeApp:bundleRelease          # AAB para Play Console
```

iOS: abre `iosApp/iosApp.xcodeproj` en Xcode, elige tu equipo en *Signing & Capabilities* y ejecuta. La fase de compilación llama a Gradle para generar `ComposeApp.framework`.

### Firebase (opcional)

Sin configuración, la app funciona sin cuenta y los registros quedan pendientes (naranja). Para activar la nube, agrega a `local.properties` (no se sube a git):

```properties
FIREBASE_API_KEY=...
FIREBASE_PROJECT_ID=...
FIREBASE_STORAGE_BUCKET=...appspot.com
```

Activa Email/Password en Authentication, crea Firestore y Storage, y publica las reglas:

```bash
firebase deploy --only firestore:rules,storage --project <tu-proyecto>
```

### Pruebas con Firebase Emulator

Requiere firebase-tools y JDK 21 para los emuladores.

```bash
firebase emulators:exec --project demo-estoycontigo --only auth,firestore,storage \
  "FIREBASE_EMULATOR_HOST=127.0.0.1 ./gradlew :composeApp:testDebugUnitTest --tests '*FirebaseEmulatorTest*'"
```

Para que la app del emulador Android use los emuladores de Firebase, agrega `FIREBASE_EMULATOR_HOST=10.0.2.2` y `FIREBASE_PROJECT_ID=demo-estoycontigo` a `local.properties`.

### Firma del release

Crea un keystore **fuera del repositorio** y un `keystore.properties` en la raíz (ignorado por git):

```properties
storeFile=/ruta/fuera/del/repo/estoycontigo-release.jks
storePassword=...
keyAlias=estoycontigo
keyPassword=...
```

Sin ese archivo, el release se genera sin firmar.

## Documentación

- [Arquitectura](docs/ARQUITECTURA.md)
- [Aviso de privacidad](docs/PRIVACIDAD.md)
- [Seguridad de los datos (Play Console) y App Privacy (App Store)](docs/SEGURIDAD_DATOS.md)
- [Ética, seguridad clínica y manejo de crisis](docs/SEGURIDAD_CLINICA.md)
- [Matriz de pruebas](docs/MATRIZ_PRUEBAS.md)
- [Bitácora de uso de IA](docs/BITACORA_IA.md)
