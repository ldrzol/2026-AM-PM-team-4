package com.mintly.app.ui.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.content.Intent
import com.mintly.app.data.model.Category
import com.mintly.app.data.model.Favorite
import com.mintly.app.data.model.FriendGroup
import com.mintly.app.ui.components.*
import com.mintly.app.ui.home.toAvatarFace
import com.mintly.app.ui.theme.거지방Colors
import com.mintly.app.ui.theme.Shape10
import com.mintly.app.ui.theme.Shape14
import com.mintly.app.ui.theme.Shape20
import com.mintly.app.ui.theme.ShapePill

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    vm: SettingsViewModel = hiltViewModel(),
    onLogout: () -> Unit,
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    var showEditName      by remember { mutableStateOf(false) }
    var showPersonalInfo  by remember { mutableStateOf(false) }
    var showCreateRoom    by remember { mutableStateOf(false) }
    var showJoinRoom      by remember { mutableStateOf(false) }
    var showFavorites     by remember { mutableStateOf(false) }
    var showCategories    by remember { mutableStateOf(false) }
    var showShareMode     by remember { mutableStateOf(false) }
    var showSpendingAlert by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var createdRoom       by remember { mutableStateOf<FriendGroup?>(null) }

    var alertEnabled by remember { mutableStateOf(false) }
    var alertAmount  by remember { mutableStateOf("") }

    LaunchedEffect(state.error, state.successMsg) {
        if (state.error != null || state.successMsg != null) {
            kotlinx.coroutines.delay(2500)
            vm.clearMessages()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState()),
        ) {

            // ─── 프로필 헤더 ────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEAF8F5))
                    .padding(horizontal = 32.dp, vertical = 32.dp),
            ) {
                state.profile?.let { profile ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(22.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(116.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center,
                        ) {
                            거지방Avatar(
                                size   = 82.dp,
                                color  = profile.avatarColor,
                                face   = profile.avatarFace.toAvatarFace(),
                                hat    = if (profile.hasCrownUntil != null) "crown" else profile.currentHat,
                                outfit = profile.forcedOutfit ?: profile.currentOutfit,
                                forced = profile.forcedOutfit != null,
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                profile.displayName.ifBlank { profile.username.ifBlank { "사용자" } },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black,
                            )
                        }
                        SettingsCoinChip(amount = profile.coins)
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ─── 정보 ───────────────────────────────────────────
            SettingsGroupLabel("정보")
            SettingsCard {
                SettingsRow(
                    icon      = Icons.Rounded.Person,
                    iconBg    = 거지방Colors.Mint100,
                    iconColor = 거지방Colors.Mint600,
                    title     = "프로필 수정",
                    subtitle  = "닉네임",
                    onClick   = { showEditName = true },
                )
                RowDivider()
                SettingsRow(
                    icon      = Icons.Rounded.Lock,
                    iconBg    = 거지방Colors.Mint100,
                    iconColor = 거지방Colors.Mint600,
                    title     = "개인정보 수정",
                    subtitle  = "비밀번호, 이메일",
                    onClick   = { showPersonalInfo = true },
                )
            }

            Spacer(Modifier.height(28.dp))

            // ─── 소셜 ───────────────────────────────────────────
            SettingsGroupLabel("소셜")
            SettingsCard {
                SettingsRow(
                    icon      = Icons.Rounded.MeetingRoom,
                    iconBg    = 거지방Colors.Mint100,
                    iconColor = 거지방Colors.Mint600,
                    title     = "방 만들기",
                    subtitle  = "초대 코드 생성",
                    onClick   = { showCreateRoom = true },
                )
                RowDivider()
                SettingsRow(
                    icon      = Icons.Rounded.QrCode2,
                    iconBg    = 거지방Colors.Mint100,
                    iconColor = 거지방Colors.Mint600,
                    title     = "방 참여하기",
                    subtitle  = "초대 코드 입력",
                    onClick   = { showJoinRoom = true },
                )
            }

            Spacer(Modifier.height(28.dp))

            // ─── 가계부 설정 ────────────────────────────────────
            SettingsGroupLabel("가계부 설정")
            SettingsCard {
                SettingsRow(
                    icon      = Icons.Rounded.Star,
                    iconBg    = 거지방Colors.Mint100,
                    iconColor = 거지방Colors.Mint600,
                    title     = "즐겨찾기",
                    subtitle  = "자주 사용하는 항목",
                    onClick   = { showFavorites = true },
                )
                RowDivider()
                SettingsRow(
                    icon      = Icons.Rounded.Category,
                    iconBg    = 거지방Colors.Mint100,
                    iconColor = 거지방Colors.Mint600,
                    title     = "카테고리",
                    subtitle  = "수입/지출 항목 관리",
                    onClick   = { showCategories = true },
                )
                RowDivider()
                // 과소비 알람 (인라인 스위치)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showSpendingAlert = true }
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(50.dp).clip(CircleShape).background(거지방Colors.Mint100.copy(alpha = 0.72f)),
                    ) {
                        Icon(Icons.Rounded.NotificationsActive, null, tint = 거지방Colors.Mint600.copy(alpha = 0.72f), modifier = Modifier.size(23.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("과소비 알람 설정", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = Color.Black)
                        Text(
                            if (alertEnabled && alertAmount.isNotBlank())
                                "%,d원 초과 알림".format(alertAmount.toLongOrNull() ?: 0L)
                            else if (alertEnabled) "켜짐" else "꺼짐",
                            style = MaterialTheme.typography.bodySmall,
                            color = 거지방Colors.Gray500,
                        )
                    }
                    Icon(Icons.Rounded.KeyboardArrowRight, null, tint = 거지방Colors.Gray400, modifier = Modifier.size(30.dp))
                }
                RowDivider()
                SettingsRow(
                    icon      = Icons.Rounded.Share,
                    iconBg    = 거지방Colors.Mint100,
                    iconColor = 거지방Colors.Mint600,
                    title     = "지출 범위 공유",
                    subtitle  = when (state.profile?.shareMode) {
                        "percent" -> "퍼센트로 공유"
                        "amount"  -> "금액으로 공유"
                        else      -> null
                    },
                    onClick   = { showShareMode = true },
                )
            }

            Spacer(Modifier.height(28.dp))

            // ─── 기타 ───────────────────────────────────────────
            SettingsGroupLabel("기타")
            SettingsCard {
                SettingsRow(
                    icon       = Icons.Rounded.Logout,
                    iconBg     = Color(0xFFFDECEC),
                    iconColor  = 거지방Colors.Danger,
                    title      = "로그아웃",
                    titleColor = 거지방Colors.Danger,
                    showArrow  = false,
                    onClick    = { showLogoutConfirm = true },
                )
            }

            Spacer(Modifier.height(32.dp))
            Text(
                "Mintly v1.0.0",
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                style = MaterialTheme.typography.labelSmall,
                color = 거지방Colors.Gray300,
                textAlign = TextAlign.Center,
            )
        }

        // ─── 스낵바 ─────────────────────────────────────────────
        if (state.error != null || state.successMsg != null) {
            Box(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
            ) {
                거지방Snackbar(
                    message   = state.error ?: state.successMsg,
                    isError   = state.error != null,
                    onDismiss = vm::clearMessages,
                )
            }
        }
    }

    // ─── 다이얼로그 / 시트 ─────────────────────────────────────
    if (showEditName) {
        EditNameDialog(
            current   = state.profile?.displayName ?: "",
            onSave    = { vm.updateDisplayName(it); showEditName = false },
            onDismiss = { showEditName = false },
        )
    }
    if (showPersonalInfo) {
        PersonalInfoSheet(
            onChangePassword = { vm.changePassword(it) },
            onChangeEmail    = { vm.changeEmail(it) },
            onDismiss = { showPersonalInfo = false },
        )
    }
    if (showCreateRoom) {
        CreateRoomDialog(
            onCreate = { name, emoji ->
                vm.createRoom(name, emoji) { group ->
                    createdRoom = group
                }
                showCreateRoom = false
            },
            onDismiss = { showCreateRoom = false },
        )
    }
    createdRoom?.let { room ->
        RoomCreatedSheet(
            room      = room,
            onDismiss = { createdRoom = null },
        )
    }
    if (showJoinRoom) {
        JoinRoomDialog(
            onJoin    = { code -> vm.joinRoom(code) {}; showJoinRoom = false },
            onDismiss = { showJoinRoom = false },
        )
    }
    if (showFavorites) {
        FavoritesSheet(
            favorites  = state.favorites,
            categories = state.categories,
            onAdd      = { vm.addFavorite(it) },
            onDelete   = { vm.deleteFavorite(it) },
            onDismiss  = { showFavorites = false },
        )
    }
    if (showCategories) {
        CategoryManagementSheet(
            categories = state.categories,
            onAdd      = { name, icon, color, kind -> vm.addCategory(name, icon, color, kind) },
            onDelete   = { vm.deleteCategory(it) },
            onDismiss  = { showCategories = false },
        )
    }
    if (showShareMode) {
        ShareModeDialog(
            current   = state.profile?.shareMode ?: "percent",
            onSelect  = { vm.updateShareMode(it); showShareMode = false },
            onDismiss = { showShareMode = false },
        )
    }
    if (showSpendingAlert) {
        SpendingAlertDialog(
            enabled       = alertEnabled,
            currentAmount = alertAmount,
            onSave        = { enabled, amount -> alertEnabled = enabled; alertAmount = amount; showSpendingAlert = false },
            onDismiss     = { showSpendingAlert = false },
        )
    }
    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            icon   = { Icon(Icons.Rounded.Logout, null, tint = 거지방Colors.Danger) },
            title  = { Text("로그아웃", fontWeight = FontWeight.Bold) },
            text   = { Text("정말 로그아웃하시겠습니까?", color = 거지방Colors.Gray600) },
            confirmButton = {
                Button(
                    onClick = { vm.signOut(onLogout) },
                    colors  = ButtonDefaults.buttonColors(containerColor = 거지방Colors.Danger),
                    shape   = ShapePill,
                ) { Text("로그아웃") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("취소", color = 거지방Colors.Gray600)
                }
            },
            shape = Shape20,
        )
    }
}

// ──────────────────────────────────────────────────────────
// 공통 헬퍼 컴포넌트
// ──────────────────────────────────────────────────────────

@Composable
private fun SettingsGroupLabel(text: String) {
    Text(
        text      = text,
        modifier  = Modifier.padding(horizontal = 36.dp, vertical = 8.dp),
        style     = MaterialTheme.typography.titleSmall,
        color     = 거지방Colors.Gray500,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun SettingsCoinChip(amount: Int) {
    Surface(
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
                modifier = Modifier.size(30.dp).clip(CircleShape).background(거지방Colors.Coin),
                contentAlignment = Alignment.Center,
            ) {
                Text("₩", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Text("%,d".format(amount), style = MaterialTheme.typography.titleSmall, color = 거지방Colors.CoinDark, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier        = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        shape           = Shape20,
        color           = Color.White,
        tonalElevation  = 0.dp,
        shadowElevation = 1.dp,
        border          = BorderStroke(1.dp, 거지방Colors.Gray100),
    ) {
        Column(content = content)
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(
        modifier  = Modifier.padding(start = 86.dp),
        color     = 거지방Colors.Gray100,
        thickness = 0.5.dp,
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    iconBg: Color,
    iconColor: Color,
    title: String,
    subtitle: String?  = null,
    titleColor: Color  = 거지방Colors.Gray900,
    showArrow: Boolean = true,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = if (titleColor == 거지방Colors.Danger) 14.dp else 17.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(if (titleColor == 거지방Colors.Danger) 44.dp else 50.dp).clip(CircleShape).background(iconBg.copy(alpha = 0.72f)),
        ) {
            Icon(icon, null, tint = iconColor.copy(alpha = 0.72f), modifier = Modifier.size(if (titleColor == 거지방Colors.Danger) 21.dp else 23.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = titleColor)
            if (subtitle != null) {
                Spacer(Modifier.height(3.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = 거지방Colors.Gray500)
            }
        }
        if (showArrow) {
            Icon(Icons.Rounded.ChevronRight, null, tint = 거지방Colors.Gray300, modifier = Modifier.size(30.dp))
        }
    }
}

// ──────────────────────────────────────────────────────────
// 개인정보 수정 시트
// ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonalInfoSheet(
    onChangePassword: (String) -> Unit,
    onChangeEmail: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showChangePw    by remember { mutableStateOf(false) }
    var showChangeEmail by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
        ) {
            // 헤더
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 4.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("개인정보 수정", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = 거지방Colors.Gray900)
                IconButton(onClick = onDismiss) { Icon(Icons.Rounded.Close, null, tint = 거지방Colors.Gray400) }
            }
            HorizontalDivider(color = 거지방Colors.Gray100)

            // 비밀번호 변경
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showChangePw = true }
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFE8F0FE)),
                ) {
                    Icon(Icons.Rounded.Lock, null, tint = Color(0xFF3F6BB3), modifier = Modifier.size(24.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("비밀번호 변경", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = 거지방Colors.Gray900)
                    Text("계정 비밀번호를 변경합니다", style = MaterialTheme.typography.bodySmall, color = 거지방Colors.Gray400)
                }
                Icon(Icons.Rounded.ChevronRight, null, tint = 거지방Colors.Gray300, modifier = Modifier.size(20.dp))
            }

            HorizontalDivider(modifier = Modifier.padding(start = 84.dp), color = 거지방Colors.Gray100, thickness = 0.5.dp)

            // 이메일 변경
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showChangeEmail = true }
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFE8F0FE)),
                ) {
                    Icon(Icons.Rounded.Email, null, tint = Color(0xFF3F6BB3), modifier = Modifier.size(24.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("이메일 변경", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = 거지방Colors.Gray900)
                    Text("계정 이메일을 변경합니다", style = MaterialTheme.typography.bodySmall, color = 거지방Colors.Gray400)
                }
                Icon(Icons.Rounded.ChevronRight, null, tint = 거지방Colors.Gray300, modifier = Modifier.size(20.dp))
            }
        }
    }

    if (showChangePw) {
        ChangePasswordDialog(
            onSave    = { onChangePassword(it); showChangePw = false },
            onDismiss = { showChangePw = false },
        )
    }
    if (showChangeEmail) {
        ChangeEmailDialog(
            onSave    = { onChangeEmail(it); showChangeEmail = false },
            onDismiss = { showChangeEmail = false },
        )
    }
}

@Composable
private fun ChangeEmailDialog(onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var newEmail    by remember { mutableStateOf("") }
    var confirmEmail by remember { mutableStateOf("") }
    val match       = newEmail.isNotBlank() && newEmail == confirmEmail
    val validFormat = newEmail.contains("@") && newEmail.contains(".")

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = Shape20, color = Color.White, shadowElevation = 8.dp) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFE8F0FE)),
                    ) { Icon(Icons.Rounded.Email, null, tint = Color(0xFF3F6BB3), modifier = Modifier.size(20.dp)) }
                    Text("이메일 변경", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Text(
                    "새 이메일로 인증 링크가 발송됩니다.\n링크를 클릭하면 이메일이 변경됩니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = 거지방Colors.Gray500,
                )
                OutlinedTextField(
                    value = newEmail,
                    onValueChange = { newEmail = it },
                    label = { Text("새 이메일") },
                    singleLine = true,
                    shape = Shape10,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = 거지방Colors.Mint400,
                        unfocusedBorderColor = 거지방Colors.Gray200,
                    ),
                )
                OutlinedTextField(
                    value = confirmEmail,
                    onValueChange = { confirmEmail = it },
                    label = { Text("이메일 확인") },
                    singleLine = true,
                    shape = Shape10,
                    modifier = Modifier.fillMaxWidth(),
                    isError = confirmEmail.isNotBlank() && newEmail != confirmEmail,
                    supportingText = {
                        if (confirmEmail.isNotBlank() && newEmail != confirmEmail) {
                            Text("이메일이 일치하지 않습니다", color = 거지방Colors.Danger)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = 거지방Colors.Mint400,
                        unfocusedBorderColor = 거지방Colors.Gray200,
                    ),
                )
                if (newEmail.isNotBlank() && !validFormat) {
                    Text("올바른 이메일 형식을 입력해주세요", style = MaterialTheme.typography.bodySmall, color = 거지방Colors.Warning)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss, modifier = Modifier.weight(1f), shape = ShapePill,
                        border = BorderStroke(1.dp, 거지방Colors.Gray200),
                    ) { Text("취소", color = 거지방Colors.Gray600) }
                    Button(
                        onClick = { if (match && validFormat) onSave(newEmail) },
                        enabled = match && validFormat,
                        modifier = Modifier.weight(1f), shape = ShapePill,
                        colors = ButtonDefaults.buttonColors(containerColor = 거지방Colors.Mint400),
                    ) { Text("변경") }
                }
            }
        }
    }
}

@Composable
private fun ChangePasswordDialog(onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var newPw    by remember { mutableStateOf("") }
    var confirmPw by remember { mutableStateOf("") }
    var showPw   by remember { mutableStateOf(false) }
    val match    = newPw.isNotBlank() && newPw == confirmPw
    val minLen   = newPw.length >= 6

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = Shape20, color = Color.White, shadowElevation = 8.dp) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFE8F0FE)),
                    ) { Icon(Icons.Rounded.Lock, null, tint = Color(0xFF3F6BB3), modifier = Modifier.size(20.dp)) }
                    Text("비밀번호 변경", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                OutlinedTextField(
                    value = newPw,
                    onValueChange = { newPw = it },
                    label = { Text("새 비밀번호") },
                    singleLine = true,
                    shape = Shape10,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (showPw) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPw = !showPw }) {
                            Icon(
                                if (showPw) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                null, tint = 거지방Colors.Gray400, modifier = Modifier.size(18.dp),
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = 거지방Colors.Mint400,
                        unfocusedBorderColor = 거지방Colors.Gray200,
                    ),
                )
                OutlinedTextField(
                    value = confirmPw,
                    onValueChange = { confirmPw = it },
                    label = { Text("비밀번호 확인") },
                    singleLine = true,
                    shape = Shape10,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (showPw) VisualTransformation.None else PasswordVisualTransformation(),
                    isError = confirmPw.isNotBlank() && newPw != confirmPw,
                    supportingText = {
                        if (confirmPw.isNotBlank() && newPw != confirmPw) {
                            Text("비밀번호가 일치하지 않습니다", color = 거지방Colors.Danger)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = 거지방Colors.Mint400,
                        unfocusedBorderColor = 거지방Colors.Gray200,
                    ),
                )
                if (newPw.isNotBlank() && !minLen) {
                    Text("6자 이상 입력해주세요", style = MaterialTheme.typography.bodySmall, color = 거지방Colors.Warning)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss, modifier = Modifier.weight(1f), shape = ShapePill,
                        border = BorderStroke(1.dp, 거지방Colors.Gray200),
                    ) { Text("취소", color = 거지방Colors.Gray600) }
                    Button(
                        onClick = { if (match && minLen) onSave(newPw) },
                        enabled = match && minLen,
                        modifier = Modifier.weight(1f), shape = ShapePill,
                        colors = ButtonDefaults.buttonColors(containerColor = 거지방Colors.Mint400),
                    ) { Text("변경") }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────
// 즐겨찾기 관리 시트
// ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoritesSheet(
    favorites: List<Favorite>,
    categories: List<Category>,
    onAdd: (Favorite) -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedKind  by remember { mutableStateOf("expense") }
    var showAddDialog by remember { mutableStateOf(false) }

    val filtered = favorites.filter { it.kind == selectedKind }
    val accentColor = if (selectedKind == "expense") 거지방Colors.Expense else 거지방Colors.Income

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
            // 헤더
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 4.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("즐겨찾기", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = 거지방Colors.Gray900)
                IconButton(onClick = onDismiss) { Icon(Icons.Rounded.Close, null, tint = 거지방Colors.Gray400) }
            }

            // 종류 탭
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
                    .clip(ShapePill)
                    .background(거지방Colors.Gray100)
                    .padding(4.dp),
            ) {
                listOf("expense" to "지출", "income" to "수입").forEach { (id, label) ->
                    val active = selectedKind == id
                    val aColor = if (id == "expense") 거지방Colors.Expense else 거지방Colors.Income
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(ShapePill)
                            .background(if (active) Color.White else Color.Transparent)
                            .clickable { selectedKind = id }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal, color = if (active) aColor else 거지방Colors.Gray500)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("⭐", fontSize = 32.sp)
                        Text("등록된 즐겨찾기가 없습니다", style = MaterialTheme.typography.bodyMedium, color = 거지방Colors.Gray400)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp),
                ) {
                    items(filtered) { fav ->
                        FavoriteListItem(fav = fav, accentColor = accentColor, onDelete = onDelete)
                        HorizontalDivider(modifier = Modifier.padding(start = 76.dp), color = 거지방Colors.Gray100, thickness = 0.5.dp)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(50.dp),
                shape = ShapePill,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
            ) {
                Icon(Icons.Rounded.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("즐겨찾기 추가", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    if (showAddDialog) {
        AddFavoriteDialog(
            kind       = selectedKind,
            categories = categories,
            onSave     = { onAdd(it); showAddDialog = false },
            onDismiss  = { showAddDialog = false },
        )
    }
}

@Composable
private fun FavoriteListItem(fav: Favorite, accentColor: Color, onDelete: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(44.dp).clip(CircleShape).background(accentColor.copy(alpha = 0.1f)),
        ) {
            Text(getCategoryEmoji(fav.category?.icon ?: ""), fontSize = 20.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                fav.category?.name ?: "미분류",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = 거지방Colors.Gray900,
            )
            val detail = buildString {
                fav.amount?.let { append("%,d원".format(it)) }
                fav.memo?.takeIf { it.isNotBlank() }?.let { if (isNotEmpty()) append(" · "); append(it) }
            }
            if (detail.isNotBlank()) {
                Text(detail, style = MaterialTheme.typography.bodyMedium, color = 거지방Colors.Gray400)
            }
        }
        IconButton(
            onClick = { onDelete(fav.id) },
            modifier = Modifier.size(36.dp),
        ) {
            Icon(Icons.Rounded.Close, null, tint = 거지방Colors.Danger, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun AddFavoriteDialog(
    kind: String,
    categories: List<Category>,
    onSave: (Favorite) -> Unit,
    onDismiss: () -> Unit,
) {
    val filteredCats = categories.filter { it.kind == kind }
    var selectedCat  by remember { mutableStateOf<Category?>(null) }
    var amount       by remember { mutableStateOf("") }
    var memo         by remember { mutableStateOf("") }
    val accentColor  = if (kind == "expense") 거지방Colors.Expense else 거지방Colors.Income

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = Shape20, color = Color.White, shadowElevation = 8.dp) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFFFF8E7)),
                    ) { Icon(Icons.Rounded.Star, null, tint = 거지방Colors.Coin, modifier = Modifier.size(20.dp)) }
                    Text("즐겨찾기 추가", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Text(
                    if (kind == "expense") "지출 즐겨찾기" else "수입 즐겨찾기",
                    style = MaterialTheme.typography.bodySmall, color = accentColor, fontWeight = FontWeight.Medium,
                )

                // 카테고리 선택
                if (filteredCats.isNotEmpty()) {
                    Text("카테고리 (선택)", style = MaterialTheme.typography.labelMedium, color = 거지방Colors.Gray500, fontWeight = FontWeight.SemiBold)
                    filteredCats.chunked(4).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { cat ->
                                val sel = selectedCat?.id == cat.id
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(Shape14)
                                        .background(if (sel) accentColor.copy(alpha = 0.12f) else 거지방Colors.Gray50)
                                        .border(if (sel) 1.5.dp else 1.dp, if (sel) accentColor else 거지방Colors.Gray200, Shape14)
                                        .clickable { selectedCat = if (sel) null else cat }
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(3.dp),
                                ) {
                                    Text(getCategoryEmoji(cat.icon), fontSize = 18.sp, textAlign = TextAlign.Center)
                                    Text(cat.name, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = if (sel) accentColor else 거지방Colors.Gray600, textAlign = TextAlign.Center, maxLines = 1)
                                }
                            }
                            repeat(4 - row.size) { Box(Modifier.weight(1f)) }
                        }
                    }
                }

                // 금액 (선택)
                거지방TextField(
                    value = amount,
                    onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 10) amount = it },
                    label = "금액 (선택)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )

                // 메모 (선택)
                거지방TextField(value = memo, onValueChange = { memo = it }, label = "메모 (선택)")

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss, modifier = Modifier.weight(1f), shape = ShapePill,
                        border = BorderStroke(1.dp, 거지방Colors.Gray200),
                    ) { Text("취소", color = 거지방Colors.Gray600) }
                    Button(
                        onClick = {
                            onSave(
                                Favorite(
                                    kind       = kind,
                                    categoryId = selectedCat?.id,
                                    amount     = amount.toIntOrNull(),
                                    memo       = memo.takeIf { it.isNotBlank() },
                                    category   = selectedCat,
                                )
                            )
                        },
                        modifier = Modifier.weight(1f), shape = ShapePill,
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    ) { Text("추가") }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────
// 카테고리 관리 시트
// ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryManagementSheet(
    categories: List<Category>,
    onAdd: (String, String, String, String) -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedKind  by remember { mutableStateOf("expense") }
    var showAddDialog by remember { mutableStateOf(false) }

    val filtered = categories.filter { it.kind == selectedKind }

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
            // 헤더
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 4.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("카테고리", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = 거지방Colors.Gray900)
                IconButton(onClick = onDismiss) { Icon(Icons.Rounded.Close, null, tint = 거지방Colors.Gray400) }
            }

            // 종류 탭
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
                    .clip(ShapePill)
                    .background(거지방Colors.Gray100)
                    .padding(4.dp),
            ) {
                listOf("expense" to "지출", "income" to "수입").forEach { (id, label) ->
                    val active = selectedKind == id
                    val aColor = if (id == "expense") 거지방Colors.Expense else 거지방Colors.Income
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(ShapePill)
                            .background(if (active) Color.White else Color.Transparent)
                            .clickable { selectedKind = id }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal, color = if (active) aColor else 거지방Colors.Gray500)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    Text("카테고리가 없습니다", style = MaterialTheme.typography.bodyMedium, color = 거지방Colors.Gray400)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp)) {
                    items(filtered) { cat ->
                        CategoryListItem(cat = cat, onDelete = if (cat.ownerId != null) onDelete else null)
                        HorizontalDivider(modifier = Modifier.padding(start = 76.dp), color = 거지방Colors.Gray100, thickness = 0.5.dp)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(50.dp),
                shape = ShapePill,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2)),
            ) {
                Icon(Icons.Rounded.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("카테고리 추가", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    if (showAddDialog) {
        AddCategoryDialog(
            kind      = selectedKind,
            onSave    = { name, icon, color -> onAdd(name, icon, color, selectedKind); showAddDialog = false },
            onDismiss = { showAddDialog = false },
        )
    }
}

@Composable
private fun CategoryListItem(cat: Category, onDelete: ((String) -> Unit)?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(44.dp).clip(CircleShape).background(cat.color.toComposeColor().copy(alpha = 0.15f)),
        ) {
            Text(getCategoryEmoji(cat.icon), fontSize = 20.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(cat.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = 거지방Colors.Gray900)
            Text(
                if (cat.ownerId != null) "사용자 카테고리" else "기본 카테고리",
                style = MaterialTheme.typography.bodyMedium,
                color = if (cat.ownerId != null) 거지방Colors.Mint500 else 거지방Colors.Gray400,
            )
        }
        if (onDelete != null) {
            IconButton(onClick = { onDelete(cat.id) }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Rounded.Close, null, tint = 거지방Colors.Danger, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun AddCategoryDialog(
    kind: String,
    onSave: (String, String, String) -> Unit,
    onDismiss: () -> Unit,
) {
    val colorOptions = listOf(
        "#5BA89A", "#74BBAE", "#E5896B", "#6BA3D6",
        "#E8B547", "#9C7DD9", "#DC5B5B", "#52B788",
        "#F4845F", "#767D84",
    )

    var name         by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("restaurant") }
    var selectedColor by remember { mutableStateOf(colorOptions.first()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = Shape20, color = Color.White, shadowElevation = 8.dp) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFEDE7F6)),
                    ) { Icon(Icons.Rounded.Category, null, tint = Color(0xFF7B1FA2), modifier = Modifier.size(20.dp)) }
                    Text("카테고리 추가", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }

                // 이름
                거지방TextField(value = name, onValueChange = { name = it }, label = "카테고리 이름")

                // 아이콘 선택
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("아이콘", style = MaterialTheme.typography.labelMedium, color = 거지방Colors.Gray500, fontWeight = FontWeight.SemiBold)
                    categoryIconOptions.chunked(5).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { (iconName, emoji) ->
                                val sel = selectedIcon == iconName
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(if (sel) selectedColor.toComposeColor().copy(alpha = 0.2f) else 거지방Colors.Gray100)
                                        .border(if (sel) 2.dp else 0.dp, selectedColor.toComposeColor(), CircleShape)
                                        .clickable { selectedIcon = iconName },
                                ) {
                                    Text(emoji, fontSize = 20.sp)
                                }
                            }
                            repeat(5 - row.size) { Box(Modifier.size(44.dp)) }
                        }
                    }
                }

                // 색상 선택
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("색상", style = MaterialTheme.typography.labelMedium, color = 거지방Colors.Gray500, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        colorOptions.forEach { hex ->
                            val sel = selectedColor == hex
                            Box(
                                modifier = Modifier
                                    .size(if (sel) 34.dp else 28.dp)
                                    .clip(CircleShape)
                                    .background(hex.toComposeColor())
                                    .border(if (sel) 3.dp else 0.dp, Color.White, CircleShape)
                                    .clickable { selectedColor = hex },
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss, modifier = Modifier.weight(1f), shape = ShapePill,
                        border = BorderStroke(1.dp, 거지방Colors.Gray200),
                    ) { Text("취소", color = 거지방Colors.Gray600) }
                    Button(
                        onClick = { if (name.isNotBlank()) onSave(name, selectedIcon, selectedColor) },
                        enabled = name.isNotBlank(),
                        modifier = Modifier.weight(1f), shape = ShapePill,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2)),
                    ) { Text("추가") }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────
// 방 생성 완료 시트
// ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoomCreatedSheet(room: FriendGroup, onDismiss: () -> Unit) {
    val sheetState    = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val clipboard     = LocalClipboardManager.current
    val context       = LocalContext.current
    var justCopied    by remember { mutableStateOf(false) }

    // 복사 후 1.5초 뒤에 상태 초기화
    LaunchedEffect(justCopied) {
        if (justCopied) {
            kotlinx.coroutines.delay(1500)
            justCopied = false
        }
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
            // 성공 아이콘
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(거지방Colors.Mint100),
            ) {
                Text(room.emoji, fontSize = 34.sp)
            }

            Spacer(Modifier.height(14.dp))

            Text(
                "방이 만들어졌어요!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = 거지방Colors.Gray900,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                room.name,
                style = MaterialTheme.typography.bodyMedium,
                color = 거지방Colors.Gray500,
            )

            Spacer(Modifier.height(28.dp))

            // 초대코드 카드
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = Shape14,
                color = 거지방Colors.Mint50,
                border = BorderStroke(1.5.dp, 거지방Colors.Mint200),
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 20.dp, horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        "초대 코드",
                        style = MaterialTheme.typography.labelSmall,
                        color = 거지방Colors.Mint600,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                    )
                    // 코드를 한 글자씩 칸으로 표시
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        room.inviteCode.forEach { ch ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(Shape10)
                                    .background(Color.White)
                                    .border(1.dp, 거지방Colors.Mint200, Shape10),
                            ) {
                                Text(
                                    ch.toString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = 거지방Colors.Mint700,
                                )
                            }
                        }
                    }
                    Text(
                        "친구에게 이 코드를 알려주세요",
                        style = MaterialTheme.typography.labelSmall,
                        color = 거지방Colors.Mint500,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // 복사 + 공유 버튼 행
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(room.inviteCode))
                        justCopied = true
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = ShapePill,
                    border = BorderStroke(
                        1.5.dp,
                        if (justCopied) 거지방Colors.Mint400 else 거지방Colors.Gray200,
                    ),
                ) {
                    Icon(
                        if (justCopied) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
                        null,
                        tint = if (justCopied) 거지방Colors.Mint500 else 거지방Colors.Gray600,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (justCopied) "복사됨!" else "코드 복사",
                        color = if (justCopied) 거지방Colors.Mint500 else 거지방Colors.Gray700,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "거지방 '${room.name}'에 초대합니다!\n초대 코드: ${room.inviteCode}\n앱에서 '방 참여하기'를 눌러 입력해 주세요 🏠"
                            )
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

            Spacer(Modifier.height(10.dp))

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("닫기", color = 거지방Colors.Gray500, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

// ──────────────────────────────────────────────────────────
// 닉네임 변경 다이얼로그
// ──────────────────────────────────────────────────────────

@Composable
private fun EditNameDialog(current: String, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(current) }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = Shape20, color = Color.White, shadowElevation = 8.dp) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(거지방Colors.Mint100),
                    ) { Icon(Icons.Rounded.Person, null, tint = 거지방Colors.Mint600, modifier = Modifier.size(20.dp)) }
                    Text("닉네임 변경", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                거지방TextField(value = name, onValueChange = { name = it }, label = "새 닉네임")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss, modifier = Modifier.weight(1f), shape = ShapePill,
                        border = BorderStroke(1.dp, 거지방Colors.Gray200),
                    ) { Text("취소", color = 거지방Colors.Gray600) }
                    Button(
                        onClick = { if (name.isNotBlank()) onSave(name) },
                        enabled = name.isNotBlank(),
                        modifier = Modifier.weight(1f), shape = ShapePill,
                        colors = ButtonDefaults.buttonColors(containerColor = 거지방Colors.Mint400),
                    ) { Text("저장") }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────
// 방 만들기 / 참여 다이얼로그
// ──────────────────────────────────────────────────────────

@Composable
private fun CreateRoomDialog(onCreate: (String, String) -> Unit, onDismiss: () -> Unit) {
    var name  by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("🏠") }
    val emojis = listOf("🏠", "🛏️", "🎓", "💼", "🌟", "🎮", "🌈", "🏃")

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = Shape20, color = Color.White, shadowElevation = 8.dp) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(거지방Colors.Mint100),
                    ) { Icon(Icons.Rounded.Add, null, tint = 거지방Colors.Mint600, modifier = Modifier.size(20.dp)) }
                    Text("방 만들기", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                거지방TextField(value = name, onValueChange = { name = it }, label = "방 이름")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("아이콘 선택", style = MaterialTheme.typography.labelMedium, color = 거지방Colors.Gray500, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        emojis.forEach { e ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (emoji == e) 거지방Colors.Mint100 else 거지방Colors.Gray100)
                                    .border(if (emoji == e) 2.dp else 0.dp, 거지방Colors.Mint400, CircleShape)
                                    .clickable { emoji = e },
                            ) { Text(e, fontSize = 18.sp) }
                        }
                    }
                }
                Text("초대 코드는 자동으로 생성됩니다", style = MaterialTheme.typography.bodyMedium, color = 거지방Colors.Gray400)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss, modifier = Modifier.weight(1f), shape = ShapePill,
                        border = BorderStroke(1.dp, 거지방Colors.Gray200),
                    ) { Text("취소", color = 거지방Colors.Gray600) }
                    Button(
                        onClick = { if (name.isNotBlank()) onCreate(name, emoji) },
                        enabled = name.isNotBlank(),
                        modifier = Modifier.weight(1f), shape = ShapePill,
                        colors = ButtonDefaults.buttonColors(containerColor = 거지방Colors.Mint400),
                    ) { Text("만들기") }
                }
            }
        }
    }
}

@Composable
private fun JoinRoomDialog(onJoin: (String) -> Unit, onDismiss: () -> Unit) {
    var code by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = Shape20, color = Color.White, shadowElevation = 8.dp) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(거지방Colors.Mint100),
                    ) { Icon(Icons.Rounded.Login, null, tint = 거지방Colors.Mint600, modifier = Modifier.size(20.dp)) }
                    Text("방 참여하기", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Text("친구에게 초대 코드를 받아 입력해주세요", style = MaterialTheme.typography.bodyMedium, color = 거지방Colors.Gray500)
                거지방TextField(value = code.uppercase(), onValueChange = { code = it.uppercase() }, label = "초대 코드 (6자리)")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss, modifier = Modifier.weight(1f), shape = ShapePill,
                        border = BorderStroke(1.dp, 거지방Colors.Gray200),
                    ) { Text("취소", color = 거지방Colors.Gray600) }
                    Button(
                        onClick = { if (code.length >= 4) onJoin(code) },
                        enabled = code.length >= 4,
                        modifier = Modifier.weight(1f), shape = ShapePill,
                        colors = ButtonDefaults.buttonColors(containerColor = 거지방Colors.Mint400),
                    ) { Text("참여하기") }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────
// 공유 방식 / 과소비 알람 다이얼로그
// ──────────────────────────────────────────────────────────

@Composable
private fun ShareModeDialog(current: String, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = Shape20, color = Color.White, shadowElevation = 8.dp) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFEDE7F6)),
                    ) { Icon(Icons.Rounded.Share, null, tint = Color(0xFF7B1FA2), modifier = Modifier.size(20.dp)) }
                    Text("지출 범위 공유", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Text("랭킹에서 지출을 어떻게 공유할지 선택하세요", style = MaterialTheme.typography.bodyMedium, color = 거지방Colors.Gray500)
                listOf(Triple("percent", "퍼센트", "수입 대비 지출 비율로 표시"), Triple("amount", "금액", "실제 지출 금액을 표시")).forEach { (mode, label, desc) ->
                    val selected = current == mode
                    Surface(
                        shape = Shape14,
                        color = if (selected) 거지방Colors.Mint50 else 거지방Colors.Gray50,
                        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) 거지방Colors.Mint400 else 거지방Colors.Gray200),
                        modifier = Modifier.fillMaxWidth().clickable { onSelect(mode) },
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            RadioButton(selected = selected, onClick = { onSelect(mode) }, colors = RadioButtonDefaults.colors(selectedColor = 거지방Colors.Mint400), modifier = Modifier.size(20.dp))
                            Column {
                                Text(label, fontWeight = FontWeight.Bold, color = 거지방Colors.Gray900)
                                Text(desc, style = MaterialTheme.typography.bodySmall, color = 거지방Colors.Gray500)
                            }
                        }
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("닫기", color = 거지방Colors.Gray500)
                }
            }
        }
    }
}

@Composable
private fun SpendingAlertDialog(
    enabled: Boolean,
    currentAmount: String,
    onSave: (Boolean, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var isEnabled by remember { mutableStateOf(enabled) }
    var amount    by remember { mutableStateOf(currentAmount) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = Shape20, color = Color.White, shadowElevation = 8.dp) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFFFF3E0)),
                    ) { Icon(Icons.Rounded.NotificationsActive, null, tint = Color(0xFFFF8F00), modifier = Modifier.size(20.dp)) }
                    Text("과소비 알람 설정", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Text("설정한 금액을 초과하면 알림을 드려요", style = MaterialTheme.typography.bodyMedium, color = 거지방Colors.Gray500)
                Surface(
                    shape = Shape14,
                    color = if (isEnabled) 거지방Colors.Mint50 else 거지방Colors.Gray50,
                    border = BorderStroke(1.dp, if (isEnabled) 거지방Colors.Mint200 else 거지방Colors.Gray200),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text("알람 활성화", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = if (isEnabled) 거지방Colors.Mint700 else 거지방Colors.Gray700)
                            Text(if (isEnabled) "알람이 켜져 있어요" else "알람이 꺼져 있어요", style = MaterialTheme.typography.bodySmall, color = if (isEnabled) 거지방Colors.Mint500 else 거지방Colors.Gray400)
                        }
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { isEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = 거지방Colors.Mint400, uncheckedThumbColor = Color.White, uncheckedTrackColor = 거지방Colors.Gray300),
                        )
                    }
                }
                if (isEnabled) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("월 지출 한도", style = MaterialTheme.typography.labelMedium, color = 거지방Colors.Gray500, fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 10) amount = it },
                            placeholder = { Text("지출 한도 금액 입력", color = 거지방Colors.Gray400) },
                            trailingIcon = { Text("원", color = 거지방Colors.Gray500, modifier = Modifier.padding(end = 12.dp)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = Shape10,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = 거지방Colors.Mint400, unfocusedBorderColor = 거지방Colors.Gray200),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        )
                        if (amount.isNotBlank()) {
                            Text("월 %,d원 초과 시 알림".format(amount.toLongOrNull() ?: 0L), style = MaterialTheme.typography.bodySmall, color = 거지방Colors.Mint500, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                Button(
                    onClick = { onSave(isEnabled, amount) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = ShapePill,
                    colors = ButtonDefaults.buttonColors(containerColor = 거지방Colors.Mint400),
                ) { Text("저장하기", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold) }
            }
        }
    }
}
