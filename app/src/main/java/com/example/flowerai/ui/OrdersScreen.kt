package com.example.flowerai.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.flowerai.R
import com.example.flowerai.network.CartItemResponse
import com.example.flowerai.network.RetrofitClient

private val OrdersBackground = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFF5F7),
        Color(0xFFFDEEF2),
        Color(0xFFF8EEF8)
    )
)

private val OrdersCardBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFF8FA),
        Color(0xFFFFF0F4),
        Color(0xFFFCEBF1)
    )
)

fun Bouquet.unitPriceLei(): Int =
    price.filter { it.isDigit() }.toIntOrNull() ?: 0

fun Bouquet.lineTotalLei(): Int = unitPriceLei() * quantity

@Composable
fun OrdersScreen(
    bouquets: List<Bouquet>,
    sessionId: String? = null,
    onIncreaseQuantity: (Bouquet) -> Unit,
    onDecreaseQuantity: (Bouquet) -> Unit,
    onRemoveFromCart: (Bouquet) -> Unit,
    onCheckoutClick: () -> Unit
) {
    var serverCartItems by remember(sessionId) { mutableStateOf<List<CartItemResponse>?>(null) }

    LaunchedEffect(sessionId) {
        serverCartItems = if (sessionId != null) {
            try {
                RetrofitClient.api.getCart(sessionId).items
            } catch (error: Exception) {
                null
            }
        } else {
            null
        }
    }

    val localCartItems = bouquets.filter { it.isInCart }
    val serverItems = serverCartItems.orEmpty()
    val serverIds = serverItems.map { it.bouquetId }.toSet()
    val localOnlyCartItems = localCartItems.filter { it.id !in serverIds }
    val total = serverItems.sumOf { it.price * it.quantity } + localOnlyCartItems.sumOf { it.lineTotalLei() }
    val totalCount = serverItems.sumOf { it.quantity } + localOnlyCartItems.sumOf { it.quantity }
    val isEmpty = serverCartItems != null && serverItems.isEmpty() && localOnlyCartItems.isEmpty() ||
        serverCartItems == null && localCartItems.isEmpty()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OrdersBackground)
    ) {
        SoftAnimatedBackdrop()

        if (isEmpty) {
            EmptyCartState()
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        OrdersHeaderCard(count = totalCount)
                    }

                    if (serverCartItems != null) {
                        items(serverItems, key = { "server-${it.bouquetId}" }) { item ->
                            ServerOrderItemCard(item = item)
                        }
                    }

                    items(localOnlyCartItems, key = { "local-${it.id}" }) { bouquet ->
                        OrderItemCard(
                            bouquet = bouquet,
                            onIncreaseQuantity = { onIncreaseQuantity(bouquet) },
                            onDecreaseQuantity = { onDecreaseQuantity(bouquet) },
                            onRemoveFromCart = { onRemoveFromCart(bouquet) }
                        )
                    }
                }

                CheckoutBar(
                    total = total,
                    onCheckoutClick = onCheckoutClick
                )
            }
        }
    }
}

@Composable
private fun ServerOrderItemCard(item: CartItemResponse) {
    val imageRes = when (item.imageResource) {
        "bouquet1" -> R.drawable.bouquet1
        "bouquet2" -> R.drawable.bouquet2
        "bouquet3" -> R.drawable.bouquet3
        "bouquet4" -> R.drawable.bouquet4
        "bouquet5_pink" -> R.drawable.bouquet5_pink
        "bouquet5_black" -> R.drawable.bouquet5_black
        "bouquet5_white" -> R.drawable.bouquet5_white
        else -> R.drawable.bouquet1
    }

    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.74f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(OrdersCardBrush)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = item.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(104.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFFFF1F4))
                    .padding(6.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF352730)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${item.price} ${item.currency}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF9A657C)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Количество: ${item.quantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF7B6871)
                )
            }

            Text(
                text = "${item.price * item.quantity} ${item.currency}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF352730)
            )
        }
    }
}

@Composable
private fun OrdersHeaderCard(count: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = Color.Transparent,
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFC55F82),
                            Color(0xFFA25679),
                            Color(0xFF755280)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Моя корзина",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Проверьте состав заказа перед оформлением.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.88f)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = "$count шт.",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderItemCard(
    bouquet: Bouquet,
    onIncreaseQuantity: () -> Unit,
    onDecreaseQuantity: () -> Unit,
    onRemoveFromCart: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.74f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(OrdersCardBrush)
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                OrderItemInfo(bouquet = bouquet)

                IconButton(
                    onClick = onRemoveFromCart
                ) {
                    Text(
                        text = "Удалить",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF9E7D8B),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    QuantityButton(
                        onClick = onDecreaseQuantity,
                        label = "-"
                    )
                    Text(
                        text = bouquet.quantity.toString(),
                        modifier = Modifier.padding(horizontal = 14.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF352730)
                    )
                    QuantityButton(
                        onClick = onIncreaseQuantity,
                        label = "+"
                    )
                }

                Text(
                    text = "${bouquet.lineTotalLei()} лей",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF352730)
                )
            }
        }
    }
}

@Composable
private fun RowScope.OrderItemInfo(
    bouquet: Bouquet
) {
    Row(
        modifier = Modifier.weight(1f),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Image(
            painter = painterResource(id = bouquet.imageForSelectedWrapping()),
            contentDescription = bouquet.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(104.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFFFF1F4))
                .padding(6.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = bouquet.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF352730)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = bouquet.price,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF9A657C)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Обёртка: ${bouquet.wrappingOption.title}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF7B6871)
            )
        }
    }
}

@Composable
private fun QuantityButton(
    onClick: () -> Unit,
    label: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.9f),
        border = BorderStroke(1.dp, Color(0xFFE8CBD6))
    ) {
        Box(
            modifier = Modifier.size(38.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5A3948)
            )
        }
    }
}

@Composable
private fun CheckoutBar(
    total: Int,
    onCheckoutClick: () -> Unit
) {
    Surface(
        color = Color.White.copy(alpha = 0.72f),
        shadowElevation = 10.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Общая сумма",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF352730)
                )
                Text(
                    text = "$total лей",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF352730)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onCheckoutClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF4B0C9),
                    contentColor = Color(0xFF4A2B36)
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Перейти к оформлению заказа",
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$total лей",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

@Composable
private fun EmptyCartState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color.White.copy(alpha = 0.78f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.84f))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = Color(0xFFE18AAE)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Корзина пока пустая",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF352730)
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OrdersScreenPreview() {
    OrdersScreen(
        bouquets = listOf(
            Bouquet(
                id = 1,
                name = "Noir Lavender",
                price = "830 лей",
                imageRes = R.drawable.bouquet1,
                isInCart = true,
                quantity = 2,
                wrappingOption = WrappingOption.Black
            )
        ),
        onIncreaseQuantity = {},
        onDecreaseQuantity = {},
        onRemoveFromCart = {},
        onCheckoutClick = {}
    )
}
