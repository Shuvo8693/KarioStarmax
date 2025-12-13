package com.starmax.sdkdemo.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.material3.Switch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleIndicateCallback
import com.clj.fastble.callback.BleMtuChangedCallback
import com.clj.fastble.callback.BleNotifyCallback
import com.clj.fastble.callback.BleRssiCallback
import com.clj.fastble.callback.BleWriteCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.sifli.ezip.FileValidator
import com.sifli.ezip.sifliEzipUtil

import com.starmax.bluetoothsdk.BleFileSender
import com.starmax.bluetoothsdk.BleFileSender.allFileData
import com.starmax.bluetoothsdk.BleFileSender.dataSize
import com.starmax.bluetoothsdk.BleFileSenderListener
import com.starmax.bluetoothsdk.BmpUtils
import com.starmax.bluetoothsdk.FileUtils
import com.starmax.bluetoothsdk.Notify
import com.starmax.bluetoothsdk.Notify.GetFileV2
import com.starmax.bluetoothsdk.Notify.Reply
import com.starmax.bluetoothsdk.StarmaxBleClient
import com.starmax.bluetoothsdk.StarmaxSend
import com.starmax.bluetoothsdk.StarmaxSendRequest
import com.starmax.bluetoothsdk.Utils
import com.starmax.bluetoothsdk.data.Clock
import com.starmax.bluetoothsdk.data.EventReminder
import com.starmax.bluetoothsdk.data.HistoryType
import com.starmax.bluetoothsdk.data.MessageType
import com.starmax.bluetoothsdk.data.NotifyType
import com.starmax.bluetoothsdk.data.SummerWorldClock
import com.starmax.bluetoothsdk.data.WeatherDay
import com.starmax.bluetoothsdk.factory.SportHistoryFactory
import com.starmax.bluetoothsdk.factory.SummerWorldClockFactory
import com.starmax.bluetoothsdk.factory.WeatherSevenFactory
import com.starmax.net.repository.CrackRepository
import com.starmax.net.repository.UiRepository
import com.starmax.sdkdemo.service.RxBleService
import com.starmax.sdkdemo.utils.NetFileUtils
import com.starmax.sdkdemo.utils.SlmM1Crack
import com.starmax.sdkdemo.utils.TestRepository
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Integer.min
import java.lang.ref.SoftReference
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.HashMap
import java.util.UUID
import java.util.regex.Pattern
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream
import kotlin.math.roundToInt


enum class BleState {
    DISCONNECTED,
    CONNECTTING,
    CONNECTED
}

class BleViewModel() : ViewModel(), KoinComponent {
    var savePath = ""
    private var localBasePath = ""

    var tryOpenNotify = mutableStateOf(true)
        private set

    /**
     * 写
     */
    val WriteServiceUUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9d")
    val WriteCharacteristicUUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9d")

    /**
     * 读
     */
    val NotifyServiceUUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9d")
    val NotifyCharacteristicUUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9d")

    var bleDevice: SoftReference<BleDevice>? by mutableStateOf(null)
        private set

    var bleGatt: SoftReference<BluetoothGatt>? by mutableStateOf(null)
        private set

    var bleModel = ""
    var bleVersion = ""
    var bleUiVersion = ""
    var uiSupportDifferentialUpgrade = false
    var lcdWidth = 80
    var lcdHeight = 160

    var disconnectSubject = PublishSubject.create<Int>()

    var originData = mutableStateOf("")
        private set

    var bleState by mutableStateOf(BleState.DISCONNECTED)
        private set

    var bleStateLiveData = MutableLiveData(BleState.DISCONNECTED)

    var bleMessage = mutableStateOf("")
        private set

    val bleStateLabel: String
        get() {
            val data = when (bleState) {
                BleState.DISCONNECTED -> "Disconnected"
                BleState.CONNECTTING -> "Connecting"
                BleState.CONNECTED -> "Connected"
            }
            return data
        }

    var bleResponse = mutableStateOf("")
        private set
    var bleResponseLabel = mutableStateOf("")
        private set

    val context: Context by inject()

    private val sendDisposable = CompositeDisposable()
    private val messageDisposable = CompositeDisposable()

    var imageUri: Uri? = null
    var binUri: Uri? = null

    var msgType = 0
    var msgContent = 0

    var packageId = 0

    private var bleService: RxBleService? = null

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder: RxBleService.RCBinder = service as RxBleService.RCBinder
            bleService = binder.service
            Log.e("BleViewModel", "-- RxBleService-- 已连接")
        }

        override fun onServiceDisconnected(name: ComponentName) {
            bleService = null
            Log.e("BleViewModel", "-- RxBleService-- 已断连")
        }
    }

    var bleGattCallback: BleGattCallback = object : BleGattCallback() {
        override fun onStartConnect() {
            bleState = BleState.CONNECTTING
            bleStateLiveData.postValue(bleState)
            bleMessage.value = "蓝牙正在连接"
        }

        override fun onConnectFail(bleDevice: BleDevice?, exception: BleException?) {
            bleState = BleState.DISCONNECTED
            bleStateLiveData.postValue(bleState)
            bleMessage.value = "蓝牙连接失败" + exception.toString()
        }

        override fun onConnectSuccess(
            newBleDevice: BleDevice?,
            gatt: BluetoothGatt?,
            status: Int
        ) {
            bleDevice = SoftReference(newBleDevice)
            bleState = BleState.CONNECTED
            bleGatt = SoftReference(gatt)
            bleStateLiveData.postValue(bleState)
            bleMessage.value = "蓝牙连接成功"

            Log.d("BleViewModel", gatt?.getService(NotifyServiceUUID).toString())

            if (gatt?.getService(NotifyServiceUUID) == null) {
                Handler(Looper.getMainLooper()).postDelayed({
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        gatt?.discoverServices()
                    }

                }, 1000)
                return
            }

            Handler(Looper.getMainLooper()).postDelayed({
                if (tryOpenNotify.value) {
                    openNotify(bleDevice!!.get())
                } else {
                    openIndicate(bleDevice!!.get())
                }
            }, 3000)

        }

        override fun onDisConnected(
            isActiveDisConnected: Boolean,
            device: BleDevice?,
            gatt: BluetoothGatt?,
            status: Int
        ) {
            bleState = BleState.DISCONNECTED
            bleGatt = SoftReference(gatt)
            bleStateLiveData.postValue(bleState)
            bleMessage.value = "蓝牙连接断开"
            disconnectSubject.onNext(1)

            //isActiveDisConnected 为false时，尝试重连，我这里是延迟2秒
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {

        }

    }

    init {
        initPath()
        //蓝牙打开、关闭广播监听
        context.registerReceiver(BluetoothListenerReceiver(this), makeFilter())
        StarmaxBleClient.instance.setWrite { byteArray -> sendMsg(byteArray) }
    }


    fun setNotify(boolean: Boolean) {
        tryOpenNotify.value = boolean
    }

    fun getDeviceName(): String {
        val name = bleDevice?.get()?.name;
        if (name != null) {
            return name;
        }

        return "";
    }

    fun initPath() {
        if (context !is Application) {
            return
        }

        var basepath = context.getExternalFilesDir(null)?.path
        if (basepath == null) {
            basepath = Environment.getExternalStorageDirectory().absolutePath
        }
        localBasePath = basepath!!
        savePath = basepath + "/SDKDemo/Device_update/"
        println("下载地址：" + savePath)
    }

    fun connect(newBleDevice: BleDevice?) {
        bleDevice = SoftReference(newBleDevice)
        if (bleDevice != null) {
            BleManager.getInstance().connect(bleDevice!!.get(), bleGattCallback)
        }
    }

    fun getRssi() {
        BleManager.getInstance().readRssi(bleDevice!!.get(), object : BleRssiCallback() {
            override fun onRssiSuccess(rssi: Int) {
                bleResponseLabel.value = "信号强度：" + rssi
            }

            override fun onRssiFailure(exception: BleException?) {
                TODO("Not yet implemented")
            }
        })
    }

    fun openIndicate(newBleDevice: BleDevice?) {
        BleManager.getInstance().indicate(
            newBleDevice,
            NotifyServiceUUID.toString(),
            NotifyCharacteristicUUID.toString(),
            object : BleIndicateCallback() {
                override fun onIndicateSuccess() {
                    bleMessage.value = "打开indicate成功"
                    handleOpenSuccess()
                }

                override fun onIndicateFailure(exception: BleException?) {
                    bleMessage.value = "打开indicate失败：$exception"
                }

                @SuppressLint("MissingPermission", "NewApi")
                override fun onCharacteristicChanged(data: ByteArray) {
                    StarmaxBleClient.instance.notify(data)
                }
            })
    }

    fun openNotify(newBleDevice: BleDevice?) {
        BleManager.getInstance().notify(
            newBleDevice,
            NotifyServiceUUID.toString(),
            NotifyCharacteristicUUID.toString(),
            object : BleNotifyCallback() {
                @RequiresApi(Build.VERSION_CODES.O)
                @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
                override fun onNotifySuccess() {
                    bleMessage.value = "Notify opened successfully"
                    bleGatt?.get()?.setPreferredPhy(
                        BluetoothDevice.PHY_LE_2M,
                        BluetoothDevice.PHY_LE_2M,
                        BluetoothDevice.PHY_OPTION_NO_PREFERRED
                    )

                    handleOpenSuccess()

                }

                override fun onNotifyFailure(exception: BleException) {
                    bleMessage.value = "Failed to open notify：$exception"
                }

                @SuppressLint("MissingPermission", "NewApi")
                override fun onCharacteristicChanged(data: ByteArray) {
                    //Utils.p(data)
                    println(StarmaxBleClient.instance.notify(data))
                }
            })
    }

    private fun handleOpenSuccess() {
        TestRepository.testLocal(localBasePath, Date().toString() + "\n", "log.txt")
        changeMtu {
            //getVersion(false)
            getVersion(false)
            StarmaxBleClient.instance.notifyStream().takeUntil(disconnectSubject).subscribe(
                {
                    if (it.data is Notify.StepHistory) {
                        originData.value = it.byteArray.asList().drop(7).chunked(12).joinToString("\n") { byteArray ->
                            byteArray.map { byte -> String.format("%02X", byte) }.toString()
                            }
                        print(originData.value)
                    } else if (it.data is Notify.OriginSleepHistory) {
                        originData.value = it.byteArray.asList().drop(7).chunked(120)
                            .mapIndexed { hourIndex, hourByteArray ->
                                "${hourIndex}hour：\n" + (hourByteArray.chunked(10)
                                    .joinToString("\n") { lineByteArray ->
                                        lineByteArray.chunked(2)
                                            .joinToString(",") { minuteByteArray ->
                                                minuteByteArray.reversed()
                                                    .joinToString("") { minuteByte ->
                                                        String.format(
                                                            "%02X",
                                                            minuteByte
                                                        )
                                                    }
                                            }
                                    })
                            }.joinToString("\n")
                    } else {
                        originData.value = it.byteArray.map { String.format("%02X", it) }.toString()
                    }
                    //bleResponse.value = it.data.toString()

                    if (it.data is Notify.Reply) {//TODO Sometimes the saiwei.txt file isn't saved; the goal is to save it every time.
                        if ((it.data as Notify.Reply).type == NotifyType.Log.name) {
                            Utils.p(it.byteArray)
                            TestRepository.testLocal(
                                localBasePath,
                                it.byteArray.toString(Charsets.US_ASCII)
                                    .replace("TAG=", "\nTAG="),
                                "saiwei.txt"
                            )
                            if (it.byteArray.size == 1) {
                                bleResponseLabel.value = "Reply NAK" // 回复nak -> Reply NAK
                            } else if (it.byteArray.size >= 2 && it.byteArray[0] == 0x00.toByte() && it.byteArray[2] == 0x00.toByte()) {
                                bleResponseLabel.value =
                                    "Log sent successfully" // log发送完成 -> Log sent successfully
                            } else {
                                getLog()
                            }
                        }
                        return@subscribe
                    }

                    if (it.data !is Notify.Diff) {
                        if (it.data is Notify.TempHistory) {
                            if (!(it.data as Notify.TempHistory).hasNext) {
                                TestRepository.testLocal(
                                    localBasePath,
                                    it.byteArray.drop(7).map {
                                        String.format(
                                            "0x%02X",
                                            it
                                        )
                                    }.chunked(40).joinToString(",\n") {
                                        it.joinToString(",")
                                    } + ",\n\n\n",
                                    "temp.txt"
                                )
                            }
                        } else if (it.data is Notify.OriginSleepHistory) {
                            if (!(it.data as Notify.OriginSleepHistory).hasNext) {
                                TestRepository.testLocal(
                                    localBasePath,
                                    it.byteArray.drop(7).map {
                                        String.format(
                                            "0x%02X",
                                            it
                                        )
                                    }.chunked(40).joinToString(",\n") {
                                        it.joinToString(",")
                                    } + ",\n\n\n",
                                    "origin_sleep.txt"
                                )
                            }
                        } else if (it.data is Notify.SleepHistory) {
                            TestRepository.testLocal(
                                localBasePath,
                                Date().toString() + "\n" + "\n" + it.byteArray.map {
                                    String.format(
                                        "0x%02X",
                                        it
                                    )
                                }.chunked(40).joinToString(",\n") {
                                    it.joinToString(",")
                                } + "\n\n\n",
                                "sleep.txt"
                            )
                        } else {
                            TestRepository.testLocal(
                                localBasePath,
                                Date().toString() + "\n" + "\n" + it.byteArray.map {
                                    String.format("0x%02X", it)
                                }.chunked(40)
                                    .joinToString(",\n") { it.joinToString(",") } + "\n\n\n",
                                "demo-test.txt"
                            )
                        }

                    }


                },
                {
                    it.printStackTrace()
                }
            ).let {}

            StarmaxBleClient.instance.realTimeDataStream().takeUntil(disconnectSubject).subscribe({
                    bleResponse.value = JSONObject(
                        mapOf(
                            "gsensor_list" to it.gensorsList.map {
                                hashMapOf(
                                    "x" to it.x,
                                    "y" to it.y,
                                    "z" to it.z,
                                )
                            }.toMutableList(),
                            "steps" to it.steps,
                            "calore" to it.calore,
                            "distance" to it.distance,
                            "heart_rate" to it.heartRate,
                            "blood_pressure_ss" to it.bloodPressureSs,
                            "blood_pressure_fz" to it.bloodPressureFz,
                            "blood_oxygen" to it.bloodOxygen,
                            "temp" to it.temp,
                            "blood_sugar" to it.bloodSugar
                        )
                    ).toString()
                }, {

                }).let { }

            StarmaxBleClient.instance.sportSyncFromDeviceStream().takeUntil(disconnectSubject)
                .subscribe({
                    val sportStatusLabel = when (it.sportStatus and 0x0F) {
                        1 -> "Start" // 开启 -> Start
                        2 -> "In progress" // 进行中 -> In progress
                        3 -> "Pause" // 暂停 -> Pause
                        4 -> "Resume" // 恢复 -> Resume
                        5 -> "End" // 结束 -> End
                        else -> "Unknown" // 未知 -> Unknown
                    }

                    bleResponseLabel.value = ("Sport type:${it.sportType}\n" // 运动类型: -> Sport type:
                            + "Sport status:${sportStatusLabel}\n" // 运动状态: -> Sport status:
                            + "Steps:${it.steps}\n" // 步数: -> Steps:
                            + "Distance:${it.distance}\n" // 距离: -> Distance:
                            + "Speed:${it.speed}\n" // 速度: -> Speed:
                            + "Calories:${it.calorie}\n" // 卡路里: -> Calories:
                            + "Pace time:${it.paceTime}\n" // 配速: -> Pace time:
                            + "Cadence:${it.cadence}\n" // 步频: -> Cadence:
                            + "Stride:${it.stepFrequency}\n" // 步幅: -> Stride:
                            + "Heart rate:${it.heartRate}\n" // 心率: -> Heart rate:
                            + "Sport duration:${it.sportSeconds}\n" // 运动时间: -> Sport duration:
                            )
                }, {

                }).let { }

            StarmaxBleClient.instance.healthMeasureStream()
                .takeUntil(disconnectSubject)
                .subscribe(
                    {
                        if (it.status == 0) {
                            when (it.type) {
                                0x63 -> bleResponseLabel.value =
                                    "Heart rate value:${it.dataList[0]}" // 心率值: -> Heart rate value:

                                0x66 -> bleResponseLabel.value =
                                    "Pressure value:${it.dataList[0]}" // 压力值: -> Pressure value:

                                else -> {

                                }
                            }
                        } else {
                            bleResponseLabel.value =
                                "Measurement failed" // 測量失敗 -> Measurement failed
                        }
                    },
                    {

                    }
                ).let {}

            StarmaxBleClient.instance.unpairStream().takeUntil(disconnectSubject)
                .takeUntil(disconnectSubject)
                .subscribe({
                    bleResponseLabel.value =
                        "Received unbind communication, preparing to reply" // 收到可解綁通信，准备回复 -> Received unbind communication, preparing to reply
                    StarmaxBleClient.instance.unpairAck().subscribe({
                        bleResponseLabel.value =
                            "Received unbind communication, replied" // 收到可解綁通信,已应答 -> Received unbind communication, replied
                    }, {}).let { }
                }, {

                }).let { }

            StarmaxBleClient.instance.nfcCardStatusStream()
                .takeUntil(disconnectSubject)
                .subscribe(
                    {
                        bleResponseLabel.value =
                            (if (it.type == 1) "Create white card" else "Copy card") + (if (it.status == 1) "successful" else "failed") // 创建白卡 -> Create white card, 复制卡片 -> Copy card, 成功 -> successful, 失败 -> failed
                    },
                    {

                    }
                ).let {}

            StarmaxBleClient.instance.nfcM1Stream()
                .takeUntil(disconnectSubject)
                .subscribe(
                    {
                        var waitM1DataList = it.waitM1DataList

                        bleResponseLabel.value =
                            "Obtained NFC data waiting to be cracked" // 获取到NFC待破解数据 -> Obtained NFC data waiting to be cracked

                        StarmaxBleClient.instance.nfcM1Ack().subscribe({
                            bleResponseLabel.value =
                                "Obtained NFC data waiting to be cracked:" + byteArrayToHexString(
                                    waitM1DataList.map { it.toByte() } // 获取到NFC待破解数据: -> Obtained NFC data waiting to be cracked:
                                        .toByteArray()) + ", replied" // ,已应答 -> , replied
                        }, {}).let { }

                        object : Thread() {
                            override fun run() {

                                println(byteArrayToHexString(it.waitM1DataList.map { it.toByte() }
                                    .toByteArray()))

                                CrackRepository.m1(byteArrayToHexString(it.waitM1DataList.map { it.toByte() }
                                    .toByteArray()), onSuccess = { crackData, _ ->
                                    bleResponseLabel.value =
                                        "Obtained NFC data waiting to be cracked, cracked successfully" // 获取到NFC待破解数据,破解完成 -> Obtained NFC data waiting to be cracked, cracked successfully
                                    if (crackData != null) {
                                        StarmaxBleClient.instance.nfcM1Result(
                                            true,
                                            hexStringToByteArray(crackData.crackData)
                                        ).subscribe({
                                            bleResponseLabel.value =
                                                "Obtained NFC data waiting to be cracked, cracked successfully, replied" + byteArrayToHexString( // 获取到NFC待破解数据,破解完成,已回复 -> Obtained NFC data waiting to be cracked, cracked successfully, replied
                                                    hexStringToByteArray(crackData.crackData)
                                                )
                                        }, {

                                        }).let { }
                                    }
                                }, onError = { e ->
                                    e?.printStackTrace()
                                })
                            }
                        }.start()


                    },
                    {

                    }
                ).let {}
        }
    }

    fun byteArrayToHexString(data: ByteArray): String {
        val bytes = data
        val stringBuilder = StringBuilder()

        for (i in bytes.indices) {
            stringBuilder.append(String.format("%02X", bytes[i])) // 将字节转换为十六进制字符串
        }
        return stringBuilder.toString()
    }

    fun hexStringToByteArray(hexString: String): ByteArray {
        val len = hexString.length
        val data = ByteArray(len / 2)
        for (i in 0 until len step 2) {
            val hex = hexString.substring(i, i + 2)
            data[i / 2] = hex.toInt(16).toByte()
        }
        return data
    }

    fun setBroadcastOnOff(onOff: Boolean) {
        StarmaxBleClient.instance.setBroadcastOnOff(onOff).subscribe({

        }, {

        }).let {
            sendDisposable.add(it)
        }
    }

    fun pair() {
        initData()
        StarmaxBleClient.instance.pair().subscribe({
            bleResponseLabel.value = "佩戴状态:" + it.pairStatus
        }, {

        }).let {
            sendDisposable.add(it)
        }
    }

    fun pairGts10(type: Int) {
        initData()
        StarmaxBleClient.instance.pairGts10(type).subscribe({
            bleResponseLabel.value = "佩戴状态:" + it.pairStatus
        }, {

        }).let {
            sendDisposable.add(it)
        }
    }

    fun getBtStatus() {
        initData()
        Utils.p(StarmaxSend().getBtStatus())

        StarmaxBleClient.instance.getBtStatus().subscribe({
            bleResponseLabel.value = "bt状态:" + it.btStatus
        }, {

        }).let {
            sendDisposable.add(it)
        }
    }

    fun findDevice(isFind: Boolean) {
        initData()
        StarmaxBleClient.instance.findDevice(isFind = isFind).subscribe({
            bleResponseLabel.value = "查找手环成功"
        }, {}).let {

        }
    }

    fun getPower() {
        initData()
        StarmaxBleClient.instance.getPower().subscribe({
            bleResponseLabel.value = ("电量:${it.power}\n" + "是否充电:${it.isCharge}")
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun getVersion(labelVisible: Boolean) {
        initData()
        val calendar = Calendar.getInstance()
        val lastMills = calendar.timeInMillis
        StarmaxBleClient.instance.getVersion().subscribe({
            val currentCalendar = Calendar.getInstance()
            bleModel = it.model
            bleVersion = it.version
            bleUiVersion = it.uiVersion
            uiSupportDifferentialUpgrade = it.uiSupportDifferentialUpgrade
            lcdWidth = it.lcdWidth
            lcdHeight = it.lcdHeight

            if (labelVisible) {//目的是发送表盘时需要bleModel实例
                bleResponseLabel.value = (
                        "固件版本:${bleVersion}\n"
                                + "ui版本:${bleUiVersion}\n"
                                + "设备接收buf大小:${it.bufferSize}\n"
                                + "lcd宽:${it.lcdWidth}\n"
                                + "lcd高:${it.lcdHeight}\n"
                                + "屏幕类型:${it.screenType}\n"
                                + "设备型号:${bleModel}\n"
                                + "ui是否强制升级:${it.uiForceUpdate}\n"
                                + "是否支持差分升级:${uiSupportDifferentialUpgrade}\n"
                                + "是否支持血糖:${it.supportSugar}\n"
                                + "设备协议版本:${it.protocolVersion}\n"
                                + "app协议版本:${StarmaxSend().version()}\n"
                                + "是否支持新睡眠:${it.sleepVersion}\n"
                                + "睡眠展示方式:${it.sleepShowType}\n"
                                + "是否支持睡眠计划:${it.supportSleepPlan}\n"
                                + "是否支持双向运动:${it.supportSyncSport}\n"
                                + "表盘版本号:${it.dialVersion}\n"
                                + "耗时：${currentCalendar.timeInMillis - lastMills}"
                        )
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun setTime() {
        initData()
        StarmaxBleClient.instance.setTime().subscribe({
            if (it.status == 0) {
                bleResponseLabel.value = "设置时区成功"
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun setTimeOffset() {
        initData()
        StarmaxBleClient.instance.setTimeOffset().subscribe({
            if (it.status == 0) {
                bleResponseLabel.value = "设置时区成功"
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun getTimeOffset() {
        initData()
        StarmaxBleClient.instance.getTimeOffset().subscribe({
            if (it.status == 0) {
                bleResponseLabel.value = "获取时区成功"
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun getHealthDetail() {
        initData()
        StarmaxBleClient.instance.getHealthDetail().subscribe({
            if (it.status == 0) {
                bleResponseLabel.value = (
                        "Total step count:${it.totalSteps}\n"
                                + "Total calories (Cal):${it.totalHeat}\n"
                                + "Total distance (m):${it.totalDistance}\n"
                                + "Total sleep duration (minutes):${it.totalSleep}\n"
                                + "Deep sleep duration:${it.totalDeepSleep}\n"
                                + "Light sleep duration:${it.totalLightSleep}\n"
                                + "Current heart rate:${it.currentHeartRate}\n"
                                + "Current blood pressure:${it.currentSs} /${it.currentFz}\n"
                                + "Current blood oxygen:${it.currentBloodOxygen}\n"
                                + "Current pressure:${it.currentPressure}\n"
                                + "Current MAI:${it.currentMai}\n"
                                + "Current MET:${it.currentMet}\n"
                                + "Current temperature:${it.currentTemp}\n"
                                + "Current blood sugar:${it.currentBloodSugar}\n"
                                + "Is worn:${it.isWear}\n"
                                + "Respiration rate:${it.respirationRate}\n"
                                + "Shake head:${it.shakeHead}"
                        )
                print(bleResponseLabel.value)
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {

        }
    }

    fun getClock() {
        initData()
        StarmaxBleClient.instance.getClock().subscribe({
            if (it.status == 0) {
                bleResponseLabel.value = it.toString()
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun setClock() {
        initData()
        StarmaxBleClient.instance.setClock(
            clocks = arrayListOf(
                Clock(9, 0, true, intArrayOf(1, 1, 0, 1, 0, 1, 0), 0),
                Clock(11, 45, true, intArrayOf(1, 1, 0, 1, 0, 1, 0), 0),
                Clock(18, 0, false, intArrayOf(1, 1, 0, 1, 0, 1, 0), 0)
            )
        ).subscribe({
            if (it.status == 0) {
                bleResponseLabel.value = "设置闹钟成功"
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun getLongSit() {
        initData()
        StarmaxBleClient.instance.getLongSit().subscribe({
            if (it.status == 0) {
                bleResponseLabel.value = it.toString()
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun setLongSit() {
        initData()
        StarmaxBleClient.instance.setLongSit(
            true,
            9,
            0,
            23,
            0,
            1
        ).subscribe({
            if (it.status == 0) {
                bleResponseLabel.value = "设置久坐成功"
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun getDrinkWater() {
        initData()
        StarmaxBleClient.instance.getDrinkWater().subscribe({
            if (it.status == 0) {
                bleResponseLabel.value = it.toString()
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun setDrinkWater() {
        initData()
        StarmaxBleClient.instance.setDrinkWater(
            true,
            9,
            0,
            23,
            0,
            1
        ).subscribe({
            if (it.status == 0) {
                bleResponseLabel.value = "设置喝水成功"
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun sendMessage() {
        initData()
        StarmaxBleClient.instance.sendMessage(
            MessageType.Other,
            "新消息",
            "我觉得能接收十个字的我觉得能接收十个字的我觉得能接收十个字的我觉得能接收十个字的我觉得能接收十个字的我觉得能接收十个字的我觉得能接收十个字的"
        ).subscribe({
            if (it.status == 0) {
                bleResponseLabel.value = "发送消息成功"
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun setWeather() {
        initData()
        StarmaxBleClient.instance.setWeather(
            arrayListOf(
                WeatherDay(-9, 40, -20, 0x05, 0x25, 0x0a, 0x07, 0x01, 0x01),
                WeatherDay(-10, 0, -16, 0x05, 0x25, 0x0a, 0x07, 0x01, 0x05),
                WeatherDay(-11, 0, -10, 0x05, 0x25, 0x0a, 0x07, 0x01, 0x06),
                WeatherDay(-12, 35, 19, 0x05, 0x25, 0x0a, 0x07, 0x01, 0x12)
            )
        ).subscribe({
            if (it.status == 0) {
                bleResponseLabel.value = "设置天气成功"
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun getWeatherSeven() {
        initData()
        StarmaxBleClient.instance.getWeatherSeven().subscribe({
            if (it.status == 0) {
                bleResponseLabel.value = "读取天气成功"
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }

            val result = WeatherSevenFactory().buildGetMap(it)
            bleResponse.value = JSONObject(result.obj!!).toString()
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun setWeatherSeven() {
        initData()
        StarmaxBleClient.instance.setWeatherSeven(
            cityName = "深圳市",
            arrayListOf(
                WeatherDay(
                    -60,
                    -60,
                    -60,
                    0x05,
                    0x25,
                    0x0a,
                    0x07,
                    0x01,
                    0x06,
                    0,
                    0,
                    23,
                    59,
                    0,
                    0,
                    23,
                    59
                ),
                WeatherDay(
                    -60,
                    -60,
                    -60,
                    0x05,
                    0x25,
                    0x0a,
                    0x07,
                    0x01,
                    0x06,
                    0,
                    0,
                    23,
                    59,
                    0,
                    0,
                    23,
                    59
                ),
                WeatherDay(
                    -60,
                    -60,
                    -60,
                    0x05,
                    0x25,
                    0x0a,
                    0x07,
                    0x01,
                    0x06,
                    0,
                    0,
                    23,
                    59,
                    0,
                    0,
                    23,
                    59
                ),
                WeatherDay(
                    -60,
                    -60,
                    -60,
                    0x05,
                    0x25,
                    0x0a,
                    0x07,
                    0x01,
                    0x06,
                    0,
                    0,
                    23,
                    59,
                    0,
                    0,
                    23,
                    59
                ),
                WeatherDay(
                    -60,
                    -60,
                    -60,
                    0x05,
                    0x25,
                    0x0a,
                    0x07,
                    0x01,
                    0x06,
                    0,
                    0,
                    23,
                    59,
                    0,
                    0,
                    23,
                    59
                ),
                WeatherDay(
                    -60,
                    -60,
                    -60,
                    0x05,
                    0x25,
                    0x0a,
                    0x07,
                    0x01,
                    0x06,
                    0,
                    0,
                    23,
                    59,
                    0,
                    0,
                    23,
                    59
                ),
                WeatherDay(
                    -60,
                    -60,
                    -60,
                    0x05,
                    0x25,
                    0x0a,
                    0x07,
                    0x01,
                    0x06,
                    0,
                    0,
                    23,
                    59,
                    0,
                    0,
                    23,
                    59
                ),
                WeatherDay(
                    -60,
                    -60,
                    -60,
                    0x05,
                    0x25,
                    0x0a,
                    0x07,
                    0x01,
                    0x06,
                    0,
                    0,
                    23,
                    59,
                    0,
                    0,
                    23,
                    59
                ),
            )
        ).subscribe({
            if (it.status == 0) {
                bleResponseLabel.value = "设置天气成功"
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun getSummerWorldClock() {
        initData()
        StarmaxBleClient.instance.getSummerWorldClock().subscribe({
            if (it.status == 0) {
                bleResponseLabel.value = "读取天气成功"
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }

            val result = SummerWorldClockFactory().buildGetMap(it)
            bleResponse.value = JSONObject(result.obj!!).toString()
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun setSummerWorldClock() {
        initData()
        StarmaxBleClient.instance.setSummerWorldClock(
            arrayListOf(
                SummerWorldClock(2, 3, 2, 11, 1, 60),
                SummerWorldClock(3, 3, 1 + 8, 10, 1 + 8, 60),
                SummerWorldClock(4, 3, 1 + 8, 10, 1 + 8, 60),
                SummerWorldClock(5, 3, 2, 11, 1, 60),
                SummerWorldClock(9, 10, 1, 4, 1, 60),
                SummerWorldClock(10, 3, 2, 11, 1, 60),
                SummerWorldClock(12, 3, 1 + 8, 10, 1 + 8, 60),
                SummerWorldClock(13, 3, 1 + 8, 10, 1 + 8, 60),
                SummerWorldClock(17, 3, 2, 11, 1, 60),
                SummerWorldClock(18, 3, 2, 11, 1, 60),
                SummerWorldClock(20, 3, 1 + 8, 10, 1 + 8, 60),
                SummerWorldClock(23, 10, 1, 4, 1, 60),
                SummerWorldClock(25, 10, 1, 4, 1, 60),
            )
        ).subscribe({
            if (it.status == 0) {
                bleResponseLabel.value = "设置世界时钟成功"
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun sendMusic() {
        initData()
        StarmaxBleClient.instance.musicControl(
            1, 20, 30, "Умри если меня не любишь (不爱我就去死吧)", ""
        ).subscribe({
            bleResponseLabel.value = "音乐控制成功"
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun getLog() {//TODO
//        initData()
//        StarmaxBleClient.instance.getLog().subscribe({
//            bleResponseLabel.value = "获取log成功"
//        }, {}).let {
//            sendDisposable.add(it)
//        }
    }

    fun getCustomDeviceMode() {
        initData()
        StarmaxBleClient.instance.getCustomDeviceMode().subscribe({
            if (it.status == 0) {
                val modeStr = if (it.mode == 0) "正常模式" else "上课模式"

                bleResponseLabel.value = "类型:${it.type},模式:${modeStr}"
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun setCustomDeviceMode() {
        initData()
        StarmaxBleClient.instance.setCustomDeviceMode(0).subscribe({
            if (it.status == 0) {
                bleResponseLabel.value = it.toString()
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun getCustomDeviceName() {
        initData()
        StarmaxBleClient.instance.getCustomDeviceName().subscribe({
            if (it.status == 0) {
                bleResponseLabel.value = "类型:${it.type},名称:${it.deviceName}"
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun setCustomDeviceName() {
        initData()
        StarmaxBleClient.instance.setCustomDeviceName("A99").subscribe({
            if (it.status == 0) {
                bleResponseLabel.value = it.toString()
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun getCustomDeviceDailyData() {
        initData()
        StarmaxBleClient.instance.getCustomDeviceDailyData().subscribe({
            if (it.status == 0) {
                bleResponseLabel.value = ("设备名称:${it.deviceName}\n"
                        + "当前模式:${it.mode}\n"
                        + "心率:${it.heartRate}\n"
                        + "步数:${it.steps}\n"
                        + "血压:${it.ss}/${it.fz}\n"
                        + "血氧:${it.oxygen}\n"
                        + "血糖:${it.bloodSugar}\n"
                        + "温度:${it.temp}\n"
                        + "梅脱:${it.met}\n"
                        + "MAI:${it.mai}\n"
                        + "压力:${it.stress}\n"
                        + "卡路里:${it.calorie}\n"
                        + "电量:${it.power}\n"
                        )
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {

        }
    }

    fun getSportMode() {
        initData()
        StarmaxBleClient.instance.getSportMode().subscribe({
            if (it.status == 0) {
                var str = ""

                val dataList = it.sportModesList
                for (i in 0 until dataList.size) {
                    str += "运动模式:${sportModeLabel(dataList[i])}\n"
                }

                bleResponseLabel.value = str
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun setDisplayMode(isDisplay: Boolean) {
        initData()
        StarmaxBleClient.instance.setDisplayMode(isDisplay).subscribe({
            if (it.status == 0) {
                bleResponseLabel.value = if (isDisplay) "开启演示模式成功" else "关闭演示模式"
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun setShipMode(isOpen: Boolean) {
        initData()
        StarmaxBleClient.instance.setShipMode(isOpen).subscribe({
            if (it.status == 0) {
                bleResponseLabel.value = if (isOpen) "开启船运模式成功" else "关闭船运模式成功"
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }


    fun getCustomDeviceShake() {
        initData()
        StarmaxBleClient.instance.getCustomDeviceShake().subscribe({
            if (it.status == 0) {
                bleResponseLabel.value = "类型:${it.type},时间:${it.time}"
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun setCustomDeviceShake() {
        initData()
        StarmaxBleClient.instance.setCustomDeviceShake(300).subscribe({
            if (it.status == 0) {
                bleResponseLabel.value = it.toString()
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun getCustomDeviceShakeOnOff() {
        initData()
        StarmaxBleClient.instance.getCustomDeviceShakeOnOff().subscribe({
            if (it.status == 0) {
                bleResponseLabel.value =
                    "类型:${it.type},摇一摇:${it.shakeOnOff},抬腕:${it.handOnOff}"
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun getCustomDeviceShakeTimes() {
        initData()
        StarmaxBleClient.instance.getCustomDeviceShakeTime().subscribe({
            if (it.status == 0) {
                bleResponseLabel.value =
                    "类型:${it.type},摇一摇次数:${it.shakeTimes},抬腕次数:${it.handTimes}"
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun setCustomDeviceShakeOnOff() {
        initData()
        StarmaxBleClient.instance.setCustomDeviceShakeOnOff(true, true).subscribe({
            if (it.status == 0) {
                bleResponseLabel.value = it.toString()
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun setSportMode() {
        initData()
        StarmaxBleClient.instance.setSportMode(
            listOf(
                0x0A,
                0x0B,
                0x0C,
                0x0D
            )
        ).subscribe({
            if (it.status == 0) {
                bleResponseLabel.value = it.toString()
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun sendHealthMeasure(isOpen: Boolean) {
        initData()
        StarmaxBleClient.instance.sendHealthMeasure(HistoryType.Pressure, isOpen).subscribe({
            bleResponseLabel.value = if (isOpen) "开启成功" else "关闭成功"
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun sendHeartRateHealthMeasure(isOpen: Boolean) {
        initData()
        StarmaxBleClient.instance.sendHealthMeasure(HistoryType.HeartRate, isOpen).subscribe({
            bleResponseLabel.value = if (isOpen) "开启成功" else "关闭成功"
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun getDebugInfo(fileType: Int) {
        initData()
        StarmaxBleClient.instance.getDebugInfo(packageId, fileType).subscribe({
            if (it.status == 0) {
                if (it.dataList.size > 0) {
                    TestRepository.testLocal(
                        localBasePath,
                        if (fileType == 3) {
                            it.dataList.map { it.toByte() }.toByteArray()
                                .toString(Charsets.US_ASCII).replace("TAG=", "\nTAG=")
                        } else {
                            it.dataList.map { String.format("0x%02X", it.toByte()) }.toList()
                                .chunked(8)
                                .map {
                                    it.joinToString(",")
                                }.joinToString(",\n") + ",\n\n\n"
                        },
                        when (fileType) {
                            1 -> "battery.txt"
                            2 -> "gsensor.txt"
                            3 -> "sleep.txt"
                            else -> "sleep.txt"
                        }
                    )

                    packageId += 1
                    getDebugInfo(fileType)
                }
                bleResponseLabel.value =
                    "获取" + (if (fileType == 1) "battery.txt" else "gsensor.txt") + "第" + packageId + "包"
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun getSportHistory() {
        initData()
        StarmaxBleClient.instance.getSportHistory().subscribe({
            if (it.status == 0) {
                bleResponseLabel.value =
                    SportHistoryFactory(StarmaxBleClient.instance.bleNotify).buildMapFromProtobuf(it)
                        .toJson()
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun getStepHistory(time: Long) {
        initData()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        StarmaxBleClient.instance.getStepHistory(calendar).subscribe({
            if (it.status == 0) {
                var str = ("采样间隔:" + it.interval + "分钟\n"
                        + "日期:" + it.year + "-" + it.month + "-" + it.day + "\n"
                        + "数据长度:" + it.dataLength + "\n"
                        )

                val stepList = it.stepsList
                for (i in 0 until stepList.size) {
                    val oneData = stepList[i]
                    str += ("时间:" + oneData.hour + ":" + oneData.minute
                            + " 步数" + oneData.steps
                            + ",卡路里" + ((oneData.calorie).toDouble() / 1000) + "千卡"
                            + ",距离" + ((oneData.distance).toDouble() / 10) + "米\n")
                }

                val sleepList = it.sleepsList
                for (i in 0 until sleepList.size) {
                    val oneData = sleepList[i]
                    str += ("时间:" + oneData.hour + ":" + oneData.minute
                            + " 睡眠状态" + oneData.sleepStatus + "\n")
                }

                bleResponseLabel.value = str
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun getBloodPressureHistory(time: Long) {
        initData()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        StarmaxBleClient.instance.getBloodPressureHistory(calendar).subscribe({
            if (it.status == 0) {
                var str = ("采样间隔:" + it.interval + "分钟\n"
                        + "日期:" + it.year + "-" + it.month + "-" + it.day + "\n"
                        + "数据长度:" + it.dataLength + "\n"
                        )

                val dataList = it.dataList
                for (i in 0 until dataList.size) {
                    val oneData = dataList[i]
                    str += "时间:" + oneData.hour + ":" + oneData.minute + " 伸缩压" + oneData.ss + " 舒张压" + oneData.fz + "\n"
                }

                bleResponseLabel.value = str
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun getSleepClock() {
        initData()
        StarmaxBleClient.instance.getSleepClock().subscribe({
            if (it.status == 0) {
                val str =
                    ("入睡:${it.fallAsleepHour}:${it.fallAsleepMinute} ${it.fallAsleepOnOff}\n"
                            + "起床:${it.getUpHour}:${it.getUpMinute} ${it.getUpOnOff}\n"
                            + "重复:" + (it.repeatsList.map { it.toString() }.joinToString(","))
                            + "提前" + it.reminderBeforeFallAsleep + "分钟提醒\n"
                            )
                bleResponseLabel.value = str
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun setSleepClock() {
        initData()
        StarmaxBleClient.instance.setSleepClock(
            fallAsleepHour = 0,
            fallAsleepMinute = 0,
            fallAsleepOnOff = true,
            getUpHour = 23,
            getUpMinute = 59,
            getUpOnOff = true,
            onOff = true,
            repeats = intArrayOf(1, 1, 1, 1, 1, 1, 1),
            reminderBeforeFallAsleep = 30
        ).subscribe({
            if (it.status == 0) {

                bleResponseLabel.value = it.toString()
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun getHeartRateHistory(time: Long) {
        initData()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        StarmaxBleClient.instance.getHeartRateHistory(calendar).subscribe({
            if (it.status == 0) {
                var str = ("采样间隔:" + it.interval + "分钟\n"
                        + "日期:" + it.year + "-" + it.month + "-" + it.day + "\n"
                        + "数据长度:" + it.dataLength + "\n"
                        )

                val dataList = it.dataList
                for (i in 0 until dataList.size) {
                    val oneData = dataList[i]
                    str += "时间:" + oneData.hour + ":" + oneData.minute + " 心率" + oneData.value + "%\n"
                }

                bleResponseLabel.value = str
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun getExerciseHistory(time: Long) {
        initData()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        StarmaxBleClient.instance.getExerciseHistory(calendar).subscribe({
            if (it.status == 0) {
                var str = ("采样间隔:" + it.interval + "分钟\n"
                        + "日期:" + it.year + "-" + it.month + "-" + it.day + "\n"
                        + "数据长度:" + it.dataLength + "\n"
                        )

                val exerciseDataList = it.exerciseDataList
                for (i in 0 until exerciseDataList.size) {
                    val oneData = exerciseDataList[i]
                    str += "时间:" + oneData.hour + ":" + oneData.minute + " 中高强度" + oneData.value + "\n"
                }

                val standDataList = it.standDataList
                for (i in 0 until standDataList.size) {
                    val oneData = standDataList[i]
                    str += "时间:" + oneData.hour + ":" + oneData.minute + " 站立" + oneData.value + "\n"
                }

                bleResponseLabel.value = str
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun getBloodOxygenHistory(time: Long) {
        initData()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        StarmaxBleClient.instance.getBloodOxygenHistory(calendar).subscribe({
            if (it.status == 0) {
                var str = ("采样间隔:" + it.interval + "分钟\n"
                        + "日期:" + it.year + "-" + it.month + "-" + it.day + "\n"
                        + "数据长度:" + it.dataLength + "\n"
                        )

                val dataList = it.dataList
                for (i in 0 until dataList.size) {
                    val oneData = dataList[i]
                    str += "时间:" + oneData.hour + ":" + oneData.minute + " 血氧" + oneData.value + "%\n"
                }

                bleResponseLabel.value = str
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun getRespirationRateHistory(time: Long) {
        initData()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        StarmaxBleClient.instance.getRespirationRateHistory(calendar).subscribe({
            if (it.status == 0) {
                var str = ("采样间隔:" + it.interval + "分钟\n"
                        + "日期:" + it.year + "-" + it.month + "-" + it.day + "\n"
                        + "数据长度:" + it.dataLength + "\n"
                        )

                val dataList = it.dataList
                for (i in 0 until dataList.size) {
                    val oneData = dataList[i]
                    str += "时间:" + oneData.hour + ":" + oneData.minute + " 呼吸率" + oneData.value + "%\n"
                }

                bleResponseLabel.value = str
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun getPressureHistory(time: Long) {
        initData()

        changeMtu {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = time
            StarmaxBleClient.instance.getPressureHistory(calendar).subscribe({
                if (it.status == 0) {
                    var str = ("采样间隔:" + it.interval + "分钟\n"
                            + "日期:" + it.year + "-" + it.month + "-" + it.day + "\n"
                            + "数据长度:" + it.dataLength + "\n"
                            )

                    val dataList = it.dataList
                    for (i in 0 until dataList.size) {
                        val oneData = dataList[i]
                        str += "时间:" + oneData.hour + ":" + oneData.minute + " 压力" + oneData.value + "%\n"
                    }

                    bleResponseLabel.value = str
                } else {
                    bleResponseLabel.value = statusLabel(it.status)
                }
            }, {}).let {
                sendDisposable.add(it)
            }
        }
    }

    fun getMetHistory(time: Long) {
        initData()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        StarmaxBleClient.instance.getMetHistory(calendar).subscribe({
            if (it.status == 0) {
                var str = ("采样间隔:" + it.interval + "分钟\n"
                        + "日期:" + it.year + "-" + it.month + "-" + it.day + "\n"
                        + "数据长度:" + it.dataLength + "\n"
                        )

                val dataList = it.dataList
                for (i in 0 until dataList.size) {
                    val oneData = dataList[i]
                    str += "梅脱:" + oneData
                }

                bleResponseLabel.value = str
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun getOriginSleepHistory(time: Long) {
        initData()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        StarmaxBleClient.instance.getOriginSleepHistory(calendar).subscribe({
            if (it.status == 0) {
                var str = ("采样间隔:" + it.interval + "分钟\n"
                        + "日期:" + it.year + "-" + it.month + "-" + it.day + "\n"
                        + "数据长度:" + it.dataLength + "\n"
                        )

                val dataList = it.dataList
                for (i in 0 until dataList.size) {
                    val oneData = dataList[i]
                    if (oneData.value > 0) {
                        val valueList = Utils.int2byte(oneData.value, 2)
                        str += "时间:" + oneData.hour + ":" + oneData.minute + " 红外:" + valueList[1] + " sar:" + (valueList[0] * 256) + "\n"
                    }
                }

                bleResponseLabel.value = str
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun getShakeHeadHistory(time: Long) {
        initData()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        StarmaxBleClient.instance.getShakeHeadHistory(calendar).subscribe({
            if (it.status == 0) {
                var str = ("采样间隔:" + it.interval + "分钟\n"
                        + "日期:" + it.year + "-" + it.month + "-" + it.day + "\n"
                        + "数据长度:" + it.dataLength + "\n"
                        )

                val dataList = it.dataList
                for (i in 0 until dataList.size) {
                    val oneData = dataList[i]
                    if (oneData.value > 0) {
                        str += "时间:" + oneData.hour + ":" + oneData.minute + " 摇头:" + oneData.value + "\n"
                    }
                }

                bleResponseLabel.value = str
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun getTempHistory(time: Long) {
        initData()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        StarmaxBleClient.instance.getTempHistory(calendar).subscribe({
            if (it.status == 0) {
                var str = ("采样间隔:" + it.interval + "分钟\n"
                        + "日期:" + it.year + "-" + it.month + "-" + it.day + "\n"
                        + "数据长度:" + it.dataLength + "\n"
                        )

                val dataList = it.dataList
                for (i in 0 until dataList.size) {
                    val oneData = dataList[i]
                    str += "时间:" + oneData.hour + ":" + oneData.minute + " 温度" + oneData.value + "%\n"
                }

                bleResponseLabel.value = str
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun getMaiHistory(time: Long) {
        initData()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        StarmaxBleClient.instance.getMaiHistory(calendar).subscribe({
            if (it.status == 0) {
                var str = ("采样间隔:" + it.interval + "分钟\n"
                        + "日期:" + it.year + "-" + it.month + "-" + it.day + "\n"
                        + "数据长度:" + it.dataLength + "\n"
                        )

                val dataList = it.dataList
                for (i in 0 until dataList.size) {
                    val oneData = dataList[i]
                    str += "MAI:" + oneData
                }

                bleResponseLabel.value = str
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun getBloodSugarHistory(time: Long) {
        initData()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        StarmaxBleClient.instance.getBloodSugarHistory(calendar).subscribe({
            if (it.status == 0) {
                var str = ("采样间隔:" + it.interval + "分钟\n"
                        + "日期:" + it.year + "-" + it.month + "-" + it.day + "\n"
                        + "数据长度:" + it.dataLength + "\n"
                        )

                val dataList = it.dataList
                for (i in 0 until dataList.size) {
                    val oneData = dataList[i]
                    str += "时间:" + oneData.hour + ":" + oneData.minute + " 血糖" + oneData.value + "\n"
                }

                bleResponseLabel.value = str
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun getCustomHealthGoalsHistory(time: Long) {
        initData()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        StarmaxBleClient.instance.getCustomHealthGoalsHistory(calendar).subscribe({
            if (it.status == 0) {
                var str = ("日期:" + it.year + "-" + it.month + "-" + it.day + "\n\n")

                it.goalInfosList.forEach { goalInfo ->
                    str += "-------------------------\n处方索引:${goalInfo.index}\n开始日期:${goalInfo.startYear}-${goalInfo.startMonth}-${goalInfo.startDay}\n结束日期:${goalInfo.endYear}-${goalInfo.endMonth}-${goalInfo.endDay}\n----------------------------\n\n"
                    goalInfo.taskInfosList.forEach { taskInfo ->
                        str += "任务索引:${taskInfo.index}\n状态:${taskInfo.status}\n完成百分比:${taskInfo.scale},实时计时${taskInfo.realSeconds}\n"
                        str += "目标时间:${taskInfo.goalMinutes},实际时间:${taskInfo.completeMinutes}\n"
                        str += "执行任务时间:${taskInfo.goalStartHour}:${taskInfo.goalStartMinute}-${taskInfo.goalEndHour}:${taskInfo.goalEndMinute}\n"
                        str += "平均心率:${taskInfo.avgHeartRate},平均步频:${taskInfo.avgStepFreq}\n"
                        str += "处方任务产生步数:${taskInfo.steps}\n\n"
                    }
                }

                bleResponseLabel.value = str
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun getSleepHistory(time: Long) {
        initData()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        StarmaxBleClient.instance.getSleepHistory(calendar).subscribe({
            if (it.status == 0) {
                var str = ("采样间隔:" + it.interval + "分钟\n"
                        + "日期:" + it.year + "-" + it.month + "-" + it.day + "\n"
                        + "数据长度:" + it.dataLength + "\n"
                        )

                val dataList = it.dataList
                for (i in 0 until dataList.size) {
                    val oneData = dataList[i]
                    if (oneData.status != 0) {
                        str += "时间:" + oneData.hour + ":" + oneData.minute + " 状态" + oneData.status + "\n"
                    }

                }

                bleResponseLabel.value = str
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun getGoalsDayAndNightHistory(time: Long) {
        initData()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        StarmaxBleClient.instance.getGoalsDayAndNightHistory(calendar).subscribe({ it ->
            if (it.status == 0) {
                var str = ("日期:" + it.year + "-" + it.month + "-" + it.day + "\n\n")

                str += "总步数：${it.totalSteps},目标:${it.totalStepGoals}\n\n"
                str += "朝朝：\n"
                it.dayGoals.let { n ->
                    str += "步数：${n.steps},目标:${n.stepGoals}\n"
                    str += "时间:${n.startHour}:${n.startMinute}-${n.endHour}:${n.endMinute}\n"
                    str += "完成状态:${n.status}\n\n"
                }
                str += "暮暮：\n"
                it.nightGoals.let { m ->
                    str += "步数：${m.steps},目标:${m.stepGoals}\n"
                    str += "时间:${m.startHour}:${m.startMinute}-${m.endHour}:${m.endMinute}\n"
                    str += "完成状态:${m.status}\n\n"
                }

                bleResponseLabel.value = str
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun getValidHistoryDates() {
        initData()
        getValidHistoryDates(HistoryType.Step)
    }

    fun getCustomHealthGoalsValidHistoryDates() {
        initData()
        getCustomValidHistoryDates(HistoryType.CustomHealthGoals)
    }

    fun getGoalsDayAndNightValidHistoryDates() {
        initData()
        getCustomValidHistoryDates(HistoryType.GoalsDayAndNight)
    }

    fun getSupportLanguages() {
        initData()
        StarmaxBleClient.instance.getSupportLanguages().subscribe({
            if (it.status == 0) {
                var str = ""
                val languageList = listOf(
                    "简体中文",
                    "繁体中文",
                    "英文",
                    "俄语",
                    "法语",
                    "西班牙语",
                    "德语",
                    "日语",
                    "意大利语",
                    "韩语",
                    "荷兰语",
                    "泰语",
                    "越南语",
                    "马来语",
                    "印尼语",
                    "葡萄牙语",
                    "罗马尼亚语",
                    "波兰语",
                    "土耳其语",
                    "蒙古语",
                    "印地语",
                    "阿拉伯语"
                )
                val dataList = it.languagesList.map { language ->
                    languageList[language]
                }.joinToString("\n")

                bleResponseLabel.value = dataList
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun getSleepValidHistoryDates() {
        getValidHistoryDates(HistoryType.Sleep)
    }

    fun getMetValidHistoryDates() {
        getValidHistoryDates(HistoryType.Met)
    }

    fun getMaiValidHistoryDates() {
        getValidHistoryDates(HistoryType.Mai)
    }

    fun getBloodSugarValidHistoryDates() {
        getValidHistoryDates(HistoryType.BloodSugar)
    }

    fun getBloodOxygenValidHistoryDates() {
        getValidHistoryDates(HistoryType.BloodOxygen)
    }

    fun getShakeHeadValidHistoryDates() {
        getValidHistoryDates(HistoryType.ShakeHead)
    }

    fun getValidHistoryDates(historyType: HistoryType) {
        initData()
        StarmaxBleClient.instance.getValidHistoryDates(historyType).subscribe({
            if (it.status == 0) {
                var str = "有效日期\n"

                val dataList = it.dataList
                for (i in 0 until dataList.size) {
                    val oneData = dataList[i]
                    val year = oneData.year
                    val month = oneData.month
                    val day = oneData.day
                    str += "$year-$month-$day\n"
                }

                bleResponseLabel.value = str
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun getCustomValidHistoryDates(historyType: HistoryType) {
        initData()
        StarmaxBleClient.instance.getCustomValidHistoryDates(historyType).subscribe({
            if (it.status == 0) {
                var str = "有效日期\n"

                val dataList = it.dataList
                for (i in 0 until dataList.size) {
                    val oneData = dataList[i]
                    val year = oneData.year
                    val month = oneData.month
                    val day = oneData.day
                    str += "$year-$month-$day\n"
                }

                bleResponseLabel.value = str
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun sendUi() {
        initData()
        object : Thread() {
            override fun run() {
                UiRepository.getVersion(
                    model = bleModel,
                    version = bleUiVersion,
                    onSuccess = { ui, _ ->
                        if (ui == null) {
                            return@getVersion
                        }

                        val file = File(savePath)
                        if (!file.exists()) file.mkdirs()
                        val url = ui.binUrl
                        val saveName = url.substring(url.lastIndexOf('/') + 1, url.length)

                        val apkFile = File(savePath + saveName)
                        if (apkFile.exists()) apkFile.delete()
                        object : Thread() {
                            override fun run() {
                                try {
                                    NetFileUtils.downloadUpdateFile(url, apkFile) {
                                        changeMtu {
                                            try {
                                                val fis = FileInputStream(apkFile)
                                                BleFileSender.initFile(
                                                    fis,
                                                    object :
                                                        BleFileSenderListener() {
                                                        override fun onSuccess() {}
                                                        override fun onTotalSuccess() {}

                                                        override fun onProgress(progress: Double) {
                                                            bleMessage.value =
                                                                "当前进度${progress}%"
                                                        }

                                                        override fun onFailure(status: Int) {
                                                            bleMessage.value = "安装失败"
                                                        }

                                                        override fun onStart() {
                                                            val data = StarmaxSend()
                                                                .sendUi(offset = 0, ui.version)
                                                            sendMsg(data)
                                                        }

                                                        override fun onCheckSum() {

                                                        }

                                                        override fun onSendComplete() {

                                                        }

                                                        override fun onSend() {
                                                            if (BleFileSender.hasNext()) {
                                                                val data = StarmaxSend().sendFile()
                                                                sendMsg(data)
                                                            }
                                                        }
                                                    })

                                                BleFileSender.sliceBuffer = 8

                                                BleFileSender.onStart()
                                            } catch (e: FileNotFoundException) {
                                                bleMessage.value = "文件未找到"
                                                e.printStackTrace()
                                            }
                                        }
                                    }
                                } catch (e: java.lang.Exception) {
                                    bleMessage.value = "服务器错误"
                                    e.printStackTrace()
                                }
                            }
                        }.start()
                    },
                    onError = { e ->
                        bleMessage.value = "服务器错误"
                        e?.printStackTrace()
                    })
            }
        }.start()

    }

    fun sendUiDiff() {
        initData()
        if (!uiSupportDifferentialUpgrade) {
            bleMessage.value = "当前设备不支持UI差分升级"
            return
        }
        object : Thread() {
            override fun run() {
                UiRepository.getDiff(
                    model = bleModel,
                    version = bleUiVersion,
                    onSuccess = { ui, _ ->
                        if (ui == null) {
                            return@getDiff
                        }

                        val file = File(savePath)
                        if (!file.exists()) file.mkdirs()
                        val url = ui.binUrl
                        val saveName = url.substring(url.lastIndexOf('/') + 1, url.length)

                        val apkFile = File(savePath + saveName)
                        if (apkFile.exists()) apkFile.delete()
                        object : Thread() {
                            override fun run() {
                                try {
                                    NetFileUtils.downloadUpdateFile(url, apkFile) {
                                        changeMtu {
                                            try {
                                                val fis = FileInputStream(apkFile)

                                                BleFileSender.initFile(
                                                    fis,
                                                    object :
                                                        BleFileSenderListener() {
                                                        override fun onSuccess() {}
                                                        override fun onTotalSuccess() {}

                                                        override fun onProgress(progress: Double) {
                                                            bleMessage.value =
                                                                "当前进度${progress}%"
                                                        }

                                                        override fun onFailure(status: Int) {}

                                                        override fun onStart() {
                                                            val data = StarmaxSend()
                                                                .sendUi(
                                                                    offset = ui.offset,
                                                                    ui.version
                                                                )
                                                            sendMsg(data)
                                                        }

                                                        override fun onCheckSum() {

                                                        }

                                                        override fun onSendComplete() {

                                                        }

                                                        override fun onSend() {
                                                            if (BleFileSender.hasNext()) {
                                                                val data = StarmaxSend().sendFile()
                                                                sendMsg(data)
                                                            }
                                                        }
                                                    })

                                                BleFileSender.sliceBuffer = 8

                                                BleFileSender.onStart()
                                            } catch (e: FileNotFoundException) {
                                                bleMessage.value = "文件未找到"
                                                e.printStackTrace()
                                            }
                                        }
                                    }
                                } catch (e: java.lang.Exception) {
                                    bleMessage.value = "服务器错误"
                                    e.printStackTrace()
                                }
                            }
                        }.start()
                    },
                    onError = { e ->
                        bleMessage.value = "服务器错误"
                        e?.printStackTrace()
                    })
            }
        }.start()

    }

    fun sendUiLocal(context: Context, uri: Uri) {
        initData()
        try {
            val fis = context.contentResolver.openInputStream(uri)

            BleFileSender.initFile(
                fis,
                object :
                    BleFileSenderListener() {
                    override fun onSuccess() {}
                    override fun onTotalSuccess() {}

                    override fun onProgress(progress: Double) {
                        bleMessage.value = "当前进度${progress}%"
                    }

                    override fun onCheckSum() {

                    }

                    override fun onSendComplete() {

                    }

                    override fun onFailure(status: Int) {}

                    override fun onStart() {
                        val data = StarmaxSend().sendUi(offset = 0, "1.0.0")
                        sendMsg(data)
                    }

                    override fun onSend() {
                        if (BleFileSender.hasNext()) {
                            println()
                            val data = StarmaxSend().sendFile()
                            sendMsg(data)
                        }
                    }
                })

            BleFileSender.sliceBuffer = 8

            BleFileSender.onStart()
        } catch (e: FileNotFoundException) {
            bleMessage.value = "未找到文件"
            e.printStackTrace()
        }
    }

    fun sendDialV2Local(context: Context, uri: Uri) {
        initData()
        val fis = context.contentResolver.openInputStream(uri)
        if (fis == null) {
            return
        }
        val file = File(savePath)
        if (!file.exists()) file.mkdirs()
        val saveName = savePath + "dialv2.zip"

        val apkFile = File(saveName)
        if (apkFile.exists()) apkFile.delete()

        NetFileUtils.copyUpdateFile(fis, File(saveName)) {

            val destinationDir = File(savePath + "/zip/")
            val filePaths = unzipAndGetFilePaths(File(saveName), destinationDir).filter {
                (!it.contains("_md5.bin")) && it.contains(".bin")
            }.map {
                val currentFile = File(it)
                val relativePath =
                    currentFile.relativeTo(destinationDir).path.replace(File.separator, "/")
                val md5File =
                    File(currentFile.parent, "${currentFile.nameWithoutExtension}_md5.bin")
                println("${currentFile.nameWithoutExtension}_md5.bin")
                val md5 = md5File.takeIf { that -> that.exists() }?.readText()?.trim() ?: ""

                mapOf(
                    "relative_path" to relativePath,
                    "md5" to md5,
                    "current_file" to currentFile
                )
            }
            filePaths.forEach {
                Log.e("BleViewModel", "relative_path:${it["relative_path"]},md5:${it["md5"]}")
                if ((it["relative_path"] as String).contains("DialLayout.bin")) {
                    val currentFile = it["current_file"] as File
                    val bytes = FileUtils.inputStream2Bytes(FileInputStream(currentFile))
                    if (bytes != null) {
                        val jsonStr = bytes.toString(Charsets.UTF_8)
                        val jsonObj = JSONObject(jsonStr)
                        val info = jsonObj["Info"] as JSONObject
                        val head = info["Head"] as JSONObject
                        head.put("WatchPID", 25001)

                        FileOutputStream(currentFile).use { fos ->
                            fos.write(jsonObj.toString().toByteArray(Charsets.UTF_8))
                            fos.flush()
                        }
                    }

                }
            }

            changeMtu {
                try {

                    if (filePaths.isNotEmpty()) {
                        sendFileV2(0, filePaths)
                    }


                } catch (e: FileNotFoundException) {
                    bleMessage.value = "服务器错误"
                    e.printStackTrace()
                }
            }
        }
    }

    fun sendCustomDialV2Local(context: Context) {
        initData()
        lcdWidth = 410
        lcdHeight = 502
        Log.d("imageUri", imageUri.toString());
        Log.d("binUri", binUri.toString());

        if (binUri == null) {
            return
        }

        if (imageUri == null) {
            return
        }


        val bin = context.contentResolver.openInputStream(binUri!!) as FileInputStream?
        val img =
            context.contentResolver.openInputStream(imageUri!!) as FileInputStream? ?: return

        if (bin == null) {
            return
        }

        val file = File(savePath)
        if (!file.exists()) file.mkdirs()
        val saveName = savePath + "dialv2.zip"

        val apkFile = File(saveName)
        if (apkFile.exists()) apkFile.delete()

        NetFileUtils.copyUpdateFile(bin, File(saveName)) {//TODO
            val destinationDir = File(savePath + "/zip/")

            val filePathsList = unzipAndGetFilePaths(File(saveName), destinationDir).filter {
                (!it.contains("_md5.bin")) && it.contains(".bin")
            }.toMutableList()

            val filePaths = filePathsList.map {
                val currentFile = File(it)
                val relativePath =
                    currentFile.relativeTo(destinationDir).path.replace(File.separator, "/")
                val md5File =
                    File(currentFile.parent, "${currentFile.nameWithoutExtension}_md5.bin")
                println("${currentFile.nameWithoutExtension}_md5.bin")
                val md5 = md5File.takeIf { that -> that.exists() }?.readText()?.trim() ?: ""

                mapOf(
                    "relative_path" to relativePath,
                    "md5" to md5,
                    "current_file" to currentFile
                )
            }

            val canChangeColorFiles = mutableMapOf<String, String>()
            filePaths.forEach {
                Log.e("BleViewModel", "relative_path:${it["relative_path"]},md5:${it["md5"]}")
                if ((it["relative_path"] as String).contains("DialLayout.bin")) {
                    val currentFile = it["current_file"] as File
                    val bytes = FileUtils.inputStream2Bytes(FileInputStream(currentFile))
                    if (bytes != null) {
                        val jsonStr = bytes.toString(Charsets.UTF_8)
                        val jsonObj = JSONObject(jsonStr)
                        val info = jsonObj["Info"] as JSONObject
                        val head = info["Head"] as JSONObject
                        val watchHeight = head.get("WatchHeight") as Int

                        head.put("WatchPID", 5001)
                        val elements = (info["Elements"] as JSONObject)["Elements"] as JSONArray
                        for (i in 0 until elements.length()) {
                            val element = elements.get(i) as JSONObject
                            if (intArrayOf(4, 7).contains(element["ElemType"] as Int)) {
                                val numInfo = element["NumInfo"] as JSONObject
                                numInfo.put("Y", numInfo.get("Y") as Int + (watchHeight / 5))
                                val startId = numInfo.get("DigitalStartId") as Int
                                if (startId > 0) {
                                    val endId = numInfo.get("DigitalEndId") as Int
                                    for (h in (startId - 40000)..(endId - 40000)) {
                                        val currentResName =
                                            currentFile.parent + File.separator + String.format(
                                                "%04d.bin",
                                                h
                                            )
                                        canChangeColorFiles[currentResName] = currentResName
                                    }
                                }
                            } else if (intArrayOf(12).contains(element["ElemType"] as Int)) {
                                val imageInfo = element["ImgInfo"] as JSONObject
                                imageInfo.put("Y", imageInfo.get("Y") as Int + (watchHeight / 5))
                                val startId = imageInfo.get("ImgStartId") as Int
                                if (startId > 0) {
                                    val endId = imageInfo.get("ImgEndId") as Int
                                    for (h in (startId - 40000)..(endId - 40000)) {
                                        val currentResName =
                                            currentFile.parent + File.separator + String.format(
                                                "%04d.bin",
                                                h
                                            )
                                        canChangeColorFiles[currentResName] = currentResName
                                    }
                                }
                            }
                        }

                        FileOutputStream(currentFile).use { fos ->
                            fos.write(jsonObj.toString().toByteArray(Charsets.UTF_8))
                            fos.flush()
                        }
                    }
                }

                if ((it["relative_path"] as String).contains("ezip/0000.bin")) {

                    val currentFile = it["current_file"] as File

                    var srcBitmap = BitmapFactory.decodeStream(img)

                    srcBitmap = com.starmax.sdkdemo.utils.BmpUtils.convertSize(
                        srcBitmap,
                        lcdWidth,
                        lcdHeight
                    )

                    val outputStream = ByteArrayOutputStream()

                    // 3. 将 Bitmap 写入 PNG 格式
                    srcBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)

                    Log.d("pngData", "${FileValidator.isPngData(outputStream.toByteArray())}")
                    val bmpCopy =
                        sifliEzipUtil.pngToEzip(outputStream.toByteArray(), "rgb565A", 0, 1, 2)
                    Log.d(
                        "pngDataZip",
                        "${
                            sifliEzipUtil.pngToEzip(
                                outputStream.toByteArray(),
                                "rgb565A",
                                0,
                                1,
                                2
                            ).size
                        }"
                    )

                    FileOutputStream(currentFile).use { fos ->
                        fos.write(bmpCopy)
                        fos.flush()
                    }

                    outputStream.flush()
                    outputStream.close()
                    srcBitmap.recycle()
                } else {
//                    val currentFile = it["current_file"] as File
//                    val regex = Regex("/(\\d+)\\.bin")
//                    val matchResult = regex.find(it["relative_path"] as String)
//                    val filePath = "Numbers/${matchResult?.groupValues?.get(1)}.png"
//
//                    if (isAssetFileExists(filePath)) {
//                        val bytes = FileUtils.inputStream2Bytes(FileInputStream(currentFile))
//                        val imgNumber = context.assets.open(filePath)
//
//                        val srcBitmap = BitmapFactory.decodeStream(imgNumber)
//
//                        val bmpBytes = com.starmax.sdkdemo.utils.BmpUtils.convertColor(
//                            srcBitmap,
//                            lcdWidth,
//                            lcdHeight
//                        )
//
//                        val bmpCopy = bmpBytes // 不再需要截取文件头
//
//                        FileOutputStream(currentFile).use { fos ->
//                            val header = bytes!!.sliceArray(0..3)
//                            header[0] = 0x05
//                            fos.write(header + bmpCopy)
//                            fos.flush()
//                        }
//                        srcBitmap.recycle()
//                    }
                }
            }

            println("开始改色")
            filePaths.forEach {
                val currentFile = (it["current_file"] as File)
                if (canChangeColorFiles[currentFile.absolutePath] != null) {
                    println(currentFile.absolutePath)
                    val bytes = FileUtils.inputStream2Bytes(FileInputStream(currentFile))
                    if (bytes != null) {
                        var newBytes = com.starmax.sdkdemo.utils.BmpUtils.argbConvertColor(
                            bytes,
                            0xFF,
                            0x00,
                            0x00
                        )

                        FileOutputStream(currentFile).use { fos ->
                            //val header = bytes!!.sliceArray(0..3)
                            //newBytes[0] = 0x05
                            fos.write(newBytes)
                            fos.flush()
                        }
                    }

                }
            }

            changeMtu {
                try {

                    if (filePaths.isNotEmpty()) {
                        sendFileV2(0, filePaths)
                    }


                } catch (e: FileNotFoundException) {
                    bleMessage.value = "服务器错误"
                    e.printStackTrace()
                }
            }
        }
    }

    fun isAssetFileExists(filePath: String): Boolean {
        initData()
        return try {
            context.assets.open(filePath)
            true
        } catch (e: IOException) {
            false
        }
    }

    fun sendFileV2LocalByDiffMd5(context: Context, uri: Uri) {
        val deviceLocalJsonMap = mutableMapOf<String, String>()
        StarmaxBleClient.instance.getFileHeader(3)?.subscribe({
            if (it !is GetFileV2) {
                sendFileV2Local(context, uri, deviceLocalJsonMap)
                return@subscribe
            }

            if (it.isOpen == 1) {
                var jsonBytes = byteArrayOf()
                fun getFileRefresh(index: Int, len: Int) {
                    if (index > len) {
                        println("开始读取手表本地md5")
                        for (i in 0 until jsonBytes.size step 64) {
                            val localName =
                                jsonBytes.sliceArray(i until i + 32).filter { it != 0x00.toByte() }
                                    .toByteArray()
                                    .toString(charset = Charsets.US_ASCII).trim()

                            val localMd5 =
                                jsonBytes.sliceArray(i + 32 until i + 64)
                                    .toString(charset = Charsets.US_ASCII)
                            println("localName:" + localName + ",localMd5:" + localMd5)
                            deviceLocalJsonMap[localName] = localMd5
                        }
                        sendFileV2Local(context, uri, deviceLocalJsonMap)
                        return
                    }
                    StarmaxBleClient.instance.getFileContent(index)
                        .subscribe({ response ->
                            jsonBytes += response.data.toByteArray()
                            getFileRefresh(index + 1, len)
                        }, {

                        }).let { }
                }

                dataSize = if (it.fileSize % 4096 == 0) {
                    (it.fileSize / 4096)
                } else {
                    (it.fileSize / 4096) + 1
                }
                getFileRefresh(0, dataSize - 1)
            }
        }, {

        }).let { }
    }

    fun sendFileV2Local(context: Context, uri: Uri, deviceLocalJsonMap: Map<String, String>) {
        initData()
        val fis = context.contentResolver.openInputStream(uri)
        if (fis == null) {
            return
        }
        val file = File(savePath)
        if (!file.exists()) file.mkdirs()
        val saveName = savePath + "filev2.zip"

        val apkFile = File(saveName)
        if (apkFile.exists()) apkFile.delete()

        NetFileUtils.copyUpdateFile(fis, File(saveName)) {
            val destinationDir = File(savePath + "/zip/")
            val filePathsList = unzipAndGetFilePaths(File(saveName), destinationDir)

            val jsonFilePaths = filePathsList.firstOrNull {
                it.endsWith("all_md5s.json")
            }
            var jsonMap = mutableMapOf<String, String>()
            if (jsonFilePaths != null) {
                val jsonString = File(jsonFilePaths).readText()
                // 使用正则表达式解析 JSON
                val regex = Pattern.compile("\"([^\"]+)\":\\s*\"([^\"]+)\"")
                val matcher = regex.matcher(jsonString)

                // 遍历匹配结果并填充 Map
                while (matcher.find()) {
                    val key = matcher.group(1)
                    val value = matcher.group(2)
                    if (value != null) {
                        if (key != null) {
                            jsonMap[key.toString()] = value
                        }
                    }
                }
            }

            val filePaths = filePathsList.filter {
                (!it.contains("_md5.bin")) && it.contains(".bin")
                        && (!it.endsWith("ctrl_packet.bin"))
                        && (!it.endsWith("outapp.bin"))
                        && (!it.endsWith("outfont.bin"))
                        && (!it.endsWith("outres.bin"))
                        && (!it.endsWith("outroot.bin"))
            }.sortedBy {
                if (it.endsWith("all_file_md5s.bin")) 1 else 0
            }.map {
                val currentFile = File(it)
                val relativePath =
                    currentFile.relativeTo(destinationDir).path.replace(File.separator, "/")
                val md5Str =
                    if (jsonMap[currentFile.name] != null) jsonMap[currentFile.name] else ""
                val deviceLocalMd5Str =
                    if (deviceLocalJsonMap[currentFile.name] != null) deviceLocalJsonMap[currentFile.name] else ""
                println("网络:${md5Str}")
                println("手表本地:${deviceLocalMd5Str}")
                mapOf(
                    "relative_path" to relativePath,
                    "md5" to md5Str,
                    "device_local_md5" to deviceLocalMd5Str,
                    "current_file" to currentFile
                )
            }.filter {
                !(it["device_local_md5"] == it["md5"] && it["device_local_md5"] != "")
            }




            try {
                if (filePaths.isNotEmpty()) {
                    sendFileV2(0, filePaths)
                }
            } catch (e: FileNotFoundException) {
                bleMessage.value = "服务器错误"
                e.printStackTrace()
            }
        }


    }

    fun sendFileV2(index: Int, filePaths: List<Map<String, Any?>>) {
        initData()

        val currentInfo = filePaths[index]
        try {
            val currentFile = currentInfo["current_file"] as File
            val bin = FileInputStream(currentFile)
            BleFileSender.initFile(
                bin,
                object : BleFileSenderListener() {
                    override fun onSuccess() {
                        if (index < filePaths.size - 1) {
                            sendFileV2(index + 1, filePaths)
                        } else {
                            val data = StarmaxSend()
                                .sendTotalFileComplete()
                            //p(data)
                            sendMsg(data)
                        }
                    }

                    override fun onTotalSuccess() {

                    }

                    override fun onProgress(progress: Double) {
                        bleMessage.value =
                            "文件:${currentInfo["relative_path"]},当前进度:${progress.toInt()}%,md5:${currentInfo["md5"] as String}"
                    }

                    override fun onFailure(status: Int) {
                        bleMessage.value = "发送失败"
                    }

                    override fun onCheckSum() {

                    }

                    override fun onSendComplete() {
                        val data = StarmaxSend()
                            .sendSingleFileComplete()
                        //p(data)
                        sendMsg(data)

                    }

                    override fun onStart() {
                        val data = StarmaxSend()
                            .sendFileV2Header(
                                currentInfo["md5"] as String,
                                filePaths.size,
                                index,
                                BleFileSender.allFileData.size,
                                currentInfo["relative_path"] as String
                            )
                        //p(data)
                        sendMsg(data)
                    }

                    override fun onSend() {
                        if (BleFileSender.hasNext()) {
                            val data = StarmaxSend().sendFileV2Content()

                            sendMsg(data)
                        }
                    }
                })

            BleFileSender.sliceBuffer = 8

            BleFileSender.onStart()
        } catch (e: FileNotFoundException) {
            bleMessage.value = "服务器错误"
            e.printStackTrace()
        }
    }

    fun sendDialLocal(context: Context) {
        initData()
        changeMtu {
            try {
                val bin = context.contentResolver.openInputStream(binUri!!) as FileInputStream?
                var lastSendCalendar = Calendar.getInstance()
                BleFileSender.initFile(
                    bin,
                    object : BleFileSenderListener() {
                        override fun onSuccess() {}
                        override fun onTotalSuccess() {}

                        override fun onProgress(progress: Double) {
                            bleMessage.value = "当前进度${progress.toInt()}%"
                        }

                        override fun onFailure(status: Int) {}

                        override fun onCheckSum() {

                        }

                        override fun onSendComplete() {

                        }

                        override fun onStart() {
                            val data = StarmaxSend()
                                .sendDial(
                                    5001,
                                    BmpUtils.bmp24to16(255, 255, 255),
                                    1
                                )
                            //p(data)
                            sendMsg(data)
                        }

                        override fun onSend() {
                            if (BleFileSender.hasNext()) {
                                val data = StarmaxSend().sendFile()

                                sendMsg(data)
                            }
                        }
                    })

                BleFileSender.sliceBuffer = 8

                BleFileSender.onStart()
            } catch (e: FileNotFoundException) {
                bleMessage.value = "服务器错误"
                e.printStackTrace()
            }
        }
    }

    fun unittest() {
        initData()
        object : Thread() {
            override fun run() {

                val client = OkHttpClient();
                val request = Request.Builder()
                    .url("https://www.runmefit.cn/api/firmware/unit_test")
                    .build();

                try {
                    val response = client.newCall(request).execute();
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string();
                        println(responseBody)
                        if (responseBody != null) {
                            val array = JSONArray(responseBody)
                            for (i in 0 until array.length()) {
                                val obj = array.get(i) as JSONObject
                                val name = obj.get("name") as String
                                val value = obj.get("value") as Int
                                val status = obj.get("status") as Int
                                StarmaxBleClient.instance.deviceUnitTest(name, value, status)
                                    .subscribe({
                                        bleResponseLabel.value =
                                            "单元测试:${name},值:${value},状态${status}"
                                    }, {}).let {
                                        sendDisposable.add(it)
                                    }
                            }
                        }
                    } else {
                        // 处理错误情况
                    }
                } catch (e: IOException) {
                    e.printStackTrace();
                }
            }
        }.start()

    }

    fun sendLogoLocal(context: Context) {
        initData()
        changeMtu {
            try {
                val bin = context.contentResolver.openInputStream(binUri!!) as FileInputStream?
                BleFileSender.initFile(
                    bin,
                    object : BleFileSenderListener() {
                        override fun onSuccess() {}
                        override fun onTotalSuccess() {}

                        override fun onProgress(progress: Double) {
                            bleMessage.value = "当前进度${progress.toInt()}%"
                        }

                        override fun onFailure(status: Int) {}

                        override fun onCheckSum() {

                        }

                        override fun onSendComplete() {

                        }

                        override fun onStart() {
                            val data = StarmaxSend()
                                .sendLogo()
                            sendMsg(data)
                        }

                        override fun onSend() {
                            if (BleFileSender.hasNext()) {
                                val data = StarmaxSend().sendFile()

                                sendMsg(data)
                            }
                        }
                    })

                BleFileSender.sliceBuffer = 8

                BleFileSender.onStart()
            } catch (e: FileNotFoundException) {
                bleMessage.value = "服务器错误"
                e.printStackTrace()
            }
        }
    }

    fun sendMp3Local(context: Context, uri: Uri, otherInfo: Int) {
        initData()
        changeMtu {
            try {
                val bin = context.contentResolver.openInputStream(uri) as FileInputStream?
                BleFileSender.initFile(
                    bin,
                    object : BleFileSenderListener() {
                        override fun onSuccess() {}
                        override fun onTotalSuccess() {}

                        override fun onProgress(progress: Double) {
                            bleMessage.value = "当前进度${progress.toInt()}%"
                        }

                        override fun onFailure(status: Int) {}

                        override fun onCheckSum() {

                        }

                        override fun onSendComplete() {

                        }

                        override fun onStart() {
                            val data = StarmaxSend().sendMp3WithEventReminder(otherInfo)
                            sendMsg(data)
                        }

                        override fun onSend() {
                            if (BleFileSender.hasNext()) {
                                val data = StarmaxSend().sendFile()

                                sendMsg(data)
                            }
                        }
                    })

                BleFileSender.sliceBuffer = 8

                BleFileSender.onStart()
            } catch (e: FileNotFoundException) {
                bleMessage.value = "服务器错误"
                e.printStackTrace()
            }
        }
    }

    fun sendPgl(inChina: Boolean) {
        initData()
        val file = File(savePath)
        if (!file.exists()) file.mkdirs()
        val url =
            if (inChina) "https://starcourse.rx-networks.cn/Ixby68SvY2/f1e1G7C7E7J7.pgl" else "https://starcourse.location.io/Ixby68SvY2/f1e1G7C7E7J7.pgl"
        val saveName = url.substring(url.lastIndexOf('/') + 1, url.length)

        val apkFile = File(savePath + saveName)
        if (apkFile.exists()) apkFile.delete()
        object : Thread() {
            override fun run() {
                try {
                    NetFileUtils.downloadUpdateFile(url, apkFile) {
                        changeMtu {
                            try {
                                val fis = FileInputStream(apkFile)

                                BleFileSender.initFile(
                                    fis,
                                    object :
                                        BleFileSenderListener() {
                                        override fun onSuccess() {}
                                        override fun onTotalSuccess() {}

                                        override fun onProgress(progress: Double) {
                                            bleMessage.value =
                                                "当前进度${progress}%"
                                        }

                                        override fun onFailure(status: Int) {}

                                        override fun onStart() {
                                            val data = StarmaxSend().sendPgl()
                                            sendMsg(data)
                                        }

                                        override fun onCheckSum() {

                                        }

                                        override fun onSendComplete() {

                                        }

                                        override fun onSend() {
                                            if (BleFileSender.hasNext()) {
                                                val data = StarmaxSend().sendFile()
                                                sendMsg(data)
                                            }
                                        }
                                    })

                                BleFileSender.sliceBuffer = 8

                                BleFileSender.onStart()
                            } catch (e: FileNotFoundException) {
                                bleMessage.value = "文件未找到"
                                e.printStackTrace()
                            }
                        }
                    }
                } catch (e: java.lang.Exception) {
                    bleMessage.value = "服务器错误"
                    e.printStackTrace()
                }
            }
        }.start()
    }

    fun clearLogo() {
        initData()
        StarmaxBleClient.instance.clearLogo().subscribe({
            if (it.status == 0) {
                bleResponseLabel.value = "清除logo成功"
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun unzipAndGetFilePaths(zipFile: File, destinationDir: File): List<String> {
        initData()
        val filePathsList = mutableListOf<String>()
        val buffer = ByteArray(1024)
        val zipInputStream = ZipInputStream(FileInputStream(zipFile))

        if (!destinationDir.exists()) {
            destinationDir.mkdirs()
        } else {
            destinationDir.deleteRecursively()
            destinationDir.mkdirs()
        }

        var zipEntry = zipInputStream.nextEntry
        while (zipEntry != null) {
            val newFile = File(destinationDir, zipEntry.name)
            filePathsList.add(newFile.absolutePath) // 添加文件路径到列表中
            if (zipEntry.isDirectory) {
                newFile.mkdirs()
            } else {
                newFile.parentFile?.mkdirs()
                val fileOutputStream = FileOutputStream(newFile)
                var len: Int
                while (zipInputStream.read(buffer).also { len = it } > 0) {
                    fileOutputStream.write(buffer, 0, len)
                }
                fileOutputStream.close()
            }
            zipEntry = zipInputStream.nextEntry
        }
        zipInputStream.closeEntry()
        zipInputStream.close()

        return filePathsList
    }

    fun sendGts7FirmwareLocal(context: Context) {
        initData()
        if (binUri == null) {
            return
        }

        changeMtu {
            try {
                val bin = context.contentResolver.openInputStream(binUri!!) as FileInputStream?
                val lastSendCalendar = Calendar.getInstance()
                BleFileSender.initFile(
                    bin,
                    object : BleFileSenderListener() {
                        override fun onSuccess() {
                            val currentCalendar = Calendar.getInstance()
                            // 计算时间差异（以秒为单位）
                            // 计算时间差异（以秒为单位）
                            val diffInSeconds: Long =
                                (currentCalendar.getTimeInMillis() - lastSendCalendar.getTimeInMillis()) / 1000
                            bleMessage.value = "发送完成,耗时${diffInSeconds}"
                        }

                        override fun onTotalSuccess() {
                        }

                        override fun onProgress(progress: Double) {
                            val currentCalendar = Calendar.getInstance()
                            // 计算时间差异（以秒为单位）
                            // 计算时间差异（以秒为单位）
                            val diffInSeconds: Long =
                                (currentCalendar.getTimeInMillis() - lastSendCalendar.getTimeInMillis()) / 1000

                            bleMessage.value = "当前进度${progress.toInt()}%,耗时${diffInSeconds}"

                        }

                        override fun onFailure(status: Int) {
                            bleMessage.value = "发送失败"
                        }

                        override fun onCheckSum() {
                            val data = StarmaxSend().sendDiffCheckSum()
                            Log.d("Diff Sender", "${BleFileSender.checksumData.size}")
                            bleMessage.value =
                                "正在发送第${BleFileSender.checksumSendIndex}包校验码"
                            sendMsg(data)
                        }

                        override fun onStart() {
                            val data = StarmaxSend().sendDiffHeader()
                            bleMessage.value = "发送文件头"
                            sendMsg(data)
                        }

                        override fun onSendComplete() {
                            val data = StarmaxSend().sendDiffComplete()
                            bleMessage.value = "发送结束通知固件"
                            sendMsg(data)
                        }

                        override fun onSend() {
                            val data = StarmaxSend().sendDiffFile()
                            //bleMessage.value = "正在发送，当前偏移"+BleFileSender.checksumIndex+"/"+BleFileSender.checksumInfo.size
                            sendMsg(data)
                        }
                    })

                BleFileSender.sliceBuffer = 8

                BleFileSender.onStart()
            } catch (e: FileNotFoundException) {
                bleMessage.value = "服务器错误"
                e.printStackTrace()
            }
        }
    }

    fun sendGts7CrcLocal(context: Context) {
        initData()
        if (binUri == null) {
            return
        }

        try {
            val bin = context.contentResolver.openInputStream(binUri!!) as FileInputStream?
            val lastSendCalendar = Calendar.getInstance()
            BleFileSender.initFile(
                bin,
                object : BleFileSenderListener() {
                    override fun onSuccess() {
                        val currentCalendar = Calendar.getInstance()
                        // 计算时间差异（以秒为单位）
                        // 计算时间差异（以秒为单位）
                        val diffInSeconds: Long =
                            (currentCalendar.getTimeInMillis() - lastSendCalendar.getTimeInMillis()) / 1000
                        bleMessage.value = "发送完成,耗时${diffInSeconds}"
                    }

                    override fun onTotalSuccess() {
                    }

                    override fun onProgress(progress: Double) {
                        val currentCalendar = Calendar.getInstance()
                        // 计算时间差异（以秒为单位）
                        // 计算时间差异（以秒为单位）
                        val diffInSeconds: Long =
                            (currentCalendar.getTimeInMillis() - lastSendCalendar.getTimeInMillis()) / 1000

                        bleMessage.value = "当前进度${progress.toInt()}%,耗时${diffInSeconds}"

                    }

                    override fun onFailure(status: Int) {
                        bleMessage.value = "发送失败"
                    }

                    override fun onCheckSum() {
                        val data = StarmaxSend().sendDiffCheckSum()
                        Log.d("Diff Sender", "正在发送第${BleFileSender.checksumSendIndex}包")
                        Log.d("Diff Sender", "checksum 大小:${BleFileSender.checksumData.size}")
                        bleMessage.value = "正在发送第${BleFileSender.checksumSendIndex}包校验码"
                        Log.d("Diff Sender", "data 大小:${data.size}")
                        Utils.p(data)
                        StarmaxBleClient.instance.notify(
                            StarmaxSendRequest(
                                0xF3,
                                intArrayOf(0x00, 0x01)
                            ).datas
                        )
                    }

                    override fun onStart() {
                        val data = StarmaxSend().sendDiffHeader()
                        bleMessage.value = "发送文件头"
                        Utils.p(data)

                        StarmaxBleClient.instance.notify(
                            StarmaxSendRequest(
                                0xF3,
                                intArrayOf(0x00, 0x00)
                            ).datas
                        )
                    }

                    override fun onSendComplete() {
                        val data = StarmaxSend().sendDiffComplete()
                        bleMessage.value = "发送结束通知固件"

                        Utils.p(data)
                    }

                    override fun onSend() {
                        val data = StarmaxSend().sendDiffFile()
                        Utils.p(data)
                    }
                })

            BleFileSender.sliceBuffer = 8

            BleFileSender.onStart()
        } catch (e: FileNotFoundException) {
            bleMessage.value = "服务器错误"
            e.printStackTrace()
        }
    }

    fun sendCustomDial(context: Context) {
        initData()
        if (binUri == null) {
            return
        }

        if (imageUri == null) {
            return
        }

        changeMtu {
            try {
                val bin = context.contentResolver.openInputStream(binUri!!) as FileInputStream?
                val img =
                    context.contentResolver.openInputStream(imageUri!!) as FileInputStream?

                var lastSendCalendar = Calendar.getInstance()
                BleFileSender.initFileWithBackground(
                    bin,
                    lcdWidth, lcdHeight,
                    img,
                    object : BleFileSenderListener() {
                        override fun onSuccess() {}
                        override fun onTotalSuccess() {
                        }

                        override fun onProgress(progress: Double) {
                            bleMessage.value = "当前进度${progress.toInt()}%"
                        }

                        override fun onFailure(status: Int) {}
                        override fun onStart() {
                            val data = StarmaxSend()
                                .sendDial(
                                    5001,
                                    BmpUtils.bmp24to16(255, 255, 255),
                                    1
                                )
                            Utils.p(data)
                            sendMsg(data)
                        }

                        override fun onCheckSum() {

                        }

                        override fun onSendComplete() {

                        }

                        override fun onSend() {
                            if (BleFileSender.hasNext()) {
                                val data = StarmaxSend().sendFile()
                                //p(data)
                                BleManager.getInstance().write(
                                    bleDevice?.get(),
                                    WriteServiceUUID.toString(),
                                    WriteCharacteristicUUID.toString(),
                                    data,
                                    object : BleWriteCallback() {
                                        override fun onWriteSuccess(
                                            current: Int,
                                            total: Int,
                                            justWrite: ByteArray?
                                        ) {
                                            if (current == total) {
                                                val newSendCalendar = Calendar.getInstance()
                                                val millis =
                                                    newSendCalendar.timeInMillis - lastSendCalendar.timeInMillis
                                                Log.e(
                                                    "BleFileSender",
                                                    "发送时间:${millis},当前rssi:"
                                                )
                                                lastSendCalendar = Calendar.getInstance()
                                            }
                                        }

                                        override fun onWriteFailure(exception: BleException?) {
                                            //bleMessage.value = "指令发送失败"
                                        }
                                    })

                            }
                        }
                    })

                BleFileSender.sliceBuffer = 8

                BleFileSender.onStart()
            } catch (e: FileNotFoundException) {
                bleMessage.value = "服务器错误"
                e.printStackTrace()
            }
        }
    }

    fun getDialInfo() {
        initData()
        StarmaxBleClient.instance.getDialInfo().subscribe({
            var str = ""

            val dataList = it.infosList
            for (i in 0 until dataList.size) {
                val oneData = dataList[i]
                val isSelected = oneData.isSelected
                val dialId = oneData.dialId
                val dialColor = oneData.dialColor
                val align = oneData.align
                if (isSelected == 1) {
                    str += "已选择\n"
                }
                str += "表盘id:${dialId}\n"
                str += "表盘颜色:${
                    Utils.bytesToHex(
                        Utils.int2byte(
                            dialColor,
                            3
                        )
                    )
                }\n"
                str += "位置:${align}\n"
            }

            bleResponseLabel.value = str
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun switchDial() {
        initData()
        StarmaxBleClient.instance.switchDial(5001).subscribe({
            if (it.status == 0) {
                bleResponseLabel.value = "切换表盘成功"
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun unpair() {
        initData()
        StarmaxBleClient.instance.unpair(0).subscribe(
            { bleResponseLabel.value = "解绑成功" },
            {}
        ).also { sendDisposable.add(it) }
    }

    fun unpairCheck() {
        initData()
        StarmaxBleClient.instance.unpair(1).subscribe({
            bleResponseLabel.value = "解绑成功"
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun reset() {
        initData()
        StarmaxBleClient.instance.reset().subscribe({
            bleResponseLabel.value = "恢复出厂成功"
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun close() {
        initData()
        StarmaxBleClient.instance.close().subscribe({
            bleResponseLabel.value = "关机成功"
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun shippingMode() {
        initData()
        StarmaxBleClient.instance.shippingMode().subscribe({
            bleResponseLabel.value = "进入船运模式"
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun getNfcCardInfo() {
        initData()
        StarmaxBleClient.instance.getNfcInfo().subscribe({
            if (it.status == 0) {
                var str = ("类型:" + it.type)

                val cardsList = it.cardsList
                for (i in 0 until cardsList.size) {
                    val oneData = cardsList[i]
                    str += "卡片类型:" + oneData.cardType + ",卡片名称" + oneData.cardName + "%\n"
                }

                bleResponseLabel.value = str
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    /**
     * @param data
     */
    fun sendMsg(data: ByteArray?) {
        if (bleDevice == null || bleDevice!!.get() == null || !BleManager.getInstance()
                .isConnected(bleDevice!!.get())
        ) {
            sendDisposable.clear() //清空发送栈
            viewModelScope.launch {
                Toast.makeText(context, "蓝牙未连接", Toast.LENGTH_SHORT).show()
            }
            return
        }
//          Utils.p(data!!)
//        Utils.p(data!!)

        BleManager.getInstance().write(
            bleDevice?.get(),
            WriteServiceUUID.toString(),
            WriteCharacteristicUUID.toString(),
            data,
            object : BleWriteCallback() {
                override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                    //bleMessage.value = "指令发送成功"
                    //println("当前 $current 总共 $total 已写 $justWrite")
                }

                override fun onWriteFailure(exception: BleException?) {
                    //bleMessage.value = "指令发送失败"
                }
            })
    }

    fun disconnect() {
        BleManager.getInstance().disconnectAllDevice()
    }


    fun changeMtu(onMtuChanged: () -> Unit) {
        BleManager.getInstance().setMtu(bleDevice?.get(), 512, object : BleMtuChangedCallback() {
            override fun onSetMTUFailure(exception: BleException) {
                // 设置MTU失败
                Log.e("BleViewModel", exception.description)
            }

            override fun onMtuChanged(mtu: Int) {

                BleManager.getInstance().setSplitWriteNum(min(mtu - 3, 512))
                Log.e("BleViewModel", "设置mtu${mtu}成功")
                onMtuChanged()
            }
        })
    }

    private fun statusLabel(status: Int): String {
        return when (status) {
            0 -> "命令正确"
            1 -> "命令码错误"
            2 -> "校验码错误"
            3 -> "数据长度错误"
            4 -> "数据无效"
            else -> "数据无效"
        };
    }

    private fun sportModeLabel(mode: Int): String {
        return when (mode) {
            0X00 -> "室内跑步"
            0X01 -> "户外跑步"
            0X03 -> "户外骑行"
            0X04 -> "健走"
            0X05 -> "跳绳"
            0X06 -> "足球"
            0X07 -> "羽毛球"
            0X09 -> "篮球"
            0X0A -> "椭圆机"
            0X0B -> "徒步"
            0X0C -> "瑜伽"
            0X0D -> "力量训练"
            0X0E -> "爬山"
            0X0F -> "自由运动"
            0X10 -> "户外步行"
            0X12 -> "室内单车"
            else -> "数据无效"
        };
    }

    class BluetoothListenerReceiver(val bleViewModel: BleViewModel) : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0)) {
                        BluetoothAdapter.STATE_TURNING_ON -> Log.e(
                            "BleReceiver",
                            "onReceive---------蓝牙正在打开中"
                        )

                        BluetoothAdapter.STATE_ON -> {
                            Log.e("BleReceiver", "onReceive---------蓝牙已经打开")
//                            Handler(Looper.getMainLooper()).postDelayed({
//                                BleManager.getInstance().connect(
//                                    bleViewModel.bleDevice?.get()?.mac,
//                                    bleViewModel.bleGattCallback
//                                )
//                            }, 1000)

                        }

                        BluetoothAdapter.STATE_TURNING_OFF -> {
                            Log.e(
                                "BleReceiver",
                                "onReceive---------蓝牙正在关闭中"
                            )
                        }

                        BluetoothAdapter.STATE_OFF -> {
                            Log.e("BleReceiver", "onReceive---------蓝牙已经关闭")
                            bleViewModel.bleState = BleState.DISCONNECTED
                            BleManager.getInstance().destroy()
                        }
                    }
                }
            }
        }
    }

    private fun makeFilter(): IntentFilter {
        val filter = IntentFilter()
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        return filter
    }

    fun bindDevice(): MutableMap<String, Any> {
        val bluetoothDevice = bleDevice!!.get()!!.device

        Log.e("BleViewModel", "绑定设备类型" + bluetoothDevice.type.toString())
        val label = when (bluetoothDevice.type) {
            1 -> "经典蓝牙"
            2 -> "LE蓝牙"
            3 -> "双模蓝牙"
            else -> "未知蓝牙"
        }
        Toast.makeText(context, label, Toast.LENGTH_SHORT).show()
        var result = false
        val data: MutableMap<String, Any> = HashMap()
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if ((bluetoothDevice.type == BluetoothDevice.DEVICE_TYPE_DUAL || bluetoothDevice.type == BluetoothDevice.DEVICE_TYPE_CLASSIC) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                result = createBind(bluetoothDevice, BluetoothDevice.TRANSPORT_BREDR)
                Log.e("BleViewModel", "双模蓝牙绑定" + if (result) "成功" else "失败")
            } else if (bluetoothDevice.type == BluetoothDevice.DEVICE_TYPE_LE) {
                result = createBind(bluetoothDevice)
                Log.e("BleViewModel", "经典蓝牙绑定" + if (result) "成功" else "失败")
            }
        }

        data["is_success"] = result
        data["type"] = bluetoothDevice.type

        return data
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun createBind(device: BluetoothDevice?): Boolean {
        var bRet = false
        if (Build.VERSION.SDK_INT >= 20) {
            bRet = device!!.createBond()
        } else {
            val btClass: Class<*> = device!!.javaClass
            try {
                val createBondMethod = btClass.getMethod("createBond")
                val `object` = createBondMethod.invoke(device) as? Boolean ?: return false
                bRet = `object`
            } catch (var6: java.lang.Exception) {
                var6.printStackTrace()
            }
        }

        return bRet
    }

    fun createBind(device: BluetoothDevice?, transport: Int): Boolean {
        if (device == null) return false
        var bRet = false
        try {
            Log.e("BleViewModel", "进入双模蓝牙绑定")
            val bluetoothDeviceClass = device.javaClass
            val createBondMethod =
                bluetoothDeviceClass.getDeclaredMethod("createBond", transport.javaClass)
            createBondMethod.isAccessible = true
            val obj = createBondMethod.invoke(device, transport)
            if (obj !is Boolean) return false
            bRet = obj
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bRet
    }

    fun initData() {
        bleResponseLabel.value = "";
        bleResponse.value = "";
        originData.value = ""
    }


    fun bindService() {
        context.bindService(
            Intent(
                context,
                RxBleService::class.java
            ), serviceConnection, Context.BIND_AUTO_CREATE
        )
    }

    fun unbindService() {
        context.unbindService(serviceConnection)
    }
}