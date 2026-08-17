package com.sonza.music

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.sonza.music.core.permissions.PermissionManager
import com.sonza.music.core.theme.SonzaTheme
import com.sonza.music.navigation.SonzaNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Permissions handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val requiredPermissions = PermissionManager.getRequiredAudioPermissions()
        permissionLauncher.launch(requiredPermissions.toTypedArray())

        setContent {
            SonzaTheme {
                SonzaNavHost()
            }
        }
    }
}
