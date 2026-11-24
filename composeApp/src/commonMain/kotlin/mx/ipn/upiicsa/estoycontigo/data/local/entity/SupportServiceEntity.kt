package mx.ipn.upiicsa.estoycontigo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "support_services")
data class SupportServiceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val category: String,
    val phone: String?,
    val phoneExtension: String?,
    val whatsapp: String?,
    val email: String?,
    val address: String?,
    val schedule: String,
    val is24h: Boolean,
    val latitude: Double?,
    val longitude: Double?,
    val website: String?,
    val sortOrder: Int,
)
