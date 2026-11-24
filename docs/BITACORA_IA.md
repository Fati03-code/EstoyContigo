# Bitácora de uso de IA

Herramienta: Devin (Cognition AI), agente de ingeniería de software. Uso: generación de código, investigación de líneas de apoyo, pruebas y documentación, bajo dirección del equipo.

| # | Solicitud del equipo | Qué hizo la IA | Revisión / decisión humana |
|---|---|---|---|
| 1 | Analizar requisitos, contexto y rúbricas del zip | Resumió requisitos y propuso KMP + Compose (Kotlin, compatible con la rúbrica) para Android e iOS | El equipo pidió Android Studio y soporte iOS |
| 2 | Verificar líneas de crisis | Buscó fuentes oficiales: Línea de la Vida, SAPTEL, LOCATEL, Orientación UPIICSA | Verificar cada semestre; algunos números de otras instituciones se descartaron por no estar confirmados |
| 3 | Persistencia y offline-first | Room como SSOT, `isSynced`, `SyncManager` con Mutex y WorkManager | Requisito propuesto por el equipo |
| 4 | Hardware | Acelerómetro (agitar y movimiento en respiración), cámara (foto del día) y GPS (directorio) | Propuesta del equipo |
| 5 | Nube | Firebase vía REST para compartir código con iOS; reglas por usuario | Claves fuera del repo |
| 6 | Interfaz | Tema de colores neutros por emoción y mascota Michi dibujada con Canvas | Mascota gato propuesta por el equipo |
| 7 | Seguridad clínica | `RiskDetector` conservador, SOS global, aviso de no sustitución | Revisar frases con orientación psicológica |
| 8 | Pruebas | 26 pruebas automáticas y E2E con Firebase Emulator; 4 fallos corregidos (ver matriz) | — |
| 9 | Release | APK/AAB firmado con keystore fuera del repo, R8 activado | Cada equipo debe guardar su keystore de forma segura |

## Limitaciones
- La IA no reemplaza la validación de un profesional de salud mental del contenido y las frases de riesgo.
- La compilación y ejecución en iOS requieren Mac con Xcode y no se verificaron en un dispositivo.
- Las respuestas de la IA se revisaron y probaron antes de integrarse.
