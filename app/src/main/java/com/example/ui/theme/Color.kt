package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// SK Mobile Shoppy and Electronic - Premium Black + Gold Color Palette
// Core Palette specified in User Request
val BlackBackground = Color(0xFF0B0B0B)        // Primary background: #0B0B0B
val BlackSurface = Color(0xFF171717)           // Surface/cards: #171717
val BlackSurfaceSecondary = Color(0xFF222222)  // Secondary surface: #222222
val GoldPrimary = Color(0xFFD4AF37)            // Gold primary/accent: #D4AF37
val GoldHighlight = Color(0xFFF5D76E)          // Gold highlight: #F5D76E
val TextMain = Color(0xFFFFFFFF)               // Main text: #FFFFFF
val TextSecondary = Color(0xFFC5C5C5)          // Secondary text: #C5C5C5
val BlackBorder = Color(0xFF383838)            // Borders/dividers: #383838
val ColorError = Color(0xFFCF6679)             // Error color: #CF6679

// Complementary Accent Colors
val GoldDark = Color(0xFFA68523)
val TextMuted = Color(0xFF888888)
val ColorSuccess = Color(0xFF4ADE80)
val GoldBadgeBg = Color(0xFF2E2714)

// Backward-compatible mappings for existing UI components
val ShopPrimaryBlue = GoldPrimary              // #D4AF37 Gold primary
val ShopPrimaryDarkBlue = GoldDark             // #A68523 Deep Gold
val ShopPrimaryLightBlue = BlackSurfaceSecondary // #222222 Secondary Surface

val ShopAccentCyan = GoldPrimary               // #D4AF37
val ShopAccentAmber = GoldHighlight            // #F5D76E Gold Highlight
val ShopSuccessGreen = ColorSuccess            // #4ADE80
val ShopDiscountGreen = GoldHighlight          // #F5D76E
val ShopDiscountBadgeBg = GoldBadgeBg          // #2E2714 Subtle Gold Tint

val ShopBackgroundLight = BlackBackground      // #0B0B0B Primary Background
val ShopSurfaceLight = BlackSurface            // #171717 Surface/Cards
val ShopSurfaceVariantLight = BlackSurfaceSecondary // #222222 Secondary Surface
val ShopOutlineLight = BlackBorder             // #383838 Borders/Dividers

val ShopTextPrimary = TextMain                 // #FFFFFF Main Text
val ShopTextSecondary = TextSecondary          // #C5C5C5 Secondary Text
val ShopTextMuted = TextMuted                  // #888888 Muted Text

// Dark Theme Variants (fully aligned with Black + Gold palette)
val ShopPrimaryBlueDark = GoldPrimary
val ShopBackgroundDark = BlackBackground
val ShopSurfaceDark = BlackSurface
val ShopSurfaceVariantDark = BlackSurfaceSecondary
val ShopTextPrimaryDark = TextMain
val ShopTextSecondaryDark = TextSecondary
