package com.example.flowerai.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
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

private val FavoritesBackground = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFF6F7),
        Color(0xFFFCEFF2),
        Color(0xFFF8EEF7)
    )
)

private val FavoritesHeroBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFFBE6483),
        Color(0xFFA55B7A),
        Color(0xFF865B84)
    )
)

private val FavoritesCardBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFF8FA),
        Color(0xFFFFF1F5),
        Color(0xFFFDECEF)
    )
)

@Composable
fun FavoritesScreen(
    bouquets: List<Bouquet>,
    onFavoriteToggle: (Bouquet) -> Unit,
    onDetailsClick: (Bouquet) -> Unit,
    onAddToCartClick: (Bouquet) -> Unit
) {
    val favorites = bouquets.filter { it.isFavorite }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FavoritesBackground)
    ) {
        SoftAnimatedBackdrop()

        if (favorites.isEmpty()) {
            EmptyFavoritesState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    FavoritesHeaderCard(count = favorites.size)
                }

                items(favorites, key = { it.id }) { bouquet ->
                    FavoriteBouquetCard(
                        bouquet = bouquet,
                        onFavoriteToggle = { onFavoriteToggle(bouquet) },
                        onDetailsClick = { onDetailsClick(bouquet) },
                        onAddToCartClick = { onAddToCartClick(bouquet) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoritesHeaderCard(count: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = Color.Transparent,
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .background(FavoritesHeroBrush)
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Избранное",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "Сохранённые букеты, к которым\nхочется вернуться.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.88f),
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color.White.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = "$count в избранном",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteBouquetCard(
    bouquet: Bouquet,
    onFavoriteToggle: () -> Unit,
    onDetailsClick: () -> Unit,
    onAddToCartClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.75f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 7.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(FavoritesCardBrush)
                .padding(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(94.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFFFF1F4)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = bouquet.imageRes),
                        contentDescription = bouquet.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp)
                    )

                    IconButton(
                        onClick = onFavoriteToggle,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .background(Color.White.copy(alpha = 0.96f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Убрать из избранного",
                            tint = Color(0xFFE35B8F)
                        )
                    }
                }

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
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDetailsClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.92f),
                        contentColor = Color(0xFF5B3343)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFEBC8D4)),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp
                    )
                ) {
                    Text(
                        text = "Подробнее",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = onAddToCartClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (bouquet.isInCart) Color(0xFFE8DDE3) else Color(0xFFF4B0C9),
                        contentColor = Color(0xFF4A2B36)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!bouquet.isInCart) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = if (bouquet.isInCart) "В корзине" else "В корзину",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyFavoritesState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White.copy(alpha = 0.76f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.82f))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color(0xFFE58DB0),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Пока ничего нет в избранном",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3A2932)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Сохраняйте понравившиеся букеты через сердечко в ассортименте.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF7A6871)
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun FavoritesScreenPreview() {
    FavoritesScreen(
        bouquets = listOf(
            Bouquet(
                id = 1,
                name = "Noir Lavender",
                price = "830 лей",
                imageRes = R.drawable.bouquet1,
                isFavorite = true
            )
        ),
        onFavoriteToggle = {},
        onDetailsClick = {},
        onAddToCartClick = {}
    )
}
