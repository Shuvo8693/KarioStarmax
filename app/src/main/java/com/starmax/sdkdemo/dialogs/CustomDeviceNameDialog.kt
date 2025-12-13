package com.starmax.sdkdemo.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.starmax.sdkdemo.pages.lazyKoinViewModel
import com.starmax.sdkdemo.viewmodel.CustomDeviceNameViewModel
import com.starmax.sdkdemo.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun CustomDeviceNameDialog() {
    val viewModel: CustomDeviceNameViewModel = koinViewModel()
    val homeViewModel: HomeViewModel by lazyKoinViewModel()

    CustomDeviceNameDialogView(homeViewModel = homeViewModel,viewModel = viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDeviceNameDialogView(homeViewModel: HomeViewModel,viewModel: CustomDeviceNameViewModel) {
    Dialog(
        onDismissRequest = { homeViewModel.toggleCustomDeviceNameDialog() }) {
        Card(
        ) {
            Column(
                modifier = Modifier.padding(15.dp)
            ) {
                Text(
                    text = "手表设备名称",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(15.dp)
                )
                Divider()
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "名称", style = MaterialTheme.typography.labelSmall)
                    TextField(value = viewModel.deviceName, onValueChange = {
                        viewModel.deviceName = it
                    },
                        textStyle = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.offset(x = 15.dp),
                        placeholder = {
                            Text(text = "手表名称",style = MaterialTheme.typography.labelSmall)
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
                        homeViewModel.toggleCustomDeviceNameDialog()
                    }) {
                        Text(text = "取消")
                    }
                    ElevatedButton(
                        onClick = {
                            viewModel.setData()
                            homeViewModel.toggleCustomDeviceNameDialog()
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
fun PreviewCustomDeviceNameDialog() {
    CustomDeviceNameDialogView(viewModel = CustomDeviceNameViewModel(), homeViewModel = HomeViewModel())
}