package com.example.naepapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.naepapp.ui.navigation.NaepNavHost
import com.example.naepapp.ui.theme.NAEPAppTheme

// MainActivity.kt  — the single Activity
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()                         // draw behind system bars; Scaffold applies the insets
        setContent { NAEPAppTheme { NaepNavHost() } }
    }
}