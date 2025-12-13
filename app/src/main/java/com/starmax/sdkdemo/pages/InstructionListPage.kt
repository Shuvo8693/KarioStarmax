package com.starmax.sdkdemo.pages

import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.KeyboardArrowLeft
import androidx.compose.material.icons.sharp.KeyboardArrowLeft
import androidx.compose.material.icons.twotone.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults.contentPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.starmax.sdkdemo.NavPage
import com.starmax.sdkdemo.ui.theme.AppTheme
import com.starmax.sdkdemo.viewmodel.BleViewModel
import com.starmax.sdkdemo.viewmodel.HomeViewModel
import com.starmax.sdkdemo.viewmodel.InstructionListViewModel
import com.starmax.sdkdemo.viewmodel.OtaType
import com.starmax.sdkdemo.viewmodel.OtaViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InstructionListPage(navController: NavController,viewModel: InstructionListViewModel = viewModel()){

    val lazyListState = rememberLazyListState()

    val homeViewModel: HomeViewModel by lazyKoinViewModel()
    val bleViewModel: BleViewModel by lazyKoinViewModel()
    val otaViewModel: OtaViewModel by lazyKoinViewModel()
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val activity = context as? AppCompatActivity



    val selectOtaLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
            val uri = it.data?.data
            if(uri != null){
                otaViewModel.getFromPath(bleViewModel.bleDevice!!.get()!!, uri)
            }
            scope.launch {
                navController.popBackStack()
            }
        }

    val selectGts7FirmwareLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
        val uri = it.data?.data
        bleViewModel.binUri = uri
        bleViewModel.sendGts7FirmwareLocal(context)
        scope.launch {
            navController.popBackStack()
        }
    }

    val selectUiLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
            val uri = it.data?.data
            if(uri != null){
                bleViewModel.sendUiLocal(context,uri)
            }
            scope.launch {
                navController.popBackStack()
            }
        }

    val selectGts7CrcLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
        val uri = it.data?.data
        bleViewModel.binUri = uri
        bleViewModel.sendGts7CrcLocal(context)
        scope.launch {
            navController.popBackStack()
        }
    }
    val selectBinLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
            val uri = it.data?.data
            bleViewModel.binUri = uri
            bleViewModel.sendCustomDial(context)
            scope.launch {
                navController.popBackStack()
            }
        }

    val selectImageLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
            val uri = it.data?.data
            bleViewModel.imageUri = uri
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            selectBinLauncher.launch(intent)
            scope.launch {
                navController.popBackStack()
            }
        }

    val selectBinV2Launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
            val uri = it.data?.data
            Log.d("ActivityResultBin", uri.toString());
            bleViewModel.binUri = uri
            bleViewModel.sendCustomDialV2Local(context)
            scope.launch {
                navController.popBackStack()
            }
        }

    val selectImageV2Launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
            val uri = it.data?.data
            Log.d("ActivityResultImage", uri.toString());
            bleViewModel.imageUri = uri
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            selectBinV2Launcher.launch(intent)
//            scope.launch {
//                navController.popBackStack()
//            }
        }

    val selectDialLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
            val uri = it.data?.data
            bleViewModel.binUri = uri
            bleViewModel.sendDialLocal(context)
            scope.launch {
                navController.popBackStack()
            }
        }

    val selectLogoLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
            val uri = it.data?.data
            bleViewModel.binUri = uri
            bleViewModel.sendLogoLocal(context)
            scope.launch {
                navController.popBackStack()
            }
        }
    val selectFileV2Launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
            val uri = it.data?.data
            if(uri != null){
                bleViewModel.sendFileV2LocalByDiffMd5(context,uri)
            }
            scope.launch {
                navController.popBackStack()
            }
        }

    val selectDialV2Launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
            val uri = it.data?.data
            if(uri != null){
                bleViewModel.binUri = uri
                bleViewModel.sendDialV2Local(context,uri)
            }
            scope.launch {
                navController.popBackStack()
            }
        }

    AppTheme {
        Scaffold (
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(text = "Function button")
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            navController.popBackStack()
                        }) {
                            Icon(Icons.Sharp.KeyboardArrowLeft, contentDescription = "Return")
                        }
                    },
                )
            }
        ){
                innerPadding ->
            LazyColumn(
                modifier = Modifier.padding(innerPadding),
                state = lazyListState,
            ) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(
                            horizontal = 15.dp
                        )
                    ) {
                        OutlinedTextField(
                            value = viewModel.searchName,
                            onValueChange = {
                                val instruction = it.replace(" ", "")
                                viewModel.searchName = instruction
                            },
                            label = {
                                Text(text = "Function name", style = MaterialTheme.typography.labelSmall)
                            },
                            textStyle = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.TwoTone.Search,
                                    contentDescription = "search",
                                    modifier = Modifier.clickable{
                                        viewModel.startSearch()
                                    }
                                )
                            }

                        )

                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }

                viewModel.instructionMap.forEach { (key,value) ->
                    item {
                        Text(text = key, Modifier.padding(start = 10.dp), fontWeight = FontWeight.Bold)
                    }
                    item {
                        FlowRow(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            value.forEach { instruction ->
                                Button(
                                    onClick = {// Due to Composable limitations, a new method cannot be created
//                                        val instruction = mInstruction.trim()
//                                        val instruction = mInstruction.replace(" ", "")

                                        var isPopBackStack = true;


                                        when(instruction){
                                            "Connect OTA Bluetooth" -> {
                                                if (bleViewModel.bleDevice?.get() != null) {
                                                    otaViewModel.connect(bleViewModel.bleDevice?.get())
                                                }
                                            }
                                            "Realtek OTA Upgrade" -> {
                                                if (bleViewModel.bleDevice?.get() != null) {
                                                    otaViewModel.otaType = OtaType.Real
                                                    otaViewModel.download(
                                                        bleViewModel.bleDevice!!.get()!!,
                                                        bleViewModel.bleModel,
                                                        bleViewModel.bleVersion
                                                    )
                                                }
                                            }
                                            "Broadcom OTA Upgrade" -> {
                                                if (bleViewModel.bleDevice?.get() != null) {
                                                    otaViewModel.otaType = OtaType.BK
                                                    otaViewModel.download(
                                                        bleViewModel.bleDevice!!.get()!!,
                                                        bleViewModel.bleModel,
                                                        bleViewModel.bleVersion
                                                    )
                                                }
                                            }
                                            "Jieli OTA Upgrade (Local)" -> {
                                                if (bleViewModel.bleDevice?.get() != null) {
                                                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                                                    intent.type = "*/*"
                                                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                                                    otaViewModel.otaType = OtaType.JieLi
                                                    selectOtaLauncher.launch(intent)
                                                    isPopBackStack = false
                                                }
                                            }
                                            "X03 Differential Upgrade (Local)" -> {
                                                if (bleViewModel.bleDevice?.get() != null) {
                                                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                                                    intent.type = "*/*"
                                                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                                                    selectGts7FirmwareLauncher.launch(intent)
                                                    isPopBackStack = false
                                                }
                                            }
                                            "Realtek OTA Upgrade (Local)" -> {
                                                if (bleViewModel.bleDevice?.get() != null) {
                                                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                                                    intent.type = "*/*"
                                                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                                                    otaViewModel.otaType = OtaType.Real
                                                    selectOtaLauncher.launch(intent)
                                                    isPopBackStack = false
                                                }
                                            }
                                            "Broadcom OTA Upgrade (Local)" -> {
                                                if (bleViewModel.bleDevice?.get() != null) {
                                                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                                                    intent.type = "*/*"
                                                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                                                    otaViewModel.otaType = OtaType.BK
                                                    selectOtaLauncher.launch(intent)
                                                    isPopBackStack = false
                                                }
                                            }
                                            "Jieli Upgrade" -> {
                                                if (bleViewModel.bleDevice?.get() != null) {
                                                    otaViewModel.otaType = OtaType.JieLi
                                                    otaViewModel.download(
                                                        bleViewModel.bleDevice!!.get()!!,
                                                        bleViewModel.bleModel,
                                                        bleViewModel.bleVersion
                                                    )
                                                }
                                            }
                                            "Sifli OTA Upgrade (Local)" -> {
                                                if (bleViewModel.bleDevice?.get() != null) {
                                                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                                                    intent.type = "*/*"
                                                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                                                    otaViewModel.otaType = OtaType.Sifli
                                                    selectOtaLauncher.launch(intent)
                                                    isPopBackStack = false
                                                }
                                            }
                                            "Send UI" -> {
                                                if (bleViewModel.bleDevice?.get() != null) {
                                                    bleViewModel.sendUi()
                                                }
                                            }
                                            "Send UI (Differential Upgrade)" -> {
                                                if (bleViewModel.bleDevice?.get() != null) {
                                                    bleViewModel.sendUiDiff()
                                                }
                                            }
                                            "Send UI (Local)" -> {
                                                if (bleViewModel.bleDevice?.get() != null) {
                                                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                                                    intent.type = "*/*"
                                                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                                                    selectUiLauncher.launch(intent)
                                                    isPopBackStack = false
                                                }
                                            }
                                            "Differential Upgrade CRC Check (Local)" -> {
                                                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                                                intent.type = "*/*"
                                                intent.addCategory(Intent.CATEGORY_OPENABLE)
                                                selectGts7CrcLauncher.launch(intent)
                                                isPopBackStack = false
                                            }
                                            "Sync Head Shaking Record" -> {
                                                activity?.let {
                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
                                                    picker.addOnPositiveButtonClickListener {
                                                            that ->
                                                        bleViewModel.getShakeHeadHistory(that)
                                                    }
                                                    picker.show(it.supportFragmentManager, picker.toString())
                                                }
                                            }
                                            "Sync Heart Rate Record" -> {
                                                activity?.let {
                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
                                                    picker.addOnPositiveButtonClickListener {
                                                            that ->
                                                        bleViewModel.getHeartRateHistory(that)
                                                    }
                                                    picker.show(it.supportFragmentManager, picker.toString())
                                                }
                                            }
                                            "Sync Standing Count Medium-High Intensity" -> {
                                                activity?.let {
                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
                                                    picker.addOnPositiveButtonClickListener {
                                                            that ->
                                                        bleViewModel.getExerciseHistory(that)
                                                    }
                                                    picker.show(it.supportFragmentManager, picker.toString())
                                                }
                                            }
                                            "Sync Blood Pressure Record" -> {
                                                activity?.let {
                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
                                                    picker.addOnPositiveButtonClickListener {
                                                            that ->
                                                        bleViewModel.getBloodPressureHistory(that)
                                                    }
                                                    picker.show(it.supportFragmentManager, picker.toString())
                                                }
                                            }
                                            "Sync Respiration Rate Record" -> {
                                                activity?.let {
                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
                                                    picker.addOnPositiveButtonClickListener {
                                                            that ->
                                                        bleViewModel.getRespirationRateHistory(that)
                                                    }
                                                    picker.show(it.supportFragmentManager, picker.toString())
                                                }
                                            }
                                            "Sync Blood Oxygen Record" -> {
                                                activity?.let {
                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
                                                    picker.addOnPositiveButtonClickListener { that ->
                                                        bleViewModel.getBloodOxygenHistory(that)
                                                    }
                                                    picker.show(it.supportFragmentManager, picker.toString())
                                                }
                                            }
                                            "Sync Pressure Record" -> {
                                                activity?.let {
                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
                                                    picker.addOnPositiveButtonClickListener {
                                                            that ->
                                                        bleViewModel.getPressureHistory(that)
                                                    }
                                                    picker.show(it.supportFragmentManager, picker.toString())
                                                }
                                            }
                                            "Sync MET Record" -> {
                                                activity?.let {
                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
                                                    picker.addOnPositiveButtonClickListener {
                                                            that ->
                                                        bleViewModel.getMetHistory(that)
                                                    }
                                                    picker.show(it.supportFragmentManager, picker.toString())
                                                }
                                            }
                                            "Sync Temperature Record" -> {
                                                activity?.let {
                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
                                                    picker.addOnPositiveButtonClickListener {
                                                            that ->
                                                        bleViewModel.getTempHistory(that)
                                                    }
                                                    picker.show(it.supportFragmentManager, picker.toString())
                                                }
                                            }
                                            "Sync MAI" -> {
                                                activity?.let {
                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
                                                    picker.addOnPositiveButtonClickListener {
                                                            that ->
                                                        bleViewModel.getMaiHistory(that)
                                                    }
                                                    picker.show(it.supportFragmentManager, picker.toString())
                                                }
                                            }
                                            "Sync Blood Sugar" -> {
                                                activity?.let {
                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
                                                    picker.addOnPositiveButtonClickListener {
                                                            that ->
                                                        bleViewModel.getBloodSugarHistory(that)
                                                    }
                                                    picker.show(it.supportFragmentManager, picker.toString())
                                                }
                                            }
                                            "Sync Sleep Record" -> {
                                                activity?.let {
                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
                                                    picker.addOnPositiveButtonClickListener {
                                                            that ->
                                                        bleViewModel.getSleepHistory(that)
                                                    }
                                                    picker.show(it.supportFragmentManager, picker.toString())
                                                }
                                            }
                                            "Sync Raw Sleep Data" -> {
                                                activity?.let {
                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
                                                    picker.addOnPositiveButtonClickListener {
                                                            that ->
                                                        bleViewModel.getOriginSleepHistory(that)
                                                    }
                                                    picker.show(it.supportFragmentManager, picker.toString())
                                                }
                                            }
                                            "Sync Step Count Valid Dates" -> bleViewModel.getValidHistoryDates()
                                            "Sync Sleep Valid Dates" -> bleViewModel.getSleepValidHistoryDates()
                                            "Sync MET Valid Dates" -> bleViewModel.getMetValidHistoryDates()
                                            "Sync Mai Valid Dates" -> bleViewModel.getMaiValidHistoryDates()
                                            "Sync Blood Sugar Valid Dates" -> bleViewModel.getBloodSugarValidHistoryDates()
                                            "Sync Blood Oxygen Valid Dates" -> bleViewModel.getBloodOxygenValidHistoryDates()
                                            "Sync Head Shaking Valid Dates" -> bleViewModel.getShakeHeadValidHistoryDates()
                                            "Sync Sport" -> homeViewModel.toggleSportSyncToDeviceDialog()
                                            "Sync Sport Record" -> bleViewModel.getSportHistory()
                                            "Sync Step Count Sleep Record" -> {
                                                activity?.let {
                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
                                                    picker.addOnPositiveButtonClickListener {
                                                            that ->
                                                        bleViewModel.getStepHistory(that)
                                                    }
                                                    picker.show(it.supportFragmentManager, picker.toString())
                                                }
                                            }
                                            "Get Drink Water Reminder" -> bleViewModel.getDrinkWater()
                                            "Get Sport Mode" -> bleViewModel.getSportMode()
                                            "Get Watch Mode" -> homeViewModel.toggleCustomDeviceModeDialog()
                                            "Get NFC Card Info" -> bleViewModel.getNfcCardInfo()
                                            "Get BT Status" -> bleViewModel.getBtStatus()
                                            "Get Power" -> bleViewModel.getPower()
                                            "Get Version Info" -> bleViewModel.getVersion(true)
                                            "Get Supported Languages" -> bleViewModel.getSupportLanguages()
                                            "Get Time Zone" -> bleViewModel.getTimeOffset()
                                            "Get Daylight Saving Time" -> bleViewModel.getSummerWorldClock()
                                            "Get Current Health Data" -> bleViewModel.getHealthDetail()
                                            "Get Watch Daily Data" -> bleViewModel.getCustomDeviceDailyData()
                                            "Get Watch Vibration Duration" -> homeViewModel.toggleCustomDeviceShakeTimeDialog()
                                            "Get Watch Name" -> homeViewModel.toggleCustomDeviceNameDialog()
                                            "Get Shake Times" -> bleViewModel.getCustomDeviceShakeTimes()
                                            "Get Shake On/Off Switch" -> homeViewModel.toggleCustomDeviceShakeOnOffDialog()
                                            "Get Broadcast Interval" -> homeViewModel.toggleCustomBroadcastDialog()
                                            "Get File System" -> homeViewModel.toggleFileSystemOpen()
                                            "Get Dial Info" -> {
                                                if (bleViewModel.bleDevice?.get() != null) {
                                                    bleViewModel.getDialInfo()
                                                }
                                            }
                                            "Get Alarm Clock" -> bleViewModel.getClock()
                                            "Get Log" -> {
                                                if (bleViewModel.bleDevice?.get() != null) {
                                                    bleViewModel.getLog()
                                                }
                                            }
                                            "Set Server" -> homeViewModel.toggleSetNet()
                                            "Set Status" -> homeViewModel.toggleSetState()
                                            "Set Time Zone" -> bleViewModel.setTimeOffset()
                                            "Set Daylight Saving Time" -> bleViewModel.setSummerWorldClock()
                                            "Set User Info" -> homeViewModel.toggleUserInfo()
                                            "Set Daily Goal" -> homeViewModel.toggleGoals()
                                            "Set Frequent Contacts" -> homeViewModel.toggleContactOpen()
                                            "Set Emergency Contacts" -> homeViewModel.toggleSosOpen()
                                            "Set Do Not Disturb" -> homeViewModel.toggleNotDisturbOpen()
                                            "Set Sleep Plan" -> bleViewModel.setSleepClock()
                                            "Set Sport Mode" -> bleViewModel.setSportMode()
                                            "Set Password" -> homeViewModel.togglePasswordOpen()
                                            "Set Female Health" -> homeViewModel.toggleFemaleHealthOpen()
                                            "Set Sedentary Reminder" -> bleViewModel.setLongSit()
                                            "Set Drink Water Reminder" -> bleViewModel.setDrinkWater()
                                            "Keep Foreground Running" -> bleViewModel.bindService()
                                            "Disconnect Foreground Running" -> bleViewModel.unbindService()
                                            "Volume" -> homeViewModel.toggleVolume()
                                            "Pair Command" -> bleViewModel.pair()
                                            "Bluetooth Broadcast Data Update On"->bleViewModel.setBroadcastOnOff(true)
                                            "Bluetooth Broadcast Data Update Off"->bleViewModel.setBroadcastOnOff(false)
                                            "Pair Command (GTS10) Pop-up" -> bleViewModel.pairGts10(0)
                                            "Pair Command (GTS10) No Pop-up" -> bleViewModel.pairGts10(1)
                                            "GTS10 Two-way Pairing" -> scope.launch { navController.navigate(NavPage.Gts10PairPage.name)}
                                            "Find Device" -> bleViewModel.findDevice(true)
                                            "Stop Finding" -> bleViewModel.findDevice(false)
                                            "Camera Control" -> homeViewModel.toggleCamera()
                                            "Call Control" -> homeViewModel.toggleCall()
                                            "Sync Time" -> bleViewModel.setTime()
                                            "Send Message" -> homeViewModel.toggleMessageOpen()
                                            "App Store" -> homeViewModel.toggleAppOpen()
                                            "World Clock" -> homeViewModel.toggleWorldClockOpen()
                                            "Time Format" -> homeViewModel.toggleDateFormatDialog()
                                            "Power Consumption Mode" -> scope.launch { navController.navigate(NavPage.QuickBatteryModePage.name)}
                                            "Unit Test" -> {
                                                if (bleViewModel.bleDevice?.get() != null) {
                                                    bleViewModel.unittest()
                                                }
                                            }
                                            "Restore Factory Settings" -> {
                                                if (bleViewModel.bleDevice?.get() != null) {
                                                    bleViewModel.reset()
                                                }
                                            }
                                            "Shut Down" -> {
                                                if (bleViewModel.bleDevice?.get() != null) {
                                                    bleViewModel.close()
                                                }
                                            }
                                            "Open Heart Rate Switch" -> homeViewModel.toggleHeartRateOpen()
                                            "GTS10 Heart Rate Interval" -> scope.launch { navController.navigate(NavPage.Gts10HealthIntervalPage.name)}
                                            "Enable Pressure Measurement" -> bleViewModel.sendHealthMeasure(true)
                                            "Disable Pressure Measurement" -> bleViewModel.sendHealthMeasure(false)
                                            "Enable Heart Rate Measurement" -> bleViewModel.sendHeartRateHealthMeasure(true)
                                            "Disable Heart Rate Measurement" -> bleViewModel.sendHeartRateHealthMeasure(false)
                                            "Enable Demo Mode" -> bleViewModel.setDisplayMode(true)
                                            "Disable Demo Mode" -> bleViewModel.setDisplayMode(false)
                                            "Enable Shipping Mode" -> bleViewModel.setShipMode(true)
                                            "Custom Switch" -> homeViewModel.toggleCustomDialog()
                                            "Sport Mode On/Off Switch" -> homeViewModel.toggleSportModeOnOffDialog()
                                            "Send Weather (4 Days)" -> bleViewModel.setWeather()
                                            "Send Weather (7 Days)" -> bleViewModel.setWeatherSeven()
                                            "Read Weather (7 Days)" -> bleViewModel.getWeatherSeven()
                                            "Send Ephemeris (Domestic)" -> {
                                                if (bleViewModel.bleDevice?.get() != null) {
                                                    bleViewModel.sendPgl(true)
                                                }
                                            }
                                            "Send Ephemeris (Foreign)" -> {
                                                if (bleViewModel.bleDevice?.get() != null) {
                                                    bleViewModel.sendPgl(false)
                                                }
                                            }
                                            "Send Custom Dial" -> {
                                                if (bleViewModel.bleDevice?.get() != null) {
                                                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                                                    intent.type = "image/*"
                                                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                                                    selectImageLauncher.launch(intent)
                                                    isPopBackStack = false
                                                }
                                            }
                                            "Send Custom Dial V2" -> {
                                                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                                                intent.type = "image/*"
                                                intent.addCategory(Intent.CATEGORY_OPENABLE)
                                                // The original snippet was cut here
                                            }
                                        }

                                        if (isPopBackStack) {
                                            navController.popBackStack()
                                        }

                                    },
                                    modifier = Modifier.padding(horizontal = 2.dp),
                                    content = {
                                        Text(text = instruction, style = MaterialTheme.typography.labelSmall)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

//    AppTheme {
//        Scaffold (
//            topBar = {
//                CenterAlignedTopAppBar(
//                    title = {
//                        Text(text = "Function button")
//                    },
//                    navigationIcon = {
//                        IconButton(onClick = {
//                            navController.popBackStack()
//                        }) {
//                            Icon(Icons.Sharp.KeyboardArrowLeft, contentDescription = "返回")
//                        }
//                    },
//                )
//            }
//        ){
//            innerPadding ->
//            LazyColumn(
//                modifier = Modifier.padding(innerPadding),
//                state = lazyListState,
//            ) {
//                item {
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        modifier = Modifier.padding(
//                            horizontal = 15.dp
//                        )
//                    ) {
//                        OutlinedTextField(
//                            value = viewModel.searchName,
//                            onValueChange = {
//                                val instruction = it.replace(" ", "")
//                                viewModel.searchName = instruction
//                            },
//                            label = {
//                                Text(text = "Function name", style = MaterialTheme.typography.labelSmall)
//                            },
//                            textStyle = MaterialTheme.typography.labelSmall,
//                            modifier = Modifier.fillMaxWidth(),
//                            trailingIcon = {
//                                Icon(
//                                    imageVector = Icons.TwoTone.Search,
//                                    contentDescription = "search",
//                                    modifier = Modifier.clickable{
//                                        viewModel.startSearch()
//                                    }
//                                )
//                            }
//
//                        )
//
//                    }
//                }
//
//                item {
//                    Spacer(modifier = Modifier.height(20.dp))
//                }
//
//                viewModel.instructionMap.forEach { (key,value) ->
//                    item {
//                        Text(text = key, Modifier.padding(start = 10.dp), fontWeight = FontWeight.Bold)
//                    }
//                    item {
//                        FlowRow(
//                            modifier = Modifier.padding(8.dp)
//                        ) {
//                            value.forEach { instruction ->
//                                Button(
//                                    onClick = {//由于Composable限制，不能创建新的方法
////                                        val instruction = mInstruction.trim()
////                                        val instruction = mInstruction.replace(" ", "")
//
//                                        var isPopBackStack = true;
//
//
//                                        when(instruction){
//                                            "连接OTA蓝牙" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    otaViewModel.connect(bleViewModel.bleDevice?.get())
//                                                }
//                                            }
//                                            "瑞昱ota升级" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    otaViewModel.otaType = OtaType.Real
//                                                    otaViewModel.download(
//                                                        bleViewModel.bleDevice!!.get()!!,
//                                                        bleViewModel.bleModel,
//                                                        bleViewModel.bleVersion
//                                                    )
//                                                }
//                                            }
//                                            "博通ota升级" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    otaViewModel.otaType = OtaType.BK
//                                                    otaViewModel.download(
//                                                        bleViewModel.bleDevice!!.get()!!,
//                                                        bleViewModel.bleModel,
//                                                        bleViewModel.bleVersion
//                                                    )
//                                                }
//                                            }
//                                            "杰理ota升级(本地)" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//                                                    intent.type = "*/*"
//                                                    intent.addCategory(Intent.CATEGORY_OPENABLE)
//                                                    otaViewModel.otaType = OtaType.JieLi
//                                                    selectOtaLauncher.launch(intent)
//                                                    isPopBackStack = false
//                                                }
//                                            }
//                                            "X03差分升级(本地)" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//                                                    intent.type = "*/*"
//                                                    intent.addCategory(Intent.CATEGORY_OPENABLE)
//                                                    selectGts7FirmwareLauncher.launch(intent)
//                                                    isPopBackStack = false
//                                                }
//                                            }
//                                            "瑞昱ota升级(本地)" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//                                                    intent.type = "*/*"
//                                                    intent.addCategory(Intent.CATEGORY_OPENABLE)
//                                                    otaViewModel.otaType = OtaType.Real
//                                                    selectOtaLauncher.launch(intent)
//                                                    isPopBackStack = false
//                                                }
//                                            }
//                                            "博通ota升级(本地)" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//                                                    intent.type = "*/*"
//                                                    intent.addCategory(Intent.CATEGORY_OPENABLE)
//                                                    otaViewModel.otaType = OtaType.BK
//                                                    selectOtaLauncher.launch(intent)
//                                                    isPopBackStack = false
//                                                }
//                                            }
//                                            "杰理升级" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    otaViewModel.otaType = OtaType.JieLi
//                                                    otaViewModel.download(
//                                                        bleViewModel.bleDevice!!.get()!!,
//                                                        bleViewModel.bleModel,
//                                                        bleViewModel.bleVersion
//                                                    )
//                                                }
//                                            }
//                                            "思澈ota升级(本地)" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//                                                    intent.type = "*/*"
//                                                    intent.addCategory(Intent.CATEGORY_OPENABLE)
//                                                    otaViewModel.otaType = OtaType.Sifli
//                                                    selectOtaLauncher.launch(intent)
//                                                    isPopBackStack = false
//                                                }
//                                            }
//                                            "发送ui" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    bleViewModel.sendUi()
//                                                }
//                                            }
//                                            "发送ui(差分升级)" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    bleViewModel.sendUiDiff()
//                                                }
//                                            }
//                                            "发送ui(本地)" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//                                                    intent.type = "*/*"
//                                                    intent.addCategory(Intent.CATEGORY_OPENABLE)
//                                                    selectUiLauncher.launch(intent)
//                                                    isPopBackStack = false
//                                                }
//                                            }
//                                            "差分升级CRC校验(本地)" -> {
//                                                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//                                                intent.type = "*/*"
//                                                intent.addCategory(Intent.CATEGORY_OPENABLE)
//                                                selectGts7CrcLauncher.launch(intent)
//                                                isPopBackStack = false
//                                            }
//                                            "同步摇头记录" -> {
//                                                activity?.let {
//                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
//                                                    picker.addOnPositiveButtonClickListener {
//                                                            that ->
//                                                        bleViewModel.getShakeHeadHistory(that)
//                                                    }
//                                                    picker.show(it.supportFragmentManager, picker.toString())
//                                                }
//                                            }
//                                            "同步心率记录" -> {
//                                                activity?.let {
//                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
//                                                    picker.addOnPositiveButtonClickListener {
//                                                            that ->
//                                                        bleViewModel.getHeartRateHistory(that)
//                                                    }
//                                                    picker.show(it.supportFragmentManager, picker.toString())
//                                                }
//                                            }
//                                            "同步站立次数中高强度" -> {
//                                                activity?.let {
//                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
//                                                    picker.addOnPositiveButtonClickListener {
//                                                            that ->
//                                                        bleViewModel.getExerciseHistory(that)
//                                                    }
//                                                    picker.show(it.supportFragmentManager, picker.toString())
//                                                }
//                                            }
//                                            "同步血压记录" -> {
//                                                activity?.let {
//                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
//                                                    picker.addOnPositiveButtonClickListener {
//                                                            that ->
//                                                        bleViewModel.getBloodPressureHistory(that)
//                                                    }
//                                                    picker.show(it.supportFragmentManager, picker.toString())
//                                                }
//                                            }
//                                            "同步呼吸率记录" -> {
//                                                activity?.let {
//                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
//                                                    picker.addOnPositiveButtonClickListener {
//                                                            that ->
//                                                        bleViewModel.getRespirationRateHistory(that)
//                                                    }
//                                                    picker.show(it.supportFragmentManager, picker.toString())
//                                                }
//                                            }
//                                            "同步血氧记录" -> {
//                                                activity?.let {
//                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
//                                                    picker.addOnPositiveButtonClickListener {
//                                                            that ->
//                                                        bleViewModel.getBloodOxygenHistory(that)
//                                                    }
//                                                    picker.show(it.supportFragmentManager, picker.toString())
//                                                }
//                                            }
//                                            "同步压力记录" -> {
//                                                activity?.let {
//                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
//                                                    picker.addOnPositiveButtonClickListener {
//                                                            that ->
//                                                        bleViewModel.getPressureHistory(that)
//                                                    }
//                                                    picker.show(it.supportFragmentManager, picker.toString())
//                                                }
//                                            }
//                                            "同步梅脱记录" -> {
//                                                activity?.let {
//                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
//                                                    picker.addOnPositiveButtonClickListener {
//                                                            that ->
//                                                        bleViewModel.getMetHistory(that)
//                                                    }
//                                                    picker.show(it.supportFragmentManager, picker.toString())
//                                                }
//                                            }
//                                            "同步温度记录" -> {
//                                                activity?.let {
//                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
//                                                    picker.addOnPositiveButtonClickListener {
//                                                            that ->
//                                                        bleViewModel.getTempHistory(that)
//                                                    }
//                                                    picker.show(it.supportFragmentManager, picker.toString())
//                                                }
//                                            }
//                                            "同步MAI" -> {
//                                                activity?.let {
//                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
//                                                    picker.addOnPositiveButtonClickListener {
//                                                            that ->
//                                                        bleViewModel.getMaiHistory(that)
//                                                    }
//                                                    picker.show(it.supportFragmentManager, picker.toString())
//                                                }
//                                            }
//                                            "同步血糖" -> {
//                                                activity?.let {
//                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
//                                                    picker.addOnPositiveButtonClickListener {
//                                                            that ->
//                                                        bleViewModel.getBloodSugarHistory(that)
//                                                    }
//                                                    picker.show(it.supportFragmentManager, picker.toString())
//                                                }
//                                            }
//                                            "同步睡眠记录" -> {
//                                                activity?.let {
//                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
//                                                    picker.addOnPositiveButtonClickListener {
//                                                            that ->
//                                                        bleViewModel.getSleepHistory(that)
//                                                    }
//                                                    picker.show(it.supportFragmentManager, picker.toString())
//                                                }
//                                            }
//                                            "同步睡眠原始数据" -> {
//                                                activity?.let {
//                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
//                                                    picker.addOnPositiveButtonClickListener {
//                                                            that ->
//                                                        bleViewModel.getOriginSleepHistory(that)
//                                                    }
//                                                    picker.show(it.supportFragmentManager, picker.toString())
//                                                }
//                                            }
//                                            "同步步数有效日期" -> bleViewModel.getValidHistoryDates()
//                                            "同步睡眠有效日期" -> bleViewModel.getSleepValidHistoryDates()
//                                            "同步梅脱有效日期" -> bleViewModel.getMetValidHistoryDates()
//                                            "同步Mai有效日期" -> bleViewModel.getMaiValidHistoryDates()
//                                            "同步血糖有效日期" -> bleViewModel.getBloodSugarValidHistoryDates()
//                                            "同步血氧有效日期" -> bleViewModel.getBloodOxygenValidHistoryDates()
//                                            "同步摇头有效日期" -> bleViewModel.getShakeHeadValidHistoryDates()
//                                            "同步运动" -> homeViewModel.toggleSportSyncToDeviceDialog()
//                                            "同步运动记录" -> bleViewModel.getSportHistory()
//                                            "同步记步睡眠记录" -> {
//                                                activity?.let {
//                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
//                                                    picker.addOnPositiveButtonClickListener {
//                                                            that ->
//                                                        bleViewModel.getStepHistory(that)
//                                                    }
//                                                    picker.show(it.supportFragmentManager, picker.toString())
//                                                }
//                                            }
//                                            "获取喝水提醒" -> bleViewModel.getDrinkWater()
//                                            "获取运动模式" -> bleViewModel.getSportMode()
//                                            "获取手表模式" -> homeViewModel.toggleCustomDeviceModeDialog()
//                                            "获取NFC卡片信息" -> bleViewModel.getNfcCardInfo()
//                                            "获取BT状态" -> bleViewModel.getBtStatus()
//                                            "获取电量" -> bleViewModel.getPower()
//                                            "获取版本信息" -> bleViewModel.getVersion(true)
//                                            "获取支持语言" -> bleViewModel.getSupportLanguages()
//                                            "获取时区" -> bleViewModel.getTimeOffset()
//                                            "获取夏令时" -> bleViewModel.getSummerWorldClock()
//                                            "获取当前健康数据" -> bleViewModel.getHealthDetail()
//                                            "获取手表日常数据" -> bleViewModel.getCustomDeviceDailyData()
//                                            "获取手表震动时长" -> homeViewModel.toggleCustomDeviceShakeTimeDialog()
//                                            "获取手表名称" -> homeViewModel.toggleCustomDeviceNameDialog()
//                                            "获取摇一摇次数" -> bleViewModel.getCustomDeviceShakeTimes()
//                                            "获取摇一摇开关" -> homeViewModel.toggleCustomDeviceShakeOnOffDialog()
//                                            "获取广播间隔" -> homeViewModel.toggleCustomBroadcastDialog()
//                                            "获取文件系统" -> homeViewModel.toggleFileSystemOpen()
//                                            "获取表盘信息" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    bleViewModel.getDialInfo()
//                                                }
//                                            }
//                                            "获取闹钟" -> bleViewModel.getClock()
//                                            "获取log" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    bleViewModel.getLog()
//                                                }
//                                            }
//                                            "设置服务器" -> homeViewModel.toggleSetNet()
//                                            "设置状态" -> homeViewModel.toggleSetState()
//                                            "设置时区" -> bleViewModel.setTimeOffset()
//                                            "设置夏令时" -> bleViewModel.setSummerWorldClock()
//                                            "设置用户信息" -> homeViewModel.toggleUserInfo()
//                                            "设置一天目标" -> homeViewModel.toggleGoals()
//                                            "设置常用联系人" -> homeViewModel.toggleContactOpen()
//                                            "设置紧急联系人" -> homeViewModel.toggleSosOpen()
//                                            "设置勿扰" -> homeViewModel.toggleNotDisturbOpen()
//                                            "设置睡眠计划" -> bleViewModel.setSleepClock()
//                                            "设置运动模式" -> bleViewModel.setSportMode()
//                                            "设置密码" -> homeViewModel.togglePasswordOpen()
//                                            "设置女性健康" -> homeViewModel.toggleFemaleHealthOpen()
//                                            "设置久坐提醒" -> bleViewModel.setLongSit()
//                                            "设置喝水提醒" -> bleViewModel.setDrinkWater()
//                                            "保持前台运行" -> bleViewModel.bindService()
//                                            "断开前台运行" -> bleViewModel.unbindService()
//                                            "音量" -> homeViewModel.toggleVolume()
//                                            "配对指令" -> bleViewModel.pair()
//                                            "蓝牙广播数据更新打开"->bleViewModel.setBroadcastOnOff(true)
//                                            "蓝牙广播数据更新关闭"->bleViewModel.setBroadcastOnOff(false)
//                                            "配对指令(GTS10)弹出" -> bleViewModel.pairGts10(0)
//                                            "配对指令(GTS10)不弹出" -> bleViewModel.pairGts10(1)
//                                            "GTS10双向配对" -> scope.launch { navController.navigate(NavPage.Gts10PairPage.name)}
//                                            "查找设备" -> bleViewModel.findDevice(true)
//                                            "停止查找" -> bleViewModel.findDevice(false)
//                                            "拍照控制" -> homeViewModel.toggleCamera()
//                                            "电话控制" -> homeViewModel.toggleCall()
//                                            "同步时间" -> bleViewModel.setTime()
//                                            "发送消息" -> homeViewModel.toggleMessageOpen()
//                                            "应用商店" -> homeViewModel.toggleAppOpen()
//                                            "世界时钟" -> homeViewModel.toggleWorldClockOpen()
//                                            "时间制式" -> homeViewModel.toggleDateFormatDialog()
//                                            "耗电模式" -> scope.launch { navController.navigate(NavPage.QuickBatteryModePage.name)}
//                                            "单元测试" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    bleViewModel.unittest()
//                                                }
//                                            }
//                                            "恢复出厂设置" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    bleViewModel.reset()
//                                                }
//                                            }
//                                            "关机" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    bleViewModel.close()
//                                                }
//                                            }
//                                            "打开心率开关" -> homeViewModel.toggleHeartRateOpen()
//                                            "GTS10心率间隔" -> scope.launch { navController.navigate(NavPage.Gts10HealthIntervalPage.name)}
//                                            "开启压力测量" -> bleViewModel.sendHealthMeasure(true)
//                                            "关闭压力测量" -> bleViewModel.sendHealthMeasure(false)
//                                            "开启心率测量" -> bleViewModel.sendHeartRateHealthMeasure(true)
//                                            "关闭心率测量" -> bleViewModel.sendHeartRateHealthMeasure(false)
//                                            "开启演示模式" -> bleViewModel.setDisplayMode(true)
//                                            "关闭演示模式" -> bleViewModel.setDisplayMode(false)
//                                            "开启船运模式" -> bleViewModel.setShipMode(true)
//                                            "自定义开关" -> homeViewModel.toggleCustomDialog()
//                                            "运动模式开关" -> homeViewModel.toggleSportModeOnOffDialog()
//                                            "发送天气(4天)" -> bleViewModel.setWeather()
//                                            "发送天气(7天)" -> bleViewModel.setWeatherSeven()
//                                            "读取天气(7天)" -> bleViewModel.getWeatherSeven()
//                                            "发送星历(国内)" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    bleViewModel.sendPgl(true)
//                                                }
//                                            }
//                                            "发送星历(国外)" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    bleViewModel.sendPgl(false)
//                                                }
//                                            }
//                                            "发送自定义表盘" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//                                                    intent.type = "image/*"
//                                                    intent.addCategory(Intent.CATEGORY_OPENABLE)
//                                                    selectImageLauncher.launch(intent)
//                                                    isPopBackStack = false
//                                                }
//                                            }
//                                            "发送自定义表盘V2" -> {
//                                                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//                                                intent.type = "image/*"
//                                                intent.addCategory(Intent.CATEGORY_OPENABLE)
//                                                selectImageV2Launcher.launch(intent)
//                                                isPopBackStack = false
//                                            }
//                                            "发送本地表盘" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//                                                    intent.type = "*/*"
//                                                    intent.addCategory(Intent.CATEGORY_OPENABLE)
//                                                    selectDialLauncher.launch(intent)
//                                                    isPopBackStack = false
//                                                }
//                                            }
//                                            "切换表盘" -> bleViewModel.switchDial()
//                                            "设置闹钟" -> bleViewModel.setClock()
//                                            "读取信号强度" -> bleViewModel.getRssi()
//                                            "赛维算法" -> {
//                                                bleViewModel.packageId = 0
//                                                bleViewModel.getDebugInfo(3)
//                                            }
//                                            "血糖校准" -> homeViewModel.toggleBloodSugarCalibrationDialog()
//                                            "血压校准" -> homeViewModel.toggleBloodPressureCalibrationDialog()
//                                            "朝朝暮暮" -> scope.launch { navController.navigate(NavPage.GoalsDayAndNightPage.name) }
//                                            "步数未达标" -> scope.launch { navController.navigate(NavPage.GoalsNotUpPage.name)}
//                                            "健康处方" -> scope.launch { navController.navigate(NavPage.CustomHealthGoalsPage.name)}
//                                            "朝朝暮暮有效日期列表" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    bleViewModel.getGoalsDayAndNightValidHistoryDates()
//                                                }
//                                            }
//                                            "获取健康处方历史" -> {
//                                                activity?.let {
//                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
//                                                    picker.addOnPositiveButtonClickListener {
//                                                            that ->
//                                                        bleViewModel.getCustomHealthGoalsHistory(that)
//                                                    }
//                                                    picker.show(it.supportFragmentManager, picker.toString())
//                                                }
//                                            }
//                                            "获取朝朝暮暮历史" -> {
//                                                activity?.let {
//                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
//                                                    picker.addOnPositiveButtonClickListener {
//                                                            that ->
//                                                        bleViewModel.getGoalsDayAndNightHistory(that)
//                                                    }
//                                                    picker.show(it.supportFragmentManager, picker.toString())
//                                                }
//                                            }
//                                            "发送本地logo" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//                                                    intent.type = "*/*"
//                                                    intent.addCategory(Intent.CATEGORY_OPENABLE)
//                                                    selectLogoLauncher.launch(intent)
//                                                    isPopBackStack = false
//                                                }
//                                            }
//                                            "清除logo" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    bleViewModel.clearLogo()
//                                                }
//                                            }
//                                            "设置NFC卡片" -> homeViewModel.toggleNfcDialog()
//                                            "配对设备" -> bleViewModel.bindDevice()
//                                            "检测开关" -> homeViewModel.toggleHealthOpen()
//                                            "实时数据开关" -> homeViewModel.toggleRealTimeDataOpen()
//                                            "获取久坐提醒" -> bleViewModel.getLongSit()
//                                            "发送音乐" -> bleViewModel.sendMusic()
//                                            "事件提醒" -> scope.launch { navController.navigate(NavPage.EventReminderPage.name)}
//                                            "获取电量文件" -> {
//                                                bleViewModel.packageId = 0
//                                                bleViewModel.getDebugInfo(1)
//                                            }
//                                            "获取sensor文件" -> {
//                                                bleViewModel.packageId = 0
//                                                bleViewModel.getDebugInfo(2)
//                                            }
//                                            "获取睡眠计划" -> bleViewModel.getSleepClock()
//                                            "发送文件系统" -> {
//                                                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//                                                intent.type = "*/*"
//                                                intent.addCategory(Intent.CATEGORY_OPENABLE)
//                                                selectFileV2Launcher.launch(intent)
//                                                isPopBackStack = false
//                                            }
//                                            "发送表盘V2" -> {
//                                                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//                                                intent.type = "*/*"
//                                                intent.addCategory(Intent.CATEGORY_OPENABLE)
//                                                selectDialV2Launcher.launch(intent)
//                                                isPopBackStack = false
//                                            }
//
//                                            "杰理日志获取" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    otaViewModel.onStartLog()
//                                                }
//                                            }
//                                            "思澈应用下载(本地)" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//                                                    intent.type = "*/*"
//                                                    intent.addCategory(Intent.CATEGORY_OPENABLE)
//                                                    otaViewModel.otaType = OtaType.SifliWatchFace
//                                                    selectOtaLauncher.launch(intent)
//                                                    isPopBackStack = false
//                                                }
//                                            }
//                                            "解绑指令" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    bleViewModel.unpair()
//                                                }
//                                            }
//                                            "解绑检测" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    bleViewModel.unpairCheck()
//                                                }
//                                            }
//                                            "船运模式" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    bleViewModel.shippingMode()
//                                                }
//                                            }
//
//                                        }
//
//                                        if (isPopBackStack) {
//                                            navController.popBackStack()
//                                        }
//
//
//                                    },
//                                    modifier = Modifier
//                                        .wrapContentWidth()
//                                        .padding(end = 10.dp) // 按钮宽度根据内容自适应
//                                ) {
//                                    Text(text = instruction)
//                                }
//
//                            }
//                        }
//                    }
//
//                }
//
//            }
//
//        }
//    }
}
