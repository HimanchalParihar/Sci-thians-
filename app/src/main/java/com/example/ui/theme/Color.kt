package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Utopia Radiant Modern Civic Theme: Midnight Sapphire + Electric Rose, Solar Saffron & Neon Cyan
val UtopiaRed = Color(0xFFFF3366)          // Electric Rose / Hot Crimson
val UtopiaRedDark = Color(0xFFC70039)
val UtopiaRedLight = Color(0xFFFF6584)

val UtopiaSaffron = Color(0xFFFF9E00)      // Solar Saffron
val UtopiaOrange = Color(0xFFFF6D00)       // Vivid Flame Orange
val UtopiaGold = Color(0xFFFFD166)         // Radiant Amber Gold

// Surfaces - Deep Obsidian & Rich Midnight Sapphire (far richer than stark pitch black)
val UtopiaBlackBg = Color(0xFF0A0F1D)      // Midnight Navy Slate
val UtopiaSurfaceDark = Color(0xFF131D31)  // Deep Sapphire Glass
val UtopiaSurfaceElevatedDark = Color(0xFF1B2842) // Elevated card surface
val UtopiaSurfaceGlass = Color(0xDD16223A)
val UtopiaBorderDark = Color(0xFF27385A)   // High-contrast clean card border
val UtopiaBorderGlow = Color(0x6600F5D4)   // Radiant Cyan Border Glow

// Typography & Text
val UtopiaTextWhite = Color(0xFFF8FAFC)
val UtopiaTextMuted = Color(0xFF94A3B8)
val UtopiaTextFaint = Color(0xFF64748B)

// Light Theme
val UtopiaLightBg = Color(0xFFF1F5F9)
val UtopiaSurfaceLight = Color(0xFFFFFFFF)
val UtopiaSurfaceElevatedLight = Color(0xFFE2E8F0)
val UtopiaBorderLight = Color(0xFFCBD5E1)
val UtopiaTextBlack = Color(0xFF0F172A)
val UtopiaTextMutedLight = Color(0xFF475569)

// High-Energy Civic Accents
val CivicGreen = Color(0xFF00E676)         // Energetic Emerald Mint
val CivicGreenGlow = Color(0x4400E676)
val CivicAmber = Color(0xFFFFB703)
val CivicBlue = Color(0xFF38BDF8)          // Vibrant Sky Cyan
val CivicCyan = Color(0xFF00F5D4)          // Electric Neon Teal
val CivicPurple = Color(0xFFA855F7)        // Vivid Amethyst
val CivicPink = Color(0xFFFF007F)          // Cyber Neon Pink

// Dynamic Gradient Brushes
val CivicEmberBrush = Brush.horizontalGradient(
    colors = listOf(UtopiaRed, UtopiaOrange, UtopiaSaffron)
)

val CivicMintBrush = Brush.horizontalGradient(
    colors = listOf(CivicGreen, CivicCyan)
)

val CivicNeonBrush = Brush.horizontalGradient(
    colors = listOf(CivicCyan, CivicBlue, CivicPurple)
)

val CivicSunsetBrush = Brush.horizontalGradient(
    colors = listOf(CivicPink, UtopiaRed, UtopiaSaffron)
)

val CivicCardBorderBrush = Brush.linearGradient(
    colors = listOf(
        Color(0x7700F5D4),
        Color(0x33FF3366),
        Color(0x44FFB703)
    )
)

val HeroHeaderBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF1A1A3A),
        Color(0xFF131D31),
        UtopiaBlackBg
    )
)
