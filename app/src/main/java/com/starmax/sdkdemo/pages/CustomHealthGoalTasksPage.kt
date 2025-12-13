package com.starmax.sdkdemo.pages

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
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
import com.starmax.sdkdemo.viewmodel.CustomHealthGoalTasksViewModel

@Composable
fun CustomHealthGoalTasksPage(index: Int, navController: NavController) {
    val viewModel: CustomHealthGoalTasksViewModel by lazyKoinViewModel()

    LaunchedEffect(Unit) {
        viewModel.getData(index)
    }

    CustomHealthGoalTasksPageView(navController = navController,viewModel = viewModel)
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomHealthGoalTasksPageView(navController: NavController, viewModel: CustomHealthGoalTasksViewModel) {

    val context = LocalContext.current

    AppTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(text = "健康处方任务${viewModel.customHealthGoalTasks.goalsIndex}")
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
                        Text(text = "任务列表", style = MaterialTheme.typography.labelSmall)
                    }
                }
                items(count = viewModel.customHealthGoalTasks.tasksCount){
                        index ->
                    val task = viewModel.customHealthGoalTasks.tasksList[index]

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp)
                    ) {
                        Text(text = "任务索引:${task.index}", style = MaterialTheme.typography.labelSmall)
                        Text(text = "任务开始时间:${task.startHour}:${task.startMinute}", modifier = Modifier.clickable {
                            (context as AppCompatActivity).let {
                                val picker = MaterialTimePicker.Builder().build()
                                picker.addOnPositiveButtonClickListener {
                                    viewModel.customHealthGoalTasks.setTasks(index, task.toBuilder().apply {
                                        startHour = picker.hour
                                        startMinute = picker.minute
                                    }.build())
                                    viewModel.refresh()
                                }
                                picker.show(it.supportFragmentManager, picker.toString())
                            }
                        }, style = MaterialTheme.typography.labelSmall)
                        Text(text = "任务结束时间:${task.endHour}:${task.endMinute}", modifier = Modifier.clickable {
                            (context as AppCompatActivity).let {
                                val picker = MaterialTimePicker.Builder().build()
                                picker.addOnPositiveButtonClickListener {
                                    viewModel.customHealthGoalTasks.setTasks(index, task.toBuilder().apply {
                                        endHour = picker.hour
                                        endMinute = picker.minute
                                    }.build())
                                    viewModel.refresh()
                                }
                                picker.show(it.supportFragmentManager, picker.toString())
                            }
                        }, style = MaterialTheme.typography.labelSmall)
                        Text(text = "处方任务目标心率区间:${task.goalHeartRateMin}-${task.goalHeartRateMax}", style = MaterialTheme.typography.labelSmall)
                        RangeSlider(value = task.goalHeartRateMin.toFloat() .. task.goalHeartRateMax.toFloat(), valueRange = 0f..255f, onValueChange = {
                            viewModel.customHealthGoalTasks.setTasks(index,task.toBuilder().apply {
                                goalHeartRateMin = it.start.toInt()
                                goalHeartRateMax = it.endInclusive.toInt()
                            }.build())
                            viewModel.refresh()
                        })
                        Text(text = "处方任务目标步频区间:${task.goalStrideFreqMin}-${task.goalStrideFreqMax}", style = MaterialTheme.typography.labelSmall)
                        RangeSlider(value = task.goalStrideFreqMin.toFloat() .. task.goalStrideFreqMax.toFloat(), valueRange = 0f..300f, onValueChange = {
                            viewModel.customHealthGoalTasks.setTasks(index,task.toBuilder().apply {
                                goalStrideFreqMin = it.start.toInt()
                                goalStrideFreqMax = it.endInclusive.toInt()
                            }.build())
                            viewModel.refresh()
                        })

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(15.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(text = "目标时间", style = MaterialTheme.typography.labelSmall)
                            TextField(value = task.goalMinutes.toString(), onValueChange = {
                                viewModel.customHealthGoalTasks.setTasks(index,task.toBuilder().apply {
                                    goalMinutes = it.toIntOrNull() ?: 0
                                }.build())
                                viewModel.refresh()
                            },
                                textStyle = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.offset(x = 15.dp),
                                placeholder = {
                                    Text(text = "总步数目标",style = MaterialTheme.typography.labelSmall)
                                })
                        }
                        IconButton(onClick = {
                            viewModel.removeTask(index)
                        }) {
                            Icon(Icons.Outlined.Remove, contentDescription = "")
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
                        IconButton(onClick = {
                            viewModel.addTask()
                        }) {
                            Icon(Icons.Outlined.Add, contentDescription = "")
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
fun PreviewCustomHealthGoalTasksPageView() {
    PreviewInit() {
        val navController = rememberNavController()
        CustomHealthGoalTasksPage(0,navController)
    }

}