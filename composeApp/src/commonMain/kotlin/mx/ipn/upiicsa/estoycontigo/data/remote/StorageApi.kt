package mx.ipn.upiicsa.estoycontigo.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.readRawBytes
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.encodeURLParameter
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mx.ipn.upiicsa.estoycontigo.config.FirebaseConfig

/** Firebase Cloud Storage REST para las fotos del diario (users/{uid}/photos/{id}.jpg). */
class StorageApi(private val client: HttpClient, private val config: FirebaseConfig) {

    fun photoPath(uid: String, entryId: String) = "users/$uid/photos/$entryId.jpg"

    suspend fun uploadPhoto(idToken: String, path: String, bytes: ByteArray): String {
        client.post(config.storageObjectsUrl()) {
            header(HttpHeaders.Authorization, "Firebase $idToken")
            parameter("uploadType", "media")
            parameter("name", path)
            contentType(ContentType.Image.JPEG)
            setBody(bytes)
        }.ensureSuccess()
        return path
    }

    suspend fun downloadPhoto(idToken: String, path: String): ByteArray =
        client.get("${config.storageObjectsUrl()}/${path.encodeURLParameter()}") {
            header(HttpHeaders.Authorization, "Firebase $idToken")
            parameter("alt", "media")
        }.ensureSuccess().readRawBytes()

    suspend fun listPhotos(idToken: String, uid: String): List<String> {
        val result = mutableListOf<String>()
        var pageToken: String? = null
        do {
            val text = client.get(config.storageObjectsUrl()) {
                header(HttpHeaders.Authorization, "Firebase $idToken")
                parameter("prefix", "users/$uid/photos/")
                pageToken?.let { parameter("pageToken", it) }
            }.ensureSuccess().bodyAsText()
            val json = Json.parseToJsonElement(text).jsonObject
            json["items"]?.jsonArray?.forEach { el ->
                el.jsonObject["name"]?.jsonPrimitive?.content?.let(result::add)
            }
            pageToken = json["nextPageToken"]?.jsonPrimitive?.content
        } while (pageToken != null)
        return result
    }

    suspend fun deletePhoto(idToken: String, path: String) {
        val response = client.delete("${config.storageObjectsUrl()}/${path.encodeURLParameter()}") {
            header(HttpHeaders.Authorization, "Firebase $idToken")
        }
        if (response.status != HttpStatusCode.NotFound) response.ensureSuccess()
    }
}
