package com.example.myplanes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.myplanes.ui.MainScreen
import com.example.myplanes.ui.theme.MyPlanesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { MyPlanesTheme { MainScreen(
                    onAddTask = { /* экран создания задачи добавим на шаге 5 */ }
                )
            }
        }
    }
}