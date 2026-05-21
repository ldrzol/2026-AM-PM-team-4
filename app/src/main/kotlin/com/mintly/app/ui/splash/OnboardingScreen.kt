package com.mintly.app.ui.splash

import androidx.compose.runtime.key
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mintly.app.R
import com.mintly.app.ui.theme.거지방Colors
import com.mintly.app.ui.theme.Pretendard

private data class OnboardingPage(
    val title: String,
    val illustrationRes: Int,
    val subtitle: String,
    val illustrationOffsetY: Dp = 0.dp,   // 이미지 세로 위치 미세 조정
    val illustrationWidth: Float = 0.75f,  // 이미지 가로 크기 비율
)

private val PAGES = listOf(
    OnboardingPage(
        title = "이번 달에 얼마나\n돈을 썼는",
        illustrationRes = R.drawable.onboarding_slide1,
        subtitle = "같이 기록해요",
    ),
    OnboardingPage(
        title = "수입, 소비를 작성하고",
        illustrationRes = R.drawable.onboarding_slide2,
        subtitle = "친구들과 경쟁하세요!",
    ),
    OnboardingPage(
        title = "거기에 꾸밀 수 있는\n나만의 아바타와",
        illustrationRes = R.drawable.onboarding_slide3,
        subtitle = "효율적인 소비를 해요",
        illustrationOffsetY = (-30).dp,  // 3번째 이미지를 위로 올려 맞춤
        illustrationWidth = 0.68f,
    ),
)

private const val TOTAL = 4 // 콘텐츠 3개 + 마지막 로고 1개

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    var page by remember { mutableIntStateOf(0) }
    val isLast = page == TOTAL - 1

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        // ─── 상단 진행 바 ─────────────────────────────────────
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            repeat(TOTAL) { i ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (i == page) 거지방Colors.Mint400
                            else 거지방Colors.MintLight.copy(alpha = 0.3f)
                        ),
                )
            }
        }

        // ─── 페이지 콘텐츠 (전환 효과 없음) ─────────────────────
        Box(modifier = Modifier.fillMaxSize()) {
            key(page) {
                if (page < PAGES.size) ContentPage(PAGES[page])
                else LastPage()
            }
        }

        // ─── 하단 버튼 ────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Button(
                onClick = { if (!isLast) page++ else onFinish() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = 거지방Colors.MintLight),
            ) {
                Text(
                    text = if (!isLast) "다음" else "시작하기",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    color = Color.White,
                )
            }

            // 건너뛰기 자리를 고정 높이로 확보해서 모든 페이지 버튼 위치 통일
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (!isLast) {
                    TextButton(onClick = onFinish) {
                        Text(
                            text = "건너뛰기",
                            fontFamily = Pretendard,
                            fontSize = 14.sp,
                            color = 거지방Colors.Gray400,
                        )
                    }
                }
            }
        }
    }
}

// ── 슬라이드 1~3 ──────────────────────────────────────────────────────
@Composable
private fun ContentPage(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp)
            .padding(top = 130.dp, bottom = 168.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 제목 (상단)
        Text(
            text = page.title,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            color = 거지방Colors.Gray900,
            textAlign = TextAlign.Center,
            lineHeight = 42.sp,
        )

        Spacer(Modifier.weight(1f))

        // 일러스트 (중앙)
        Image(
            painter = painterResource(page.illustrationRes),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth(page.illustrationWidth)
                .aspectRatio(1f)
                .offset(y = page.illustrationOffsetY),
        )

        Spacer(Modifier.height(40.dp))

        // 부제목 (일러스트 아래)
        Text(
            text = page.subtitle,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            color = 거지방Colors.Gray900,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.weight(1f))
    }
}

// ── 마지막 슬라이드: 로고 + 태그라인 ─────────────────────────────────
@Composable
private fun LastPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp)
            .padding(bottom = 168.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // 거지방 로고 (민트 색상 벡터)
        Image(
            painter = painterResource(R.drawable.ic_logo_main),
            contentDescription = "거지방",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth(0.48f)
                .aspectRatio(178f / 61f),
        )

        Spacer(Modifier.height(28.dp))

        // 태그라인
        Text(
            text = "저희와 함께\n가계부를 적어가요",
            fontFamily = Pretendard,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = 거지방Colors.Gray900,
            textAlign = TextAlign.Center,
            lineHeight = 40.sp,
        )
    }
}
