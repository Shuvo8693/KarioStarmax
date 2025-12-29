package com.starmax.sdkdemo.viewmodel

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.collections.filterValues

class InstructionListViewModel() : ViewModel() {

//    val instructionMap = mapOf(
//        "OTA" to listOf("瑞昱ota升级(本地)","博通ota升级(本地)", "杰理ota升级(本地)","X03差分升级(本地)"),
//        "同步" to listOf("同步摇头记录","同步心率记录","同步站立次数中高强度","同步血压记录","同步呼吸率记录"),
//        "获取数据" to listOf("获取喝水提醒","获取运动模式","获取手表模式"),
//        "其他" to listOf()
//    )


    var searchName: String by mutableStateOf("")

    var instructionMap by mutableStateOf(
        mapOf(
            "OTA" to listOf(
                "Connect OTA Bluetooth",
//                "Realtek OTA Upgrade (Local)",
//                "Broadcom OTA Upgrade (Local)",
                "Jieli OTA Upgrade (Local)",
//                "X03 Differential Upgrade (Local)",
//                "Realtek OTA Upgrade",
//                "Broadcom OTA Upgrade",
//                "Jieli Upgrade",
//                "Sifli OTA Upgrade (Local)",
//                "Send UI (Local)",
//                "Send UI (Differential Upgrade)",
//                "Differential Upgrade CRC Check (Local)",
//                "Send File System",
//                "Sifli App Download (Local)"
            ),
            "Sync" to listOf(
//                "Sync Head Shaking Record",
                "Sync Heart Rate Record",
//                "Sync Standing Count Medium-High Intensity",
                "Sync Blood Pressure Record",
                "Sync Respiration Rate Record",
                "Sync Blood Oxygen Record",
                "Sync Stress Record",
                "Sync MET Record",
                "Sync Temperature Record",
                "Sync MAI",
                "Sync Blood Sugar",
                "Sync Sleep Record",
//                "Sync Raw Sleep Data",
//                "Sync Step Count Valid Dates",
//                "Sync Sleep Valid Dates",
//                "Sync MET Valid Dates",
//                "Sync Mai Valid Dates",
//                "Sync Blood Sugar Valid Dates",
//                "Sync Blood Oxygen Valid Dates",
//                "Sync Head Shaking Valid Dates",
                "Sync Sport",
                "Sync Sport Record",
//                "Sync Step Count Sleep Record"
            ),
            "Get Data" to listOf(
                "Get Drink Water Reminder",
                "Get Sport Mode",
//                "Get Watch Mode",
                "Get Goals",
                "Get User Info",
//                "Get NFC Card Info",
//                "Get BT Status",
//                "Get Power",
                "Get Version Info",
                "Get Supported Languages",
                "Get Time Zone",
//                "Get Daylight Saving Time",
//                "Get Current Health Data",
//                "Get Watch Name",
//                "Get Watch Daily Data",
//                "Get Watch Vibration Duration",
//                "Get Shake Times",
//                "Get Shake On/Off Switch",
//                "Get Broadcast Interval",
//                "Get File System",
//                "Get Dial Info",
//                "Get Log",
//                "Get Alarm Clock",
//                "Get Sedentary Reminder",
//                "Get Power File",
//                "Get Sensor File",
//                "Get Sleep Plan",
//                "Jieli Log Get"
            ),

            "Settings" to listOf(
//                "Set Server",
//                "Set Status",
                "Set Time Zone",
//                "Set Daylight Saving Time",
                "Set User Info",
                "Set Daily Goal",
//                "Set Frequent Contacts",
//                "Set Emergency Contacts",
                "Set Do Not Disturb",
                "Set Sleep Plan",
                "Set Sport Mode",
//                "Set Alarm Clock",
//                "Set Password",
//                "Set Female Health",
                "Set Sedentary Reminder",
                "Set Drink Water Reminder",
//                "Event Reminder",
//                "Set NFC Card",
//                "Bluetooth Broadcast Data Update On",
//                "Bluetooth Broadcast Data Update Off"
            ),
            "Control" to listOf(
//                "Keep Foreground Running",
//                "Disconnect Foreground Running",
                "Volume",
                "Pair Device",
//                "Pair Command",
//                "Unbind Command",
//                "Unbind Detection",
//                "Pair Command (GTS10) Pop-up",
//                "Pair Command (GTS10) No Pop-up",
//                "GTS10 Two-way Pairing",
                "Find Device",
                "Stop Finding",
                "Camera Control",
                "Call Control",
//                "Sync Time",
//                "Send Message",
//                "App Store",
//                "World Clock",
                "Time Format",
//                "Power Consumption Mode",
//                "Unit Test",
//                "Restore Factory Settings",
                "Shut Down",
            ),
            "Switch" to listOf(
//                "Open Heart Rate Switch",
                "GTS10 Heart Rate Interval",
//                "Enable Pressure Measurement",
//                "Disable Pressure Measurement",
//                "Enable Heart Rate Measurement",
//                "Disable Heart Rate Measurement",
//                "Enable Demo Mode",
//                "Disable Demo Mode",
//                "Shipping Mode",
//                "Enable Shipping Mode",
//                "Disable Shipping Mode",
//                "Custom Switch",
//                "Sport Mode On/Off Switch",
//                "Detection Switch",
//                "Real-time Data Switch"
            ),
            "Weather" to listOf(
//                "Send Weather (4 Days)",
                "Send Weather (7 Days)",
                "Read Weather (7 Days)",
//                "Send Ephemeris (Domestic)",
//                "Send Ephemeris (Foreign)"
            ),
//            "Dial" to listOf(
//                "Send Custom Dial",
//                "Send Custom Dial V2",
//                "Send Local Dial",
//                "Switch Dial",
//                "Send Dial V2"
//            ),

//            "Other" to listOf(
//                "Read Signal Strength",
//                "Saiwei Algorithm",
//                "Blood Sugar Calibration",
//                "Blood Pressure Calibration",
//                "Morning and Evening",
//                "Steps Not Reached",
//                "Health Prescription",
//                "Morning and Evening Valid Dates List",
//                "Prescription Valid Dates List",
//                "Get Health Prescription History",
//                "Get Morning and Evening History",
//                "Send Local Logo",
//                "Clear Logo",
//            )
        )
    )
    var keepInstructionMap by mutableStateOf(instructionMap.toMap())


   /* var instructionMap by mutableStateOf(
        mapOf(
            "OTA" to listOf(
                "Connect OTA Bluetooth",
                "Realtek OTA Upgrade (Local)",
                "Broadcom OTA Upgrade (Local)",
                "Jieli OTA Upgrade (Local)",
                "X03 Differential Upgrade (Local)",
                "Realtek OTA Upgrade",
                "Broadcom OTA Upgrade",
                "Jieli Upgrade",
                "Sifli OTA Upgrade (Local)",
                "Send UI (Local)",
                "Send UI (Differential Upgrade)",
                "Differential Upgrade CRC Check (Local)",
                "Send File System",
                "Sifli App Download (Local)"
            ),
            "Sync" to listOf(
                "Sync Head Shaking Record",
                "Sync Heart Rate Record",
                "Sync Standing Count Medium-High Intensity",
                "Sync Blood Pressure Record",
                "Sync Respiration Rate Record",
                "Sync Blood Oxygen Record",
                "Sync Pressure Record",
                "Sync MET Record",
                "Sync Temperature Record",
                "Sync MAI",
                "Sync Blood Sugar",
                "Sync Sleep Record",
                "Sync Raw Sleep Data",
                "Sync Step Count Valid Dates",
                "Sync Sleep Valid Dates",
                "Sync MET Valid Dates",
                "Sync Mai Valid Dates",
                "Sync Blood Sugar Valid Dates",
                "Sync Blood Oxygen Valid Dates",
                "Sync Head Shaking Valid Dates",
                "Sync Sport",
                "Sync Sport Record",
                "Sync Step Count Sleep Record"
            ),
            "Get Data" to listOf(
                "Get Drink Water Reminder",
                "Get Sport Mode",
                "Get Watch Mode",
                "Get NFC Card Info",
                "Get BT Status",
                "Get Power",
                "Get Version Info",
                "Get Supported Languages",
                "Get Time Zone",
                "Get Daylight Saving Time",
                "Get Current Health Data",
                "Get Watch Name",
                "Get Watch Daily Data",
                "Get Watch Vibration Duration",
                "Get Shake Times",
                "Get Shake On/Off Switch",
                "Get Broadcast Interval",
                "Get File System",
                "Get Dial Info",
                "Get Log",
                "Get Alarm Clock",
                "Get Sedentary Reminder",
                "Get Power File",
                "Get Sensor File",
                "Get Sleep Plan",
                "Jieli Log Get"
            ),
            "Settings" to listOf(
                "Set Server",
                "Set Status",
                "Set Time Zone",
                "Set Daylight Saving Time",
                "Set User Info",
                "Set Daily Goal",
                "Set Frequent Contacts",
                "Set Emergency Contacts",
                "Set Do Not Disturb",
                "Set Sleep Plan",
                "Set Sport Mode",
                "Set Password",
                "Set Female Health",
                "Set Sedentary Reminder",
                "Set Drink Water Reminder",
                "Event Reminder",
                "Set NFC Card",
                "Bluetooth Broadcast Data Update On",
                "Bluetooth Broadcast Data Update Off"
            ),
            "Control" to listOf(
                "Keep Foreground Running",
                "Disconnect Foreground Running",
                "Volume",
                "Pair Device",
                "Pair Command",
                "Unbind Command",
                "Unbind Detection",
                "Pair Command (GTS10) Pop-up",
                "Pair Command (GTS10) No Pop-up",
                "GTS10 Two-way Pairing",
                "Find Device",
                "Stop Finding",
                "Camera Control",
                "Call Control",
                "Sync Time",
                "Send Message",
                "App Store",
                "World Clock",
                "Time Format",
                "Power Consumption Mode",
                "Unit Test",
                "Restore Factory Settings",
                "Shut Down",
            ),
            "Switch" to listOf(
                "Open Heart Rate Switch",
                "GTS10 Heart Rate Interval",
                "Enable Pressure Measurement",
                "Disable Pressure Measurement",
                "Enable Heart Rate Measurement",
                "Disable Heart Rate Measurement",
                "Enable Demo Mode",
                "Disable Demo Mode",
                "Shipping Mode",
                "Enable Shipping Mode",
                "Disable Shipping Mode",
                "Custom Switch",
                "Sport Mode On/Off Switch",
                "Detection Switch",
                "Real-time Data Switch"
            ),
            "Weather" to listOf(
                "Send Weather (4 Days)",
                "Read Weather (7 Days)",
                "Send Weather (7 Days)",
                "Send Ephemeris (Domestic)",
                "Send Ephemeris (Foreign)"
            ),
            "Dial" to listOf(
                "Send Custom Dial",
                "Send Custom Dial V2",
                "Send Local Dial",
                "Switch Dial",
                "Send Dial V2"
            ),
            "Other" to listOf(
                "Set Alarm Clock",
                "Read Signal Strength",
                "Saiwei Algorithm",
                "Blood Sugar Calibration",
                "Blood Pressure Calibration",
                "Morning and Evening",
                "Steps Not Reached",
                "Health Prescription",
                "Morning and Evening Valid Dates List",
                "Prescription Valid Dates List",
                "Get Health Prescription History",
                "Get Morning and Evening History",
                "Send Local Logo",
                "Clear Logo",
            )
        )
    )*/
    /* var instructionMap by mutableStateOf(mapOf(
         "OTA" to listOf("连接OTA蓝牙", "瑞昱ota升级(本地)", "博通ota升级(本地)", "杰理ota升级(本地)", "X03差分升级(本地)",
             "瑞昱ota升级", "博通ota升级", "杰理升级", "思澈ota升级(本地)","发送ui(本地)","发送ui(差分升级)", "差分升级CRC校验(本地)",
             "发送文件系统","思澈应用下载(本地)"
         ),
         "同步" to listOf("同步摇头记录", "同步心率记录", "同步站立次数中高强度", "同步血压记录", "同步呼吸率记录", "同步血氧记录",
             "同步压力记录", "同步梅脱记录", "同步温度记录", "同步MAI", "同步血糖", "同步睡眠记录", "同步睡眠原始数据",
             "同步步数有效日期", "同步睡眠有效日期", "同步梅脱有效日期", "同步Mai有效日期", "同步血糖有效日期", "同步血氧有效日期",
             "同步摇头有效日期", "同步运动", "同步运动记录", "同步记步睡眠记录"
         ),
         "获取数据" to listOf("获取喝水提醒", "获取运动模式", "获取手表模式", "获取NFC卡片信息", "获取BT状态", "获取电量",
             "获取版本信息", "获取支持语言", "获取时区", "获取夏令时", "获取当前健康数据","获取手表名称", "获取手表日常数据", "获取手表震动时长",
             "获取摇一摇次数", "获取摇一摇开关", "获取广播间隔", "获取文件系统", "获取表盘信息", "获取log","获取闹钟","获取久坐提醒",
             "获取电量文件","获取sensor文件","获取睡眠计划","杰理日志获取"
         ),
         "设置" to listOf("设置服务器", "设置状态", "设置时区", "设置夏令时", "设置用户信息", "设置一天目标", "设置常用联系人",
             "设置紧急联系人", "设置勿扰", "设置睡眠计划", "设置运动模式", "设置密码", "设置女性健康", "设置久坐提醒",
             "设置喝水提醒", "事件提醒","设置NFC卡片","蓝牙广播数据更新打开","蓝牙广播数据更新关闭"
         ),
         "控制" to listOf(
             "保持前台运行", "断开前台运行", "音量","配对设备", "配对指令","解绑指令","解绑检测","配对指令(GTS10)弹出", "配对指令(GTS10)不弹出","GTS10双向配对", "查找设备",
             "停止查找", "拍照控制", "电话控制", "同步时间", "发送消息", "应用商店", "世界时钟", "时间制式", "耗电模式", "单元测试",
             "恢复出厂设置", "关机",
         ),
         "开关" to listOf(
             "打开心率开关", "GTS10心率间隔", "开启压力测量", "关闭压力测量", "开启心率测量", "关闭心率测量", "开启演示模式",
             "关闭演示模式","船运模式", "开启船运模式", "关闭船运模式", "自定义开关", "运动模式开关","检测开关","实时数据开关"
         ),
         "天气" to listOf("发送天气(4天)", "读取天气(7天)", "发送天气(7天)", "发送星历(国内)", "发送星历(国外)"
         ),
         "表盘" to listOf("发送自定义表盘", "发送自定义表盘V2", "发送本地表盘", "切换表盘","发送表盘V2"
         ),
         "其他" to listOf( "设置闹钟", "读取信号强度", "赛维算法", "血糖校准", "血压校准", "朝朝暮暮", "步数未达标",
             "健康处方", "朝朝暮暮有效日期列表", "处方有效日期列表", "获取健康处方历史", "获取朝朝暮暮历史", "发送本地logo",
             "清除logo",
         )
     ))*/

/*    val instructionList = listOf(
        "设置服务器", "保持前台运行", "断开前台运行", "音量", "配对指令", "配对指令(GTS10)弹出", "配对指令(GTS10)不弹出",
        "获取NFC卡片信息", "设置NFC卡片", "自定义开关", "运动模式开关", "获取BT状态", "配对设备", "设置状态", "查找设备",
        "停止查找", "拍照控制", "电话控制", "获取电量", "读取信号强度", "连接OTA蓝牙", "获取版本信息", "同步时间", "获取支持语言",
        "获取时区", "设置时区", "获取夏令时", "设置夏令时", "设置用户信息", "设置一天目标", "获取当前健康数据", "检测开关", "实时数据开关",
        "打开心率开关", "GTS10心率间隔", "设置常用联系人", "设置紧急联系人", "设置勿扰", "获取闹钟", "设置闹钟", "获取久坐提醒", "设置久坐提醒",
        "获取喝水提醒", "设置喝水提醒", "发送消息","发送天气(4天)","读取天气(7天)","发送天气(7天)","发送音乐","获取事件提醒","设置事件提醒",
        "获取运动模式","设置运动模式","应用商店","世界时钟","设置密码","女性健康","开启压力测量","关闭压力测量","开启心率测量","关闭心率测量",
        "获取手表模式","获取手表名称","获取摇一摇次数","同步运动","开启演示模式","关闭演示模式","开启船运模式","关闭船运模式","获取手表日常数据",
        "获取手表震动时长","获取摇一摇开关","获取广播间隔","读取电量文件","读取sensor文件","赛维算法","血糖校准","血压校准","同步运动记录","同步记步睡眠记录",
        "获取睡眠计划","设置睡眠计划","时间制式","同步摇头记录","同步心率记录","同步站立次数中高强度","同步血压记录","同步呼吸率记录","同步血氧记录",
        "同步压力记录","同步梅脱记录","同步温度记录","同步MAI","同步血糖","同步睡眠记录","同步睡眠原始数据","同步步数有效日期","同步睡眠有效日期",
        "同步梅脱有效日期","同步Mai有效日期","同步血糖有效日期","同步血氧有效日期","同步摇头有效日期","发送ui","发送星历(国内)","发送星历(国外)",
        "发送ui(差分升级)","发送ui(本地)","发送文件系统","获取文件系统","发送表盘V2","瑞昱ota升级","博通ota升级","杰理升级","杰理日志获取",
        "瑞昱ota升级(本地)","博通ota升级(本地)", "杰理ota升级(本地)","X03差分升级(本地)","思澈ota升级(本地)","思澈应用下载(本地)",
        "差分升级CRC校验(本地)","发送自定义表盘","发送自定义表盘V2","发送本地表盘","发送本地logo","清除logo","获取表盘信息","切换表盘","解绑指令",
        "解绑检测","恢复出厂设置","关机","船运模式","单元测试","朝朝暮暮","步数未达标","健康处方","朝朝暮暮有效日期列表","处方有效日期列表",
        "获取健康处方历史","获取朝朝暮暮历史","耗电模式","获取log"
    )*/

    fun startSearch() {
        instructionMap = keepInstructionMap.mapValues { (_, value) ->
            value.filter { it.contains(searchName, ignoreCase = true) }
        }.filterValues { it.isNotEmpty() }

    }


}