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
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

private val CheckoutBackground = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFF5F7),
        Color(0xFFFDF0F3),
        Color(0xFFF8EEF8)
    )
)

private val CheckoutCardBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFF9FB),
        Color(0xFFFFF0F5),
        Color(0xFFFCEAF2)
    )
)

@Composable
fun CheckoutScreen(
    bouquets: List<Bouquet>,
    serverCartItems: List<CartItemResponse>? = null,
    onBackClick: () -> Unit,
    onWrappingSelected: (Bouquet, WrappingOption) -> Unit
) {
    val cartItems = bouquets.filter { it.isInCart }
    val serverItems = serverCartItems.orEmpty()
    val serverIds = serverItems.map { it.bouquetId }.toSet()
    val localOnlyCartItems = cartItems.filter { it.id !in serverIds }
    val total = serverItems.sumOf { it.price * it.quantity } + localOnlyCartItems.sumOf { it.lineTotalLei() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CheckoutBackground)
    ) {
        SoftAnimatedBackdrop()

        Column(modifier = Modifier.fillMaxSize()) {
            CheckoutTopBar(onBackClick = onBackClick)

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Оформление заказа",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF352730)
                    )
                }

                item {
                    Text(
                        text = "Для каждого букета можно заранее выбрать цвет обёртки.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF75656E)
                    )
                }

                if (serverCartItems != null) {
                    items(serverItems, key = { "server-${it.bouquetId}" }) { item ->
                        val bouquet = bouquets.firstOrNull { it.id == item.bouquetId }
                        CheckoutServerBouquetCard(
                            item = item,
                            bouquet = bouquet,
                            onWrappingSelected = { option ->
                                if (bouquet != null) {
                                    onWrappingSelected(bouquet, option)
                                }
                            }
                        )
                    }
                }

                items(localOnlyCartItems, key = { "local-${it.id}" }) { bouquet ->
                    CheckoutBouquetCard(
                        bouquet = bouquet,
                        onWrappingSelected = { option ->
                            onWrappingSelected(bouquet, option)
                        }
                    )
                }
            }

            Surface(
                color = Color.White.copy(alpha = 0.76f),
                shadowElevation = 10.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Итого",
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
                        onClick = { },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF4B0C9),
                            contentColor = Color(0xFF4A2B36)
                        )
                    ) {
                        Text(
                            text = "Подтвердить заказ",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        }
    }
}

@Composable
private fun CheckoutServerBouquetCard(
    item: CartItemResponse,
    bouquet: Bouquet?,
    onWrappingSelected: (WrappingOption) -> Unit
) {
    val fallbackImageRes = when (item.imageResource) {
        "bouquet1" -> R.drawable.bouquet1
        "bouquet2" -> R.drawable.bouquet2
        "bouquet3" -> R.drawable.bouquet3
        "bouquet4" -> R.drawable.bouquet4
        "bouquet5_pink" -> R.drawable.bouquet5_pink
        "bouquet5_black" -> R.drawable.bouquet5_black
        "bouquet5_white" -> R.drawable.bouquet5_white
        else -> R.drawable.bouquet1
    }
    val imageRes = bouquet?.imageForSelectedWrapping() ?: fallbackImageRes

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.82f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CheckoutCardBrush)
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(226.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFFF3F7),
                                Color(0xFFFFEAF1)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = item.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF352730)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Количество: ${item.quantity}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF7B6871)
                    )
                    if (bouquet != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Обёртка: ${bouquet.wrappingOption.title}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9A657C)
                        )
                    }
                }

                Text(
                    text = "${item.price * item.quantity} ${item.currency}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF352730)
                )
            }

            if (bouquet != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Выберите обёртку",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF352730)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Color.White.copy(alpha = 0.42f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.58f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        WrappingOption.entries.forEach { option ->
                            WrappingChip(
                                option = option,
                                selected = bouquet.wrappingOption == option,
                                onClick = { onWrappingSelected(option) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckoutTopBar(
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            onClick = onBackClick,
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.9f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.84f))
        ) {
            Box(
                modifier = Modifier.size(42.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = Color(0xFF4E3542)
                )
            }
        }

        Text(
            text = "Назад к корзине",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF4E3542)
        )
    }
}

@Composable
private fun CheckoutBouquetCard(
    bouquet: Bouquet,
    onWrappingSelected: (WrappingOption) -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.82f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CheckoutCardBrush)
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(232.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFFFF2F5)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = bouquet.imageForSelectedWrapping()),
                    contentDescription = bouquet.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = bouquet.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF352730)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${bouquet.quantity} шт. • ${bouquet.lineTotalLei()} лей",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF8C6879)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Выберите обёртку",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF352730)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color.White.copy(alpha = 0.42f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.58f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    WrappingOption.entries.forEach { option ->
                        WrappingChip(
                            option = option,
                            selected = bouquet.wrappingOption == option,
                            onClick = { onWrappingSelected(option) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.WrappingChip(
    option: WrappingOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(17.dp),
        color = if (selected) Color(0xFFF5B1CC) else Color(0xFFFFF8FA).copy(alpha = 0.72f),
        border = BorderStroke(
            1.dp,
            if (selected) Color(0xFFD976A0) else Color.White.copy(alpha = 0.78f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 9.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.55f))
                    .padding(3.dp)
                    .clip(CircleShape)
                    .background(Color(option.colorHex)),
                contentAlignment = Alignment.Center
            ) {
                if (option == WrappingOption.White) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }

            Spacer(modifier = Modifier.height(7.dp))

            Text(
                text = option.title,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) Color(0xFF5A263B) else Color(0xFF6C5862),
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CheckoutScreenPreview() {
    CheckoutScreen(
        bouquets = listOf(
            Bouquet(
                id = 1,
                name = "Noir Lavender",
                price = "830 лей",
                imageRes = R.drawable.bouquet1,
                isInCart = true,
                quantity = 2,
                wrappingOption = WrappingOption.SoftPink
            )
        ),
        onBackClick = {},
        onWrappingSelected = { _, _ -> }
    )
}
