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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

// ─── 아바타 얼굴 설정 (하위 호환 유지) ───────────────────────
data class AvatarFace(
    val frontHair: String  = "none",
    val backHair:  String  = "none",
    val eye:       String  = "arc",
    val eyebrow:   String  = "none",
    val nose:      String  = "none",
    val mouth:     String  = "smile",
    val glasses:   String? = null,
)

// ─── 바디 팔레트 ───────────────────────────────────────────────
private data class BodyPalette(val body: Color, val shade: Color)

private val BODY_PALETTES = mapOf(
    "mint"   to BodyPalette(Color(0xFF74BBAE), Color(0xFF5BA89A)),
    "peach"  to BodyPalette(Color(0xFFE5896B), Color(0xFFC46E51)),
    "blue"   to BodyPalette(Color(0xFF6BA3D6), Color(0xFF4A85BB)),
    "purple" to BodyPalette(Color(0xFF9C7DD9), Color(0xFF7E5FBE)),
    "yellow" to BodyPalette(Color(0xFFE8B547), Color(0xFFC8961F)),
    "pink"   to BodyPalette(Color(0xFFEF8FAB), Color(0xFFD06888)),
    "green"  to BodyPalette(Color(0xFF5DBB77), Color(0xFF3E9E5A)),
)

private val FEATURE = Color(0xFF1F2A28)

// ─── 메인 Avatar Composable ───────────────────────────────────
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

    Canvas(
        modifier = modifier.size(size, size * 150f / 120f),
    ) {
        val W = 120f
        val H = 150f
        val sx = this.size.width  / W
        val sy = this.size.height / H

        withTransform({ scale(sx, sy, pivot = Offset.Zero) }) {
            drawBlobCharacter(pal, hat, face)
        }
    }
}

// ─── 블롭 캐릭터 드로잉 ───────────────────────────────────────
private fun DrawScope.drawBlobCharacter(
    pal: BodyPalette,
    hat: String?,
    face: AvatarFace = AvatarFace(),
) {
    val cx = 60f
    val cy = 70f
    val bodyTop = 23f

    // ── 그림자 ────────────────────────────────────────────────
    drawOval(
        color = Color(0x1A000000),
        topLeft = Offset(19f, 136f),
        size = Size(82f, 9f),
    )

    // ── 글로우 아우라 ─────────────────────────────────────────
    drawCircle(
        brush  = Brush.radialGradient(
            colors = listOf(pal.body.copy(alpha = 0.16f), Color.Transparent),
            center = Offset(cx, cy),
            radius = 74f,
        ),
        radius = 74f,
        center = Offset(cx, cy),
    )

    // ── 발 (몸 바닥에 붙임) ───────────────────────────────────
    drawOval(pal.shade, topLeft = Offset(28f, 108f), size = Size(23f, 15f))
    drawOval(pal.shade, topLeft = Offset(69f, 108f), size = Size(23f, 15f))

    // ── 귀 ───────────────────────────────────────────────────
    drawOval(pal.body, topLeft = Offset(6f, 55f), size = Size(27f, 55f))
    drawOval(pal.body, topLeft = Offset(87f, 55f), size = Size(27f, 55f))

    // ── 메인 바디 ─────────────────────────────────────────────
    drawOval(
        color = pal.body,
        topLeft = Offset(18f, bodyTop),
        size = Size(84f, 90f),
    )

    // ── 바디 하단 셰이딩 (입체감) ─────────────────────────────
    drawOval(
        color = Color.White.copy(alpha = 0.14f),
        topLeft = Offset(33f, 72f),
        size = Size(54f, 44f),
    )

    // ── 팔 ───────────────────────────────────────────────────


    // ── 볼터치 ────────────────────────────────────────────────
    drawCircle(
        color = Color(0xFFE6A592).copy(alpha = 0.52f),
        radius = 8.5f,
        center = Offset(38f, 82f),
    )
    drawCircle(
        color = Color(0xFFE6A592).copy(alpha = 0.52f),
        radius = 8.5f,
        center = Offset(82f, 82f),
    )

    // ── 눈 ────────────────────────────────────────────────────
    drawEyes(face.eye)

    // ── 입 ───────────────────────────────────────────────────
    drawMouth(face.mouth)

    // ── 모자 / 헤어 악세서리 ─────────────────────────────────
    val hatId = hat?.removePrefix("hat_")
    drawHat(hatId, cx, bodyTop)
}

// ─── 눈 스타일 ────────────────────────────────────────────────
private fun DrawScope.drawEyes(eye: String) {
    val lx = 47f  // 왼눈 중심 x  (cx=60 기준 -13)
    val rx = 73f  // 오른눈 중심 x (cx=60 기준 +13)
    val ey = 65f  // 눈 중심 y
    val stroke = Stroke(width = 3.2f, cap = StrokeCap.Round)

    fun arcEye(hx: Float) = Path().apply {
        moveTo(hx - 6f, ey + 5f)
        cubicTo(hx - 3f, ey - 4f, hx + 3f, ey - 4f, hx + 6f, ey + 5f)
    }

    fun heartEye(hx: Float): Path {
        val s = 5f
        return Path().apply {
            moveTo(hx, ey + s * 0.55f)
            cubicTo(hx - s * 1.15f, ey + s * 0.15f, hx - s, ey - s * 0.65f, hx - s * 0.3f, ey - s * 0.8f)
            cubicTo(hx - s * 0.05f, ey - s * 0.95f, hx + s * 0.05f, ey - s * 0.95f, hx + s * 0.3f, ey - s * 0.8f)
            cubicTo(hx + s, ey - s * 0.65f, hx + s * 1.15f, ey + s * 0.15f, hx, ey + s * 0.55f)
            close()
        }
    }

    when (eye) {
        "dot" -> {
            drawCircle(FEATURE, radius = 3.5f, center = Offset(lx, ey))
            drawCircle(FEATURE, radius = 3.5f, center = Offset(rx, ey))
        }
        "wink" -> {
            drawPath(arcEye(lx), FEATURE, style = stroke)
            drawLine(FEATURE, Offset(rx - 6f, ey), Offset(rx + 6f, ey), strokeWidth = 3.2f, cap = StrokeCap.Round)
        }
        "star" -> {
            drawLine(FEATURE, Offset(lx - 4f, ey - 4f), Offset(lx + 4f, ey + 5f), strokeWidth = 2.8f, cap = StrokeCap.Round)
            drawLine(FEATURE, Offset(lx + 4f, ey - 4f), Offset(lx - 4f, ey + 5f), strokeWidth = 2.8f, cap = StrokeCap.Round)
            drawLine(FEATURE, Offset(rx - 4f, ey - 4f), Offset(rx + 4f, ey + 5f), strokeWidth = 2.8f, cap = StrokeCap.Round)
            drawLine(FEATURE, Offset(rx + 4f, ey - 4f), Offset(rx - 4f, ey + 5f), strokeWidth = 2.8f, cap = StrokeCap.Round)
        }
        "heart" -> {
            drawPath(heartEye(lx), Color(0xFFFF6B9D))
            drawPath(heartEye(rx), Color(0xFFFF6B9D))
        }
        else -> { // "arc" (기본)
            drawPath(arcEye(lx), FEATURE, style = stroke)
            drawPath(arcEye(rx), FEATURE, style = stroke)
        }
    }
}

// ─── 입 스타일 ────────────────────────────────────────────────
private fun DrawScope.drawMouth(mouth: String) {
    val stroke = Stroke(width = 2.8f, cap = StrokeCap.Round)
    when (mouth) {
        "open" -> {
            // 벌린 입
            val path = Path().apply {
                moveTo(50f, 91f); cubicTo(55f, 101f, 65f, 101f, 70f, 91f); close()
            }
            drawPath(path, Color(0xFF1F2A28))
            drawPath(path, Color(0xFFFF8FAB).copy(alpha = 0.5f))
        }
        "flat" -> {
            drawLine(FEATURE, Offset(52f, 94f), Offset(68f, 94f), strokeWidth = 2.8f, cap = StrokeCap.Round)
        }
        "sad" -> {
            val path = Path().apply { moveTo(50f, 97f); cubicTo(55f, 89f, 65f, 89f, 70f, 97f) }
            drawPath(path, FEATURE, style = stroke)
        }
        else -> { // "smile" (기본)
            val path = Path().apply { moveTo(50f, 91f); cubicTo(55f, 99f, 65f, 99f, 70f, 91f) }
            drawPath(path, FEATURE, style = stroke)
        }
    }
}

// ─── 헤어 악세서리 드로잉 ────────────────────────────────────
private fun DrawScope.drawHat(hat: String?, cx: Float, ty: Float) {
    when (hat) {

        // ── 왕관 ───────────────────────────────────────────
        "crown" -> {
            val crownPath = Path().apply {
                moveTo(cx - 20f, ty + 6f); lineTo(cx - 20f, ty - 10f); lineTo(cx - 10f, ty - 3f)
                lineTo(cx - 3f,  ty - 15f); lineTo(cx + 3f, ty - 3f); lineTo(cx + 10f, ty - 15f)
                lineTo(cx + 20f, ty - 3f); lineTo(cx + 20f, ty + 6f); close()
            }
            drawPath(crownPath, Color(0xFFF5C842))
            drawPath(crownPath, Color(0xFFB8862A), style = Stroke(width = 1.2f))
            drawRect(Color(0xFFE8B547), topLeft = Offset(cx - 20f, ty + 5f), size = Size(40f, 5f))
            drawCircle(Color(0xFFE5896B), radius = 2f, center = Offset(cx - 10f, ty - 3f))
            drawCircle(Color(0xFF6BA3D6), radius = 2f, center = Offset(cx + 3f,  ty - 3f))
            drawCircle(Color(0xFFE8B547), radius = 2f, center = Offset(cx + 10f, ty - 3f))
        }

        // ── 캡 모자 ─────────────────────────────────────────
        "cap" -> {
            val capPath = Path().apply {
                moveTo(cx - 18f, ty + 2f)
                arcTo(
                    rect = androidx.compose.ui.geometry.Rect(cx - 18f, ty - 12f, cx + 18f, ty + 2f),
                    startAngleDegrees = 180f, sweepAngleDegrees = 180f, forceMoveTo = false,
                )
                close()
            }
            drawPath(capPath, Color(0xFF356B62))
            val brimPath = Path().apply {
                moveTo(cx - 20f, ty + 2f); lineTo(cx + 28f, ty + 2f)
                lineTo(cx + 28f, ty + 6f); lineTo(cx - 20f, ty + 6f); close()
            }
            drawPath(brimPath, Color(0xFF244D46))
        }

        // ── 비니 ────────────────────────────────────────────
        "beanie" -> {
            val beanPath = Path().apply {
                moveTo(cx - 17f, ty + 4f)
                cubicTo(cx - 19f, ty - 8f, cx - 10f, ty - 16f, cx, ty - 16f)
                cubicTo(cx + 10f, ty - 16f, cx + 19f, ty - 8f, cx + 17f, ty + 4f)
                close()
            }
            drawPath(beanPath, Color(0xFF6BA3D6))
            drawCircle(Color(0xFF4A85BB), radius = 5f, center = Offset(cx, ty - 18f))
        }

        // ── 토끼 귀 ─────────────────────────────────────────
        "bunny" -> {
            val leftEar = Path().apply {
                moveTo(cx - 10f, ty + 2f)
                cubicTo(cx - 18f, ty - 18f, cx - 22f, ty - 24f, cx - 13f, ty - 10f)
            }
            val rightEar = Path().apply {
                moveTo(cx + 10f, ty + 2f)
                cubicTo(cx + 18f, ty - 18f, cx + 22f, ty - 24f, cx + 13f, ty - 10f)
            }
            drawPath(leftEar,  Color(0xFFFFC8D4), style = Stroke(width = 8f, cap = StrokeCap.Round))
            drawPath(rightEar, Color(0xFFFFC8D4), style = Stroke(width = 8f, cap = StrokeCap.Round))
            drawPath(leftEar,  Color(0xFFFFAABB), style = Stroke(width = 3.5f, cap = StrokeCap.Round))
            drawPath(rightEar, Color(0xFFFFAABB), style = Stroke(width = 3.5f, cap = StrokeCap.Round))
        }

        // ── 꽃 화관 ─────────────────────────────────────────
        "flower" -> {
            for (i in 0 until 7) {
                val angle = Math.toRadians(i * (360.0 / 7) - 90)
                val px = (cx + 16 * cos(angle)).toFloat()
                val py = (ty + 4 + 8 * sin(angle)).toFloat()
                drawCircle(Color(0xFFFFB3C6), radius = 6f, center = Offset(px, py))
            }
            for (i in 0 until 7) {
                val angle = Math.toRadians(i * (360.0 / 7) - 90)
                val px = (cx + 16 * cos(angle)).toFloat()
                val py = (ty + 4 + 8 * sin(angle)).toFloat()
                drawCircle(Color(0xFFFFD93D), radius = 3f, center = Offset(px, py))
            }
        }

        // ── 리본 (중앙) ──────────────────────────────────────
        "bow" -> {
            val bx = cx
            val by = ty - 1f
            val leftWing = Path().apply {
                moveTo(bx, by)
                cubicTo(bx - 14f, by - 9f, bx - 20f, by - 1f, bx - 11f, by + 5f)
                cubicTo(bx - 5f, by + 9f, bx, by + 4f, bx, by)
            }
            val rightWing = Path().apply {
                moveTo(bx, by)
                cubicTo(bx + 14f, by - 9f, bx + 20f, by - 1f, bx + 11f, by + 5f)
                cubicTo(bx + 5f, by + 9f, bx, by + 4f, bx, by)
            }
            drawPath(leftWing,  Color(0xFFFF6B9D))
            drawPath(rightWing, Color(0xFFFF6B9D))
            drawPath(leftWing,  Color(0xFFE04078), style = Stroke(1.5f))
            drawPath(rightWing, Color(0xFFE04078), style = Stroke(1.5f))
            drawCircle(Color(0xFFE04078), radius = 4f, center = Offset(bx, by))
            drawCircle(Color(0xFFFFB3D0), radius = 2f, center = Offset(bx, by))
        }

        // ── 고양이 귀 ────────────────────────────────────────
        "cat" -> {
            val leftOuter = Path().apply {
                moveTo(cx - 18f, ty + 4f); lineTo(cx - 26f, ty - 14f); lineTo(cx - 8f, ty); close()
            }
            val rightOuter = Path().apply {
                moveTo(cx + 18f, ty + 4f); lineTo(cx + 26f, ty - 14f); lineTo(cx + 8f, ty); close()
            }
            drawPath(leftOuter,  Color(0xFF2C2C2C))
            drawPath(rightOuter, Color(0xFF2C2C2C))
            val leftInner = Path().apply {
                moveTo(cx - 17f, ty + 2f); lineTo(cx - 23f, ty - 9f); lineTo(cx - 10f, ty); close()
            }
            val rightInner = Path().apply {
                moveTo(cx + 17f, ty + 2f); lineTo(cx + 23f, ty - 9f); lineTo(cx + 10f, ty); close()
            }
            drawPath(leftInner,  Color(0xFFFFB3C6))
            drawPath(rightInner, Color(0xFFFFB3C6))
        }

        // ── 별 핀 ────────────────────────────────────────────
        "star" -> {
            val sx = cx + 24f
            val sy = ty + 8f
            fun star(scx: Float, scy: Float, r: Float): Path {
                val p = Path(); val ir = r * 0.45f
                for (i in 0 until 10) {
                    val a = (Math.PI * i / 5 - Math.PI / 2).toDouble()
                    val rad = if (i % 2 == 0) r else ir
                    val px = (scx + rad * cos(a)).toFloat()
                    val py = (scy + rad * sin(a)).toFloat()
                    if (i == 0) p.moveTo(px, py) else p.lineTo(px, py)
                }
                p.close(); return p
            }
            val stick = Path().apply { moveTo(sx, sy + 8f); lineTo(cx + 8f, ty + 24f) }
            drawPath(stick, Color(0xFFCCCCCC), style = Stroke(2.5f, cap = StrokeCap.Round))
            drawPath(star(sx, sy, 9f), Color(0xFFFFD700))
            drawPath(star(sx, sy, 9f), Color(0xFFE8A800), style = Stroke(1.5f))
        }

        // ── 꽃 핀 ────────────────────────────────────────────
        "flower_clip" -> {
            val fx = cx - 26f
            val fy = ty + 8f
            val stick = Path().apply { moveTo(fx, fy + 6f); lineTo(cx - 10f, ty + 22f) }
            drawPath(stick, Color(0xFFCCCCCC), style = Stroke(2.5f, cap = StrokeCap.Round))
            for (i in 0 until 5) {
                val angle = Math.toRadians(i * 72.0 - 90)
                val px = (fx + 7 * cos(angle)).toFloat()
                val py = (fy + 7 * sin(angle)).toFloat()
                drawCircle(Color(0xFFFF9FBD), radius = 5.5f, center = Offset(px, py))
            }
            drawCircle(Color(0xFFFFE066), radius = 4f, center = Offset(fx, fy))
        }

        // ── 사이드 리본 ──────────────────────────────────────
        "side_ribbon" -> {
            val bx = cx + 22f
            val by = ty + 6f
            val leftWing = Path().apply {
                moveTo(bx, by)
                cubicTo(bx - 11f, by - 7f, bx - 15f, by, bx - 8f, by + 4f)
                cubicTo(bx - 3f, by + 6f, bx, by + 3f, bx, by)
            }
            val rightWing = Path().apply {
                moveTo(bx, by)
                cubicTo(bx + 11f, by - 7f, bx + 15f, by, bx + 8f, by + 4f)
                cubicTo(bx + 3f, by + 6f, bx, by + 3f, bx, by)
            }
            drawPath(leftWing,  Color(0xFF9B59B6))
            drawPath(rightWing, Color(0xFF9B59B6))
            drawPath(leftWing,  Color(0xFF6C3483), style = Stroke(1.5f))
            drawPath(rightWing, Color(0xFF6C3483), style = Stroke(1.5f))
            drawCircle(Color(0xFF6C3483), radius = 3f, center = Offset(bx, by))
        }
    }
}

// ─── 미니 아바타 (랭킹 등) ────────────────────────────────────
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
