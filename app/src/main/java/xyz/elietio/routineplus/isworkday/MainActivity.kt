package xyz.elietio.routineplus.isworkday

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import xyz.elietio.routineplus.isworkday.ui.navigation.AppNavigation
import xyz.elietio.routineplus.isworkday.ui.theme.RoutinePlusTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RoutinePlusTheme {
                AppNavigation()
            }
        }
    }
}
