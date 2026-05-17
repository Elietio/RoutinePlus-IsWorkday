package xyz.elietio.routineplus.isworkday

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map
import xyz.elietio.routineplus.isworkday.data.repository.HolidayRepository
import xyz.elietio.routineplus.isworkday.ui.navigation.AppNavigation
import xyz.elietio.routineplus.isworkday.ui.theme.RoutinePlusTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var dataStore: DataStore<Preferences>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by dataStore.data
                .map { it[HolidayRepository.KEY_THEME_MODE] ?: 0 }
                .collectAsState(initial = 0)

            val darkTheme = when (themeMode) {
                1 -> false
                2 -> true
                else -> isSystemInDarkTheme()
            }

            RoutinePlusTheme(darkTheme = darkTheme) {
                AppNavigation()
            }
        }
    }
}
