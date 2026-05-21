package com.mintly.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object 거지방Colors {
    // Mint scale (브랜드 컬러)
    val Mint50  = Color(0xFFF0F8F6)
    val Mint100 = Color(0xFFDBEEEA)
    val Mint200 = Color(0xFFB8DDD4)
    val Mint300 = Color(0xFF95CCBE)
    val Mint400 = Color(0xFF74BBAE)  // PRIMARY
    val Mint500 = Color(0xFF5BA89A)
    val Mint600 = Color(0xFF468A7E)
    val Mint700 = Color(0xFF356B62)
    val Mint800 = Color(0xFF244D46)
    val Mint900 = Color(0xFF142E2A)
    val MintLight = Color(0xFF85CBBE)
    val MintTint  = Color(0xFF9BEBDD)

    // Neutrals (cool light gray)
    val Gray0   = Color(0xFFFFFFFF)
    val Gray50  = Color(0xFFFAFAFB)
    val Gray100 = Color(0xFFF4F5F6)
    val Gray200 = Color(0xFFE8EAEC)
    val Gray300 = Color(0xFFD5D9DC)
    val Gray400 = Color(0xFFA8AFB5)
    val Gray500 = Color(0xFF767D84)
    val Gray600 = Color(0xFF555B61)
    val Gray700 = Color(0xFF3A3E43)
    val Gray800 = Color(0xFF23262A)
    val Gray900 = Color(0xFF131517)

    // Semantic
    val Income     = Mint500
    val IncomeBg   = Mint50
    val Expense    = Color(0xFFE5896B)
    val ExpenseBg  = Color(0xFFFBEDE7)
    val Coin       = Color(0xFFE8B547)
    val CoinDark   = Color(0xFFB8862A)
    val Danger     = Color(0xFFDC5B5B)
    val Info       = Color(0xFF6BA3D6)
    val Warning    = Color(0xFFE8B547)

    // Rarity colors
    val Common    = Gray400
    val Rare      = Info
    val Epic      = Color(0xFF9C7DD9)
    val Legendary = Coin

    // Coin chip
    val CoinBg    = Color(0xFFFFF8E7)

    // Rank badge
    val RankFirstBg     = Color(0xFFFFF8E7)
    val RankLoserBg     = Color(0xFFF5F0EB)
    val RankLoserBorder = Color(0xFF9C7D6B)
    val RankLoserText   = Color(0xFF6B5740)
    val Rank2           = Color(0xFFB8C0CC)
    val Rank3           = Color(0xFFB87333)
}

val 거지방LightColorScheme = lightColorScheme(
    primary       = 거지방Colors.Mint400,
    onPrimary     = Color.White,
    primaryContainer = 거지방Colors.Mint100,
    onPrimaryContainer = 거지방Colors.Mint900,
    secondary     = 거지방Colors.Mint600,
    onSecondary   = Color.White,
    background    = 거지방Colors.Gray50,
    onBackground  = 거지방Colors.Gray900,
    surface       = 거지방Colors.Gray0,
    onSurface     = 거지방Colors.Gray900,
    surfaceVariant = 거지방Colors.Gray100,
    onSurfaceVariant = 거지방Colors.Gray600,
    outline       = 거지방Colors.Gray200,
    outlineVariant = 거지방Colors.Gray300,
    error         = 거지방Colors.Danger,
    onError       = Color.White,
)
