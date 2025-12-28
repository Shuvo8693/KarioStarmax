package com.starmax.sdkdemo.pages

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.sharp.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.gson.Gson
import com.starmax.sdkdemo.data_model.WeatherResponse
import com.starmax.sdkdemo.ui.theme.AppTheme
import com.starmax.sdkdemo.viewmodel.BleViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WeatherPage(navController: NavController) {
    val viewModel: BleViewModel by lazyKoinViewModel()
    WeatherPageView(navController = navController, bleViewModel = viewModel)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherPageView(navController: NavController, bleViewModel: BleViewModel) {
    val bleResponseValue = bleViewModel.bleResponseLabel.value

    // Parse JSON weather data
    val weatherData = remember(bleResponseValue) {
        try {
            if (bleResponseValue.isNotBlank()) {
                Gson().fromJson(bleResponseValue, WeatherResponse::class.java)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    AppTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Weather Forecast",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Sharp.KeyboardArrowLeft, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        ) { innerPadding ->

            if (weatherData != null) {
                LazyColumn(
                    contentPadding = innerPadding,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Header Card with current location and date
                    item {
                        CurrentWeatherHeader(weatherData)
                    }

                    // Weather forecast list
                    itemsIndexed(weatherData.days) { index, day ->
                        WeatherDayCard(
                            day = day,
                            dayIndex = index,
                            currentDate = Calendar.getInstance().apply {
                                set(weatherData.year, weatherData.month - 1, weatherData.day)
                            }
                        )
                    }
                }
            } else {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "No weather data available",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Please sync weather data from device",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Gson() {
    TODO("Not yet implemented")
}

@Composable
fun CurrentWeatherHeader(weatherData: WeatherResponse) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )

                Text(
                    text = weatherData.city.trim().replace("\u0000", ""),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Text(
                    text = "${weatherData.year}-${weatherData.month.toString().padStart(2, '0')}-${weatherData.day.toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )

                Text(
                    text = "${weatherData.hour.toString().padStart(2, '0')}:${weatherData.minute.toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun WeatherDayCard(day: com.starmax.sdkdemo.data_model.WeatherDay, dayIndex: Int, currentDate: Calendar) {
    val date = Calendar.getInstance().apply {
        time = currentDate.time
        add(Calendar.DAY_OF_MONTH, dayIndex)
    }

    val dayName = when (dayIndex) {
        0 -> "Today"
        1 -> "Tomorrow"
        else -> SimpleDateFormat("EEEE", Locale.getDefault()).format(date.time)
    }

    val dateString = SimpleDateFormat("MMM dd", Locale.getDefault()).format(date.time)

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Day name and date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = dateString,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = day.getWeatherTypeIcon(),
                    style = MaterialTheme.typography.displayMedium,
                    fontSize = 48.sp
                )
            }

            // Weather type
            Text(
                text = day.getWeatherTypeName(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Divider(color = MaterialTheme.colorScheme.outlineVariant)

            // Temperature
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeatherInfoItem(
                    icon = Icons.Default.DeviceThermostat,
                    label = "Temperature",
                    value = "${day.temp}°C",
                    modifier = Modifier.weight(1f)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = Color(0xFFFF5722),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${day.max_temp}°",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFFF5722)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${day.min_temp}°",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF2196F3)
                        )
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant)

            // Weather details grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeatherDetailItem(
                    icon = "💨",
                    label = "Wind",
                    value = "${day.wind_speed} km/h",
                    modifier = Modifier.weight(1f)
                )

                WeatherDetailItem(
                    icon = "💧",
                    label = "Humidity",
                    value = "${day.dampness}%",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeatherDetailItem(
                    icon = "☀️",
                    label = "UV Index",
                    value = "${day.uv}",
                    modifier = Modifier.weight(1f)
                )

                WeatherDetailItem(
                    icon = "👁️",
                    label = "Visibility",
                    value = "${day.seeing} km",
                    modifier = Modifier.weight(1f)
                )
            }

            // Air Quality
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(day.getAirQualityColor().copy(alpha = 0.2f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🌍", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Air Quality",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = day.getAirQualityText(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = day.getAirQualityColor()
                )
            }

            // Sunrise & Sunset
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                SunTimeItem(
                    icon = "🌅",
                    label = "Sunrise",
                    time = "${day.sunrise_hour.toString().padStart(2, '0')}:${day.sunrise_minute.toString().padStart(2, '0')}"
                )

                SunTimeItem(
                    icon = "🌇",
                    label = "Sunset",
                    time = "${day.sunset_hour.toString().padStart(2, '0')}:${day.sunset_minute.toString().padStart(2, '0')}"
                )
            }
        }
    }
}

@Composable
fun WeatherInfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun WeatherDetailItem(
    icon: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SunTimeItem(
    icon: String,
    label: String,
    time: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = icon, fontSize = 24.sp)
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = time,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
