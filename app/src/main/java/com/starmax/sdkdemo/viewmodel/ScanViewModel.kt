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
        BleManager.getInstance().initScanRule(BleScanRuleConfig.Builder()
            .setScanTimeOut(10000)
            .build())
        BleManager.getInstance().scan(object :BleScanCallback(){
            override fun onScanStarted(success: Boolean) {

            }

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
                            broadcast[bleDevice.mac] = "支持一键双连"
                        }

                        Log.d("ScanVM","已找到广播")
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
                                } else if(rawData.slice(2 .. 3).toByteArray().contentEquals(byteArrayOf(
                                        0xBB.toByte(), 0xEE.toByte()
                                    ))){
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
                "蓝牙前缀:"+rawData.slice(4 until 6).toByteArray().toString(Charsets.US_ASCII) + "," +
                "蓝牙名称:"+rawData.slice(6 until 9).toByteArray().toString(Charsets.US_ASCII) + "," +
                "当前模式:"+ (if(rawData[9].toInt() == 0)  "正常模式" else "上课模式") + "," +
                "摇一摇次数:"+ (rawData[10].toInt() and 0xFF).toString()+ "," +
                "抬腕次数:"+rawData[11].toInt().toString() + "," +
                "心率:"+(rawData[12].toInt() and 0xFF).toString() + "," +
                "步数:"+Utils.byteArray2Sum(rawData.slice(13 until 16)).toString() + "," +
                "血压:"+((rawData[16].toInt() and 0xFF).toString())+"/"+((rawData[17].toInt() and 0xFF).toString())+ "," +
                "血氧:"+(rawData[18].toInt() and 0xFF).toString() + "," +
                "血糖:"+rawData[19].toInt().toString() + "," +
                "温度:"+Utils.byteArray2Sum(rawData.slice(20 until 22)).toString() + "," +
                "梅脱:"+(rawData[22].toInt() and 0xFF).toString() + "," +
                "MAI:"+(rawData[23].toInt() and 0xFF).toString() + "," +
                "压力:"+(rawData[24].toInt() and 0xFF).toString() + "," +
                "卡路里:"+Utils.byteArray2Sum(rawData.slice(25 until 28)).toString() + "," +
                "电量:"+(rawData[28].toInt() and 0xFF).toString();
    }

    fun get0x020106Str(rawData: ByteArray): String {
        return "时间戳：" + Utils.byteArray2Sum(rawData.slice(2 until 6)).toString() + "," +
                "步数：" + Utils.byteArray2Sum(rawData.slice(6 until 8)).toString() + "," +
                "卡路里:" + Utils.byteArray2Sum(rawData.slice(8 until 10)).toString() + "," +
                "睡眠:" + Utils.byteArray2Sum(rawData.slice(10 until 12)).toString() + "," +
                "心率:" + (rawData[12].toInt() and 0xFF).toString() + "," +
                "血压:" + ((rawData[13].toInt() and 0xFF).toString()) + "/" + ((rawData[14].toInt() and 0xFF).toString()) + "," +
                "血氧:" + (rawData[15].toInt() and 0xFF).toString() + "," +
                "血糖:" + rawData[16].toInt().toString() + "," +
                "体温:" + Utils.byteArray2Sum(rawData.slice(17 until 19)).toString() + "," +
                "电量:" + (rawData[19].toInt() and 0xFF).toString() + "," +
                "Mac地址:" + rawData.slice(20 until 26).map {
            String.format(
                "%02X",
                it
            )
        }.joinToString(":");
    }

    fun getFF0002AAEEStr(rawData: ByteArray) : String{
        return "SN:"+Utils.bytesToHex(rawData.slice(4 .. 6).toByteArray().reversedArray()) +","+
                "心率:"+(rawData[7].toInt() and 0xFF).toString() + "," +
                "步数:"+ Utils.byteArray2Sum(rawData.slice(8..10)).toString() + "," +
                "血压:"+ (rawData[11].toInt() and 0xFF).toString() + "/" + (rawData[12].toInt() and 0xFF).toString()  + "," +
                "血氧:"+rawData[13].toInt().toString() + "," +
                "血糖:"+rawData[14].toInt().toString() + "," +
                "温度:"+Utils.byteArray2Sum(rawData.slice(15..16)).toString() + "," +
                "梅脱:"+rawData[17].toInt().toString() + "," +
                "MAI:"+rawData[18].toInt().toString() + "," +
                "压力:"+rawData[19].toInt().toString() + "," +
                if(rawData.size > 20){
                    "卡路里:" +Utils.byteArray2Sum(rawData.slice(20..22)).toString() + ","+
                            "电量:" +rawData[23].toInt().toString()
                }else{
                    ""
                }
    }

    fun getFF0002AA55Str(rawData: ByteArray) : String{
        val PID = rawData.slice(4 .. 6).toByteArray()

        return "时间戳:X${String.format("%02d",PID[0].toInt())}M${String.format("%02d",PID[1].toInt())}T${String.format("%03d",PID[2].toInt())}"+","+
                "心率:"+(rawData[7].toInt() and 0xFF).toString() + "," +
                "步数:"+ Utils.byteArray2Sum(rawData.slice(8..10)).toString() + "," +
                "血压:"+ (rawData[11].toInt() and 0xFF).toString() + "/" + (rawData[12].toInt() and 0xFF).toString()  + "," +
                "血氧:"+rawData[13].toInt().toString() + "," +
                "血糖:"+rawData[14].toInt().toString() + "," +
                "温度:"+Utils.byteArray2Sum(rawData.slice(15..16)).toString() + "," +
                "梅脱:"+rawData[17].toInt().toString() + "," +
                "MAI:"+rawData[18].toInt().toString() + "," +
                "压力:"+rawData[19].toInt().toString() + "," +
                if(rawData.size > 20){
                    "卡路里:" +Utils.byteArray2Sum(rawData.slice(20..22)).toString() + ","+
                            "电量:" +rawData[23].toInt().toString()
                }else{
                    ""
                }
    }

    fun getFF0002BBEEStr(rawData: ByteArray) : String{
        return "SN:"+Utils.bytesToHex(rawData.slice(4 .. 6).toByteArray().reversedArray()) +","+
                "BATB:"+Utils.byteArray2Sum(rawData.slice(7 .. 9).toByteArray())+","+
                "电量等级:"+rawData[10].toInt().toString() + "," +
                "ADC:"+Utils.byteArray2Sum(rawData.slice(11 .. 13).toByteArray())+","+
                "电量:"+rawData[14].toInt().toString()
    }



    fun getFFAAEEStr(rawData: ByteArray) : String{
        return "SN:"+Utils.bytesToHex(rawData.slice(2 .. 4).toByteArray().reversedArray()) +","+
                "蓝牙名称:"+rawData.slice(5 .. 7).toByteArray().toString(Charsets.US_ASCII) + "," +
                "当前模式:"+ (if(rawData[8].toInt() == 0)  "正常模式" else "上课模式") + "," +
                "摇一摇次数:"+ (rawData[9].toInt() and 0xFF).toString()+ "," +
                "抬腕次数:"+rawData[10].toInt().toString() + "," +
                "心率:"+(rawData[11].toInt() and 0xFF).toString() + "," +
                "步数:"+Utils.byteArray2Sum(rawData.slice(12..14)).toString() + "," +
                "血压:"+((rawData[15].toInt() and 0xFF).toString())+"/"+((rawData[16].toInt() and 0xFF).toString())+ "," +
                "血氧:"+(rawData[17].toInt() and 0xFF).toString() + "," +
                "血糖:"+rawData[18].toInt().toString() + "," +
                "温度:"+Utils.byteArray2Sum(rawData.slice(19..20)).toString() + "," +
                "梅脱:"+(rawData[21].toInt() and 0xFF).toString() + "," +
                "MAI:"+(rawData[22].toInt() and 0xFF).toString() + "," +
                "压力:"+(rawData[23].toInt() and 0xFF).toString() + "," +
                "卡路里:"+Utils.byteArray2Sum(rawData.slice(24..26)).toString() + "," +
                "电量:"+(rawData[27].toInt() and 0xFF).toString()
    }

    fun getFFAAEE1Str(rawData: ByteArray) : String{
        return "X:"+Utils.byteArray2SumDesign(rawData.slice(2..3)) +","+
                "Y:"+ Utils.byteArray2SumDesign(rawData.slice(4..5)) + "," +
                "Z:"+ Utils.byteArray2SumDesign(rawData.slice(6..7)) + "," +
                "心率:"+ (rawData[8].toInt() and 0xFF).toString()+ "," +
                "血氧:"+rawData[9].toInt().toString() + "," +
                "温度:"+Utils.byteArray2Sum(rawData.slice(10..11)).toString() + "," +
                "电量:"+(rawData[12].toInt() and 0xFF).toString() + "," +
                "佩戴:"+(rawData[13].toInt() and 0xFF).toString() + "," +
                "步数:"+Utils.byteArray2Sum(rawData.slice(14..16)).toString() + "," +
                "卡路里:"+Utils.byteArray2Sum(rawData.slice(17..19)).toString()
    }

    fun getAAEEStr(rawData: ByteArray) : String{
        return "SN:"+Utils.bytesToHex(rawData.slice(2 .. 4).toByteArray().reversedArray()) +","+
                "蓝牙名称:"+rawData.slice(5 .. 7).toByteArray().toString(Charsets.US_ASCII) + "," +
                "当前模式:"+ (if(rawData[8].toInt() == 0)  "正常模式" else "上课模式") + "," +
                "摇一摇次数:"+ (rawData[9].toInt() and 0xFF).toString()+ "," +
                "抬腕次数:"+rawData[10].toInt().toString() + "," +
                "心率:"+(rawData[11].toInt() and 0xFF).toString() + "," +
                "步数:"+Utils.byteArray2Sum(rawData.slice(12..14)).toString() + "," +
                "血压:"+((rawData[15].toInt() and 0xFF).toString())+"/"+((rawData[16].toInt() and 0xFF).toString())+ "," +
                "血氧:"+(rawData[17].toInt() and 0xFF).toString() + "," +
                "血糖:"+rawData[18].toInt().toString() + "," +
                "温度:"+Utils.byteArray2Sum(rawData.slice(19..20)).toString() + "," +
                "梅脱:"+(rawData[21].toInt() and 0xFF).toString() + "," +
                "MAI:"+(rawData[22].toInt() and 0xFF).toString() + "," +
                "压力:"+(rawData[23].toInt() and 0xFF).toString() + "," +
                "卡路里:"+Utils.byteArray2Sum(rawData.slice(24..26)).toString() + "," +
                "电量:"+(rawData[27].toInt() and 0xFF).toString()
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
                "Packet ID:" + (rawData.slice(21..25).map {
            String.format(
                "0x%02X",
                it
            )
        }.joinToString(","))
    }


    fun getFFStartStr(rawData: ByteArray) : String{
        return "SN:"+Utils.bytesToHex(rawData.slice(1 until 4).toByteArray().reversedArray()) +","+
                "蓝牙前缀:"+rawData.slice(0 until 2).toByteArray().toString(Charsets.US_ASCII) + "," +
                "蓝牙名称:"+rawData.slice(2 until 5).toByteArray().toString(Charsets.US_ASCII) + "," +
                "当前模式:"+ (if(rawData[5].toInt() == 0)  "正常模式" else "上课模式") + "," +
                "摇一摇次数:"+ (rawData[6].toInt() and 0xFF).toString()+ "," +
                "抬腕次数:"+rawData[7].toInt().toString() + "," +
                "心率:"+(rawData[8].toInt() and 0xFF).toString() + "," +
                "步数:"+Utils.byteArray2Sum(rawData.slice(9 until 12)).toString() + "," +
                "血压:"+((rawData[12].toInt() and 0xFF).toString())+"/"+((rawData[13].toInt() and 0xFF).toString())+ "," +
                "血氧:"+(rawData[14].toInt() and 0xFF).toString() + "," +
                "血糖:"+rawData[15].toInt().toString() + "," +
                "温度:"+Utils.byteArray2Sum(rawData.slice(16 until 18)).toString() + "," +
                "梅脱:"+(rawData[18].toInt() and 0xFF).toString() + "," +
                "MAI:"+(rawData[19].toInt() and 0xFF).toString() + "," +
                "压力:"+(rawData[20].toInt() and 0xFF).toString() + "," +
                "卡路里:"+Utils.byteArray2Sum(rawData.slice(21 until 24)).toString() + "," +
                "电量:"+(rawData[24].toInt() and 0xFF).toString() + ","
    }
}