package com.starmax.sdkdemo.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starmax.bluetoothsdk.Notify.CustomHealthGoal
import com.starmax.bluetoothsdk.Notify.CustomHealthGoals
import com.starmax.bluetoothsdk.StarmaxBleClient
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CustomHealthGoalsViewModel(

) : ViewModel() , KoinComponent {
    var customHealthGoals by mutableStateOf(CustomHealthGoals.newBuilder())
    val context : Context by inject()

    fun refresh(){
        customHealthGoals = customHealthGoals.build().toBuilder()
    }

    fun addGoal(){
        val index = if(customHealthGoals.goalsList.isNotEmpty()) customHealthGoals.goalsList.last().index + 1 else  1
        customHealthGoals.addGoals(CustomHealthGoal.newBuilder().setIndex(index))
        refresh()
    }

    fun removeGoal(index: Int){
        customHealthGoals.removeGoals(index)
        refresh()
    }

    fun getData() {
        StarmaxBleClient.instance.getCustomHealthGoals().subscribe({
            customHealthGoals = it.toBuilder()
        }, {

        }).let {}
    }

    fun setData() {
        StarmaxBleClient.instance.setCustomHealthGoals(customHealthGoals.build()).subscribe({
            viewModelScope.launch {
                Toast.makeText(context, "设置状态成功", Toast.LENGTH_SHORT).show()
            }
        }, {
            e ->
            e.printStackTrace()
        }).let {}
    }
}