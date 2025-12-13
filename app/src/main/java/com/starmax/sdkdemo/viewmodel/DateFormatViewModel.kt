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

class DateFormatViewModel(

) : ViewModel() , KoinComponent {
    var dateFormat by mutableStateOf(0)

    val context : Context by inject()

    fun getData() {
        StarmaxBleClient.instance.getDateFormat().subscribe({
            it.dateFormat
        }, {

        }).let { }
    }

    fun setData() {
        StarmaxBleClient.instance.setDateFormat(
            dateFormat
        ).subscribe({
            viewModelScope.launch {
                Toast.makeText(context, "设置时间格式成功", Toast.LENGTH_SHORT).show()
            }
        }, {

        }).let { }
    }
}