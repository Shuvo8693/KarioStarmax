package com.starmax.sdkdemo.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.CheckBox
import androidx.compose.material.icons.twotone.CheckBoxOutlineBlank
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.starmax.bluetoothsdk.data.MessageType
import com.starmax.sdkdemo.pages.lazyKoinViewModel
import com.starmax.sdkdemo.viewmodel.HomeViewModel
import com.starmax.sdkdemo.viewmodel.MessageViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun MessageDialog() {
    val viewModel: MessageViewModel = koinViewModel()
    val homeViewModel: HomeViewModel by lazyKoinViewModel()

    MessageDialogView(homeViewModel = homeViewModel,viewModel = viewModel)
}

@Composable
fun MessageDialogView(homeViewModel: HomeViewModel, viewModel: MessageViewModel) {
    Dialog(
        onDismissRequest = { homeViewModel.toggleMessageOpen() }) {
        Card(
        ) {
            Column(
                modifier = Modifier.padding(15.dp)
            ) {
                Text(
                    text = "消息",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(15.dp)
                )
                Divider()
                LazyVerticalGrid(columns = GridCells.Fixed(3)){
                    items(MessageType.values().size, key = {
                        MessageType.values()[it]
                    }) {
                            i ->
                        val key = MessageType.values()[i]

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            IconToggleButton(
                                checked = viewModel.selectedMessageType == key,
                                onCheckedChange = { it ->
                                    viewModel.selectedMessageType = key
                                }) {
                                Icon(
                                    if (viewModel.selectedMessageType == key) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                                    key.name
                                )
                            }
                            Text(text = key.name, style = MaterialTheme.typography.labelSmall)
                        }

                    }
                }
                TextField(value = viewModel.title, onValueChange = {
                    viewModel.title = it
                },
                    textStyle = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.offset(x = 15.dp),
                    placeholder = {
                        Text(text = "标题",style = MaterialTheme.typography.labelSmall)
                    })
                TextField(value = viewModel.content, onValueChange = {
                    viewModel.content = it
                },
                    textStyle = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.offset(x = 15.dp),
                    placeholder = {
                        Text(text = "内容",style = MaterialTheme.typography.labelSmall)
                    })
                TextField(value = viewModel.messageId, onValueChange = {
                    viewModel.messageId = it
                },
                    textStyle = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.offset(x = 15.dp),
                    placeholder = {
                        Text(text = "消息id",style = MaterialTheme.typography.labelSmall)
                    })
                Divider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 15.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = {
                        homeViewModel.toggleMessageOpen()
                    }) {
                        Text(text = "取消")
                    }
                    ElevatedButton(
                        onClick = {
                            viewModel.sendToBle()
                            homeViewModel.toggleMessageOpen()
                        }, colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.padding(horizontal = 15.dp)
                    ) {
                        Text(text = "消息")
                    }
                    ElevatedButton(
                        onClick = {
                            viewModel.sendToExtendBle()
                            homeViewModel.toggleMessageOpen()
                        }, colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.offset(15.dp)
                    ) {
                        Text(text = "扩展消息")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMessageDialog() {
    MessageDialogView(viewModel = MessageViewModel(), homeViewModel = HomeViewModel())
}