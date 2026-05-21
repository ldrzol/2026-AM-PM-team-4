package com.mintly.app.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.mintly.app.data.model.ChatMessage
import com.mintly.app.data.repository.AuthRepository
import com.mintly.app.data.repository.ChatRepository
import com.mintly.app.ui.components.거지방TopBar
import com.mintly.app.ui.home.toAvatarFace
import com.mintly.app.ui.components.MiniAvatar
import com.mintly.app.ui.theme.거지방Colors
import com.mintly.app.ui.theme.Shape14
import com.mintly.app.ui.theme.ShapePill
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.time.OffsetDateTime
import javax.inject.Inject

// ─── ChatViewModel ─────────────────────────────────────────
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepo: ChatRepository,
    private val authRepo: AuthRepository,
) : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _myUserId = MutableStateFlow<String?>(null)
    val myUserId: StateFlow<String?> = _myUserId.asStateFlow()

    private var currentRoomId: String? = null

    fun init(groupId: String, groupName: String) {
        viewModelScope.launch {
            _myUserId.value = authRepo.currentUserId()
            val room = chatRepo.getOrCreateChatRoom(groupId, groupName)
            currentRoomId = room.id
            _messages.value = chatRepo.getMessages(room.id)

            // 실시간 구독
            chatRepo.messageFlow(room.id).collect { msg ->
                _messages.value = _messages.value + msg
            }
        }
    }

    fun send(content: String) {
        val roomId = currentRoomId ?: return
        if (content.isBlank()) return
        viewModelScope.launch {
            chatRepo.sendMessage(roomId, content.trim())
            _messages.value = chatRepo.getMessages(roomId)
        }
    }
}

// ─── ChatScreen ────────────────────────────────────────────
@Composable
fun ChatScreen(
    groupId: String,
    groupName: String,
    onBack: () -> Unit,
    vm: ChatViewModel = hiltViewModel(),
) {
    val messages by vm.messages.collectAsStateWithLifecycle()
    val myUserId by vm.myUserId.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var input by remember { mutableStateOf("") }

    LaunchedEffect(groupId) { vm.init(groupId, groupName) }
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    Scaffold(
        topBar = { 거지방TopBar(title = groupName, onBack = onBack) },
        containerColor = 거지방Colors.Gray50,
        bottomBar = {
            Surface(color = Color.White, shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier.weight(1f),
                        shape = ShapePill,
                        placeholder = { Text("메시지 입력...") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = 거지방Colors.Mint400,
                            unfocusedBorderColor = 거지방Colors.Gray200,
                        ),
                    )
                    FloatingActionButton(
                        onClick = { vm.send(input); input = "" },
                        modifier = Modifier.size(44.dp),
                        containerColor = 거지방Colors.Mint400,
                        contentColor   = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp),
                    ) {
                        Icon(Icons.Rounded.Send, null, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatBubble(msg = msg, isMe = msg.userId == myUserId)
            }
        }
    }
}

@Composable
private fun ChatBubble(msg: ChatMessage, isMe: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (!isMe) {
            msg.profile?.let { p ->
                MiniAvatar(
                    size = 32.dp,
                    color = p.avatarColor,
                    face = p.avatarFace.toAvatarFace(),
                    modifier = Modifier.padding(end = 6.dp),
                )
            }
        }

        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            if (!isMe) {
                Text(
                    msg.profile?.displayName ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = 거지방Colors.Gray500,
                )
            }
            Surface(
                shape = if (isMe)
                    androidx.compose.foundation.shape.RoundedCornerShape(14.dp, 14.dp, 2.dp, 14.dp)
                else
                    androidx.compose.foundation.shape.RoundedCornerShape(14.dp, 14.dp, 14.dp, 2.dp),
                color = if (isMe) 거지방Colors.Mint400 else Color.White,
                shadowElevation = 1.dp,
            ) {
                Text(
                    msg.content,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isMe) Color.White else 거지방Colors.Gray900,
                )
            }
            msg.createdAt?.let { ts ->
                Text(
                    formatTime(ts),
                    style = MaterialTheme.typography.labelSmall,
                    color = 거지방Colors.Gray400,
                    fontSize = 10.sp,
                )
            }
        }
    }
}

private fun formatTime(timestamp: String): String = runCatching {
    val odt = OffsetDateTime.parse(timestamp)
    odt.format(DateTimeFormatter.ofPattern("HH:mm"))
}.getOrElse { "" }
