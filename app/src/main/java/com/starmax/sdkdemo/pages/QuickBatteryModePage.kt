package com.starmax.sdkdemo.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.starmax.sdkdemo.PreviewInit
import com.starmax.sdkdemo.ui.theme.AppTheme
import com.starmax.sdkdemo.viewmodel.QuickBatteryModeViewModel

@Composable
fun QuickBatteryModePage(navController: NavController) {
    val viewModel: QuickBatteryModeViewModel by lazyKoinViewModel()

    QuickBatteryModeView(navController = navController,viewModel = viewModel)
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickBatteryModeView(navController: NavController, viewModel: QuickBatteryModeViewModel) {

    val context = LocalContext.current

    AppTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(text = "耗电测试")
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            navController.popBackStack()
                        }) {
                            Icon(Icons.Sharp.KeyboardArrowLeft, contentDescription = "返回")
                        }
                    },
                )
            }
        ) { innerPadding ->
            LazyColumn(
                contentPadding = innerPadding
            ) {
                item{
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedButton(onClick = {
                            viewModel.setQuickBatteryMode()
                        }) {
                            Text(text = "进入快速耗电模式")

                        }
                    }
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = "设备定时点亮间隔", style = MaterialTheme.typography.labelSmall)
                        TextField(value = viewModel.light.toString(), onValueChange = {
                            viewModel.light = it.toIntOrNull() ?: 0
                        },
                            textStyle = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.weight(1.0f).padding(15.dp),
                            placeholder = {
                                Text(text = "设备定时点亮间隔",style = MaterialTheme.typography.labelSmall)
                            })
                        OutlinedButton(onClick = {
                            viewModel.setBatteryLight()
                        }) {
                            Text(text = "发送")
                        }
                    }
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = "获取设备电量:${viewModel.scale},电压:${viewModel.v}", style = MaterialTheme.typography.labelSmall)
                        OutlinedButton(onClick = {
                            viewModel.getBatteryV()
                        }) {
                            Text(text = "发送")
                        }
                    }
                }
                item{
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedButton(onClick = {
                            viewModel.offQuickBatteryMode(0x01)
                        }) {
                            Text(text = "退出快速耗电模式")

                        }
                    }
                }
                item{
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedButton(onClick = {
                            viewModel.offQuickBatteryMode(0x02)
                        }) {
                            Text(text = "退出设备定时点亮间隔")

                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewQuickBatteryModePage() {
    PreviewInit() {
        val navController = rememberNavController()
        QuickBatteryModePage(navController)
    }

}