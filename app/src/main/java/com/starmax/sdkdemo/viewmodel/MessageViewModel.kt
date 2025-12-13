package com.starmax.sdkdemo.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starmax.bluetoothsdk.StarmaxBleClient
import com.starmax.bluetoothsdk.data.MessageType
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MessageViewModel : ViewModel(), KoinComponent {
    var selectedMessageType by mutableStateOf(MessageType.Other)
    var title by mutableStateOf("")
    var content by mutableStateOf("")
    var messageId by mutableStateOf("")

    val context: Context by inject()

    init {
        StarmaxBleClient.instance.messageExtendStatusStream()
            .subscribe(
                {
                    val label = when (it.status) {
                        0 -> "失败"
                        1 -> "成功"
                        2 -> "稍后执行"
                        3 -> "确认"
                        4 -> "不执行"
                        else -> ""
                    }
                    viewModelScope.launch {
                        Toast.makeText(context, "${it.messageId}${label}", Toast.LENGTH_SHORT)
                            .show()
                    }
                },
                {

                }
            ).let {}
    }

    fun sendToBle() {
        StarmaxBleClient.instance.sendMessage(selectedMessageType, title, content).subscribe({
            viewModelScope.launch {
                Toast.makeText(context, "发送消息成功", Toast.LENGTH_SHORT).show()
            }
        }, {

        }).let {

        }
    }

    fun sendToExtendBle() {
        StarmaxBleClient.instance.sendMessageExtend(title, content, messageId.toIntOrNull() ?: 0)
            .subscribe({
                viewModelScope.launch {
                    Toast.makeText(context, "发送消息成功", Toast.LENGTH_SHORT).show()
                }
            }, {

            }).let {

        }
    }
}