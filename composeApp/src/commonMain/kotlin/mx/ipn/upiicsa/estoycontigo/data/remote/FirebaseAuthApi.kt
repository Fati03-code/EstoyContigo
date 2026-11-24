package mx.ipn.upiicsa.estoycontigo.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.http.parameters
import mx.ipn.upiicsa.estoycontigo.config.FirebaseConfig
import mx.ipn.upiicsa.estoycontigo.data.remote.dto.AuthResponse
import mx.ipn.upiicsa.estoycontigo.data.remote.dto.EmailPasswordRequest
import mx.ipn.upiicsa.estoycontigo.data.remote.dto.IdTokenRequest
import mx.ipn.upiicsa.estoycontigo.data.remote.dto.PasswordResetRequest
import mx.ipn.upiicsa.estoycontigo.data.remote.dto.RefreshResponse

/** Firebase Authentication (Identity Toolkit REST API). */
class FirebaseAuthApi(private val client: HttpClient, private val config: FirebaseConfig) {

    suspend fun signUp(email: String, password: String): AuthResponse =
        postJson(config.authUrl("signUp"), EmailPasswordRequest(email, password)).body()

    suspend fun signIn(email: String, password: String): AuthResponse =
        postJson(config.authUrl("signInWithPassword"), EmailPasswordRequest(email, password)).body()

    suspend fun sendPasswordReset(email: String) {
        postJson(config.authUrl("sendOobCode"), PasswordResetRequest(email))
    }

    suspend fun deleteAccount(idToken: String) {
        postJson(config.authUrl("delete"), IdTokenRequest(idToken))
    }

    suspend fun refresh(refreshToken: String): RefreshResponse {
        val response = client.submitForm(
            url = config.tokenUrl(),
            formParameters = parameters {
                append("grant_type", "refresh_token")
                append("refresh_token", refreshToken)
            },
        )
        return response.ensureSuccess().body()
    }

    private suspend inline fun <reified T : Any> postJson(url: String, body: T): HttpResponse =
        client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.ensureSuccess()
}

suspend fun HttpResponse.ensureSuccess(): HttpResponse {
    if (!status.isSuccess()) throw RemoteException(status.value, bodyAsText())
    return this
}
