package com.starmax.sdkdemo.pages

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.KeyboardArrowLeft
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
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
import com.starmax.sdkdemo.viewmodel.BleViewModel
import com.starmax.sdkdemo.viewmodel.HomeViewModel

@Composable
fun HealthHistoryPage(navController: NavController) {
    val viewModel: BleViewModel by lazyKoinViewModel()

    HealthHistoryPageView(navController = navController, bleViewModel = viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HealthHistoryPageView(navController: NavController, bleViewModel: BleViewModel) {
    val bleResponseValue = bleViewModel.bleResponseLabel.value
    val viewModel: HomeViewModel by lazyKoinViewModel()

    AppTheme {
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
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(text = "Health History")
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            navController.popBackStack()
                        }) {
                            Icon(Icons.Sharp.KeyboardArrowLeft, contentDescription = "")
                        }
                    },
                )
            }
        ) { innerPadding ->

            LazyColumn(
                contentPadding = innerPadding,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize().padding(16.dp)
            ) {
                item {
                    Card(
                        shape = MaterialTheme.shapes.large,
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Device Response",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            HorizontalDivider(
                                Modifier,
                                DividerDefaults.Thickness,
                                DividerDefaults.color
                            )

                            Text(
                                text = bleResponseValue.ifBlank { "No data available" },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 20.sp,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}