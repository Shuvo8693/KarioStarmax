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
import com.starmax.sdkdemo.viewmodel.CallControlViewModel
import com.starmax.sdkdemo.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun CallControlDialog() {
    val viewModel: CallControlViewModel = koinViewModel()
    val homeViewModel: HomeViewModel by lazyKoinViewModel()

    CallControlDialogView(homeViewModel = homeViewModel, viewModel = viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallControlDialogView(homeViewModel: HomeViewModel, viewModel: CallControlViewModel) {
    Dialog(
        onDismissRequest = { homeViewModel.toggleCall() }) {
        Card(
        ) {
            Column(
                modifier = Modifier.padding(15.dp)
            ) {
                Text(
                    text = "Call Control",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(15.dp)
                )
                Divider()
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconToggleButton(
                        checked = viewModel.callControlType == CallControlType.HangUp,
                        onCheckedChange = { _ ->
                            viewModel.callControlType = CallControlType.HangUp
                        }) {
                        Icon(
                            if (viewModel.callControlType == CallControlType.HangUp) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                            "Hang Up"
                        )
                    }
                    Text(text = "Hang Up", style = MaterialTheme.typography.labelSmall)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconToggleButton(
                        checked = viewModel.callControlType == CallControlType.Answer,
                        onCheckedChange = { _ ->
                            viewModel.callControlType = CallControlType.Answer
                        }) {
                        Icon(
                            if (viewModel.callControlType == CallControlType.Answer) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                            "Answer"
                        )
                    }
                    Text(text = "Answer", style = MaterialTheme.typography.labelSmall)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconToggleButton(
                        checked = viewModel.callControlType == CallControlType.Incoming,
                        onCheckedChange = { _ ->
                            viewModel.callControlType = CallControlType.Incoming
                        }) {
                        Icon(
                            if (viewModel.callControlType == CallControlType.Incoming) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                            "Incoming Call"
                        )
                    }
                    Text(text = "Incoming", style = MaterialTheme.typography.labelSmall)
                    TextField(value = viewModel.callNumber, onValueChange = {
                        viewModel.callNumber = it
                    },
                        textStyle = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.offset(x = 15.dp),
                        placeholder = {
                            Text(text = "Number/Name", style = MaterialTheme.typography.labelSmall)
                        })
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconToggleButton(
                        checked = viewModel.callControlType == CallControlType.Exit,
                        onCheckedChange = { _ ->
                            viewModel.callControlType = CallControlType.Exit
                        }) {
                        Icon(
                            if (viewModel.callControlType == CallControlType.Exit) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                            "Outgoing Call"
                        )
                    }
                    Text(text = "Outgoing", style = MaterialTheme.typography.labelSmall)
                    TextField(value = viewModel.exitNumber, onValueChange = {
                        viewModel.exitNumber = it
                    },
                        textStyle = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.offset(x = 15.dp),
                        placeholder = {
                            Text(text = "Number/Name", style = MaterialTheme.typography.labelSmall)
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
                        homeViewModel.toggleCall()
                    }) {
                        Text(text = "Cancel")
                    }
                    ElevatedButton(
                        onClick = {
                            viewModel.send()
                            homeViewModel.toggleCall()
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
fun PreviewCallControlDialog() {
    CallControlDialogView(viewModel = CallControlViewModel(), homeViewModel = HomeViewModel())
}