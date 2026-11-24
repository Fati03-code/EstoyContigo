# Arquitectura

## Capas

```mermaid
flowchart TB
  subgraph UI["UI (Compose Multiplatform)"]
    Screens["Pantallas: Onboarding, Inicio, Ánimo, Diario,\nPausas, Respiración, Directorio, SOS, Ajustes"]
    VM["ViewModels + SavedStateHandle"]
  end
  subgraph Domain["Dominio"]
    UC["RiskDetector · Validators · InterventionCatalog · GeoDistance"]
  end
  subgraph Data["Datos"]
    Repo["MoodRepository · DirectoryRepository · AuthRepository"]
    Room[("Room\n(SSOT)")]
    DS[("DataStore\npreferencias y sesión")]
    Sync["SyncManager"]
    Remote["Firebase REST (Ktor)\nAuth · Firestore · Storage"]
  end
  subgraph Platform["Plataforma (expect/actual vía interfaces)"]
    HW["Cámara · GPS · Acelerómetro"]
    Sched["WorkManager / UserNotifications"]
    Net["ConnectivityMonitor"]
  end
  Screens --> VM --> Repo
  VM --> UC
  Repo --> Room
  Repo --> DS
  Sync --> Room
  Sync --> Remote
  Net -- "al reconectar" --> Sched --> Sync
  VM --> HW
```

## Flujo offline-first

```mermaid
sequenceDiagram
  participant U as Estudiante
  participant VM as MoodViewModel
  participant R as Room
  participant W as SyncWorker
  participant F as Firebase
  U->>VM: Guardar registro
  VM->>R: insert(isSynced=false)
  R-->>U: Aparece en el diario (naranja)
  Note over W: Al reconectar / cada 6 h
  W->>R: pendientes()
  W->>F: Subir foto (Storage) y documento (Firestore)
  F-->>W: 200 OK
  W->>R: markSynced(id, updatedAt)
  R-->>U: El registro cambia a verde
```

- La UI **solo lee de Room** (Flow). La nube nunca se lee directamente desde la UI.
- `markSynced` solo marca si `updatedAt` no cambió mientras se subía, para no perder ediciones.
- Borrar un registro lo marca `isDeleted`; el sync borra en la nube y después en Room.
- `SyncManager` usa un `Mutex` para evitar dos sincronizaciones a la vez y renueva el token si expiró.

## Ciclo de vida

- Cada pantalla tiene su ViewModel (Koin). El borrador del registro de ánimo vive en `SavedStateHandle`, así que no se pierde al rotar ni si el sistema mata el proceso.
- Estados de UI pequeños (página del onboarding, casillas) usan `rememberSaveable`.
- Los sensores se exponen como `Flow` (`callbackFlow`) y se desregistran cuando la pantalla deja de recolectarlos.

## Seguridad

- Rutas en la nube: `users/{uid}/moods/{id}` y `users/{uid}/photos/{id}.jpg`. Las reglas de `firebase/` solo permiten acceso al dueño y validan tipos y tamaños.
- Claves de Firebase y keystore fuera de git (`local.properties`, `keystore.properties`).
- `android:allowBackup="false"` y `data_extraction_rules` excluyen la base de datos, las preferencias y las fotos de las copias en la nube y de la transferencia entre dispositivos.
- Fotos guardadas en almacenamiento privado, comprimidas y sin metadatos GPS.
