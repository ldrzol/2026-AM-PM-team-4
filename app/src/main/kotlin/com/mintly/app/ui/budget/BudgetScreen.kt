package com.mintly.app.ui.budget

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mintly.app.data.model.Favorite
import com.mintly.app.data.model.Transaction
import com.mintly.app.ui.components.*
import com.mintly.app.ui.theme.거지방Colors
import com.mintly.app.ui.theme.Shape14
import com.mintly.app.ui.theme.ShapePill
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun BudgetScreen(vm: BudgetViewModel = hiltViewModel()) {
    val state           by vm.uiState.collectAsStateWithLifecycle()
    var showAddSheet    by remember { mutableStateOf(false) }
    var showFavSheet    by remember { mutableStateOf(false) }
    var selectedDate    by remember { mutableStateOf<LocalDate?>(null) }
    val txByDate        = remember(state.transactions) { vm.transactionsByDate() }

    // 선택된 날짜로 필터링
    val filteredTxByDate = remember(txByDate, selectedDate) {
        if (selectedDate != null) {
            val key = selectedDate!!.format(DateTimeFormatter.ISO_LOCAL_DATE)
            txByDate.filterKeys { it == key }
        } else txByDate
    }

    LaunchedEffect(state.error, state.successMsg) {
        if (state.error != null || state.successMsg != null) {
            kotlinx.coroutines.delay(2500)
            vm.clearMessages()
        }
    }

    // 달이 바뀌면 선택된 날짜 초기화
    LaunchedEffect(state.year, state.month) {
        selectedDate = null
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentPadding = PaddingValues(bottom = 88.dp),
        ) {
            // ─── 달력 헤더 ────────────────────────────────────
            item {
                Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {

                    // 월 이동 네비게이터 (중앙 정렬 + 오른쪽 별 버튼)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 12.dp),
                    ) {
                        Row(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(
                                onClick = {
                                    val ym = YearMonth.of(state.year, state.month).minusMonths(1)
                                    vm.changeMonth(ym.year, ym.monthValue)
                                },
                                modifier = Modifier.size(40.dp),
                            ) {
                                Icon(Icons.Rounded.ChevronLeft, null, tint = 거지방Colors.Gray600)
                            }
                            Text(
                                text = "${state.year}년 ${state.month}월",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = 거지방Colors.Gray900,
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                            IconButton(
                                onClick = {
                                    val ym = YearMonth.of(state.year, state.month).plusMonths(1)
                                    vm.changeMonth(ym.year, ym.monthValue)
                                },
                                modifier = Modifier.size(40.dp),
                            ) {
                                Icon(Icons.Rounded.ChevronRight, null, tint = 거지방Colors.Gray600)
                            }
                        }
                        // 즐겨찾기 빠른 추가 버튼
                        IconButton(
                            onClick = { showFavSheet = true },
                            modifier = Modifier.align(Alignment.CenterEnd).size(52.dp),
                        ) {
                            Icon(
                                Icons.Rounded.Star,
                                contentDescription = "즐겨찾기 빠른 추가",
                                tint = if (state.favorites.isEmpty()) 거지방Colors.Gray300 else 거지방Colors.Coin,
                                modifier = Modifier.size(32.dp),
                            )
                        }
                    }

                    // 월 요약 (3칸 + 구분선)
                    val income  = state.transactions.filter { it.kind == "income"  }.sumOf { it.amount }
                    val expense = state.transactions.filter { it.kind == "expense" }.sumOf { it.amount }
                    val net     = income - expense

                    HorizontalDivider(color = 거지방Colors.Gray100)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                            .padding(vertical = 14.dp),
                    ) {
                        MonthlySummaryCol(
                            modifier = Modifier.weight(1f),
                            label    = "수입",
                            text     = "+%,d".format(income),
                            color    = 거지방Colors.Income,
                        )
                        Box(
                            Modifier
                                .width(1.dp)
                                .fillMaxHeight()
                                .background(거지방Colors.Gray200),
                        )
                        MonthlySummaryCol(
                            modifier = Modifier.weight(1f),
                            label    = "지출",
                            text     = "-%,d".format(expense),
                            color    = 거지방Colors.Expense,
                        )
                        Box(
                            Modifier
                                .width(1.dp)
                                .fillMaxHeight()
                                .background(거지방Colors.Gray200),
                        )
                        MonthlySummaryCol(
                            modifier = Modifier.weight(1f),
                            label    = "합계",
                            text     = "%,d".format(net),
                            color    = if (net >= 0) 거지방Colors.Gray900 else 거지방Colors.Expense,
                            bold     = true,
                        )
                    }
                    HorizontalDivider(color = 거지방Colors.Gray100)

                    // 달력
                    BudgetCalendar(
                        year         = state.year,
                        month        = state.month,
                        txByDate     = txByDate,
                        selectedDate = selectedDate,
                        onDaySelected = { date ->
                            selectedDate = if (selectedDate == date) null else date
                        },
                    )
                }
            }

            // ─── 거래 목록 헤더 ───────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(거지방Colors.Gray50)
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val listTitle = if (selectedDate != null) {
                        val dow = selectedDate!!.dayOfWeek
                            .getDisplayName(TextStyle.SHORT, Locale.KOREAN)
                        "${selectedDate!!.monthValue}월 ${selectedDate!!.dayOfMonth}일 ($dow)"
                    } else "전체 내역"

                    Text(
                        listTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = 거지방Colors.Gray800,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (selectedDate != null) {
                            TextButton(
                                onClick = { selectedDate = null },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                Text(
                                    "전체 보기",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = 거지방Colors.Mint500,
                                )
                            }
                        }
                        Text(
                            "${filteredTxByDate.values.sumOf { it.size }}건",
                            style = MaterialTheme.typography.bodyLarge,
                            color = 거지방Colors.Gray400,
                        )
                    }
                }
            }

            // ─── 거래 목록 ────────────────────────────────────
            if (filteredTxByDate.isEmpty()) {
                item {
                    EmptyState(
                        emoji   = "💸",
                        message = if (selectedDate != null)
                            "이 날의 거래 내역이 없습니다"
                        else
                            "이번 달 거래 내역이 없습니다\n아래 + 버튼으로 추가해보세요",
                    )
                }
            } else {
                filteredTxByDate.forEach { (date, txList) ->
                    item { DateHeader(date = date, txList = txList) }
                    items(txList, key = { it.id }) { tx ->
                        TransactionRow(tx = tx, onDelete = { vm.deleteTransaction(tx.id) })
                    }
                }
            }
        }

        // ─── FAB ─────────────────────────────────────────────
        LargeFloatingActionButton(
            onClick = { showAddSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            shape          = CircleShape,
            containerColor = 거지방Colors.Mint400,
            contentColor   = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "거래 추가", modifier = Modifier.size(36.dp))
        }

        // ─── 스낵바 ───────────────────────────────────────────
        if (state.error != null || state.successMsg != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 88.dp),
            ) {
                거지방Snackbar(
                    message   = state.error ?: state.successMsg,
                    isError   = state.error != null,
                    onDismiss = vm::clearMessages,
                )
            }
        }
    }

    if (showAddSheet) {
        AddTransactionSheet(
            categories = state.categories,
            favorites  = state.favorites,
            initialDate = selectedDate,
            onAdd      = { tx -> vm.addTransaction(tx); showAddSheet = false },
            onDismiss  = { showAddSheet = false },
        )
    }

    if (showFavSheet) {
        QuickFavoritesSheet(
            favorites   = state.favorites,
            date        = selectedDate ?: LocalDate.now(),
            onAdd       = { tx -> vm.addTransaction(tx) },
            onDismiss   = { showFavSheet = false },
            onOpenFull  = { showFavSheet = false; showAddSheet = true },
        )
    }
}

// ─── 월 요약 칼럼 ─────────────────────────────────────────
@Composable
private fun MonthlySummaryCol(
    modifier: Modifier,
    label: String,
    text: String,
    color: Color,
    bold: Boolean = false,
) {
    Column(
        modifier = modifier.padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = 거지방Colors.Gray400,
        )
        Text(
            text,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = if (bold) FontWeight.ExtraBold else FontWeight.Bold,
            color = color,
        )
    }
}

// ─── 달력 ─────────────────────────────────────────────────
@Composable
private fun BudgetCalendar(
    year: Int,
    month: Int,
    txByDate: Map<String, List<Transaction>>,
    selectedDate: LocalDate?,
    onDaySelected: (LocalDate) -> Unit,
) {
    val ym          = YearMonth.of(year, month)
    val firstDay    = ym.atDay(1).dayOfWeek.value % 7   // 0=일요일
    val daysInMonth = ym.lengthOfMonth()
    val today       = LocalDate.now()

    Column(modifier = Modifier.fillMaxWidth()) {
        // 요일 헤더
        HorizontalDivider(color = 거지방Colors.Gray100)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        ) {
            listOf("일", "월", "화", "수", "목", "금", "토").forEachIndexed { idx, dow ->
                Text(
                    dow,
                    modifier   = Modifier.weight(1f),
                    style      = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = when (idx) {
                        0    -> Color(0xFFE57373)
                        6    -> Color(0xFF7986CB)
                        else -> 거지방Colors.Gray400
                    },
                    textAlign = TextAlign.Center,
                )
            }
        }
        HorizontalDivider(color = 거지방Colors.Gray100)

        val cells = firstDay + daysInMonth
        val rows  = (cells + 6) / 7
        var day   = 1

        repeat(rows) { rowIdx ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val cellIdx = rowIdx * 7 + col
                    if (cellIdx < firstDay || day > daysInMonth) {
                        Box(modifier = Modifier.weight(1f).height(96.dp))
                    } else {
                        val currentDay = day
                        val date    = LocalDate.of(year, month, currentDay)
                        val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                        val dayTx   = txByDate[dateStr] ?: emptyList()
                        val income  = dayTx.filter { it.kind == "income"  }.sumOf { it.amount }
                        val expense = dayTx.filter { it.kind == "expense" }.sumOf { it.amount }
                        val isToday    = today == date
                        val isSelected = selectedDate == date
                        val isSunday   = col == 0
                        val isSaturday = col == 6

                        CalendarDayCell(
                            modifier   = Modifier.weight(1f),
                            day        = currentDay,
                            income     = income,
                            expense    = expense,
                            isToday    = isToday,
                            isSelected = isSelected,
                            isSunday   = isSunday,
                            isSaturday = isSaturday,
                            onClick    = { onDaySelected(date) },
                        )
                        day++
                    }
                }
            }
            // 행 구분선
            HorizontalDivider(color = 거지방Colors.Gray100)
        }
    }
}

@Composable
private fun CalendarDayCell(
    modifier: Modifier,
    day: Int,
    income: Int,
    expense: Int,
    isToday: Boolean,
    isSelected: Boolean,
    isSunday: Boolean,
    isSaturday: Boolean,
    onClick: () -> Unit,
) {
    val highlight  = isToday || isSelected
    val dayNumColor = when {
        highlight  -> Color.White
        isSunday   -> Color(0xFFE57373)
        isSaturday -> Color(0xFF7986CB)
        else       -> 거지방Colors.Gray800
    }
    val circleBg = when {
        isToday    -> 거지방Colors.Mint400
        isSelected -> 거지방Colors.Mint200
        else       -> Color.Transparent
    }

    Column(
        modifier = modifier
            .height(96.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 날짜 숫자
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(circleBg),
        ) {
            Text(
                "$day",
                fontSize   = 20.sp,
                color      = dayNumColor,
                fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
            )
        }

        Spacer(Modifier.height(2.dp))

        // 수입 (초록)
        if (income > 0) {
            Text(
                text      = "+${calendarAmtFmt(income)}",
                fontSize  = 13.sp,
                color     = 거지방Colors.Income,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 15.sp,
                maxLines  = 1,
            )
        } else {
            Spacer(Modifier.height(15.dp))
        }

        // 지출 (주황/빨강)
        if (expense > 0) {
            Text(
                text      = "-${calendarAmtFmt(expense)}",
                fontSize  = 13.sp,
                color     = 거지방Colors.Expense,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 15.sp,
                maxLines  = 1,
            )
        }
    }
}

// ─── 즐겨찾기 빠른 추가 시트 ─────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickFavoritesSheet(
    favorites: List<Favorite>,
    date: LocalDate,
    onAdd: (Transaction) -> Unit,
    onDismiss: () -> Unit,
    onOpenFull: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedKind by remember { mutableStateOf("expense") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
        ) {
            // 헤더
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 4.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("즐겨찾기 빠른 추가", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    val dow = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)
                    Text(
                        "${date.monthValue}월 ${date.dayOfMonth}일 ($dow)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = 거지방Colors.Gray400,
                    )
                }
                TextButton(onClick = onOpenFull) {
                    Text("직접 입력", style = MaterialTheme.typography.bodyMedium, color = 거지방Colors.Mint500)
                }
            }
            HorizontalDivider(color = 거지방Colors.Gray100)

            // 종류 탭
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
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

            val filtered = favorites.filter { it.kind == selectedKind }

            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text("⭐", fontSize = 28.sp)
                        Text("즐겨찾기가 없습니다", style = MaterialTheme.typography.bodyLarge, color = 거지방Colors.Gray400)
                        TextButton(onClick = onOpenFull) {
                            Text("직접 추가하기", color = 거지방Colors.Mint500, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp),
                ) {
                    items(filtered) { fav ->
                        val hasAmount = fav.amount != null && fav.amount > 0
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (hasAmount) {
                                        onAdd(
                                            Transaction(
                                                kind        = fav.kind,
                                                categoryId  = fav.categoryId,
                                                amount      = fav.amount!!,
                                                memo        = fav.memo,
                                                occurredOn  = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                                                category    = fav.category,
                                            )
                                        )
                                        onDismiss()
                                    }
                                }
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            val accentColor = if (fav.kind == "expense") 거지방Colors.Expense else 거지방Colors.Income
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(52.dp).clip(Shape14).background(accentColor.copy(alpha = 0.1f)),
                            ) {
                                Text(getCategoryEmoji(fav.category?.icon ?: ""), fontSize = 24.sp)
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
                                if (detail.isNotBlank()) Text(detail, style = MaterialTheme.typography.bodyMedium, color = 거지방Colors.Gray400)
                                if (!hasAmount) Text("금액 미설정 — 직접 입력 필요", style = MaterialTheme.typography.labelMedium, color = 거지방Colors.Warning)
                            }
                            if (hasAmount) {
                                Surface(shape = ShapePill, color = accentColor.copy(alpha = 0.1f)) {
                                    Text(
                                        if (fav.kind == "expense") "-${"%,d".format(fav.amount!!)}원" else "+${"%,d".format(fav.amount!!)}원",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = accentColor,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            } else {
                                Icon(Icons.Rounded.Edit, null, tint = 거지방Colors.Gray300, modifier = Modifier.size(22.dp))
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(start = 76.dp), color = 거지방Colors.Gray100, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

private fun calendarAmtFmt(amount: Int): String = when {
    amount >= 10_000_000 -> "${amount / 10_000_000}천만"
    amount >= 1_000_000  -> {
        val v = amount / 10_000.0
        if (v % 1.0 == 0.0) "${v.toInt()}만" else "${"%.1f".format(v)}만"
    }
    amount >= 10_000     -> {
        val v = amount / 10_000.0
        if (v % 1.0 == 0.0) "${v.toInt()}만" else "${"%.1f".format(v)}만"
    }
    else                 -> "%,d".format(amount)
}

// ─── 날짜 헤더 ────────────────────────────────────────────
@Composable
private fun DateHeader(date: String, txList: List<Transaction>) {
    val income  = txList.filter { it.kind == "income"  }.sumOf { it.amount }
    val expense = txList.filter { it.kind == "expense" }.sumOf { it.amount }
    val fmt = runCatching {
        val ld  = LocalDate.parse(date)
        val dow = ld.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)
        "${ld.monthValue}월 ${ld.dayOfMonth}일 ($dow)"
    }.getOrElse { date }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(거지방Colors.Gray50)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            fmt,
            style      = MaterialTheme.typography.titleMedium,
            color      = 거지방Colors.Gray600,
            fontWeight = FontWeight.SemiBold,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (income  > 0) Text("+%,d원".format(income),  style = MaterialTheme.typography.titleMedium, color = 거지방Colors.Income,  fontWeight = FontWeight.SemiBold)
            if (expense > 0) Text("-%,d원".format(expense), style = MaterialTheme.typography.titleMedium, color = 거지방Colors.Expense, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ─── 거래 행 ──────────────────────────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TransactionRow(tx: Transaction, onDelete: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onLongClick = { showMenu = true }, onClick = {}),
        color = Color.White,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            val catColor = (tx.category?.color ?: "#767D84").toComposeColor()
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(52.dp)
                    .clip(Shape14)
                    .background(catColor.copy(alpha = 0.12f)),
            ) {
                Text(getCategoryEmoji(tx.category?.icon ?: "more_horiz"), fontSize = 24.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    tx.memo?.takeIf { it.isNotBlank() } ?: tx.category?.name ?: "기타",
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = 거지방Colors.Gray900,
                )
                if (tx.category != null) {
                    Text(tx.category.name, style = MaterialTheme.typography.bodyLarge, color = 거지방Colors.Gray400)
                }
            }
            AmountText(amount = tx.amount, kind = tx.kind, style = MaterialTheme.typography.titleMedium)
        }
        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            DropdownMenuItem(
                text = { Text("삭제", color = 거지방Colors.Danger) },
                onClick = { showMenu = false; onDelete() },
                leadingIcon = { Icon(Icons.Rounded.Delete, null, tint = 거지방Colors.Danger) },
            )
        }
    }
    HorizontalDivider(color = 거지방Colors.Gray100, thickness = 0.5.dp)
}
