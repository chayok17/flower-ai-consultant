package com.example.flowerai.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.animation.animateContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.flowerai.R
import com.example.flowerai.network.BouquetResponse
import com.example.flowerai.network.ChatRequest
import com.example.flowerai.network.RetrofitClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val ScreenBackground = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFF4F5),
        Color(0xFFFDEEEF),
        Color(0xFFF7EEF7)
    )
)

private val HeroBackground = Brush.linearGradient(
    colors = listOf(
        Color(0xFFC8577B),
        Color(0xFF9C4D77),
        Color(0xFF6C4A7F)
    )
)

private val UserBubbleBackground = Brush.linearGradient(
    colors = listOf(
        Color(0xFFF8AFC6),
        Color(0xFFF48BB3)
    )
)

private val AssistantBubbleBackground = Brush.linearGradient(
    colors = listOf(
        Color(0xFFFFFBFC),
        Color(0xFFFFF0F4)
    )
)

private val BouquetCardBackground = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFF8FA),
        Color(0xFFFFF0F4),
        Color(0xFFFCEAF0)
    )
)

private fun welcomeChatMessage(): ChatMessage =
    ChatMessage(
        id = 1,
        text = "Здравствуйте 🌸\nПомогу подобрать букет по стилю, поводу и настроению.",
        isUser = false
    )

data class ChatMessage(
    val id: Int,
    val text: String,
    val isUser: Boolean,
    val bouquets: List<BouquetResponse> = emptyList(),
    val cartAdded: BouquetResponse? = null
)

data class ChatQuickBouquet(
    val id: Int,
    val name: String,
    val subtitle: String,
    val imageRes: Int
)

@Composable
fun ChatScreen(
    sessionId: String = "android",
    onOpenCart: () -> Unit = {},
    onMessageSent: () -> Unit = {}
) {
    var userInput by remember { mutableStateOf("") }
    var showCommandList by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val messages = remember {
        mutableStateListOf(
            welcomeChatMessage()
        )
    }

    LaunchedEffect(sessionId) {
        messages.clear()
        messages.add(welcomeChatMessage())

        val history = try {
            RetrofitClient.api.getHistory(sessionId)
        } catch (error: Exception) {
            null
        }

        if (history != null && history.messages.isNotEmpty()) {
            messages.clear()
            history.messages.forEachIndexed { index, item ->
                messages.add(
                    ChatMessage(
                        id = index + 1,
                        text = item.text,
                        isUser = item.role == "user"
                    )
                )
            }
        }
    }

    val quickBouquets = listOf(
        ChatQuickBouquet(
            id = 1,
            name = "Noir Lavender",
            subtitle = "Тёмная эстетика",
            imageRes = R.drawable.bouquet1
        ),
        ChatQuickBouquet(
            id = 2,
            name = "Crimson Eclipse",
            subtitle = "Выразительный подарок",
            imageRes = R.drawable.bouquet2
        ),
        ChatQuickBouquet(
            id = 3,
            name = "Blue Mist",
            subtitle = "Нежный стиль",
            imageRes = R.drawable.bouquet3
        ),
        ChatQuickBouquet(
            id = 5,
            name = "Pink Reverie",
            subtitle = "3 обертки",
            imageRes = R.drawable.bouquet5_pink
        )
    )

    val quickPrompts = listOf(
        "Подобрать букет для девушки",
        "Хочу нежный букет",
        "Нужен премиум букет",
        "Без роз, пожалуйста"
    )

    val hasUserMessages = messages.any { it.isUser }

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        val loadingMessageId = messages.size + 2

        messages.add(
            ChatMessage(
                id = messages.size + 1,
                text = trimmed,
                isUser = true
            )
        )

        messages.add(
            ChatMessage(
                id = loadingMessageId,
                text = "Думаю над букетом...",
                isUser = false
            )
        )

        userInput = ""

        coroutineScope.launch {
            val response = try {
                Result.success(
                    RetrofitClient.api.sendMessage(
                        ChatRequest(
                            text = trimmed,
                            sessionId = sessionId
                        )
                    )
                )
            } catch (error: Exception) {
                Result.failure(error)
            }

            val reply = response.getOrNull()?.reply
                ?: "Не получилось связаться с сервером. Проверь, что FastAPI запущен на 127.0.0.1:8000."
            val bouquets = response.getOrNull()?.bouquets.orEmpty()
            val cartAdded = response.getOrNull()?.cartAdded

            val loadingIndex = messages.indexOfFirst { it.id == loadingMessageId }
            if (loadingIndex >= 0) {
                messages[loadingIndex] = ChatMessage(
                    id = loadingMessageId,
                    text = reply,
                    isUser = false,
                    bouquets = bouquets,
                    cartAdded = cartAdded
                )
            }
            onMessageSent()
        }
    }

    LaunchedEffect(messages.size, messages.lastOrNull()?.text, showCommandList) {
        delay(80)
        val lastIndex = listState.layoutInfo.totalItemsCount - 1
        if (lastIndex >= 0) {
            listState.animateScrollToItem(lastIndex)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .imePadding()
    ) {
        SoftAnimatedBackdrop()

        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 12.dp,
                    bottom = 12.dp
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    ChatHeroCard()
                }

                item {
                    SectionTitle(
                        text = "Популярные букеты",
                        actionText = "AI-подбор"
                    )
                }

                item {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        quickBouquets.forEach { bouquet ->
                            WiderBouquetCard(
                                bouquet = bouquet,
                                onClick = {
                                    sendMessage("Покажи что-нибудь в стиле ${bouquet.name}")
                                }
                            )
                        }
                    }
                }

                if (!hasUserMessages) {
                    item {
                        SectionTitle(
                            text = "Быстрые сценарии",
                            actionText = "1 тап"
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            quickPrompts.forEach { prompt ->
                                QuickPromptChip(
                                    text = prompt,
                                    onClick = { sendMessage(prompt) }
                                )
                            }
                        }
                    }
                }

                item {
                    ChatCommandHeader(
                        expanded = showCommandList,
                        onToggle = { showCommandList = !showCommandList }
                    )
                }

                items(messages) { message ->
                    ChatBubble(
                        message = message,
                        onOpenCart = onOpenCart
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            CompactChatInputBar(
                value = userInput,
                onValueChange = { userInput = it },
                onSendClick = { sendMessage(userInput) }
            )

            Spacer(
                modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars)
            )
        }
    }
}

@Composable
private fun DecorativeBackdrop() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 60.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(220.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.22f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 4.dp)
                .size(140.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFD8E5).copy(alpha = 0.35f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 140.dp)
                .size(110.dp)
                .clip(CircleShape)
                .background(Color(0xFFE7D7F7).copy(alpha = 0.45f))
        )
    }
}

@Composable
private fun ChatHeroCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        shape = RoundedCornerShape(30.dp),
        color = Color.Transparent,
        shadowElevation = 10.dp
    ) {
        Box(
            modifier = Modifier
                .background(HeroBackground)
                .padding(22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Flower AI Consultant",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Подберём букет так, чтобы он выглядел дорого, уместно и в вашем стиле.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.88f)
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.18f)
                    ) {
                        Box(
                            modifier = Modifier.size(42.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HeroChip(text = "Премиум стиль")
                    HeroChip(text = "Ответ за секунды")
                }
            }
        }
    }
}

@Composable
private fun HeroChip(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color.White.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = Color.White
        )
    }
}

@Composable
fun SectionTitle(
    text: String,
    actionText: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF352730)
        )

        if (actionText != null) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Color.White.copy(alpha = 0.56f)
            ) {
                Text(
                    text = actionText,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF8C6276)
                )
            }
        }
    }
}

@Composable
private fun ChatCommandHeader(
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        color = Color.White.copy(alpha = 0.6f),
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Диалог с флористом",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF3D2432)
                )

                Surface(
                    onClick = onToggle,
                    shape = RoundedCornerShape(999.dp),
                    color = if (expanded) Color(0xFFF7D5E2) else Color.White.copy(alpha = 0.72f),
                    border = BorderStroke(1.dp, Color(0xFFE7C1D0))
                ) {
                    Text(
                        text = if (expanded) "Скрыть" else "Команды",
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF8B5570)
                    )
                }
            }

            if (expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    CommandHint("Подобрать букет", "для мамы на день рождения до 1000 лей")
                    CommandHint("Изменить вариант", "без роз, сделай нежнее, поменяй упаковку")
                    CommandHint("Выбрать и купить", "мне нравится первый вариант, добавь в корзину")
                    CommandHint("Уход за цветами", "как сохранить розы, сколько простоит Blue Mist")
                    CommandHint("Если не знаете", "хз, без предпочтений, на вкус флориста")
                }
            }
        }
    }
}

@Composable
private fun CommandHint(
    title: String,
    example: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.58f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4B303D)
            )
            Text(
                text = example,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF7C6872),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun WiderBouquetCard(
    bouquet: ChatQuickBouquet,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(168.dp)
            .height(204.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.65f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BouquetCardBackground)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(138.dp)
                    .padding(horizontal = 6.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFFF3F6),
                                Color(0xFFFFEDF1)
                            )
                        )
                    )
            ) {
                Image(
                    painter = painterResource(id = bouquet.imageRes),
                    contentDescription = bouquet.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(1.45f)
                        .padding(horizontal = 0.dp, vertical = 2.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = bouquet.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF352730),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = bouquet.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF8B7A83),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun QuickPromptChip(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.78f),
        border = BorderStroke(1.dp, Color(0xFFF0C7D5))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Color(0xFF9D617B)
            )
            Text(
                text = text,
                color = Color(0xFF7A5668),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    onOpenCart: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!message.isUser) {
            AvatarBadge(
                text = "AI",
                background = Color(0xFF7C5075)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 24.dp,
                topEnd = 24.dp,
                bottomStart = if (message.isUser) 24.dp else 10.dp,
                bottomEnd = if (message.isUser) 10.dp else 24.dp
            ),
            color = Color.Transparent,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth(
                if (!message.isUser && message.bouquets.isNotEmpty()) 0.92f else 0.78f
            )
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = if (message.isUser) UserBubbleBackground else AssistantBubbleBackground
                    )
                    .border(
                        width = 1.dp,
                        color = if (message.isUser) {
                            Color(0xFFF4A1C0).copy(alpha = 0.9f)
                        } else {
                            Color.White.copy(alpha = 0.85f)
                        },
                        shape = RoundedCornerShape(
                            topStart = 24.dp,
                            topEnd = 24.dp,
                            bottomStart = if (message.isUser) 24.dp else 10.dp,
                            bottomEnd = if (message.isUser) 10.dp else 24.dp
                        )
                    )
                    .padding(horizontal = 15.dp, vertical = 13.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (message.isUser) Color(0xFF4D2536) else Color(0xFF34262F)
                    )

                    if (!message.isUser && message.bouquets.isNotEmpty()) {
                        ChatBouquetStrip(bouquets = message.bouquets)
                    }

                    if (!message.isUser && message.cartAdded != null) {
                        CartAddedAction(onOpenCart = onOpenCart)
                    }
                }
            }
        }

        if (message.isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            AvatarBadge(
                text = "Вы",
                background = Color(0xFFE58DB0)
            )
        }
    }
}

@Composable
private fun CartAddedAction(onOpenCart: () -> Unit) {
    Surface(
        onClick = onOpenCart,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFFFEEF5),
        border = BorderStroke(1.dp, Color(0xFFE6B7CB))
    ) {
        Text(
            text = "Открыть корзину",
            modifier = Modifier.padding(vertical = 12.dp),
            textAlign = TextAlign.Center,
            color = Color(0xFF9C4D77),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ChatBouquetStrip(bouquets: List<BouquetResponse>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Готовые букеты",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF6D4A5C)
        )

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            bouquets.forEach { bouquet ->
                ChatBouquetResultCard(bouquet = bouquet)
            }
        }
    }
}

@Composable
private fun ChatBouquetResultCard(bouquet: BouquetResponse) {
    Card(
        modifier = Modifier.width(196.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFBFC)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFFFFF3F7),
                                Color(0xFFF3EAF8)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = bouquetImageRes(bouquet.imageResource)),
                    contentDescription = bouquet.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = bouquet.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3A2732),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${bouquet.price} ${bouquet.currency}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF9C4D77)
                )

                if (bouquet.colors.isNotEmpty()) {
                    Text(
                        text = bouquet.colors.take(3).joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF7D6A74),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private fun bouquetImageRes(imageResource: String): Int {
    return when (imageResource) {
        "bouquet1" -> R.drawable.bouquet1
        "bouquet2" -> R.drawable.bouquet2
        "bouquet3" -> R.drawable.bouquet3
        "bouquet4" -> R.drawable.bouquet4
        "bouquet5_pink" -> R.drawable.bouquet5_pink
        "bouquet5_black" -> R.drawable.bouquet5_black
        "bouquet5_white" -> R.drawable.bouquet5_white
        else -> R.drawable.bouquet1
    }
}

@Composable
private fun AvatarBadge(
    text: String,
    background: Color
) {
    Surface(
        modifier = Modifier.size(34.dp),
        shape = CircleShape,
        color = background,
        shadowElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CompactChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Surface(
        color = Color.Transparent,
        shadowElevation = 0.dp
    ) {
        Column {
            HorizontalDivider(
                color = Color.White.copy(alpha = 0.5f),
                thickness = DividerDefaults.Thickness
            )

            Surface(
                color = Color.White.copy(alpha = 0.56f),
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                text = "Опишите, какой букет нужен...",
                                color = Color(0xFF9A8C93)
                            )
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.94f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color(0xFF8B5E74)
                        ),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Send
                        ),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Surface(
                        onClick = onSendClick,
                        shape = CircleShape,
                        color = Color.Transparent,
                        shadowElevation = 6.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFFCC587D),
                                            Color(0xFF944F79)
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Отправить",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ChatScreenPreview() {
    ChatScreen()
}
