package com.mintly.app.ui.shop

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mintly.app.data.model.Costume
import com.mintly.app.data.model.DEFAULT_ROULETTE_SEGMENTS
import com.mintly.app.data.model.RouletteSegment
import com.mintly.app.ui.components.*
import com.mintly.app.ui.theme.거지방Colors
import com.mintly.app.ui.theme.Shape14
import com.mintly.app.ui.theme.Shape20
import com.mintly.app.ui.theme.ShapePill
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ShopScreen(vm: ShopViewModel = hiltViewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf("roulette") }

    // 에러/성공 메시지 자동 해제
    LaunchedEffect(state.error, state.successMsg) {
        if (state.error != null || state.successMsg != null) {
            kotlinx.coroutines.delay(2500)
            vm.clearMessages()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        // ─── 헤더 ─────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("샵", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CoinChip(amount = state.coins)
                TicketChip(count = state.ticketCount)
            }
        }

        // ─── 탭 스위처 ────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(ShapePill)
                .background(거지방Colors.Gray100)
                .padding(4.dp),
        ) {
            listOf("roulette" to "🎰 룰렛", "store" to "🛍️ 코스튬 상점").forEach { (id, label) ->
                val active = tab == id
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(ShapePill)
                        .background(if (active) Color.White else Color.Transparent)
                        .clickable { tab = id }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                        color = if (active) 거지방Colors.Mint700 else 거지방Colors.Gray500,
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        if (tab == "roulette") {
            RouletteTab(state = state, onSpin = vm::spinRoulette)
        } else {
            CostumeStoreTab(
                state  = state,
                onBuy  = vm::purchaseCostume,
            )
        }
    }

    // ─── 결과 다이얼로그 ───────────────────────────────────────
    state.spinResult?.let { result ->
        SpinResultDialog(result = result, onDismiss = vm::clearMessages)
    }

    // ─── 메시지 스낵바 ─────────────────────────────────────────
    if (state.error != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            거지방Snackbar(message = state.error, isError = true, onDismiss = vm::clearMessages)
        }
    }
}

// ─── 룰렛 탭 ───────────────────────────────────────────────
@Composable
private fun RouletteTab(state: ShopUiState, onSpin: () -> Unit) {
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(state.isSpinning) {
        if (state.isSpinning) {
            rotation.animateTo(
                targetValue = rotation.value + 360f * 5 + (0..360).random().toFloat(),
                animationSpec = tween(
                    durationMillis = 3000,
                    easing = CubicBezierEasing(0.15f, 0.85f, 0.25f, 1f),
                ),
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // 룰렛 휠 그리기
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(260.dp),
        ) {
            Canvas(
                modifier = Modifier
                    .size(240.dp)
                    .rotate(rotation.value),
            ) {
                val segments = DEFAULT_ROULETTE_SEGMENTS
                val sweepAngle = 360f / segments.size
                val radius = size.minDimension / 2f
                val center = this.center

                segments.forEachIndexed { i, seg ->
                    val startAngle = i * sweepAngle - 90f
                    // 조각 그리기
                    drawArc(
                        color = Color(seg.color),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                    )
                    // 테두리
                    drawArc(
                        color = Color.White,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f),
                    )
                }
                // 중앙 원
                drawCircle(color = Color.White, radius = radius * 0.15f, center = center)
            }

            // 세그먼트 이모지/텍스트 오버레이
            val segments = DEFAULT_ROULETTE_SEGMENTS
            val sweepAngle = 360f / segments.size
            segments.forEachIndexed { i, seg ->
                val angle = Math.toRadians((i * sweepAngle + sweepAngle / 2 - 90 + rotation.value).toDouble())
                val r = 80f
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .offset(
                            x = (cos(angle) * r).dp,
                            y = (sin(angle) * r).dp,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(seg.emoji, fontSize = 18.sp)
                }
            }

            // 포인터 (위쪽 삼각형)
            Text(
                "▼",
                fontSize = 28.sp,
                color = 거지방Colors.Mint400,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-12).dp),
            )
        }

        // 스핀 버튼
        Button(
            onClick = onSpin,
            enabled = !state.isSpinning && state.ticketCount > 0,
            shape = ShapePill,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = 거지방Colors.Mint400,
                disabledContainerColor = 거지방Colors.Gray200,
            ),
        ) {
            if (state.isSpinning) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Icon(Icons.Rounded.Casino, null)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (state.ticketCount > 0) "룰렛 돌리기 (티켓 1장)" else "티켓이 없습니다",
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        // 보상 목록
        Surface(shape = Shape20, color = 거지방Colors.Gray50, border = BorderStroke(1.dp, 거지방Colors.Gray200)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("보상 목록", style = MaterialTheme.typography.labelMedium, color = 거지방Colors.Gray500)
                DEFAULT_ROULETTE_SEGMENTS.forEach { seg ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(seg.emoji, fontSize = 18.sp)
                        Text(seg.label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

// ─── 코스튬 상점 탭 ────────────────────────────────────────
@Composable
private fun CostumeStoreTab(state: ShopUiState, onBuy: (Costume) -> Unit) {
    val hats    = state.shopCostumes.filter { it.kind == "hat" }
    val outfits = state.shopCostumes.filter { it.kind == "outfit" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (hats.isNotEmpty()) {
            Text("모자", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.height(((hats.size / 3 + 1) * 130).dp),
                userScrollEnabled = false,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(hats) { costume ->
                    CostumeCard(
                        costume = costume,
                        owned   = costume.id in state.ownedCostumeIds,
                        coins   = state.coins,
                        onBuy   = { onBuy(costume) },
                    )
                }
            }
        }

        if (outfits.isNotEmpty()) {
            Text("옷", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.height(((outfits.size / 3 + 1) * 130).dp),
                userScrollEnabled = false,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(outfits) { costume ->
                    CostumeCard(
                        costume = costume,
                        owned   = costume.id in state.ownedCostumeIds,
                        coins   = state.coins,
                        onBuy   = { onBuy(costume) },
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun CostumeCard(costume: Costume, owned: Boolean, coins: Int, onBuy: () -> Unit) {
    val canAfford = coins >= costume.price
    Surface(
        shape = Shape14,
        color = if (owned) 거지방Colors.Mint50 else 거지방Colors.Gray50,
        border = BorderStroke(
            if (owned) 2.dp else 1.dp,
            if (owned) 거지방Colors.Mint400 else 거지방Colors.Gray200,
        ),
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(costume.icon, fontSize = 28.sp)
            Text(
                costume.name,
                style = MaterialTheme.typography.labelSmall,
                color = 거지방Colors.Gray800,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            )
            RarityBadge(costume.rarity)
            if (owned) {
                Text("보유중", style = MaterialTheme.typography.labelSmall, color = 거지방Colors.Mint600,
                    fontWeight = FontWeight.Bold)
            } else {
                Surface(
                    shape = ShapePill,
                    color = if (canAfford) 거지방Colors.Mint400 else 거지방Colors.Gray200,
                    modifier = Modifier.clickable(enabled = canAfford, onClick = onBuy),
                ) {
                    Text(
                        "🪙 ${costume.price}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (canAfford) Color.White else 거지방Colors.Gray500,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

// ─── 스핀 결과 다이얼로그 ─────────────────────────────────
@Composable
private fun SpinResultDialog(result: RouletteSegment, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = Shape20, color = Color.White) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("🎉", fontSize = 48.sp)
                Text(
                    result.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    when (result.rewardType) {
                        "coins"   -> "🪙 ${result.rewardValue}코인 획득!"
                        "hat"     -> "🎩 새 모자 획득!"
                        "outfit"  -> "👔 새 옷 획득!"
                        else      -> "아쉽게도 꽝... 다음에 다시 도전하세요!"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = 거지방Colors.Gray700,
                )
                Button(
                    onClick = onDismiss,
                    shape = ShapePill,
                    colors = ButtonDefaults.buttonColors(containerColor = 거지방Colors.Mint400),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("확인")
                }
            }
        }
    }
}
