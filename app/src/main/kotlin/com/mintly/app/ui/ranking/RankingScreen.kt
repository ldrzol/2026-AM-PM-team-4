package com.mintly.app.ui.ranking

import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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
    var showChat        by remember { mutableStateOf(false) }
    var showInviteGroup by remember { mutableStateOf<FriendGroup?>(null) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) vm.loadGroups()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) { vm.startRealtime() }

    val selectedGroup    = state.groups.find { it.id == state.selectedGroupId }
    val activeRankings   = state.rankings.filter { !it.isNotEntered }
    val inactiveRankings = state.rankings.filter {  it.isNotEntered }
    val myRanked         = state.rankings.find { it.profile.id == state.myUserId }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {

        LazyColumn(modifier = Modifier.fillMaxSize()) {

            // ─── 방 선택 탭 ───────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                        .padding(top = 16.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LazyRow(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(state.groups) { group ->
                            val active = group.id == state.selectedGroupId
                            FilterChip(
                                selected = active,
                                onClick  = { vm.selectGroup(group.id) },
                                label    = {
                                    Text(
                                        "${group.emoji} ${group.name}  ·  ${state.rankings.size}",
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
                    IconButton(onClick = { vm.loadGroups() }, modifier = Modifier.padding(end = 4.dp)) {
                        Icon(Icons.Rounded.Refresh, null, tint = 거지방Colors.Gray400)
                    }
                }
            }

            // ─── 로딩 ─────────────────────────────────────────
            if (state.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = 거지방Colors.Mint400)
                    }
                }
            } else if (state.groups.isEmpty()) {
                item {
                    EmptyState(
                        emoji   = "🏠",
                        message = "아직 참여한 방이 없어요\n설정에서 방을 만들거나 참여해보세요",
                    )
                }
            } else if (selectedGroup != null) {

                // ─── 민트 헤더 + 포디움 ───────────────────────
                item {
                    MintHeader(
                        group          = selectedGroup,
                        myRanked       = myRanked,
                        activeRankings = activeRankings,
                        myUserId       = state.myUserId,
                    )
                }

                // ─── 방 정보 행 ───────────────────────────────
                item {
                    GroupInfoRow(
                        group       = selectedGroup,
                        memberCount = state.rankings.size,
                        onInvite    = { showInviteGroup = selectedGroup },
                    )
                }

                // ─── 순위 리스트 (활성) ───────────────────────
                items(activeRankings, key = { it.profile.id + "_active" }) { ranked ->
                    RankedRow(
                        ranked  = ranked,
                        isMe    = ranked.profile.id == state.myUserId,
                        onClick = { vm.selectFriend(ranked.profile) },
                    )
                }

                // ─── 미입력 구분선 ────────────────────────────
                if (inactiveRankings.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = 거지방Colors.Gray200)
                            Text("미입력", style = MaterialTheme.typography.labelSmall, color = 거지방Colors.Gray400, fontWeight = FontWeight.SemiBold)
                            HorizontalDivider(modifier = Modifier.weight(1f), color = 거지방Colors.Gray200)
                        }
                    }
                    items(inactiveRankings, key = { it.profile.id + "_inactive" }) { ranked ->
                        RankedRow(
                            ranked  = ranked,
                            isMe    = ranked.profile.id == state.myUserId,
                            onClick = { vm.selectFriend(ranked.profile) },
                        )
                    }
                }

                // ─── 주간/월간 누적 버튼 ─────────────────────
                item {
                    Spacer(Modifier.height(8.dp))
                    WeeklyMonthlyButton()
                    Spacer(Modifier.height(96.dp))
                }
            }
        }

        // ─── 채팅 FAB ─────────────────────────────────────────
        FloatingActionButton(
            onClick       = { showChat = true },
            modifier      = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = 거지방Colors.Mint400,
            contentColor  = Color.White,
            elevation     = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
        ) {
            Icon(Icons.Rounded.Chat, "채팅", modifier = Modifier.size(26.dp))
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
        InviteSheet(group = group, onDismiss = { showInviteGroup = null })
    }

    // ─── 채팅 화면 ────────────────────────────────────────────
    if (showChat) {
        val groupId   = state.selectedGroupId
        val groupName = state.groups.find { it.id == groupId }?.name ?: "그룹 채팅"
        if (groupId != null) {
            ChatScreen(groupId = groupId, groupName = groupName, onBack = { showChat = false })
        }
    }
}

// ─── 민트 헤더 (포디움 포함) ──────────────────────────────
@Composable
private fun MintHeader(
    group: FriendGroup,
    myRanked: RankedMember?,
    activeRankings: List<RankedMember>,
    myUserId: String?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(거지방Colors.Mint50)
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 내 순위 텍스트
        Text(
            if (myRanked != null && !myRanked.isNotEntered)
                "오늘 ${group.name}에서 나는 ${myRanked.rank}등"
            else
                "오늘 ${group.name} 랭킹",
            style     = MaterialTheme.typography.bodyMedium,
            color     = 거지방Colors.Mint700,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "가장 적게 쓴 사람이 1등!",
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color      = 거지방Colors.Gray900,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "1등은 룰렛권 2장 + 왕관  ·  꼴등은 거지옷 😤",
            style = MaterialTheme.typography.bodySmall,
            color = 거지방Colors.Gray500,
        )
        Spacer(Modifier.height(28.dp))

        if (activeRankings.isNotEmpty()) {
            RankingPodium(activeRankings = activeRankings, myUserId = myUserId)
        } else {
            Spacer(Modifier.height(12.dp))
            Text(
                "오늘 수입·지출 입력 시 순위에 반영돼요",
                style = MaterialTheme.typography.bodySmall,
                color = 거지방Colors.Gray400,
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

// ─── 포디움 ───────────────────────────────────────────────
@Composable
private fun RankingPodium(activeRankings: List<RankedMember>, myUserId: String?) {
    val first  = activeRankings.getOrNull(0)
    val second = activeRankings.getOrNull(1)
    val third  = activeRankings.getOrNull(2)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment     = Alignment.Bottom,
        horizontalArrangement = Arrangement.Center,
    ) {
        PodiumColumn(member = second, rank = 2, podiumHeight = 62.dp,  podiumColor = Color(0xFFB0B7C3), myUserId = myUserId)
        Spacer(Modifier.width(6.dp))
        PodiumColumn(member = first,  rank = 1, podiumHeight = 88.dp,  podiumColor = Color(0xFFE8B547), myUserId = myUserId)
        Spacer(Modifier.width(6.dp))
        PodiumColumn(member = third,  rank = 3, podiumHeight = 44.dp,  podiumColor = Color(0xFFC07E40), myUserId = myUserId)
    }
}

@Composable
private fun PodiumColumn(
    member: RankedMember?,
    rank: Int,
    podiumHeight: Dp,
    podiumColor: Color,
    myUserId: String?,
) {
    val pct = if (member != null && member.incomeAmount > 0)
        (member.spentAmount.toDouble() / member.incomeAmount * 100).toInt()
    else null

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(104.dp),
    ) {
        if (member != null) {
            Box {
                MiniAvatar(
                    size  = 54.dp,
                    color = member.profile.avatarColor,
                    face  = member.profile.avatarFace.toAvatarFace(),
                    hat   = when {
                        rank == 1                            -> "crown"
                        member.profile.hasCrownUntil != null -> "crown"
                        else                                 -> member.profile.currentHat
                    },
                )
                if (member.profile.id == myUserId) {
                    Surface(
                        shape    = ShapePill,
                        color    = 거지방Colors.Mint400,
                        modifier = Modifier.align(Alignment.BottomEnd),
                    ) {
                        Text("나", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp))
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                member.profile.displayName,
                style     = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color     = 거지방Colors.Gray800,
                maxLines  = 1,
                overflow  = TextOverflow.Ellipsis,
            )
            Text(
                if (pct != null) "${pct}%" else "─",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color      = if (rank == 1) 거지방Colors.CoinDark else 거지방Colors.Gray600,
            )
        } else {
            Box(Modifier.size(54.dp))
            Spacer(Modifier.height(6.dp))
            Text(" ", style = MaterialTheme.typography.bodySmall)
            Text(" ", style = MaterialTheme.typography.titleSmall)
        }

        Spacer(Modifier.height(8.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(podiumHeight)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(podiumColor),
        ) {
            Text("$rank", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

// ─── 방 정보 행 ───────────────────────────────────────────
@Composable
private fun GroupInfoRow(group: FriendGroup, memberCount: Int, onInvite: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                "${group.emoji} ${group.name}",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = 거지방Colors.Gray900,
            )
            Text(
                "${memberCount}명 참여  ·  코드 ${group.inviteCode}",
                style = MaterialTheme.typography.bodySmall,
                color = 거지방Colors.Gray400,
            )
        }
        Surface(
            shape    = ShapePill,
            color    = 거지방Colors.Mint50,
            border   = BorderStroke(1.dp, 거지방Colors.Mint200),
            modifier = Modifier.clickable(onClick = onInvite),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Icon(Icons.Rounded.PersonAdd, null, tint = 거지방Colors.Mint600, modifier = Modifier.size(14.dp))
                Text("초대", color = 거지방Colors.Mint700, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
    HorizontalDivider(color = 거지방Colors.Gray100)
}

// ─── 순위 행 ──────────────────────────────────────────────
@Composable
private fun RankedRow(ranked: RankedMember, isMe: Boolean, onClick: () -> Unit) {
    val isNotEntered = ranked.isNotEntered
    val isFirst      = ranked.rank == 1 && !isNotEntered
    val isLast       = ranked.isLoser && !isNotEntered

    val pct = if (!isNotEntered && ranked.incomeAmount > 0)
        (ranked.spentAmount.toDouble() / ranked.incomeAmount * 100).toInt()
    else null

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color  = if (isMe) 거지방Colors.Mint50 else Color.White,
        shape  = RoundedCornerShape(0.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 13.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 순위 원
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isNotEntered     -> 거지방Colors.Gray100
                            ranked.rank == 1 -> 거지방Colors.Coin
                            ranked.rank == 2 -> Color(0xFFB0B7C3)
                            ranked.rank == 3 -> Color(0xFFC07E40)
                            else             -> 거지방Colors.Gray100
                        }
                    ),
            ) {
                Text(
                    if (isNotEntered) "─" else "${ranked.rank}",
                    fontSize   = if (!isNotEntered && ranked.rank <= 3) 15.sp else 13.sp,
                    fontWeight = FontWeight.Bold,
                    color      = if (!isNotEntered && ranked.rank <= 3) Color.White else 거지방Colors.Gray500,
                )
            }

            // 아바타 + 뱃지
            Box {
                MiniAvatar(
                    size    = 42.dp,
                    color   = ranked.profile.avatarColor,
                    face    = ranked.profile.avatarFace.toAvatarFace(),
                    hat     = if (ranked.profile.hasCrownUntil != null) "crown" else ranked.profile.currentHat,
                    outfit  = ranked.profile.forcedOutfit ?: ranked.profile.currentOutfit,
                )
                when {
                    isFirst -> Text("👑", modifier = Modifier.align(Alignment.TopEnd).offset(x = 4.dp, y = (-4).dp), fontSize = 13.sp)
                    isLast  -> Text("🌫️", modifier = Modifier.align(Alignment.TopEnd).offset(x = 4.dp, y = (-4).dp), fontSize = 13.sp)
                }
            }

            // 이름 열
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Text(
                        ranked.profile.displayName,
                        style      = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color      = if (isNotEntered) 거지방Colors.Gray400 else 거지방Colors.Gray900,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis,
                    )
                    if (isMe) {
                        Surface(shape = ShapePill, color = 거지방Colors.Mint400) {
                            Text(
                                "나",
                                color      = Color.White,
                                style      = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                }
                Text(
                    if (isNotEntered) "미입력" else "오늘 지출",
                    style = MaterialTheme.typography.bodySmall,
                    color = 거지방Colors.Gray400,
                )
            }

            // 퍼센트 (오른쪽)
            if (!isNotEntered) {
                Text(
                    if (pct != null) "${pct}%" else "─",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color      = when {
                        isFirst -> 거지방Colors.CoinDark
                        isLast  -> 거지방Colors.Danger
                        else    -> 거지방Colors.Gray700
                    },
                )
            }
        }
    }
    HorizontalDivider(color = 거지방Colors.Gray100)
}

// ─── 주간/월간 누적 랭킹 버튼 ────────────────────────────
@Composable
private fun WeeklyMonthlyButton(onClick: () -> Unit = {}) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        color    = Color.White,
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(Icons.Rounded.BarChart, null, tint = 거지방Colors.Mint500, modifier = Modifier.size(22.dp))
                Text(
                    "주간 / 월간 누적 랭킹",
                    style      = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color      = 거지방Colors.Gray800,
                )
            }
            Icon(Icons.Rounded.KeyboardArrowRight, null, tint = 거지방Colors.Gray400, modifier = Modifier.size(22.dp))
        }
    }
    HorizontalDivider(color = 거지방Colors.Gray100)
}

// ─── 빈 상태 ─────────────────────────────────────────────
@Composable
private fun EmptyState(emoji: String, message: String) {
    Column(
        modifier              = Modifier.fillMaxWidth().padding(vertical = 80.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.spacedBy(12.dp),
    ) {
        Text(emoji, fontSize = 48.sp)
        Text(
            message,
            style      = MaterialTheme.typography.bodyMedium,
            color      = 거지방Colors.Gray400,
            textAlign  = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

// ─── 초대 시트 ────────────────────────────────────────────
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

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = Color.White) {
        Column(
            modifier              = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp),
            horizontalAlignment   = Alignment.CenterHorizontally,
            verticalArrangement   = Arrangement.spacedBy(0.dp),
        ) {
            Text("${group.emoji} ${group.name}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = 거지방Colors.Gray900)
            Spacer(Modifier.height(4.dp))
            Text("친구를 초대해보세요", style = MaterialTheme.typography.bodyMedium, color = 거지방Colors.Gray500)
            Spacer(Modifier.height(20.dp))

            Surface(modifier = Modifier.fillMaxWidth(), shape = Shape14, color = 거지방Colors.Mint50, border = BorderStroke(1.5.dp, 거지방Colors.Mint200)) {
                Column(modifier = Modifier.padding(vertical = 18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("초대 코드", style = MaterialTheme.typography.labelSmall, color = 거지방Colors.Mint600, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        group.inviteCode.forEach { ch ->
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp).clip(Shape10).background(Color.White).border(1.dp, 거지방Colors.Mint200, Shape10)) {
                                Text(ch.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = 거지방Colors.Mint700)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick  = { clipboard.setText(AnnotatedString(group.inviteCode)); justCopied = true },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape    = ShapePill,
                    border   = BorderStroke(1.5.dp, if (justCopied) 거지방Colors.Mint400 else 거지방Colors.Gray200),
                ) {
                    Icon(if (justCopied) Icons.Rounded.Check else Icons.Rounded.ContentCopy, null, tint = if (justCopied) 거지방Colors.Mint500 else 거지방Colors.Gray600, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (justCopied) "복사됨!" else "코드 복사", color = if (justCopied) 거지방Colors.Mint500 else 거지방Colors.Gray700, fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick  = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "거지방 '${group.name}'에 초대합니다!\n초대 코드: ${group.inviteCode}\n앱에서 '방 참여하기'를 눌러 입력해 주세요 🏠")
                        }
                        context.startActivity(Intent.createChooser(intent, "초대 공유하기"))
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape    = ShapePill,
                    colors   = ButtonDefaults.buttonColors(containerColor = 거지방Colors.Mint400),
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
                modifier              = Modifier.padding(24.dp),
                horizontalAlignment   = Alignment.CenterHorizontally,
                verticalArrangement   = Arrangement.spacedBy(16.dp),
            ) {
                거지방Avatar(
                    size    = 100.dp,
                    color   = profile.avatarColor,
                    face    = profile.avatarFace.toAvatarFace(),
                    hat     = if (profile.hasCrownUntil != null) "crown" else profile.currentHat,
                    outfit  = profile.forcedOutfit ?: profile.currentOutfit,
                    forced  = profile.forcedOutfit != null,
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(profile.displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("@${profile.username}", style = MaterialTheme.typography.bodyMedium, color = 거지방Colors.Gray500)
                }

                if (rankedMember != null) {
                    Surface(
                        shape    = Shape14,
                        color    = if (rankedMember.isNotEntered) 거지방Colors.Gray50 else 거지방Colors.Mint50,
                        border   = BorderStroke(1.dp, if (rankedMember.isNotEntered) 거지방Colors.Gray200 else 거지방Colors.Mint200),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier              = Modifier.padding(16.dp),
                            horizontalAlignment   = Alignment.CenterHorizontally,
                            verticalArrangement   = Arrangement.spacedBy(8.dp),
                        ) {
                            if (rankedMember.isNotEntered) {
                                Text("오늘 거래 없음", style = MaterialTheme.typography.bodySmall, color = 거지방Colors.Gray400)
                                Text("─", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = 거지방Colors.Gray300)
                            } else {
                                val pct = if (rankedMember.incomeAmount > 0)
                                    (rankedMember.spentAmount.toDouble() / rankedMember.incomeAmount * 100).toInt()
                                else null
                                Text("수입 대비 지출", style = MaterialTheme.typography.bodySmall, color = 거지방Colors.Gray500)
                                Text(
                                    if (pct != null) "${pct}%" else "수입 미기록",
                                    style      = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color      = if (pct != null) 거지방Colors.Expense else 거지방Colors.Gray400,
                                )
                                if (pct != null) {
                                    Text(
                                        "수입 %,d원 → 지출 %,d원".format(rankedMember.incomeAmount, rankedMember.spentAmount),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = 거지방Colors.Gray400,
                                    )
                                }
                                Text("현재 ${rankedMember.rank}위", style = MaterialTheme.typography.bodySmall, color = 거지방Colors.Mint600)
                            }
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    shape   = ShapePill,
                    colors  = ButtonDefaults.buttonColors(containerColor = 거지방Colors.Mint400),
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("닫기") }
            }
        }
    }
}
