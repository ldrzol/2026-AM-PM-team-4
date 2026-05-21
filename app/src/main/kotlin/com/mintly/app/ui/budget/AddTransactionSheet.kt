package com.mintly.app.ui.budget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mintly.app.data.model.Category
import com.mintly.app.data.model.Favorite
import com.mintly.app.data.model.Transaction
import com.mintly.app.ui.components.getCategoryEmoji
import com.mintly.app.ui.components.toComposeColor
import com.mintly.app.ui.theme.거지방Colors
import com.mintly.app.ui.theme.Shape10
import com.mintly.app.ui.theme.Shape14
import com.mintly.app.ui.theme.ShapePill
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheet(
    categories: List<Category>,
    favorites: List<Favorite>,
    initialDate: LocalDate? = null,
    onAdd: (Transaction) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var kind        by remember { mutableStateOf("expense") }
    var amount      by remember { mutableStateOf("") }
    var memo        by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf<Category?>(null) }
    var selectedDate by remember { mutableStateOf(initialDate ?: LocalDate.now()) }
    var calExpanded  by remember { mutableStateOf(false) }
    var calMonth     by remember { mutableStateOf(YearMonth.from(initialDate ?: LocalDate.now())) }

    val dateStr     = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
    val filteredCats = categories.filter { it.kind == kind }
    val accentColor  = if (kind == "expense") 거지방Colors.Expense else 거지방Colors.Income
    val chevronAngle by animateFloatAsState(
        targetValue = if (calExpanded) 180f else 0f,
        label = "chevron",
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(거지방Colors.Gray200),
                )
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
        ) {
            // ─── 헤더 ─────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "거래 추가",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = 거지방Colors.Gray900,
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, null, tint = 거지방Colors.Gray400)
                }
            }

            // ─── 수입/지출 토글 ────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 4.dp)
                    .clip(ShapePill)
                    .background(거지방Colors.Gray100)
                    .padding(4.dp),
            ) {
                listOf("expense" to "지출", "income" to "수입").forEach { (id, label) ->
                    val active = kind == id
                    val activeColor = if (id == "expense") 거지방Colors.Expense else 거지방Colors.Income
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(ShapePill)
                            .background(if (active) Color.White else Color.Transparent)
                            .clickable { kind = id; selectedCat = null }
                            .padding(vertical = 11.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                            color = if (active) activeColor else 거지방Colors.Gray500,
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ─── 금액 대형 표시 영역 ───────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(Shape14)
                    .background(accentColor.copy(alpha = 0.06f))
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = if (kind == "expense") "지출 금액" else "수입 금액",
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = if (amount.isBlank()) "0" else "%,d".format(amount.toLongOrNull() ?: 0L),
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp),
                        fontWeight = FontWeight.Bold,
                        color = if (amount.isBlank()) 거지방Colors.Gray300 else accentColor,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "원",
                        style = MaterialTheme.typography.titleMedium,
                        color = accentColor.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 10) amount = it },
                    placeholder = { Text("금액을 입력하세요", color = 거지방Colors.Gray400) },
                    trailingIcon = {
                        if (amount.isNotBlank()) {
                            IconButton(onClick = { amount = "" }) {
                                Icon(Icons.Rounded.Clear, null, tint = 거지방Colors.Gray400, modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = Shape10,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor    = accentColor,
                        unfocusedBorderColor  = accentColor.copy(alpha = 0.3f),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                )
                Spacer(Modifier.height(10.dp))
                // 빠른 금액 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    listOf(1_000, 5_000, 10_000, 50_000).forEach { quick ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    val cur = amount.toLongOrNull() ?: 0L
                                    amount = (cur + quick).toString()
                                },
                            shape = ShapePill,
                            color = Color.White,
                            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f)),
                        ) {
                            Text(
                                text = "+${quick / 1_000}천",
                                modifier = Modifier.padding(vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = accentColor,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ─── 즐겨찾기 ─────────────────────────────────────
            val myFavs = favorites.filter { it.kind == kind }
            if (myFavs.isNotEmpty()) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        "즐겨찾기",
                        style = MaterialTheme.typography.labelMedium,
                        color = 거지방Colors.Gray500,
                        fontWeight = FontWeight.SemiBold,
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(myFavs) { fav ->
                            FavoriteChip(fav = fav) {
                                selectedCat = categories.find { it.id == fav.categoryId }
                                fav.amount?.let { amount = it.toString() }
                                fav.memo?.let { memo = it }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // ─── 카테고리 선택 ────────────────────────────────
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    "카테고리",
                    style = MaterialTheme.typography.labelMedium,
                    color = 거지방Colors.Gray500,
                    fontWeight = FontWeight.SemiBold,
                )
                if (filteredCats.isEmpty()) {
                    Text(
                        "카테고리가 없습니다",
                        style = MaterialTheme.typography.bodySmall,
                        color = 거지방Colors.Gray400,
                    )
                } else {
                    val rows = filteredCats.chunked(4)
                    rows.forEach { rowCats ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            rowCats.forEach { cat ->
                                val selected = selectedCat?.id == cat.id
                                CategoryChip(
                                    modifier = Modifier.weight(1f),
                                    cat = cat,
                                    selected = selected,
                                    accentColor = accentColor,
                                ) { selectedCat = cat }
                            }
                            repeat(4 - rowCats.size) { Box(modifier = Modifier.weight(1f)) }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ─── 날짜 선택 (인라인 캘린더) ─────────────────────
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "날짜",
                    style = MaterialTheme.typography.labelMedium,
                    color = 거지방Colors.Gray500,
                    fontWeight = FontWeight.SemiBold,
                )
                // 날짜 표시 버튼
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(Shape10)
                        .border(
                            width = 1.dp,
                            color = if (calExpanded) accentColor else 거지방Colors.Gray200,
                            shape = Shape10,
                        )
                        .background(if (calExpanded) accentColor.copy(alpha = 0.04f) else Color.White)
                        .clickable {
                            calExpanded = !calExpanded
                            if (calExpanded) calMonth = YearMonth.from(selectedDate)
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        Icons.Rounded.CalendarToday,
                        null,
                        tint = if (calExpanded) accentColor else 거지방Colors.Gray500,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = dateLabel(selectedDate),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (calExpanded) accentColor else 거지방Colors.Gray700,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        Icons.Rounded.ExpandMore,
                        null,
                        tint = if (calExpanded) accentColor else 거지방Colors.Gray400,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(chevronAngle),
                    )
                }

                // 인라인 달력
                AnimatedVisibility(
                    visible = calExpanded,
                    enter = expandVertically(),
                    exit = shrinkVertically(),
                ) {
                    InlineDateCalendar(
                        selectedDate = selectedDate,
                        currentMonth = calMonth,
                        accentColor = accentColor,
                        onMonthChange = { calMonth = it },
                        onDateSelected = { date ->
                            selectedDate = date
                            calExpanded = false
                        },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ─── 메모 입력 ────────────────────────────────────
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "메모",
                    style = MaterialTheme.typography.labelMedium,
                    color = 거지방Colors.Gray500,
                    fontWeight = FontWeight.SemiBold,
                )
                OutlinedTextField(
                    value = memo,
                    onValueChange = { memo = it },
                    placeholder = { Text("메모를 입력하세요 (선택)", color = 거지방Colors.Gray400) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    minLines = 2,
                    shape = Shape10,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = 거지방Colors.Mint400,
                        unfocusedBorderColor = 거지방Colors.Gray200,
                    ),
                )
            }

            Spacer(Modifier.height(24.dp))

            // ─── 저장 버튼 ────────────────────────────────────
            val canSave = amount.isNotBlank() && (amount.toLongOrNull() ?: 0L) > 0L
            Button(
                onClick = {
                    val amountInt = amount.toIntOrNull() ?: return@Button
                    val tx = Transaction(
                        kind       = kind,
                        amount     = amountInt,
                        memo       = memo.takeIf { it.isNotBlank() },
                        categoryId = selectedCat?.id,
                        occurredOn = dateStr,
                        category   = selectedCat,
                    )
                    onAdd(tx)
                },
                enabled = canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(52.dp),
                shape = ShapePill,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    disabledContainerColor = 거지방Colors.Gray200,
                ),
            ) {
                Text(
                    "저장하기",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (canSave) Color.White else 거지방Colors.Gray400,
                )
            }
        }
    }
}

// ─── 인라인 달력 ────────────────────────────────────────────
@Composable
private fun InlineDateCalendar(
    selectedDate: LocalDate,
    currentMonth: YearMonth,
    accentColor: Color,
    onMonthChange: (YearMonth) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
) {
    val today       = LocalDate.now()
    val firstDay    = currentMonth.atDay(1)
    val daysInMonth = currentMonth.lengthOfMonth()
    // SUN=0, MON=1 ... SAT=6  (Java DayOfWeek: MON=1..SUN=7, so value%7 gives SUN=0)
    val startOffset = firstDay.dayOfWeek.value % 7
    val dayLabels   = listOf("일", "월", "화", "수", "목", "금", "토")

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = Shape14,
        color = 거지방Colors.Gray50,
        border = BorderStroke(1.dp, 거지방Colors.Gray200),
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {

            // 월 네비게이션
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = { onMonthChange(currentMonth.minusMonths(1)) },
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(Icons.Rounded.ChevronLeft, null, tint = 거지방Colors.Gray500, modifier = Modifier.size(20.dp))
                }
                Text(
                    "${currentMonth.year}년 ${currentMonth.monthValue}월",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = 거지방Colors.Gray900,
                )
                IconButton(
                    onClick = { onMonthChange(currentMonth.plusMonths(1)) },
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(Icons.Rounded.ChevronRight, null, tint = 거지방Colors.Gray500, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.height(4.dp))

            // 요일 헤더
            Row(modifier = Modifier.fillMaxWidth()) {
                dayLabels.forEachIndexed { idx, label ->
                    Text(
                        label,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall,
                        color = when (idx) {
                            0    -> Color(0xFFE05252)
                            6    -> 거지방Colors.Info
                            else -> 거지방Colors.Gray400
                        },
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Spacer(Modifier.height(2.dp))
            HorizontalDivider(color = 거지방Colors.Gray200, thickness = 0.5.dp)

            // 날짜 그리드
            val totalCells = startOffset + daysInMonth
            val rows = (totalCells + 6) / 7
            repeat(rows) { rowIdx ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    for (col in 0..6) {
                        val cellIdx = rowIdx * 7 + col
                        val day = cellIdx - startOffset + 1
                        if (day < 1 || day > daysInMonth) {
                            Box(modifier = Modifier.weight(1f).height(40.dp))
                        } else {
                            val date = currentMonth.atDay(day)
                            val isSelected = date == selectedDate
                            val isToday    = date == today
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> accentColor
                                            isToday    -> accentColor.copy(alpha = 0.15f)
                                            else       -> Color.Transparent
                                        }
                                    )
                                    .clickable { onDateSelected(date) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    "$day",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isSelected -> Color.White
                                        isToday    -> accentColor
                                        col == 0   -> Color(0xFFE05252)
                                        col == 6   -> 거지방Colors.Info
                                        else       -> 거지방Colors.Gray700
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── 날짜 레이블 포맷 ─────────────────────────────────────
private fun dateLabel(date: LocalDate): String {
    val today     = LocalDate.now()
    // Java DayOfWeek: MON=1..SUN=7  → value%7 → SUN=0,MON=1,..SAT=6
    val weekNames = arrayOf("일", "월", "화", "수", "목", "금", "토")
    val suffix    = if (date == today) "오늘" else weekNames[date.dayOfWeek.value % 7]
    return "${date.year}년 ${date.monthValue}월 ${date.dayOfMonth}일 ($suffix)"
}

// ─── 카테고리 칩 ───────────────────────────────────────────
@Composable
private fun CategoryChip(
    modifier: Modifier = Modifier,
    cat: Category,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
) {
    val catColor = if (selected) accentColor else cat.color.toComposeColor()
    Column(
        modifier = modifier
            .clip(Shape14)
            .background(if (selected) catColor.copy(alpha = 0.12f) else 거지방Colors.Gray50)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) catColor else 거지방Colors.Gray200,
                shape = Shape14,
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(getCategoryEmoji(cat.icon), fontSize = 20.sp, textAlign = TextAlign.Center)
        Text(
            cat.name,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = if (selected) catColor else 거지방Colors.Gray600,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

// ─── 즐겨찾기 칩 ───────────────────────────────────────────
@Composable
private fun FavoriteChip(fav: Favorite, onClick: () -> Unit) {
    Surface(
        shape = ShapePill,
        color = 거지방Colors.Mint50,
        border = BorderStroke(1.dp, 거지방Colors.Mint200),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(Icons.Rounded.Star, null, tint = 거지방Colors.Coin, modifier = Modifier.size(14.dp))
            Text(
                fav.memo?.takeIf { it.isNotBlank() } ?: fav.category?.name ?: "즐겨찾기",
                style = MaterialTheme.typography.labelMedium,
                color = 거지방Colors.Mint700,
                fontWeight = FontWeight.SemiBold,
            )
            if (fav.amount != null) {
                Text(
                    "%,d원".format(fav.amount),
                    style = MaterialTheme.typography.labelSmall,
                    color = 거지방Colors.Mint600,
                )
            }
        }
    }
}
