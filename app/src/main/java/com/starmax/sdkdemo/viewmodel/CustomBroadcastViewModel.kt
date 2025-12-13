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

class CustomBroadcastViewModel(

) : ViewModel() , KoinComponent {
    var interval by mutableStateOf(1000)
    var longTouchTime by mutableStateOf(10000)

    val context : Context by inject()

    fun getData() {
        StarmaxBleClient.instance.getCustomBroadcast().subscribe({
            interval = it.interval
            longTouchTime = it.longTouchTime
        }, {

        }).let { }
    }

    fun setData() {
        StarmaxBleClient.instance.setCustomBroadcast(
            interval,
            longTouchTime,
        ).subscribe({
            viewModelScope.launch {
                Toast.makeText(context, "设置广播间隔成功", Toast.LENGTH_SHORT).show()
            }
        }, {

        }).let { }
    }
}