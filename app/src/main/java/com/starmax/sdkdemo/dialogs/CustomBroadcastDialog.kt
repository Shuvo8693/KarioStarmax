package com.starmax.sdkdemo.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.starmax.sdkdemo.pages.lazyKoinViewModel
import com.starmax.sdkdemo.viewmodel.CustomBroadcastViewModel
import com.starmax.sdkdemo.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun CustomBroadcastDialog() {
    val viewModel: CustomBroadcastViewModel = koinViewModel()
    val homeViewModel: HomeViewModel by lazyKoinViewModel()

    LaunchedEffect(Unit) {
        viewModel.getData()
    }

    CustomBroadcastDialogView(homeViewModel = homeViewModel, viewModel = viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomBroadcastDialogView(homeViewModel: HomeViewModel,viewModel: CustomBroadcastViewModel) {

    Dialog(
        onDismissRequest = { homeViewModel.toggleCustomBroadcastDialog() }) {
        Card(
        ) {
            Column(
                modifier = Modifier.padding(15.dp)
            ) {
                Text(
                    text = "设置广播",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(15.dp)
                )
                Divider()
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "蓝牙广播间隔(ms)", style = MaterialTheme.typography.labelSmall)
                    TextField(value = viewModel.interval.toString(), onValueChange = {
                        viewModel.interval = it.toIntOrNull() ?: 1000
                    },
                        textStyle = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.offset(x = 15.dp),
                        placeholder = {
                            Text(text = "蓝牙广播间隔",style = MaterialTheme.typography.labelSmall)
                        })
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "长按持续时间(千卡)", style = MaterialTheme.typography.labelSmall)
                    TextField(value = viewModel.longTouchTime.toString(), onValueChange = {
                        viewModel.longTouchTime = it.toIntOrNull() ?: 3
                    },
                        textStyle = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.offset(x = 15.dp),
                        placeholder = {
                            Text(text = "长按持续时间",style = MaterialTheme.typography.labelSmall)
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
                        homeViewModel.toggleGoals()
                    }) {
                        Text(text = "取消")
                    }
                    ElevatedButton(
                        onClick = {
                            viewModel.setData()
                            homeViewModel.toggleCustomBroadcastDialog()
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
fun PreviewCustomBroadcastDialog() {
    CustomBroadcastDialogView(viewModel = CustomBroadcastViewModel(), homeViewModel = HomeViewModel())
}