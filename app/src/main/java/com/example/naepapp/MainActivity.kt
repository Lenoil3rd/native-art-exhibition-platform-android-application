package com.example.naepapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.example.naepapp.core.common.ApiError
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

        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()

            LifecycleResumeEffect(Unit) {
                CoroutineScope(Dispatchers.IO).launch {
                    val result = authRepository.restoreSession()
                    if (result.isFailure) {
                        val err = result.exceptionOrNull() as? ApiError
                        if (err?.code == "STALE_SESSION") {
                            scope.launch {
                                snackbarHostState.showSnackbar("Please sign in again.")
                            }
                        }
                    }
                }
                onPauseOrDispose {}
            }

            NAEPAppTheme {
                NaepNavHost()
            }
        }
    }
}
