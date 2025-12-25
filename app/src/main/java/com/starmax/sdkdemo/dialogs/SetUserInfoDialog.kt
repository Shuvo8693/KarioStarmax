package com.starmax.sdkdemo.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.CheckBox
import androidx.compose.material.icons.twotone.CheckBoxOutlineBlank
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.starmax.sdkdemo.pages.lazyKoinViewModel
import com.starmax.sdkdemo.viewmodel.HomeViewModel
import com.starmax.sdkdemo.viewmodel.UserInfoViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SetUserInfoDialog() {
    val viewModel: UserInfoViewModel = koinViewModel()
    val homeViewModel: HomeViewModel by lazyKoinViewModel()

    LaunchedEffect(Unit) {
        viewModel.getData()
    }

    SetUserInfoDialogView(homeViewModel = homeViewModel, viewModel = viewModel)
}

@Composable
fun SetUserInfoDialogView(homeViewModel: HomeViewModel, viewModel: UserInfoViewModel) {
    Dialog(
        onDismissRequest = { homeViewModel.toggleUserInfo() }) {
        Card(
        ) {
            Column(
                modifier = Modifier.padding(15.dp)
            ) {
                Text(
                    text = "Set User Info",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(15.dp)
                )
                Divider()
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Gender", style = MaterialTheme.typography.labelMedium)
                    IconToggleButton(checked = viewModel.sex == 0, onCheckedChange = { _ ->
                        viewModel.sex = 0
                    }) {
                        Icon(if(viewModel.sex == 0) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank, "Female")
                    }
                    Text(text = "Female", style = MaterialTheme.typography.labelSmall)

                    IconToggleButton(checked = viewModel.sex == 1, onCheckedChange = { _ ->
                        viewModel.sex = 1
                    }) {
                        Icon(if(viewModel.sex == 1) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank, "Male")
                    }
                    Text(text = "Male", style = MaterialTheme.typography.labelSmall)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Age", style = MaterialTheme.typography.labelSmall)
                    TextField(value = viewModel.age.toString(), onValueChange = {
                        viewModel.age = it.toIntOrNull() ?: 20
                    },
                        textStyle = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.offset(x = 15.dp),
                        placeholder = {
                            Text(text = "Age", style = MaterialTheme.typography.labelSmall)
                        })
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Height (cm)", style = MaterialTheme.typography.labelSmall)
                    TextField(value = viewModel.height.toString(), onValueChange = {
                        viewModel.height = it.toIntOrNull() ?: 180
                    },
                        textStyle = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.offset(x = 15.dp),
                        placeholder = {
                            Text(text = "Height", style = MaterialTheme.typography.labelSmall)
                        })
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Weight (0.1kg)", style = MaterialTheme.typography.labelSmall)
                    TextField(value = viewModel.weight.toString(), onValueChange = {
                        viewModel.weight = it.toIntOrNull() ?: 75
                    },
                        textStyle = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.offset(x = 15.dp),
                        placeholder = {
                            Text(text = "Weight", style = MaterialTheme.typography.labelSmall)
                        })
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Wearing Style", style = MaterialTheme.typography.labelMedium)
                    IconToggleButton(checked = viewModel.handWear == 0, onCheckedChange = { _ ->
                        viewModel.handWear = 0
                    }) {
                        Icon(
                            if (viewModel.handWear == 0) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                            "Left Hand"
                        )
                    }
                    Text(text = "Left Hand", style = MaterialTheme.typography.labelSmall)

                    IconToggleButton(checked = viewModel.handWear == 1, onCheckedChange = { _ ->
                        viewModel.handWear = 1
                    }) {
                        Icon(
                            if (viewModel.handWear == 1) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                            "Right Hand"
                        )
                    }
                    Text(text = "Right Hand", style = MaterialTheme.typography.labelSmall)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconToggleButton(checked = viewModel.isGTS10, onCheckedChange = { _ ->
                        viewModel.isGTS10 = !viewModel.isGTS10
                    }) {
                        Icon(if(viewModel.isGTS10) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank, "Toggle")
                    }
                    Text(text = "Is GTS10?", style = MaterialTheme.typography.labelSmall)
                }
                Divider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = {
                        homeViewModel.toggleUserInfo()
                    }) {
                        Text(text = "Cancel")
                    }
                    ElevatedButton(
                        onClick = {
                            viewModel.setData()
                            homeViewModel.toggleUserInfo()
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

@Preview(showBackground = false)
@Composable
fun PreviewSetUserInfoDialog() {
    SetUserInfoDialogView(viewModel = UserInfoViewModel(), homeViewModel = HomeViewModel())
}