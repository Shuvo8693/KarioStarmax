package com.starmax.sdkdemo.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.scan.BleScanRuleConfig
import com.starmax.bluetoothsdk.Utils

class ScanViewModel() : ViewModel() {

    var devices: List<BleDevice> by mutableStateOf(emptyList())
    var deviceNames : HashMap<String,String> = hashMapOf()
    var broadcast: HashMap<String,String> = hashMapOf()
    var searchName: String by mutableStateOf("")
    var searchMac: String by mutableStateOf("")
    var isScanning: Boolean by mutableStateOf(false)

    fun getDeviceName(index:Int) : String {
        val name = devices[index].name;
        if(name != null){
            return name;
        }

        if(deviceNames.containsKey(devices[index].mac)){
            return deviceNames[devices[index].mac]!!
        }
        return "";
    }

    fun startScan() {
        val newDevices : MutableList<BleDevice> = mutableListOf()
        isScanning = true
        BleManager.getInstance().initScanRule(BleScanRuleConfig.Builder().setScanTimeOut(10000).build())
        BleManager.getInstance().scan(object :BleScanCallback(){
            override fun onScanStarted(success: Boolean) {}

            override fun onScanning(bleDevice: BleDevice?) {
                if(bleDevice != null && bleDevice.rssi >= -120 && bleDevice.name?.contains(searchName) == true && bleDevice.mac?.contains(searchMac) == true && devices.size <= 100){
                    var isChecked = true
                    var i = 0
                    while (i < bleDevice.scanRecord.size - 1){
                        val len = bleDevice.scanRecord[i].toInt()
                        val type = bleDevice.scanRecord[i + 1].toUByte()
                        val rawData = bleDevice.scanRecord.slice(i + 2 until i + 1 + len).toByteArray()
                        i += 1 + len

                        if(type == 0x01.toUByte() && len >= 1 && rawData.first() == 0x0A.toByte()){
                            broadcast[bleDevice.mac] = "Supports one-touch dual connection" // 支持一键双连 -> Supports one-touch dual connection
                        }

                        Log.d("ScanVM","Broadcast found") // 已找到广播 -> Broadcast found
                        println(rawData)


                        if(type == 0xFF.toUByte()){
                            val firstKey = rawData[0]
                            if(firstKey == 0xEE.toByte()){
                                broadcast[bleDevice.mac] = getFFEEStr(rawData)
                            }

                            val firstData = rawData.slice(0 .. 1).toByteArray()
                            if(firstData.contentEquals(byteArrayOf(0x00,0x01))){
                                isChecked = true
                            }else if(firstData.contentEquals(byteArrayOf(0x00,0x02))){
                                if(rawData.slice(2 .. 3).toByteArray().contentEquals(byteArrayOf(
                                        0xAA.toByte(), 0xEE.toByte()
                                    ))){
                                    broadcast[bleDevice.mac] = getFF0002AAEEStr(rawData)
                                } else if(rawData.slice(2 .. 3).toByteArray().contentEquals(byteArrayOf(0xBB.toByte(), 0xEE.toByte()))){
                                    broadcast[bleDevice.mac] = getFF0002BBEEStr(rawData)
                                } else if(rawData.slice(2 .. 3).toByteArray().contentEquals(byteArrayOf(
                                        0xAA.toByte(), 0x55.toByte()
                                    ))){
                                    broadcast[bleDevice.mac] = getFF0002AA55Str(rawData)
                                }
                            } else if(firstData.contentEquals(byteArrayOf(0xAA.toByte(),0xEE.toByte()))){
                                if(rawData.size == 20){
                                    broadcast[bleDevice.mac] = getFFAAEE1Str(rawData)
                                }else{
                                    broadcast[bleDevice.mac] = getFFAAEEStr(rawData)
                                }
                            } else if(firstData.contentEquals(byteArrayOf(0xA2.toByte(),0x0A.toByte()))){
                                broadcast[bleDevice.mac] = getFFA20AStr(rawData)
                            } else if(firstData.contentEquals(byteArrayOf(0x55.toByte(),0xAA.toByte()))){
                                broadcast[bleDevice.mac] = get0x020106Str(rawData)
                            } else if(rawData.size >= 25){
                                broadcast[bleDevice.mac] = getFFStartStr(rawData)
                            }
                        }else if(type == 0xAA.toUByte()){
                            broadcast[bleDevice.mac] = getFFEEStr(rawData)
                        }
                    }

                    if(isChecked){
                        deviceNames[bleDevice.mac] = bleDevice.name
                        newDevices.add(bleDevice)
                    }

                    devices = newDevices.toList()
                }
            }

            override fun onScanFinished(scanResultList: MutableList<BleDevice>?) {
                isScanning = false
            }

        })
    }

    fun stopScan(){
        if(isScanning){
            BleManager.getInstance().cancelScan()
        }
    }

    fun getFFEEStr(rawData: ByteArray) : String {
        return  "SN:"+Utils.bytesToHex(rawData.slice(1 until 4).toByteArray().reversedArray()) +","+
                "Bluetooth Prefix:"+rawData.slice(4 until 6).toByteArray().toString(Charsets.US_ASCII) + "," + // 蓝牙前缀: -> Bluetooth Prefix:
                "Bluetooth Name:"+rawData.slice(6 until 9).toByteArray().toString(Charsets.US_ASCII) + "," + // 蓝牙名称: -> Bluetooth Name:
                "Current Mode:"+ (if(rawData[9].toInt() == 0)  "Normal Mode" else "Class Mode") + "," + // 当前模式:, 正常模式 -> Normal Mode, 上课模式 -> Class Mode
                "Shake Count:"+ (rawData[10].toInt() and 0xFF).toString()+ "," + // 摇一摇次数: -> Shake Count:
                "Raise Wrist Count:"+rawData[11].toInt().toString() + "," + // 抬腕次数: -> Raise Wrist Count:
                "Heart Rate:"+(rawData[12].toInt() and 0xFF).toString() + "," + // 心率: -> Heart Rate:
                "Steps:"+Utils.byteArray2Sum(rawData.slice(13 until 16)).toString() + "," + // 步数: -> Steps:
                "Blood Pressure:"+((rawData[16].toInt() and 0xFF).toString())+"/"+((rawData[17].toInt() and 0xFF).toString())+ "," + // 血压: -> Blood Pressure:
                "Blood Oxygen:"+(rawData[18].toInt() and 0xFF).toString() + "," + // 血氧: -> Blood Oxygen:
                "Blood Sugar:"+rawData[19].toInt().toString() + "," + // 血糖: -> Blood Sugar:
                "Temperature:"+Utils.byteArray2Sum(rawData.slice(20 until 22)).toString() + "," + // 温度: -> Temperature:
                "MET:"+ (rawData[22].toInt() and 0xFF).toString() + "," + // 梅脱: -> MET:
                "MAI:"+(rawData[23].toInt() and 0xFF).toString() + "," +
                "Pressure:"+(rawData[24].toInt() and 0xFF).toString() + "," + // 压力: -> Pressure:
                "Calories:"+Utils.byteArray2Sum(rawData.slice(25 until 28)).toString() + "," + // 卡路里: -> Calories:
                "Battery Level:"+(rawData[28].toInt() and 0xFF).toString(); // 电量: -> Battery Level:
    }

    fun get0x020106Str(rawData: ByteArray): String {
        return "Timestamp：" + Utils.byteArray2Sum(rawData.slice(2 until 6)).toString() + "," + // 时间戳： -> Timestamp：
                "Steps：" + Utils.byteArray2Sum(rawData.slice(6 until 8)).toString() + "," + // 步数： -> Steps：
                "Calories:" + Utils.byteArray2Sum(rawData.slice(8 until 10)).toString() + "," + // 卡路里: -> Calories:
                "Sleep:" + Utils.byteArray2Sum(rawData.slice(10 until 12)).toString() + "," + // 睡眠: -> Sleep:
                "Heart Rate:" + (rawData[12].toInt() and 0xFF).toString() + "," + // 心率: -> Heart Rate:
                "Blood Pressure:" + ((rawData[13].toInt() and 0xFF).toString()) + "/" + ((rawData[14].toInt() and 0xFF).toString()) + "," + // 血压: -> Blood Pressure:
                "Blood Oxygen:" + (rawData[15].toInt() and 0xFF).toString() + "," + // 血氧: -> Blood Oxygen:
                "Blood Sugar:" + rawData[16].toInt().toString() + "," + // 血糖: -> Blood Sugar:
                "Body Temperature:" + Utils.byteArray2Sum(rawData.slice(17 until 19)).toString() + "," + // 体温: -> Body Temperature:
                "Battery Level:" + (rawData[19].toInt() and 0xFF).toString() + "," + // 电量: -> Battery Level:
                "Mac Address:" + rawData.slice(20 until 26)
            .joinToString(":") { // Mac地址: -> Mac Address:
                String.format(
                    "%02X",
                    it
                )
            };
    }

    fun getFF0002AAEEStr(rawData: ByteArray) : String{
        return "SN:"+Utils.bytesToHex(rawData.slice(4 .. 6).toByteArray().reversedArray()) +","+
                "Heart Rate:"+(rawData[7].toInt() and 0xFF).toString() + "," + // 心率: -> Heart Rate:
                "Steps:"+ Utils.byteArray2Sum(rawData.slice(8..10)).toString() + "," + // 步数: -> Steps:
                "Blood Pressure:"+ (rawData[11].toInt() and 0xFF).toString() + "/" + (rawData[12].toInt() and 0xFF).toString()  + "," + // 血压: -> Blood Pressure:
                "Blood Oxygen:"+rawData[13].toInt().toString() + "," + // 血氧: -> Blood Oxygen:
                "Blood Sugar:"+rawData[14].toInt().toString() + "," + // 血糖: -> Blood Sugar:
                "Temperature:"+Utils.byteArray2Sum(rawData.slice(15..16)).toString() + "," + // 温度: -> Temperature:
                "MET:"+rawData[17].toInt().toString() + "," + // 梅脱: -> MET:
                "MAI:"+rawData[18].toInt().toString() + "," +
                "Pressure:"+rawData[19].toInt().toString() + "," + // 压力: -> Pressure:
                if(rawData.size > 20){
                    "Calories:" +Utils.byteArray2Sum(rawData.slice(20..22)).toString() + ","+ // 卡路里: -> Calories:
                            "Battery Level:" +rawData[23].toInt().toString() // 电量: -> Battery Level:
                }else{
                    ""
                }
    }

    fun getFF0002AA55Str(rawData: ByteArray) : String{
        val PID = rawData.slice(4 .. 6).toByteArray()

        return "Timestamp:X${String.format("%02d",PID[0].toInt())}M${String.format("%02d",PID[1].toInt())}T${String.format("%03d",PID[2].toInt())}"+","+ // 时间戳: -> Timestamp:
                "Heart Rate:"+(rawData[7].toInt() and 0xFF).toString() + "," + // 心率: -> Heart Rate:
                "Steps:"+ Utils.byteArray2Sum(rawData.slice(8..10)).toString() + "," + // 步数: -> Steps:
                "Blood Pressure:"+ (rawData[11].toInt() and 0xFF).toString() + "/" + (rawData[12].toInt() and 0xFF).toString()  + "," + // 血压: -> Blood Pressure:
                "Blood Oxygen:"+rawData[13].toInt().toString() + "," + // 血氧: -> Blood Oxygen:
                "Blood Sugar:"+rawData[14].toInt().toString() + "," + // 血糖: -> Blood Sugar:
                "Temperature:"+Utils.byteArray2Sum(rawData.slice(15..16)).toString() + "," + // 温度: -> Temperature:
                "MET:"+rawData[17].toInt().toString() + "," + // 梅脱: -> MET:
                "MAI:"+rawData[18].toInt().toString() + "," +
                "Pressure:"+rawData[19].toInt().toString() + "," + // 压力: -> Pressure:
                if(rawData.size > 20){
                    "Calories:" +Utils.byteArray2Sum(rawData.slice(20..22)).toString() + ","+ // 卡路里: -> Calories:
                            "Battery Level:" +rawData[23].toInt().toString() // 电量: -> Battery Level:
                }else{
                    ""
                }
    }

    fun getFF0002BBEEStr(rawData: ByteArray) : String{
        return "SN:"+Utils.bytesToHex(rawData.slice(4 .. 6).toByteArray().reversedArray()) +","+
                "BATB:"+Utils.byteArray2Sum(rawData.slice(7 .. 9).toByteArray())+","+
                "Battery Level Grade:"+rawData[10].toInt().toString() + "," + // 电量等级: -> Battery Level Grade:
                "ADC:"+Utils.byteArray2Sum(rawData.slice(11 .. 13).toByteArray())+","+
                "Battery Level:"+rawData[14].toInt().toString() // 电量: -> Battery Level:
    }



    fun getFFAAEEStr(rawData: ByteArray) : String{
        return "SN:"+Utils.bytesToHex(rawData.slice(2 .. 4).toByteArray().reversedArray()) +","+
                "Bluetooth Name:"+rawData.slice(5 .. 7).toByteArray().toString(Charsets.US_ASCII) + "," + // 蓝牙名称: -> Bluetooth Name:
                "Current Mode:"+ (if(rawData[8].toInt() == 0)  "Normal Mode" else "Class Mode") + "," + // 当前模式:, 正常模式 -> Normal Mode, 上课模式 -> Class Mode
                "Shake Count:"+ (rawData[9].toInt() and 0xFF).toString()+ "," + // 摇一摇次数: -> Shake Count:
                "Raise Wrist Count:"+rawData[10].toInt().toString() + "," + // 抬腕次数: -> Raise Wrist Count:
                "Heart Rate:"+(rawData[11].toInt() and 0xFF).toString() + "," + // 心率: -> Heart Rate:
                "Steps:"+Utils.byteArray2Sum(rawData.slice(12..14)).toString() + "," + // 步数: -> Steps:
                "Blood Pressure:"+((rawData[15].toInt() and 0xFF).toString())+"/"+((rawData[16].toInt() and 0xFF).toString())+ "," + // 血压: -> Blood Pressure:
                "Blood Oxygen:"+(rawData[17].toInt() and 0xFF).toString() + "," + // 血氧: -> Blood Oxygen:
                "Blood Sugar:"+rawData[18].toInt().toString() + "," + // 血糖: -> Blood Sugar:
                "Temperature:"+Utils.byteArray2Sum(rawData.slice(19..20)).toString() + "," + // 温度: -> Temperature:
                "MET:"+(rawData[21].toInt() and 0xFF).toString() + "," + // 梅脱: -> MET:
                "MAI:"+(rawData[22].toInt() and 0xFF).toString() + "," +
                "Pressure:"+(rawData[23].toInt() and 0xFF).toString() + "," + // 压力: -> Pressure:
                "Calories:"+Utils.byteArray2Sum(rawData.slice(24..26)).toString() + "," + // 卡路里: -> Calories:
                "Battery Level:"+(rawData[27].toInt() and 0xFF).toString() // 电量: -> Battery Level:
    }

    fun getFFAAEE1Str(rawData: ByteArray) : String{
        return "X:"+Utils.byteArray2SumDesign(rawData.slice(2..3)) +","+
                "Y:"+ Utils.byteArray2SumDesign(rawData.slice(4..5)) + "," +
                "Z:"+ Utils.byteArray2SumDesign(rawData.slice(6..7)) + "," +
                "Heart Rate:"+ (rawData[8].toInt() and 0xFF).toString()+ "," + // 心率: -> Heart Rate:
                "Blood Oxygen:"+rawData[9].toInt().toString() + "," + // 血氧: -> Blood Oxygen:
                "Temperature:"+Utils.byteArray2Sum(rawData.slice(10..11)).toString() + "," + // 温度: -> Temperature:
                "Battery Level:"+(rawData[12].toInt() and 0xFF).toString() + "," + // 电量: -> Battery Level:
                "Wearing Status:"+(rawData[13].toInt() and 0xFF).toString() + "," + // 佩戴: -> Wearing Status:
                "Steps:"+Utils.byteArray2Sum(rawData.slice(14..16)).toString() + "," + // 步数: -> Steps:
                "Calories:"+Utils.byteArray2Sum(rawData.slice(17..19)).toString() // 卡路里: -> Calories:
    }

    fun getAAEEStr(rawData: ByteArray) : String{
        return "SN:"+Utils.bytesToHex(rawData.slice(2 .. 4).toByteArray().reversedArray()) +","+
                "Bluetooth Name:"+rawData.slice(5 .. 7).toByteArray().toString(Charsets.US_ASCII) + "," + // 蓝牙名称: -> Bluetooth Name:
                "Current Mode:"+ (if(rawData[8].toInt() == 0)  "Normal Mode" else "Class Mode") + "," + // 当前模式:, 正常模式 -> Normal Mode, 上课模式 -> Class Mode
                "Shake Count:"+ (rawData[9].toInt() and 0xFF).toString()+ "," + // 摇一摇次数: -> Shake Count:
                "Raise Wrist Count:"+rawData[10].toInt().toString() + "," + // 抬腕次数: -> Raise Wrist Count:
                "Heart Rate:"+(rawData[11].toInt() and 0xFF).toString() + "," + // 心率: -> Heart Rate:
                "Steps:"+Utils.byteArray2Sum(rawData.slice(12..14)).toString() + "," + // 步数: -> Steps:
                "Blood Pressure:"+((rawData[15].toInt() and 0xFF).toString())+"/"+((rawData[16].toInt() and 0xFF).toString())+ "," + // 血压: -> Blood Pressure:
                "Blood Oxygen:"+(rawData[17].toInt() and 0xFF).toString() + "," + // 血氧: -> Blood Oxygen:
                "Blood Sugar:"+rawData[18].toInt().toString() + "," + // 血糖: -> Blood Sugar:
                "Temperature:"+Utils.byteArray2Sum(rawData.slice(19..20)).toString() + "," + // 温度: -> Temperature:
                "MET:"+(rawData[21].toInt() and 0xFF).toString() + "," + // 梅脱: -> MET:
                "MAI:"+(rawData[22].toInt() and 0xFF).toString() + "," +
                "Pressure:"+(rawData[23].toInt() and 0xFF).toString() + "," + // 压力: -> Pressure:
                "Calories:"+Utils.byteArray2Sum(rawData.slice(24..26)).toString() + "," + // 卡路里: -> Calories:
                "Battery Level:"+(rawData[27].toInt() and 0xFF).toString() // 电量: -> Battery Level:
    }

    fun getFFA20AStr(rawData: ByteArray) : String{
        var repeatsData = rawData[8].toInt() and 0xFF
        val repeats = IntArray(8)
        for( h in 0..7){
            repeats.set(h,repeatsData % 2)
            repeatsData /= 2
        }

        val accelermeterOrinentationField = repeats[4] + (repeats[5] shl 1) + (repeats[6] shl 2)

        return "Care Bloom Product Type:" + (if(rawData[3] == 0x02.toByte()) "PD200" else "S2201") + "\n" +
                "Firmware Version: ${rawData[4].toInt() and 0x0F}.${rawData[5].toInt() shr 4 and 0x0F}.${rawData[5].toInt() and 0x0F}\n" +
                "BLE transmit power level:${rawData[6]}\n" +
                "Battery level indicator: ${rawData[7]}%\n" +
                "Accelerometer is still:${repeats[0]}\n" +
                "(reserved):${repeats[1]}\n" +
                "1 for 30 seconds when PD200 is put on:${repeats[2]}\n" +
                "1 when PD200 is not being worn,0 otherwise:${repeats[3]}\n" +
                "Accelerometer orientation field:" + when(accelermeterOrinentationField){
            0 -> "Unknown orientation"
            1 -> "Free fall"
            2 -> "X-axis pointing up"
            3 -> "X-axis pointing down"
            4 -> "Y-axis pointing up"
            5 -> "Y-axis pointing down"
            6 -> "Z-axis pointing up"
            7 -> "Z-axis pointing down"
            else -> "Unknown orientation"
        } + "\n" +
                "1 during help request broadcast,0 otherwise:${repeats[7]}\n"+
                "Composite acceleration:" + Utils.byteArray2Sum(rawData.slice(9..10).reversed()) + "\n"+
                "(reserved):" + rawData[11] + "\n" +
                "(reserved):" + rawData[12] + "\n" +
                "Heart beats:" + (rawData[13].toInt() and 0xFF) + "\n" +
                "Blood Pressure: ${rawData[14].toInt() and 0xFF }/${rawData[15].toInt() and 0xFF}\n" +
                "Blood oxygen saturation: ${rawData[16].toInt() and 0xFF}\n" +
                "Skin temperature: ${rawData[17].toInt() and 0xFF}\n" +
                "Sleep Hours x 10:${rawData[18].toInt() and 0xFF}\n" +
                "Steps:" + Utils.byteArray2Sum(rawData.slice(19..20).reversed()) + "\n" +
                "Packet ID:" + (rawData.slice(21..25).joinToString(",") {
            String.format(
                "0x%02X",
                it
            )
        })
    }


    fun getFFStartStr(rawData: ByteArray) : String{
        return "SN:"+Utils.bytesToHex(rawData.slice(1 until 4).toByteArray().reversedArray()) +","+
                "Bluetooth Prefix:"+rawData.slice(0 until 2).toByteArray().toString(Charsets.US_ASCII) + "," + // 蓝牙前缀: -> Bluetooth Prefix:
                "Bluetooth Name:"+rawData.slice(2 until 5).toByteArray().toString(Charsets.US_ASCII) + "," + // 蓝牙名称: -> Bluetooth Name:
                "Current Mode:"+ (if(rawData[5].toInt() == 0)  "Normal Mode" else "Class Mode") + "," + // 当前模式:, 正常模式 -> Normal Mode, 上课模式 -> Class Mode
                "Shake Count:"+ (rawData[6].toInt() and 0xFF).toString()+ "," + // 摇一摇次数: -> Shake Count:
                "Raise Wrist Count:"+rawData[7].toInt().toString() + "," + // 抬腕次数: -> Raise Wrist Count:
                "Heart Rate:"+(rawData[8].toInt() and 0xFF).toString() + "," + // 心率: -> Heart Rate:
                "Steps:"+Utils.byteArray2Sum(rawData.slice(9 until 12)).toString() + "," + // 步数: -> Steps:
                "Blood Pressure:"+((rawData[12].toInt() and 0xFF).toString())+"/"+((rawData[13].toInt() and 0xFF).toString())+ "," + // 血压: -> Blood Pressure:
                "Blood Oxygen:"+(rawData[14].toInt() and 0xFF).toString() + "," + // 血氧: -> Blood Oxygen:
                "Blood Sugar:"+rawData[15].toInt().toString() + "," + // 血糖: -> Blood Sugar:
                "Temperature:"+Utils.byteArray2Sum(rawData.slice(16 until 18)).toString() + "," + // 温度: -> Temperature:
                "MET:"+(rawData[18].toInt() and 0xFF).toString() + "," + // 梅脱: -> MET:
                "MAI:"+(rawData[19].toInt() and 0xFF).toString() + "," +
                "Pressure:"+(rawData[20].toInt() and 0xFF).toString() + "," + // 压力: -> Pressure:
                "Calories:"+Utils.byteArray2Sum(rawData.slice(21 until 24)).toString() + "," + // 卡路里: -> Calories:
                "Battery Level:"+(rawData[24].toInt() and 0xFF).toString() + "," // 电量: -> Battery Level:
    }
}