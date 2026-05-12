package com.example.flowerai.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.flowerai.R
import com.example.flowerai.network.AuthRequest
import com.example.flowerai.network.AuthResponse
import com.example.flowerai.network.RetrofitClient
import kotlinx.coroutines.launch

private val AuthBackground = Brush.verticalGradient(
    listOf(
        Color(0xFFFFF4F7),
        Color(0xFFF8E7F0),
        Color(0xFFEFE6FF)
    )
)

private val AccentGradient = Brush.linearGradient(
    listOf(
        Color(0xFFC45C83),
        Color(0xFFE7A2C7),
        Color(0xFFA989E8)
    )
)

@Composable
fun AuthScreen(
    onAuthenticated: (AuthResponse) -> Unit
) {
    var showWelcome by remember { mutableStateOf(true) }
    var isLoginMode by remember { mutableStateOf(true) }

    if (showWelcome) {
        WelcomeScreen(
            onCreateAccount = {
                isLoginMode = false
                showWelcome = false
            },
            onLogin = {
                isLoginMode = true
                showWelcome = false
            }
        )
    } else {
        LoginRegisterScreen(
            isLoginMode = isLoginMode,
            onModeChange = { isLoginMode = it },
            onBack = { showWelcome = true },
            onAuthenticated = onAuthenticated
        )
    }
}

@Composable
private fun WelcomeScreen(
    onCreateAccount: () -> Unit,
    onLogin: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthBackground)
            .padding(24.dp)
    ) {
        AuthDecor()

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(18.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                LogoMark(size = 148)

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Flower AI",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF332631),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "умный флорист для подарков, поводов и настроения",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF7A6070),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp, start = 12.dp, end = 12.dp)
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                color = Color.White.copy(alpha = 0.76f),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GradientButton(
                        text = "Создать аккаунт",
                        onClick = onCreateAccount
                    )
                    Surface(
                        onClick = onLogin,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        color = Color.White.copy(alpha = 0.74f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE7C9DA))
                    ) {
                        Text(
                            text = "Уже есть аккаунт",
                            modifier = Modifier.padding(vertical = 15.dp),
                            textAlign = TextAlign.Center,
                            color = Color(0xFF80566B),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginRegisterScreen(
    isLoginMode: Boolean,
    onModeChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    onAuthenticated: (AuthResponse) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthBackground)
            .padding(22.dp),
        contentAlignment = Alignment.Center
    ) {
        AuthDecor()

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(34.dp),
            color = Color.White.copy(alpha = 0.9f),
            shadowElevation = 10.dp
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LogoMark(size = 88)

                Text(
                    text = if (isLoginMode) "Добро пожаловать" else "Создайте аккаунт",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF352630),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Ник от 6 символов. Пароль от 6 символов: буква, цифра и знак вроде !",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF806B75),
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    label = { Text("Ник") }
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    label = { Text("Пароль") }
                )

                if (message.isNotBlank()) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFB9577B),
                        textAlign = TextAlign.Center
                    )
                }

                GradientButton(
                    text = if (isLoading) "Подождите..." else if (isLoginMode) "Войти" else "Создать аккаунт",
                    enabled = !isLoading,
                    onClick = {
                        val validation = validateCredentials(username, password)
                        if (validation != null) {
                            message = validation
                            return@GradientButton
                        }

                        isLoading = true
                        message = ""
                        scope.launch {
                            val response = try {
                                if (isLoginMode) {
                                    RetrofitClient.api.login(AuthRequest(username.trim(), password))
                                } else {
                                    RetrofitClient.api.register(AuthRequest(username.trim(), password))
                                }
                            } catch (error: Exception) {
                                message = "Не получилось войти. Проверьте данные и сервер."
                                null
                            }

                            isLoading = false
                            if (response != null) {
                                onAuthenticated(response)
                            }
                        }
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isLoginMode) "Нет аккаунта?" else "Уже есть аккаунт?",
                        color = Color(0xFF7D6873)
                    )
                    TextButton(
                        onClick = {
                            onModeChange(!isLoginMode)
                            message = ""
                        }
                    ) {
                        Text(if (isLoginMode) "Регистрация" else "Войти")
                    }
                }

                TextButton(onClick = onBack) {
                    Text("Назад")
                }
            }
        }
    }
}

@Composable
private fun GradientButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color(0xFFE5D7DF)
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AccentGradient, RoundedCornerShape(22.dp))
                .padding(vertical = 15.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun LogoMark(size: Int) {
    Surface(
        modifier = Modifier.size(size.dp),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.72f),
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier.padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Flower AI",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun AuthDecor() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 36.dp)
                .size(138.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.35f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(96.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFC7DC).copy(alpha = 0.4f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 116.dp)
                .size(76.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xFFD9C5FF).copy(alpha = 0.48f))
                .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(28.dp))
        )
    }
}

private fun validateCredentials(username: String, password: String): String? {
    val nick = username.trim()
    if (nick.length < 6) {
        return "Ник должен быть минимум 6 символов."
    }
    if (!nick.all { it.isLetterOrDigit() || it == '_' }) {
        return "В нике используйте буквы, цифры или _."
    }
    if (password.length < 6) {
        return "Пароль должен быть минимум 6 символов."
    }
    if (!password.any { it.isLetter() }) {
        return "Добавьте в пароль хотя бы одну букву."
    }
    if (!password.any { it.isDigit() }) {
        return "Добавьте в пароль хотя бы одну цифру."
    }
    if (!password.any { !it.isLetterOrDigit() }) {
        return "Добавьте в пароль символ, например !."
    }
    return null
}
