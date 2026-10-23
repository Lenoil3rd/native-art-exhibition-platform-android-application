package com.example.naepapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.naepapp.core.common.apiCall
import com.example.naepapp.core.network.ApiService
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
    lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (BuildConfig.DEBUG) {
            CoroutineScope(Dispatchers.IO).launch {
                val result = apiCall { apiService.health() }
                if (result.isSuccess) {
                    Log.d("NAEP_HEALTH", "Health check success: code ${result.getOrNull()?.code()}")
                } else {
                    Log.d("NAEP_HEALTH", "Health check failed: ${result.exceptionOrNull()?.message}")
                }
            }
        }

        setContent { NAEPAppTheme { NaepNavHost() } }
    }
}
