package com.starmax.sdkdemo.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.sharp.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.gson.Gson
import com.starmax.sdkdemo.ui.theme.AppTheme
import com.starmax.sdkdemo.viewmodel.BleViewModel
import java.text.SimpleDateFormat
import java.util.*

data class SportHistory(
    val status: Int,
    val sport_length: Int,
    val current_sport_id: Int,
    val current_sport_data_length: Int,
    val has_next: Boolean,
    val not_valid: Boolean,
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int,
    val second: Int,
    val sport_seconds: Int,
    val type: Int,
    val steps: Int,
    val distance: Int,
    val speed: Int,
    val calorie: Int,
    val pace_time: Int,
    val step_frequency: Int,
    val heart_rate_avg: Int,
    val heart_rate_length: Int
)

@Composable
fun SportsHistoryPage(navController: NavController) {
    val viewModel: BleViewModel by lazyKoinViewModel()
    SportsHistoryPageView(navController = navController, bleViewModel = viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SportsHistoryPageView(navController: NavController, bleViewModel: BleViewModel) {
    val bleResponseValue = bleViewModel.bleResponseLabel.value

    // Parse JSON sports data
    val sportsData = remember(bleResponseValue) {
        try {
            if (bleResponseValue.isNotBlank()) {
                Gson().fromJson(bleResponseValue, SportHistory::class.java)
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
                            text = "Sports Records",
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

            if (sportsData != null && !sportsData.not_valid && sportsData.status == 0) {
                LazyColumn(
                    contentPadding = innerPadding,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Header Card with sport type and date
                    item {
                        SportRecordHeader(sportsData)
                    }

                    // Main metrics
                    item {
                        PrimaryMetricsCard(sportsData)
                    }

                    // Secondary metrics
                    item {
                        SecondaryMetricsCard(sportsData)
                    }

                    // Heart rate info if available
                    if (sportsData.heart_rate_avg > 0) {
                        item {
                            HeartRateCard(sportsData)
                        }
                    }
                }
            } else {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsRun,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "No sports data available",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Start a workout to see your records",
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
fun SportRecordHeader(data: SportHistory) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = getSportIcon(data.type),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = getSportTypeName(data.type),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatDate(data),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun PrimaryMetricsCard(data: SportHistory) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Activity Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricItem(
                    icon = Icons.Default.Timer,
                    label = "Duration",
                    value = formatDuration(data.sport_seconds),
                    color = Color(0xFFFF6B6B)
                )

                MetricItem(
                    icon = Icons.Default.Straighten,
                    label = "Distance",
                    value = formatDistance(data.distance),
                    color = Color(0xFF4ECDC4)
                )
            }

            Divider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricItem(
                    icon = Icons.Default.LocalFireDepartment,
                    label = "Calories",
                    value = "${data.calorie} kcal",
                    color = Color(0xFFFFBE0B)
                )

                MetricItem(
                    icon = Icons.Default.DirectionsWalk,
                    label = "Steps",
                    value = data.steps.toString(),
                    color = Color(0xFF95E1D3)
                )
            }
        }
    }
}

@Composable
fun SecondaryMetricsCard(data: SportHistory) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Performance Metrics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (data.speed > 0) {
                MetricRow(
                    label = "Avg Speed",
                    value = "${data.speed / 100.0} km/h",
                    icon = Icons.Default.Speed
                )
            }

            if (data.pace_time > 0) {
                MetricRow(
                    label = "Pace",
                    value = formatPace(data.pace_time),
                    icon = Icons.Default.Timer
                )
            }

            if (data.step_frequency > 0) {
                MetricRow(
                    label = "Step Frequency",
                    value = "${data.step_frequency} spm",
                    icon = Icons.Default.DirectionsWalk
                )
            }
        }
    }
}

@Composable
fun HeartRateCard(data: SportHistory) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFE5E5)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = Color(0xFFE63946)
            )
            Column {
                Text(
                    text = "Average Heart Rate",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B0000)
                )
                Text(
                    text = "${data.heart_rate_avg} bpm",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE63946)
                )
            }
        }
    }
}

@Composable
fun MetricItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = color
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MetricRow(
    label: String,
    value: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// Helper functions
fun getSportIcon(type: Int): ImageVector {
    return when (type) {
        0 -> Icons.Default.DirectionsRun  // Running
        1 -> Icons.Default.DirectionsWalk // Walking
        2 -> Icons.Default.DirectionsBike // Cycling
        else -> Icons.Default.FitnessCenter
    }
}

fun getSportTypeName(type: Int): String {
    return when (type) {
        0 -> "Running"
        1 -> "Walking"
        2 -> "Cycling"
        3 -> "Hiking"
        4 -> "Swimming"
        else -> "Workout"
    }
}

fun formatDate(data: SportHistory): String {
    return try {
        val calendar = Calendar.getInstance().apply {
            set(data.year, data.month - 1, data.day, data.hour, data.minute, data.second)
        }
        SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(calendar.time)
    } catch (e: Exception) {
        "Invalid date"
    }
}

fun formatDuration(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60

    return when {
        hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, secs)
        else -> String.format("%d:%02d", minutes, secs)
    }
}

fun formatDistance(distanceInMeters: Int): String {
    return if (distanceInMeters >= 1000) {
        String.format("%.2f km", distanceInMeters / 1000.0)
    } else {
        "$distanceInMeters m"
    }
}

fun formatPace(paceInSeconds: Int): String {
    val minutes = paceInSeconds / 60
    val seconds = paceInSeconds % 60
    return String.format("%d'%02d\"/km", minutes, seconds)
}