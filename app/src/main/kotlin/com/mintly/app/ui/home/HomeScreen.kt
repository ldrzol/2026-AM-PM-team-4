package com.mintly.app.ui.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mintly.app.data.model.RankedMember
import com.mintly.app.data.model.UserCostume
import com.mintly.app.ui.components.*
import com.mintly.app.ui.theme.거지방Colors
import com.mintly.app.ui.theme.Shape14
import com.mintly.app.ui.theme.Shape20
import com.mintly.app.ui.theme.ShapePill
import java.time.LocalDate

@Composable
fun HomeScreen(
    vm: HomeViewModel = hiltViewModel(),
    onNavigateToBudget: () -> Unit = {},
    onNavigateToRanking: () -> Unit = {},
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    var showWardrobe     by remember { mutableStateOf(false) }
    var showAvatarEditor by remember { mutableStateOf(false) }
    val today = remember { LocalDate.now() }

    LaunchedEffect(Unit) { vm.load() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState()),
    ) {
        // ─── 헤더 ─────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 24.dp, bottom = 4.dp),
        ) {
            Column(modifier = Modifier.align(Alignment.CenterStart)) {
                Text(
                    "안녕, ${state.profile?.displayName ?: ""}님",
                    style = MaterialTheme.typography.bodyMedium,
                    color = 거지방Colors.Gray500,
                )
                Text(
                    "오늘도 절약 시작! 🌱",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = 거지방Colors.Gray900,
                )
            }
            CoinChip(
                amount   = state.profile?.coins ?: 0,
                modifier = Modifier.align(Alignment.TopEnd),
            )
        }

        // ─── 아바타 ───────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp),
            contentAlignment = Alignment.Center,
        ) {
            // 글로우 배경
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(거지방Colors.Mint100, Color.Transparent),
                        )
                    ),
            )
            거지방Avatar(
                size    = 172.dp,
                color   = state.profile?.avatarColor ?: "mint",
                face    = state.profile?.avatarFace?.toAvatarFace() ?: AvatarFace(),
                hat     = state.profile?.let { if (it.hasCrownUntil != null) "crown" else it.currentHat },
                outfit  = state.profile?.let { it.forcedOutfit ?: it.currentOutfit },
                forced  = state.profile?.forcedOutfit != null,
                modifier = Modifier.clickable { showAvatarEditor = true },
            )
            // 강제 옷 뱃지
            if (state.profile?.forcedOutfit != null) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFFF3E0),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-40).dp, y = (-10).dp),
                ) {
                    Text("🧺", fontSize = 14.sp, modifier = Modifier.padding(4.dp))
                }
            }
        }

        // ─── 꾸미기 / 옷장 버튼 ──────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                onClick = { showAvatarEditor = true },
                shape   = ShapePill,
                border  = BorderStroke(1.dp, 거지방Colors.Mint300),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            ) {
                Icon(Icons.Rounded.Edit, null, modifier = Modifier.size(14.dp), tint = 거지방Colors.Mint500)
                Spacer(Modifier.width(4.dp))
                Text("꾸미기", style = MaterialTheme.typography.labelMedium, color = 거지방Colors.Mint600, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(10.dp))
            OutlinedButton(
                onClick = { showWardrobe = true },
                shape   = ShapePill,
                border  = BorderStroke(1.dp, 거지방Colors.Gray200),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            ) {
                Icon(Icons.Rounded.Checkroom, null, modifier = Modifier.size(14.dp), tint = 거지방Colors.Gray400)
                Spacer(Modifier.width(4.dp))
                Text("옷장", style = MaterialTheme.typography.labelMedium, color = 거지방Colors.Gray600, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(20.dp))

        // ─── 출석체크 카드 ────────────────────────────────────
        CheckInCard(
            today           = today,
            weekDays        = state.checkinWeekDays,
            streak          = state.profile?.checkStreak ?: 0,
            hasCheckedToday = state.hasCheckedToday,
            checkInMessage  = state.checkInMessage,
            onCheckIn       = vm::checkIn,
            modifier        = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(Modifier.height(12.dp))

        // ─── 오늘 날짜 카드 ───────────────────────────────────
        TodaySpendingCard(
            date    = today,
            income  = state.todayIncome,
            expense = state.todayExpense,
            onClick = onNavigateToBudget,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(Modifier.height(16.dp))

        // ─── 이번 달 ──────────────────────────────────────────
        Text(
            "이번 달",
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color      = 거지방Colors.Gray900,
            modifier   = Modifier.padding(horizontal = 20.dp, vertical = 2.dp),
        )
        Spacer(Modifier.height(8.dp))
        MonthlyCard(
            income   = state.monthlyIncome,
            expense  = state.monthlyExpense,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(Modifier.height(20.dp))

        // ─── 친구들 오늘 지출 ─────────────────────────────────
        if (state.friendsRanking.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "친구들 오늘 지출",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = 거지방Colors.Gray900,
                )
                TextButton(onClick = onNavigateToRanking, contentPadding = PaddingValues(0.dp)) {
                    Text(
                        "전체 보기 >",
                        style      = MaterialTheme.typography.bodySmall,
                        color      = 거지방Colors.Mint500,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            FriendsSpendingRow(
                members = state.friendsRanking,
                myId    = state.profile?.id ?: "",
            )
            Spacer(Modifier.height(20.dp))
        }
    }

    // ─── 옷장 BottomSheet ─────────────────────────────────────
    if (showWardrobe) {
        WardrobeBottomSheet(
            profile       = state.profile,
            inventory     = state.inventory,
            onEquipHat    = { vm.equipHat(it) },
            onEquipOutfit = { vm.equipOutfit(it) },
            onDismiss     = { showWardrobe = false },
        )
    }

    // ─── 아바타 편집 BottomSheet ──────────────────────────────
    if (showAvatarEditor) {
        AvatarEditorBottomSheet(
            currentColor = state.profile?.avatarColor ?: "mint",
            currentFace  = state.profile?.avatarFace?.toAvatarFace() ?: AvatarFace(),
            onSave       = { color, face -> vm.updateAvatar(color, face.toMap()) },
            onDismiss    = { showAvatarEditor = false },
        )
    }
}

// ─── 출석체크 카드 ─────────────────────────────────────────
@Composable
private fun CheckInCard(
    today: LocalDate,
    weekDays: List<Boolean>,
    streak: Int,
    hasCheckedToday: Boolean,
    checkInMessage: String?,
    onCheckIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val todayIndex = today.dayOfWeek.value - 1  // Mon=0, Sun=6
    val dayNames = listOf("월", "화", "수", "목", "금", "토", "일")

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape    = Shape20,
        color    = Color(0xFFFFF9E6),
        border   = BorderStroke(1.dp, Color(0xFFFFE082)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 헤더
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("출석체크", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Surface(shape = ShapePill, color = Color(0xFFFFD54F)) {
                    Text(
                        "+5 코인",
                        style    = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color    = Color(0xFF7A5A00),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }
            Text(
                if (streak > 0) "${streak}일 연속 · 7일마다 보너스 50코인"
                else "7일마다 보너스 50코인",
                style = MaterialTheme.typography.bodySmall,
                color = 거지방Colors.Gray500,
            )

            // 요일 그리드
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                dayNames.forEachIndexed { i, name ->
                    val isChecked = weekDays.getOrElse(i) { false }
                    val isToday   = i == todayIndex
                    val isFuture  = i > todayIndex

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            name,
                            style    = MaterialTheme.typography.labelSmall,
                            color    = 거지방Colors.Gray400,
                            fontSize = 11.sp,
                        )
                        when {
                            isChecked -> {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(거지방Colors.Mint400),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                            isToday -> {
                                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .drawBehind {
                                            drawCircle(
                                                color  = 거지방Colors.Mint400,
                                                radius = size.minDimension / 2f - 1.5f,
                                                style  = Stroke(width = 2f, pathEffect = dashEffect),
                                            )
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        "${today.dayOfMonth}",
                                        style      = MaterialTheme.typography.labelMedium,
                                        color      = 거지방Colors.Mint500,
                                        fontWeight = FontWeight.Bold,
                                        fontSize   = 12.sp,
                                    )
                                }
                            }
                            else -> {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isFuture) Color.Transparent
                                            else 거지방Colors.Gray100
                                        ),
                                )
                            }
                        }
                    }
                }
            }

            // 도장 찍기 버튼
            Button(
                onClick  = onCheckIn,
                enabled  = !hasCheckedToday,
                shape    = ShapePill,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = 거지방Colors.Mint400,
                    disabledContainerColor = 거지방Colors.Gray200,
                ),
            ) {
                Text(
                    if (hasCheckedToday) "오늘 도장 완료 ✅" else "오늘 도장 찍기 ✏️",
                    fontWeight = FontWeight.Bold,
                    color      = if (hasCheckedToday) 거지방Colors.Gray400 else Color.White,
                    fontSize   = 15.sp,
                )
            }

            // 성공 메시지
            if (checkInMessage != null) {
                Text(
                    checkInMessage,
                    style      = MaterialTheme.typography.bodyMedium,
                    color      = 거지방Colors.Mint600,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.fillMaxWidth(),
                    textAlign  = TextAlign.Center,
                )
            }
        }
    }
}

// ─── 오늘 날짜 카드 ───────────────────────────────────────
@Composable
private fun TodaySpendingCard(
    date: LocalDate,
    income: Int,
    expense: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape    = Shape20,
        color    = Color.White,
        border   = BorderStroke(1.dp, 거지방Colors.Gray200),
        shadowElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "오늘 ${date.monthValue}월 ${date.dayOfMonth}일",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = 거지방Colors.Gray900,
                    )
                    Text(
                        "탭해서 지출/수입 추가",
                        style = MaterialTheme.typography.bodySmall,
                        color = 거지방Colors.Gray400,
                    )
                }
                Icon(Icons.Rounded.KeyboardArrowRight, null, tint = 거지방Colors.Gray400)
            }
            HorizontalDivider(color = 거지방Colors.Gray100)
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text("지출", style = MaterialTheme.typography.bodySmall, color = 거지방Colors.Gray500)
                    Text(
                        "%,d원".format(expense),
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = 거지방Colors.Expense,
                    )
                }
                VerticalDivider(modifier = Modifier.height(44.dp), color = 거지방Colors.Gray100)
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text("수입", style = MaterialTheme.typography.bodySmall, color = 거지방Colors.Gray500)
                    Text(
                        "%,d원".format(income),
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = 거지방Colors.Income,
                    )
                }
            }
        }
    }
}

// ─── 이번 달 카드 ─────────────────────────────────────────
@Composable
private fun MonthlyCard(
    income: Int,
    expense: Int,
    modifier: Modifier = Modifier,
) {
    val remaining = income - expense
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape    = Shape20,
        color    = Color.White,
        border   = BorderStroke(1.dp, 거지방Colors.Gray200),
        shadowElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("이번 달 지출", style = MaterialTheme.typography.bodyMedium, color = 거지방Colors.Gray600)
                Text(
                    "%,d원".format(expense),
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color      = 거지방Colors.Expense,
                )
            }
            // 진행 바 (수입 대비 지출)
            if (income > 0) {
                val progress = (expense.toFloat() / income).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(ShapePill)
                        .background(거지방Colors.Gray100),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .clip(ShapePill)
                            .background(거지방Colors.Expense),
                    )
                }
            }
            // 수입 | 지출 | 잔여
            Row(modifier = Modifier.fillMaxWidth()) {
                MonthStatCol(modifier = Modifier.weight(1f), label = "수입",  value = "+%,d원".format(income),    color = 거지방Colors.Income)
                VerticalDivider(modifier = Modifier.height(40.dp), color = 거지방Colors.Gray100)
                MonthStatCol(modifier = Modifier.weight(1f), label = "지출",  value = "-%,d원".format(expense),   color = 거지방Colors.Expense)
                VerticalDivider(modifier = Modifier.height(40.dp), color = 거지방Colors.Gray100)
                MonthStatCol(modifier = Modifier.weight(1f), label = "잔여",  value = "%,d원".format(remaining),  color = 거지방Colors.Gray800)
            }
        }
    }
}

@Composable
private fun MonthStatCol(modifier: Modifier = Modifier, label: String, value: String, color: Color) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = 거지방Colors.Gray500)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = color)
    }
}

// ─── 친구들 지출 가로 스크롤 ──────────────────────────────
@Composable
private fun FriendsSpendingRow(members: List<RankedMember>, myId: String) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        items(members) { member ->
            FriendCard(member = member, isMe = member.profile.id == myId)
        }
    }
}

@Composable
private fun FriendCard(member: RankedMember, isMe: Boolean) {
    Box {
        Surface(
            shape  = Shape14,
            color  = if (isMe) 거지방Colors.Mint50 else 거지방Colors.Gray50,
            border = BorderStroke(1.dp, if (isMe) 거지방Colors.Mint300 else 거지방Colors.Gray200),
            modifier = Modifier.width(86.dp),
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                MiniAvatar(
                    size  = 52.dp,
                    color = member.profile.avatarColor,
                    hat   = member.profile.currentHat,
                )
                Text(
                    if (isMe) "나" else member.profile.displayName,
                    style      = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = 거지방Colors.Gray800,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                )
                Text(
                    "%,d원".format(member.spentAmount),
                    style = MaterialTheme.typography.bodySmall,
                    color = 거지방Colors.Gray500,
                )
            }
        }
        // 1등 뱃지
        if (member.isWinner && !member.isNotEntered) {
            Surface(
                shape  = ShapePill,
                color  = Color(0xFFFFF8E7),
                border = BorderStroke(1.dp, 거지방Colors.Coin),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp),
            ) {
                Text(
                    "1등",
                    style      = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color      = 거지방Colors.CoinDark,
                    modifier   = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                )
            }
        }
    }
}

// ─── 옷장 BottomSheet ─────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WardrobeBottomSheet(
    profile: com.mintly.app.data.model.Profile?,
    inventory: List<UserCostume>,
    onEquipHat: (String?) -> Unit,
    onEquipOutfit: (String?) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(20.dp).navigationBarsPadding()) {
            Text("악세서리 옷장 🎀", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            val items = inventory.filter { it.costume?.kind == "hat" }
            if (items.isEmpty()) {
                Text(
                    "보유한 악세서리가 없습니다\n샵에서 구매해보세요!",
                    color     = 거지방Colors.Gray400,
                    modifier  = Modifier.padding(vertical = 24.dp).fillMaxWidth(),
                    style     = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        CostumeItem(icon = "❌", name = "없음", selected = profile?.currentHat == null, onClick = { onEquipHat(null) })
                    }
                    items(items) { uc ->
                        val costume = uc.costume ?: return@items
                        CostumeItem(
                            icon     = costume.icon,
                            name     = costume.name,
                            selected = profile?.currentHat == costume.id,
                            onClick  = { onEquipHat(costume.id) },
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CostumeItem(icon: String, name: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(Shape14)
            .background(if (selected) 거지방Colors.Mint100 else 거지방Colors.Gray50)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) 거지방Colors.Mint400 else 거지방Colors.Gray200,
                shape = Shape14,
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
    ) {
        Text(icon, fontSize = 28.sp)
        Text(name, style = MaterialTheme.typography.bodySmall, color = 거지방Colors.Gray700)
    }
}

// ─── 아바타 편집 BottomSheet ──────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AvatarEditorBottomSheet(
    currentColor: String,
    currentFace: AvatarFace,
    onSave: (String, AvatarFace) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var color by remember { mutableStateOf(currentColor) }

    val colorOpts = listOf(
        "mint"   to "민트",
        "peach"  to "복숭아",
        "blue"   to "하늘",
        "purple" to "보라",
        "yellow" to "노랑",
        "pink"   to "핑크",
        "green"  to "연두",
    )
    val colorValues = mapOf(
        "mint"   to Color(0xFF74BBAE),
        "peach"  to Color(0xFFE5896B),
        "blue"   to Color(0xFF6BA3D6),
        "purple" to Color(0xFF9C7DD9),
        "yellow" to Color(0xFFE8B547),
        "pink"   to Color(0xFFEF8FAB),
        "green"  to Color(0xFF5DBB77),
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("색상 꾸미기 🎨", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss) { Text("취소", color = 거지방Colors.Gray500) }
                    Button(
                        onClick = { onSave(color, currentFace); onDismiss() },
                        shape   = ShapePill,
                        colors  = ButtonDefaults.buttonColors(containerColor = 거지방Colors.Mint400),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    ) { Text("저장", fontWeight = FontWeight.Bold) }
                }
            }
            HorizontalDivider(color = 거지방Colors.Gray100)

            // 미리보기
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(거지방Colors.Mint50),
                contentAlignment = Alignment.Center,
            ) {
                거지방Avatar(size = 140.dp, color = color, face = currentFace)
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "색상 선택",
                style      = MaterialTheme.typography.bodyMedium,
                color      = 거지방Colors.Gray500,
                fontWeight = FontWeight.SemiBold,
                modifier   = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                colorOpts.forEach { (id, name) ->
                    val selected = color == id
                    val c = colorValues[id] ?: 거지방Colors.Mint400
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.clickable { color = id },
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(c)
                                .then(if (selected) Modifier.border(3.dp, Color.White, CircleShape) else Modifier),
                        ) {
                            if (selected) Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Text(
                            name,
                            style      = MaterialTheme.typography.bodySmall,
                            color      = if (selected) 거지방Colors.Mint700 else 거지방Colors.Gray400,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }
        }
    }
}

// ─── 확장 함수 ────────────────────────────────────────────
fun Map<String, String>.toAvatarFace(): AvatarFace {
    val def = AvatarFace()
    return AvatarFace(
        frontHair = this["frontHair"] ?: def.frontHair,
        backHair  = this["backHair"]  ?: def.backHair,
        eye       = this["eye"]       ?: def.eye,
        eyebrow   = this["eyebrow"]   ?: def.eyebrow,
        nose      = this["nose"]      ?: def.nose,
        mouth     = this["mouth"]     ?: def.mouth,
        glasses   = this["glasses"],
    )
}

fun AvatarFace.toMap(): Map<String, String> = buildMap {
    put("frontHair", frontHair)
    put("backHair",  backHair)
    put("eye",       eye)
    put("eyebrow",   eyebrow)
    put("nose",      nose)
    put("mouth",     mouth)
    glasses?.let { put("glasses", it) }
}
