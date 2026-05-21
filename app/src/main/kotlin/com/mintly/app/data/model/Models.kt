package com.mintly.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class Profile(
    val id: String = "",
    val username: String = "",
    @SerialName("display_name") val displayName: String = "",
    @SerialName("avatar_seed")  val avatarSeed: String = "default",
    @SerialName("avatar_color") val avatarColor: String = "mint",
    @SerialName("avatar_face")  val avatarFace: Map<String, String>? = null,
    val email: String? = null,
    val coins: Int = 0,
    val phone: String? = null,
    @SerialName("current_hat")     val currentHat: String? = null,
    @SerialName("current_outfit")  val currentOutfit: String? = null,
    @SerialName("forced_outfit")   val forcedOutfit: String? = null,
    @SerialName("forced_until")    val forcedUntil: String? = null,
    @SerialName("has_crown_until") val hasCrownUntil: String? = null,
    @SerialName("share_mode")      val shareMode: String = "percent",
    @SerialName("check_streak")    val checkStreak: Int = 0,
    @SerialName("last_checkin")    val lastCheckin: String? = null,
    @SerialName("created_at")      val createdAt: String? = null,
)

@Serializable
data class FriendGroup(
    val id: String = "",
    val name: String = "",
    val emoji: String = "🏠",
    @SerialName("invite_code") val inviteCode: String = "",
    @SerialName("owner_id")    val ownerId: String = "",
    @SerialName("created_at")  val createdAt: String? = null,
)

@Serializable
data class GroupMember(
    @SerialName("group_id")  val groupId: String = "",
    @SerialName("user_id")   val userId: String = "",
    @SerialName("joined_at") val joinedAt: String? = null,
    val profile: Profile? = null,
)

@Serializable
data class Category(
    val id: String = "",
    @SerialName("owner_id")   val ownerId: String? = null,
    val name: String = "",
    val icon: String = "more_horiz",
    val color: String = "#767D84",
    val kind: String = "expense",
    @SerialName("sort_order") val sortOrder: Int = 0,
)

@Serializable
data class Transaction(
    val id: String = "",
    @SerialName("user_id")     val userId: String = "",
    @SerialName("group_id")    val groupId: String? = null,
    val kind: String = "expense",
    @SerialName("category_id") val categoryId: String? = null,
    val amount: Int = 0,
    val memo: String? = null,
    @SerialName("occurred_on") val occurredOn: String = "",
    @SerialName("created_at")  val createdAt: String? = null,
    val category: Category? = null,
)

@Serializable
data class DailyRanking(
    @SerialName("group_id")      val groupId: String = "",
    @SerialName("user_id")       val userId: String = "",
    @SerialName("for_date")      val forDate: String = "",
    @SerialName("spent_amount")  val spentAmount: Int = 0,
    val rank: Int = 0,
    @SerialName("is_winner")     val isWinner: Boolean = false,
    @SerialName("is_loser")      val isLoser: Boolean = false,
    val profile: Profile? = null,
)

@Serializable
data class Costume(
    val id: String = "",
    val kind: String = "hat",
    val name: String = "",
    val price: Int = 0,
    val rarity: String = "common",
    val icon: String = "",
    @SerialName("is_shop")    val isShop: Boolean = true,
    @SerialName("is_special") val isSpecial: Boolean = false,
)

@Serializable
data class UserCostume(
    @SerialName("user_id")     val userId: String = "",
    @SerialName("costume_id")  val costumeId: String = "",
    @SerialName("acquired_at") val acquiredAt: String? = null,
    val costume: Costume? = null,
)

@Serializable
data class RouletteTicket(
    val id: String = "",
    @SerialName("user_id")    val userId: String = "",
    val source: String = "",
    val used: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class ChatRoom(
    val id: String = "",
    @SerialName("group_id")   val groupId: String = "",
    val name: String = "",
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class ChatMessage(
    val id: String = "",
    @SerialName("room_id")    val roomId: String = "",
    @SerialName("user_id")    val userId: String = "",
    val content: String = "",
    @SerialName("created_at") val createdAt: String? = null,
    val profile: Profile? = null,
)

@Serializable
data class Favorite(
    val id: String = "",
    @SerialName("user_id")     val userId: String = "",
    val kind: String = "expense",
    @SerialName("category_id") val categoryId: String? = null,
    val amount: Int? = null,
    val memo: String? = null,
    @SerialName("sort_order")  val sortOrder: Int = 0,
    @SerialName("created_at")  val createdAt: String? = null,
    val category: Category? = null,
)

// UI 전용 - 친구 프로필 + 랭킹 조합
data class RankedMember(
    val profile: Profile,
    val spentAmount: Int,
    val incomeAmount: Int = 0,
    val rank: Int,
    val isWinner: Boolean,
    val isLoser: Boolean,
    val shareValue: String,    // 퍼센트 or 금액 (설정에 따라)
    val isNotEntered: Boolean = false, // 오늘 거래 없음
)

// 룰렛 세그먼트
data class RouletteSegment(
    val id: String,
    val label: String,
    val emoji: String,
    val color: Long,
    val rewardType: String, // "hat", "outfit", "coins", "nothing"
    val rewardValue: String,
)

val DEFAULT_ROULETTE_SEGMENTS = listOf(
    RouletteSegment("r1", "코인 30",  "🪙", 0xFFBDD7F5, "coins",   "30"),
    RouletteSegment("r2", "코인 100", "🪙", 0xFFAEE9D0, "coins",   "100"),
    RouletteSegment("r3", "코인 50",  "🪙", 0xFFFFE4A0, "coins",   "50"),
    RouletteSegment("r4", "코인 10",  "🪙", 0xFFFFB3CE, "coins",   "10"),
    RouletteSegment("r5", "코인 200", "💰", 0xFFD4C5F9, "coins",   "200"),
    RouletteSegment("r6", "코인 500", "🏆", 0xFFFFB5A7, "coins",   "500"),
    RouletteSegment("r7", "코인 20",  "🪙", 0xFFB3E8F5, "coins",   "20"),
    RouletteSegment("r8", "꽝",       "💨", 0xFFDDE3EA, "nothing", "0"),
)

val DEFAULT_COSTUMES = listOf(
    // ── 헤어 악세서리 (샵 판매) ─────────────────────────────
    Costume("hat_crown",       "hat", "왕관",       800, "legendary", "👑", isSpecial = true),
    Costume("hat_bow",         "hat", "리본",        150, "common",    "🎀"),
    Costume("hat_cat",         "hat", "고양이 귀",   350, "rare",      "🐱"),
    Costume("hat_star",        "hat", "별 핀",       180, "common",    "⭐"),
    Costume("hat_flower_clip", "hat", "꽃 핀",       220, "rare",      "🌸"),
    Costume("hat_side_ribbon", "hat", "사이드 리본", 280, "rare",      "🎗️", isShop = false),
    Costume("hat_cap",         "hat", "캡 모자",     200, "common",    "🧢", isShop = false),
    Costume("hat_bunny",       "hat", "토끼 귀",     300, "rare",      "🐰"),
    Costume("hat_flower",      "hat", "꽃 화관",     450, "epic",      "💐"),
    Costume("hat_beanie",      "hat", "비니",        250, "common",    "🧶"),
    // ── 아우터 (샵 미판매, 하위 호환) ───────────────────────
    Costume("outfit_hoodie",  "outfit", "후드티",    400, "rare",    "🧥", isShop = false),
    Costume("outfit_suit",    "outfit", "정장",      600, "epic",    "👔", isShop = false),
    Costume("outfit_casual",  "outfit", "캐주얼",    350, "common",  "👕", isShop = false),
    Costume("outfit_beggar",  "outfit", "거지옷",      0, "common",  "🧺", isShop = false, isSpecial = true),
    Costume("outfit_sports",  "outfit", "스포츠웨어",380, "common",  "🏃", isShop = false),
    Costume("outfit_hanbok",  "outfit", "한복",      700, "epic",    "👘", isShop = false),
)
