package com.starmax.sdkdemo.viewmodel   

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starmax.bluetoothsdk.Notify.GoalsNotUp
import com.starmax.bluetoothsdk.StarmaxBleClient
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class GoalsNotUpViewModel(

) : ViewModel() , KoinComponent {
    var goalsNotUp by mutableStateOf(GoalsNotUp.newBuilder())
    val context : Context by inject()

    fun refresh(){
        goalsNotUp = goalsNotUp.build().toBuilder()
    }

    fun getData() {
        StarmaxBleClient.instance.getGoalsNotUp().subscribe({
            goalsNotUp = it.toBuilder()
        }, {

        }).let {}
    }

    fun setData() {
        StarmaxBleClient.instance.setGoalsNotUp(goalsNotUp.build()).subscribe({
            viewModelScope.launch {
                Toast.makeText(context, "设置未达标成功", Toast.LENGTH_SHORT).show()
            }
        }, {

        }).let {}
    }
}