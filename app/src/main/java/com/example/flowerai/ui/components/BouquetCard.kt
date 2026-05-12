package com.example.flowerai.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.flowerai.model.Bouquet

@Composable
fun BouquetCard(bouquet: Bouquet) {

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {

        Column(modifier = Modifier.padding(12.dp)) {

            Text(
                bouquet.name,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(bouquet.flowers)

            Spacer(modifier = Modifier.height(6.dp))

            Text(bouquet.price)

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = {}) {
                Text("Заказать")
            }
        }
    }
}