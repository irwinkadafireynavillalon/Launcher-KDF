package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Definición de colores base para los 10 temas
val Themes = listOf(
    // OLED Noir (OLED)
    darkColorScheme(primary = Color(0xFFFFFFFF), background = Color(0xFF000000), surface = Color(0xFF121212)),
    // Cyberpunk (Cyber)
    darkColorScheme(primary = Color(0xFF00FF00), background = Color(0xFF1A1A1A), surface = Color(0xFF2C2C2C)),
    // Esmeralda (Emerald)
    darkColorScheme(primary = Color(0xFF50C878), background = Color(0xFF063319), surface = Color(0xFF0C5228)),
    // Royal Gold (Gold)
    darkColorScheme(primary = Color(0xFFFFD700), background = Color(0xFF1E1E1E), surface = Color(0xFF333333)),
    // Neon Violet (Violet)
    darkColorScheme(primary = Color(0xFF8A2BE2), background = Color(0xFF100020), surface = Color(0xFF200040)),
    // Fire Red (Fire)
    darkColorScheme(primary = Color(0xFFFF4500), background = Color(0xFF200000), surface = Color(0xFF400000)),
    // Ocean Blue (Ocean)
    darkColorScheme(primary = Color(0xFF00FFFF), background = Color(0xFF001020), surface = Color(0xFF002040)),
    // Sunset Orange (Sunset)
    darkColorScheme(primary = Color(0xFFFF8C00), background = Color(0xFF201000), surface = Color(0xFF402000)),
    // Royal Purple (Purple)
    darkColorScheme(primary = Color(0xFF9370DB), background = Color(0xFF150025), surface = Color(0xFF300050)),
    // Deep Mint (Mint)
    darkColorScheme(primary = Color(0xFF98FF98), background = Color(0xFF002020), surface = Color(0xFF004040))
)

data class ThemeConfig(
    val name: String,
    val colorScheme: ColorScheme
)

val ThemeList = listOf(
    ThemeConfig("OLED Noir", Themes[0]),
    ThemeConfig("Cyberpunk", Themes[1]),
    ThemeConfig("Esmeralda", Themes[2]),
    ThemeConfig("Royal Gold", Themes[3]),
    ThemeConfig("Neon Violet", Themes[4]),
    ThemeConfig("Fire Red", Themes[5]),
    ThemeConfig("Ocean Blue", Themes[6]),
    ThemeConfig("Sunset Orange", Themes[7]),
    ThemeConfig("Royal Purple", Themes[8]),
    ThemeConfig("Deep Mint", Themes[9])
)
