# Ética, seguridad clínica y manejo de crisis

## Principios
1. **No sustituye atención profesional.** El aviso aparece en el onboarding (aceptación obligatoria), en Inicio y en "Acerca de".
2. **No diagnostica.** No usa escalas clínicas ni etiquetas como "depresión". Habla de emociones cotidianas y de apoyo.
3. **Ayuda siempre a un toque.** El botón SOS está en la barra superior de todas las pantallas y funciona sin internet y sin cuenta.
4. **Sin presión.** No hay rachas, puntos ni mensajes de culpa. Registrar es opcional y "todas las emociones son válidas".
5. **Micro-intervenciones breves (1–3 min),** discretas (se pueden hacer con los ojos abiertos, sentado en el transporte) y con opción de parar en cualquier momento.

## Detección de riesgo (conservadora)
Implementada en `RiskDetector` (`domain/usecase`), con pruebas en `RiskDetectorTest`.

| Señal | Regla | Respuesta de la app |
|---|---|---|
| Posible crisis | La nota contiene frases como "quiero morir", "hacerme daño", "suicid…" (sin importar acentos o mayúsculas) | Pantalla de apoyo inmediato: 911, Línea de la Vida, SAPTEL, contacto de confianza y Orientación UPIICSA. Mensaje cálido, sin alarma. |
| Malestar sostenido | 3 o más registros de emociones difíciles con intensidad ≥ 4 entre los últimos 5 | Aviso suave en Inicio y tras guardar, invitando a hablar con Orientación Juvenil UPIICSA. |
| Estrés escolar común (p. ej. "el examen me tiene muerto") | No coincide con frases de crisis. Ante la duda se prefiere mostrar ayuda (falso positivo) a omitirla | Recomendación de micro-pausa según la emoción. |

La app **no envía alertas a terceros** ni avisa a nadie sin acción del estudiante: llamar al contacto de confianza es un botón que él decide pulsar.

## Directorio verificado (noviembre de 2026)

| Servicio | Contacto | Horario |
|---|---|---|
| Emergencias | 911 | 24 h |
| Línea de la Vida (CONASAMA) | 800 911 2000 | 24 h, gratuita |
| SAPTEL | 55 5259 8121 | 24 h |
| Consejo Ciudadano (apoyo psicológico) | 55 5533 5533 | 24 h |
| LOCATEL (apoyo psicológico) | *0311 / 55 5658 1111 | 24 h |
| Orientación Juvenil UPIICSA | 55 5624 2000 ext. 70129 · orientacionupiicsa@ipn.mx · Edificio de Gobierno, 1er piso | L–V 8:00–14:00 y 16:00–20:00 |
| Atención a la Salud UPIICSA | 55 5624 2000 ext. 70015 / 70021 · atesalud_upiicsa@ipn.mx | Horario escolar |
| Hospital Psiquiátrico Fray Bernardino Álvarez | 55 5655 4405 (urgencias ext. 23979) | 24 h urgencias |
| Instituto Nacional de Psiquiatría | 55 4160 5000 | Consulta |

Los datos están precargados en Room (`DirectorySeed`) y se pueden actualizar subiendo la versión del seed. Recomendación: verificarlos cada semestre.

## Accesibilidad y lenguaje
- Lenguaje en segunda persona, cálido y breve; sin tecnicismos clínicos.
- Colores neutros y desaturados por emoción; el estado nunca depende solo del color (siempre hay ícono y texto, p. ej. "Pendiente"/"En la nube").
- Contraste AA, texto escalable, áreas táctiles ≥ 48 dp y descripciones para lectores de pantalla.
