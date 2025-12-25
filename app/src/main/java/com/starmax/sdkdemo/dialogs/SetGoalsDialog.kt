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
import com.starmax.sdkdemo.viewmodel.GoalsViewModel
import com.starmax.sdkdemo.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SetGoalsDialog() {
    val viewModel: GoalsViewModel = koinViewModel()
    val homeViewModel: HomeViewModel by lazyKoinViewModel()

    LaunchedEffect(Unit) {
        viewModel.getGoalData()  // Fetch get goal data when the composable is first displayed
    }

    SetGoalsDialogView(homeViewModel = homeViewModel, viewModel = viewModel)
}

@Composable
fun SetGoalsDialogView(homeViewModel: HomeViewModel, viewModel: GoalsViewModel) {

    Dialog(
        onDismissRequest = { homeViewModel.toggleGoals() }) {
        Card(
        ) {
            Column(
                modifier = Modifier.padding(15.dp)
            ) {
                Text(
                    text = "Set Daily Activity Goals",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(15.dp)
                )
                Divider()
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Step Goal", style = MaterialTheme.typography.labelSmall)
                    TextField(value = viewModel.steps.toString(), onValueChange = {
                        viewModel.steps = it.toIntOrNull() ?: 10000
                    },
                        textStyle = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.offset(x = 15.dp),
                        placeholder = {
                            Text(text = "Step Goal", style = MaterialTheme.typography.labelSmall)
                        })
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Calories (kcal)", style = MaterialTheme.typography.labelSmall)
                    TextField(value = viewModel.heat.toString(), onValueChange = {
                        viewModel.heat = it.toIntOrNull() ?: 10000
                    },
                        textStyle = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.offset(x = 15.dp),
                        placeholder = {
                            Text(text = "Calories", style = MaterialTheme.typography.labelSmall)
                        })
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Distance (km)", style = MaterialTheme.typography.labelSmall)
                    TextField(value = viewModel.distance.toString(), onValueChange = {
                        viewModel.distance = it.toIntOrNull() ?: 10
                    },
                        textStyle = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.offset(x = 15.dp),
                        placeholder = {
                            Text(text = "Distance", style = MaterialTheme.typography.labelSmall)
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
                        Text(text = "Cancel")
                    }
                    ElevatedButton(
                        onClick = {
                            viewModel.setGoalData()
                            homeViewModel.toggleGoals()
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
fun PreviewSetGoalsDialog() {
    SetGoalsDialogView(viewModel = GoalsViewModel(), homeViewModel = HomeViewModel())
}