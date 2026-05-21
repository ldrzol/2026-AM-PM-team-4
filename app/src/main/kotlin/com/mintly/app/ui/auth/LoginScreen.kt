package com.mintly.app.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mintly.app.R
import com.mintly.app.ui.theme.거지방Colors
import com.mintly.app.ui.theme.Pretendard

@Composable
fun LoginScreen(
    vm: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onGoSignup: () -> Unit,
) {
    val uiState  by vm.uiState.collectAsStateWithLifecycle()
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPw   by remember { mutableStateOf(false) }
    val focus    = LocalFocusManager.current

    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            kotlinx.coroutines.delay(3000)
            vm.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(80.dp))

        // ─── 로고 ────────────────────────────────────────────
        Image(
            painter = painterResource(R.drawable.ic_logo_main),
            contentDescription = "거지방",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .width(160.dp)
                .aspectRatio(178f / 61f),
        )

        Spacer(Modifier.height(72.dp))

        // ─── 이메일 ───────────────────────────────────────────
        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = "이메일",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
        )

        Spacer(Modifier.height(12.dp))

        // ─── 비밀번호 ─────────────────────────────────────────
        AuthTextField(
            value = password,
            onValueChange = { password = it },
            label = "비밀번호",
            visualTransformation = if (showPw) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = {
                focus.clearFocus()
                vm.login(email, password, onLoginSuccess)
            }),
            trailingIcon = {
                IconButton(onClick = { showPw = !showPw }) {
                    Icon(
                        if (showPw) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        contentDescription = null,
                        tint = 거지방Colors.Gray400,
                    )
                }
            },
        )

        // ─── 에러 메시지 ──────────────────────────────────────
        if (uiState.error != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = uiState.error!!,
                fontFamily = Pretendard,
                fontSize = 13.sp,
                color = 거지방Colors.Danger,
            )
        }

        Spacer(Modifier.height(40.dp))

        // ─── 로그인 버튼 ───────────────────────────────────────
        Button(
            onClick = { vm.login(email, password, onLoginSuccess) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = 거지방Colors.MintLight),
            enabled = !uiState.isLoading,
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    "로그인",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.White,
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // ─── 회원가입 링크 ────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "아직 계정이 없으신가요?",
                fontFamily = Pretendard,
                fontSize = 14.sp,
                color = 거지방Colors.Gray500,
            )
            TextButton(onClick = onGoSignup, contentPadding = PaddingValues(horizontal = 6.dp)) {
                Text(
                    "회원가입",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = 거지방Colors.Mint400,
                )
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}

// ── 공용 입력 필드 (로그인/회원가입) ──────────────────────────────────
@Composable
internal fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                label,
                fontFamily = Pretendard,
                fontSize = 14.sp,
            )
        },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        trailingIcon = trailingIcon,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = 거지방Colors.Mint400,
            unfocusedBorderColor = 거지방Colors.Gray200,
            focusedLabelColor    = 거지방Colors.Mint400,
            unfocusedLabelColor  = 거지방Colors.Gray400,
        ),
    )
}
