package com.pavlovalexey.pleinair

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pavlovalexey.pleinair.main.ui.components.CustomSwitch
import com.pavlovalexey.pleinair.settings.ui.SettingsViewModelInterface

// моя палитра:
val MyBlack = Color(0xFF1C1E27)
val MyBlueLight = Color(0xFF9FBBF3)

// день и ночь:
private val DarkColorPalette = darkColors(
    primary = MyBlueLight,
    primaryVariant = Color.Red,
    secondary = MyBlack,
    onPrimary = Color.Gray,
    background = Color.Black.copy(alpha = 0.5f)
)

private val LightColorPalette = lightColors(
    primary = MyBlack,
    primaryVariant = Color.Green,
    secondary = Color.Cyan,
    background = Color.White.copy(alpha = 0.5f)
)

@Composable
fun PleinairTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography(
            body1 = MaterialTheme.typography.body1.copy(
                color = if (darkTheme) MyBlueLight else MyBlack
            )
        ),
        shapes = Shapes,
        content = content
    )
}

private val Typography = androidx.compose.material.Typography()
private val Shapes = androidx.compose.material.Shapes()

//@Composable
//fun ThemeSwitcher(viewModel: SettingsViewModelInterface) {
//    // Получаем текущий режим из ViewModel
//    val isNightMode by viewModel.isNightMode.observeAsState(initial = false)
//
//    PleinairTheme(darkTheme = isNightMode) {
//        Column {
//            Text(text = "Темный режим")
//            CustomSwitch(
//                checked = isNightMode,
//                onCheckedChange = { viewModel.changeNightMode(it) }
//            )
//        }
//    }
//}

//@Preview(showBackground = true)
//@Composable
//fun PreviewThemeSwitcher() {
//    val mockViewModel = object : SettingsViewModelInterface {
//        private val _isNightMode = MutableLiveData(false)
//        override val isNightMode: LiveData<Boolean> get() = _isNightMode
//
//        override fun changeNightMode(value: Boolean) {
//            _isNightMode.value = value
//        }
//
//        override fun shareApp() { /* no-op for preview */ }
//        override fun goToHelp() { /* no-op for preview */ }
//        override fun seeUserAgreement() { /* no-op for preview */ }
//        override fun seePrivacyPolicy() { /* no-op for preview */ }
//        override fun seeDonat() { /* no-op for preview */ }
//        override fun deleteUserAccount() { /* no-op for preview */ }
//    }
//
//    ThemeSwitcher(viewModel = mockViewModel)
//}
