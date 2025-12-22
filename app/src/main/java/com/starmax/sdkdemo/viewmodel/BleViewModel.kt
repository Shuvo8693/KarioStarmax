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
import androidx.compose.ui.text.intl.Locale
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
    var bleHealthResponseLabel = mutableStateOf<Map<String, Any>>(emptyMap())
    var bleBatteryResponseLabel = mutableStateOf("")

    val context: Context by inject()

    private val sendDisposable = CompositeDisposable()
    private val messageDisposable = CompositeDisposable()

    var imageUri: Uri? = null
    var binUri: Uri? = null

    var msgType = 0
    var msgContent = 0

    var packageId = 0

    @SuppressLint("StaticFieldLeak")
    private var bleService: RxBleService? = null

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder: RxBleService.RCBinder = service as RxBleService.RCBinder
            bleService = binder.service
            Log.e("BleViewModel", "-- RxBleService -- Connected")
        }

        override fun onServiceDisconnected(name: ComponentName) {
            bleService = null
            Log.e("BleViewModel", "-- RxBleService -- Disconnected")
        }
    }

    var bleGattCallback: BleGattCallback = object : BleGattCallback() {

        override fun onStartConnect() {
            bleState = BleState.CONNECTTING
            bleStateLiveData.postValue(bleState)
            bleMessage.value = "Bluetooth is connecting"
        }

        override fun onConnectFail(bleDevice: BleDevice?, exception: BleException?) {
            bleState = BleState.DISCONNECTED
            bleStateLiveData.postValue(bleState)
            bleMessage.value = "Bluetooth connection failed: $exception"
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
            bleMessage.value = "Bluetooth connected successfully"

            Log.d("BleViewModel", gatt?.getService(NotifyServiceUUID).toString())

            if (gatt?.getService(NotifyServiceUUID) == null) {
                Handler(Looper.getMainLooper()).postDelayed({
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                    ) {
                        gatt?.discoverServices()
                    }
                }, 1000)
                return
            }

            Handler(Looper.getMainLooper()).postDelayed({
                if (tryOpenNotify.value) {
                    openNotify(bleDevice!!.get())   //<<<< todo:  get bleDevice info on connect success through ghatt
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
            bleMessage.value = "Bluetooth connection disconnected"
            disconnectSubject.onNext(1)

            // When isActiveDisConnected is false, attempt reconnection (2s delay)
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
        }
    }

    init {
        initPath()
        // Bluetooth on/off broadcast listener
        ContextCompat.registerReceiver(
            context,
            BluetoothListenerReceiver(this),
            makeFilter(),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        StarmaxBleClient.instance.setWrite { byteArray -> sendMsg(byteArray) }
    }

    fun setNotify(boolean: Boolean) {
        tryOpenNotify.value = boolean
    }

    fun getDeviceName(): String {
        val name = bleDevice?.get()?.name
        return name ?: ""
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
        savePath = "$basepath/SDKDemo/Device_update/"
        println("Download path: $savePath")
    }

    fun connect(newBleDevice: BleDevice?) {
        bleDevice = SoftReference(newBleDevice)
        if (bleDevice != null) {
            BleManager.getInstance().connect(bleDevice!!.get(), bleGattCallback) // todo: Get ble device info then connect with bleGattCallback
        }
    }

    fun getRssi() {
        BleManager.getInstance().readRssi(bleDevice!!.get(), object : BleRssiCallback() {
            override fun onRssiSuccess(rssi: Int) {
                bleResponseLabel.value = "Signal strength: $rssi"
            }

            override fun onRssiFailure(exception: BleException?) {
                // Not implemented
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
                    bleMessage.value = "Indicate opened successfully"
                    handleOpenSuccess()
                }

                override fun onIndicateFailure(exception: BleException?) {
                    bleMessage.value = "Failed to open indicate: $exception"
                }

                @SuppressLint("MissingPermission", "NewApi")
                override fun onCharacteristicChanged(data: ByteArray) {
                    // Data received from BLE notify
                    StarmaxBleClient.instance.notify(data) //todo <<<===== here byte data is received from Ble notify
                }
            }
        )
    }

    fun openNotify(newBleDevice: BleDevice?) {
        BleManager.getInstance().notify( // ============== open ble notify (data is received from ble notify)
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
                    bleMessage.value = "Failed to open notify: $exception"
                }

                @SuppressLint("MissingPermission", "NewApi")
                override fun onCharacteristicChanged(data: ByteArray) {
                    // Data received from BLE notify
                    println(StarmaxBleClient.instance.notify(data))  //todo ===== here byte data is received from Ble notify
                }
            }
        )
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
                                                        String.format("%02X", minuteByte)
                                                    }
                                            }
                                    })
                            }.joinToString("\n")
                        print(originData.value)
                    } else {
                        originData.value = it.byteArray.map { String.format("%02X", it) }.toString()  //todo === decimal integer to 2 digit hexadecimal integer =====
                        print(originData.value)
                    }
                    //bleResponse.value = it.data.toString()

                    if (it.data is Notify.Reply) {  //TODO Sometimes the saiwei.txt file isn't saved; the goal is to save it every time.
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
                                            "0x%02X", it)
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
            bleResponseLabel.value = "Wearing status: " + it.pairStatus
        }, {

        }).let {
            sendDisposable.add(it)
        }
    }

    fun pairGts10(type: Int) {
        initData()
        StarmaxBleClient.instance.pairGts10(type).subscribe({
            bleResponseLabel.value = "Wearing status: " + it.pairStatus
        }, {

        }).let {
            sendDisposable.add(it)
        }
    }

    fun getBtStatus() {
        initData()
        Utils.p(StarmaxSend().getBtStatus())

        StarmaxBleClient.instance.getBtStatus().subscribe({ bleResponseLabel.value = "BT status: " + it.btStatus }, {

        }).let {
            sendDisposable.add(it)
        }
    }

    fun findDevice(isFind: Boolean) {
        initData()
        StarmaxBleClient.instance.findDevice(isFind = isFind).subscribe({
            bleResponseLabel.value = "Device found successfully"
        }, {

        }).let {
        }
    }

    fun getPower() {
        initData()
        StarmaxBleClient.instance.getPower().subscribe({
            bleBatteryResponseLabel.value = ("Battery level: ${it.power}%\n" + "Charging: ${it.isCharge}")
        }, {

        }).let {
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

            // Purpose: bleModel is required when sending watch faces
            if (labelVisible) {
                bleResponseLabel.value = (
                        "Firmware version: $bleVersion\n" +
                                "UI version: $bleUiVersion\n" +
                                "Device receive buffer size: ${it.bufferSize}\n" +
                                "LCD width: ${it.lcdWidth}\n" +
                                "LCD height: ${it.lcdHeight}\n" +
                                "Screen type: ${it.screenType}\n" +
                                "Device model: $bleModel\n" +
                                "UI force update: ${it.uiForceUpdate}\n" +
                                "Supports differential upgrade: $uiSupportDifferentialUpgrade\n" +
                                "Supports blood sugar: ${it.supportSugar}\n" +
                                "Device protocol version: ${it.protocolVersion}\n" +
                                "App protocol version: ${StarmaxSend().version()}\n" +
                                "Supports new sleep: ${it.sleepVersion}\n" +
                                "Sleep display type: ${it.sleepShowType}\n" +
                                "Supports sleep plan: ${it.supportSleepPlan}\n" +
                                "Supports two-way sport sync: ${it.supportSyncSport}\n" +
                                "Watch face version: ${it.dialVersion}\n" +
                                "Time consumed: ${currentCalendar.timeInMillis - lastMills}"
                        )
            }
        }, {

        }).let {
            sendDisposable.add(it)
        }
    }

    fun setTime() {
        initData()
        StarmaxBleClient.instance.setTime().subscribe({
            if (it.status == 0) {
                bleResponseLabel.value = "Time set successfully"
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {

        }).let {
            sendDisposable.add(it)
        }
    }

    fun setTimeOffset() {
        initData()
        StarmaxBleClient.instance.setTimeOffset().subscribe({
            if (it.status == 0) {
                bleResponseLabel.value = "Time zone set successfully"
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {

        }).let {
            sendDisposable.add(it)
        }
    }

    fun getTimeOffset() {
        initData()
        StarmaxBleClient.instance.getTimeOffset().subscribe({
            if (it.status == 0) {
                bleResponseLabel.value = "Time zone retrieved successfully"
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {

        }).let {
            sendDisposable.add(it)
        }
    }


    fun getHealthDetail() {
        initData()
//        StarmaxBleClient.instance.getHeartRateControl().subscribe({
//            if(it.status==0){
//             print(it)
//            }
//        },{}).let {  }
//        StarmaxBleClient.instance.healthMeasureStream().subscribe({
//            if (it.status==0){
//                print(it)
//            }
//        },{}).let {  }
        StarmaxBleClient.instance.getHealthDetail().subscribe({

            if (it.status == 0) {
              /*  bleResponseLabel.value = (
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
                        )*/
                val healthData = mapOf(
                    "Total step count" to it.totalSteps,
                    "Total calories (Cal)" to String.format(java.util.Locale.US,"%.2f",(it.totalHeat / 1000.0 ))  ,
                    "Total distance (m)" to String.format( java.util.Locale.US,"%.3f",it.totalDistance / 1609.34), //meter unit is converted to miles (1 mile = 1609.34 meter)
                    "Total sleep duration (minutes)" to it.totalSleep,
                    "Deep sleep duration" to it.totalDeepSleep,
                    "Light sleep duration" to it.totalLightSleep,
                    "Current heart rate" to "${ it.currentHeartRate } bpm",
                    "Current blood pressure" to "${it.currentSs}/${it.currentFz} mmHg",
                    "Current blood oxygen" to "${it.currentBloodOxygen}%",
                    "Current pressure" to it.currentPressure,
                    "Current MAI" to it.currentMai,
                    "Current MET" to it.currentMet,
                    "Current temperature" to "${(it.currentTemp/10.0)}\u00B0 C",
                    "Current blood sugar" to it.currentBloodSugar / 10.0,
                    "Is worn" to it.isWear,
                    "Respiration rate" to it.respirationRate,
                    "Shake head" to it.shakeHead
                )
                bleHealthResponseLabel.value = healthData
                bleResponseLabel.value = healthData.entries.joinToString("\n") { (key, value) ->
                    "$key: $value"
                }
                print(bleHealthResponseLabel.value)
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
                bleResponseLabel.value = "\n" + "Alarm set successfully"
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
                bleResponseLabel.value = "Set up sedentary successfully"
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
                bleResponseLabel.value = "Set drinking water successfully"
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
            "New message",
            "I think it can receive ten characters. I think it can receive ten characters. " +
                    "I think it can receive ten characters. I think it can receive ten characters. " +
                    "I think it can receive ten characters. I think it can receive ten characters. " +
                    "I think it can receive ten characters."
        ).subscribe({
            if (it.status == 0) {
                bleResponseLabel.value = "Message sent successfully"
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {
        }).let {
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
                bleResponseLabel.value = "Set weather successfully"
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
                bleResponseLabel.value = "Read weather successfully"
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
            cityName = "Shenzhen City",  // 深圳市
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
                bleResponseLabel.value = "\n" +
                        "Set weather successfully"
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
                bleResponseLabel.value = "\n" + "Read weather successfully"
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
                bleResponseLabel.value = "Set world clock successfully"
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
            1, 20, 30,
            "Die if you don’t love me (不爱我就去死吧)",
            ""
        ).subscribe({
            bleResponseLabel.value = "Music control succeeded"
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun getLog() { // TODO
//    initData()
//    StarmaxBleClient.instance.getLog().subscribe({
//        bleResponseLabel.value = "Log retrieved successfully"
//    }, {}).let {
//        sendDisposable.add(it)
//    }
    }

    fun getCustomDeviceMode() {
        initData()
        StarmaxBleClient.instance.getCustomDeviceMode().subscribe({
            if (it.status == 0) {
                val modeStr = if (it.mode == 0) "Normal mode" else "Class mode"
                bleResponseLabel.value = "Type: ${it.type}, Mode: $modeStr"
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
                bleResponseLabel.value = "Type: ${it.type}, Name: ${it.deviceName}"
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
                bleResponseLabel.value =
                    "Device name: ${it.deviceName}\n" +
                            "Current mode: ${it.mode}\n" +
                            "Heart rate: ${it.heartRate}\n" +
                            "Steps: ${it.steps}\n" +
                            "Blood pressure: ${it.ss}/${it.fz}\n" +
                            "Blood oxygen: ${it.oxygen}\n" +
                            "Blood sugar: ${it.bloodSugar}\n" +
                            "Temperature: ${it.temp}\n" +
                            "MET: ${it.met}\n" +
                            "MAI: ${it.mai}\n" +
                            "Stress: ${it.stress}\n" +
                            "Calories: ${it.calorie}\n" +
                            "Battery: ${it.power}\n"
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {})
    }

    fun getSportMode() {
        initData()
        StarmaxBleClient.instance.getSportMode().subscribe({
            if (it.status == 0) {
                var str = ""
                val dataList = it.sportModesList
                for (i in 0 until dataList.size) {
                    str += "Sport mode: ${sportModeLabel(dataList[i])}\n"
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
                bleResponseLabel.value =
                    if (isDisplay) "Demo mode enabled successfully"
                    else "Demo mode disabled"
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
                bleResponseLabel.value =
                    if (isOpen) "Shipping mode enabled successfully"
                    else "Shipping mode disabled successfully"
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
                bleResponseLabel.value = "Type: ${it.type}, Time: ${it.time}"
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
                    "Type: ${it.type}, Shake: ${it.shakeOnOff}, Wrist raise: ${it.handOnOff}"
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
                    "Type: ${it.type}, Shake count: ${it.shakeTimes}, Wrist raise count: ${it.handTimes}"
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
        StarmaxBleClient.instance.setSportMode(listOf(0x0A, 0x0B, 0x0C, 0x0D)).subscribe({
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
        StarmaxBleClient.instance
            .sendHealthMeasure(HistoryType.Pressure, isOpen)
            .subscribe({
                bleResponseLabel.value =
                    if (isOpen) "Enabled successfully" else "Disabled successfully"
            }, {}).let {
                sendDisposable.add(it)
            }
    }

    fun sendHeartRateHealthMeasure(isOpen: Boolean) {
        initData()
        StarmaxBleClient.instance
            .sendHealthMeasure(HistoryType.HeartRate, isOpen)
            .subscribe({
                bleResponseLabel.value =
                    if (isOpen) "Enabled successfully" else "Disabled successfully"
            }, {}).let {
                sendDisposable.add(it)
            }
    }

    fun getDebugInfo(fileType: Int) {
        initData()
        StarmaxBleClient.instance.getDebugInfo(packageId, fileType).subscribe({
            if (it.status == 0) {
                if (it.dataList.isNotEmpty()) {
                    TestRepository.testLocal(
                        localBasePath,
                        if (fileType == 3) {
                            it.dataList.map { it.toByte() }.toByteArray()
                                .toString(Charsets.US_ASCII)
                                .replace("TAG=", "\nTAG=")
                        } else {
                            it.dataList.map { String.format("0x%02X", it.toByte()) }
                                .chunked(8)
                                .joinToString(",\n") { it.joinToString(",") } + ",\n\n\n"
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
                    "Retrieved " +
                            (if (fileType == 1) "battery.txt" else "gsensor.txt") +
                            ", package $packageId"
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
                    SportHistoryFactory(StarmaxBleClient.instance.bleNotify)
                        .buildMapFromProtobuf(it)
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
        val calendar = Calendar.getInstance().apply { timeInMillis = time }

        StarmaxBleClient.instance.getStepHistory(calendar).subscribe({
            if (it.status == 0) {
                var str = "Sampling interval: ${it.interval} minutes\n" +
                            "Date: ${it.year}-${it.month}-${it.day}\n" +
                            "Data length: ${it.dataLength}\n"

                it.stepsList.forEach { data ->
                    str +=
                        "Time: ${data.hour}:${data.minute} " +
                                "Steps: ${data.steps}, " +
                                "Calories: ${(data.calorie.toDouble() / 1000)} kcal, " +
                                "Distance: ${(data.distance.toDouble() / 10)} m\n"
                }

                it.sleepsList.forEach { data ->
                    str +=
                        "Time: ${data.hour}:${data.minute} " +
                                "Sleep status: ${data.sleepStatus}\n"
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
        val calendar = Calendar.getInstance().apply { timeInMillis = time }

        StarmaxBleClient.instance.getBloodPressureHistory(calendar).subscribe({
            if (it.status == 0) {
                var str =
                    "Sampling interval: ${it.interval} minutes\n" +
                            "Date: ${it.year}-${it.month}-${it.day}\n" +
                            "Data length: ${it.dataLength}\n"

                it.dataList.forEach { data ->
                    str +=
                        "Time: ${data.hour}:${data.minute} " +
                                "Systolic: ${data.ss} Diastolic: ${data.fz}\n"
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
                bleResponseLabel.value =
                    "Fall asleep: ${it.fallAsleepHour}:${it.fallAsleepMinute} ${it.fallAsleepOnOff}\n" +
                            "Wake up: ${it.getUpHour}:${it.getUpMinute} ${it.getUpOnOff}\n" +
                            "Repeat: ${it.repeatsList.joinToString(",")}\n" +
                            "Reminder ${it.reminderBeforeFallAsleep} minutes before sleep\n"
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
        val calendar = Calendar.getInstance().apply { timeInMillis = time }

        StarmaxBleClient.instance.getHeartRateHistory(calendar).subscribe({
            if (it.status == 0) {
                var str =
                    "Sampling interval: ${it.interval} minutes\n" +
                            "Date: ${it.year}-${it.month}-${it.day}\n" +
                            "Data length: ${it.dataLength}\n"

                it.dataList.forEach { data ->
                    str += "Time: ${data.hour}:${data.minute} Heart rate: ${data.value}%\n"
                }

                bleResponseLabel.value = str
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let { sendDisposable.add(it) }
    }

    fun getExerciseHistory(time: Long) {
        initData()
        val calendar = Calendar.getInstance().apply { timeInMillis = time }

        StarmaxBleClient.instance.getExerciseHistory(calendar).subscribe({
            if (it.status == 0) {
                var str =
                    "Sampling interval: ${it.interval} minutes\n" +
                            "Date: ${it.year}-${it.month}-${it.day}\n" +
                            "Data length: ${it.dataLength}\n"

                it.exerciseDataList.forEach { data ->
                    str += "Time: ${data.hour}:${data.minute} Moderate/High intensity: ${data.value}\n"
                }

                it.standDataList.forEach { data ->
                    str += "Time: ${data.hour}:${data.minute} Standing: ${data.value}\n"
                }

                bleResponseLabel.value = str
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let { sendDisposable.add(it) }
    }

    fun getBloodOxygenHistory(time: Long) {
        initData()
        val calendar = Calendar.getInstance().apply { timeInMillis = time }

        StarmaxBleClient.instance.getBloodOxygenHistory(calendar).subscribe({
            if (it.status == 0) {
                var str =
                    "Sampling interval: ${it.interval} minutes\n" +
                            "Date: ${it.year}-${it.month}-${it.day}\n" +
                            "Data length: ${it.dataLength}\n"

                it.dataList.forEach { data ->
                    str += "Time: ${data.hour}:${data.minute} Blood oxygen: ${data.value}%\n"
                }

                bleResponseLabel.value = str
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let { sendDisposable.add(it) }
    }

    fun getRespirationRateHistory(time: Long) {
        initData()
        val calendar = Calendar.getInstance().apply { timeInMillis = time }

        StarmaxBleClient.instance.getRespirationRateHistory(calendar).subscribe({
            if (it.status == 0) {
                var str =
                    "Sampling interval: ${it.interval} minutes\n" +
                            "Date: ${it.year}-${it.month}-${it.day}\n" +
                            "Data length: ${it.dataLength}\n"

                it.dataList.forEach { data ->
                    str += "Time: ${data.hour}:${data.minute} Respiration rate: ${data.value}%\n"
                }

                bleResponseLabel.value = str
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let { sendDisposable.add(it) }
    }

    fun getPressureHistory(time: Long) {
        initData()
        changeMtu {
            val calendar = Calendar.getInstance().apply { timeInMillis = time }

            StarmaxBleClient.instance.getPressureHistory(calendar).subscribe({
                if (it.status == 0) {
                    var str =
                        "Sampling interval: ${it.interval} minutes\n" +
                                "Date: ${it.year}-${it.month}-${it.day}\n" +
                                "Data length: ${it.dataLength}\n"

                    it.dataList.forEach { data ->
                        str += "Time: ${data.hour}:${data.minute} Stress: ${data.value}%\n"
                    }

                    bleResponseLabel.value = str
                } else {
                    bleResponseLabel.value = statusLabel(it.status)
                }
            }, {}).let { sendDisposable.add(it) }
        }
    }

    fun getMetHistory(time: Long) {
        initData()
        val calendar = Calendar.getInstance().apply { timeInMillis = time }

        StarmaxBleClient.instance.getMetHistory(calendar).subscribe({
            if (it.status == 0) {
                var str =
                    "Sampling interval: ${it.interval} minutes\n" +
                            "Date: ${it.year}-${it.month}-${it.day}\n" +
                            "Data length: ${it.dataLength}\n"

                it.dataList.forEach { value ->
                    str += "MET: $value\n"
                }

                bleResponseLabel.value = str
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let { sendDisposable.add(it) }
    }

    fun getOriginSleepHistory(time: Long) {
        initData()
        val calendar = Calendar.getInstance().apply { timeInMillis = time }

        StarmaxBleClient.instance.getOriginSleepHistory(calendar).subscribe({
            if (it.status == 0) {
                var str =
                    "Sampling interval: ${it.interval} minutes\n" +
                            "Date: ${it.year}-${it.month}-${it.day}\n" +
                            "Data length: ${it.dataLength}\n"

                it.dataList.forEach { data ->
                    if (data.value > 0) {
                        val valueList = Utils.int2byte(data.value, 2)
                        str += "Time: ${data.hour}:${data.minute} Infrared: ${valueList[1]} SAR: ${valueList[0] * 256}\n"
                    }
                }

                bleResponseLabel.value = str
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let { sendDisposable.add(it) }
    }

    fun getShakeHeadHistory(time: Long) {
        initData()
        val calendar = Calendar.getInstance().apply { timeInMillis = time }

        StarmaxBleClient.instance.getShakeHeadHistory(calendar).subscribe({
            if (it.status == 0) {
                var str =
                    "Sampling interval: ${it.interval} minutes\n" +
                            "Date: ${it.year}-${it.month}-${it.day}\n" +
                            "Data length: ${it.dataLength}\n"

                it.dataList.forEach { data ->
                    if (data.value > 0) {
                        str += "Time: ${data.hour}:${data.minute} Head shake: ${data.value}\n"
                    }
                }

                bleResponseLabel.value = str
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let { sendDisposable.add(it) }
    }

    fun getTempHistory(time: Long) {
        initData()
        val calendar = Calendar.getInstance().apply { timeInMillis = time }

        StarmaxBleClient.instance.getTempHistory(calendar).subscribe({
            if (it.status == 0) {
                var str =
                    "Sampling interval: ${it.interval} minutes\n" +
                            "Date: ${it.year}-${it.month}-${it.day}\n" +
                            "Data length: ${it.dataLength}\n"

                it.dataList.forEach { data ->
                    str += "Time: ${data.hour}:${data.minute} Temperature: ${data.value}%\n"
                }

                bleResponseLabel.value = str
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let { sendDisposable.add(it) }
    }

    fun getMaiHistory(time: Long) {
        initData()
        val calendar = Calendar.getInstance().apply { timeInMillis = time }

        StarmaxBleClient.instance.getMaiHistory(calendar).subscribe({
            if (it.status == 0) {
                var str =
                    "Sampling interval: ${it.interval} minutes\n" +
                            "Date: ${it.year}-${it.month}-${it.day}\n" +
                            "Data length: ${it.dataLength}\n"

                it.dataList.forEach { value ->
                    str += "MAI: $value\n"
                }

                bleResponseLabel.value = str
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let { sendDisposable.add(it) }
    }

    fun getBloodSugarHistory(time: Long) {
        initData()
        val calendar = Calendar.getInstance().apply { timeInMillis = time }

        StarmaxBleClient.instance.getBloodSugarHistory(calendar).subscribe({
            if (it.status == 0) {
                var str =
                    "Sampling interval: ${it.interval} minutes\n" +
                            "Date: ${it.year}-${it.month}-${it.day}\n" +
                            "Data length: ${it.dataLength}\n"

                it.dataList.forEach { data ->
                    str += "Time: ${data.hour}:${data.minute} Blood sugar: ${data.value}\n"
                }

                bleResponseLabel.value = str
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let { sendDisposable.add(it) }
    }

    fun getCustomHealthGoalsHistory(time: Long) {
        initData()
        val calendar = Calendar.getInstance().apply { timeInMillis = time }

        StarmaxBleClient.instance.getCustomHealthGoalsHistory(calendar).subscribe({
            if (it.status == 0) {
                var str = "Date: ${it.year}-${it.month}-${it.day}\n\n"

                it.goalInfosList.forEach { goal ->
                    str += "-------------------------\n" +
                            "Prescription index: ${goal.index}\n" +
                            "Start date: ${goal.startYear}-${goal.startMonth}-${goal.startDay}\n" +
                            "End date: ${goal.endYear}-${goal.endMonth}-${goal.endDay}\n" +
                            "-------------------------\n\n"

                    goal.taskInfosList.forEach { task ->
                        str += "Task index: ${task.index}\n" +
                                "Status: ${task.status}\n" +
                                "Completion: ${task.scale}%, Real time: ${task.realSeconds}s\n" +
                                "Goal time: ${task.goalMinutes}, Actual time: ${task.completeMinutes}\n" +
                                "Task time: ${task.goalStartHour}:${task.goalStartMinute}-${task.goalEndHour}:${task.goalEndMinute}\n" +
                                "Avg HR: ${task.avgHeartRate}, Avg step freq: ${task.avgStepFreq}\n" +
                                "Steps from task: ${task.steps}\n\n"
                    }
                }

                bleResponseLabel.value = str
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let { sendDisposable.add(it) }
    }

    fun getSleepHistory(time: Long) {
        initData()
        val calendar = Calendar.getInstance().apply { timeInMillis = time }
        StarmaxBleClient.instance.getSleepHistory(calendar).subscribe({
            if (it.status == 0) {
                var str =
                    "Sampling interval: ${it.interval} minutes\n" +
                            "Date: ${it.year}-${it.month}-${it.day}\n" +
                            "Data length: ${it.dataLength}\n"

                it.dataList.forEach { data ->
                    if (data.status != 0) {
                        str += "Time: ${data.hour}:${data.minute} Status: ${data.status}\n"
                    }
                }

                bleResponseLabel.value = str
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let { sendDisposable.add(it) }
    }

    fun getGoalsDayAndNightHistory(time: Long) {
        initData()
        val calendar = Calendar.getInstance().apply { timeInMillis = time }

        StarmaxBleClient.instance.getGoalsDayAndNightHistory(calendar).subscribe({
            if (it.status == 0) {
                var str = "Date: ${it.year}-${it.month}-${it.day}\n\n"

                str += "Total steps: ${it.totalSteps}, Goal: ${it.totalStepGoals}\n\n"

                str += "Day:\n"
                it.dayGoals.let { d ->
                    str += "Steps: ${d.steps}, Goal: ${d.stepGoals}\n"
                    str += "Time: ${d.startHour}:${d.startMinute}-${d.endHour}:${d.endMinute}\n"
                    str += "Status: ${d.status}\n\n"
                }

                str += "Night:\n"
                it.nightGoals.let { n ->
                    str += "Steps: ${n.steps}, Goal: ${n.stepGoals}\n"
                    str += "Time: ${n.startHour}:${n.startMinute}-${n.endHour}:${n.endMinute}\n"
                    str += "Status: ${n.status}\n\n"
                }

                bleResponseLabel.value = str
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let { sendDisposable.add(it) }
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
                    "Simplified Chinese",
                    "Traditional Chinese",
                    "English",
                    "Russian",
                    "French",
                    "Spanish",
                    "German",
                    "Japanese",
                    "Italian",
                    "Korean",
                    "Dutch",
                    "Thai",
                    "Vietnamese",
                    "Malay",
                    "Indonesian",
                    "Portuguese",
                    "Romanian",
                    "Polish",
                    "Turkish",
                    "Mongolian",
                    "Hindi",
                    "Arabic"
                )

                val dataList = it.languagesList.joinToString("\n") { language ->
                    languageList[language]
                }

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
                var str = "Valid date\n"

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
                var str = "Valid dates\n"

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
                        if (ui == null) return@getVersion

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
                                                    object : BleFileSenderListener() {
                                                        override fun onSuccess() {}
                                                        override fun onTotalSuccess() {}

                                                        override fun onProgress(progress: Double) {
                                                            bleMessage.value =
                                                                "Current progress ${progress}%"
                                                        }

                                                        override fun onFailure(status: Int) {
                                                            bleMessage.value = "Installation failed"
                                                        }

                                                        override fun onStart() {
                                                            val data = StarmaxSend()
                                                                .sendUi(offset = 0, ui.version)
                                                            sendMsg(data)
                                                        }

                                                        override fun onCheckSum() {}
                                                        override fun onSendComplete() {}
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
                                                bleMessage.value = "File not found"
                                                e.printStackTrace()
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    bleMessage.value = "Server error"
                                    e.printStackTrace()
                                }
                            }
                        }.start()
                    },
                    onError = { e ->
                        bleMessage.value = "Server error"
                        e?.printStackTrace()
                    })
            }
        }.start()
    }

    fun sendUiDiff() {
        initData()
        if (!uiSupportDifferentialUpgrade) {
            bleMessage.value = "The current device does not support UI differential upgrade"
            return
        }
        object : Thread() {
            override fun run() {
                UiRepository.getDiff(
                    model = bleModel,
                    version = bleUiVersion,
                    onSuccess = { ui, _ ->
                        if (ui == null) return@getDiff

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
                                                    object : BleFileSenderListener() {
                                                        override fun onSuccess() {}
                                                        override fun onTotalSuccess() {}

                                                        override fun onProgress(progress: Double) {
                                                            bleMessage.value =
                                                                "Current progress ${progress}%"
                                                        }

                                                        override fun onFailure(status: Int) {}
                                                        override fun onStart() {
                                                            val data = StarmaxSend()
                                                                .sendUi(offset = ui.offset, ui.version)
                                                            sendMsg(data)
                                                        }

                                                        override fun onCheckSum() {}
                                                        override fun onSendComplete() {}
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
                                                bleMessage.value = "File not found"
                                                e.printStackTrace()
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    bleMessage.value = "Server error"
                                    e.printStackTrace()
                                }
                            }
                        }.start()
                    },
                    onError = { e ->
                        bleMessage.value = "Server error"
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
                object : BleFileSenderListener() {
                    override fun onSuccess() {}
                    override fun onTotalSuccess() {}

                    override fun onProgress(progress: Double) {
                        bleMessage.value = "Current progress ${progress}%"
                    }

                    override fun onCheckSum() {}
                    override fun onSendComplete() {}
                    override fun onFailure(status: Int) {}

                    override fun onStart() {
                        val data = StarmaxSend().sendUi(offset = 0, "1.0.0")
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
            bleMessage.value = "File not found"
            e.printStackTrace()
        }
    }

    fun sendDialV2Local(context: Context, uri: Uri) {
        initData()
        val fis = context.contentResolver.openInputStream(uri) ?: return
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
                    bleMessage.value = "Server error"
                    e.printStackTrace()
                }
            }
        }
    }

    fun sendCustomDialV2Local(context: Context) {
        initData()
        lcdWidth = 410
        lcdHeight = 502
        Log.d("imageUri", imageUri.toString())
        Log.d("binUri", binUri.toString())

        if (binUri == null || imageUri == null) return

        val bin = context.contentResolver.openInputStream(binUri!!) as FileInputStream?
        val img = context.contentResolver.openInputStream(imageUri!!) as FileInputStream? ?: return

        if (bin == null) return

        val file = File(savePath)
        if (!file.exists()) file.mkdirs()
        val saveName = savePath + "dialv2.zip"

        val apkFile = File(saveName)
        if (apkFile.exists()) apkFile.delete()

        NetFileUtils.copyUpdateFile(bin, File(saveName)) {
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
                    srcBitmap = com.starmax.sdkdemo.utils.BmpUtils.convertSize(srcBitmap, lcdWidth, lcdHeight)

                    val outputStream = ByteArrayOutputStream()
                    // Write Bitmap into PNG format
                    srcBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)

                    Log.d("pngData", "${FileValidator.isPngData(outputStream.toByteArray())}")
                    val bmpCopy = sifliEzipUtil.pngToEzip(outputStream.toByteArray(), "rgb565A", 0, 1, 2)
                    Log.d("pngDataZip", "${bmpCopy.size}")

                    FileOutputStream(currentFile).use { fos ->
                        fos.write(bmpCopy)
                        fos.flush()
                    }

                    outputStream.flush()
                    outputStream.close()
                    srcBitmap.recycle()
                }
            }

            println("Start color modification")
            filePaths.forEach {
                val currentFile = it["current_file"] as File
                if (canChangeColorFiles[currentFile.absolutePath] != null) {
                    println(currentFile.absolutePath)
                    val bytes = FileUtils.inputStream2Bytes(FileInputStream(currentFile))
                    if (bytes != null) {
                        val newBytes = com.starmax.sdkdemo.utils.BmpUtils.argbConvertColor(bytes, 0xFF, 0x00, 0x00)
                        FileOutputStream(currentFile).use { fos ->
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
                    bleMessage.value = "Server error"
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
                        println("Start reading the watch's local MD5")
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
                val md5Str = jsonMap[currentFile.name] ?: ""
                val deviceLocalMd5Str = deviceLocalJsonMap[currentFile.name] ?: ""
                println("Network: $md5Str")
                println("Watch local: $deviceLocalMd5Str")
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
                bleMessage.value = "Server error"
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
                            val data = StarmaxSend().sendTotalFileComplete()
                            sendMsg(data)
                        }
                    }

                    override fun onTotalSuccess() {}

                    override fun onProgress(progress: Double) {
                        bleMessage.value =
                            "File: ${currentInfo["relative_path"]}, Progress: ${progress.toInt()}%, md5: ${currentInfo["md5"] as String}"
                    }

                    override fun onFailure(status: Int) {
                        bleMessage.value = "Send failed"
                    }

                    override fun onCheckSum() {}

                    override fun onSendComplete() {
                        val data = StarmaxSend().sendSingleFileComplete()
                        sendMsg(data)
                    }

                    override fun onStart() {
                        val data = StarmaxSend().sendFileV2Header(
                            currentInfo["md5"] as String,
                            filePaths.size,
                            index,
                            BleFileSender.allFileData.size,
                            currentInfo["relative_path"] as String
                        )
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
            bleMessage.value = "Server error"
            e.printStackTrace()
        }
    }

    fun sendDialLocal(context: Context) {
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
                            bleMessage.value = "Progress: ${progress.toInt()}%"
                        }

                        override fun onFailure(status: Int) {}

                        override fun onCheckSum() {}

                        override fun onSendComplete() {}

                        override fun onStart() {
                            val data = StarmaxSend().sendDial(
                                5001,
                                BmpUtils.bmp24to16(255, 255, 255),
                                1
                            )
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
                bleMessage.value = "Server error"
                e.printStackTrace()
            }
        }
    }

    fun unittest() {
        initData()
        object : Thread() {
            override fun run() {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://www.runmefit.cn/api/firmware/unit_test")
                    .build()

                try {
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
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
                                            "Unit test: ${name}, Value: ${value}, Status: ${status}"
                                    }, {}).let {
                                        sendDisposable.add(it)
                                    }
                            }
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
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
                            bleMessage.value = "Progress: ${progress.toInt()}%"
                        }

                        override fun onFailure(status: Int) {}

                        override fun onCheckSum() {}

                        override fun onSendComplete() {}

                        override fun onStart() {
                            val data = StarmaxSend().sendLogo()
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
                bleMessage.value = "Server error"
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
                            bleMessage.value = "Progress: ${progress.toInt()}%"
                        }

                        override fun onFailure(status: Int) {}

                        override fun onCheckSum() {}

                        override fun onSendComplete() {}

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
                bleMessage.value = "Server error"
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
                                            bleMessage.value = "Current progress ${progress}%"
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
                                bleMessage.value = "\n" + "file not found"
                                e.printStackTrace()
                            }
                        }
                    }
                } catch (e: java.lang.Exception) {
                    bleMessage.value = "Server error"
                    e.printStackTrace()
                }
            }
        }.start()
    }

    fun clearLogo() {
        initData()
        StarmaxBleClient.instance.clearLogo().subscribe({
            if (it.status == 0) {
                bleResponseLabel.value = "\n" + "Logo cleared successfully"
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
        if (binUri == null) return

        changeMtu {
            try {
                val bin = context.contentResolver.openInputStream(binUri!!) as FileInputStream?
                val lastSendCalendar = Calendar.getInstance()
                BleFileSender.initFile(
                    bin,
                    object : BleFileSenderListener() {
                        override fun onSuccess() {
                            val currentCalendar = Calendar.getInstance()
                            val diffInSeconds: Long =
                                (currentCalendar.timeInMillis - lastSendCalendar.timeInMillis) / 1000
                            bleMessage.value = "Send completed, time elapsed: ${diffInSeconds}s"
                        }

                        override fun onTotalSuccess() {}

                        override fun onProgress(progress: Double) {
                            val currentCalendar = Calendar.getInstance()
                            val diffInSeconds: Long =
                                (currentCalendar.timeInMillis - lastSendCalendar.timeInMillis) / 1000
                            bleMessage.value = "Progress: ${progress.toInt()}%, Time elapsed: ${diffInSeconds}s"
                        }

                        override fun onFailure(status: Int) {
                            bleMessage.value = "Send failed"
                        }

                        override fun onCheckSum() {
                            val data = StarmaxSend().sendDiffCheckSum()
                            Log.d("Diff Sender", "${BleFileSender.checksumData.size}")
                            bleMessage.value = "Sending checksum packet ${BleFileSender.checksumSendIndex}"
                            sendMsg(data)
                        }

                        override fun onStart() {
                            val data = StarmaxSend().sendDiffHeader()
                            bleMessage.value = "Sending file header"
                            sendMsg(data)
                        }

                        override fun onSendComplete() {
                            val data = StarmaxSend().sendDiffComplete()
                            bleMessage.value = "Send complete notification to firmware"
                            sendMsg(data)
                        }

                        override fun onSend() {
                            val data = StarmaxSend().sendDiffFile()
                            sendMsg(data)
                        }
                    })

                BleFileSender.sliceBuffer = 8
                BleFileSender.onStart()
            } catch (e: FileNotFoundException) {
                bleMessage.value = "Server error"
                e.printStackTrace()
            }
        }
    }

    fun sendGts7CrcLocal(context: Context) {
        initData()
        if (binUri == null) return

        try {
            val bin = context.contentResolver.openInputStream(binUri!!) as FileInputStream?
            val lastSendCalendar = Calendar.getInstance()
            BleFileSender.initFile(
                bin,
                object : BleFileSenderListener() {
                    override fun onSuccess() {
                        val currentCalendar = Calendar.getInstance()
                        val diffInSeconds: Long =
                            (currentCalendar.timeInMillis - lastSendCalendar.timeInMillis) / 1000
                        bleMessage.value = "Send completed, time elapsed: ${diffInSeconds}s"
                    }

                    override fun onTotalSuccess() {}

                    override fun onProgress(progress: Double) {
                        val currentCalendar = Calendar.getInstance()
                        val diffInSeconds: Long =
                            (currentCalendar.timeInMillis - lastSendCalendar.timeInMillis) / 1000
                        bleMessage.value = "Progress: ${progress.toInt()}%, Time elapsed: ${diffInSeconds}s"
                    }

                    override fun onFailure(status: Int) {
                        bleMessage.value = "Send failed"
                    }

                    override fun onCheckSum() {
                        val data = StarmaxSend().sendDiffCheckSum()
                        Log.d("Diff Sender", "Sending checksum packet ${BleFileSender.checksumSendIndex}")
                        Log.d("Diff Sender", "Checksum size: ${BleFileSender.checksumData.size}")
                        bleMessage.value = "Sending checksum packet ${BleFileSender.checksumSendIndex}"
                        Log.d("Diff Sender", "Data size: ${data.size}")
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
                        bleMessage.value = "Sending file header"
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
                        bleMessage.value = "Send complete notification to firmware"
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
            bleMessage.value = "Server error"
            e.printStackTrace()
        }
    }

    fun sendCustomDial(context: Context) {
        initData()
        if (binUri == null || imageUri == null) return

        changeMtu {
            try {
                val bin = context.contentResolver.openInputStream(binUri!!) as FileInputStream?
                val img = context.contentResolver.openInputStream(imageUri!!) as FileInputStream?

                var lastSendCalendar = Calendar.getInstance()
                BleFileSender.initFileWithBackground(
                    bin,
                    lcdWidth, lcdHeight,
                    img,
                    object : BleFileSenderListener() {
                        override fun onSuccess() {}
                        override fun onTotalSuccess() {}

                        override fun onProgress(progress: Double) {
                            bleMessage.value = "Progress: ${progress.toInt()}%"
                        }

                        override fun onFailure(status: Int) {}

                        override fun onStart() {
                            val data = StarmaxSend().sendDial(5001, BmpUtils.bmp24to16(255, 255, 255), 1)
                            Utils.p(data)
                            sendMsg(data)
                        }

                        override fun onCheckSum() {}

                        override fun onSendComplete() {}

                        override fun onSend() {
                            if (BleFileSender.hasNext()) {
                                val data = StarmaxSend().sendFile()
                                BleManager.getInstance().write(
                                    bleDevice?.get(),
                                    WriteServiceUUID.toString(),
                                    WriteCharacteristicUUID.toString(),
                                    data,
                                    object : BleWriteCallback() {
                                        override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                                            if (current == total) {
                                                val newSendCalendar = Calendar.getInstance()
                                                val millis = newSendCalendar.timeInMillis - lastSendCalendar.timeInMillis
                                                Log.e("BleFileSender", "Send time: ${millis}ms, current RSSI:")
                                                lastSendCalendar = Calendar.getInstance()
                                            }
                                        }

                                        override fun onWriteFailure(exception: BleException?) {}
                                    }
                                )
                            }
                        }
                    })

                BleFileSender.sliceBuffer = 8
                BleFileSender.onStart()
            } catch (e: FileNotFoundException) {
                bleMessage.value = "Server error"
                e.printStackTrace()
            }
        }
    }

    fun getDialInfo() {
        initData()
        StarmaxBleClient.instance.getDialInfo().subscribe({
            var str = ""
            val dataList = it.infosList
            for (oneData in dataList) {
                val isSelected = oneData.isSelected
                val dialId = oneData.dialId
                val dialColor = oneData.dialColor
                val align = oneData.align
                if (isSelected == 1) str += "Selected\n"
                str += "Dial ID: $dialId\n"
                str += "Dial Color: ${Utils.bytesToHex(Utils.int2byte(dialColor, 3))}\n"
                str += "Position: $align\n"
            }
            bleResponseLabel.value = str
        }, {}).let { sendDisposable.add(it) }
    }


    fun switchDial() {
        initData()
        StarmaxBleClient.instance.switchDial(5001).subscribe({
            if (it.status == 0) {
                bleResponseLabel.value = "Dial switch successful"
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
            { bleResponseLabel.value = "Unpair successful" },
            {}
        ).also { sendDisposable.add(it) }
    }

    fun unpairCheck() {
        initData()
        StarmaxBleClient.instance.unpair(1).subscribe({
            bleResponseLabel.value = "Unpair successful"
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun reset() {
        initData()
        StarmaxBleClient.instance.reset().subscribe({
            bleResponseLabel.value = "Factory reset successful"
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun close() {
        initData()
        StarmaxBleClient.instance.close().subscribe({
            bleResponseLabel.value = "Shutdown successful"
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun shippingMode() {
        initData()
        StarmaxBleClient.instance.shippingMode().subscribe({
            bleResponseLabel.value = "Entered shipping mode"
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun getNfcCardInfo() {
        initData()
        StarmaxBleClient.instance.getNfcInfo().subscribe({
            if (it.status == 0) {
                var str = ("Type:" + it.type)

                val cardsList = it.cardsList
                for (i in 0 until cardsList.size) {
                    val oneData = cardsList[i]
                    str += "Card type:" + oneData.cardType + ", Card name:" + oneData.cardName + "%\n"
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
     * Send a BLE message
     */
    fun sendMsg(data: ByteArray?) {
        if (bleDevice == null || bleDevice!!.get() == null || !BleManager.getInstance()
                .isConnected(bleDevice!!.get())
        ) {
            sendDisposable.clear() // Clear sending queue
            viewModelScope.launch {
                Toast.makeText(context, "Bluetooth not connected", Toast.LENGTH_SHORT).show()
            }
            return
        }

        BleManager.getInstance().write(
            bleDevice?.get(),
            WriteServiceUUID.toString(),
            WriteCharacteristicUUID.toString(),
            data,
            object : BleWriteCallback() {
                override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                    // bleMessage.value = "Command sent successfully"
                }

                override fun onWriteFailure(exception: BleException?) {
                    // bleMessage.value = "Command sending failed"
                }
            })
    }

    fun disconnect() {
        BleManager.getInstance().disconnectAllDevice()
    }

    fun changeMtu(onMtuChanged: () -> Unit) {
        BleManager.getInstance().setMtu(bleDevice?.get(), 512, object : BleMtuChangedCallback() {
            override fun onSetMTUFailure(exception: BleException) {
                // Failed to set MTU
                Log.e("BleViewModel", exception.description)
            }

            override fun onMtuChanged(mtu: Int) {
                BleManager.getInstance().setSplitWriteNum(min(mtu - 3, 512))
                Log.e("BleViewModel", "MTU set to $mtu successfully")
                onMtuChanged()
            }
        })
    }

    private fun statusLabel(status: Int): String {
        return when (status) {
            0 -> "Command correct"
            1 -> "Command code error"
            2 -> "Checksum error"
            3 -> "Data length error"
            4 -> "Invalid data"
            else -> "Invalid data"
        };
    }

    private fun sportModeLabel(mode: Int): String {
        return when (mode) {
            0X00 -> "Indoor running"
            0X01 -> "Outdoor running"
            0X03 -> "Outdoor cycling"
            0X04 -> "Brisk walking"
            0X05 -> "Jump rope"
            0X06 -> "Football"
            0X07 -> "Badminton"
            0X09 -> "Basketball"
            0X0A -> "Elliptical machine"
            0X0B -> "Hiking"
            0X0C -> "Yoga"
            0X0D -> "Strength training"
            0X0E -> "Mountaineering"
            0X0F -> "Free exercise"
            0X10 -> "Outdoor walking"
            0X12 -> "Indoor cycling"
            else -> "Invalid data"
        };
    }


    class BluetoothListenerReceiver(val bleViewModel: BleViewModel) : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0)) {
                        BluetoothAdapter.STATE_TURNING_ON -> Log.e(
                            "BleReceiver",
                            "onReceive---------Bluetooth is turning on"
                        )

                        BluetoothAdapter.STATE_ON -> {
                            Log.e("BleReceiver", "onReceive---------Bluetooth is turned on")
                            // Optionally, you can reconnect to the device here
                            // Handler(Looper.getMainLooper()).postDelayed({
                            //     BleManager.getInstance().connect(
                            //         bleViewModel.bleDevice?.get()?.mac,
                            //         bleViewModel.bleGattCallback
                            //     )
                            // }, 1000)
                        }

                        BluetoothAdapter.STATE_TURNING_OFF -> {
                            Log.e(
                                "BleReceiver",
                                "onReceive---------Bluetooth is turning off"
                            )
                        }

                        BluetoothAdapter.STATE_OFF -> {
                            Log.e("BleReceiver", "onReceive---------Bluetooth is turned off")
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

        Log.e("BleViewModel", "Device type: " + bluetoothDevice.type.toString())
        val label = when (bluetoothDevice.type) {
            1 -> "Classic Bluetooth"
            2 -> "LE Bluetooth"
            3 -> "Dual-mode Bluetooth"
            else -> "Unknown Bluetooth"
        }
        Toast.makeText(context, label, Toast.LENGTH_SHORT).show()

        var result = false
        val data: MutableMap<String, Any> = HashMap()
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if ((bluetoothDevice.type == BluetoothDevice.DEVICE_TYPE_DUAL || bluetoothDevice.type == BluetoothDevice.DEVICE_TYPE_CLASSIC)
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            ) {
                result = createBind(bluetoothDevice, BluetoothDevice.TRANSPORT_BREDR)
                Log.e("BleViewModel", "Dual-mode Bluetooth bind " + if (result) "successful" else "failed")
            } else if (bluetoothDevice.type == BluetoothDevice.DEVICE_TYPE_LE) {
                result = createBind(bluetoothDevice)
                Log.e("BleViewModel", "Classic Bluetooth bind " + if (result) "successful" else "failed")
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
            Log.e("BleViewModel", "Binding Dual-mode Bluetooth")
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