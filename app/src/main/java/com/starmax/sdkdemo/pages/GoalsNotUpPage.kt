package com.starmax.sdkdemo.pages

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
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
import com.starmax.sdkdemo.viewmodel.GoalsNotUpViewModel

@Composable
fun GoalsNotUpPage(navController: NavController) {
    val viewModel: GoalsNotUpViewModel by lazyKoinViewModel()

    LaunchedEffect(Unit) {
        viewModel.getData()
    }

    GoalsNotUpDialogView(navController = navController,viewModel = viewModel)
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsNotUpDialogView(navController: NavController, viewModel: GoalsNotUpViewModel) {

    val context = LocalContext.current

    AppTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(text = "步数未达标")
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
                        Text(text = "步数未达标时间", style = MaterialTheme.typography.labelMedium)
                        TextButton(onClick = {
                            (context as AppCompatActivity).let {
                                val picker = MaterialTimePicker.Builder().build()
                                picker.addOnPositiveButtonClickListener {
                                    viewModel.goalsNotUp.hour = picker.hour
                                    viewModel.goalsNotUp.minute = picker.minute
                                    viewModel.refresh()
                                }
                                picker.show(it.supportFragmentManager, picker.toString())
                            }
                        }) {
                            Text(text = "${viewModel.goalsNotUp.hour}:${viewModel.goalsNotUp.minute}")
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
fun PreviewGoalsNotUpDialog() {
    PreviewInit() {
        val navController = rememberNavController()
        GoalsNotUpPage(navController)
    }

}