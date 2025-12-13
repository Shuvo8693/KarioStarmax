package com.starmax.sdkdemo.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starmax.bluetoothsdk.Notify.GoalsDayAndNight
import com.starmax.bluetoothsdk.StarmaxBleClient
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class QuickBatteryModeViewModel(

) : ViewModel() , KoinComponent {
    var light by mutableStateOf(100)
    var scale by mutableStateOf(0)
    var v by mutableStateOf(0)
    val context : Context by inject()

    fun setQuickBatteryMode() {
        StarmaxBleClient.instance.setQuickBatteryMode().subscribe({
            viewModelScope.launch {
                Toast.makeText(context, "设置进入快速耗电模式成功", Toast.LENGTH_SHORT).show()
            }
        }, {

        }).let {}
    }

    fun setBatteryLight(){
        StarmaxBleClient.instance.setBatteryLight(light).subscribe({
            viewModelScope.launch {
                Toast.makeText(context, "设置设备常亮间隔成功", Toast.LENGTH_SHORT).show()
            }
        }, {

        }).let {}
    }

    fun getBatteryV() {
        StarmaxBleClient.instance.getBatteryV().subscribe({
            scale = it.batteryScale
            v = it.batteryVoltage
            viewModelScope.launch {
                Toast.makeText(context, "获取设备电量电压成功", Toast.LENGTH_SHORT).show()
            }
        }, {

        }).let {}
    }

    fun offQuickBatteryMode(mode: Int) {
        StarmaxBleClient.instance.offQuickBatteryMode(mode).subscribe({
            viewModelScope.launch {
                Toast.makeText(context, "退出模式成功", Toast.LENGTH_SHORT).show()
            }
        }, {

        }).let {}
    }
}