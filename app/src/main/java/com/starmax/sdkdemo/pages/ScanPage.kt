package com.starmax.sdkdemo.pages

import androidx.activity.ComponentActivity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.sharp.KeyboardArrowLeft
import androidx.compose.material.icons.twotone.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.starmax.sdkdemo.ui.theme.AppTheme
import com.starmax.sdkdemo.viewmodel.BleState
import com.starmax.sdkdemo.viewmodel.BleViewModel
import com.starmax.sdkdemo.viewmodel.ScanViewModel
import kotlinx.coroutines.launch

@Composable
fun ScanPage(navController: NavController, viewModel: ScanViewModel = viewModel()) {
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopScan()
        }
    }

    val bleViewModel: BleViewModel = viewModel(LocalContext.current as ComponentActivity)

    ScanPageView(navController, viewModel, bleViewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanPageView(
    navController: NavController,
    viewModel: ScanViewModel = viewModel(),
    bleViewModel: BleViewModel
) {
    val bleDevice = bleViewModel.bleDevice
    val isConnected = bleDevice?.get() != null && bleViewModel.bleState == BleState.CONNECTED
    val connectedMac = bleDevice?.get()?.mac
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()


    AppTheme {
        Scaffold(
            snackbarHost = {SnackbarHost(hostState = snackbarHostState)},
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(text = "Search Devices")
                    },
                    navigationIcon = {
                        IconButton(
                            modifier = Modifier.height(25.dp),
                            onClick = {
                            navController.popBackStack()
                        },
                        ) {
                            Icon(Icons.Sharp.KeyboardArrowLeft, contentDescription = "return")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { innerPadding ->

            LazyColumn(
                contentPadding = innerPadding,
                modifier = Modifier.fillMaxSize()
            ) {
                // ===== CONNECTION STATUS CARD =====
                if (isConnected) {
                    item {
                        ConnectedDeviceCard(
                            deviceName = bleViewModel.getDeviceName(),
                            deviceMac = connectedMac ?: "",
                            onDisconnect = {
                                bleViewModel.disconnect()
                            }
                        )
                    }
                }else if(bleViewModel.bleState == BleState.CONNECTTING){
                    item {
//                        Text(text = "CONNECTING...")
                        BouncingWatchConnecting()
                    }
                }

                // ===== BLUETOOTH NAME FIELD =====
                item {
                    IOSStyleScanCard(isScanning = viewModel.isScanning) {
                        if (viewModel.isScanning) {
                            viewModel.stopScan()
                        } else {
                            viewModel.startScan()
                        }
                    }
                }

                // ===== MAC ADDRESS FIELD =====
          /*      item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 15.dp, vertical = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = viewModel.searchMac,
                            onValueChange = {
                                viewModel.searchMac = it
                            },
                            label = {
                                Text(
                                    text = "Mac address",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            textStyle = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(text = "", style = MaterialTheme.typography.labelSmall)
                            },
                            trailingIcon = {
                                IconButton(onClick = {
                                    if (viewModel.isScanning) {
                                        viewModel.stopScan()
                                    } else {
                                        viewModel.startScan()
                                    }
                                }) {
                                    Icon(
                                        if (viewModel.isScanning) Icons.TwoTone.SearchOff else Icons.TwoTone.Search,
                                        contentDescription = "search"
                                    )
                                }
                            }
                        )
                    }
                }
*/
                // ===== SCANNING INDICATOR =====
                if (viewModel.isScanning) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Scanning for devices...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // ===== DEVICES HEADER =====
                if (viewModel.devices.isNotEmpty()) {
                    item {
                        Text(
                            text = "Available Devices (${viewModel.devices.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                // ===== BLUETOOTH DEVICE LIST =====
                items(viewModel.devices.size) { index ->
                    val device = viewModel.devices[index]
                    val isThisDeviceConnected = connectedMac == device.mac && bleViewModel.bleState == BleState.CONNECTED


                    DeviceListItem(
                        deviceName = viewModel.getDeviceName(index),
                        deviceMac = device.mac,
                        broadcastInfo = if (viewModel.broadcast.contains(device.mac))
                            viewModel.broadcast[device.mac] else "",
                        isConnected = isThisDeviceConnected,
                        onConnect = {
                            if (!isThisDeviceConnected) {
                                bleViewModel.connect(device)
//                                navController.popBackStack()
                            }
                        },
                        onDisconnect = {
                            if (isThisDeviceConnected) {
//                                bleViewModel.disconnect()
                                scope.launch {
                                    snackbarHostState.showSnackbar("Device is already connected")
                                }

                            }
                        }
                    )
                    HorizontalDivider()
                }

                // ===== EMPTY STATE =====
                if (viewModel.devices.isEmpty() && !viewModel.isScanning) {
                    item {
                        EmptyDeviceState()
                    }
                }
            }
        }
    }
}

@Composable
fun BouncingWatchConnecting() {
    val infiniteTransition = rememberInfiniteTransition(label = "bounce")

    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetY"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .offset(y = offsetY.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Watch,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "CONNECTING...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Pairing with smartwatch",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}


// ===== iOS-Style Card =====
@Composable
fun IOSStyleScanCard(
    isScanning: Boolean,
    onScanToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(13.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            Text(
                text = if (isScanning) "Scanning..." else "Ready to Scan",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (isScanning)
                    "Looking for Bluetooth devices"
                else
                    "Tap button to start scanning",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onScanToggle,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isScanning)
                        MaterialTheme.colorScheme.error
                    else
                        Color(0xFF5AB8A8)
                )
            ) {
                Icon(
                    if (isScanning) Icons.Default.Stop else Icons.Default.Search,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isScanning) "Stop Scanning" else "Start Scanning",
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}


@Composable
fun ConnectedDeviceCard(
    deviceName: String,
    deviceMac: String,
    onDisconnect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.TwoTone.Bluetooth,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Connected",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = deviceName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = deviceMac,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            TextButton(onClick = onDisconnect) {
                Text("Disconnect")
            }
        }
    }
}

@Composable
fun DeviceListItem(
    deviceName: String,
    deviceMac: String,
    broadcastInfo: String?,
    isConnected: Boolean,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isConnected)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else
            Color.Transparent,
        animationSpec = tween(300)
    )

    ListItem(
        modifier = Modifier
            .clickable {
                if (isConnected) {
                    onDisconnect()
                } else {
                    onConnect()
                }
            }
            .background(backgroundColor),
        headlineContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = deviceName)
                if (isConnected) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF4CAF50))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "CONNECTED",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        overlineContent = { Text(text = deviceMac) },
        supportingContent = {
            broadcastInfo?.let {
                if (it.isNotEmpty()) {
                    Text(text = it)
                }
            }
        },
        leadingContent = {
            Icon(
                Icons.TwoTone.Bluetooth,
                contentDescription = "",
                tint = if (isConnected) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface
            )
        },
        trailingContent = {
            if (isConnected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Connected",
                    tint = Color(0xFF4CAF50)
                )
            } else {
                Icon(
                    Icons.TwoTone.SignalCellularAlt,
                    contentDescription = "Signal"
                )
            }
        }
    )
}

@Composable
fun EmptyDeviceState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.TwoTone.BluetoothSearching,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No devices found",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap the search icon to scan for devices",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewScanPage() {
    val navController = rememberNavController()
    ScanPageView(navController, ScanViewModel(), BleViewModel())
}