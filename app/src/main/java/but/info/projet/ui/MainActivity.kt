package but.info.projet.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import but.info.projet.data.Club
import but.info.projet.data.ClubRepository
import but.info.projet.ui.theme.ProjetTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProjetTheme {
                Scaffold(modifier = Modifier.Companion.fillMaxSize()) { innerPadding ->
                    App(
                        modifier = Modifier.Companion.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun App(modifier: Modifier = Modifier.Companion) {
    val context = LocalContext.current.applicationContext
    val repository = remember(context) { ClubRepository(context) }
    val scope = rememberCoroutineScope()

    var clubs by remember { mutableStateOf<List<Club>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showDisabledClubs by remember { mutableStateOf(true) }
    var showLoginDialog by remember { mutableStateOf(false) }
    var loginIdentifier by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf<String?>(null) }
    var loginLoading by remember { mutableStateOf(false) }
    var pendingDeactivation by remember { mutableStateOf<Club?>(null) }

    suspend fun performDeactivation(club: Club) {
        // Optimistic UI: reflect local deactivation immediately.
        clubs = clubs.map {
            if (it.id == club.id) it.copy(active = 0, dirty = 1) else it
        }

        val success = withContext(Dispatchers.IO) {
            repository.deactivateClub(club.id)
        }
        clubs = withContext(Dispatchers.IO) { repository.getAllLocal() }
        if (!success) {
            error = "Desactivation distante impossible. Verifie tes droits."
            if (!repository.hasAuthentication()) {
                pendingDeactivation = club
                showLoginDialog = true
            }
        } else {
            error = null
        }
    }

    fun reload() {
        scope.launch {
            loading = true
            error = null
            try {
                clubs = withContext(Dispatchers.IO) { repository.synchronize() }
            } catch (e: Exception) {
                error = e.message ?: "Erreur de synchronisation"
                clubs = withContext(Dispatchers.IO) { repository.getAllLocal() }
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        reload()
    }

    when {
        loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        error != null && clubs.isEmpty() -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = error ?: "Erreur inconnue")
            }
        }
        else -> {
            ClubList(
                clubs = clubs,
                modifier = modifier,
                error = error,
                showDisabledClubs = showDisabledClubs,
                onShowDisabledChange = { showDisabledClubs = it },
                onDeactivate = { club ->
                    scope.launch {
                        if (!repository.hasAuthentication()) {
                            pendingDeactivation = club
                            loginError = null
                            showLoginDialog = true
                            return@launch
                        }
                        performDeactivation(club)
                    }
                }
            )
        }
    }

    if (showLoginDialog) {
        LoginDialog(
            identifier = loginIdentifier,
            password = loginPassword,
            loading = loginLoading,
            error = loginError,
            onIdentifierChange = { loginIdentifier = it },
            onPasswordChange = { loginPassword = it },
            onDismiss = {
                if (!loginLoading) {
                    showLoginDialog = false
                    pendingDeactivation = null
                }
            },
            onConfirm = {
                scope.launch {
                    if (loginIdentifier.isBlank() || loginPassword.isBlank()) {
                        loginError = "Renseigne identifiant et mot de passe."
                        return@launch
                    }

                    loginLoading = true
                    loginError = null
                    val authenticated = withContext(Dispatchers.IO) {
                        repository.authenticate(loginIdentifier.trim(), loginPassword)
                    }
                    loginLoading = false

                    if (!authenticated) {
                        loginError = "Connexion impossible. Identifiants invalides ou droits manquants."
                        return@launch
                    }

                    showLoginDialog = false
                    error = null
                    val target = pendingDeactivation
                    pendingDeactivation = null
                    if (target != null) {
                        performDeactivation(target)
                    }
                }
            }
        )
    }
}

@Composable
private fun ClubList(
    clubs: List<Club>,
    modifier: Modifier = Modifier,
    error: String? = null,
    showDisabledClubs: Boolean = true,
    onShowDisabledChange: (Boolean) -> Unit = {},
    onDeactivate: (Club) -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    val filteredClubs = if (showDisabledClubs) clubs else clubs.filter { it.active == 1 }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Clubs: ${filteredClubs.size}")
            Box {
                Button(onClick = { dropdownExpanded = true }) {
                    Text(if (showDisabledClubs) "Avec desactives" else "Sans desactives")
                }
                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Afficher desactives") },
                        onClick = {
                            onShowDisabledChange(true)
                            dropdownExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Masquer desactives") },
                        onClick = {
                            onShowDisabledChange(false)
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }

        if (error != null) {
            Text(
                text = error,
                color = Color.Red,
                modifier = Modifier.padding(12.dp)
            )
        }
        if (filteredClubs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(if (clubs.isEmpty()) "Aucun club disponible" else "Aucun club actif")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredClubs) { club ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = club.name)
                            Text(text = club.address)
                            if (club.active == 0) {
                                Text(
                                    text = "Desactive",
                                    color = Color.Red,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            } else {
                                Button(
                                    onClick = { onDeactivate(club) },
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Text("Desactiver")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginDialog(
    identifier: String,
    password: String,
    loading: Boolean,
    error: String?,
    onIdentifierChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Connexion requise") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("La desactivation d'un club necessite une connexion.")
                OutlinedTextField(
                    value = identifier,
                    onValueChange = onIdentifierChange,
                    label = { Text("Identifiant") },
                    placeholder = { Text("pauld") },
                    singleLine = true,
                    enabled = !loading
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = { Text("Mot de passe") },
                    placeholder = { Text("12345678") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    enabled = !loading
                )
                if (error != null) {
                    Text(
                        text = error,
                        color = Color.Red
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !loading
            ) {
                Text(if (loading) "Connexion..." else "Se connecter")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !loading
            ) {
                Text("Annuler")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    ProjetTheme {
        ClubList(
            clubs = listOf(
                Club(1, "Club A", "Adresse A"),
                Club(2, "Club B", "Adresse B", active = 0)
            ),
            onDeactivate = {}
        )
    }
}
