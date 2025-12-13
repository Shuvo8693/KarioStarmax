package com.starmax.sdkdemo.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starmax.bluetoothsdk.Notify.CustomHealthGoalTask
import com.starmax.bluetoothsdk.Notify.CustomHealthGoalTasks
import com.starmax.bluetoothsdk.Notify.HealthInterval
import com.starmax.bluetoothsdk.StarmaxBleClient
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class Gts10HealthIntervalViewModel(

) : ViewModel() , KoinComponent {
    var intervals by mutableStateOf(listOf(HealthInterval.newBuilder()))
    val context : Context by inject()

    fun refresh(){
        intervals = intervals.map { it.build().toBuilder() }
    }

    fun addInterval(){
        intervals = intervals + HealthInterval.newBuilder()
        refresh()
    }

    fun removeInterval(index:Int){
        intervals = intervals.drop(index)
        refresh()
    }

    fun getData() {
        StarmaxBleClient.instance.getX04HealthIntervals().subscribe({
            intervals = it.dataList.map { it.toBuilder() }
        }, {

        }).let {}
    }

    fun setData() {
        StarmaxBleClient.instance.setX04HealthIntervals(intervals.map { it.build() }).subscribe({
            viewModelScope.launch {
                Toast.makeText(context, "设置状态成功", Toast.LENGTH_SHORT).show()
            }
        }, {

        }).let {}
    }
}