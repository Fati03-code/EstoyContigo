package mx.ipn.upiicsa.estoycontigo

import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.runBlocking
import mx.ipn.upiicsa.estoycontigo.config.FirebaseConfig
import mx.ipn.upiicsa.estoycontigo.data.remote.FirebaseAuthApi
import mx.ipn.upiicsa.estoycontigo.data.remote.FirestoreApi
import mx.ipn.upiicsa.estoycontigo.data.remote.RemoteException
import mx.ipn.upiicsa.estoycontigo.data.remote.StorageApi
import mx.ipn.upiicsa.estoycontigo.data.remote.createHttpClient
import mx.ipn.upiicsa.estoycontigo.data.remote.dto.MoodDocument
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Prueba E2E contra Firebase Emulator Suite (Auth + Firestore + Storage) con las reglas reales.
 * Ejecutar: firebase emulators:exec --project demo-estoycontigo --only auth,firestore,storage \
 *   "FIREBASE_EMULATOR_HOST=127.0.0.1 ./gradlew :composeApp:testDebugUnitTest --tests '*FirebaseEmulatorTest*'"
 */
class FirebaseEmulatorTest {
    private val host = System.getenv("FIREBASE_EMULATOR_HOST").orEmpty()
    private val config = FirebaseConfig(apiKey = "", projectId = "demo-estoycontigo", storageBucket = "demo-estoycontigo.appspot.com", emulatorHost = host)
    private val client = createHttpClient(OkHttp.create())
    private val auth = FirebaseAuthApi(client, config)
    private val firestore = FirestoreApi(client, config)
    private val storage = StorageApi(client, config)

    @Before fun requireEmulator() = assumeTrue("FIREBASE_EMULATOR_HOST no definido", host.isNotBlank())

    private fun email() = "alumno${Random.nextInt(1_000_000)}@alumno.ipn.mx"
    private fun doc(id: String) = MoodDocument(id, "ANSIEDAD", 4, "Examen de cálculo", "EXAMENES,TRANSPORTE", 1_000L, 2_000L, null)

    @Test fun signUpSignInAndRefresh() = runBlocking<Unit> {
        val mail = email()
        val created = auth.signUp(mail, "contrasena123")
        val signed = auth.signIn(mail, "contrasena123")
        assertEquals(created.localId, signed.localId)
        assertTrue(auth.refresh(signed.refreshToken).idToken.isNotBlank())
        val error = assertFailsWith<RemoteException> { auth.signIn(mail, "incorrecta") }
        assertTrue(error.message.isNotBlank())
    }

    @Test fun moodRoundTripScopedToOwner() = runBlocking<Unit> {
        val a = auth.signUp(email(), "contrasena123")
        val b = auth.signUp(email(), "contrasena123")
        firestore.upsertMood(a.localId, a.idToken, doc("m1"))
        firestore.upsertMood(a.localId, a.idToken, doc("m1").copy(intensity = 2, updatedAt = 3_000L))
        val list = firestore.listMoods(a.localId, a.idToken)
        assertEquals(1, list.size)
        assertEquals(2, list.first().intensity)
        assertEquals("EXAMENES,TRANSPORTE", list.first().tags)

        // Reglas: otra cuenta no puede leer ni escribir los registros de A.
        assertFailsWith<RemoteException> { firestore.listMoods(a.localId, b.idToken) }
        assertFailsWith<RemoteException> { firestore.upsertMood(a.localId, b.idToken, doc("intruso")) }

        firestore.deleteMood(a.localId, a.idToken, "m1")
        assertTrue(firestore.listMoods(a.localId, a.idToken).isEmpty())
    }

    @Test fun invalidIntensityRejectedByRules() = runBlocking<Unit> {
        val a = auth.signUp(email(), "contrasena123")
        assertFailsWith<RemoteException> { firestore.upsertMood(a.localId, a.idToken, doc("x").copy(intensity = 9)) }
    }

    @Test fun photoUploadIsPrivate() = runBlocking<Unit> {
        val a = auth.signUp(email(), "contrasena123")
        val b = auth.signUp(email(), "contrasena123")
        val bytes = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 1, 2, 3, 0xFF.toByte(), 0xD9.toByte())
        val path = storage.uploadPhoto(a.idToken, storage.photoPath(a.localId, "m1"), bytes)
        assertContentEquals(bytes, storage.downloadPhoto(a.idToken, path))
        assertFailsWith<RemoteException> { storage.downloadPhoto(b.idToken, path) }
        assertFailsWith<RemoteException> { storage.uploadPhoto(b.idToken, storage.photoPath(a.localId, "m2"), bytes) }
        storage.deletePhoto(a.idToken, path)
        auth.deleteAccount(a.idToken)
    }
}
