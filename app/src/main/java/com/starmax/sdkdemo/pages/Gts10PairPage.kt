package com.starmax.sdkdemo.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.KeyboardArrowLeft
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.starmax.sdkdemo.PreviewInit
import com.starmax.sdkdemo.ui.theme.AppTheme
import com.starmax.sdkdemo.viewmodel.Gts10PairViewModel

@Composable
fun Gts10PairPage(navController: NavController) {
    val viewModel: Gts10PairViewModel by lazyKoinViewModel()

    Gts10PairPageView(navController = navController,viewModel = viewModel)
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Gts10PairPageView(navController: NavController, viewModel: Gts10PairViewModel) {

    val context = LocalContext.current

    AppTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(text = "\n" + "GTS10 pairing")
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
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        ElevatedButton(
                            onClick = {
                                viewModel.startPair()
                            }, colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.offset(15.dp).padding(horizontal = 15.dp)
                        ) {
                            Text(text = "发起配对")
                        }
                    }
                }
                item{
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if(viewModel.deviceIsChecked){
                            ElevatedButton(
                                onClick = {
                                    viewModel.pairReply(false)
                                }, colors = ButtonDefaults.elevatedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.offset(15.dp)
                            ) {
                                Text(text = "手机取消配对")
                            }
                            ElevatedButton(
                                onClick = {
                                    viewModel.pairReply(true)
                                }, colors = ButtonDefaults.elevatedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.offset(15.dp).padding(horizontal = 15.dp)
                            ) {
                                Text(text = "手机确认配对")
                            }
                        }
                    }
                }
                item {
                    if(viewModel.deviceIsChecked) {
                        Text(text = "UUID:${viewModel.uuid}")
                    }
                }
                item{
                    if(viewModel.deviceIsChecked) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(15.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            ElevatedButton(
                                onClick = {
                                    viewModel.generateUUID()
                                }, colors = ButtonDefaults.elevatedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.offset(15.dp)
                            ) {
                                Text(text = "生成uuid")
                            }
                            ElevatedButton(
                                onClick = {
                                    viewModel.getUUID()
                                }, colors = ButtonDefaults.elevatedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.offset(15.dp)
                            ) {
                                Text(text = "获取uuid")
                            }
                            ElevatedButton(
                                onClick = {
                                    viewModel.sendUUID()
                                }, colors = ButtonDefaults.elevatedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.offset(15.dp).padding(horizontal = 15.dp)
                            ) {
                                Text(text = "发送uuid")
                            }
                        }
                    }
                }
                item{
                    if(viewModel.deviceIsChecked) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(15.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            ElevatedButton(
                                onClick = {
                                    viewModel.unpair(false)
                                }, colors = ButtonDefaults.elevatedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.offset(15.dp)
                            ) {
                                Text(text = "解绑UUID全部置零")
                            }
                            ElevatedButton(
                                onClick = {
                                    viewModel.unpair(true)
                                }, colors = ButtonDefaults.elevatedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.offset(15.dp).padding(horizontal = 15.dp)
                            ) {
                                Text(text = "解绑并恢复出厂")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewGts10PairPage() {
    PreviewInit() {
        val navController = rememberNavController()
        Gts10PairPage(navController)
    }

}