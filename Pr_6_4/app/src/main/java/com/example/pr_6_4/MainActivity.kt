package com.example.pr_6_4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.pr_6_4.presentation.navigation.NavGraph
import com.example.pr_6_4.ui.theme.Pr62Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Pr62Theme {
                NavGraph()
            }
        }
    }
}
