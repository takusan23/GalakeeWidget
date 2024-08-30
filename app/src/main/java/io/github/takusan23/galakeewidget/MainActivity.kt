package io.github.takusan23.galakeewidget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.takusan23.galakeewidget.ui.MainScreen
import io.github.takusan23.galakeewidget.ui.theme.GalakeeWidgetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GalakeeWidgetTheme {
                MainScreen()
            }
        }
    }
}
