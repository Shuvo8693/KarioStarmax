package com.starmax.sdkdemo.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starmax.bluetoothsdk.StarmaxBleClient
import com.starmax.bluetoothsdk.StarmaxMapResponse
import com.starmax.bluetoothsdk.data.NotifyType
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

class Gts10PairViewModel(

) : ViewModel(), KoinComponent {
    val context: Context by inject()
    var uuid: UUID by mutableStateOf(UUID.randomUUID())
    var deviceIsChecked by mutableStateOf(false)

    fun startPair() {
        StarmaxBleClient.instance.gts10MutualAuth().subscribe({
            deviceIsChecked = it.pairStatus == 1
            viewModelScope.launch {
                Toast.makeText(
                    context,
                    "发起配对" + if (it.pairStatus == 1) "成功" else "失败",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }, {

        }).let { }
    }

    fun pairReply(isSuccess: Boolean) {
        StarmaxBleClient.instance.gts10MutualAuthReply(isSuccess).subscribe({
            viewModelScope.launch {
                Toast.makeText(
                    context,
                    "配对" + if (isSuccess) "确认" else "取消",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }, {

        }).let { }
    }

    fun generateUUID() {
        uuid = UUID.randomUUID()
    }

    fun getUUID() {
        StarmaxBleClient.instance.gts10MutualAuthGetUUID().subscribe({
            uuid = UUID.fromString(it.uuid)
            viewModelScope.launch {
                Toast.makeText(context, "UUID获取成功", Toast.LENGTH_SHORT).show()
            }
        }, {

        }).let { }
    }

    fun sendUUID() {
        StarmaxBleClient.instance.gts10MutualAuthSendUUID(uuid).subscribe({
            val statusLabel = when (it.pairStatus) {
                0 -> "接收失败"
                1 -> "校验不通过"
                2 -> "校验通过"
                else -> ""
            }
            viewModelScope.launch {
                Toast.makeText(context, "UUID" + statusLabel, Toast.LENGTH_SHORT).show()
            }
        }, {

        }).let { }
    }

    fun unpair(needRestore: Boolean) {
        StarmaxBleClient.instance.gts10MutualAuthUnpair(needRestore).subscribe({
            val statusLabel = when (it.pairStatus) {
                0 -> "解绑完成"
                1 -> "解绑取消"
                else -> ""
            }
            viewModelScope.launch {
                Toast.makeText(context, statusLabel, Toast.LENGTH_SHORT).show()
            }
        }, {

        }).let { }
    }

}