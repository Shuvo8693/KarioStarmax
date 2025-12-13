package com.starmax.sdkdemo.dialogs

import com.starmax.sdkdemo.viewmodel.CustomDeviceShakeOnOffViewModel

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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.starmax.sdkdemo.pages.lazyKoinViewModel
import com.starmax.sdkdemo.viewmodel.HomeViewModel

@Composable
fun CustomDeviceShakeOnOffDialog() {
    val viewModel: CustomDeviceShakeOnOffViewModel = viewModel()
    val homeViewModel: HomeViewModel by lazyKoinViewModel()

    CustomDeviceShakeOnOffDialogView(homeViewModel = homeViewModel, viewModel = viewModel)
}
@Composable
fun CustomDeviceShakeOnOffDialogView(homeViewModel: HomeViewModel,viewModel: CustomDeviceShakeOnOffViewModel) {

    Dialog(
        onDismissRequest = { homeViewModel.toggleCustomDeviceShakeOnOffDialog() }) {
        Card(
        ) {
            Column(
                modifier = Modifier.padding(15.dp)
            ) {
                Text(
                    text = "开关",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(15.dp)
                )
                Divider()
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconToggleButton(checked = viewModel.shake, onCheckedChange = { it ->
                        viewModel.shake = !viewModel.shake
                    }) {
                        Icon(if(viewModel.shake) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank, "开关")
                    }
                    Text(text = "摇一摇开关", style = MaterialTheme.typography.labelSmall)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconToggleButton(checked = viewModel.hand, onCheckedChange = { it ->
                        viewModel.hand = !viewModel.hand
                    }) {
                        Icon(if(viewModel.hand) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank, "开关")
                    }
                    Text(text = "抬腕开关", style = MaterialTheme.typography.labelSmall)
                }
                Divider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = {
                        homeViewModel.toggleCustomDeviceShakeOnOffDialog()
                    }) {
                        Text(text = "取消")
                    }
                    ElevatedButton(
                        onClick = {
                            viewModel.setData()
                            homeViewModel.toggleCustomDeviceShakeOnOffDialog()
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
fun PreviewCustomDeviceShakeOnOffDialog() {
    CustomDeviceShakeOnOffDialogView(viewModel = CustomDeviceShakeOnOffViewModel(), homeViewModel = HomeViewModel())
}