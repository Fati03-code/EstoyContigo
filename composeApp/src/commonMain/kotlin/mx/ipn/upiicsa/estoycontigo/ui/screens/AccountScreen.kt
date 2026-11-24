package mx.ipn.upiicsa.estoycontigo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.ipn.upiicsa.estoycontigo.ui.components.InfoCard
import mx.ipn.upiicsa.estoycontigo.ui.components.Michi
import mx.ipn.upiicsa.estoycontigo.ui.components.MichiMood
import mx.ipn.upiicsa.estoycontigo.ui.viewmodel.AuthViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AccountScreen(canGoBack: Boolean, onBack: () -> Unit, onDone: () -> Unit, vm: AuthViewModel = koinViewModel()) {
    val s by vm.state.collectAsStateWithLifecycle()
    var showPassword by rememberSaveable { mutableStateOf(false) }

    AppScreen(title = "Respaldo en la nube", onBack = if (canGoBack) onBack else null) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Michi(size = 110.dp, mood = MichiMood.CURIOUS)
            if (s.signedInEmail != null) {
                Text("Sesión iniciada", style = MaterialTheme.typography.headlineSmall)
                Text("Tus registros se respaldan de forma privada en la cuenta ${s.signedInEmail}.",
                    style = MaterialTheme.typography.bodyMedium)
                Button(onClick = onDone, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Continuar") }
                return@Column
            }
            Text(
                "Crear una cuenta es opcional. Sirve para respaldar tu diario y fotos y recuperarlos si cambias de teléfono. " +
                    "Solo tú puedes ver tus registros.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!s.cloudAvailable) {
                InfoCard(container = MaterialTheme.colorScheme.secondaryContainer) {
                    Text("El respaldo en la nube no está configurado en esta versión. Puedes usar la app completa sin cuenta.",
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
            OutlinedTextField(
                value = s.email, onValueChange = vm::onEmail,
                label = { Text("Correo") }, singleLine = true,
                isError = s.emailError != null,
                supportingText = s.emailError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
                enabled = s.cloudAvailable,
            )
            OutlinedTextField(
                value = s.password, onValueChange = vm::onPassword,
                label = { Text("Contraseña") }, singleLine = true,
                isError = s.passwordError != null,
                supportingText = { Text(s.passwordError ?: if (s.isSignUp) "Mínimo 8 caracteres" else "") },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            if (showPassword) "Ocultar contraseña" else "Mostrar contraseña",
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { vm.submit(onDone) }),
                modifier = Modifier.fillMaxWidth(),
                enabled = s.cloudAvailable,
            )
            s.message?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            }
            Button(
                onClick = { vm.submit(onDone) }, enabled = s.cloudAvailable && !s.isLoading,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            ) {
                if (s.isLoading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text(if (s.isSignUp) "Crear cuenta" else "Iniciar sesión")
            }
            TextButton(onClick = vm::toggleMode, enabled = s.cloudAvailable) {
                Text(if (s.isSignUp) "Ya tengo cuenta: iniciar sesión" else "No tengo cuenta: crear una")
            }
            if (!s.isSignUp) TextButton(onClick = vm::resetPassword) { Text("Olvidé mi contraseña") }
            Spacer(Modifier.height(4.dp))
            OutlinedButton(onClick = { vm.continueOffline(onDone) }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                Text("Usar sin cuenta (solo en este teléfono)")
            }
        }
    }
}
