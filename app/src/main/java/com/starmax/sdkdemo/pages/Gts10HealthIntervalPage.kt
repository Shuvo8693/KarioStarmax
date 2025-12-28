package com.starmax.sdkdemo.pages

import androidx.compose.animation.core.StartOffsetType.Companion.Delay
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.sharp.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.starmax.bluetoothsdk.Notify
import com.starmax.bluetoothsdk.Notify.HealthInterval.HealthIntervalType
import com.starmax.sdkdemo.PreviewInit
import com.starmax.sdkdemo.ui.theme.AppTheme
import com.starmax.sdkdemo.viewmodel.Gts10HealthIntervalViewModel
import kotlinx.coroutines.delay

@Composable
fun Gts10HealthIntervalPage(navController: NavController) {
    val viewModel: Gts10HealthIntervalViewModel by lazyKoinViewModel()

    LaunchedEffect(Unit) {
        viewModel.getData()
    }

    Gts10HealthIntervalPageView(navController = navController, viewModel = viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Gts10HealthIntervalPageView(
    navController: NavController,
    viewModel: Gts10HealthIntervalViewModel
) {
    val context = LocalContext.current

    AppTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(text = "GTS10 Measurement Interval")
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            navController.popBackStack()
                        }) {
                            Icon(
                                Icons.Sharp.KeyboardArrowLeft,
                                contentDescription = "Back"
                            )
                        }
                    },
                )
            }
        ) { innerPadding ->
            LazyColumn(contentPadding = innerPadding) {
                items(count = viewModel.intervals.size) { index ->
                    val task = viewModel.intervals[index]

                    Column(
                        modifier = Modifier.fillMaxWidth().padding(15.dp)
                    ) {
                        SelectIntervalType(task = task, viewModel = viewModel)

                        Text(
                            text = "Measurement Interval: ${task.measureInterval}",
                            style = MaterialTheme.typography.labelSmall
                        )

                        Slider(
                            value = task.measureInterval.toFloat(),
                            valueRange = 0f..255f,
                            steps = 255,
                            onValueChange = {
                                viewModel.intervals[index].measureInterval = it.toInt()
                                viewModel.refresh()
                            }
                        )

                        Text(
                            text = "Storage Interval: ${task.storeInterval}",
                            style = MaterialTheme.typography.labelSmall
                        )

                        Slider(
                            value = task.storeInterval.toFloat(),
                            valueRange = 0f..255f,
                            steps = 255,
                            onValueChange = {
                                viewModel.intervals[index].storeInterval = it.toInt()
                                viewModel.refresh()
                            }
                        )

                        IconButton(onClick = {
                            viewModel.removeInterval(index)
                        }) {
                            Icon(Icons.Outlined.Remove, contentDescription = "")
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            viewModel.addInterval()
                        }) {
                            Icon(Icons.Outlined.Add, contentDescription = "")
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = {
                            navController.popBackStack()
                        }) {
                            Text(text = "Cancel")
                        }

                        ElevatedButton(
                            onClick = {
                                viewModel.setData()
                                navController.popBackStack()
                            },
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.offset(15.dp)
                        ) {
                            Text(text = "Confirm")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SelectIntervalType(
    task: Notify.HealthInterval.Builder,
    viewModel: Gts10HealthIntervalViewModel
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Type: ${task.type.name}")

        Box {
            TextButton(onClick = { expanded = !expanded }) {
                Text(text = "Select")
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                HealthIntervalType.values()
                    .filter { it != HealthIntervalType.Unknown &&
                                it != HealthIntervalType.UNRECOGNIZED
                    }
                    .forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.name) },
                            onClick = {
                                task.type = option
                                viewModel.refresh()
                                expanded = false
                            }
                        )
                    }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewGts10HealthIntervalPageView() {
    PreviewInit {
        val navController = rememberNavController()
        Gts10HealthIntervalPage(navController)
    }
}
