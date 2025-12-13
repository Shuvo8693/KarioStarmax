package com.starmax.sdkdemo.dialogs

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.android.material.timepicker.MaterialTimePicker
import com.starmax.sdkdemo.pages.lazyKoinViewModel
import com.starmax.sdkdemo.viewmodel.CustomDeviceShakeTimeViewModel
import com.starmax.sdkdemo.viewmodel.HeartRateViewModel
import com.starmax.sdkdemo.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDeviceShakeTimeDialog() {
    val activity = LocalContext.current as AppCompatActivity
    val viewModel: CustomDeviceShakeTimeViewModel = koinViewModel()
    val homeViewModel: HomeViewModel by lazyKoinViewModel()

    LaunchedEffect(Unit) {
        viewModel.getData()
    }

    Dialog(
        onDismissRequest = { homeViewModel.toggleCustomDeviceShakeTimeDialog() }) {
        Card(
        ) {
            Column(
                modifier = Modifier.padding(15.dp)
            ) {
                Text(
                    text = "震动",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(15.dp)
                )
                Divider()
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "震动时长", style = MaterialTheme.typography.labelSmall)
                    TextField(value = viewModel.time.toString(), onValueChange = {
                        viewModel.time = it.toIntOrNull() ?: 500
                    },
                        textStyle = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.offset(x = 15.dp),
                        placeholder = {
                            Text(text = "震动时长(单位ms)", style = MaterialTheme.typography.labelSmall)
                        })
                }
                Divider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = {
                        homeViewModel.toggleCustomDeviceShakeTimeDialog()
                    }) {
                        Text(text = "取消")
                    }
                    ElevatedButton(
                        onClick = {
                            viewModel.setData()
                            homeViewModel.toggleCustomDeviceShakeTimeDialog()
                        }, colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.offset(15.dp)
                    ) {
                        Text(text = "确定")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCustomDeviceShakeTimeDialog() {
    CustomDeviceShakeTimeDialog()
}