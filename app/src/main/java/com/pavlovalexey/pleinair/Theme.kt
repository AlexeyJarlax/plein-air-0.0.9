package com.pavlovalexey.pleinair

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource

@Composable
fun PleinairTheme (
darkTheme: Boolean = isSystemInDarkTheme(),
content: @Composable () -> Unit
) {

// моя палитра:
val MyBlack = Color(0xFF1C1E27)
val MyBlueLight = Color(0xFF9FBBF3)
val MySecondaryBackground = colorResource(id = R.color.my_normal_blue)

// день и ночь:
    val LightColorPalette = lightColors(
        primary = MyBlack,
        primaryVariant = MySecondaryBackground,
        secondary = Color.Cyan,
        background = Color.White
    )

val DarkColorPalette = darkColors(
    primary = MyBlueLight,
    primaryVariant = MySecondaryBackground,
    secondary = MyBlack,
    onPrimary = Color.Gray,
    background = MyBlack,
)

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
            ),
            h6 = MaterialTheme.typography.h6.copy(
                color = if (darkTheme) MyBlueLight else MyBlack
            ),
            body2 = MaterialTheme.typography.body2.copy(
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
