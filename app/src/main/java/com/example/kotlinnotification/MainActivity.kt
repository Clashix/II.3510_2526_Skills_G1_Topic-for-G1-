package com.example.kotlinnotification

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.kotlinnotification.ui.theme.KotlinNotificationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    private var hasNotificationPermission by mutableStateOf(false)
    
    // Gestionnaire de résultat pour la demande de permission
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasNotificationPermission = isGranted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Vérifier la permission au démarrage
        checkNotificationPermission()
        
        enableEdgeToEdge()
        setContent {
            KotlinNotificationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PermissionScreen(
                        modifier = Modifier.padding(innerPadding),
                        hasPermission = hasNotificationPermission,
                        onRequestPermission = { requestNotificationPermission() },
                        onSimpleNotification = { delaySeconds ->
                            lifecycleScope.launch {
                                delay(delaySeconds * 1000L)
                                simpleNotification(this@MainActivity)
                            }
                        },
                        onInteractiveNotification = { delaySeconds ->
                            lifecycleScope.launch {
                                delay(delaySeconds * 1000L)
                                interactiveNotification(this@MainActivity)
                            }
                        }
                    )
                }
            }
        }
    }
    
    /**
     * Vérifie si la permission de notification est accordée
     */
    private fun checkNotificationPermission() {
        hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Pour les versions Android < 13, la permission n'est pas nécessaire
            true
        }
    }
    
    /**
     * Demande la permission de notification
     */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
fun PermissionScreen(
    modifier: Modifier = Modifier,
    hasPermission: Boolean = false,
    onRequestPermission: () -> Unit = {},
    onSimpleNotification: (Int) -> Unit = {},
    onInteractiveNotification: (Int) -> Unit = {}
) {
    var simpleDelay by remember { mutableStateOf("3") }
    var interactiveDelay by remember { mutableStateOf("3") }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Titre
        Text(
            text = "Permissions Notifications",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // Carte d'état de la permission
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (hasPermission) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icône et statut
                Text(
                    text = if (hasPermission) "[OK]" else "[!]",
                    style = MaterialTheme.typography.displayLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = if (hasPermission) 
                        "Permission accordée" 
                    else 
                        "Permission requise",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = if (hasPermission) 
                        "Vous pouvez maintenant envoyer des notifications" 
                    else 
                        "Cette application nécessite votre autorisation pour envoyer des notifications",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                if (!hasPermission) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onRequestPermission,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Demander la permission")
                    }
                }
            }
        }
        
        // Afficher les boutons de notification si la permission est accordée
        if (hasPermission) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // Section des notifications
            Text(
                text = "Notifications Demo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Notification Simple
            OutlinedTextField(
                value = simpleDelay,
                onValueChange = { if (it.all { char -> char.isDigit() } && it.length <= 3) simpleDelay = it },
                label = { Text("Delai (secondes)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { 
                    val delay = simpleDelay.toIntOrNull() ?: 3
                    onSimpleNotification(delay)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Envoyer une notification simple")
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Notification Interactive
            OutlinedTextField(
                value = interactiveDelay,
                onValueChange = { if (it.all { char -> char.isDigit() } && it.length <= 3) interactiveDelay = it },
                label = { Text("Delai (secondes)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { 
                    val delay = interactiveDelay.toIntOrNull() ?: 3
                    onInteractiveNotification(delay)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Envoyer une notification interactive")
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Informations
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "A propos des permissions",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "• Sur Android 13+, la permission POST_NOTIFICATIONS est requise\n" +
                            "• Sur les versions antérieures, la permission est automatique\n" +
                            "• L'utilisateur peut accepter ou refuser la demande\n" +
                            "• Les permissions peuvent être modifiées dans les paramètres",
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.5f
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Sans Permission")
@Composable
fun PermissionScreenPreview() {
    KotlinNotificationTheme {
        PermissionScreen(hasPermission = false)
    }
}

@Preview(showBackground = true, name = "Avec Permission")
@Composable
fun PermissionScreenWithAccessPreview() {
    KotlinNotificationTheme {
        PermissionScreen(hasPermission = true)
    }
}
