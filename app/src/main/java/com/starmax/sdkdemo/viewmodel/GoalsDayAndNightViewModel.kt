package com.starmax.sdkdemo.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starmax.bluetoothsdk.Notify.GoalsDayAndNight
import com.starmax.bluetoothsdk.Notify.GoalsNotUp
import com.starmax.bluetoothsdk.StarmaxBleClient
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class GoalsDayAndNightViewModel(

) : ViewModel() , KoinComponent {
    var goalsDayAndNight by mutableStateOf(GoalsDayAndNight.newBuilder())
    val context : Context by inject()

    fun refresh(){
        goalsDayAndNight = goalsDayAndNight.build().toBuilder()
    }

    fun getData() {
        StarmaxBleClient.instance.getGoalsDayAndNight().subscribe({
            goalsDayAndNight = it.toBuilder()
        }, {

        }).let {}
    }

    fun setData() {
        StarmaxBleClient.instance.setGoalsDayAndNight(goalsDayAndNight.build()).subscribe({
            viewModelScope.launch {
                Toast.makeText(context, "设置状态成功", Toast.LENGTH_SHORT).show()
            }
        }, {

        }).let {}
    }
}