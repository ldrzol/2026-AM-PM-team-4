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
            Text("샵", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CoinChip(amount = state.coins)
                TicketChip(count = state.ticketCount)
            }
        }

        // ─── 탭 스위처 ────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf("roulette" to "🎲 룰렛", "store" to "🛍️ 코스튬 상점").forEach { (id, label) ->
                val active = tab == id
                Surface(
                    shape    = ShapePill,
                    color    = if (active) Color.White else Color.Transparent,
                    border   = if (active) BorderStroke(1.5.dp, 거지방Colors.Mint400) else null,
                    modifier = Modifier.clickable { tab = id },
                ) {
                    Text(
                        label,
                        style      = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                        color      = if (active) 거지방Colors.Mint600 else 거지방Colors.Gray400,
                        modifier   = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        if (tab == "roulette") {
            RouletteTab(state = state, onSpin = vm::spinRoulette)
        } else {
            CostumeStoreTab(
                state       = state,
                onBuy       = vm::purchaseCostume,
                onBuyTicket = vm::purchaseTicket,
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
                targetValue = rotation.value + 360f * 6 + (0..360).random().toFloat(),
                animationSpec = tween(
                    durationMillis = 3500,
                    easing = CubicBezierEasing(0.05f, 0.9f, 0.1f, 1f),
                ),
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {

        // ── 룰렛 카드 ────────────────────────────────────────
        Surface(
            shape  = Shape20,
            color  = Color.White,
            border = BorderStroke(1.dp, 거지방Colors.Gray200),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp),
        ) {

        // ── 룰렛 휠 ─────────────────────────────────────────
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(280.dp),
        ) {
            // 베젤 링
            Canvas(modifier = Modifier.size(280.dp)) {
                drawCircle(color = Color(0xFFDDDDDD), radius = size.minDimension / 2f)
            }

            // 휠 본체 (단색 슬라이스)
            Canvas(
                modifier = Modifier
                    .size(260.dp)
                    .rotate(rotation.value),
            ) {
                val segments = DEFAULT_ROULETTE_SEGMENTS
                val sweepAngle = 360f / segments.size
                val radius = size.minDimension / 2f
                val center = this.center

                segments.forEachIndexed { i, seg ->
                    val startAngle = i * sweepAngle - 90f
                    drawArc(
                        color = Color(seg.color),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                    )
                    // 슬라이스 구분선
                    drawArc(
                        color = Color.White,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f),
                    )
                }
                // 중앙 허브
                drawCircle(color = Color.White, radius = radius * 0.17f, center = center)
                drawCircle(
                    color = Color(0xFFDDDDDD),
                    radius = radius * 0.17f,
                    center = center,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f),
                )
            }

            // 세그먼트 레이블 오버레이
            val segments = DEFAULT_ROULETTE_SEGMENTS
            val sweepAngle = 360f / segments.size
            segments.forEachIndexed { i, seg ->
                val angleDeg = i * sweepAngle + sweepAngle / 2 - 90 + rotation.value
                val angleRad = Math.toRadians(angleDeg.toDouble())
                val r = 80f
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .offset(
                            x = (cos(angleRad) * r).dp,
                            y = (sin(angleRad) * r).dp,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(seg.emoji, fontSize = 16.sp)
                        Text(
                            seg.label,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            // 포인터
            Text(
                "▼",
                fontSize = 26.sp,
                color = 거지방Colors.Mint400,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-10).dp),
            )
        }

        // ── 스핀 버튼 ────────────────────────────────────────
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
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
        ) {
            if (state.isSpinning) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.5.dp)
                Spacer(Modifier.width(8.dp))
                Text("돌리는 중...", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            } else {
                Icon(Icons.Rounded.Casino, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    if (state.ticketCount > 0) "룰렛 돌리기  ·  티켓 ${state.ticketCount}장" else "티켓이 없습니다",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                )
            }
        }

        } // Column 닫기
        } // Surface 닫기

        // ── 룰렛권 얻는 법 ───────────────────────────────────
        Surface(
            shape  = Shape20,
            color  = 거지방Colors.Gray50,
            border = BorderStroke(1.dp, 거지방Colors.Gray200),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "룰렛권 얻는 법",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = 거지방Colors.Gray600,
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.EmojiEvents, null, tint = 거지방Colors.Mint500, modifier = Modifier.size(16.dp))
                    Text("그룹에서 1등 하면 2장 지급", style = MaterialTheme.typography.bodyMedium, color = 거지방Colors.Gray700)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.ShoppingCart, null, tint = 거지방Colors.Mint500, modifier = Modifier.size(16.dp))
                    Text("상점에서 코인 300개로 구매 가능", style = MaterialTheme.typography.bodyMedium, color = 거지방Colors.Gray700)
                }
            }
        }

        // ── 보상 목록 (그리드 스타일) ────────────────────────
        Surface(
            shape = Shape20,
            color = 거지방Colors.Gray50,
            border = BorderStroke(1.dp, 거지방Colors.Gray200),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "보상 목록",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = 거지방Colors.Gray600,
                )
                DEFAULT_ROULETTE_SEGMENTS.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        row.forEach { seg ->
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = Shape14,
                                color = Color(seg.color).copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, Color(seg.color).copy(alpha = 0.3f)),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Text(seg.emoji, fontSize = 18.sp)
                                    Text(
                                        seg.label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = 거지방Colors.Gray800,
                                    )
                                }
                            }
                        }
                        // 홀수 개일 때 빈 칸 채우기
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

// ─── 코스튬 상점 탭 ────────────────────────────────────────
@Composable
private fun CostumeStoreTab(state: ShopUiState, onBuy: (Costume) -> Unit, onBuyTicket: () -> Unit) {
    val accessories = state.shopCostumes.filter { it.kind == "hat" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // ── 룰렛 티켓 ──────────────────────────────────────
        Text("룰렛 티켓 🎟️", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        TicketPurchaseCard(coins = state.coins, onBuy = onBuyTicket)

        // ── 헤어 악세서리 ───────────────────────────────────
        if (accessories.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("준비 중입니다 🎀", style = MaterialTheme.typography.bodyLarge, color = 거지방Colors.Gray400)
            }
        } else {
            Text("헤어 악세서리 🎀", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.height(((accessories.size / 3 + 1) * 130).dp),
                userScrollEnabled = false,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(accessories) { costume ->
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
private fun TicketPurchaseCard(coins: Int, onBuy: () -> Unit) {
    val canAfford = coins >= 300
    Surface(
        shape = Shape14,
        color = if (canAfford) 거지방Colors.Mint50 else 거지방Colors.Gray50,
        border = BorderStroke(
            if (canAfford) 1.5.dp else 1.dp,
            if (canAfford) 거지방Colors.Mint300 else 거지방Colors.Gray200,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("🎟️", fontSize = 34.sp)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    "룰렛 티켓 1장",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = 거지방Colors.Gray900,
                )
                Text(
                    "룰렛을 한 번 돌릴 수 있어요",
                    style = MaterialTheme.typography.bodySmall,
                    color = 거지방Colors.Gray500,
                )
            }
            Surface(
                shape = ShapePill,
                color = if (canAfford) 거지방Colors.Mint400 else 거지방Colors.Gray200,
                modifier = Modifier.clickable(enabled = canAfford, onClick = onBuy),
            ) {
                Text(
                    "🪙 300",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (canAfford) androidx.compose.ui.graphics.Color.White else 거지방Colors.Gray500,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        }
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
                style = MaterialTheme.typography.bodySmall,
                color = 거지방Colors.Gray800,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            )
            RarityBadge(costume.rarity)
            if (owned) {
                Text("보유중", style = MaterialTheme.typography.bodySmall, color = 거지방Colors.Mint600,
                    fontWeight = FontWeight.Bold)
            } else {
                Surface(
                    shape = ShapePill,
                    color = if (canAfford) 거지방Colors.Mint400 else 거지방Colors.Gray200,
                    modifier = Modifier.clickable(enabled = canAfford, onClick = onBuy),
                ) {
                    Text(
                        "🪙 ${costume.price}",
                        style = MaterialTheme.typography.bodySmall,
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
                    style = MaterialTheme.typography.titleLarge,
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
