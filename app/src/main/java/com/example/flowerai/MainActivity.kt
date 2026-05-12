package com.example.flowerai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.flowerai.ui.MainScreen
import com.example.flowerai.ui.theme.FlowerAIConsultantTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            FlowerAIConsultantTheme {

                MainScreen()

            }

        }

    }

}