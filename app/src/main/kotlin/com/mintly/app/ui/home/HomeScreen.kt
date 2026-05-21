package com.mintly.app.ui.home

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mintly.app.data.model.Category
import com.mintly.app.data.model.DEFAULT_COSTUMES
import com.mintly.app.data.model.UserCostume
import com.mintly.app.ui.components.*
import com.mintly.app.ui.theme.거지방Colors
import com.mintly.app.ui.theme.Shape14
import com.mintly.app.ui.theme.Shape20
import com.mintly.app.ui.theme.ShapePill

@Composable
fun HomeScreen(vm: HomeViewModel = hiltViewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    var showWardrobe     by remember { mutableStateOf(false) }
    var showAvatarEditor by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.load() }

    val profile = state.profile

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(거지방Colors.Gray50)
            .verticalScroll(rememberScrollState()),
    ) {
        // ─── 그라디언트 헤더 ──────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(거지방Colors.Mint400, 거지방Colors.Mint300)
                    )
                )
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp, bottom = 28.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "안녕하세요 👋",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                    Text(
                        profile?.displayName ?: "...",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = ShapePill,
                        color = Color.White.copy(alpha = 0.2f),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text("🪙", fontSize = 14.sp)
                            Text(
                                "%,d".format(profile?.coins ?: 0),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                        }
                    }
                    if (state.inventory.isNotEmpty()) {
                        Surface(shape = ShapePill, color = Color.White.copy(alpha = 0.2f)) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text("🎟", fontSize = 14.sp)
                                Text(
                                    "${state.inventory.size}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                            }
                        }
                    }
                }
            }
        }

        // ─── 아바타 카드 ──────────────────────────────────────
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = (-16).dp),
            shape = Shape20,
            color = Color.White,
            shadowElevation = 4.dp,
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // 아바타 + 왕관 뱃지
                Box(contentAlignment = Alignment.TopEnd) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(Shape20)
                            .background(거지방Colors.Mint50),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        거지방Avatar(
                            size = 80.dp,
                            color = profile?.avatarColor ?: "mint",
                            face = profile?.avatarFace?.toAvatarFace() ?: AvatarFace(),
                            hat = profile?.let { if (it.hasCrownUntil != null) "crown" else it.currentHat },
                            outfit = profile?.let { it.forcedOutfit ?: it.currentOutfit },
                            forced = profile?.forcedOutfit != null,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    if (profile?.hasCrownUntil != null) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFF8E7),
                            modifier = Modifier.offset(x = 4.dp, y = (-4).dp),
                        ) {
                            Text("👑", fontSize = 14.sp, modifier = Modifier.padding(4.dp))
                        }
                    }
                    if (profile?.forcedOutfit != null) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFF3E0),
                            modifier = Modifier.align(Alignment.BottomEnd).offset(x = 4.dp, y = 4.dp),
                        ) {
                            Text("🧺", fontSize = 12.sp, modifier = Modifier.padding(4.dp))
                        }
                    }
                }

                // 이름 + 버튼
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        profile?.displayName ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = 거지방Colors.Gray900,
                    )
                    Text(
                        "@${profile?.username ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = 거지방Colors.Gray400,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { showAvatarEditor = true },
                            shape = ShapePill,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = 거지방Colors.Mint400),
                        ) {
                            Icon(Icons.Rounded.Edit, null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("꾸미기", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { showWardrobe = true },
                            shape = ShapePill,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            border = BorderStroke(1.dp, 거지방Colors.Mint300),
                        ) {
                            Icon(Icons.Rounded.Checkroom, null, modifier = Modifier.size(14.dp), tint = 거지방Colors.Mint500)
                            Spacer(Modifier.width(4.dp))
                            Text("옷장", style = MaterialTheme.typography.labelSmall, color = 거지방Colors.Mint600, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height((-8).dp))

        // ─── 오늘의 소비 ─────────────────────────────────────
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = Shape20,
            color = Color.White,
            shadowElevation = 1.dp,
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("오늘의 소비", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = 거지방Colors.Gray900)
                    val net = state.todayIncome - state.todayExpense
                    Text(
                        if (net >= 0) "+%,d원".format(net) else "%,d원".format(net),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (net >= 0) 거지방Colors.Income else 거지방Colors.Expense,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    SummaryCard(modifier = Modifier.weight(1f), label = "수입", amount = state.todayIncome, kind = "income")
                    SummaryCard(modifier = Modifier.weight(1f), label = "지출", amount = state.todayExpense, kind = "expense")
                }
            }
        }

        // ─── 이번 달 지출 분석 ────────────────────────────────
        if (state.monthlyStats.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = Shape20,
                color = Color.White,
                shadowElevation = 1.dp,
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("이번 달 지출 분석", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = 거지방Colors.Gray900)
                        Text(
                            "TOP ${minOf(state.monthlyStats.size, 5)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = 거지방Colors.Gray400,
                        )
                    }
                    CategoryBarChart(stats = state.monthlyStats)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }

    // ─── 옷장 BottomSheet ─────────────────────────────────────
    if (showWardrobe) {
        WardrobeBottomSheet(
            profile = state.profile,
            inventory = state.inventory,
            onEquipHat    = { vm.equipHat(it) },
            onEquipOutfit = { vm.equipOutfit(it) },
            onDismiss = { showWardrobe = false },
        )
    }

    // ─── 아바타 편집 BottomSheet ──────────────────────────────
    if (showAvatarEditor) {
        AvatarEditorBottomSheet(
            currentColor = state.profile?.avatarColor ?: "mint",
            currentFace  = state.profile?.avatarFace?.toAvatarFace() ?: AvatarFace(),
            onSave = { color, face -> vm.updateAvatar(color, face.toMap()) },
            onDismiss = { showAvatarEditor = false },
        )
    }
}

// ─── 요약 카드 ────────────────────────────────────────────
@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    label: String,
    amount: Int,
    kind: String,
) {
    val bg = if (kind == "income") 거지방Colors.IncomeBg else 거지방Colors.ExpenseBg
    val color = if (kind == "income") 거지방Colors.Income else 거지방Colors.Expense
    Surface(modifier = modifier, shape = Shape14, color = bg) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = color.copy(alpha = 0.7f))
            Text(
                text = "%,d원".format(amount),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color,
            )
        }
    }
}

// ─── 카테고리 바 차트 ─────────────────────────────────────
@Composable
private fun CategoryBarChart(stats: Map<Category, Int>) {
    val sorted = stats.entries.sortedByDescending { it.value }.take(5)
    val max = sorted.firstOrNull()?.value?.toFloat() ?: 1f

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        sorted.forEach { (cat, amount) ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(cat.name, style = MaterialTheme.typography.bodySmall, color = 거지방Colors.Gray700)
                    Text(
                        "%,d원".format(amount),
                        style = MaterialTheme.typography.bodySmall,
                        color = 거지방Colors.Gray600,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(ShapePill)
                        .background(거지방Colors.Gray100),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = amount / max)
                            .fillMaxHeight()
                            .clip(ShapePill)
                            .background(cat.color.toComposeColor(거지방Colors.Mint400)),
                    )
                }
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
    var tab by remember { mutableStateOf("hat") }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(20.dp).navigationBarsPadding()) {
            Text("옷장", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            // 탭
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("hat" to "모자", "outfit" to "옷").forEach { (id, label) ->
                    FilterChip(
                        selected = tab == id,
                        onClick  = { tab = id },
                        label    = { Text(label) },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = 거지방Colors.Mint400,
                            selectedLabelColor     = Color.White,
                        ),
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            val items = inventory.filter { it.costume?.kind == tab }
            if (items.isEmpty()) {
                Text("보유한 아이템이 없습니다", color = 거지방Colors.Gray400,
                    modifier = Modifier.padding(vertical = 24.dp).fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium)
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // 없음 선택
                    item {
                        val selected = if (tab == "hat") profile?.currentHat == null else profile?.currentOutfit == null
                        CostumeItem(
                            icon = "❌",
                            name = "없음",
                            selected = selected,
                            onClick = { if (tab == "hat") onEquipHat(null) else onEquipOutfit(null) },
                        )
                    }
                    items(items) { uc ->
                        val costume = uc.costume ?: return@items
                        val isEquipped = if (tab == "hat") profile?.currentHat == costume.id
                        else profile?.currentOutfit == costume.id
                        CostumeItem(
                            icon = costume.icon,
                            name = costume.name,
                            selected = isEquipped,
                            onClick = {
                                if (tab == "hat") onEquipHat(costume.id)
                                else onEquipOutfit(costume.id)
                            },
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
            .clip(com.mintly.app.ui.theme.Shape14)
            .background(if (selected) 거지방Colors.Mint100 else 거지방Colors.Gray50)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) 거지방Colors.Mint400 else 거지방Colors.Gray200,
                shape = com.mintly.app.ui.theme.Shape14,
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
    ) {
        Text(icon, fontSize = 28.sp)
        Text(name, style = MaterialTheme.typography.labelSmall, color = 거지방Colors.Gray700)
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
    var face  by remember { mutableStateOf(currentFace) }
    var tab   by remember { mutableStateOf("face") } // "face" | "hair" | "color"

    // 옵션 목록
    val colorOpts     = listOf("mint" to "민트", "peach" to "복숭아", "blue" to "하늘", "purple" to "보라", "yellow" to "노랑")
    val colorValues   = mapOf("mint" to Color(0xFF74BBAE), "peach" to Color(0xFFE5896B), "blue" to Color(0xFF6BA3D6), "purple" to Color(0xFF9C7DD9), "yellow" to Color(0xFFE8B547))
    val eyeOpts       = listOf("e1" to "기본 👁", "e28" to "큰 눈 👀", "dots" to "점눈 ·", "arc" to "^_^")
    val noseOpts      = listOf("n1" to "작은 코", "n3" to "콧구멍", "n10" to "U자 코")
    val mouthOpts     = listOf("m2" to "미소 🙂", "m17" to "물결 〜", "m22" to "한쪽 ◝", "m26" to "활짝 😄", "line" to "무표정 —")
    val glassesOpts   = listOf(null to "없음 🚫", "round" to "둥근 ⭕", "square" to "각진 ◻", "half" to "하프 ⌓", "heart" to "하트 ♡", "star" to "별 ⭐")
    val frontHairOpts = listOf("none" to "없음", "fh3" to "스타일 1", "fh5" to "스타일 2", "fh9" to "스타일 3", "fh13" to "스타일 4", "fh30" to "스타일 5", "fh43" to "스타일 6")
    val backHairOpts  = listOf("none" to "없음", "bh5" to "스타일 1", "bh9" to "스타일 2", "bh31" to "스타일 3", "bh43" to "스타일 4")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
        ) {
            // ─ 헤더 ──────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("아바타 꾸미기", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss) { Text("취소", color = 거지방Colors.Gray500) }
                    Button(
                        onClick = { onSave(color, face); onDismiss() },
                        shape = ShapePill,
                        colors = ButtonDefaults.buttonColors(containerColor = 거지방Colors.Mint400),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    ) { Text("저장", fontWeight = FontWeight.Bold) }
                }
            }
            HorizontalDivider(color = 거지방Colors.Gray100)

            // ─ 아바타 미리보기 ────────────────────────────────
            Box(
                modifier = Modifier.fillMaxWidth().height(160.dp).background(거지방Colors.Mint50),
                contentAlignment = Alignment.Center,
            ) {
                거지방Avatar(size = 130.dp, color = color, face = face)
            }

            // ─ 탭 ────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .clip(ShapePill)
                    .background(거지방Colors.Gray100)
                    .padding(4.dp),
            ) {
                listOf("face" to "얼굴 😊", "hair" to "헤어 💇", "color" to "색상 🎨").forEach { (id, label) ->
                    val active = tab == id
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(ShapePill)
                            .background(if (active) Color.White else Color.Transparent)
                            .clickable { tab = id }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                            color = if (active) 거지방Colors.Mint700 else 거지방Colors.Gray500,
                        )
                    }
                }
            }

            // ─ 탭 콘텐츠 ─────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp, max = 320.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                when (tab) {
                    "face" -> {
                        AvatarSelectorRow("눈", eyeOpts.map { it.first to it.second }, face.eye) { face = face.copy(eye = it) }
                        AvatarSelectorRow("코", noseOpts.map { it.first to it.second }, face.nose) { face = face.copy(nose = it) }
                        AvatarSelectorRow("입", mouthOpts.map { it.first to it.second }, face.mouth) { face = face.copy(mouth = it) }
                        AvatarSelectorRow(
                            "안경",
                            glassesOpts.map { (k, v) -> (k ?: "none") to v },
                            face.glasses ?: "none",
                        ) { face = face.copy(glasses = if (it == "none") null else it) }
                    }
                    "hair" -> {
                        AvatarSelectorRow("앞머리", frontHairOpts.map { it.first to it.second }, face.frontHair) { face = face.copy(frontHair = it) }
                        AvatarSelectorRow("뒷머리", backHairOpts.map { it.first to it.second }, face.backHair) { face = face.copy(backHair = it) }
                    }
                    "color" -> {
                        Text("바디 색상", style = MaterialTheme.typography.labelMedium, color = 거지방Colors.Gray500, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            colorOpts.forEach { (id, name) ->
                                val selected = color == id
                                val c = colorValues[id] ?: 거지방Colors.Mint400
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.clickable { color = id },
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(c)
                                            .border(if (selected) 3.dp else 0.dp, Color.White, CircleShape),
                                    ) {
                                        if (selected) {
                                            Icon(Icons.Rounded.Check, null, tint = Color.White,
                                                modifier = Modifier.align(Alignment.Center).size(20.dp))
                                        }
                                    }
                                    Text(name, style = MaterialTheme.typography.labelSmall, color = if (selected) 거지방Colors.Mint700 else 거지방Colors.Gray500, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AvatarSelectorRow(
    label: String,
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = 거지방Colors.Gray500, fontWeight = FontWeight.SemiBold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(options) { (id, name) ->
                val isSelected = selected == id
                Surface(
                    shape = ShapePill,
                    color = if (isSelected) 거지방Colors.Mint100 else 거지방Colors.Gray50,
                    border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) 거지방Colors.Mint400 else 거지방Colors.Gray200),
                    modifier = Modifier.clickable { onSelect(id) },
                ) {
                    Text(
                        name,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) 거지방Colors.Mint700 else 거지방Colors.Gray600,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}


// ─── 확장 함수 ────────────────────────────────────────────
fun Map<String, String>.toAvatarFace(): AvatarFace {
    val def = AvatarFace()  // 기본값은 AvatarFace 정의에서 가져옴
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
