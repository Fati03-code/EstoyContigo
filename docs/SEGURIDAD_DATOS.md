# Seguridad de los datos (Google Play) y App Privacy (App Store)

Categoría sugerida: **Salud y bienestar** (Health & Fitness). Clasificación de contenido: todo público; incluye temas de salud mental y líneas de crisis.

## Google Play — formulario "Seguridad de los datos"

| Pregunta | Respuesta |
|---|---|
| ¿Recopila o comparte datos? | Recopila (solo si el usuario crea cuenta). No comparte. |
| ¿Cifrado en tránsito? | Sí (HTTPS). |
| ¿El usuario puede pedir que se borren? | Sí, desde Ajustes → Borrar todos mis datos (incluye la cuenta). |

| Tipo de dato (Play) | Recopilado | Compartido | Opcional | Finalidad |
|---|---|---|---|---|
| Información personal → Correo | Sí | No | Sí | Administración de la cuenta |
| Salud y actividad física → Info de salud (estado emocional) | Sí | No | Sí | Funcionalidad de la app |
| Fotos y videos → Fotos | Sí | No | Sí | Funcionalidad de la app |
| Ubicación aproximada/precisa | **No** (se procesa solo en el dispositivo) | No | — | — |
| Identificadores, analítica, publicidad | No | No | — | — |

Notas para Play Console:
- Política de *Health apps*: declarar que la app no es dispositivo médico y mostrar el aviso en la ficha.
- Permisos: `CAMERA`, `ACCESS_FINE/COARSE_LOCATION`, `POST_NOTIFICATIONS`. Ninguno en segundo plano. No usa `CALL_PHONE` (marca con `ACTION_DIAL`).
- Enlace a la política de privacidad: publicar `docs/PRIVACIDAD.md` (por ejemplo con GitHub Pages).

## App Store — App Privacy

| Dato | Vinculado al usuario | Rastreo | Finalidad |
|---|---|---|---|
| Correo | Sí | No | Funcionalidad de la app |
| Información de salud | Sí | No | Funcionalidad de la app |
| Fotos | Sí | No | Funcionalidad de la app |

Textos de permisos en `iosApp/iosApp/Info.plist`: cámara, fotos, ubicación en uso y movimiento.
