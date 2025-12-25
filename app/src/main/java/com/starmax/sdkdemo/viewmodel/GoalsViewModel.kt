package com.starmax.sdkdemo.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starmax.bluetoothsdk.StarmaxBleClient
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class GoalsViewModel() : ViewModel() , KoinComponent {
    var steps by mutableStateOf(1000)

    var heat by mutableStateOf(10000)

    var distance by mutableStateOf(10)

    val context : Context by inject()

    fun getGoalData() {
        StarmaxBleClient.instance.getGoals().subscribe({
            steps = it.steps
            heat = it.heat
            distance = it.distance
        }, {

        }).let { }
    }

    fun setGoalData() {
        StarmaxBleClient.instance.setGoals(
            steps,
            heat,
            distance,
        ).subscribe({
            viewModelScope.launch {
                Toast.makeText(context,  "Set goal successfully", Toast.LENGTH_SHORT).show()
            }
        }, {

        }).let { }
    }
}