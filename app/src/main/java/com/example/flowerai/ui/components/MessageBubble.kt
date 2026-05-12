package com.example.flowerai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.flowerai.model.Message

@Composable
fun MessageBubble(message: Message) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement =
            if (message.isUser) Arrangement.End else Arrangement.Start
    ) {

        Box(
            modifier = Modifier
                .widthIn(max = 260.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(22.dp)
                )
                .background(
                    color =
                        if (message.isUser)
                            Color(0xFFFFE4EC)
                        else
                            Color(0xFFF2F2F2),
                    shape = RoundedCornerShape(22.dp)
                )
                .padding(12.dp)
        ) {

            if (message.text != null) {

                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium
                )

            }

            if (message.bouquet != null) {

                Column {

                    Text(
                        text = message.bouquet.name,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(message.bouquet.flowers)

                    Spacer(modifier = Modifier.height(4.dp))

                    Text("Цена: ${message.bouquet.price}")

                }

            }

        }

    }

}