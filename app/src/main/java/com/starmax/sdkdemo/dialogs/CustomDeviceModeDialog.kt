package com.starmax.sdkdemo.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.CheckBox
import androidx.compose.material.icons.twotone.CheckBoxOutlineBlank
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.starmax.bluetoothsdk.data.CallControlType
import com.starmax.sdkdemo.pages.lazyKoinViewModel
import com.starmax.sdkdemo.viewmodel.CustomDeviceModeViewModel
import com.starmax.sdkdemo.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun CustomDeviceModeDialog() {
    val viewModel: CustomDeviceModeViewModel = koinViewModel()
    val homeViewModel: HomeViewModel by lazyKoinViewModel()

    CustomDeviceModeDialogView(homeViewModel = homeViewModel, viewModel = viewModel)
}

@Composable
fun CustomDeviceModeDialogView(homeViewModel: HomeViewModel, viewModel: CustomDeviceModeViewModel) {
    Dialog(
        onDismissRequest = { homeViewModel.toggleCustomDeviceModeDialog() }) {
        Card(
        ) {
            Column(
                modifier = Modifier.padding(15.dp)
            ) {
                Text(
                    text = "Watch Mode",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(15.dp)
                )
                Divider()
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconToggleButton(
                        checked = viewModel.mode == 0,
                        onCheckedChange = { _ ->
                            viewModel.mode = 0
                        }) {
                        Icon(
                            if (viewModel.mode == 0) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                            "Normal Mode"
                        )
                    }
                    Text(text = "Normal Mode", style = MaterialTheme.typography.labelSmall)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconToggleButton(
                        checked = viewModel.mode == 1,
                        onCheckedChange = { _ ->
                            viewModel.mode = 1
                        }) {
                        Icon(
                            if (viewModel.mode == 1) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                            "School Mode"
                        )
                    }
                    Text(text = "School Mode", style = MaterialTheme.typography.labelSmall)
                }
                Divider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = {
                        homeViewModel.toggleCustomDeviceModeDialog()
                    }) {
                        Text(text = "Cancel")
                    }
                    ElevatedButton(
                        onClick = {
                            viewModel.setData()
                            homeViewModel.toggleCustomDeviceModeDialog()
                        }, colors = ButtonDefaults.elevatedButtonColors(
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

@Preview(showBackground = true)
@Composable
fun PreviewCustomDeviceModeDialog() {
    CustomDeviceModeDialogView(viewModel = CustomDeviceModeViewModel(), homeViewModel = HomeViewModel())
}