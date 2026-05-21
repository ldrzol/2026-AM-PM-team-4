package com.mintly.app.data.repository

import com.mintly.app.data.model.Costume
import com.mintly.app.data.model.DEFAULT_COSTUMES
import com.mintly.app.data.model.DEFAULT_ROULETTE_SEGMENTS
import com.mintly.app.data.model.RouletteSegment
import com.mintly.app.data.model.RouletteTicket
import com.mintly.app.data.model.UserCostume
import com.mintly.app.data.supabase.SupabaseManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class ShopRepository @Inject constructor(
    private val supabase: SupabaseManager,
) {
    private val client get() = supabase.client

    // ── 코스튬 목록 ───────────────────────────────────────────

    suspend fun getShopCostumes(): List<Costume> = runCatching {
        client.from("costumes")
            .select { filter { eq("is_shop", true) } }
            .decodeList<Costume>()
    }.getOrElse { DEFAULT_COSTUMES.filter { it.isShop } }

    suspend fun getMyInventory(): List<UserCostume> {
        val uid = client.auth.currentUserOrNull()?.id ?: return emptyList()
        return runCatching {
            client.from("user_costumes")
                .select(Columns.raw("*, costume:costumes(*)")) {
                    filter { eq("user_id", uid) }
                }
                .decodeList<UserCostume>()
        }.getOrElse { emptyList() }
    }

    suspend fun purchaseCostume(costumeId: String, price: Int): Result<Unit> = runCatching {
        val uid = client.auth.currentUserOrNull()?.id ?: error("Not logged in")
        // 코인 차감
        client.postgrest.rpc("purchase_costume", buildJsonObject {
            put("p_user_id",   uid)
            put("p_costume_id", costumeId)
            put("p_price",     price)
        })
    }

    // ── 룰렛 ─────────────────────────────────────────────────

    suspend fun getMyTicketCount(): Int {
        val uid = client.auth.currentUserOrNull()?.id ?: return 0
        return runCatching {
            client.from("roulette_tickets")
                .select { filter { eq("user_id", uid); eq("used", false) } }
                .decodeList<RouletteTicket>()
                .size
        }.getOrElse { 0 }
    }

    suspend fun spinRoulette(): Result<RouletteSegment> = runCatching {
        val uid = client.auth.currentUserOrNull()?.id ?: error("Not logged in")
        // 미사용 티켓 1장 조회
        val ticket = client.from("roulette_tickets")
            .select { filter { eq("user_id", uid); eq("used", false) } }
            .decodeList<RouletteTicket>()
            .firstOrNull() ?: error("티켓이 없습니다")

        // 랜덤 결과 선택 (가중치: 꽝 확률 30%, 코인 50%, 아이템 20%)
        val segments = DEFAULT_ROULETTE_SEGMENTS
        val segment = segments[Random.nextInt(segments.size)]

        // 티켓 사용 처리
        client.from("roulette_tickets")
            .update(mapOf("used" to true, "used_at" to "now()")) {
                filter { eq("id", ticket.id) }
            }

        // 보상 지급
        when (segment.rewardType) {
            "coins" -> {
                val amount = segment.rewardValue.toIntOrNull() ?: 0
                client.postgrest.rpc("add_coins", buildJsonObject {
                    put("p_user_id", uid)
                    put("p_amount",  amount)
                })
            }
            "hat", "outfit" -> {
                val costumeId = when (segment.rewardType) {
                    "outfit" -> "outfit_${segment.rewardValue}"
                    else     -> "hat_${segment.rewardValue}"
                }
                runCatching {
                    client.from("user_costumes").insert(
                        mapOf("user_id" to uid, "costume_id" to costumeId)
                    )
                }
            }
        }
        segment
    }

}
