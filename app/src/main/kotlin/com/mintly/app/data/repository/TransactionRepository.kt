package com.mintly.app.data.repository

import com.mintly.app.data.model.Category
import com.mintly.app.data.model.Favorite
import com.mintly.app.data.model.Transaction
import com.mintly.app.data.supabase.SupabaseManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val supabase: SupabaseManager,
) {
    private val client get() = supabase.client

    // ── 거래 내역 ──────────────────────────────────────────────

    suspend fun getTransactionsForMonth(year: Int, month: Int): List<Transaction> {
        val uid = client.auth.currentUserOrNull()?.id ?: return emptyList()
        val from = "%04d-%02d-01".format(year, month)
        val to   = "%04d-%02d-01".format(if (month == 12) year + 1 else year, if (month == 12) 1 else month + 1)
        return runCatching {
            client.from("transactions")
                .select(Columns.raw("*, category:categories(*)")) {
                    filter {
                        eq("user_id", uid)
                        gte("occurred_on", from)
                        lt("occurred_on", to)
                    }
                    order("occurred_on", Order.DESCENDING)
                }
                .decodeList<Transaction>()
        }.getOrElse { emptyList() }
    }

    suspend fun addTransaction(tx: Transaction): Result<Transaction> = runCatching {
        val uid = client.auth.currentUserOrNull()?.id ?: error("Not logged in")
        client.from("transactions")
            .insert(tx.copy(userId = uid))
            .decodeSingle<Transaction>()
    }

    suspend fun updateTransaction(tx: Transaction): Result<Unit> = runCatching {
        client.from("transactions").update(tx) { filter { eq("id", tx.id) } }
    }

    suspend fun deleteTransaction(id: String): Result<Unit> = runCatching {
        client.from("transactions").delete { filter { eq("id", id) } }
    }

    // ── 카테고리 ──────────────────────────────────────────────

    suspend fun getCategories(): List<Category> {
        val uid = client.auth.currentUserOrNull()?.id ?: return emptyList()
        return runCatching {
            client.from("categories")
                .select {
                    filter {
                        or {
                            exact("owner_id", null)
                            eq("owner_id", uid)
                        }
                    }
                    order("sort_order", Order.ASCENDING)
                }
                .decodeList<Category>()
        }.getOrElse { emptyList() }
    }

    suspend fun addCategory(category: Category): Result<Unit> = runCatching {
        val uid = client.auth.currentUserOrNull()?.id ?: error("Not logged in")
        client.from("categories").insert(category.copy(ownerId = uid))
    }

    suspend fun deleteCategory(id: String): Result<Unit> = runCatching {
        client.from("categories").delete { filter { eq("id", id) } }
    }

    // ── 즐겨찾기 ──────────────────────────────────────────────

    suspend fun getFavorites(): List<Favorite> {
        val uid = client.auth.currentUserOrNull()?.id ?: return emptyList()
        return runCatching {
            client.from("favorites")
                .select(Columns.raw("*, category:categories(*)")) {
                    filter { eq("user_id", uid) }
                    order("sort_order", Order.ASCENDING)
                }
                .decodeList<Favorite>()
        }.getOrElse { emptyList() }
    }

    suspend fun addFavorite(fav: Favorite): Result<Unit> = runCatching {
        val uid = client.auth.currentUserOrNull()?.id ?: error("Not logged in")
        client.from("favorites").insert(fav.copy(userId = uid))
    }

    suspend fun deleteFavorite(id: String): Result<Unit> = runCatching {
        client.from("favorites").delete { filter { eq("id", id) } }
    }

    // ── 월별 카테고리 분석 ─────────────────────────────────────

    suspend fun getMonthlyCategoryStats(year: Int, month: Int): Map<Category, Int> {
        val txList = getTransactionsForMonth(year, month)
        return txList
            .filter { it.kind == "expense" && it.category != null }
            .groupBy { it.category!! }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
    }
}
