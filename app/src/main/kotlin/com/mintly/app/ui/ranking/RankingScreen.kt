package com.mintly.app.ui.ranking

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mintly.app.data.model.FriendGroup
import com.mintly.app.data.model.Profile
import com.mintly.app.data.model.RankedMember
import com.mintly.app.ui.chat.ChatScreen
import com.mintly.app.ui.components.*
import com.mintly.app.ui.home.toAvatarFace
import com.mintly.app.ui.theme.거지방Colors
import com.mintly.app.ui.theme.Shape10
import com.mintly.app.ui.theme.Shape14
import com.mintly.app.ui.theme.Shape20
import com.mintly.app.ui.theme.ShapePill

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreen(vm: RankingViewModel = hiltViewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    var showChat         by remember { mutableStateOf(false) }
    var showInviteGroup  by remember { mutableStateOf<FriendGroup?>(null) }

    // 탭 진입·복귀 시마다 그룹 목록 새로고침 (방 만들고 돌아왔을 때 즉시 반영)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                vm.loadGroups()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        vm.startRealtime()
    }

    val selectedGroup = state.groups.find { it.id == state.selectedGroupId }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ─── 헤더 ─────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 20.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("랭킹", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    if (selectedGroup != null) {
                        Text(
                            "${selectedGroup.emoji} ${selectedGroup.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = 거지방Colors.Gray400,
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // 초대하기 버튼 (방 선택됐을 때만)
                    if (selectedGroup != null) {
                        IconButton(onClick = { showInviteGroup = selectedGroup }) {
                            Icon(Icons.Rounded.PersonAdd, null, tint = 거지방Colors.Mint500)
                        }
                    }
                    IconButton(onClick = { vm.loadGroups() }) {
                        Icon(Icons.Rounded.Refresh, null, tint = 거지방Colors.Gray400)
                    }
                }
            }

            // ─── 방 선택 탭 ───────────────────────────────────
            if (state.groups.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(state.groups) { group ->
                        val active = group.id == state.selectedGroupId
                        FilterChip(
                            selected = active,
                            onClick  = { vm.selectGroup(group.id) },
                            label    = {
                                Text(
                                    "${group.emoji} ${group.name}",
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = 거지방Colors.Mint400,
                                selectedLabelColor     = Color.White,
                            ),
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            // ─── 선택된 방 초대코드 칩 ─────────────────────────
            if (selectedGroup != null && state.rankings.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "오늘 지출 순위  •  ${state.rankings.size}명 참여",
                        style = MaterialTheme.typography.labelSmall,
                        color = 거지방Colors.Gray400,
                    )
                    Surface(
                        shape = ShapePill,
                        color = 거지방Colors.Mint50,
                        border = BorderStroke(1.dp, 거지방Colors.Mint200),
                        modifier = Modifier.clickable { showInviteGroup = selectedGroup },
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(Icons.Rounded.PersonAdd, null, tint = 거지방Colors.Mint600, modifier = Modifier.size(12.dp))
                            Text(
                                "초대 ${selectedGroup.inviteCode}",
                                style = MaterialTheme.typography.labelSmall,
                                color = 거지방Colors.Mint700,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            // ─── 컨텐츠 ───────────────────────────────────────
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = 거지방Colors.Mint400)
                }
            } else if (state.groups.isEmpty()) {
                EmptyState(
                    emoji = "🏠",
                    message = "아직 참여한 방이 없어요\n설정에서 방을 만들거나 참여해보세요",
                )
            } else if (state.rankings.isEmpty()) {
                EmptyState(
                    emoji = "📊",
                    message = "오늘 아직 지출 기록이 없어요\n지출을 기록하면 순위가 표시됩니다",
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    itemsIndexed(state.rankings, key = { _, r -> r.profile.id }) { _, ranked ->
                        RankRow(
                            ranked  = ranked,
                            onClick = { vm.selectFriend(ranked.profile) },
                        )
                    }
                }
            }
        }

        // ─── 채팅 FAB ─────────────────────────────────────────
        FloatingActionButton(
            onClick = { showChat = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = 거지방Colors.Mint400,
            contentColor   = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
        ) {
            Icon(Icons.Rounded.Chat, contentDescription = "채팅", modifier = Modifier.size(26.dp))
        }
    }

    // ─── 친구 프로필 다이얼로그 ──────────────────────────────
    state.selectedFriend?.let { friend ->
        FriendProfileDialog(
            profile      = friend,
            rankedMember = state.rankings.find { it.profile.id == friend.id },
            onDismiss    = vm::clearSelectedFriend,
        )
    }

    // ─── 초대 시트 ────────────────────────────────────────────
    showInviteGroup?.let { group ->
        InviteSheet(
            group     = group,
            onDismiss = { showInviteGroup = null },
        )
    }

    // ─── 채팅 화면 ────────────────────────────────────────────
    if (showChat) {
        val groupId   = state.selectedGroupId
        val groupName = state.groups.find { it.id == groupId }?.name ?: "그룹 채팅"
        if (groupId != null) {
            ChatScreen(
                groupId   = groupId,
                groupName = groupName,
                onBack    = { showChat = false },
            )
        }
    }
}

// ─── 초대 시트 ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InviteSheet(group: FriendGroup, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val clipboard  = LocalClipboardManager.current
    val context    = LocalContext.current
    var justCopied by remember { mutableStateOf(false) }

    LaunchedEffect(justCopied) {
        if (justCopied) { kotlinx.coroutines.delay(1500); justCopied = false }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Text(
                "${group.emoji} ${group.name}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = 거지방Colors.Gray900,
            )
            Spacer(Modifier.height(4.dp))
            Text("친구를 초대해보세요", style = MaterialTheme.typography.bodySmall, color = 거지방Colors.Gray500)

            Spacer(Modifier.height(20.dp))

            // 코드 칸 표시
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = Shape14,
                color = 거지방Colors.Mint50,
                border = BorderStroke(1.5.dp, 거지방Colors.Mint200),
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("초대 코드", style = MaterialTheme.typography.labelSmall, color = 거지방Colors.Mint600,
                        fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        group.inviteCode.forEach { ch ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(Shape10)
                                    .background(Color.White)
                                    .border(1.dp, 거지방Colors.Mint200, Shape10),
                            ) {
                                Text(ch.toString(), style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold, color = 거지방Colors.Mint700)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = { clipboard.setText(AnnotatedString(group.inviteCode)); justCopied = true },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = ShapePill,
                    border = BorderStroke(1.5.dp, if (justCopied) 거지방Colors.Mint400 else 거지방Colors.Gray200),
                ) {
                    Icon(
                        if (justCopied) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
                        null,
                        tint = if (justCopied) 거지방Colors.Mint500 else 거지방Colors.Gray600,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(if (justCopied) "복사됨!" else "코드 복사",
                        color = if (justCopied) 거지방Colors.Mint500 else 거지방Colors.Gray700,
                        fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT,
                                "거지방 '${group.name}'에 초대합니다!\n초대 코드: ${group.inviteCode}\n앱에서 '방 참여하기'를 눌러 입력해 주세요 🏠")
                        }
                        context.startActivity(Intent.createChooser(intent, "초대 공유하기"))
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = ShapePill,
                    colors = ButtonDefaults.buttonColors(containerColor = 거지방Colors.Mint400),
                ) {
                    Icon(Icons.Rounded.Share, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("초대 공유", fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("닫기", color = 거지방Colors.Gray500)
            }
        }
    }
}

// ─── 랭킹 행 ───────────────────────────────────────────────
@Composable
private fun RankRow(ranked: RankedMember, onClick: () -> Unit) {
    val isFirst  = ranked.rank == 1
    val isLoser  = ranked.isLoser
    val bgColor  = when {
        isFirst -> 거지방Colors.RankFirstBg
        isLoser -> 거지방Colors.RankLoserBg
        else    -> Color.White
    }
    val borderColor = when {
        isFirst -> 거지방Colors.Coin
        isLoser -> 거지방Colors.RankLoserBorder
        else    -> 거지방Colors.Gray200
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = Shape14,
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 순위
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        when (ranked.rank) {
                            1    -> 거지방Colors.Coin
                            2    -> 거지방Colors.Rank2
                            3    -> 거지방Colors.Rank3
                            else -> 거지방Colors.Gray100
                        }
                    ),
            ) {
                Text(
                    when (ranked.rank) {
                        1 -> "👑"
                        else -> "${ranked.rank}"
                    },
                    fontSize = if (ranked.rank == 1) 16.sp else 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (ranked.rank <= 3) Color.White else 거지방Colors.Gray700,
                )
            }

            // 아바타
            Box {
                MiniAvatar(
                    size = 44.dp,
                    color = ranked.profile.avatarColor,
                    face = ranked.profile.avatarFace.toAvatarFace(),
                    hat = if (ranked.profile.hasCrownUntil != null) "crown" else ranked.profile.currentHat,
                    outfit = ranked.profile.forcedOutfit ?: ranked.profile.currentOutfit,
                )
                if (isLoser) {
                    Text("😤", fontSize = 14.sp, modifier = Modifier.align(Alignment.TopEnd))
                }
            }

            // 이름 + 지출
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    ranked.profile.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = ranked.shareValue,
                    style = MaterialTheme.typography.bodySmall,
                    color = 거지방Colors.Expense,
                )
            }

            // 1등/꼴등 뱃지
            if (isFirst) {
                Surface(shape = ShapePill, color = 거지방Colors.RankFirstBg, border = BorderStroke(1.dp, 거지방Colors.Coin)) {
                    Text("1등 🥇", style = MaterialTheme.typography.labelSmall, color = 거지방Colors.CoinDark,
                        fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }
            if (isLoser) {
                Surface(shape = ShapePill, color = 거지방Colors.RankLoserBg, border = BorderStroke(1.dp, 거지방Colors.RankLoserBorder)) {
                    Text("꼴등 😅", style = MaterialTheme.typography.labelSmall, color = 거지방Colors.RankLoserText,
                        fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }
        }
    }
}

// ─── 친구 프로필 다이얼로그 ──────────────────────────────
@Composable
private fun FriendProfileDialog(
    profile: Profile,
    rankedMember: RankedMember?,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = Shape20, color = Color.White) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box {
                    거지방Avatar(
                        size = 100.dp,
                        color = profile.avatarColor,
                        face = profile.avatarFace.toAvatarFace(),
                        hat = if (profile.hasCrownUntil != null) "crown" else profile.currentHat,
                        outfit = profile.forcedOutfit ?: profile.currentOutfit,
                        forced = profile.forcedOutfit != null,
                    )
                    if (profile.hasCrownUntil != null) {
                        Text("👑", fontSize = 20.sp, modifier = Modifier.align(Alignment.TopEnd))
                    }
                }

                Text(
                    profile.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "@${profile.username}",
                    style = MaterialTheme.typography.bodySmall,
                    color = 거지방Colors.Gray500,
                )

                if (rankedMember != null) {
                    Surface(
                        shape = Shape14,
                        color = 거지방Colors.Mint50,
                        border = BorderStroke(1.dp, 거지방Colors.Mint200),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            // shareMode에 따라 다른 정보 표시
                            if (profile.shareMode == "percent") {
                                val pct = if (rankedMember.incomeAmount > 0)
                                    (rankedMember.spentAmount.toDouble() / rankedMember.incomeAmount * 100).toInt()
                                else null
                                Text("수입 대비 지출", style = MaterialTheme.typography.labelMedium, color = 거지방Colors.Gray500)
                                Text(
                                    if (pct != null) "${pct}%" else "정보 없음",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = 거지방Colors.Expense,
                                )
                                if (pct != null) {
                                    Text(
                                        "수입 %,d원 → 지출 %,d원".format(rankedMember.incomeAmount, rankedMember.spentAmount),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = 거지방Colors.Gray400,
                                    )
                                }
                            } else {
                                // amount 모드
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("수입", style = MaterialTheme.typography.labelSmall, color = 거지방Colors.Gray500)
                                        Text(
                                            "+%,d원".format(rankedMember.incomeAmount),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = 거지방Colors.Income,
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("지출", style = MaterialTheme.typography.labelSmall, color = 거지방Colors.Gray500)
                                        Text(
                                            "-%,d원".format(rankedMember.spentAmount),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = 거지방Colors.Expense,
                                        )
                                    }
                                }
                            }
                            Text(
                                "현재 ${rankedMember.rank}위",
                                style = MaterialTheme.typography.labelMedium,
                                color = 거지방Colors.Mint600,
                            )
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    shape = ShapePill,
                    colors = ButtonDefaults.buttonColors(containerColor = 거지방Colors.Mint400),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("닫기")
                }
            }
        }
    }
}
