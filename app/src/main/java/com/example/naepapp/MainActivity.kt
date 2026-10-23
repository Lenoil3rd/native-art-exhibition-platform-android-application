package com.example.naepapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.naepapp.data.repository.AuthRepository
import com.example.naepapp.ui.navigation.NaepNavHost
import com.example.naepapp.ui.theme.NAEPAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                authRepository.restoreSession()
            } catch (_: Exception) {}
        }

        setContent { NAEPAppTheme { NaepNavHost() } }
    }
}
