package com.starmax.sdkdemo.pages

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
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
import com.google.android.material.datepicker.MaterialDatePicker
import com.starmax.sdkdemo.NavPage
import com.starmax.sdkdemo.PreviewInit
import com.starmax.sdkdemo.ui.theme.AppTheme
import com.starmax.sdkdemo.viewmodel.CustomHealthGoalsViewModel
import java.util.Calendar

@Composable
fun CustomHealthGoalsPage(navController: NavController) {
    val viewModel: CustomHealthGoalsViewModel by lazyKoinViewModel()

    LaunchedEffect(Unit) {
        viewModel.getData()
    }

    CustomHealthGoalsPageView(navController = navController,viewModel = viewModel)
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomHealthGoalsPageView(navController: NavController, viewModel: CustomHealthGoalsViewModel) {

    val context = LocalContext.current

    AppTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(text = "健康处方")
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
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = "处方任务运动强度判定时间", style = MaterialTheme.typography.labelSmall)
                        TextField(value = viewModel.customHealthGoals.intensityDeterminationTime.toString(), onValueChange = {
                            viewModel.customHealthGoals.intensityDeterminationTime = it.toIntOrNull() ?: 0
                            viewModel.refresh()
                        },
                            textStyle = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.offset(x = 15.dp),
                            placeholder = {
                                Text(text = "处方任务运动强度判定时间",style = MaterialTheme.typography.labelSmall)
                            })
                    }
                }
                item{
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = "处方列表", style = MaterialTheme.typography.labelSmall)
                    }
                }
                items(count = viewModel.customHealthGoals.goalsCount){
                    index ->
                    val goal = viewModel.customHealthGoals.goalsList[index]

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp)
                    ) {
                        Text(text = "处方索引:${goal.index}", style = MaterialTheme.typography.labelSmall)
                        Text(text = "处方开始日期:${goal.startYear}-${goal.startMonth}-${goal.startDay}", modifier = Modifier.clickable {
                            (context as AppCompatActivity).let {
                                val picker = MaterialDatePicker.Builder.datePicker().build()
                                picker.addOnPositiveButtonClickListener {
                                    val calendar = Calendar.getInstance()
                                    calendar.timeInMillis = it
                                    viewModel.customHealthGoals.setGoals(index, goal.toBuilder().apply {
                                        startYear = calendar.get(Calendar.YEAR)
                                        startMonth = calendar.get(Calendar.MONTH) + 1
                                        startDay = calendar.get(Calendar.DATE)
                                    })
                                    viewModel.refresh()
                                }
                                picker.show(it.supportFragmentManager, picker.toString())
                            }
                        }, style = MaterialTheme.typography.labelSmall)
                        Text(text = "处方结束日期:${goal.endYear}-${goal.endMonth}-${goal.endDay}", modifier = Modifier.clickable {
                            (context as AppCompatActivity).let {
                                val picker = MaterialDatePicker.Builder.datePicker().build()
                                picker.addOnPositiveButtonClickListener {
                                    val calendar = Calendar.getInstance()
                                    calendar.timeInMillis = it
                                    viewModel.customHealthGoals.setGoals(index, goal.toBuilder().apply {
                                        endYear = calendar.get(Calendar.YEAR)
                                        endMonth = calendar.get(Calendar.MONTH) + 1
                                        endDay = calendar.get(Calendar.DATE)
                                    })
                                    viewModel.refresh()
                                }
                                picker.show(it.supportFragmentManager, picker.toString())
                            }
                        }, style = MaterialTheme.typography.labelSmall)
                        Text(text = "处方名称:", style = MaterialTheme.typography.labelSmall)
                        TextField(value = goal.name, onValueChange = {
                            viewModel.customHealthGoals.setGoals(index,goal.toBuilder().setName(it).build())
                            viewModel.refresh()
                        },
                            textStyle = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.offset(x = 15.dp),
                            placeholder = {
                                Text(text = "处方名称",style = MaterialTheme.typography.labelSmall)
                            })
                        TextButton(onClick = {
                            navController.navigate("${NavPage.CustomHealthGoalTasksPage.name}/${goal.index}")
                        }) {
                            Text(text = "设置任务")
                        }
                        TextButton(onClick = {
                            viewModel.removeGoal(index)
                        }) {
                            Text(text = "删除任务")
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
                            viewModel.addGoal()
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
fun PreviewCustomHealthGoalsPageView() {
    PreviewInit() {
        val navController = rememberNavController()
        CustomHealthGoalsPage(navController)
    }

}