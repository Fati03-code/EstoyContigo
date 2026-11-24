package mx.ipn.upiicsa.estoycontigo.data.local

import mx.ipn.upiicsa.estoycontigo.data.local.entity.MoodEntryEntity
import mx.ipn.upiicsa.estoycontigo.data.local.entity.SupportServiceEntity
import mx.ipn.upiicsa.estoycontigo.domain.model.Emotion
import mx.ipn.upiicsa.estoycontigo.domain.model.MoodEntry
import mx.ipn.upiicsa.estoycontigo.domain.model.MoodTag
import mx.ipn.upiicsa.estoycontigo.domain.model.ServiceCategory
import mx.ipn.upiicsa.estoycontigo.domain.model.SupportService
import mx.ipn.upiicsa.estoycontigo.domain.model.SyncState

fun MoodEntryEntity.toDomain(): MoodEntry = MoodEntry(
    id = id,
    emotion = Emotion.fromKey(emotion) ?: Emotion.CALMA,
    intensity = intensity,
    note = note,
    tags = MoodTag.parse(tags),
    createdAt = createdAt,
    photoPath = photoPath,
    syncState = if (isSynced) SyncState.SYNCED else SyncState.PENDING,
)

fun SupportServiceEntity.toDomain(): SupportService = SupportService(
    id = id,
    name = name,
    description = description,
    category = ServiceCategory.entries.firstOrNull { it.name == category } ?: ServiceCategory.CLINIC,
    phone = phone,
    phoneExtension = phoneExtension,
    whatsapp = whatsapp,
    email = email,
    address = address,
    schedule = schedule,
    is24h = is24h,
    latitude = latitude,
    longitude = longitude,
    website = website,
)
