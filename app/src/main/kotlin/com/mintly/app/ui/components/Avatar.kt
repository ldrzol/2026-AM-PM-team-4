package com.mintly.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

// ─── 아바타 얼굴 설정 ─────────────────────────────────────
data class AvatarFace(
    val frontHair: String  = "fh_basic",
    val backHair:  String  = "bh_basic",
    val eye:       String  = "basic",
    val eyebrow:   String  = "basic",
    val nose:      String  = "n_basic",
    val mouth:     String  = "basic_smile",
    val glasses:   String? = null,   // null = 안경 없음
)

// ─── 바디 팔레트 ───────────────────────────────────────────
private data class BodyPalette(val body: Color, val shade: Color)

private val BODY_PALETTES = mapOf(
    "mint"   to BodyPalette(Color(0xFF74BBAE), Color(0xFF5BA89A)),
    "peach"  to BodyPalette(Color(0xFFE5896B), Color(0xFFC46E51)),
    "blue"   to BodyPalette(Color(0xFF6BA3D6), Color(0xFF4A85BB)),
    "purple" to BodyPalette(Color(0xFF9C7DD9), Color(0xFF7E5FBE)),
    "yellow" to BodyPalette(Color(0xFFE8B547), Color(0xFFC8961F)),
)

private val SKIN_LIGHT = Color(0xFFFFF7EC)
private val SKIN_MID   = Color(0xFFFFEED8)
private val SKIN_DEEP  = Color(0xFFFFE8CA)
private val HAIR_COLOR = Color(0xFF663C13)
private val BROW_COLOR = Color(0xFF341C06)
private val FEATURE    = Color(0xFF1F2A28)

// ─── 기본형.svg 변환 상수 (SVG 102×187 → Canvas 120×150) ──
// SVG 얼굴 중심 (51,60) → Canvas 얼굴 중심 (hCx=60, hCy=48)
private const val BASIC_SCALE = 0.75f
private const val BASIC_TX    = 21.75f   // 60 - 51 * 0.75
private const val BASIC_TY    = 3.0f     // 48 - 60 * 0.75

// ─── SVG 경로 파서 헬퍼 ───────────────────────────────────
private fun parsePath(d: String): Path = PathParser().parsePathString(d).toPath()

// ─── 앞머리 경로 (100×100 캔버스 또는 102×187 SVG 좌표계) ─
private val FRONT_HAIR_PATHS: Map<String, Path> by lazy {
    mapOf(
        // ── 기본형.svg 앞머리 (102×187 SVG 좌표계, BASIC_SCALE 적용) ──
        "fh_basic" to parsePath("M50.8402 0C-5.90934 0-2.82193 49.3333 5.81547 74L8.49649 73.3971L12.1645 49.0831L17.8823 57.1877L25.3262 39.5736L33.3096 52.8653L42.0481 36.0076L50.5709 52.8653L58.77 34.3866L69.0189 52.8653L76.247 39.5736L83.5831 57.8361L89.0851 49.9476L92.4318 74H94.7623C103.767 49.3333 107.59 0 50.8402 0Z"),
        // ── 기존 앞머리 (100×100 좌표계, hairScale 적용) ──
        "fh3"  to parsePath("M 100 96 L 90.7 96 C 92.1 75.3 86 33.9 50.6 33.9 C 15.2 33.9 8.3 73.9 9.3 93.9 L 0 93.9 C 0 30.6 20.4 8.9 30.7 6 C 29.2 13.9 32 16.4 33.6 16.7 C 34.6 13.5 39.5 6.1 41.8 0 C 45.2 4.4 49.2 9.6 50.6 11.6 C 52.3 6.7 56 1.8 57.7 0 C 58.8 0.7 64.4 9.6 67.0 13.9 C 68.1 9.9 71 5.9 72.3 4.3 C 94.7 33.9 100 76 100 96 Z"),
        "fh5"  to parsePath("M 14 86 L 0 86 L 9 47 L 2 47 C 5 35 17 17 23 11 L 17 11 C 20 7 30 0 51 1 C 71 2 81 9 84 13 L 78 13 C 87 24 95 42 98 50 L 91 50 C 94 56 99 76 101 86 L 82 86 L 80 58 L 77 73 L 63 37 L 36 67 L 36 47 L 32 47 L 14 86 Z"),
        "fh9"  to parsePath("M 50 0 C 88 0 95 50 80 95 L 73 47 L 60 28 C 55 33 35 49 30 53 C 29 50 34 38 39 31 L 25 50 L 18 95 C 1 50 12 0 50 0 Z"),
        "fh13" to parsePath("M 0 75 C 25 75 55 40 65 25 C 82 30 88 65 89 80 C 95 84 102 81 105 80 C 104 53 92 0 51 0 C 11 0 1 50 0 75 Z"),
        "fh30" to parsePath("M 28 47 C 26 47 13 78 6 92 C 6 84 0 71 0 47 C 0 22 27 0 48 0 C 64 0 73 9 76 14 C 84 18 100 32 100 53 C 100 73 94 85 91 89 C 92 40 80 25 73 23 C 67 35 41 60 28 72 C 29 65 28 53 28 47 Z"),
        "fh43" to parsePath("M 16 55 L 16 85 C 16 86 15 87 14 87 L 12 87 C 11 87 11 87 10 86 L 0 65 C -0 64 0 63 1 63 L 7 57 C 7 56 7 56 7 55 L 0 38 C -0 37 0 36 1 35 L 16 26 C 16 26 17 25 17 24 L 20 6 C 20 5 21 5 22 5 L 40 9 C 40 9 41 9 41 9 L 50 1 C 51 0 52 1 53 1 L 60 9 C 60 9 61 9 61 9 L 80 5 C 81 5 82 5 82 6 L 85 24 C 85 25 85 25 86 26 L 100 35 C 101 36 101 37 100 38 L 94 56 C 94 56 94 57 94 58 L 99 63 C 100 64 100 65 99 66 C 93 71 90 80 89 84 C 89 85 88 87 87 87 L 86 87 C 85 87 84 86 84 85 L 84 54 C 84 53 84 53 83 53 C 76 48 69 38 67 33 C 67 32 67 32 66 31 C 50 27 38 30 33 31 C 33 31 32 32 32 32 C 25 44 19 50 16 53 C 16 53 16 54 16 55 Z"),
    )
}

// ─── 뒷머리 경로 ───────────────────────────────────────────
private val BACK_HAIR_PATHS: Map<String, Path> by lazy {
    mapOf(
        // ── 기본형.svg 뒷머리 (102×187 SVG 좌표계, BASIC_SCALE 적용) ──
        "bh_basic" to parsePath("M7.91598 57.7331V49.116L90.624 48L94.8027 49.116C96.0707 64.0525 99.4616 68.6483 100.999 69.0791C101.143 73.244 90.624 77.409 90.624 79.7069V85.3081C88.895 96.9126 84.14 103.069 81.9786 104.697L77.2237 96.654C76.9931 99.6413 69.827 105.463 66.2728 108C65.4659 105.472 61.2296 101.968 59.2124 100.532C57.0222 102.945 53.1126 106.516 51.4315 108C50.3941 108 45.0435 103.021 42.4979 100.532C41.3452 100.876 38.2713 105.654 36.8784 108C34.3424 106.851 27.6566 98.7126 24.6307 94.787C24.1697 96.8551 21.3647 102.255 20.0199 104.697C17.0228 102.744 13.1996 94.4997 11.6626 90.622L14.2563 82.1484C7.41029 78.507 1 69.0791 1 69.0791C4.99211 65.3808 6.75785 63.0392 7.91598 57.7331Z"),
        // ── 기존 뒷머리 (100×100 좌표계, hairScale 적용) ──
        "bh5"  to parsePath("M 5 95 L 0 29 L 48 0 L 100 29 L 91 95 L 76 78 L 48 100 L 19 78 L 5 95 Z"),
        "bh9"  to Path().apply {
            addPath(parsePath("M 33 25 C 33 5 13 0 2 0 L 0 27 L 21 64 C 25 60 33 45 33 25 Z"))
            addPath(parsePath("M 67 25 C 67 5 87 0 98 0 L 100 27 L 79 64 C 75 60 67 45 67 25 Z"))
        },
        "bh31" to parsePath("M 0 86 C 10 63 12 22 11 4 L 100 0 C 98 36 104 73 108 86 C 99 85 92 81 90 79 C 89 85 91 93 92 96 C 81 94 74 84 72 79 C 70 89 58 98 52 102 C 45 98 39 85 36 79 C 34 87 21 94 15 96 C 16 94 17 84 17 79 C 13 82 4 85 0 86 Z"),
        "bh43" to parsePath("M 6 67 C 1 60 0 44 0 37 C 18 -26 103 6 106 30 C 108 50 104 63 101 67 L 95 64 C 93 81 80 93 73 96 L 65 88 C 63 91 56 97 53 100 C 51 99 45 92 42 88 L 34 96 C 20 90 13 73 11 64 L 6 67 Z"),
    )
}

// ─── 기본형 눈 경로 (102×187 SVG 좌표계) ──────────────────
private val BASIC_EYE_PATHS: Map<String, Path> by lazy {
    mapOf(
        // 오른쪽 눈: 눈구멍 원 + 위 눈꺼풀 아치
        "right" to parsePath("M69.163 57.0474C71.6482 57.0475 73.663 59.0621 73.663 61.5474C73.6628 64.0324 71.6481 66.0473 69.163 66.0474C66.6778 66.0474 64.6632 64.0325 64.663 61.5474C64.663 59.0621 66.6777 57.0474 69.163 57.0474ZM66.2558 53.1157C70.9984 52.2209 76.4574 53.6006 81.7987 59.1821L78.8886 61.9673C74.3596 57.2346 70.2317 56.4646 67.0028 57.0737C63.6153 57.713 60.8292 59.9467 59.3339 61.8276L57.7567 60.5747L56.1796 59.3208C58.1005 56.9043 61.6723 53.9807 66.2558 53.1157Z"),
        // 왼쪽 눈: 눈구멍 원 + 위 눈꺼풀 아치
        "left"  to parsePath("M31.837 57.0474C29.3518 57.0475 27.337 59.0621 27.337 61.5474C27.3372 64.0324 29.3519 66.0473 31.837 66.0474C34.3222 66.0474 36.3368 64.0325 36.337 61.5474C36.337 59.0621 34.3223 57.0474 31.837 57.0474ZM34.7442 53.1157C30.0016 52.2209 24.5426 53.6006 19.2013 59.1821L22.1114 61.9673C26.6404 57.2346 30.7683 56.4646 33.9972 57.0737C37.3847 57.713 40.1708 59.9467 41.6661 61.8276L43.2433 60.5747L44.8204 59.3208C42.8995 56.9043 39.3277 53.9807 34.7442 53.1157Z"),
    )
}

// ─── 눈썹 경로 ─────────────────────────────────────────────
private val EYEBROW_PATHS: Map<String, Path> by lazy {
    mapOf(
        // ── 기본형.svg 눈썹 (102×187 SVG 좌표계) ──
        "basic_right" to parsePath("M56.5659 50.5909L54.8605 47.5841C70.8686 38.3696 82.3832 41.471 86.1394 44.1735L85.1522 45.071C77.936 39.6858 63.0879 46.5071 56.5659 50.5909Z"),
        "basic_left"  to parsePath("M45.4341 50.5909L47.1395 47.5841C31.1314 38.3696 19.6168 41.471 15.8606 44.1735L16.8478 45.071C24.064 39.6858 38.9121 46.5071 45.4341 50.5909Z"),
        // ── 기존 눈썹 ──
        "b1"  to Path().apply {
            addPath(parsePath("M 0 8 L 0 6 L 15 0 L 17 4 L 0 8 Z"))
            addPath(parsePath("M 38 8 L 38 6 L 23 0 L 21 4 L 38 8 Z"))
        },
        "b10" to Path().apply {
            // 곡선 눈썹 - stroke용
        },
        "b15" to Path().apply {
            addPath(parsePath("M 0 2 L 16 2 L 16 7 L 0 7 Z"))
            addPath(parsePath("M 22 2 L 38 2 L 38 7 L 22 7 Z"))
        },
    )
}

// ─── 코 경로 ───────────────────────────────────────────────
private val NOSE_PATHS: Map<String, Path> by lazy {
    mapOf(
        // ── 기본형.svg 코 (102×187 SVG 좌표계) ──
        "n_basic" to parsePath("M52.7627 66.271C52.782 68.3198 52.3359 69.9599 51.8389 71.1655C51.5912 71.7663 51.333 72.255 51.1202 72.6245C51.0146 72.8078 50.9195 72.9622 50.8487 73.0776C50.812 73.1374 50.7856 73.1809 50.7637 73.2173C50.7464 73.2462 50.7381 73.2601 50.7364 73.2632C50.2019 74.265 48.9473 75.1675 49.2666 77.5093C49.3187 77.8899 49.6322 78.3258 50.5655 78.7104C51.463 79.0803 52.5584 79.2272 53.2764 79.2368L53.2549 80.7915L53.2344 82.3462C52.2459 82.333 50.7391 82.145 49.3809 81.5854C48.0589 81.0407 46.4614 79.9508 46.1856 77.9292C45.9312 76.063 46.3595 74.5801 46.8975 73.4985C47.1622 72.9665 47.4492 72.5402 47.6739 72.228C47.7761 72.086 47.8913 71.9316 47.9454 71.8579C48.0281 71.7453 48.0188 71.7505 47.9922 71.8003C48.3388 71.1504 49.6818 69.4316 49.6524 66.3003L51.2081 66.2856L52.7627 66.271Z"),
        // ── 기존 코 ──
        "n1" to parsePath("M 4 0 C 4 1.5 3.7 2.7 3.4 3.6 C 3 4.6 2 5.2 2 7 C 2 8 3 8.5 4.5 8.7 L 4.5 11 C 2.5 10.9 0 10.2 0 7.6 C 0 6.5 0.3 5.6 0.7 4.8 C 1.4 3.5 2 2.3 2 0 Z"),
        "n3" to Path(), // 두 콧구멍 - 원으로 처리
    )
}

// ─── 입 경로 ───────────────────────────────────────────────
private val MOUTH_PATHS: Map<String, Path> by lazy {
    mapOf(
        // ── 기본형.svg 입 (이빨 미소, 102×187 SVG 좌표계) ──
        "basic_smile" to parsePath("M40.8042 88C40.8156 88.0127 40.9502 88.1761 41.3218 88.4268C41.7047 88.685 42.283 88.9964 43.0981 89.2969C44.7279 89.8976 47.2672 90.4395 50.9829 90.4395C54.732 90.4394 57.2874 89.9882 58.9272 89.46C59.747 89.1958 60.3149 88.9196 60.6821 88.6885C61.0727 88.4426 61.154 88.3005 61.1255 88.3477L62.563 89.2178L64.0005 90.0869C63.6559 90.6567 63.0993 91.1374 62.4722 91.5322C61.82 91.9427 60.9849 92.3272 59.9575 92.6582C57.9023 93.3203 54.983 93.7998 50.9829 93.7998C46.9489 93.7998 44.0055 93.2125 41.937 92.4502C39.942 91.7149 38.594 90.7511 37.9995 89.8496L40.8042 88Z"),
        // ── 기존 입 ──
        "m2"  to parsePath("M -6 0 C -5 1 -3 2 0 2 C 3 2 5 1 6 0 L 7 2 C 5 3 3 4 0 4 C -3 4 -5 3 -7 2 Z"),
        "m17" to parsePath("M -8 0 L -7 2 L -2 1 L -1 2 L 1 2 L 2 1 L 7 2 L 8 0"),
        "m22" to parsePath("M -7 0 C -3 0 4 0 7 -2"),
        "m26" to parsePath("M -8 0 C -6 4 -3 6 0 6 C 3 6 6 4 8 0 C 4 1 -4 1 -8 0 Z"),
        "line" to parsePath("M -5 0 L 5 0"),
    )
}

// ─── 메인 Avatar Composable ───────────────────────────────
@Composable
fun 거지방Avatar(
    modifier: Modifier = Modifier,
    color: String = "mint",
    face: AvatarFace = AvatarFace(),
    hat: String? = null,
    outfit: String? = null,
    forced: Boolean = false,
    size: Dp = 120.dp,
) {
    val pal = remember(color) { BODY_PALETTES[color] ?: BODY_PALETTES["mint"]!! }
    val effectiveOutfit = if (forced) "beggar" else outfit?.removePrefix("outfit_")

    Canvas(
        modifier = modifier.size(size, size * 150f / 120f),
    ) {
        val W = 120f
        val H = 150f
        val sx = this.size.width  / W
        val sy = this.size.height / H

        withTransform({ scale(sx, sy, pivot = Offset.Zero) }) {
            drawAvatarContent(face, pal, hat, effectiveOutfit)
        }
    }
}

private fun DrawScope.drawAvatarContent(
    face: AvatarFace,
    pal: BodyPalette,
    hat: String?,
    outfit: String?,
) {
    val hCx = 60f; val hCy = 48f; val hRx = 32f; val hRy = 38f
    val hairScale = 0.72f
    val hairTx = hCx - 50f * hairScale
    val hairTy = hCy - 38f - 4f

    // 그림자
    drawOval(Color(0x1E142E2A), topLeft = Offset(hCx - 26, 141f), size = Size(52f, 6f))

    // 뒷머리 (기본형 경로는 BASIC_SCALE, 기존 경로는 hairScale 적용)
    BACK_HAIR_PATHS[face.backHair]?.let { path ->
        if (face.backHair == "bh_basic") {
            withTransform({
                translate(BASIC_TX, BASIC_TY)
                scale(BASIC_SCALE, BASIC_SCALE, pivot = Offset.Zero)
            }) { drawPath(path, HAIR_COLOR) }
        } else {
            withTransform({
                translate(hairTx, hairTy)
                scale(hairScale, hairScale, pivot = Offset.Zero)
            }) { drawPath(path, HAIR_COLOR) }
        }
    }

    // 몸통
    val bodyPath = Path().apply {
        moveTo(hCx - 24, 130f)
        cubicTo(hCx - 30, 110f, hCx - 18, 92f, hCx - 14, 86f)
        lineTo(hCx + 14, 86f)
        cubicTo(hCx + 18, 92f, hCx + 30, 110f, hCx + 24, 130f)
        close()
    }
    drawPath(bodyPath, pal.body)

    // 팔
    withTransform({ rotate(-12f, pivot = Offset(hCx - 28, 108f)) }) {
        drawOval(pal.body, topLeft = Offset(hCx - 34, 97f), size = Size(12f, 22f))
    }
    withTransform({ rotate(12f, pivot = Offset(hCx + 28, 108f)) }) {
        drawOval(pal.body, topLeft = Offset(hCx + 22, 97f), size = Size(12f, 22f))
    }

    // 옷 (후드, 정장, 거지옷 등)
    when (outfit) {
        "beggar" -> {
            val ragPath = parsePath("M 0 4 L 6 36 L 12 26 L 17 40 L 22 28 L 28 42 L 34 28 L 40 38 L 44 4 Z")
            withTransform({ translate(hCx - 22, 88f) }) {
                drawPath(ragPath, Color(0xEB6B5740))
            }
        }
        "hoodie" -> {
            // 후드티: 원형 목+어깨
            val hoodPath = Path().apply {
                moveTo(hCx - 22, 88f)
                cubicTo(hCx - 26, 100f, hCx - 30, 115f, hCx - 24, 130f)
                lineTo(hCx + 24, 130f)
                cubicTo(hCx + 30, 115f, hCx + 26, 100f, hCx + 22, 88f)
                close()
            }
            drawPath(hoodPath, Color(0xCC5588BB))
        }
        "suit" -> {
            val suitPath = Path().apply {
                moveTo(hCx - 22, 88f)
                lineTo(hCx - 14, 100f)
                lineTo(hCx, 96f)
                lineTo(hCx + 14, 100f)
                lineTo(hCx + 22, 88f)
                cubicTo(hCx + 26, 100f, hCx + 28, 115f, hCx + 24, 130f)
                lineTo(hCx - 24, 130f)
                cubicTo(hCx - 28, 115f, hCx - 26, 100f, hCx - 22, 88f)
            }
            drawPath(suitPath, Color(0xFF3A3E43))
            // 넥타이
            val tiePath = Path().apply {
                moveTo(hCx - 3, 90f); lineTo(hCx + 3, 90f)
                lineTo(hCx + 5, 108f); lineTo(hCx, 115f); lineTo(hCx - 5, 108f); close()
            }
            drawPath(tiePath, Color(0xFFDC5B5B))
        }
        "sports" -> {
            // 스포츠웨어: 밝은 색 민소매
            val sportPath = Path().apply {
                moveTo(hCx - 22, 88f)
                cubicTo(hCx - 26, 100f, hCx - 26, 115f, hCx - 22, 130f)
                lineTo(hCx + 22, 130f)
                cubicTo(hCx + 26, 115f, hCx + 26, 100f, hCx + 22, 88f)
                close()
            }
            drawPath(sportPath, Color(0xFF6BA3D6))
            // 줄무늬
            for (i in 0 until 3) {
                drawRect(
                    color = Color.White.copy(alpha = 0.3f),
                    topLeft = Offset(hCx - 10 + i * 8, 92f),
                    size = Size(3f, 36f),
                )
            }
        }
        "hanbok" -> {
            // 한복: 연한 분홍 저고리
            val hanbokPath = Path().apply {
                moveTo(hCx - 22, 88f)
                lineTo(hCx - 18, 88f)
                cubicTo(hCx - 16, 94f, hCx - 10, 98f, hCx, 96f)
                cubicTo(hCx + 10, 98f, hCx + 16, 94f, hCx + 18, 88f)
                lineTo(hCx + 22, 88f)
                cubicTo(hCx + 28, 100f, hCx + 28, 115f, hCx + 22, 130f)
                lineTo(hCx - 22, 130f)
                cubicTo(hCx - 28, 115f, hCx - 28, 100f, hCx - 22, 88f)
            }
            drawPath(hanbokPath, Color(0xFFFFC8D4))
            // 동정 (흰 깃)
            val collar = Path().apply {
                moveTo(hCx - 6, 88f); lineTo(hCx, 100f); lineTo(hCx + 6, 88f)
            }
            drawPath(collar, Color.White, style = Stroke(width = 3f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
        }
    }

    // 머리
    val skinBrush = Brush.radialGradient(
        colorStops = arrayOf(0.21f to SKIN_LIGHT, 0.65f to SKIN_MID, 1.0f to SKIN_DEEP),
        center = Offset(hCx, hCy - 5f),
        radius = hRx * 1.6f,
    )
    drawOval(brush = skinBrush, topLeft = Offset(hCx - hRx, hCy - hRy), size = Size(hRx * 2, hRy * 2))

    // 앞머리 (기본형 경로는 BASIC_SCALE, 기존 경로는 hairScale 적용)
    FRONT_HAIR_PATHS[face.frontHair]?.let { path ->
        if (face.frontHair == "fh_basic") {
            withTransform({
                translate(BASIC_TX, BASIC_TY)
                scale(BASIC_SCALE, BASIC_SCALE, pivot = Offset.Zero)
            }) { drawPath(path, HAIR_COLOR) }
        } else {
            withTransform({
                translate(hairTx, hairTy)
                scale(hairScale, hairScale, pivot = Offset.Zero)
            }) { drawPath(path, HAIR_COLOR) }
        }
    }

    // 눈썹
    drawEyebrow(face.eyebrow, hCx - 19, hCy - 14f)

    // 눈
    drawEyes(face.eye, hCx - 19, hCy - 6f)

    // 안경 (눈 위에 덧그림)
    face.glasses?.let { drawGlasses(it, hCx, hCy - 6f) }

    // 코
    drawNose(face.nose, hCx, hCy + 4f)

    // 입
    drawMouth(face.mouth, hCx, hCy + 18f)

    // 모자
    drawHat(hat?.removePrefix("hat_"), hCx, hCy - hRy + 4f)
}

private fun DrawScope.drawEyebrow(style: String, x: Float, y: Float) {
    when (style) {
        // ── 기본형.svg 눈썹 ──────────────────────────────────
        "basic" -> {
            withTransform({
                translate(BASIC_TX, BASIC_TY)
                scale(BASIC_SCALE, BASIC_SCALE, pivot = Offset.Zero)
            }) {
                EYEBROW_PATHS["basic_right"]?.let { drawPath(it, BROW_COLOR) }
                EYEBROW_PATHS["basic_left"]?.let  { drawPath(it, BROW_COLOR) }
            }
        }
        // ── 기존 눈썹 ────────────────────────────────────────
        "b1" -> {
            val p = Path().apply {
                moveTo(x, y + 8); lineTo(x, y + 6); lineTo(x + 15, y); lineTo(x + 17, y + 4); close()
                moveTo(x + 38, y + 8); lineTo(x + 38, y + 6); lineTo(x + 23, y); lineTo(x + 21, y + 4); close()
            }
            drawPath(p, BROW_COLOR)
        }
        "b10" -> {
            // 곡선
            val p1 = Path().apply { moveTo(x, y + 4); cubicTo(x + 4, y - 1, x + 12, y - 1, x + 16, y + 4) }
            val p2 = Path().apply { moveTo(x + 22, y + 4); cubicTo(x + 26, y - 1, x + 30, y - 1, x + 38, y + 4) }
            drawPath(p1, BROW_COLOR, style = Stroke(width = 2.5f))
            drawPath(p2, BROW_COLOR, style = Stroke(width = 2.5f))
        }
        "b15" -> {
            // 두꺼운 직사각형
            drawRect(BROW_COLOR, topLeft = Offset(x, y + 2), size = Size(16f, 5f), alpha = 1f)
            drawRect(BROW_COLOR, topLeft = Offset(x + 22, y + 2), size = Size(16f, 5f), alpha = 1f)
        }
        "b21" -> {
            // 점선
            val dots = listOf(0, 5, 10, 15, 22, 27, 32, 37)
            dots.forEachIndexed { i, dx ->
                drawCircle(BROW_COLOR, radius = 1.2f, center = Offset(x + dx, y + 4 + (i % 2)))
            }
        }
    }
}

private fun DrawScope.drawEyes(style: String, x: Float, y: Float) {
    when (style) {
        // ── 기본형.svg 눈 (눈구멍 윤곽 + 눈꺼풀 아치 + 동공) ──
        "basic" -> {
            withTransform({
                translate(BASIC_TX, BASIC_TY)
                scale(BASIC_SCALE, BASIC_SCALE, pivot = Offset.Zero)
            }) {
                // 오른쪽 눈 윤곽 + 아치
                BASIC_EYE_PATHS["right"]?.let { drawPath(it, FEATURE) }
                // 오른쪽 동공 (r=5.63518)
                drawCircle(FEATURE, radius = 5.63518f, center = Offset(69.0498f, 61.1293f))
                // 왼쪽 눈 윤곽 + 아치
                BASIC_EYE_PATHS["left"]?.let  { drawPath(it, FEATURE) }
                // 왼쪽 동공 (matrix(-1 0 0 1 37.5854 55.4941) 변환)
                drawCircle(FEATURE, radius = 5.63518f, center = Offset(31.95f, 61.129f))
            }
        }
        // ── 기존 눈 ─────────────────────────────────────────
        "e1" -> {
            // 기본 눈 (원 + 눈꺼풀)
            drawCircle(FEATURE, radius = 3.5f, center = Offset(x + 8, y + 8.5f))
            drawCircle(FEATURE, radius = 3.5f, center = Offset(x + 30, y + 8.5f))
        }
        "e28" -> {
            // 활짝 눈 (외곽)
            val left = Path().apply {
                moveTo(x + 6.5f, y); cubicTo(x + 9.5f, y, x + 13.5f, y + 1.5f, x + 13.5f, y + 5f)
                cubicTo(x + 13.5f, y + 8.5f, x + 10.5f, y + 12f, x + 6.5f, y + 12f)
                cubicTo(x + 2.5f, y + 12f, x, y + 8.5f, x, y + 5f)
                cubicTo(x, y + 1.5f, x + 3.5f, y, x + 6.5f, y); close()
            }
            val right = Path().apply {
                val rx = x + 22f
                moveTo(rx + 6.5f, y); cubicTo(rx + 9.5f, y, rx + 13.5f, y + 1.5f, rx + 13.5f, y + 5f)
                cubicTo(rx + 13.5f, y + 8.5f, rx + 10.5f, y + 12f, rx + 6.5f, y + 12f)
                cubicTo(rx + 2.5f, y + 12f, rx, y + 8.5f, rx, y + 5f)
                cubicTo(rx, y + 1.5f, rx + 3.5f, y, rx + 6.5f, y); close()
            }
            drawPath(left,  FEATURE, style = Stroke(1.5f))
            drawPath(right, FEATURE, style = Stroke(1.5f))
            drawCircle(FEATURE, 2f, Offset(x + 6.5f, y + 5f))
            drawCircle(FEATURE, 2f, Offset(x + 28.5f, y + 5f))
        }
        "dots" -> {
            drawCircle(FEATURE, radius = 2.5f, center = Offset(x + 8, y + 8))
            drawCircle(FEATURE, radius = 2.5f, center = Offset(x + 30, y + 8))
        }
        "arc" -> {
            val p1 = Path().apply { moveTo(x + 1, y + 9); cubicTo(x + 4, y + 4, x + 11, y + 4, x + 15, y + 9) }
            val p2 = Path().apply { moveTo(x + 23, y + 9); cubicTo(x + 26, y + 4, x + 33, y + 4, x + 37, y + 9) }
            drawPath(p1, FEATURE, style = Stroke(width = 2f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
            drawPath(p2, FEATURE, style = Stroke(width = 2f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
        }
        else -> {
            drawCircle(FEATURE, radius = 3.5f, center = Offset(x + 8, y + 8.5f))
            drawCircle(FEATURE, radius = 3.5f, center = Offset(x + 30, y + 8.5f))
        }
    }
}

private fun DrawScope.drawNose(style: String, x: Float, y: Float) {
    when (style) {
        // ── 기본형.svg 코 ────────────────────────────────────
        "n_basic" -> {
            withTransform({
                translate(BASIC_TX, BASIC_TY)
                scale(BASIC_SCALE, BASIC_SCALE, pivot = Offset.Zero)
            }) {
                NOSE_PATHS["n_basic"]?.let { drawPath(it, FEATURE) }
            }
        }
        // ── 기존 코 ─────────────────────────────────────────
        "n1" -> {
            NOSE_PATHS["n1"]?.let { path ->
                withTransform({ translate(x, y) }) { drawPath(path, FEATURE) }
            }
        }
        "n3" -> {
            drawOval(FEATURE, topLeft = Offset(x - 3.5f, y + 2), size = Size(3f, 4f))
            drawOval(FEATURE, topLeft = Offset(x + 0.5f, y + 2), size = Size(3f, 4f))
        }
        "n10" -> {
            val p = Path().apply {
                moveTo(x - 3, y); lineTo(x - 3, y + 5)
                cubicTo(x - 3, y + 6, x - 1, y + 7, x, y + 7)
                cubicTo(x + 1, y + 7, x + 3, y + 6, x + 3, y + 5)
                lineTo(x + 3, y)
            }
            drawPath(p, FEATURE, style = Stroke(1f))
        }
    }
}

private fun DrawScope.drawMouth(style: String, x: Float, y: Float) {
    when (style) {
        // ── 기본형.svg 입 (이빨 미소) ─────────────────────────
        "basic_smile" -> {
            withTransform({
                translate(BASIC_TX, BASIC_TY)
                scale(BASIC_SCALE, BASIC_SCALE, pivot = Offset.Zero)
            }) {
                MOUTH_PATHS["basic_smile"]?.let { drawPath(it, FEATURE) }
            }
        }
        // ── 기존 입 ─────────────────────────────────────────
        "m2" -> MOUTH_PATHS["m2"]?.let { p -> withTransform({ translate(x, y) }) { drawPath(p, FEATURE) } }
        "m17" -> {
            val p = Path().apply {
                moveTo(x - 8, y); lineTo(x - 7, y + 2); lineTo(x - 2, y + 1); lineTo(x - 1, y + 2)
                lineTo(x + 1, y + 2); lineTo(x + 2, y + 1); lineTo(x + 7, y + 2); lineTo(x + 8, y)
            }
            drawPath(p, FEATURE, style = Stroke(width = 1.5f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
        }
        "m22" -> {
            val p = Path().apply { moveTo(x - 7, y); cubicTo(x - 3, y, x + 4, y, x + 7, y - 2) }
            drawPath(p, FEATURE, style = Stroke(width = 2f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
        }
        "m26" -> MOUTH_PATHS["m26"]?.let { p -> withTransform({ translate(x, y) }) { drawPath(p, FEATURE) } }
        "line" -> {
            val p = Path().apply { moveTo(x - 5, y); lineTo(x + 5, y) }
            drawPath(p, FEATURE, style = Stroke(width = 2f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
        }
        else  -> MOUTH_PATHS["m2"]?.let { p -> withTransform({ translate(x, y) }) { drawPath(p, FEATURE) } }
    }
}

// ─── 안경 ─────────────────────────────────────────────────
private fun DrawScope.drawGlasses(style: String, cx: Float, y: Float) {
    val frameColor = Color(0xFF2C2C2C)
    val frameStroke = Stroke(width = 2.2f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
    when (style) {
        "round" -> {
            // 둥근 안경
            val path = Path().apply {
                // 왼쪽 프레임
                addOval(androidx.compose.ui.geometry.Rect(cx - 19f, y, cx - 5f, y + 14f))
                // 오른쪽 프레임
                addOval(androidx.compose.ui.geometry.Rect(cx + 5f, y, cx + 19f, y + 14f))
                // 브리지 (코 연결)
                moveTo(cx - 5f, y + 7f); lineTo(cx + 5f, y + 7f)
                // 왼쪽 안경다리
                moveTo(cx - 19f, y + 7f); lineTo(cx - 24f, y + 6f)
                // 오른쪽 안경다리
                moveTo(cx + 19f, y + 7f); lineTo(cx + 24f, y + 6f)
            }
            drawPath(path, frameColor, style = frameStroke)
        }
        "square" -> {
            // 각진 안경
            val path = Path().apply {
                // 왼쪽 프레임
                addRect(androidx.compose.ui.geometry.Rect(cx - 19f, y, cx - 5f, y + 13f))
                // 오른쪽 프레임
                addRect(androidx.compose.ui.geometry.Rect(cx + 5f, y, cx + 19f, y + 13f))
                // 브리지
                moveTo(cx - 5f, y + 6.5f); lineTo(cx + 5f, y + 6.5f)
                // 안경다리
                moveTo(cx - 19f, y + 6.5f); lineTo(cx - 24f, y + 5.5f)
                moveTo(cx + 19f, y + 6.5f); lineTo(cx + 24f, y + 5.5f)
            }
            drawPath(path, frameColor, style = frameStroke)
        }
        "half" -> {
            // 하프 안경 (아래 반만)
            val path = Path().apply {
                moveTo(cx - 19f, y + 7f)
                arcTo(androidx.compose.ui.geometry.Rect(cx - 19f, y, cx - 5f, y + 14f), 0f, 180f, false)
                moveTo(cx + 5f, y + 7f)
                arcTo(androidx.compose.ui.geometry.Rect(cx + 5f, y, cx + 19f, y + 14f), 0f, 180f, false)
                moveTo(cx - 5f, y + 7f); lineTo(cx + 5f, y + 7f)
                moveTo(cx - 19f, y + 7f); lineTo(cx - 24f, y + 6f)
                moveTo(cx + 19f, y + 7f); lineTo(cx + 24f, y + 6f)
            }
            drawPath(path, frameColor, style = frameStroke)
        }
        "heart" -> {
            // 하트 안경 (귀여움)
            val pinkFrame = Color(0xFFFF80AB)
            val path = Path().apply {
                // 왼쪽 하트
                moveTo(cx - 12f, y + 3f)
                cubicTo(cx - 12f, y, cx - 19f, y, cx - 19f, y + 5f)
                cubicTo(cx - 19f, y + 10f, cx - 12f, y + 13f, cx - 12f, y + 13f)
                cubicTo(cx - 12f, y + 13f, cx - 5f, y + 10f, cx - 5f, y + 5f)
                cubicTo(cx - 5f, y, cx - 12f, y, cx - 12f, y + 3f)
                // 오른쪽 하트
                moveTo(cx + 12f, y + 3f)
                cubicTo(cx + 12f, y, cx + 5f, y, cx + 5f, y + 5f)
                cubicTo(cx + 5f, y + 10f, cx + 12f, y + 13f, cx + 12f, y + 13f)
                cubicTo(cx + 12f, y + 13f, cx + 19f, y + 10f, cx + 19f, y + 5f)
                cubicTo(cx + 19f, y, cx + 12f, y, cx + 12f, y + 3f)
            }
            drawPath(path, pinkFrame, style = Stroke(width = 2f))
            // 브리지
            val bridge = Path().apply { moveTo(cx - 5f, y + 7f); lineTo(cx + 5f, y + 7f) }
            drawPath(bridge, pinkFrame, style = Stroke(width = 2f))
        }
        "star" -> {
            // 별 모양 안경 (특별)
            val goldColor = Color(0xFFE8B547)
            fun starPath(cx: Float, cy: Float, r: Float): Path {
                val path = Path()
                val innerR = r * 0.45f
                for (i in 0 until 10) {
                    val angle = (Math.PI * i / 5 - Math.PI / 2).toFloat()
                    val radius = if (i % 2 == 0) r else innerR
                    val px = cx + radius * cos(angle)
                    val py = cy + radius * sin(angle)
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                path.close()
                return path
            }
            drawPath(starPath(cx - 12f, y + 7f, 7f), goldColor, style = Stroke(width = 1.8f))
            drawPath(starPath(cx + 12f, y + 7f, 7f), goldColor, style = Stroke(width = 1.8f))
            val bridge = Path().apply { moveTo(cx - 5f, y + 7f); lineTo(cx + 5f, y + 7f) }
            drawPath(bridge, goldColor, style = Stroke(width = 1.8f))
        }
    }
}

private fun DrawScope.drawHat(hat: String?, cx: Float, ty: Float) {
    when (hat) {
        "crown" -> {
            val crownPath = Path().apply {
                moveTo(cx - 20, ty + 6); lineTo(cx - 20, ty - 10); lineTo(cx - 10, ty - 3)
                lineTo(cx - 3, ty - 15); lineTo(cx + 3, ty - 3); lineTo(cx + 10, ty - 15)
                lineTo(cx + 20, ty - 3); lineTo(cx + 20, ty + 6); close()
            }
            drawPath(crownPath, Color(0xFFF5C842))
            drawPath(crownPath, Color(0xFFB8862A), style = Stroke(width = 1.2f))
            drawRect(Color(0xFFE8B547), topLeft = Offset(cx - 20, ty + 5), size = Size(40f, 5f))
            drawRect(Color(0xFFB8862A), topLeft = Offset(cx - 20, ty + 5), size = Size(40f, 5f), alpha = 0f,
                style = Stroke(width = 1.2f))
            drawCircle(Color(0xFFE5896B), radius = 2f, center = Offset(cx - 10, ty - 3))
            drawCircle(Color(0xFF6BA3D6), radius = 2f, center = Offset(cx + 3,  ty - 3))
        }
        "flower" -> {
            // 꽃 화관 - 원들로 표현
            for (i in 0 until 5) {
                val angle = Math.toRadians(i * 72.0 - 90)
                val px = cx + (10 * Math.cos(angle)).toFloat()
                val py = ty + (6 * Math.sin(angle)).toFloat()
                drawCircle(Color(0xFFFFB3C6), radius = 5f, center = Offset(px, py))
            }
            drawCircle(Color(0xFFFFD93D), radius = 4f, center = Offset(cx, ty))
        }
        "cap" -> {
            val capPath = Path().apply {
                moveTo(cx - 18, ty + 2); arcTo(
                    rect = androidx.compose.ui.geometry.Rect(cx - 18, ty - 10, cx + 18, ty + 2),
                    startAngleDegrees = 180f, sweepAngleDegrees = 180f, forceMoveTo = false
                )
                close()
            }
            drawPath(capPath, Color(0xFF356B62))
            // 챙
            val brimPath = Path().apply {
                moveTo(cx - 20, ty + 2); lineTo(cx + 28, ty + 2); lineTo(cx + 28, ty + 6); lineTo(cx - 20, ty + 6); close()
            }
            drawPath(brimPath, Color(0xFF244D46))
        }
        "beanie" -> {
            val beanPath = Path().apply {
                moveTo(cx - 16, ty + 4)
                cubicTo(cx - 18, ty - 6, cx - 10, ty - 14, cx, ty - 14)
                cubicTo(cx + 10, ty - 14, cx + 18, ty - 6, cx + 16, ty + 4)
                close()
            }
            drawPath(beanPath, Color(0xFF6BA3D6))
            drawCircle(Color(0xFF4A85BB), radius = 5f, center = Offset(cx, ty - 16))
        }
        "bunny" -> {
            // 토끼 귀 두 개
            val leftEar = Path().apply {
                moveTo(cx - 10, ty); cubicTo(cx - 16, ty - 20, cx - 20, ty - 22, cx - 12, ty - 10)
            }
            val rightEar = Path().apply {
                moveTo(cx + 10, ty); cubicTo(cx + 16, ty - 20, cx + 20, ty - 22, cx + 12, ty - 10)
            }
            drawPath(leftEar,  Color(0xFFFFC8D4), style = Stroke(width = 7f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
            drawPath(rightEar, Color(0xFFFFC8D4), style = Stroke(width = 7f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
            drawPath(leftEar,  Color(0xFFFFAABB), style = Stroke(width = 3f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
            drawPath(rightEar, Color(0xFFFFAABB), style = Stroke(width = 3f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
        }
    }
}

// ─── 미니 아바타 (랭킹, 채팅 목록용) ─────────────────────
@Composable
fun MiniAvatar(
    modifier: Modifier = Modifier,
    color: String = "mint",
    face: AvatarFace = AvatarFace(),
    hat: String? = null,
    outfit: String? = null,
    size: Dp = 48.dp,
) {
    거지방Avatar(modifier = modifier, color = color, face = face, hat = hat, outfit = outfit, size = size)
}
