package com.example.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.api.AuthService
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    context: android.content.Context,
    onSignedIn: (Map<String, Any>) -> Unit,
    onOpen: (String) -> Unit
) {
    var nationalCode by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var capToken by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    fun submit() {
        error = ""
        if (nationalCode.isBlank() || password.isBlank()) {
            error = "کد ملی و رمز عبور را وارد کنید."
            return
        }
        scope.launch {
            loading = true
            try {
                onSignedIn(AuthService.login(context, nationalCode, password, capToken))
            } catch (issue: Exception) {
                error = issue.message ?: "ورود انجام نشد. اتصال اینترنت و تنظیمات سرویس را بررسی کنید."
            } finally {
                loading = false
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.logintopsectionbg),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.28f))
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(R.drawable.loginlogo),
                        contentDescription = "نشان صدور بارنامه شهری",
                        modifier = Modifier.size(92.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "صدور بارنامه شهری",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                    Text("ورود به سامانه", color = Color.White.copy(alpha = 0.9f))
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .offset(y = (-18).dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    OutlinedTextField(
                        value = nationalCode,
                        onValueChange = { nationalCode = it.take(11) },
                        label = { Text("کد ملی / شناسه ملی") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("رمز عبور") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = capToken,
                        onValueChange = { capToken = it },
                        label = { Text("کد امنیتی (در صورت درخواست سرویس)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    if (error.isNotBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.height(18.dp))
                    Button(
                        onClick = ::submit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = !loading
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("در حال ورود...")
                        } else {
                            Text("ورود")
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { onSignedIn(mapOf("mode" to "demo")) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = !loading
                    ) {
                        Text("ورود به نسخه آزمایشی")
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = { onOpen("signup") }, modifier = Modifier.weight(1f)) {
                            Text("ثبت نام")
                        }
                        OutlinedButton(onClick = { onOpen("forgotPassword") }, modifier = Modifier.weight(1f)) {
                            Text("فراموشی رمز")
                        }
                    }
                }
            }
            Text(
                text = "ورود زنده از طریق سرویس بارنامه انجام می‌شود.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
