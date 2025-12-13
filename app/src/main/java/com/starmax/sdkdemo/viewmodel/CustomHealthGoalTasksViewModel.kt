package com.starmax.sdkdemo.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starmax.bluetoothsdk.Notify.CustomHealthGoal
import com.starmax.bluetoothsdk.Notify.CustomHealthGoalTask
import com.starmax.bluetoothsdk.Notify.CustomHealthGoalTasks
import com.starmax.bluetoothsdk.Notify.CustomHealthGoals
import com.starmax.bluetoothsdk.StarmaxBleClient
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CustomHealthGoalTasksViewModel(

) : ViewModel() , KoinComponent {
    var customHealthGoalTasks by mutableStateOf(CustomHealthGoalTasks.newBuilder())
    val context : Context by inject()

    fun refresh(){
        customHealthGoalTasks = customHealthGoalTasks.build().toBuilder()
    }

    fun addTask(){
        val index = customHealthGoalTasks.tasksCount
        customHealthGoalTasks.addTasks(CustomHealthGoalTask.newBuilder().setIndex(index))
        refresh()
    }

    fun removeTask(index:Int){
        customHealthGoalTasks.removeTasks(index)
        refresh()
    }

    fun getData(index: Int) {
        StarmaxBleClient.instance.getCustomHealthGoalTasks(index).subscribe({
            customHealthGoalTasks = it.toBuilder()
        }, {

        }).let {}
    }

    fun setData() {
        StarmaxBleClient.instance.setCustomHealthGoalTasks(customHealthGoalTasks.build()).subscribe({
            viewModelScope.launch {
                Toast.makeText(context, "设置状态成功", Toast.LENGTH_SHORT).show()
            }
        }, {

        }).let {}
    }
}