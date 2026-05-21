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
    var showWardrobe by remember { mutableStateOf(false) }
    var showAvatarEditor by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.load() }

    val profile = state.profile

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        // ─── 헤더 ─────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("안녕하세요 👋", style = MaterialTheme.typography.bodyMedium, color = 거지방Colors.Gray500)
                Text(
                    profile?.displayName ?: "...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                CoinChip(amount = profile?.coins ?: 0)
                TicketChip(count = state.inventory.size) // 간략 표시
            }
        }

        // ─── 아바타 + 옷장 ────────────────────────────────────
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            shape = Shape20,
            color = 거지방Colors.Mint50,
            border = BorderStroke(1.dp, 거지방Colors.Mint200),
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // 아바타
                Box(contentAlignment = Alignment.TopEnd) {
                    거지방Avatar(
                        size = 100.dp,
                        color = profile?.avatarColor ?: "mint",
                        face = profile?.avatarFace?.toAvatarFace() ?: AvatarFace(),
                        hat = profile?.let {
                            if (it.hasCrownUntil != null) "crown" else it.currentHat
                        },
                        outfit = profile?.let {
                            if (it.forcedOutfit != null) it.forcedOutfit else it.currentOutfit
                        },
                        forced = profile?.forcedOutfit != null,
                    )
                    // 왕관 뱃지
                    if (profile?.hasCrownUntil != null) {
                        Text("👑", fontSize = 18.sp, modifier = Modifier.offset(x = 4.dp, y = (-4).dp))
                    }
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = profile?.displayName ?: "",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    // 옷장 버튼
                    Button(
                        onClick = { showWardrobe = true },
                        shape = ShapePill,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = 거지방Colors.Mint400),
                    ) {
                        Icon(Icons.Rounded.Checkroom, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("옷장", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                    // 아바타 편집 버튼
                    OutlinedButton(
                        onClick = { showAvatarEditor = true },
                        shape = ShapePill,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        border = BorderStroke(1.dp, 거지방Colors.Mint300),
                    ) {
                        Icon(Icons.Rounded.Edit, null, modifier = Modifier.size(16.dp), tint = 거지방Colors.Mint500)
                        Spacer(Modifier.width(4.dp))
                        Text("편집", style = MaterialTheme.typography.labelMedium, color = 거지방Colors.Mint500,
                            fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // ─── 오늘의 소비 요약 ─────────────────────────────────
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            shape = Shape20,
            color = Color.White,
            shadowElevation = 1.dp,
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "오늘의 소비",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        label = "수입",
                        amount = state.todayIncome,
                        kind = "income",
                    )
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        label = "지출",
                        amount = state.todayExpense,
                        kind = "expense",
                    )
                }
            }
        }

        // ─── 이번 달 카테고리 분석 ─────────────────────────────
        if (state.monthlyStats.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                shape = Shape20,
                color = Color.White,
                shadowElevation = 1.dp,
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "이번 달 지출 분석",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
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
    var color  by remember { mutableStateOf(currentColor) }
    var face   by remember { mutableStateOf(currentFace) }

    val colors = listOf("mint" to "🟢", "peach" to "🟠", "blue" to "🔵", "purple" to "🟣", "yellow" to "🟡")
    val hairs  = listOf("없음" to "none", "스타일1" to "fh3", "스타일2" to "fh5", "스타일3" to "fh9", "스타일4" to "fh13")
    val eyes   = listOf("기본" to "e1", "활짝" to "e28", "점눈" to "dots", "^_^" to "arc")
    val mouths = listOf("미소" to "m2", "물결" to "m17", "한쪽" to "m22", "활짝" to "m26", "무표정" to "line")

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("아바타 편집", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            // 미리보기
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                거지방Avatar(size = 120.dp, color = color, face = face)
            }

            // 색상
            SelectorRow("바디 색상", colors.map { it.second to it.first }, color) { color = it }

            // 앞머리
            SelectorRow("앞머리", hairs.map { it.second to it.first }, face.frontHair) {
                face = face.copy(frontHair = it)
            }

            // 눈
            SelectorRow("눈", eyes.map { it.second to it.first }, face.eye) {
                face = face.copy(eye = it)
            }

            // 입
            SelectorRow("입", mouths.map { it.second to it.first }, face.mouth) {
                face = face.copy(mouth = it)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = ShapePill,
                    border = BorderStroke(1.dp, 거지방Colors.Gray300)) {
                    Text("취소")
                }
                Button(
                    onClick = { onSave(color, face); onDismiss() },
                    modifier = Modifier.weight(1f),
                    shape = ShapePill,
                    colors = ButtonDefaults.buttonColors(containerColor = 거지방Colors.Mint400),
                ) {
                    Text("저장")
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SelectorRow(
    label: String,
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = 거지방Colors.Gray500)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(options) { (id, name) ->
                FilterChip(
                    selected = selected == id,
                    onClick  = { onSelect(id) },
                    label    = { Text(name, style = MaterialTheme.typography.labelSmall) },
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = 거지방Colors.Mint400,
                        selectedLabelColor     = Color.White,
                    ),
                )
            }
        }
    }
}

// ─── 확장 함수 ────────────────────────────────────────────
fun Map<String, String>.toAvatarFace() = AvatarFace(
    frontHair = this["frontHair"] ?: "fh3",
    backHair  = this["backHair"]  ?: "bh5",
    eye       = this["eye"]       ?: "e1",
    eyebrow   = this["eyebrow"]   ?: "b15",
    nose      = this["nose"]      ?: "n1",
    mouth     = this["mouth"]     ?: "m2",
)

fun AvatarFace.toMap(): Map<String, String> = mapOf(
    "frontHair" to frontHair,
    "backHair"  to backHair,
    "eye"       to eye,
    "eyebrow"   to eyebrow,
    "nose"      to nose,
    "mouth"     to mouth,
)
