package com.example.flowerai.ui

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.flowerai.R

private val CatalogBackground = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFF5F6),
        Color(0xFFFDEEF1),
        Color(0xFFF8EEF8)
    )
)

private val CatalogHeroBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFFC55F82),
        Color(0xFFA25479),
        Color(0xFF6D4F81)
    )
)

private val CardSurfaceBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFF8FA),
        Color(0xFFFFF1F5),
        Color(0xFFFDEAF0)
    )
)

private enum class BouquetFilter(val title: String) {
    All("Все"),
    Premium("Премиум"),
    Gentle("Нежные"),
    Dark("Тёмные")
}

@Composable
fun CatalogScreen(
    bouquets: List<Bouquet>,
    onFavoriteToggle: (Bouquet) -> Unit,
    onDetailsClick: (Bouquet) -> Unit
) {
    var activeFilter by remember { mutableStateOf(BouquetFilter.All) }
    val filteredBouquets = bouquets.filter { bouquet ->
        when (activeFilter) {
            BouquetFilter.All -> true
            BouquetFilter.Premium -> bouquet.tags.any { tag ->
                tag.contains("премиум", ignoreCase = true) ||
                    tag.contains("статус", ignoreCase = true) ||
                    tag.contains("элегант", ignoreCase = true)
            }
            BouquetFilter.Gentle -> bouquet.tags.any { tag ->
                tag.contains("неж", ignoreCase = true) ||
                    tag.contains("пастель", ignoreCase = true) ||
                    tag.contains("воздуш", ignoreCase = true) ||
                    tag.contains("весенн", ignoreCase = true)
            }
            BouquetFilter.Dark -> bouquet.tags.any { tag ->
                tag.contains("тем", ignoreCase = true) ||
                    tag.contains("марсала", ignoreCase = true) ||
                    tag.contains("выраз", ignoreCase = true) ||
                    tag.contains("драм", ignoreCase = true)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CatalogBackground)
    ) {
        SoftAnimatedBackdrop()

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 12.dp,
                end = 12.dp,
                top = 12.dp,
                bottom = 18.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                CatalogHero(
                    count = filteredBouquets.size
                )
            }

            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                FilterRow(
                    activeFilter = activeFilter,
                    onFilterSelected = { activeFilter = it }
                )
            }

            items(filteredBouquets, key = { it.id }) { bouquet ->
                BouquetCard(
                    bouquet = bouquet,
                    onFavoriteClick = { onFavoriteToggle(bouquet) },
                    onDetailsClick = {
                        onDetailsClick(bouquet)
                    }
                )
            }
        }
    }
}

@Composable
private fun CatalogBackdrop() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 10.dp)
                .size(180.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.22f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(120.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFD5E2).copy(alpha = 0.3f))
        )
    }
}

@Composable
private fun CatalogHero(count: Int) {
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
                .background(CatalogHeroBrush)
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Ассортимент",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Выразительные букеты с тёмной, нежной и премиальной эстетикой.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HeroStatChip(text = "$count коллекции")
                    HeroStatChip(text = "Ручная сборка")
                }
            }
        }
    }
}

@Composable
private fun HeroStatChip(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color.White.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
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
private fun FilterRow(
    activeFilter: BouquetFilter,
    onFilterSelected: (BouquetFilter) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BouquetFilter.entries.forEach { filter ->
            FilterChip(
                text = filter.title,
                isActive = activeFilter == filter,
                onClick = { onFilterSelected(filter) }
            )
        }
    }
}

@Composable
private fun FilterChip(
    text: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = if (isActive) Color(0xFFF8D7E3) else Color.White.copy(alpha = 0.72f),
        border = BorderStroke(
            1.dp,
            if (isActive) Color(0xFFE7AEC2) else Color.White.copy(alpha = 0.75f)
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (isActive) Color(0xFF7A4B61) else Color(0xFF8C7480)
        )
    }
}

@Composable
fun BouquetCard(
    bouquet: Bouquet,
    onFavoriteClick: () -> Unit,
    onDetailsClick: () -> Unit
) {
    val images = bouquet.displayImages()
    var imageIndex by remember(bouquet.id, images.size) { mutableStateOf(0) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .width(164.dp),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .background(CardSurfaceBrush)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (images.size > 1) 190.dp else 180.dp)
                    .padding(horizontal = 8.dp, vertical = 8.dp)
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
                    painter = painterResource(id = images[imageIndex]),
                    contentDescription = bouquet.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(0.dp)
                )

                if (images.size > 1) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VariantArrowButton(
                            onClick = {
                                imageIndex = if (imageIndex == 0) images.lastIndex else imageIndex - 1
                            },
                            left = true
                        )
                        VariantArrowButton(
                            onClick = {
                                imageIndex = if (imageIndex == images.lastIndex) 0 else imageIndex + 1
                            },
                            left = false
                        )
                    }

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        images.forEachIndexed { index, _ ->
                            Box(
                                modifier = Modifier
                                    .size(if (index == imageIndex) 7.dp else 5.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (index == imageIndex) Color(0xFFC35D83)
                                        else Color.White.copy(alpha = 0.78f)
                                    )
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.94f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (bouquet.isFavorite) {
                            Icons.Default.Favorite
                        } else {
                            Icons.Default.FavoriteBorder
                        },
                        contentDescription = "Избранное",
                        tint = if (bouquet.isFavorite) {
                            Color(0xFFE35B8F)
                        } else {
                            Color(0xFFB9B0B5)
                        }
                    )
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = bouquet.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF352730),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = bouquet.price,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF9A657C)
                )

                if (bouquet.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = bouquet.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF7D6A74),
                        maxLines = if (images.size > 1) 2 else 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDetailsClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF5B2CA),
                        contentColor = Color(0xFF4A2B36)
                    ),
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
            }
        }
    }
}

@Composable
private fun VariantArrowButton(
    onClick: () -> Unit,
    left: Boolean
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(34.dp)
            .background(Color.White.copy(alpha = 0.86f), CircleShape)
    ) {
        Icon(
            imageVector = if (left) Icons.Default.KeyboardArrowLeft else Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFF7A5365),
            modifier = Modifier.size(22.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CatalogScreenPreview() {
    CatalogScreen(
        bouquets = listOf(
            Bouquet(1, "Noir Lavender", "830 лей", R.drawable.bouquet1),
            Bouquet(2, "Crimson Eclipse", "1005 лей", R.drawable.bouquet2)
        ),
        onFavoriteToggle = {},
        onDetailsClick = {}
    )
}
