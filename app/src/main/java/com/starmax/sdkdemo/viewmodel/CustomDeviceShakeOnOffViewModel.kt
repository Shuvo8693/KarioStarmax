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

class CustomDeviceShakeOnOffViewModel(

) : ViewModel(),KoinComponent {
    var shake by mutableStateOf(false)
    var hand by mutableStateOf(false)

    val context : Context by inject()

    fun getData() {
        StarmaxBleClient.instance.getCustomDeviceShakeOnOff().subscribe({
            shake = it.shakeOnOff
            hand = it.handOnOff
        }, {

        }).let { }
    }

    fun setData() {
        StarmaxBleClient.instance.setCustomDeviceShakeOnOff(
            shake,
            hand
        ).subscribe({
            viewModelScope.launch {
                Toast.makeText(context, "设置开关成功", Toast.LENGTH_SHORT).show()
            }
        }, {

        }).let { }
    }
}