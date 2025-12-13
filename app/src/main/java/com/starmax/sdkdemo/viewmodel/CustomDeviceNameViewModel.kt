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

class CustomDeviceNameViewModel(

) : ViewModel(),KoinComponent {
    var deviceName by mutableStateOf("")

    val context : Context by inject()

    fun getData() {
        StarmaxBleClient.instance.getCustomDeviceName().subscribe({
            deviceName = it.deviceName
        }, {

        }).let { }
    }

    fun setData() {
        StarmaxBleClient.instance.setCustomDeviceName(
            deviceName,
        ).subscribe({
            viewModelScope.launch {
                Toast.makeText(context, "设置名称成功", Toast.LENGTH_SHORT).show()
            }
        }, {

        }).let { }
    }
}