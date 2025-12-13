package com.starmax.sdkdemo.pages

import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.KeyboardArrowLeft
import androidx.compose.material.icons.twotone.Bluetooth
import androidx.compose.material.icons.twotone.Search
import androidx.compose.material.icons.twotone.SearchOff
import androidx.compose.material.icons.twotone.SignalCellularAlt
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.starmax.sdkdemo.ui.theme.AppTheme
import com.starmax.sdkdemo.viewmodel.BleViewModel
import com.starmax.sdkdemo.viewmodel.ScanViewModel

@Composable
fun ScanPage(navController: NavController, viewModel: ScanViewModel = viewModel()){
    DisposableEffect(Unit){
        onDispose {
            viewModel.stopScan()
        }
    }

    val bleViewModel : BleViewModel = viewModel(LocalContext.current as ComponentActivity)

    ScanPageView(navController,viewModel,bleViewModel)
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanPageView(navController: NavController, viewModel: ScanViewModel = viewModel(), bleViewModel : BleViewModel) {

    AppTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(text = "Bluetooth search")
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            navController.popBackStack()
                        }) {
                            Icon(Icons.Sharp.KeyboardArrowLeft, contentDescription = "return")
                        }
                    },
                )
            }
        ) { innerPadding ->

            LazyColumn(
                contentPadding = innerPadding
            ) {
                //==== bluetooth name field =====
                item{

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(
                            horizontal = 15.dp
                        )
                    ) {
                        OutlinedTextField(value = viewModel.searchName, onValueChange = {
                            viewModel.searchName = it
                        },
                            label = {
                                Text(text = "Bluetooth name", style = MaterialTheme.typography.labelSmall)
                            },
                            textStyle = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(text = "GTS5",style = MaterialTheme.typography.labelSmall)
                            },trailingIcon = {
                                Icon(
                                    if(viewModel.isScanning) Icons.TwoTone.SearchOff else Icons.TwoTone.Search,
                                    modifier = Modifier.clickable {
                                        viewModel.startScan()    //todo <=== start scan method
                                    },
                                    contentDescription = "search"
                                )
                            })

                    }

                }
                //==== Mac address name field =====
                item{
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 15.dp)
                    ) {
                        OutlinedTextField(value = viewModel.searchMac, onValueChange = {
                            viewModel.searchMac = it
                        },
                            label = {
                                Text(text = "Mac address", style = MaterialTheme.typography.labelSmall)
                            },
                            textStyle = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(text = "",style = MaterialTheme.typography.labelSmall)
                            },trailingIcon = {
                                Icon(
                                    if(viewModel.isScanning) Icons.TwoTone.SearchOff else Icons.TwoTone.Search,
                                    modifier = Modifier.clickable {
                                        viewModel.startScan()
                                    },
                                    contentDescription = "search"
                                )
                            })

                    }

                }
                //==== bluetooth host name list =====
                items(viewModel.devices.size) { index ->
                    ListItem(
                        modifier = Modifier.clickable {
                            bleViewModel.connect(viewModel.devices[index]) //==== connect to device ====
                            navController.popBackStack()
                        },
                        headlineContent = { Text(text = viewModel.getDeviceName(index)) },
                        overlineContent = { Text(text = viewModel.devices[index].mac) },
                        supportingContent = {
                            (if (viewModel.broadcast.contains(viewModel.devices[index].mac)) viewModel.broadcast[viewModel.devices[index].mac] else "")?.let {
                                Text(text = it)
                            }
                        },
                        leadingContent = { Icon(Icons.TwoTone.Bluetooth, contentDescription = "") },
                        trailingContent = {
                            Icon(
                                Icons.TwoTone.SignalCellularAlt,
                                contentDescription = ""
                            )
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun PreviewScanPage() {
    val navController = rememberNavController()
    ScanPageView(navController,ScanViewModel(),BleViewModel())
}