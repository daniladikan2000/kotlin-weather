package com.example.weather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.weather.ui.theme.WeatherTheme
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            WeatherTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF87CEEB), Color(0xFF4682B4))
                            )
                        )
                ) {
                    WeatherScreen()
                }
            }
        }
    }
}

@Composable
fun WeatherScreen(weatherViewModel: WeatherViewModel = viewModel()) {
    val weatherState = weatherViewModel.weatherState.value
    val isLoading = weatherViewModel.isLoading.value
    val error = weatherViewModel.error.value
    val cityInput by weatherViewModel.cityInput
    val langCode by weatherViewModel.langCode

    LaunchedEffect(Unit) {
        weatherViewModel.fetchWeather()
    }

    Scaffold(
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = cityInput,
                onValueChange = { weatherViewModel.onCityInputChange(it) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { weatherViewModel.fetchWeather() }) {
                        Icon(Icons.Default.Search, contentDescription = "Поиск")
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.3f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.3f),
                    focusedIndicatorColor = Color.White,
                    cursorColor = Color.White,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedTrailingIconColor = Color.White,
                    unfocusedTrailingIconColor = Color.White.copy(alpha = 0.7f),
                ),
                shape = RoundedCornerShape(16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> CircularProgressIndicator(color = Color.White)
                error != null -> Text(text = error, color = Color.Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                weatherState != null -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        WeatherInfoCard(weatherState, langCode)
                        Spacer(modifier = Modifier.height(16.dp))
                        DetailsCard(weatherState, langCode)
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherInfoCard(weather: WeatherResponse, langCode: String) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = weather.name, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))

            val iconUrl = "https://openweathermap.org/img/wn/${weather.weather.firstOrNull()?.icon}@4x.png"
            AsyncImage(
                model = iconUrl,
                contentDescription = "Weather icon",
                modifier = Modifier.size(140.dp)
            )

            Text(
                text = "${weather.main.temp.roundToInt()}°C",
                fontSize = 72.sp,
                fontWeight = FontWeight.Light,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = weather.weather.firstOrNull()?.description?.replaceFirstChar { it.titlecase(Locale.getDefault()) } ?: "",
                fontSize = 22.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                InfoItem(
                    icon = Icons.Filled.WaterDrop,
                    title = if (langCode == "ru") "Влажность" else "Humidity",
                    value = "${weather.main.humidity}%"
                )
                InfoItem(
                    icon = Icons.Filled.Air,
                    title = if (langCode == "ru") "Ветер" else "Wind",
                    value = "${weather.wind.speed.roundToInt()} м/с"
                )
            }
        }
    }
}

@Composable
fun DetailsCard(weather: WeatherResponse, langCode: String) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            InfoItem(
                icon = Icons.Default.Thermostat,
                title = if (langCode == "ru") "Ощущается" else "Feels like",
                value = "${weather.main.feels_like.roundToInt()}°C"
            )
            InfoItem(
                icon = Icons.Default.WbSunny,
                title = if (langCode == "ru") "Восход" else "Sunrise",
                value = formatTime(weather.sys.sunrise)
            )
            InfoItem(
                icon = Icons.Outlined.NightsStay,
                title = if (langCode == "ru") "Закат" else "Sunset",
                value = formatTime(weather.sys.sunset)
            )
        }
    }
}

@Composable
fun InfoItem(icon: ImageVector, title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = title, tint = Color.White, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        Text(text = title, fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
    }
}

private fun formatTime(unixTime: Long): String {
    val date = Date(unixTime * 1000L)
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(date)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WeatherTheme {
        Box (
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF87CEEB), Color(0xFF4682B4))
                    )
                )
        ) {
            WeatherScreen()
        }
    }
}