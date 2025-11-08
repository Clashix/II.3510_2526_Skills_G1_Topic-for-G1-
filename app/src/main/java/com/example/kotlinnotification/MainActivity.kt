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

/**
 * MainActivity - Main entry point of the application
 * Handles permission requests and UI setup
 */
class MainActivity : ComponentActivity() {
    
    // Track if notification permission is granted
    private var hasNotificationPermission by mutableStateOf(false)
    
    // Handles the result when user accepts/denies permission
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasNotificationPermission = isGranted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check permission when app starts
        checkNotificationPermission()
        
        enableEdgeToEdge()
        setContent {
            KotlinNotificationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PermissionScreen(
                        modifier = Modifier.padding(innerPadding),
                        hasPermission = hasNotificationPermission,
                        onRequestPermission = { requestNotificationPermission() },
                        // Send simple notification after delay
                        onSimpleNotification = { delaySeconds ->
                            lifecycleScope.launch {
                                delay(delaySeconds * 1000L)
                                simpleNotification(this@MainActivity)
                            }
                        },
                        // Send interactive notification after delay
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
    
    // Check if notification permission is granted
    // Android 13+ requires runtime permission, older versions don't
    private fun checkNotificationPermission() {
        hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Permission automatic on Android < 13
            true
        }
    }
    
    // Request notification permission from user
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

/**
 * Main UI screen that displays permission status and notification controls
 */
@Composable
fun PermissionScreen(
    modifier: Modifier = Modifier,
    hasPermission: Boolean = false,
    onRequestPermission: () -> Unit = {},
    onSimpleNotification: (Int) -> Unit = {},
    onInteractiveNotification: (Int) -> Unit = {}
) {
    // Store delay values as strings (for TextField input)
    var simpleDelay by remember { mutableStateOf("3") }
    var interactiveDelay by remember { mutableStateOf("3") }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Title
        Text(
            text = "Permissions Notifications",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // Permission status card (green if granted, red if denied)
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
                
                // Show request button only if permission not granted
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
        
        // Show notification controls only if permission is granted
        if (hasPermission) {
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Notifications Demo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Simple notification: delay input and send button
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
            
            // Interactive notification: delay input and send button
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
        
        // Information card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
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
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.5f,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// Preview functions for Android Studio design preview
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
