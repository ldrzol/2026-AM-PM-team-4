package com.mintly.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mintly.app.ui.theme.거지방Colors
import com.mintly.app.ui.theme.Shape10
import com.mintly.app.ui.theme.Shape20
import com.mintly.app.ui.theme.ShapePill

// ─── 화면 상단 헤더 ────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun 거지방TopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.ArrowBackIosNew, contentDescription = "뒤로")
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White,
            titleContentColor = 거지방Colors.Gray900,
        ),
    )
}

// ─── 코인 뱃지 ─────────────────────────────────────────────
@Composable
fun CoinChip(
    amount: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = ShapePill,
        color = 거지방Colors.CoinBg,
        border = BorderStroke(1.dp, 거지방Colors.Coin.copy(alpha = 0.5f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("🪙", fontSize = 14.sp)
            Text(
                text = "%,d".format(amount),
                style = MaterialTheme.typography.labelMedium,
                color = 거지방Colors.CoinDark,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

// ─── 티켓 뱃지 ─────────────────────────────────────────────
@Composable
fun TicketChip(
    count: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = ShapePill,
        color = 거지방Colors.Mint50,
        border = BorderStroke(1.dp, 거지방Colors.Mint200),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("🎟️", fontSize = 14.sp)
            Text(
                text = "${count}장",
                style = MaterialTheme.typography.labelMedium,
                color = 거지방Colors.Mint700,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

// ─── 수입/지출 태그 ────────────────────────────────────────
@Composable
fun KindTag(
    kind: String,
    modifier: Modifier = Modifier,
) {
    val isIncome = kind == "income"
    Surface(
        modifier = modifier,
        shape = ShapePill,
        color = if (isIncome) 거지방Colors.IncomeBg else 거지방Colors.ExpenseBg,
    ) {
        Text(
            text = if (isIncome) "수입" else "지출",
            style = MaterialTheme.typography.labelSmall,
            color = if (isIncome) 거지방Colors.Income else 거지방Colors.Expense,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

// ─── 희귀도 뱃지 ───────────────────────────────────────────
@Composable
fun RarityBadge(rarity: String, modifier: Modifier = Modifier) {
    val (color, label) = when (rarity) {
        "legendary" -> 거지방Colors.Legendary to "전설"
        "epic"      -> 거지방Colors.Epic      to "에픽"
        "rare"      -> 거지방Colors.Rare      to "레어"
        else        -> 거지방Colors.Common    to "일반"
    }
    Surface(
        modifier = modifier,
        shape = ShapePill,
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.4f)),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
        )
    }
}

// ─── 기본 버튼 ─────────────────────────────────────────────
@Composable
fun 거지방Button(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier.height(52.dp),
        shape = ShapePill,
        colors = ButtonDefaults.buttonColors(containerColor = 거지방Colors.Mint400),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp,
            )
        } else {
            Text(text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ─── 외곽선 버튼 ───────────────────────────────────────────
@Composable
fun 거지방OutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(52.dp),
        shape = ShapePill,
        border = BorderStroke(1.dp, 거지방Colors.Mint400),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = 거지방Colors.Mint400),
    ) {
        Text(text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

// ─── 입력 필드 ─────────────────────────────────────────────
@Composable
fun 거지방TextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null,
    isPassword: Boolean = false,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default,
    singleLine: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        shape = Shape10,
        trailingIcon = trailingIcon,
        visualTransformation = if (isPassword)
            androidx.compose.ui.text.input.PasswordVisualTransformation()
        else
            androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = 거지방Colors.Mint400,
            unfocusedBorderColor = 거지방Colors.Gray200,
            focusedLabelColor    = 거지방Colors.Mint500,
        ),
    )
}

// ─── 에러/성공 스낵바 ──────────────────────────────────────
@Composable
fun 거지방Snackbar(
    message: String?,
    isError: Boolean = true,
    onDismiss: () -> Unit = {},
) {
    AnimatedVisibility(visible = message != null, enter = fadeIn(), exit = fadeOut()) {
        if (message != null) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = onDismiss) { Text("닫기", color = Color.White) }
                },
                containerColor = if (isError) 거지방Colors.Danger else 거지방Colors.Mint500,
            ) {
                Text(message, color = Color.White)
            }
        }
    }
}

// ─── 섹션 구분 헤더 ────────────────────────────────────────
@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = 거지방Colors.Gray500,
        modifier = modifier.padding(horizontal = 20.dp, vertical = 8.dp),
    )
}

// ─── 설정 행 ───────────────────────────────────────────────
@Composable
fun SettingRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String? = null,
    trailing: @Composable () -> Unit = {
        Icon(
            Icons.Rounded.ArrowBackIosNew,
            contentDescription = null,
            tint = 거지방Colors.Gray400,
            modifier = Modifier.rotate180(),
        )
    },
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        icon()
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = 거지방Colors.Gray500)
            }
        }
        trailing()
    }
}

private fun Modifier.rotate180() = this.rotate(180f)

// ─── 금액 텍스트 ───────────────────────────────────────────
@Composable
fun AmountText(
    amount: Int,
    kind: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge,
) {
    val color = if (kind == "income") 거지방Colors.Income else 거지방Colors.Expense
    val prefix = if (kind == "income") "+" else "-"
    Text(
        text = "$prefix %,d원".format(amount),
        style = style,
        color = color,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier,
    )
}

// ─── 로딩 오버레이 ─────────────────────────────────────────
@Composable
fun LoadingOverlay(visible: Boolean) {
    AnimatedVisibility(visible = visible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .clickable(enabled = false, indication = null, interactionSource = remember { MutableInteractionSource() }) {},
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = 거지방Colors.Mint400)
        }
    }
}

// ─── 빈 상태 ───────────────────────────────────────────────
@Composable
fun EmptyState(
    emoji: String = "📭",
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(emoji, fontSize = 48.sp)
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = 거지방Colors.Gray500,
            textAlign = TextAlign.Center,
        )
    }
}

// ─── 공유 유틸 ─────────────────────────────────────────────
fun getCategoryEmoji(icon: String): String = when (icon) {
    "restaurant"       -> "🍽️"
    "directions_bus"   -> "🚌"
    "local_cafe"       -> "☕"
    "shopping_bag"     -> "🛍️"
    "home"             -> "🏠"
    "medical_services" -> "💊"
    "movie"            -> "🎬"
    "payments"         -> "💰"
    "savings"          -> "💵"
    "attach_money"     -> "💸"
    "sports"           -> "🏋️"
    "music"            -> "🎵"
    "travel"           -> "✈️"
    "education"        -> "📚"
    "phone"            -> "📱"
    "game"             -> "🎮"
    "gift"             -> "🎁"
    "fuel"             -> "⛽"
    "car"              -> "🚗"
    "pet"              -> "🐶"
    "beauty"           -> "💅"
    "grocery"          -> "🛒"
    "work"             -> "💼"
    "food"             -> "🍔"
    "drinks"           -> "🍺"
    else               -> "💳"
}

val categoryIconOptions: List<Pair<String, String>> = listOf(
    "restaurant" to "🍽️", "food" to "🍔", "local_cafe" to "☕", "drinks" to "🍺",
    "grocery" to "🛒",    "shopping_bag" to "🛍️", "beauty" to "💅", "home" to "🏠",
    "medical_services" to "💊", "directions_bus" to "🚌", "car" to "🚗", "fuel" to "⛽",
    "travel" to "✈️",    "movie" to "🎬", "game" to "🎮", "music" to "🎵",
    "sports" to "🏋️",   "education" to "📚", "phone" to "📱", "work" to "💼",
    "pet" to "🐶",       "gift" to "🎁", "savings" to "💵", "payments" to "💰",
    "attach_money" to "💸",
)

fun String.toComposeColor(fallback: Color = 거지방Colors.Gray400): Color =
    try { Color(android.graphics.Color.parseColor(this)) }
    catch (e: Exception) { fallback }
