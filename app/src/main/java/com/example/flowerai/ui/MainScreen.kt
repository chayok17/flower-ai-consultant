package com.example.flowerai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.flowerai.network.AuthResponse
import com.example.flowerai.network.CartItemResponse
import com.example.flowerai.network.ChatSummaryResponse
import com.example.flowerai.network.RetrofitClient
import com.example.flowerai.R
import kotlinx.coroutines.launch

sealed class Screen(val title: String) {
    object Chat : Screen("Flower Assistant 🌸")
    object Catalog : Screen("Ассортимент")
    object Orders : Screen("Мои заказы")
    object Checkout : Screen("Оформление")
    object Favorites : Screen("Избранное")
    object Settings : Screen("Настройки")
    object Details : Screen("Букет")
}

data class UserSession(
    val userId: Int,
    val username: String,
    val sessionId: String
)

private val DrawerBackground = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFF7FA),
        Color(0xFFFBEFF6),
        Color(0xFFF4EEF8)
    )
)

private val DrawerHeaderBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFFFFDDE9),
        Color(0xFFE9DDF8)
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val authPrefs = remember {
        context.getSharedPreferences("flower_auth", android.content.Context.MODE_PRIVATE)
    }
    var userSession by remember {
        mutableStateOf(
            authPrefs.getString("session_id", null)?.let { sessionId ->
                UserSession(
                    userId = authPrefs.getInt("user_id", 0),
                    username = authPrefs.getString("username", "user") ?: "user",
                    sessionId = sessionId
                )
            }
        )
    }

    fun saveAuth(response: AuthResponse) {
        authPrefs.edit()
            .putInt("user_id", response.userId)
            .putString("username", response.username)
            .putString("session_id", response.sessionId)
            .apply()
        userSession = UserSession(response.userId, response.username, response.sessionId)
    }

    fun logout() {
        authPrefs.edit().clear().apply()
        userSession = null
    }

    val currentUser = userSession
    if (currentUser == null) {
        AuthScreen(onAuthenticated = { saveAuth(it) })
        return
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    var selectedSessionId by remember(currentUser.sessionId) { mutableStateOf(currentUser.sessionId) }
    val chatSummaries = remember(currentUser.userId) { mutableStateListOf<ChatSummaryResponse>() }
    val bouquets = remember {
        mutableStateListOf(
            Bouquet(
                1,
                "Noir Lavender",
                "830 лей",
                com.example.flowerai.R.drawable.bouquet1,
                description = defaultBouquetDescription(1),
                tags = defaultBouquetTags(1),
                composition = defaultBouquetComposition(1),
                occasions = defaultBouquetOccasions(1)
            ),
            Bouquet(
                2,
                "Crimson Eclipse",
                "1005 лей",
                com.example.flowerai.R.drawable.bouquet2,
                description = defaultBouquetDescription(2),
                tags = defaultBouquetTags(2),
                composition = defaultBouquetComposition(2),
                occasions = defaultBouquetOccasions(2)
            ),
            Bouquet(
                3,
                "Blue Mist",
                "875 лей",
                com.example.flowerai.R.drawable.bouquet3,
                description = defaultBouquetDescription(3),
                tags = defaultBouquetTags(3),
                composition = defaultBouquetComposition(3),
                occasions = defaultBouquetOccasions(3)
            ),
            Bouquet(
                4,
                "Ghost Orchid",
                "1340 лей",
                com.example.flowerai.R.drawable.bouquet4,
                description = defaultBouquetDescription(4),
                tags = defaultBouquetTags(4),
                composition = defaultBouquetComposition(4),
                occasions = defaultBouquetOccasions(4)
            ),
            Bouquet(
                5,
                "Pink Reverie",
                "1005 лей",
                com.example.flowerai.R.drawable.bouquet5_pink,
                imageVariants = listOf(
                    com.example.flowerai.R.drawable.bouquet5_black,
                    com.example.flowerai.R.drawable.bouquet5_pink,
                    com.example.flowerai.R.drawable.bouquet5_white
                ),
                description = defaultBouquetDescription(5),
                tags = defaultBouquetTags(5),
                composition = defaultBouquetComposition(5),
                occasions = defaultBouquetOccasions(5)
            )
        )
    }

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Chat) }
    var selectedBouquet by remember { mutableStateOf<Bouquet?>(null) }
    var checkoutServerItems by remember { mutableStateOf<List<CartItemResponse>?>(null) }

    suspend fun refreshChats() {
        val chats = try {
            RetrofitClient.api.getChats(currentUser.userId).items
        } catch (error: Exception) {
            emptyList()
        }
        chatSummaries.clear()
        chatSummaries.addAll(chats)
        if (chatSummaries.none { it.sessionId == selectedSessionId }) {
            selectedSessionId = chatSummaries.firstOrNull()?.sessionId ?: currentUser.sessionId
        }
    }

    LaunchedEffect(currentUser.userId) {
        refreshChats()
    }

    fun updateBouquet(updated: Bouquet) {
        val index = bouquets.indexOfFirst { it.id == updated.id }
        if (index != -1) {
            bouquets[index] = updated
            if (selectedBouquet?.id == updated.id) {
                selectedBouquet = updated
            }
        }
    }

    fun toggleFavorite(bouquet: Bouquet) {
        updateBouquet(
            bouquet.copy(isFavorite = !bouquet.isFavorite)
        )
    }

    fun addToCart(bouquet: Bouquet) {
        if (!bouquet.isInCart) {
            updateBouquet(
                bouquet.copy(isInCart = true, quantity = 1)
            )
        }
    }

    fun removeFromCart(bouquet: Bouquet) {
        updateBouquet(
            bouquet.copy(isInCart = false, quantity = 1)
        )
    }

    fun increaseQuantity(bouquet: Bouquet) {
        updateBouquet(
            bouquet.copy(quantity = bouquet.quantity + 1, isInCart = true)
        )
    }

    fun decreaseQuantity(bouquet: Bouquet) {
        if (bouquet.quantity <= 1) {
            removeFromCart(bouquet)
        } else {
            updateBouquet(
                bouquet.copy(quantity = bouquet.quantity - 1)
            )
        }
    }

    fun updateWrapping(bouquet: Bouquet, wrappingOption: WrappingOption) {
        updateBouquet(
            bouquet.copy(wrappingOption = wrappingOption)
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.Transparent
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DrawerBackground)
                        .padding(vertical = 14.dp)
                ) {
                    DrawerHeader(username = currentUser.username)

                    Spacer(modifier = Modifier.height(14.dp))

                    DrawerSectionLabel(text = "Навигация")

                    DrawerItem(
                        text = "Ассистент",
                        icon = Icons.Default.Menu,
                        selected = currentScreen == Screen.Chat
                    ) {
                        currentScreen = Screen.Chat
                        navController.navigate(Routes.CHAT)
                        scope.launch { drawerState.close() }
                    }

                    DrawerItem(
                        text = "Ассортимент",
                        icon = Icons.Default.Home,
                        selected = currentScreen == Screen.Catalog
                    ) {
                        currentScreen = Screen.Catalog
                        navController.navigate(Routes.CATALOG)
                        scope.launch { drawerState.close() }
                    }

                    DrawerItem(
                        text = "Мои заказы",
                        icon = Icons.Default.ShoppingCart,
                        selected = currentScreen == Screen.Orders
                    ) {
                        currentScreen = Screen.Orders
                        navController.navigate(Routes.ORDERS)
                        scope.launch { drawerState.close() }
                    }

                    DrawerItem(
                        text = "Избранное",
                        icon = Icons.Default.Star,
                        selected = currentScreen == Screen.Favorites
                    ) {
                        currentScreen = Screen.Favorites
                        navController.navigate(Routes.FAVORITES)
                        scope.launch { drawerState.close() }
                    }

                    DrawerItem(
                        text = "Настройки",
                        icon = Icons.Default.Settings,
                        selected = currentScreen == Screen.Settings
                    ) {
                        currentScreen = Screen.Settings
                        navController.navigate(Routes.SETTINGS)
                        scope.launch { drawerState.close() }
                    }

                    DrawerItem(
                        text = "Выйти",
                        icon = Icons.Default.Settings,
                        selected = false
                    ) {
                        logout()
                        scope.launch { drawerState.close() }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    DrawerSectionLabel(text = "Диалоги")

                    DrawerItem(
                        text = "Новый чат",
                        icon = Icons.Default.Add,
                        selected = false
                    ) {
                        scope.launch {
                            val created = try {
                                RetrofitClient.api.createChat(currentUser.userId)
                            } catch (error: Exception) {
                                null
                            }
                            if (created != null) {
                                selectedSessionId = created.sessionId
                                refreshChats()
                                currentScreen = Screen.Chat
                                navController.navigate(Routes.CHAT)
                            }
                            drawerState.close()
                        }
                    }

                    chatSummaries.forEach { chat ->
                        ChatDrawerItem(
                            chat = chat,
                            selected = currentScreen == Screen.Chat && selectedSessionId == chat.sessionId,
                            onClick = {
                                selectedSessionId = chat.sessionId
                                currentScreen = Screen.Chat
                                navController.navigate(Routes.CHAT)
                                scope.launch { drawerState.close() }
                            },
                            onDelete = {
                                scope.launch {
                                    val deletedSessionId = chat.sessionId
                                    try {
                                        RetrofitClient.api.deleteChat(currentUser.userId, deletedSessionId)
                                    } catch (error: Exception) {
                                        null
                                    }
                                    refreshChats()
                                    if (selectedSessionId == deletedSessionId) {
                                        selectedSessionId = chatSummaries.firstOrNull()?.sessionId
                                            ?: currentUser.sessionId
                                        currentScreen = Screen.Chat
                                        navController.navigate(Routes.CHAT)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (currentScreen != Screen.Details && currentScreen != Screen.Checkout) {
                    TopAppBar(
                        title = {
                            Row {
                                Image(
                                    painter = painterResource(id = R.drawable.logo),
                                    contentDescription = "Flower AI",
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(currentScreen.title)
                            }
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    scope.launch { drawerState.open() }
                                }
                            ) {
                                Icon(Icons.Default.Menu, contentDescription = null)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color(0xFFFFF4F7)
                        )
                    )
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = Routes.CHAT
                ) {
                    composable(Routes.CHAT) {
                        currentScreen = Screen.Chat
                        ChatScreen(
                            sessionId = selectedSessionId,
                            onOpenCart = {
                                currentScreen = Screen.Orders
                                navController.navigate(Routes.ORDERS)
                            },
                            onMessageSent = {
                                scope.launch { refreshChats() }
                            }
                        )
                    }

                    composable(Routes.CATALOG) {
                        currentScreen = Screen.Catalog
                        CatalogScreen(
                            bouquets = bouquets,
                            onFavoriteToggle = { bouquet ->
                                toggleFavorite(bouquet)
                            },
                            onDetailsClick = { bouquet ->
                                selectedBouquet = bouquet
                                currentScreen = Screen.Details
                                navController.navigate(Routes.DETAILS)
                            }
                        )
                    }

                    composable(Routes.ORDERS) {
                        currentScreen = Screen.Orders
                        OrdersScreen(
                            bouquets = bouquets,
                            sessionId = selectedSessionId,
                            onIncreaseQuantity = { bouquet ->
                                increaseQuantity(bouquet)
                            },
                            onDecreaseQuantity = { bouquet ->
                                decreaseQuantity(bouquet)
                            },
                            onRemoveFromCart = { bouquet ->
                                removeFromCart(bouquet)
                            },
                            onCheckoutClick = {
                                scope.launch {
                                    checkoutServerItems = try {
                                        RetrofitClient.api.getCart(selectedSessionId).items
                                    } catch (error: Exception) {
                                        null
                                    }
                                    currentScreen = Screen.Checkout
                                    navController.navigate(Routes.CHECKOUT)
                                }
                            }
                        )
                    }

                    composable(Routes.CHECKOUT) {
                        currentScreen = Screen.Checkout
                        CheckoutScreen(
                            bouquets = bouquets,
                            serverCartItems = checkoutServerItems,
                            onBackClick = {
                                currentScreen = Screen.Orders
                                navController.popBackStack()
                            },
                            onWrappingSelected = { bouquet, option ->
                                updateWrapping(bouquet, option)
                            }
                        )
                    }

                    composable(Routes.FAVORITES) {
                        currentScreen = Screen.Favorites
                        FavoritesScreen(
                            bouquets = bouquets,
                            onFavoriteToggle = { bouquet ->
                                toggleFavorite(bouquet)
                            },
                            onDetailsClick = { bouquet ->
                                selectedBouquet = bouquet
                                currentScreen = Screen.Details
                                navController.navigate(Routes.DETAILS)
                            },
                            onAddToCartClick = { bouquet ->
                                addToCart(bouquet)
                            }
                        )
                    }

                    composable(Routes.SETTINGS) {
                        currentScreen = Screen.Settings
                        SettingsScreen()
                    }

                    composable(Routes.DETAILS) {
                        selectedBouquet?.let { bouquet ->
                            BouquetDetailsScreen(
                                bouquet = bouquet,
                                onBackClick = {
                                    currentScreen = Screen.Catalog
                                    navController.popBackStack()
                                },
                                onFavoriteToggle = { selected ->
                                    toggleFavorite(selected)
                                },
                                onAddToCartClick = {
                                    addToCart(bouquet)
                                    currentScreen = Screen.Orders
                                    navController.navigate(Routes.ORDERS)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerItem(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(24.dp),
        color = if (selected) Color(0xFFFFDDE8) else Color.White.copy(alpha = 0.34f),
        shadowElevation = if (selected) 5.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(34.dp),
                shape = CircleShape,
                color = if (selected) Color(0xFFC35D83) else Color.White.copy(alpha = 0.72f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (selected) Color.White else Color(0xFF7B6872),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) Color(0xFF3B2430) else Color(0xFF5F5159),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun DrawerHeader(username: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(30.dp),
        color = Color.Transparent,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .background(DrawerHeaderBrush)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(54.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.72f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Flower AI",
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Flower Assistant",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3D2A34),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "@$username",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF7E6672),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun DrawerSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF9B7B8B),
        modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp)
    )
}

@Composable
fun ChatDrawerItem(
    chat: ChatSummaryResponse,
    selected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(24.dp),
        color = if (selected) Color(0xFFFFDDE8) else Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 6.dp, top = 9.dp, bottom = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = null,
                tint = if (selected) Color(0xFF7A435E) else Color(0xFF71616B),
                modifier = Modifier.size(20.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chat.title.ifBlank { "Новый чат" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = Color(0xFF3C2D35),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (chat.messageCount > 0) {
                    Text(
                        text = "${chat.messageCount} сообщ.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF8C7782)
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Удалить чат",
                    tint = Color(0xFF9D7184),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainScreenPreview() {
    MainScreen()
}
