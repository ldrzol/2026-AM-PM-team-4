package com.mintly.app.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mintly.app.data.model.Profile
import com.mintly.app.data.model.UserCostume
import com.mintly.app.ui.components.AvatarFace
import com.mintly.app.ui.components.MiniAvatar
import com.mintly.app.ui.components.거지방Avatar
import com.mintly.app.ui.theme.Shape14
import com.mintly.app.ui.theme.Shape20
import com.mintly.app.ui.theme.ShapePill
import com.mintly.app.ui.theme.거지방Colors
import java.time.LocalDate
import java.time.ZoneId

private val KOREA_ZONE: ZoneId = ZoneId.of("Asia/Seoul")

@Composable
fun HomeScreen(
    vm: HomeViewModel = hiltViewModel(),
    onNavigateToBudget: () -> Unit = {},
    onNavigateToRanking: () -> Unit = {},
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    var showCustomizeSheet by remember { mutableStateOf(false) }
    var today by remember { mutableStateOf(LocalDate.now(KOREA_ZONE)) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) vm.load()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        vm.load()
        while (true) {
            kotlinx.coroutines.delay(60_000)
            val current = LocalDate.now(KOREA_ZONE)
            if (current != today) {
                today = current
                vm.load()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState()),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5FBF8))
                .padding(top = 40.dp, bottom = 32.dp),
        ) {
            Header(
                profile = state.profile,
                modifier = Modifier.padding(horizontal = 32.dp),
            )

            Spacer(Modifier.height(86.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(270.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0x3374BBAE), Color.Transparent),
                            ),
                        ),
                )
                거지방Avatar(
                    size = 210.dp,
                    color = state.profile?.avatarColor ?: "mint",
                    face = state.profile?.avatarFace?.toAvatarFace() ?: AvatarFace(),
                    hat = state.profile?.let { if (it.hasCrownUntil != null) "crown" else it.currentHat },
                    outfit = state.profile?.let { it.forcedOutfit ?: it.currentOutfit },
                    forced = state.profile?.forcedOutfit != null,
                    modifier = Modifier.clickable { showCustomizeSheet = true },
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 34.dp, bottom = 12.dp)
                        .clickable { showCustomizeSheet = true },
                    shape = ShapePill,
                    color = Color.White,
                    border = BorderStroke(1.dp, 거지방Colors.Mint200),
                    shadowElevation = 2.dp,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(Icons.Rounded.Edit, null, tint = 거지방Colors.Mint600, modifier = Modifier.size(16.dp))
                        Text("꾸미기", color = 거지방Colors.Mint700, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        CheckInCard(
            today = today,
            weekDays = state.checkinWeekDays,
            streak = state.profile?.checkStreak ?: 0,
            hasCheckedToday = state.hasCheckedToday,
            checkInMessage = state.checkInMessage ?: state.checkInError,
            onCheckIn = vm::checkIn,
            modifier = Modifier
                .offset(y = (-10).dp)
                .padding(horizontal = 32.dp),
        )

        TodaySpendingCard(
            date = today,
            income = state.todayIncome,
            expense = state.todayExpense,
            onClick = onNavigateToBudget,
            modifier = Modifier.padding(horizontal = 32.dp),
        )

        Spacer(Modifier.height(28.dp))

        Text(
            "이번 달",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = 거지방Colors.Gray900,
            modifier = Modifier.padding(horizontal = 40.dp),
        )

        Spacer(Modifier.height(18.dp))

        MonthlyCard(
            income = state.monthlyIncome,
            expense = state.monthlyExpense,
            modifier = Modifier.padding(horizontal = 32.dp),
        )

        Spacer(Modifier.height(28.dp))
    }

    if (showCustomizeSheet) {
        CustomizeBottomSheet(
            profile = state.profile,
            inventory = state.inventory,
            currentColor = state.profile?.avatarColor ?: "mint",
            currentFace = state.profile?.avatarFace?.toAvatarFace() ?: AvatarFace(),
            onSaveColor = { color, face -> vm.updateAvatar(color, face.toMap()) },
            onEquipHat = vm::equipHat,
            onEquipOutfit = vm::equipOutfit,
            onDismiss = { showCustomizeSheet = false },
        )
    }
}

@Composable
private fun Header(profile: Profile?, modifier: Modifier = Modifier) {
    val nickname = profile?.displayName?.takeIf { it.isNotBlank() }
        ?: profile?.username?.takeIf { it.isNotBlank() }
        ?: "사용자"
    Box(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.align(Alignment.CenterStart).padding(end = 118.dp)) {
            Text(
                "안녕, ${nickname}님",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF65727C),
            )
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "오늘의 선택이 내 통장을 지켜줘요",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = Color.Black,
                )
                Spacer(Modifier.width(10.dp))
                Text("🌱", fontSize = 34.sp)
            }
        }
        HomeCoinChip(
            amount = profile?.coins ?: 0,
            modifier = Modifier.align(Alignment.TopEnd),
        )
    }
}

@Composable
private fun HomeCoinChip(amount: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = ShapePill,
        color = Color(0xFFFFF8E7),
        border = BorderStroke(1.dp, Color(0xFFEFD088)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(33.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8B547)),
                contentAlignment = Alignment.Center,
            ) {
                Text("₩", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
            }
            Text(
                "%,d".format(amount),
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFB8862A),
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

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
    val todayIndex = today.dayOfWeek.value - 1
    val dayNames = listOf("월", "화", "수", "목", "금", "토", "일")

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = Shape20,
        color = Color(0xFFFFFCF3),
        border = BorderStroke(1.5.dp, Color(0xFFEFD088)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 26.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("출석체크", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                Surface(shape = ShapePill, color = Color(0xFFE8B547)) {
                    Text(
                        "+50 코인",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF7A5A00),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }

            Text(
                if (streak > 0) "${streak}일 연속 출석 중 🔥" else "매일 출석하면 코인을 받아요",
                style = MaterialTheme.typography.bodyMedium,
                color = 거지방Colors.Gray500,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                dayNames.forEachIndexed { index, name ->
                    val isChecked = weekDays.getOrElse(index) { false }
                    val isToday = index == todayIndex
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(name, color = 거지방Colors.Gray500, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        CheckInDay(
                            checked = isChecked,
                            today = isToday,
                            day = today.dayOfMonth,
                        )
                    }
                }
            }

            Button(
                onClick = onCheckIn,
                enabled = !hasCheckedToday,
                shape = ShapePill,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF74BBAE),
                    disabledContainerColor = 거지방Colors.Gray200,
                ),
            ) {
                Text(
                    if (hasCheckedToday) "오늘 도장 완료" else "오늘 도장 찍기 🎉",
                    fontWeight = FontWeight.Black,
                    color = if (hasCheckedToday) 거지방Colors.Gray500 else Color.White,
                    fontSize = 18.sp,
                )
            }

            if (checkInMessage != null) {
                Text(
                    checkInMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = 거지방Colors.Mint600,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun CheckInDay(checked: Boolean, today: Boolean, day: Int) {
    when {
        checked -> Box(
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(Color(0xFF74BBAE)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(30.dp))
        }

        today -> {
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(9f, 7f), 0f)
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .drawBehind {
                        drawCircle(
                            color = Color(0xFF74BBAE),
                            radius = size.minDimension / 2f - 2.5f,
                            style = Stroke(width = 3f, pathEffect = dashEffect),
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text("$day", color = 거지방Colors.Mint600, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        }

        else -> Box(modifier = Modifier.size(58.dp))
    }
}

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
        shape = Shape20,
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE3E7EA)),
    ) {
        Column {
            Row(
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "오늘 ${date.monthValue}월 ${date.dayOfMonth}일",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.Black,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "탭해서 지출/수입 추가",
                        style = MaterialTheme.typography.bodyMedium,
                        color = 거지방Colors.Mint600,
                    )
                }
                Icon(Icons.Rounded.KeyboardArrowRight, null, tint = 거지방Colors.Gray500, modifier = Modifier.size(34.dp))
            }

            HorizontalDivider(color = Color(0xFFE3E7EA))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 26.dp),
            ) {
                TodayAmount(label = "지출", value = "%,d원".format(expense), color = Color(0xFFE5896B), modifier = Modifier.weight(1f))
                VerticalDivider(modifier = Modifier.height(64.dp), color = Color(0xFFE3E7EA))
                TodayAmount(label = "수입", value = "%,d원".format(income), color = 거지방Colors.Mint500, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TodayAmount(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = 거지방Colors.Gray600)
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = color)
    }
}

@Composable
private fun MonthlyCard(income: Int, expense: Int, modifier: Modifier = Modifier) {
    val budget = 500_000
    val remaining = income - expense
    val progress = (expense.toFloat() / budget).coerceIn(0f, 1f)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = Shape20,
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE3E7EA)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 26.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("이번 달 지출", style = MaterialTheme.typography.bodyLarge, color = 거지방Colors.Gray600)
                Text(
                    "%,d / %,d원".format(expense, budget),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = 거지방Colors.Gray700,
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(ShapePill)
                    .background(거지방Colors.Gray100),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(ShapePill)
                        .background(Color(0xFFE5896B)),
                )
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                MonthStatCol(Modifier.weight(1f), "수입", "+%,d원".format(income), 거지방Colors.Mint600)
                VerticalDivider(modifier = Modifier.height(48.dp), color = Color(0xFFE3E7EA))
                MonthStatCol(Modifier.weight(1f), "지출", "-%,d원".format(expense), Color(0xFFE5896B))
                VerticalDivider(modifier = Modifier.height(48.dp), color = Color(0xFFE3E7EA))
                MonthStatCol(Modifier.weight(1f), "잔여", "%,d원".format(remaining), Color.Black)
            }
        }
    }
}

@Composable
private fun MonthStatCol(modifier: Modifier, label: String, value: String, color: Color) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = 거지방Colors.Gray600)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = color)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomizeBottomSheet(
    profile: Profile?,
    inventory: List<UserCostume>,
    currentColor: String,
    currentFace: AvatarFace,
    onSaveColor: (String, AvatarFace) -> Unit,
    onEquipHat: (String?) -> Unit,
    onEquipOutfit: (String?) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var color by remember { mutableStateOf(currentColor) }
    var eye   by remember { mutableStateOf(currentFace.eye) }
    var mouth by remember { mutableStateOf(currentFace.mouth) }

    val colorOptions = listOf(
        "mint" to "민트",
        "peach" to "복숭아",
        "blue" to "하늘",
        "purple" to "보라",
        "yellow" to "노랑",
        "pink" to "핑크",
        "green" to "연두",
    )
    val colorValues = mapOf(
        "mint" to Color(0xFF74BBAE),
        "peach" to Color(0xFFE5896B),
        "blue" to Color(0xFF6BA3D6),
        "purple" to Color(0xFF9C7DD9),
        "yellow" to Color(0xFFE8B547),
        "pink" to Color(0xFFEF8FAB),
        "green" to Color(0xFF5DBB77),
    )
    val eyeOptions   = listOf("arc" to "^_^", "dot" to "··", "wink" to ";)", "star" to "✕✕", "heart" to "♥♥")
    val mouthOptions = listOf("smile" to "웃음", "open" to "와하", "flat" to "무표정", "sad" to "슬픔")

    val hats    = inventory.filter { it.costume?.kind == "hat" }
    val outfits = inventory.filter { it.costume?.kind == "outfit" }

    val previewFace = remember(eye, mouth, currentFace) {
        currentFace.copy(eye = eye, mouth = mouth)
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = Color.White) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("캐릭터 꾸미기", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                Button(
                    onClick = {
                        onSaveColor(color, previewFace)
                        onDismiss()
                    },
                    shape = ShapePill,
                    colors = ButtonDefaults.buttonColors(containerColor = 거지방Colors.Mint400),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                ) {
                    Text("저장", fontWeight = FontWeight.Bold)
                }
            }
            HorizontalDivider(color = 거지방Colors.Gray100)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .background(거지방Colors.Mint50),
                contentAlignment = Alignment.Center,
            ) {
                거지방Avatar(
                    size = 132.dp,
                    color = color,
                    face = previewFace,
                    hat = profile?.let { if (it.hasCrownUntil != null) "crown" else it.currentHat },
                    outfit = profile?.let { it.forcedOutfit ?: it.currentOutfit },
                    forced = profile?.forcedOutfit != null,
                )
            }

            SectionTitle("색상")
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                colorOptions.forEach { (id, name) ->
                    val selected = color == id
                    val swatch = colorValues[id] ?: 거지방Colors.Mint400
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
                                .background(swatch)
                                .border(if (selected) 3.dp else 0.dp, Color.White, CircleShape),
                        ) {
                            if (selected) Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Text(name, style = MaterialTheme.typography.labelSmall, color = if (selected) 거지방Colors.Mint700 else 거지방Colors.Gray500)
                    }
                }
            }

            SectionTitle("눈 모양")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 22.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(eyeOptions) { (id, label) ->
                    FaceChip(label = label, selected = eye == id, onClick = { eye = id })
                }
            }

            SectionTitle("입 모양")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 22.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(mouthOptions) { (id, label) ->
                    FaceChip(label = label, selected = mouth == id, onClick = { mouth = id })
                }
            }

            SectionTitle("액세서리")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 22.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    CostumeItem(icon = "×", name = "없음", selected = profile?.currentHat == null) { onEquipHat(null) }
                }
                items(hats) { userCostume ->
                    val costume = userCostume.costume ?: return@items
                    CostumeItem(
                        icon = costume.icon,
                        name = costume.name,
                        selected = profile?.currentHat == costume.id,
                        onClick = { onEquipHat(costume.id) },
                    )
                }
            }

            if (outfits.isNotEmpty()) {
                SectionTitle("의상")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 22.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item {
                        CostumeItem(icon = "×", name = "없음", selected = profile?.currentOutfit == null) { onEquipOutfit(null) }
                    }
                    items(outfits) { userCostume ->
                        val costume = userCostume.costume ?: return@items
                        CostumeItem(
                            icon = costume.icon,
                            name = costume.name,
                            selected = profile?.currentOutfit == costume.id,
                            onClick = { onEquipOutfit(costume.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FaceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = ShapePill,
        color = if (selected) 거지방Colors.Mint100 else 거지방Colors.Gray50,
        border = BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) 거지방Colors.Mint400 else 거지방Colors.Gray200,
        ),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) 거지방Colors.Mint700 else 거지방Colors.Gray600,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = 거지방Colors.Gray600,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 22.dp).padding(top = 20.dp, bottom = 10.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WardrobeBottomSheet(
    profile: Profile?,
    inventory: List<UserCostume>,
    onEquipHat: (String?) -> Unit,
    onEquipOutfit: (String?) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = Color.White) {
        Column(modifier = Modifier.padding(20.dp).navigationBarsPadding()) {
            Text("액세서리 옷장", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            val hats = inventory.filter { it.costume?.kind == "hat" }
            if (hats.isEmpty()) {
                Text(
                    "보유한 액세서리가 없습니다.\n상점에서 구매해보세요!",
                    color = 거지방Colors.Gray400,
                    modifier = Modifier.padding(vertical = 24.dp).fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        CostumeItem(icon = "×", name = "없음", selected = profile?.currentHat == null) { onEquipHat(null) }
                    }
                    items(hats) { userCostume ->
                        val costume = userCostume.costume ?: return@items
                        CostumeItem(
                            icon = costume.icon,
                            name = costume.name,
                            selected = profile?.currentHat == costume.id,
                            onClick = { onEquipHat(costume.id) },
                        )
                    }
                }
            }

            val outfits = inventory.filter { it.costume?.kind == "outfit" }
            if (outfits.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                Text("의상", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        CostumeItem(icon = "×", name = "없음", selected = profile?.currentOutfit == null) { onEquipOutfit(null) }
                    }
                    items(outfits) { userCostume ->
                        val costume = userCostume.costume ?: return@items
                        CostumeItem(
                            icon = costume.icon,
                            name = costume.name,
                            selected = profile?.currentOutfit == costume.id,
                            onClick = { onEquipOutfit(costume.id) },
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
        verticalArrangement = Arrangement.spacedBy(6.dp),
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

    val colorOptions = listOf(
        "mint" to "민트",
        "peach" to "복숭아",
        "blue" to "하늘",
        "purple" to "보라",
        "yellow" to "노랑",
        "pink" to "핑크",
        "green" to "연두",
    )
    val colorValues = mapOf(
        "mint" to Color(0xFF74BBAE),
        "peach" to Color(0xFFE5896B),
        "blue" to Color(0xFF6BA3D6),
        "purple" to Color(0xFF9C7DD9),
        "yellow" to Color(0xFFE8B547),
        "pink" to Color(0xFFEF8FAB),
        "green" to Color(0xFF5DBB77),
    )

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = Color.White) {
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
                Text("아바타 색상", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss) { Text("취소", color = 거지방Colors.Gray500) }
                    Button(
                        onClick = { onSave(color, currentFace); onDismiss() },
                        shape = ShapePill,
                        colors = ButtonDefaults.buttonColors(containerColor = 거지방Colors.Mint400),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    ) {
                        Text("저장", fontWeight = FontWeight.Bold)
                    }
                }
            }
            HorizontalDivider(color = 거지방Colors.Gray100)

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
                style = MaterialTheme.typography.bodyMedium,
                color = 거지방Colors.Gray500,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                colorOptions.forEach { (id, name) ->
                    val selected = color == id
                    val swatch = colorValues[id] ?: 거지방Colors.Mint400
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
                                .background(swatch)
                                .then(if (selected) Modifier.border(3.dp, Color.White, CircleShape) else Modifier),
                        ) {
                            if (selected) Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Text(
                            name,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (selected) 거지방Colors.Mint700 else 거지방Colors.Gray400,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }
        }
    }
}

fun Map<String, String>?.toAvatarFace(): AvatarFace {
    val m = this ?: emptyMap()
    val default = AvatarFace()
    return AvatarFace(
        frontHair = m["frontHair"] ?: default.frontHair,
        backHair  = m["backHair"]  ?: default.backHair,
        eye       = m["eye"]       ?: default.eye,
        eyebrow   = m["eyebrow"]   ?: default.eyebrow,
        nose      = m["nose"]      ?: default.nose,
        mouth     = m["mouth"]     ?: default.mouth,
        glasses   = m["glasses"],
    )
}

fun AvatarFace.toMap(): Map<String, String> = buildMap {
    put("frontHair", frontHair)
    put("backHair", backHair)
    put("eye", eye)
    put("eyebrow", eyebrow)
    put("nose", nose)
    put("mouth", mouth)
    glasses?.let { put("glasses", it) }
}
