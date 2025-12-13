package com.starmax.sdkdemo.pages

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.android.material.timepicker.MaterialTimePicker
import com.starmax.sdkdemo.PreviewInit
import com.starmax.sdkdemo.ui.theme.AppTheme
import com.starmax.sdkdemo.viewmodel.GoalsDayAndNightViewModel

@Composable
fun GoalsDayAndNightPage(navController: NavController) {
    val viewModel: GoalsDayAndNightViewModel by lazyKoinViewModel()

    LaunchedEffect(Unit) {
        viewModel.getData()
    }

    GoalsDayAndNightDialogView(navController = navController,viewModel = viewModel)
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsDayAndNightDialogView(navController: NavController, viewModel: GoalsDayAndNightViewModel) {

    val context = LocalContext.current

    AppTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(text = "朝朝暮暮")
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "朝朝步数", style = MaterialTheme.typography.labelSmall)
                        Text(text = viewModel.goalsDayAndNight.dayGoals.steps.toString(),style = MaterialTheme.typography.labelSmall)
                    }
                }
                item{
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "朝朝步数目标", style = MaterialTheme.typography.labelSmall)
                        TextField(value = viewModel.goalsDayAndNight.dayGoals.stepGoals.toString(), onValueChange = {
                            val builder = viewModel.goalsDayAndNight.dayGoals.toBuilder()
                            builder.stepGoals = it.toIntOrNull() ?: 0
                            viewModel.goalsDayAndNight.dayGoals = builder.build()
                            viewModel.refresh()
                        },
                            textStyle = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.offset(x = 15.dp),
                            placeholder = {
                                Text(text = "朝朝步数目标",style = MaterialTheme.typography.labelSmall)
                            })
                    }
                }
                item{
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "朝朝开始时间", style = MaterialTheme.typography.labelMedium)
                        TextButton(onClick = {
                            (context as AppCompatActivity).let {
                                val picker = MaterialTimePicker.Builder().build()
                                picker.addOnPositiveButtonClickListener {
                                    val builder = viewModel.goalsDayAndNight.dayGoals.toBuilder()
                                    builder.startHour = picker.hour
                                    builder.startMinute = picker.minute
                                    viewModel.goalsDayAndNight.dayGoals = builder.build()
                                    viewModel.refresh()
                                }
                                picker.show(it.supportFragmentManager, picker.toString())
                            }
                        }) {
                            Text(text = "${viewModel.goalsDayAndNight.dayGoals.startHour}:${viewModel.goalsDayAndNight.dayGoals.startMinute}")
                        }
                    }
                }
                item{
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "朝朝结束时间", style = MaterialTheme.typography.labelMedium)
                        TextButton(onClick = {
                            (context as AppCompatActivity).let {
                                val picker = MaterialTimePicker.Builder().build()
                                picker.addOnPositiveButtonClickListener {
                                    val builder = viewModel.goalsDayAndNight.dayGoals.toBuilder()
                                    builder.endHour = picker.hour
                                    builder.endMinute = picker.minute
                                    viewModel.goalsDayAndNight.dayGoals = builder.build()
                                    viewModel.refresh()
                                }
                                picker.show(it.supportFragmentManager, picker.toString())
                            }
                        }) {
                            Text(text = "${viewModel.goalsDayAndNight.dayGoals.endHour}:${viewModel.goalsDayAndNight.dayGoals.endMinute}")
                        }
                    }
                }
                item{
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "朝朝状态", style = MaterialTheme.typography.labelSmall)
                        Text(text = viewModel.goalsDayAndNight.dayGoals.status.toString(),style = MaterialTheme.typography.labelSmall)
                    }
                }
                item{
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "暮暮步数", style = MaterialTheme.typography.labelSmall)
                        Text(text = viewModel.goalsDayAndNight.nightGoals.steps.toString(),style = MaterialTheme.typography.labelSmall)
                    }
                }
                item{
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "暮暮步数目标", style = MaterialTheme.typography.labelSmall)
                        TextField(value = viewModel.goalsDayAndNight.nightGoals.stepGoals.toString(), onValueChange = {
                            val builder = viewModel.goalsDayAndNight.nightGoals.toBuilder()
                            builder.stepGoals = it.toIntOrNull() ?: 0
                            viewModel.goalsDayAndNight.nightGoals = builder.build()
                            viewModel.refresh()
                        },
                            textStyle = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.offset(x = 15.dp),
                            placeholder = {
                                Text(text = "暮暮步数目标",style = MaterialTheme.typography.labelSmall)
                            })
                    }
                }
                item{
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "暮暮开始时间", style = MaterialTheme.typography.labelMedium)
                        TextButton(onClick = {
                            (context as AppCompatActivity).let {
                                val picker = MaterialTimePicker.Builder().build()
                                picker.addOnPositiveButtonClickListener {
                                    val builder = viewModel.goalsDayAndNight.nightGoals.toBuilder()
                                    builder.startHour = picker.hour
                                    builder.startMinute = picker.minute
                                    viewModel.goalsDayAndNight.nightGoals = builder.build()
                                    viewModel.refresh()
                                }
                                picker.show(it.supportFragmentManager, picker.toString())
                            }
                        }) {
                            Text(text = "${viewModel.goalsDayAndNight.nightGoals.startHour}:${viewModel.goalsDayAndNight.nightGoals.startMinute}")
                        }
                    }
                }
                item{
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "暮暮结束时间", style = MaterialTheme.typography.labelMedium)
                        TextButton(onClick = {
                            (context as AppCompatActivity).let {
                                val picker = MaterialTimePicker.Builder().build()
                                picker.addOnPositiveButtonClickListener {
                                    val builder = viewModel.goalsDayAndNight.nightGoals.toBuilder()
                                    builder.endHour = picker.hour
                                    builder.endMinute = picker.minute
                                    viewModel.goalsDayAndNight.nightGoals = builder.build()
                                    viewModel.refresh()
                                }
                                picker.show(it.supportFragmentManager, picker.toString())
                            }
                        }) {
                            Text(text = "${viewModel.goalsDayAndNight.nightGoals.endHour}:${viewModel.goalsDayAndNight.nightGoals.endMinute}")
                        }
                    }
                }
                item{
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "暮暮状态", style = MaterialTheme.typography.labelSmall)
                        Text(text = viewModel.goalsDayAndNight.nightGoals.status.toString(),style = MaterialTheme.typography.labelSmall)
                    }
                }
                item{
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = {
                            navController.popBackStack()
                        }) {
                            Text(text = "取消")
                        }
                        ElevatedButton(
                            onClick = {
                                viewModel.setData()
                                navController.popBackStack()
                            }, colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.offset(15.dp)
                        ) {
                            Text(text = "确定")
                        }
                    }
                }

            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewGoalsDayAndNightDialog() {
    PreviewInit() {
        val navController = rememberNavController()
        GoalsDayAndNightPage(navController)
    }

}