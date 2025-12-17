/*
package com.starmax.sdkdemo.pages

import android.provider.CalendarContract
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Bluetooth
import androidx.compose.material.icons.twotone.GridView
import androidx.compose.material.icons.twotone.SignalCellularOff
import androidx.compose.material.icons.twotone.SmartButton
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.starmax.net.NetApi
import com.starmax.net.NetChannel
import com.starmax.sdkdemo.dialogs.AppDialog
import com.starmax.sdkdemo.dialogs.BloodPressureCalibrationDialog
import com.starmax.sdkdemo.dialogs.BloodSugarCalibrationDialog
import com.starmax.sdkdemo.dialogs.CallControlDialog
import com.starmax.sdkdemo.dialogs.CameraControlDialog
import com.starmax.sdkdemo.dialogs.CustomBroadcastDialog
import com.starmax.sdkdemo.dialogs.CustomDeviceModeDialog
import com.starmax.sdkdemo.dialogs.CustomDeviceNameDialog
import com.starmax.sdkdemo.dialogs.CustomDeviceShakeOnOffDialog
import com.starmax.sdkdemo.dialogs.CustomDeviceShakeTimeDialog
import com.starmax.sdkdemo.dialogs.CustomOnOffDialog
import com.starmax.sdkdemo.dialogs.CustomSportModeOnOffDialog
import com.starmax.sdkdemo.dialogs.DateFormatDialog
import com.starmax.sdkdemo.dialogs.FemaleHealthDialog
import com.starmax.sdkdemo.dialogs.FileSystemDialog
import com.starmax.sdkdemo.dialogs.HomeDrawer
import com.starmax.sdkdemo.dialogs.MessageDialog
import com.starmax.sdkdemo.dialogs.NfcCardDialog
import com.starmax.sdkdemo.dialogs.PasswordDialog
import com.starmax.sdkdemo.dialogs.SetContactDialog
import com.starmax.sdkdemo.dialogs.SetGoalsDialog
import com.starmax.sdkdemo.dialogs.SetHealthOpenDialog
import com.starmax.sdkdemo.dialogs.SetHeartDialog
import com.starmax.sdkdemo.dialogs.SetNetDialog
import com.starmax.sdkdemo.dialogs.SetNotDisturbDialog
import com.starmax.sdkdemo.dialogs.SetRealTimeDataMeasureDialog
import com.starmax.sdkdemo.dialogs.SetRealTimeDataOpenDialog
import com.starmax.sdkdemo.dialogs.SetSosDialog
import com.starmax.sdkdemo.dialogs.SetStateDialog
import com.starmax.sdkdemo.dialogs.SetUserInfoDialog
import com.starmax.sdkdemo.dialogs.SportSyncToDeviceDialog
import com.starmax.sdkdemo.dialogs.VolumeDialog
import com.starmax.sdkdemo.dialogs.WorldClockDialog
import com.starmax.sdkdemo.ui.theme.AppTheme
import com.starmax.sdkdemo.viewmodel.BleState
import com.starmax.sdkdemo.viewmodel.BleViewModel
import com.starmax.sdkdemo.viewmodel.HomeViewModel
import com.starmax.sdkdemo.viewmodel.OtaViewModel
import com.starmax.sdkdemo.viewmodel.SetNetModel
import kotlinx.coroutines.launch
import okhttp3.internal.notify
import org.koin.androidx.compose.koinViewModel
import org.koin.core.component.get

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(navController: NavController) {
    val netViewModel: SetNetModel = koinViewModel()
    val bleViewModel: BleViewModel by lazyKoinViewModel()
    val otaViewModel: OtaViewModel by lazyKoinViewModel()
    val viewModel: HomeViewModel by lazyKoinViewModel()
    val scope = rememberCoroutineScope()
    val snackbarHostState = SnackbarHostState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    otaViewModel.otaMessage.observeForever { message ->
        scope.launch {
            if (message != "") {
                bleViewModel.bleMessage.value = message
            }
        }
    }


    AppTheme {
        if (viewModel.openNetDialog) {
            SetNetDialog()
        }
        if (viewModel.openFileSystemDialog) {
            FileSystemDialog()
        }
        if (viewModel.openVolumeDialog) {
            VolumeDialog()
        }
        if (viewModel.openNfcDialog) {
            NfcCardDialog()
        }
        if (viewModel.openStateDialog) {
            SetStateDialog()
        }
        if (viewModel.openCameraDialog) {
            CameraControlDialog()
        }
        if(viewModel.openCustomOnOffDialog){
            CustomOnOffDialog()
        }
        if(viewModel.openCustomDeviceModeDialog){
            CustomDeviceModeDialog()
        }
        if(viewModel.openSportSyncToDeviceDialog){
            SportSyncToDeviceDialog()
        }
        if(viewModel.openCustomDeviceNameDialog){
            CustomDeviceNameDialog()
        }
        if(viewModel.openCustomDeviceShakeTimeDialog){
            CustomDeviceShakeTimeDialog()
        }
        if(viewModel.openCustomDeviceShakeOnOffDialog){
            CustomDeviceShakeOnOffDialog()
        }
        if(viewModel.openSportModeOnOffDialog){
            CustomSportModeOnOffDialog()
        }
        if(viewModel.openCustomBroadcastDialog){
            CustomBroadcastDialog()
        }
        if (viewModel.openCallDialog) {
            CallControlDialog()
        }
        if (viewModel.openDateFormatDialog) {
            DateFormatDialog()
        }
        if (viewModel.openNotDisturbDialog) {
            SetNotDisturbDialog()
        }
        if (viewModel.openUserInfoDialog) {
            SetUserInfoDialog()
        }
        if (viewModel.openGoalsDialog) {
            SetGoalsDialog()
        }
        if (viewModel.openHealthOpenDialog) {
            SetHealthOpenDialog()
        }
        if (viewModel.openHeartRateDialog) {
            SetHeartDialog()
        }
        if (viewModel.openRealTimeDataDialog) {
            SetRealTimeDataOpenDialog()
        }
        if (viewModel.openRealTimeMeasureDialog) {
            SetRealTimeDataMeasureDialog()
        }
        if (viewModel.openContactDialog) {
            SetContactDialog()
        }
        if (viewModel.openSosDialog) {
            SetSosDialog()
        }
        if (viewModel.openAppDialog) {
            AppDialog()
        }
        if (viewModel.openMessageDialog) {
            MessageDialog()
        }
        if (viewModel.openWorldClockDialog) {
            WorldClockDialog()
        }
        if (viewModel.openPasswordDialog) {
            PasswordDialog()
        }
        if(viewModel.openFemaleHealthDialog){
            FemaleHealthDialog()
        }
        if(viewModel.openBloodSugarCalibrationDialog){
            BloodSugarCalibrationDialog()
        }
        if(viewModel.openBloodPressureCalibrationDialog){
            BloodPressureCalibrationDialog()
        }
        Scaffold(
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState
                )
            },
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = "Kario Watch")
                    }, actions = {
                        TextButton(onClick = {
                            viewModel.toScan(navController)
                        }) {
                            Text(
                                text = "Pair Device",
                                color = Color.White
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                viewModel.toInstructionList(navController)
//                                drawerState.open()
                            }
                        }) {
                            Icon(Icons.TwoTone.GridView, contentDescription = "command list")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF3ABA75),
                        titleContentColor = MaterialTheme.colorScheme.inverseOnSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.inverseOnSurface
                    )
                )
            },
            content = { innerPadding ->

                LazyColumn(
                    contentPadding = innerPadding,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // ================= CONNECTION CARD =================
                    item {

//                         val bleState = bleViewModel.bleStateLiveData.value
//                        val isConnected = bleState == BleState.CONNECTED
                        val bleDevice = bleViewModel.bleDevice
                        val isConnected = bleDevice?.get() != null

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {

                                Row {
                                    Icon(
                                        Icons.TwoTone.Bluetooth,
                                        contentDescription = null,
                                        tint = if (isConnected)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.error
                                    )

                                    Column(
                                        modifier = Modifier.padding(start = 12.dp)
                                    ) {

                                        Text(
                                            text = if (isConnected) bleViewModel.getDeviceName()
                                            else "Not Connected",
                                            style = MaterialTheme.typography.titleMedium
                                        )

                                        Text(
                                            text = if (isConnected)
                                                bleDevice!!.get()!!.mac
                                            else
                                                "Tap Pair Device to connect",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (isConnected) {
                                    TextButton(onClick = {
                                        bleViewModel.disconnect()
                                    }) {
                                        Text("Disconnect")
                                    }
                                }
                            }
                        }
                    }

                    // ================== SERVER & CHANNEL ==================
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text =
                                        (if (netViewModel.server == NetApi.Server) "Production" else "Test") +
                                                " Server • " +
                                                (if (netViewModel.channel == NetChannel.Release) "Release" else "Beta"),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    // ================== STATUS MESSAGE ==================
                    item {
                        if (bleViewModel.bleMessage.value.isNotBlank()) {
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = bleViewModel.bleMessage.value,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }

                    // ================== ACTION BUTTONS ==================
                    item {
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(onClick = {
                                viewModel.toScan(navController)
                            }) {
                                Text("Pair Device", color = Color.White)
                            }
                        }
                    }

                    // ================== SMARTWATCH DATA CARD ==================
                    item {

                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {

                                Text(
                                    text = "Smartwatch Data",
                                    style = MaterialTheme.typography.titleMedium
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    thickness = DividerDefaults.Thickness,
                                    color = DividerDefaults.color
                                )

                                // Parsed label/value
                                Text(
                                    text = bleViewModel.bleResponseLabel.value,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = bleViewModel.bleResponse.value,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 4.dp)
                                )

                                Divider(modifier = Modifier.padding(vertical = 12.dp))

                                // Raw data
                                Text(
                                    text = "Raw Data:",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Text(
                                    text = bleViewModel.originData.value,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

        )
        HomeDrawer(drawerState,navController)
    }
}



@Preview(showBackground = true)
@Composable
fun PreviewHomePage() {
    val navController = rememberNavController()
    HomePage(navController)
}*/


package com.starmax.sdkdemo.pages

import android.provider.CalendarContract
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.twotone.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.starmax.sdkdemo.dialogs.*
import com.starmax.sdkdemo.ui.theme.AppTheme
import com.starmax.sdkdemo.viewmodel.BleState
import com.starmax.sdkdemo.viewmodel.BleViewModel
import com.starmax.sdkdemo.viewmodel.HomeViewModel
import com.starmax.sdkdemo.viewmodel.OtaViewModel
import com.starmax.sdkdemo.viewmodel.SetNetModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(navController: NavController) {
    val netViewModel: SetNetModel = koinViewModel()
    val bleViewModel: BleViewModel by lazyKoinViewModel()
    val otaViewModel: OtaViewModel by lazyKoinViewModel()
    val viewModel: HomeViewModel by lazyKoinViewModel()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Observer for OTA messages
    otaViewModel.otaMessage.observeForever { message ->
        scope.launch {
            if (message.isNotEmpty()) {
                bleViewModel.bleMessage.value = message
            }
        }
    }

    AppTheme {
        // All dialogs
        if (viewModel.openNetDialog) SetNetDialog()
        if (viewModel.openFileSystemDialog) FileSystemDialog()
        if (viewModel.openVolumeDialog) VolumeDialog()
        if (viewModel.openNfcDialog) NfcCardDialog()
        if (viewModel.openStateDialog) SetStateDialog()
        if (viewModel.openCameraDialog) CameraControlDialog()
        if (viewModel.openCustomOnOffDialog) CustomOnOffDialog()
        if (viewModel.openCustomDeviceModeDialog) CustomDeviceModeDialog()
        if (viewModel.openSportSyncToDeviceDialog) SportSyncToDeviceDialog()
        if (viewModel.openCustomDeviceNameDialog) CustomDeviceNameDialog()
        if (viewModel.openCustomDeviceShakeTimeDialog) CustomDeviceShakeTimeDialog()
        if (viewModel.openCustomDeviceShakeOnOffDialog) CustomDeviceShakeOnOffDialog()
        if (viewModel.openSportModeOnOffDialog) CustomSportModeOnOffDialog()
        if (viewModel.openCustomBroadcastDialog) CustomBroadcastDialog()
        if (viewModel.openCallDialog) CallControlDialog()
        if (viewModel.openDateFormatDialog) DateFormatDialog()
        if (viewModel.openNotDisturbDialog) SetNotDisturbDialog()
        if (viewModel.openUserInfoDialog) SetUserInfoDialog()
        if (viewModel.openGoalsDialog) SetGoalsDialog()
        if (viewModel.openHealthOpenDialog) SetHealthOpenDialog()
        if (viewModel.openHeartRateDialog) SetHeartDialog()
        if (viewModel.openRealTimeDataDialog) SetRealTimeDataOpenDialog()
        if (viewModel.openRealTimeMeasureDialog) SetRealTimeDataMeasureDialog()
        if (viewModel.openContactDialog) SetContactDialog()
        if (viewModel.openSosDialog) SetSosDialog()
        if (viewModel.openAppDialog) AppDialog()
        if (viewModel.openMessageDialog) MessageDialog()
        if (viewModel.openWorldClockDialog) WorldClockDialog()
        if (viewModel.openPasswordDialog) PasswordDialog()
        if (viewModel.openFemaleHealthDialog) FemaleHealthDialog()
        if (viewModel.openBloodSugarCalibrationDialog) BloodSugarCalibrationDialog()
        if (viewModel.openBloodPressureCalibrationDialog) BloodPressureCalibrationDialog()

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Health",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        IconButton(onClick = {
                            viewModel.toInstructionList(navController)
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                        IconButton(onClick = {
                            viewModel.toScan(navController)
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            content = { innerPadding ->
                LazyColumn(
                    contentPadding = innerPadding,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ================= WELLNESS WATCH CARD =================
                    item {
                        WellnessCard(bleViewModel)
                    }

                    // ================= TODAY'S STATS =================
                    item {
                        TodayStatsRow(bleViewModel)
                    }

                    // ================= HEALTH METRICS GRID =================
                    item {
                        HealthMetricsGrid(bleViewModel)
                    }

                    // ================= EDIT DATA CARD BUTTON =================
//                    item {
//                        TextButton(
//                            onClick = { viewModel.toScan(navController) },
//                            modifier = Modifier.fillMaxWidth()
//                        ) {
//                            Text("Edit data card")
//                        }
//                    }
                }
            }
        )
    }
}

@Composable
fun WellnessCard(bleViewModel: BleViewModel) {
    val bleDevice = bleViewModel.bleDevice
    val isConnected = bleDevice?.get() != null

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFB8E6D5)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = if (isConnected) bleViewModel.getDeviceName().uppercase()
                else "KARIO WELLNESS WATCH",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E5A3C)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isConnected)
                    "Connected • ${bleDevice!!.get()!!.mac}"
                else
                    "Wear 2 more nights for Runmefit AI to learn your sleep pattern",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF1E5A3C).copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun TodayStatsRow(bleViewModel: BleViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(
            icon = Icons.Default.DirectionsWalk,
            value = bleViewModel.bleHealthResponseLabel.value["Total step count"]?.toString() ?:"_",
            label = "steps",
            iconColor = Color(0xFF34A853)
        )
        StatItem(
            icon = Icons.Default.Place,
            value = bleViewModel.bleHealthResponseLabel.value["Total distance (m)"]?.toString() ?:"_",
            label = "miles",
            iconColor = Color(0xFF4285F4)
        )
        StatItem(
            icon = Icons.Default.LocalFireDepartment,
            value = bleViewModel.bleHealthResponseLabel.value["Total calories (Cal)"]?.toString() ?:"_",
            label = "kcal",
            iconColor = Color(0xFFEA4335)
        )
    }
}

@Composable
fun StatItem(
    icon: ImageVector,
    value: String,
    label: String,
    iconColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
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
fun HealthMetricsGrid(bleViewModel: BleViewModel) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HealthMetricCard(
                title = "Heart rate",
                value = bleViewModel.bleHealthResponseLabel.value["Current heart rate"]?.toString() ?:"No Data",
                icon = Icons.Default.Favorite,
                iconColor = Color(0xFFEA4335),
                modifier = Modifier.weight(1f)
            )
            HealthMetricCard(
                title = "Sleep",
                value = bleViewModel.bleHealthResponseLabel.value["Total sleep duration (minutes)"]?.toString() ?:"No Data",
                icon = Icons.Default.Bedtime,
                iconColor = Color(0xFF9C27B0),
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HealthMetricCard(
                title = "Sports records",
                value = "No data",
                icon = Icons.Default.DirectionsRun,
                iconColor = Color(0xFFFF9800),
                modifier = Modifier.weight(1f)
            )
            HealthMetricCard(
                title = "Mood tracking",
                value = "No data",
                icon = Icons.Default.EmojiEmotions,
                iconColor = Color(0xFF2196F3),
                modifier = Modifier.weight(1f)
            )
        }

        // Row 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HealthMetricCard(
                title = "Weight",
                value = "No data",
                icon = Icons.Default.MonitorWeight,
                iconColor = Color(0xFF00BCD4),
                modifier = Modifier.weight(1f)
            )
            HealthMetricCard(
                title = "Blood oxygen",
                value = bleViewModel.bleHealthResponseLabel.value["Current blood oxygen"]?.toString() ?:"No Data",
                icon = Icons.Default.Opacity,
                iconColor = Color(0xFFEA4335),
                modifier = Modifier.weight(1f)
            )
        }

        // Row 4
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HealthMetricCard(
                title = "Blood Glucose",
                value = bleViewModel.bleHealthResponseLabel.value["Current blood sugar"]?.toString() ?:"No Data",
                icon = Icons.Default.WaterDrop,
                iconColor = Color(0xFFFFC107),
                modifier = Modifier.weight(1f)
            )
            HealthMetricCard(
                title = "Skin temperature",
                value = bleViewModel.bleHealthResponseLabel.value["Current temperature"]?.toString() ?:"No Data",
                icon = Icons.Default.Thermostat,
                iconColor = Color(0xFFE91E63),
                modifier = Modifier.weight(1f)
            )
        }

        // Row 5 (single item)
        HealthMetricCard(
            title = "HRV",
            value = "No data",
            icon = Icons.Default.Timeline,
            iconColor = Color(0xFFFF5252),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun HealthMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
