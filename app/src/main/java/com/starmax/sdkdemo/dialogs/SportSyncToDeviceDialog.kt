package com.starmax.sdkdemo.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.CheckBox
import androidx.compose.material.icons.twotone.CheckBoxOutlineBlank
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.starmax.sdkdemo.pages.lazyKoinViewModel
import com.starmax.sdkdemo.viewmodel.HomeViewModel
import com.starmax.sdkdemo.viewmodel.SportSyncToDeviceViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SportSyncToDeviceDialog() {
    val viewModel: SportSyncToDeviceViewModel = koinViewModel()
    val homeViewModel: HomeViewModel by lazyKoinViewModel()

    SportSyncToDeviceDialogView(homeViewModel = homeViewModel, viewModel = viewModel)
}

@Composable
fun SportSyncToDeviceDialogView(homeViewModel: HomeViewModel, viewModel: SportSyncToDeviceViewModel) {
    AlertDialog(
        modifier = Modifier.fillMaxHeight(1f),
        confirmButton = {
            ElevatedButton(
                onClick = {
                    viewModel.randomLocationData()
                }, colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.offset(15.dp)
            ) {
                Text(text = "经纬度生成")
            }
            ElevatedButton(
                onClick = {
                    viewModel.setData()
                    homeViewModel.toggleSportSyncToDeviceDialog()
                }, colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.offset(15.dp)
            ) {
                Text(text = "确定")
            }

        },
        dismissButton = {
            OutlinedButton(onClick = {
                homeViewModel.toggleSportSyncToDeviceDialog()
            }) {
                Text(text = "取消")
            }
        },
        title = {
            Text(
                text = "设置运动",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(15.dp)
            )
        },
        onDismissRequest = { homeViewModel.toggleSportSyncToDeviceDialog() },
        text = {
            Column(
            ) {
                Row(
                    verticalAlignment = Alignment.Top
                ) {
                    Text(text = "运动类型", style = MaterialTheme.typography.labelMedium)
                    LazyVerticalGrid(
                        modifier = Modifier.height(150.dp),
                        columns = GridCells.Fixed(3)) {
                        val typeList = listOf(
                            "户外跑步",
                            "户外步行",
                            "户外骑行",
                            "遛狗",
                        )

                        items(typeList.size, key = {
                            typeList[it]
                        }) { i ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconToggleButton(checked = viewModel.sportType == i + 1, onCheckedChange = { it ->
                                    viewModel.sportType = i + 1
                                }) {
                                    Icon(
                                        if (viewModel.sportType == i + 1) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                                        typeList[i]
                                    )
                                }

                                Text(text = typeList[i], style = MaterialTheme.typography.labelSmall)
                            }

                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.Top
                ) {
                    Text(text = "状态", style = MaterialTheme.typography.labelMedium)
                    LazyVerticalGrid(
                        modifier = Modifier.height(100.dp),
                        columns = GridCells.Fixed(3)) {
                        val statusList = listOf(
                            "开启",
                            "进行中",
                            "暂停",
                            "恢复",
                            "结束",
                            "设置目标"
                        )

                        items(statusList.size, key = {
                            statusList[it]
                        }) { i ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconToggleButton(checked = viewModel.sportStatus == i + 1, onCheckedChange = { it ->
                                    viewModel.sportStatus = i + 1
                                }) {
                                    Icon(
                                        if (viewModel.sportStatus == i + 1) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                                        statusList[i]
                                    )
                                }

                                Text(text = statusList[i], style = MaterialTheme.typography.labelSmall)
                            }

                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "距离", style = MaterialTheme.typography.labelSmall)
                    TextField(value = viewModel.sportDistance.toString(), onValueChange = {
                        viewModel.sportDistance = it.toIntOrNull() ?: 0
                    },
                        textStyle = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.offset(x = 15.dp),
                        placeholder = {
                            Text(text = "距离", style = MaterialTheme.typography.labelSmall)
                        })
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "速度", style = MaterialTheme.typography.labelSmall)
                    TextField(value = viewModel.speed.toString(), onValueChange = {
                        viewModel.speed = it.toIntOrNull() ?: 0
                    },
                        textStyle = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.offset(x = 15.dp),
                        placeholder = {
                            Text(text = "速度", style = MaterialTheme.typography.labelSmall)
                        })
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "目标距离", style = MaterialTheme.typography.labelSmall)
                    TextField(value = viewModel.goalDistance.toString(), onValueChange = {
                        viewModel.goalDistance = it.toIntOrNull() ?: 0
                    },
                        textStyle = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.offset(x = 15.dp),
                        placeholder = {
                            Text(text = "目标距离", style = MaterialTheme.typography.labelSmall)
                        })
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "目标热量", style = MaterialTheme.typography.labelSmall)
                    TextField(value = viewModel.goalHeat.toString(), onValueChange = {
                        viewModel.goalHeat = it.toIntOrNull() ?: 0
                    },
                        textStyle = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.offset(x = 15.dp),
                        placeholder = {
                            Text(text = "目标热量", style = MaterialTheme.typography.labelSmall)
                        })
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "目标分钟", style = MaterialTheme.typography.labelSmall)
                    TextField(value = viewModel.goalMinute.toString(), onValueChange = {
                        viewModel.goalMinute = it.toIntOrNull() ?: 0
                    },
                        textStyle = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.offset(x = 15.dp),
                        placeholder = {
                            Text(text = "目标分钟", style = MaterialTheme.typography.labelSmall)
                        })
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "轨迹："+viewModel.locationData.map { "(${it.lat},${it.lng})" }.joinToString(","), style = MaterialTheme.typography.labelSmall)
                }
            }
        })
}

@Preview(showBackground = true)
@Composable
fun PreviewSportSyncToDeviceDialog() {
    SportSyncToDeviceDialogView(viewModel = SportSyncToDeviceViewModel(), homeViewModel = HomeViewModel())
}