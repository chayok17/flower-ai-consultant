package com.example.flowerai.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.flowerai.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BouquetDetailsScreen(
    bouquet: Bouquet,
    onBackClick: () -> Unit,
    onFavoriteToggle: (Bouquet) -> Unit = {},
    onAddToCartClick: () -> Unit
) {
    var isFavorite by remember(bouquet.id, bouquet.isFavorite) { mutableStateOf(bouquet.isFavorite) }
    val images = bouquet.displayImages()
    var imageIndex by remember(bouquet.id, images.size) { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDF7FA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(430.dp)
            ) {
                Image(
                    painter = painterResource(id = images[imageIndex]),
                    contentDescription = bouquet.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (images.size > 1) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DetailVariantArrow(
                            onClick = {
                                imageIndex = if (imageIndex == 0) images.lastIndex else imageIndex - 1
                            },
                            left = true
                        )
                        DetailVariantArrow(
                            onClick = {
                                imageIndex = if (imageIndex == images.lastIndex) 0 else imageIndex + 1
                            },
                            left = false
                        )
                    }

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 26.dp),
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        images.forEachIndexed { index, _ ->
                            Box(
                                modifier = Modifier
                                    .size(if (index == imageIndex) 9.dp else 7.dp)
                                    .background(
                                        color = if (index == imageIndex) Color.White else Color.White.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.10f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.38f)
                                )
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassIconButton(
                        onClick = onBackClick,
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Назад",
                                tint = Color(0xFF2E232A)
                            )
                        }
                    )

                    GlassIconButton(
                        onClick = {
                            isFavorite = !isFavorite
                            onFavoriteToggle(bouquet)
                        },
                        icon = {
                            Icon(
                                imageVector = if (isFavorite) {
                                    Icons.Default.Favorite
                                } else {
                                    Icons.Default.FavoriteBorder
                                },
                                contentDescription = "Избранное",
                                tint = if (isFavorite) Color(0xFFFF6FA1) else Color(0xFF8E8289)
                            )
                        }
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    LuxuryTag(text = "Luxury Collection")

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = bouquet.name,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = bouquet.price,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFFD6E4)
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFDF7FA)
                )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 20.dp)
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val tags = bouquet.tags.ifEmpty { defaultBouquetTags(bouquet.id) }
                        tags.forEach { tag ->
                            SoftTag(tag)
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    DetailSectionTitle("О букете")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = bouquet.description.ifBlank { defaultBouquetDescription(bouquet.id) },
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF65555E)
                    )

                    Spacer(modifier = Modifier.height(22.dp))
                    HorizontalDivider(color = Color(0xFFE9DCE1))
                    Spacer(modifier = Modifier.height(22.dp))

                    DetailSectionTitle("Состав и символика")
                    Spacer(modifier = Modifier.height(12.dp))

                    val composition = bouquet.composition.ifEmpty { defaultBouquetComposition(bouquet.id) }
                    composition.forEachIndexed { index, item ->
                        FlowerMeaningCard(
                            flower = item.flower,
                            meaning = item.meaning
                        )
                        if (index != composition.lastIndex) {
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))
                    HorizontalDivider(color = Color(0xFFE9DCE1))
                    Spacer(modifier = Modifier.height(22.dp))

                    DetailSectionTitle("Подходит для")
                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val occasions = bouquet.occasions.ifEmpty { defaultBouquetOccasions(bouquet.id) }
                        occasions.forEach { occasion ->
                            OccasionChip(occasion)
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    Button(
                        onClick = onAddToCartClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .navigationBarsPadding(),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFC1D6),
                            contentColor = Color(0xFF4A2B36)
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Выбрать букет • ${bouquet.price}",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        }
    }
}

@Composable
fun GlassIconButton(
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.90f),
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier.size(44.dp),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
    }
}

@Composable
fun LuxuryTag(text: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color.White.copy(alpha = 0.20f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SoftTag(text: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color(0xFFFFEEF4)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            color = Color(0xFF8B5E74),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun DetailSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF31242C)
    )
}

@Composable
fun FlowerMeaningCard(
    flower: String,
    meaning: String
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3F7)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = flower,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF3B2B34)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = meaning,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6A5962)
            )
        }
    }
}

@Composable
fun OccasionChip(text: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFF7E5EC)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = Color(0xFF7A5365),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun BouquetDetailsScreenPreview() {
    BouquetDetailsScreen(
        bouquet = Bouquet(
            id = 1,
            name = "Noir Lavender",
            price = "830 лей",
            imageRes = R.drawable.bouquet1
        ),
        onBackClick = {},
        onAddToCartClick = {}
    )
}

@Composable
private fun DetailVariantArrow(
    onClick: () -> Unit,
    left: Boolean
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.84f),
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier.size(42.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (left) Icons.Default.KeyboardArrowLeft else Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF3B2B34),
                modifier = Modifier.size(26.dp)
            )
        }
    }
}
