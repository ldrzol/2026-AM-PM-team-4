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
    val frontHair: String  = "fh3",
    val backHair:  String  = "bh5",
    val eye:       String  = "e1",
    val eyebrow:   String  = "b15",
    val nose:      String  = "n1",
    val mouth:     String  = "m2",
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

// ─── SVG 경로 파서 헬퍼 ───────────────────────────────────
private fun parsePath(d: String): Path = PathParser().parsePathString(d).toPath()

// ─── 앞머리 경로 (100×100 캔버스) ─────────────────────────
private val FRONT_HAIR_PATHS: Map<String, Path> by lazy {
    mapOf(
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
        "bh5"  to parsePath("M 5 95 L 0 29 L 48 0 L 100 29 L 91 95 L 76 78 L 48 100 L 19 78 L 5 95 Z"),
        "bh9"  to Path().apply {
            addPath(parsePath("M 33 25 C 33 5 13 0 2 0 L 0 27 L 21 64 C 25 60 33 45 33 25 Z"))
            addPath(parsePath("M 67 25 C 67 5 87 0 98 0 L 100 27 L 79 64 C 75 60 67 45 67 25 Z"))
        },
        "bh31" to parsePath("M 0 86 C 10 63 12 22 11 4 L 100 0 C 98 36 104 73 108 86 C 99 85 92 81 90 79 C 89 85 91 93 92 96 C 81 94 74 84 72 79 C 70 89 58 98 52 102 C 45 98 39 85 36 79 C 34 87 21 94 15 96 C 16 94 17 84 17 79 C 13 82 4 85 0 86 Z"),
        "bh43" to parsePath("M 6 67 C 1 60 0 44 0 37 C 18 -26 103 6 106 30 C 108 50 104 63 101 67 L 95 64 C 93 81 80 93 73 96 L 65 88 C 63 91 56 97 53 100 C 51 99 45 92 42 88 L 34 96 C 20 90 13 73 11 64 L 6 67 Z"),
    )
}

// ─── 눈썹 경로 ─────────────────────────────────────────────
private val EYEBROW_PATHS: Map<String, Path> by lazy {
    mapOf(
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
        "n1" to parsePath("M 4 0 C 4 1.5 3.7 2.7 3.4 3.6 C 3 4.6 2 5.2 2 7 C 2 8 3 8.5 4.5 8.7 L 4.5 11 C 2.5 10.9 0 10.2 0 7.6 C 0 6.5 0.3 5.6 0.7 4.8 C 1.4 3.5 2 2.3 2 0 Z"),
        "n3" to Path(), // 두 콧구멍 - 원으로 처리
    )
}

// ─── 입 경로 ───────────────────────────────────────────────
private val MOUTH_PATHS: Map<String, Path> by lazy {
    mapOf(
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

    // 뒷머리
    BACK_HAIR_PATHS[face.backHair]?.let { path ->
        withTransform({
            translate(hairTx, hairTy)
            scale(hairScale, hairScale, pivot = Offset.Zero)
        }) { drawPath(path, HAIR_COLOR) }
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

    // 앞머리
    FRONT_HAIR_PATHS[face.frontHair]?.let { path ->
        withTransform({
            translate(hairTx, hairTy)
            scale(hairScale, hairScale, pivot = Offset.Zero)
        }) { drawPath(path, HAIR_COLOR) }
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
