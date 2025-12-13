package com.starmax.sdkdemo.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.starmax.sdkdemo.viewmodel.CustomSportModeOnOffViewModel
import com.starmax.sdkdemo.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun CustomSportModeOnOffDialog() {
    val viewModel: CustomSportModeOnOffViewModel = koinViewModel()
    val homeViewModel: HomeViewModel by lazyKoinViewModel()

    CustomSportModeOnOffDialogView(homeViewModel = homeViewModel, viewModel = viewModel)
}

@Composable
fun CustomSportModeOnOffDialogView(homeViewModel: HomeViewModel, viewModel: CustomSportModeOnOffViewModel) {
    AlertDialog(
        confirmButton = {
            ElevatedButton(
                onClick = {
                    viewModel.setData()
                    homeViewModel.toggleSportModeOnOffDialog()
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
                homeViewModel.toggleSportModeOnOffDialog()
            }) {
                Text(text = "取消")
            }
        },
        title = {
            Text(
                text = "设置运动模式",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(15.dp)
            )
        },
        onDismissRequest = { homeViewModel.toggleSportModeOnOffDialog() },
        text = {
            LazyColumn(
                modifier = Modifier.padding(15.dp)
            ) {
                item {
                    Row(
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(text = "运动模式", style = MaterialTheme.typography.labelMedium)
                        LazyVerticalGrid(
                            modifier = Modifier.height(350.dp),
                            columns = GridCells.Fixed(2)
                        ) {
                            val sportList = mapOf(
                                0x00 to "室内跑步",
                                0x01 to "户外跑步",
                                0x03 to "户外骑行",
                                0x04 to "散步",
                                0x05 to "跳绳",
                                0x06 to "足球",
                                0x07 to "羽毛球",
                                0x09 to "篮球",
                                0x0A to "椭圆机",
                                0x0B to "徒步",
                                0x0C to "瑜伽",
                                0x0D to "力量训练",
                                0x0E to "登山",
                                0x0F to "自由活动",
                                0x10 to "户外步行",
                                0x12 to "室内骑车"
                            )

                            val keys = sportList.keys.toList()

                            items(keys.size) { i ->

                                val key = keys[i]
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconToggleButton(checked = viewModel.type == key, onCheckedChange = {
                                        viewModel.type = key
                                    }) {
                                        Icon(
                                            if (viewModel.type == key) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                                            sportList[key]!!
                                        )
                                    }

                                    Text(text = sportList[key]!!, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "开关", style = MaterialTheme.typography.labelMedium)
                        Switch(checked = viewModel.onOff, onCheckedChange = {
                            viewModel.onOff = it
                        })
                    }
                }
            }
        })
}

@Preview(showBackground = true)
@Composable
fun PreviewCustomSportModeOnOffDialog() {
    CustomSportModeOnOffDialogView(viewModel = CustomSportModeOnOffViewModel(), homeViewModel = HomeViewModel())
}