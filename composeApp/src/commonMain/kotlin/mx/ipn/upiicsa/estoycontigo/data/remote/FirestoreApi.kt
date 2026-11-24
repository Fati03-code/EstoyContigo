package mx.ipn.upiicsa.estoycontigo.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import mx.ipn.upiicsa.estoycontigo.config.FirebaseConfig
import mx.ipn.upiicsa.estoycontigo.data.remote.dto.MoodDocument

/** Cloud Firestore REST: cada usuario solo accede a users/{uid}/moods (ver firebase/firestore.rules). */
class FirestoreApi(private val client: HttpClient, private val config: FirebaseConfig) {

    private fun moodsUrl(uid: String) = "${config.firestoreDocumentsUrl()}/users/$uid/moods"

    suspend fun upsertMood(uid: String, idToken: String, doc: MoodDocument) {
        client.patch("${moodsUrl(uid)}/${doc.id}") {
            bearerAuth(idToken)
            contentType(ContentType.Application.Json)
            setBody(FirestoreMapper.toFirestore(doc).toString())
        }.ensureSuccess()
    }

    suspend fun deleteMood(uid: String, idToken: String, id: String) {
        val response = client.delete("${moodsUrl(uid)}/$id") { bearerAuth(idToken) }
        if (response.status != HttpStatusCode.NotFound) response.ensureSuccess()
    }

    suspend fun listMoods(uid: String, idToken: String): List<MoodDocument> {
        val result = mutableListOf<MoodDocument>()
        var pageToken: String? = null
        do {
            val text = client.get(moodsUrl(uid)) {
                bearerAuth(idToken)
                parameter("pageSize", 300)
                pageToken?.let { parameter("pageToken", it) }
            }.ensureSuccess().bodyAsText()
            val json = Json.parseToJsonElement(text).jsonObject
            json["documents"]?.jsonArray?.forEach { el ->
                FirestoreMapper.fromFirestore(el.jsonObject)?.let(result::add)
            }
            pageToken = json["nextPageToken"]?.jsonPrimitive?.content
        } while (pageToken != null)
        return result
    }
}

object FirestoreMapper {
    fun toFirestore(doc: MoodDocument): JsonObject = buildJsonObject {
        putJsonObject("fields") {
            putJsonObject("emotion") { put("stringValue", doc.emotion) }
            putJsonObject("intensity") { put("integerValue", doc.intensity.toString()) }
            putJsonObject("note") { put("stringValue", doc.note) }
            putJsonObject("tags") { put("stringValue", doc.tags) }
            putJsonObject("createdAt") { put("integerValue", doc.createdAt.toString()) }
            putJsonObject("updatedAt") { put("integerValue", doc.updatedAt.toString()) }
            if (doc.photoPath != null) putJsonObject("photoPath") { put("stringValue", doc.photoPath) }
        }
    }

    fun fromFirestore(json: JsonObject): MoodDocument? {
        val name = json["name"]?.jsonPrimitive?.content ?: return null
        val fields = json["fields"]?.jsonObject ?: return null
        fun str(key: String) = fields[key]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content
        fun long(key: String) = fields[key]?.jsonObject?.get("integerValue")?.jsonPrimitive?.content?.toLongOrNull()
        return MoodDocument(
            id = name.substringAfterLast('/'),
            emotion = str("emotion") ?: return null,
            intensity = long("intensity")?.toInt() ?: 3,
            note = str("note") ?: "",
            tags = str("tags") ?: "",
            createdAt = long("createdAt") ?: 0L,
            updatedAt = long("updatedAt") ?: 0L,
            photoPath = str("photoPath"),
        )
    }
}
