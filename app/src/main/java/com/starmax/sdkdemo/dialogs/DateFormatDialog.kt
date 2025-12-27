package com.starmax.sdkdemo.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.starmax.sdkdemo.pages.lazyKoinViewModel
import com.starmax.sdkdemo.viewmodel.DateFormatViewModel
import com.starmax.sdkdemo.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun DateFormatDialog() {
    val viewModel: DateFormatViewModel = koinViewModel()
    val homeViewModel: HomeViewModel by lazyKoinViewModel()

    LaunchedEffect(Unit) {
        viewModel.getData()
    }

    DateFormatDialogView(homeViewModel = homeViewModel, viewModel = viewModel)
}

@Composable
fun DateFormatDialogView(homeViewModel: HomeViewModel, viewModel: DateFormatViewModel) {
    AlertDialog(
        confirmButton = {
            ElevatedButton(
                onClick = {
                    viewModel.setData()
                    homeViewModel.toggleDateFormatDialog()
                }, colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.offset(15.dp)
            ) {
                Text(text = "Confirm")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = {
                homeViewModel.toggleDateFormatDialog()
            }) {
                Text(text = "Cancel")
            }
        },
        title = {
            Text(
                text = "Date Format Settings",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(15.dp)
            )
        },
        onDismissRequest = { homeViewModel.toggleDateFormatDialog() },
        text = {
            LazyColumn(
            ) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Time System", style = MaterialTheme.typography.labelMedium)
                        IconToggleButton(
                            checked = viewModel.dateFormat == 0,
                            onCheckedChange = { _ ->
                                viewModel.dateFormat = 0
                            }) {
                            Icon(
                                if (viewModel.dateFormat == 0) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                                "Normal Order"
                            )
                        }
                        Text(text = "Normal", style = MaterialTheme.typography.labelSmall)

                        IconToggleButton(
                            checked = viewModel.dateFormat == 1,
                            onCheckedChange = { _ ->
                                viewModel.dateFormat = 1
                            }) {
                            Icon(
                                if (viewModel.dateFormat == 1) Icons.TwoTone.CheckBox else Icons.TwoTone.CheckBoxOutlineBlank,
                                "Reverse Order"
                            )
                        }
                        Text(text = "Reverse", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        })
}

@Preview(showBackground = true)
@Composable
fun PreviewDateFormatDialog() {
    DateFormatDialogView(viewModel = DateFormatViewModel(), homeViewModel = HomeViewModel())
}