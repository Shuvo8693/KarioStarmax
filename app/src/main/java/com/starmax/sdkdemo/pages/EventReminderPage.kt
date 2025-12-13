package com.starmax.sdkdemo.pages

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
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
import com.google.android.material.timepicker.MaterialTimePicker
import com.starmax.sdkdemo.PreviewInit
import com.starmax.sdkdemo.ui.theme.AppTheme
import com.starmax.sdkdemo.viewmodel.BleViewModel
import com.starmax.sdkdemo.viewmodel.EventReminderViewModel
import java.util.Calendar

@Composable
fun EventReminderPage(navController: NavController) {
    val viewModel: EventReminderViewModel by lazyKoinViewModel()

    LaunchedEffect(Unit) {
        viewModel.getData()
    }

    EventReminderPageView(navController = navController,viewModel = viewModel)
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventReminderPageView(navController: NavController, viewModel: EventReminderViewModel) {

    val context = LocalContext.current
    val bleViewModel: BleViewModel by lazyKoinViewModel()

    val selectMp3Launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
            val uri = it.data?.data
            if(uri != null){
                bleViewModel.sendMp3Local(context,uri,viewModel.selectedIndex)
            }
            navController.popBackStack()
        }

    AppTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(text = "事件提醒")
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
                        Text(text = "事件提醒列表", style = MaterialTheme.typography.labelSmall)
                    }
                }
                items(count = viewModel.eventReminders.size){
                        index ->
                    val eventReminder = viewModel.eventReminders[index]

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp)
                    ) {
                        Text(text = "索引:${index}", style = MaterialTheme.typography.labelSmall)
                        Text(text = "日期:${eventReminder.year}-${eventReminder.month}-${eventReminder.day}", modifier = Modifier.clickable {
                            (context as AppCompatActivity).let {
                                val picker = MaterialDatePicker.Builder.datePicker().build()
                                picker.addOnPositiveButtonClickListener {
                                    val calendar = Calendar.getInstance()
                                    calendar.timeInMillis = it
                                    eventReminder.year = calendar.get(Calendar.YEAR)
                                    eventReminder.month = calendar.get(Calendar.MONTH)
                                    eventReminder.day = calendar.get(Calendar.DATE)
                                    viewModel.refreshEventReminder(index,eventReminder)
                                }
                                picker.show(it.supportFragmentManager, picker.toString())
                            }
                        }, style = MaterialTheme.typography.labelSmall)
                        Text(text = "时间:${eventReminder.hour}:${eventReminder.minute}", modifier = Modifier.clickable {
                            (context as AppCompatActivity).let {
                                val picker = MaterialTimePicker.Builder().build()
                                picker.addOnPositiveButtonClickListener {
                                    eventReminder.hour = picker.hour
                                    eventReminder.minute = picker.minute
                                    viewModel.refreshEventReminder(index,eventReminder)
                                }
                                picker.show(it.supportFragmentManager, picker.toString())
                            }
                        }, style = MaterialTheme.typography.labelSmall)
                        Text(text = "内容:", style = MaterialTheme.typography.labelSmall)
                        TextField(value = eventReminder.content, onValueChange = {
                            eventReminder.content = it
                            viewModel.refreshEventReminder(index,eventReminder)
                        },
                            textStyle = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.offset(x = 15.dp),
                            placeholder = {
                                Text(text = "内容",style = MaterialTheme.typography.labelSmall)
                            })
                        TextButton(onClick = {
                            viewModel.removeEventReminder(index)
                        }) {
                            Text(text = "删除事件")
                        }
                        TextButton(onClick = {
                            viewModel.selectedIndex = eventReminder.otherInfo
                        }) {
                            Text(text = "选择事件")
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
                            viewModel.addEventReminder()
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
                            Text(text = "下发事件")
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
                        ElevatedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                                intent.type = "*/*"
                                intent.addCategory(Intent.CATEGORY_OPENABLE)
                                selectMp3Launcher.launch(intent)
                            }, colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.offset(15.dp)
                        ) {
                            Text(text = "当前选择:${viewModel.selectedIndex},下发MP3")
                        }

                    }

                }
                item {
                    Text(text = bleViewModel.bleMessage.value)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewEventReminderPageView() {
    PreviewInit() {
        val navController = rememberNavController()
        EventReminderPage(navController)
    }

}