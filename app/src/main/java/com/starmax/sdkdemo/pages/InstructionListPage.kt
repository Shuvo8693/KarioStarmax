package com.starmax.sdkdemo.pages

import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.starmax.sdkdemo.NavPage
import com.starmax.sdkdemo.ui.theme.AppTheme
import com.starmax.sdkdemo.viewmodel.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

data class InstructionItem(
    val label: String,
    val category: String,
    val action: () -> Unit
)

/**
 * OPTIONAL:
 * If your static labels don't exactly match buildInstructionList labels,
 * put aliases here.
 */
private val LABEL_ALIASES: Map<String, String> = mapOf(
    // Example:
    "Pair Device" to "Pair Command",
    // Add more if needed...
)

private fun resolveLabel(label: String): String {
    return LABEL_ALIASES[label] ?: label
}

private fun buildActionRegistry(
    bleViewModel: BleViewModel,
    homeViewModel: HomeViewModel,
    otaViewModel: OtaViewModel,
    navController: NavController,
    activity: AppCompatActivity?,
    selectOtaLauncher: androidx.activity.compose.ManagedActivityResultLauncher<Intent, androidx.activity.result.ActivityResult>,
    selectGts7FirmwareLauncher: androidx.activity.compose.ManagedActivityResultLauncher<Intent, androidx.activity.result.ActivityResult>,
    selectUiLauncher: androidx.activity.compose.ManagedActivityResultLauncher<Intent, androidx.activity.result.ActivityResult>,
    selectGts7CrcLauncher: androidx.activity.compose.ManagedActivityResultLauncher<Intent, androidx.activity.result.ActivityResult>,
    selectImageLauncher: androidx.activity.compose.ManagedActivityResultLauncher<Intent, androidx.activity.result.ActivityResult>,
    selectImageV2Launcher: androidx.activity.compose.ManagedActivityResultLauncher<Intent, androidx.activity.result.ActivityResult>,
    selectDialLauncher: androidx.activity.compose.ManagedActivityResultLauncher<Intent, androidx.activity.result.ActivityResult>,
    selectLogoLauncher: androidx.activity.compose.ManagedActivityResultLauncher<Intent, androidx.activity.result.ActivityResult>,
    selectFileV2Launcher: androidx.activity.compose.ManagedActivityResultLauncher<Intent, androidx.activity.result.ActivityResult>,
    selectDialV2Launcher: androidx.activity.compose.ManagedActivityResultLauncher<Intent, androidx.activity.result.ActivityResult>,
    scope: CoroutineScope,
    context: android.content.Context
): Map<String, () -> Unit> {
    // We reuse buildInstructionList just to extract label -> action
    return buildInstructionList(
        bleViewModel = bleViewModel,
        homeViewModel = homeViewModel,
        otaViewModel = otaViewModel,
        navController = navController,
        activity = activity,
        selectOtaLauncher = selectOtaLauncher,
        selectGts7FirmwareLauncher = selectGts7FirmwareLauncher,
        selectUiLauncher = selectUiLauncher,
        selectGts7CrcLauncher = selectGts7CrcLauncher,
        selectImageLauncher = selectImageLauncher,
        selectImageV2Launcher = selectImageV2Launcher,
        selectDialLauncher = selectDialLauncher,
        selectLogoLauncher = selectLogoLauncher,
        selectFileV2Launcher = selectFileV2Launcher,
        selectDialV2Launcher = selectDialV2Launcher,
        scope = scope,
        context = context
    ).associate { it.label to it.action }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructionListPage(
    navController: NavController,
    viewModel: InstructionListViewModel = viewModel()
) {
    val homeViewModel: HomeViewModel by lazyKoinViewModel()
    val bleViewModel: BleViewModel by lazyKoinViewModel()
    val otaViewModel: OtaViewModel by lazyKoinViewModel()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as? AppCompatActivity

    var searchQuery by remember { mutableStateOf("") }
    var expandedCategories by remember { mutableStateOf(setOf<String>()) }

    // ═══════════════════════════════════════════════════════
    // FILE PICKERS
    // ═══════════════════════════════════════════════════════
    val selectOtaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        val uri = it.data?.data
        if (uri != null) {
            otaViewModel.getFromPath(bleViewModel.bleDevice!!.get()!!, uri)
        }
        scope.launch { navController.popBackStack() }
    }

    val selectGts7FirmwareLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        val uri = it.data?.data
        bleViewModel.binUri = uri
        bleViewModel.sendGts7FirmwareLocal(context)
        scope.launch { navController.popBackStack() }
    }

    val selectUiLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        val uri = it.data?.data
        if (uri != null) {
            bleViewModel.sendUiLocal(context, uri)
        }
        scope.launch { navController.popBackStack() }
    }

    val selectGts7CrcLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        val uri = it.data?.data
        bleViewModel.binUri = uri
        bleViewModel.sendGts7CrcLocal(context)
        scope.launch { navController.popBackStack() }
    }

    val selectBinLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        val uri = it.data?.data
        bleViewModel.binUri = uri
        bleViewModel.sendCustomDial(context)
        scope.launch { navController.popBackStack() }
    }

    val selectImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        val uri = it.data?.data
        bleViewModel.imageUri = uri
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        selectBinLauncher.launch(intent)
    }

    val selectBinV2Launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        val uri = it.data?.data
        Log.d("ActivityResultBin", uri.toString())
        bleViewModel.binUri = uri
        bleViewModel.sendCustomDialV2Local(context)
        scope.launch { navController.popBackStack() }
    }

    val selectImageV2Launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        val uri = it.data?.data
        Log.d("ActivityResultImage", uri.toString())
        bleViewModel.imageUri = uri
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        selectBinV2Launcher.launch(intent)
    }

    val selectDialLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        val uri = it.data?.data
        bleViewModel.binUri = uri
        bleViewModel.sendDialLocal(context)
        scope.launch { navController.popBackStack() }
    }

    val selectLogoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        val uri = it.data?.data
        bleViewModel.binUri = uri
        bleViewModel.sendLogoLocal(context)
        scope.launch { navController.popBackStack() }
    }

    val selectFileV2Launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        val uri = it.data?.data
        if (uri != null) {
            bleViewModel.sendFileV2LocalByDiffMd5(context, uri)
        }
        scope.launch { navController.popBackStack() }
    }

    val selectDialV2Launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        val uri = it.data?.data
        if (uri != null) {
            bleViewModel.binUri = uri
            bleViewModel.sendDialV2Local(context, uri)
        }
        scope.launch { navController.popBackStack() }
    }

    // ─────────────────────────────────────────────
    // KEY PART: build label -> action map from existing function list
    // ─────────────────────────────────────────────
    val actionRegistry = remember(
        bleViewModel, homeViewModel, otaViewModel,
        navController, activity,
        selectOtaLauncher, selectGts7FirmwareLauncher, selectUiLauncher, selectGts7CrcLauncher,
        selectImageLauncher, selectImageV2Launcher,
        selectDialLauncher, selectLogoLauncher, selectFileV2Launcher, selectDialV2Launcher,
        context, scope
    ) {
        buildActionRegistry(
            bleViewModel = bleViewModel,
            homeViewModel = homeViewModel,
            otaViewModel = otaViewModel,
            navController = navController,
            activity = activity,
            selectOtaLauncher = selectOtaLauncher,
            selectGts7FirmwareLauncher = selectGts7FirmwareLauncher,
            selectUiLauncher = selectUiLauncher,
            selectGts7CrcLauncher = selectGts7CrcLauncher,
            selectImageLauncher = selectImageLauncher,
            selectImageV2Launcher = selectImageV2Launcher,
            selectDialLauncher = selectDialLauncher,
            selectLogoLauncher = selectLogoLauncher,
            selectFileV2Launcher = selectFileV2Launcher,
            selectDialV2Launcher = selectDialV2Launcher,
            scope = scope,
            context = context
        )
    }

    // ─────────────────────────────────────────────
    // Convert  STATIC instructionMap -> InstructionItem list (for the same UI)
    // ─────────────────────────────────────────────
    val groupedInstructions: Map<String, List<InstructionItem>> =
        remember(viewModel.instructionMap, actionRegistry) {
            viewModel.instructionMap.mapValues { (category, labels) ->
                labels.map { label ->
                    val realLabel = resolveLabel(label)
                    InstructionItem(
                        label = label, // show your static label in UI
                        category = category,
                        action = {
                            val action = actionRegistry[realLabel]
                            if (action != null) {
                                action()
                            } else {
                                Log.w("InstructionList", "No action mapped for: $label (resolved=$realLabel)")
                            }
                        }
                    )
                }
            }
        }

    AppTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Features List") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Search Bar
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    modifier = Modifier.padding(16.dp)
                )

                // Instruction List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    groupedInstructions.forEach { (category, items) ->
                        item(key = category) {
                            CategorySection(
                                category = category,
                                items = items,
                                isExpanded = expandedCategories.contains(category),
                                onToggle = {
                                    expandedCategories = if (expandedCategories.contains(category)) {
                                        expandedCategories - category
                                    } else {
                                        expandedCategories + category
                                    }
                                },
                                onItemClick = { instruction ->
                                    instruction.action()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════
// SEARCH BAR
// ═══════════════════════════════════════════════════════
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Search functions...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, "Clear")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

// ═══════════════════════════════════════════════════════
// CATEGORY SECTION
// ═══════════════════════════════════════════════════════
@Composable
fun CategorySection(
    category: String,
    items: List<InstructionItem>,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onItemClick: (InstructionItem) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column {
            // Category Header
            Surface(
                onClick = onToggle,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = getCategoryIcon(category),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${items.size} functions",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand"
                    )
                }
            }

            // Category Items
            if (isExpanded) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items.forEach { item ->
                        InstructionButton(
                            label = item.label,
                            onClick = { onItemClick(item) }
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// INSTRUCTION BUTTON
// ═══════════════════════════════════════════════════════
@Composable
fun InstructionButton(
    label: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════
// CATEGORY ICONS
// ═══════════════════════════════════════════════════════
fun getCategoryIcon(category: String): ImageVector = when {
    category.contains("Sync", ignoreCase = true) -> Icons.Default.Sync
    category.contains("Get", ignoreCase = true) -> Icons.Default.GetApp
    category.contains("Set", ignoreCase = true) -> Icons.Default.Settings
    category.contains("OTA", ignoreCase = true) -> Icons.Default.SystemUpdate
    category.contains("UI", ignoreCase = true) -> Icons.Default.Palette
    category.contains("Dial", ignoreCase = true) -> Icons.Default.Watch
    category.contains("Control", ignoreCase = true) -> Icons.Default.Gamepad
    category.contains("Weather", ignoreCase = true) -> Icons.Default.Cloud
    category.contains("Device", ignoreCase = true) -> Icons.Default.Devices
    else -> Icons.Default.Apps
}

// ═══════════════════════════════════════════════════════
// BUILD INSTRUCTION LIST
// ═══════════════════════════════════════════════════════
fun buildInstructionList(
    bleViewModel: BleViewModel,
    homeViewModel: HomeViewModel,
    otaViewModel: OtaViewModel,
    navController: NavController,
    activity: AppCompatActivity?,
    selectOtaLauncher: androidx.activity.compose.ManagedActivityResultLauncher<Intent, androidx.activity.result.ActivityResult>,
    selectGts7FirmwareLauncher: androidx.activity.compose.ManagedActivityResultLauncher<Intent, androidx.activity.result.ActivityResult>,
    selectUiLauncher: androidx.activity.compose.ManagedActivityResultLauncher<Intent, androidx.activity.result.ActivityResult>,
    selectGts7CrcLauncher: androidx.activity.compose.ManagedActivityResultLauncher<Intent, androidx.activity.result.ActivityResult>,
    selectImageLauncher: androidx.activity.compose.ManagedActivityResultLauncher<Intent, androidx.activity.result.ActivityResult>,
    selectImageV2Launcher: androidx.activity.compose.ManagedActivityResultLauncher<Intent, androidx.activity.result.ActivityResult>,
    selectDialLauncher: androidx.activity.compose.ManagedActivityResultLauncher<Intent, androidx.activity.result.ActivityResult>,
    selectLogoLauncher: androidx.activity.compose.ManagedActivityResultLauncher<Intent, androidx.activity.result.ActivityResult>,
    selectFileV2Launcher: androidx.activity.compose.ManagedActivityResultLauncher<Intent, androidx.activity.result.ActivityResult>,
    selectDialV2Launcher: androidx.activity.compose.ManagedActivityResultLauncher<Intent, androidx.activity.result.ActivityResult>,
    scope: CoroutineScope,
    context: android.content.Context
): List<InstructionItem> {
    return listOf(
        // ═══════════════════════════════════════════════════════
        // OTA UPDATES
        // ═══════════════════════════════════════════════════════
        InstructionItem("Connect OTA Bluetooth", "OTA Updates") {
            bleViewModel.bleDevice?.get()?.let { otaViewModel.connect(it) }
        },
        InstructionItem("Realtek OTA Upgrade", "OTA Updates") {
            bleViewModel.bleDevice?.get()?.let {
                otaViewModel.otaType = OtaType.Real
                otaViewModel.download(it, bleViewModel.bleModel, bleViewModel.bleVersion)
            }
        },
        InstructionItem("Broadcom OTA Upgrade", "OTA Updates") {
            bleViewModel.bleDevice?.get()?.let {
                otaViewModel.otaType = OtaType.BK
                otaViewModel.download(it, bleViewModel.bleModel, bleViewModel.bleVersion)
            }
        },
        InstructionItem("Jieli OTA Upgrade (Local)", "OTA Updates") {
            bleViewModel.bleDevice?.get()?.let {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.type = "*/*"
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                otaViewModel.otaType = OtaType.JieLi
                selectOtaLauncher.launch(intent)
            }
        },
        InstructionItem("X03 Differential Upgrade (Local)", "OTA Updates") {
            bleViewModel.bleDevice?.get()?.let {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.type = "*/*"
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                selectGts7FirmwareLauncher.launch(intent)
            }
        },
        InstructionItem("Realtek OTA Upgrade (Local)", "OTA Updates") {
            bleViewModel.bleDevice?.get()?.let {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.type = "*/*"
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                otaViewModel.otaType = OtaType.Real
                selectOtaLauncher.launch(intent)
            }
        },
        InstructionItem("Broadcom OTA Upgrade (Local)", "OTA Updates") {
            bleViewModel.bleDevice?.get()?.let {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.type = "*/*"
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                otaViewModel.otaType = OtaType.BK
                selectOtaLauncher.launch(intent)
            }
        },
        InstructionItem("Jieli Upgrade", "OTA Updates") {
            bleViewModel.bleDevice?.get()?.let {
                otaViewModel.otaType = OtaType.JieLi
                otaViewModel.download(it, bleViewModel.bleModel, bleViewModel.bleVersion)
            }
        },
        InstructionItem("Sifli OTA Upgrade (Local)", "OTA Updates") {
            bleViewModel.bleDevice?.get()?.let {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.type = "*/*"
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                otaViewModel.otaType = OtaType.Sifli
                selectOtaLauncher.launch(intent)
            }
        },

        // ═══════════════════════════════════════════════════════
        // UI UPDATES
        // ═══════════════════════════════════════════════════════
        InstructionItem("Send UI", "UI Updates") {
            bleViewModel.bleDevice?.get()?.let { bleViewModel.sendUi() }
        },
        InstructionItem("Send UI (Differential Upgrade)", "UI Updates") {
            bleViewModel.bleDevice?.get()?.let { bleViewModel.sendUiDiff() }
        },
        InstructionItem("Send UI (Local)", "UI Updates") {
            bleViewModel.bleDevice?.get()?.let {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.type = "*/*"
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                selectUiLauncher.launch(intent)
            }
        },
        InstructionItem("Differential Upgrade CRC Check (Local)", "UI Updates") {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            selectGts7CrcLauncher.launch(intent)
        },

        // ═══════════════════════════════════════════════════════
        // SYNC HISTORY
        // ═══════════════════════════════════════════════════════
        InstructionItem("Sync Head Shaking Record", "Sync History") {
            activity?.let {
                val picker = MaterialDatePicker.Builder.datePicker().build()
                picker.addOnPositiveButtonClickListener { date ->
                    bleViewModel.getShakeHeadHistory(date)
                }
                picker.show(it.supportFragmentManager, picker.toString())
            }
        },
        InstructionItem("Sync Heart Rate Record", "Sync History") {
            activity?.let {
                val picker = MaterialDatePicker.Builder.datePicker().build()
                picker.addOnPositiveButtonClickListener { date ->
                    bleViewModel.getHeartRateHistory(date)
                }
                picker.show(it.supportFragmentManager, picker.toString())
            }
        },
        InstructionItem("Sync Standing Count Medium-High Intensity", "Sync History") {
            activity?.let {
                val picker = MaterialDatePicker.Builder.datePicker().build()
                picker.addOnPositiveButtonClickListener { date ->
                    bleViewModel.getExerciseHistory(date)
                }
                picker.show(it.supportFragmentManager, picker.toString())
            }
        },
        InstructionItem("Sync Blood Pressure Record", "Sync History") {
            activity?.let {
                val picker = MaterialDatePicker.Builder.datePicker().build()
                picker.addOnPositiveButtonClickListener { date ->
                    bleViewModel.getBloodPressureHistory(date)
                }
                picker.show(it.supportFragmentManager, picker.toString())
            }
        },
        InstructionItem("Sync Respiration Rate Record", "Sync History") {
            activity?.let {
                val picker = MaterialDatePicker.Builder.datePicker().build()
                picker.addOnPositiveButtonClickListener { date ->
                    bleViewModel.getRespirationRateHistory(date)
                }
                picker.show(it.supportFragmentManager, picker.toString())
            }
        },
        InstructionItem("Sync Blood Oxygen Record", "Sync History") {
            activity?.let {
                val picker = MaterialDatePicker.Builder.datePicker().build()
                picker.addOnPositiveButtonClickListener { date ->
                    bleViewModel.getBloodOxygenHistory(date)
                }
                picker.show(it.supportFragmentManager, picker.toString())
            }
        },
        InstructionItem("Sync Pressure Record", "Sync History") {
            activity?.let {
                val picker = MaterialDatePicker.Builder.datePicker().build()
                picker.addOnPositiveButtonClickListener { date ->
                    bleViewModel.getPressureHistory(date)
                }
                picker.show(it.supportFragmentManager, picker.toString())
            }
        },
        InstructionItem("Sync MET Record", "Sync History") {
            activity?.let {
                val picker = MaterialDatePicker.Builder.datePicker().build()
                picker.addOnPositiveButtonClickListener { date ->
                    bleViewModel.getMetHistory(date)
                }
                picker.show(it.supportFragmentManager, picker.toString())
            }
        },
        InstructionItem("Sync Temperature Record", "Sync History") {
            activity?.let {
                val picker = MaterialDatePicker.Builder.datePicker().build()
                picker.addOnPositiveButtonClickListener { date ->
                    bleViewModel.getTempHistory(date)
                }
                picker.show(it.supportFragmentManager, picker.toString())
            }
        },
        InstructionItem("Sync MAI", "Sync History") {
            activity?.let {
                val picker = MaterialDatePicker.Builder.datePicker().build()
                picker.addOnPositiveButtonClickListener { date ->
                    bleViewModel.getMaiHistory(date)
                }
                picker.show(it.supportFragmentManager, picker.toString())
            }
        },
        InstructionItem("Sync Blood Sugar", "Sync History") {
            activity?.let {
                val picker = MaterialDatePicker.Builder.datePicker().build()
                picker.addOnPositiveButtonClickListener { date ->
                    bleViewModel.getBloodSugarHistory(date)
                }
                picker.show(it.supportFragmentManager, picker.toString())
            }
        },
        InstructionItem("Sync Sleep Record", "Sync History") {
            activity?.let {
                val picker = MaterialDatePicker.Builder.datePicker().build()
                picker.addOnPositiveButtonClickListener { date ->
                    bleViewModel.getSleepHistory(date)
                }
                picker.show(it.supportFragmentManager, picker.toString())
            }
        },
        InstructionItem("Sync Raw Sleep Data", "Sync History") {
            activity?.let {
                val picker = MaterialDatePicker.Builder.datePicker().build()
                picker.addOnPositiveButtonClickListener { date ->
                    bleViewModel.getOriginSleepHistory(date)
                }
                picker.show(it.supportFragmentManager, picker.toString())
            }
        },
        InstructionItem("Sync Step Count Valid Dates", "Sync History") {
            bleViewModel.getValidHistoryDates()
        },
        InstructionItem("Sync Sleep Valid Dates", "Sync History") {
            bleViewModel.getSleepValidHistoryDates()
        },
        InstructionItem("Sync MET Valid Dates", "Sync History") {
            bleViewModel.getMetValidHistoryDates()
        },
        InstructionItem("Sync Mai Valid Dates", "Sync History") {
            bleViewModel.getMaiValidHistoryDates()
        },
        InstructionItem("Sync Blood Sugar Valid Dates", "Sync History") {
            bleViewModel.getBloodSugarValidHistoryDates()
        },
        InstructionItem("Sync Blood Oxygen Valid Dates", "Sync History") {
            bleViewModel.getBloodOxygenValidHistoryDates()
        },
        InstructionItem("Sync Head Shaking Valid Dates", "Sync History") {
            bleViewModel.getShakeHeadValidHistoryDates()
        },
        InstructionItem("Sync Sport", "Sync History") {
            homeViewModel.toggleSportSyncToDeviceDialog()
        },
        InstructionItem("Sync Sport Record", "Sync History") {
            bleViewModel.getSportHistory()
        },
        InstructionItem("Sync Step Count Sleep Record", "Sync History") {
            activity?.let {
                val picker = MaterialDatePicker.Builder.datePicker().build()
                picker.addOnPositiveButtonClickListener { date ->
                    bleViewModel.getStepHistory(date)
                }
                picker.show(it.supportFragmentManager, picker.toString())
            }
        },

        // ═══════════════════════════════════════════════════════
        // GET DEVICE INFO
        // ═══════════════════════════════════════════════════════
        InstructionItem("Get Drink Water Reminder", "Get Device Info") {
            bleViewModel.getDrinkWater()
        },
        InstructionItem("Get Sport Mode", "Get Device Info") {
            bleViewModel.getSportMode()
        },
        InstructionItem("Get Watch Mode", "Get Device Info") {
            homeViewModel.toggleCustomDeviceModeDialog()
        },
        InstructionItem("Get NFC Card Info", "Get Device Info") {
            bleViewModel.getNfcCardInfo()
        },
        InstructionItem("Get BT Status", "Get Device Info") {
            bleViewModel.getBtStatus()
        },
        InstructionItem("Get Power", "Get Device Info") {
            bleViewModel.getPower()
        },
        InstructionItem("Get Version Info", "Get Device Info") {
            bleViewModel.getVersion(true)
        },
        InstructionItem("Get Supported Languages", "Get Device Info") {
            bleViewModel.getSupportLanguages()
        },
        InstructionItem("Get Time Zone", "Get Device Info") {
            bleViewModel.getTimeOffset()
        },
        InstructionItem("Get Daylight Saving Time", "Get Device Info") {
            bleViewModel.getSummerWorldClock()
        },
        InstructionItem("Get Current Health Data", "Get Device Info") {
            bleViewModel.getHealthDetail()
        },
        InstructionItem("Get Watch Daily Data", "Get Device Info") {
            bleViewModel.getCustomDeviceDailyData()
        },
        InstructionItem("Get Watch Vibration Duration", "Get Device Info") {
            homeViewModel.toggleCustomDeviceShakeTimeDialog()
        },
        InstructionItem("Get Watch Name", "Get Device Info") {
            homeViewModel.toggleCustomDeviceNameDialog()
        },
        InstructionItem("Get Shake Times", "Get Device Info") {
            bleViewModel.getCustomDeviceShakeTimes()
        },
        InstructionItem("Get Shake On/Off Switch", "Get Device Info") {
            homeViewModel.toggleCustomDeviceShakeOnOffDialog()
        },
        InstructionItem("Get Broadcast Interval", "Get Device Info") {
            homeViewModel.toggleCustomBroadcastDialog()
        },
        InstructionItem("Get File System", "Get Device Info") {
            homeViewModel.toggleFileSystemOpen()
        },
        InstructionItem("Get Dial Info", "Get Device Info") {
            bleViewModel.bleDevice?.get()?.let { bleViewModel.getDialInfo() }
        },
        InstructionItem("Get Alarm Clock", "Get Device Info") {
            bleViewModel.getClock()
        },
        InstructionItem("Get Log", "Get Device Info") {
            bleViewModel.bleDevice?.get()?.let { bleViewModel.getLog() }
        },
        // ═══════════════════════════════════════════════════════
        // SET DEVICE SETTINGS
        // ═══════════════════════════════════════════════════════
        InstructionItem("Set Server", "Set Device Settings") {
            homeViewModel.toggleSetNet()
        },
        InstructionItem("Set Status", "Set Device Settings") {
            homeViewModel.toggleSetState()
        },
        InstructionItem("Set Time Zone", "Set Device Settings") {
            bleViewModel.setTimeOffset()
        },
        InstructionItem("Set Daylight Saving Time", "Set Device Settings") {
            bleViewModel.setSummerWorldClock()
        },
        InstructionItem("Set User Info", "Set Device Settings") {
            homeViewModel.toggleUserInfo()
        },
        InstructionItem("Set Daily Goal", "Set Device Settings") {
            homeViewModel.toggleGoals()
        },
        InstructionItem("Set Frequent Contacts", "Set Device Settings") {
            homeViewModel.toggleContactOpen()
        },
        InstructionItem("Set Emergency Contacts", "Set Device Settings") {
            homeViewModel.toggleSosOpen()
        },
        InstructionItem("Set Do Not Disturb", "Set Device Settings") {
            homeViewModel.toggleNotDisturbOpen()
        },
        InstructionItem("Set Sleep Plan", "Set Device Settings") {
            bleViewModel.setSleepClock()
        },
        InstructionItem("Set Sport Mode", "Set Device Settings") {
            bleViewModel.setSportMode()
        },
        InstructionItem("Set Password", "Set Device Settings") {
            homeViewModel.togglePasswordOpen()
        },
        InstructionItem("Set Female Health", "Set Device Settings") {
            homeViewModel.toggleFemaleHealthOpen()
        },
        InstructionItem("Set Sedentary Reminder", "Set Device Settings") {
            bleViewModel.setLongSit()
        },
        InstructionItem("Set Drink Water Reminder", "Set Device Settings") {
            bleViewModel.setDrinkWater()
        },

        // ═══════════════════════════════════════════════════════
        // DEVICE CONTROL
        // ═══════════════════════════════════════════════════════
        InstructionItem("Keep Foreground Running", "Device Control") {
            bleViewModel.bindService()
        },
        InstructionItem("Disconnect Foreground Running", "Device Control") {
            bleViewModel.unbindService()
        },
        InstructionItem("Volume", "Device Control") {
            homeViewModel.toggleVolume()
        },
        InstructionItem("Pair Command", "Device Control") {
            bleViewModel.pair()
        },
        InstructionItem("Bluetooth Broadcast Data Update On", "Device Control") {
            bleViewModel.setBroadcastOnOff(true)
        },
        InstructionItem("Bluetooth Broadcast Data Update Off", "Device Control") {
            bleViewModel.setBroadcastOnOff(false)
        },
        InstructionItem("Pair Command (GTS10) Pop-up", "Device Control") {
            bleViewModel.pairGts10(0)
        },
        InstructionItem("Pair Command (GTS10) No Pop-up", "Device Control") {
            bleViewModel.pairGts10(1)
        },
        InstructionItem("GTS10 Two-way Pairing", "Device Control") {
            scope.launch { navController.navigate(NavPage.Gts10PairPage.name) }
        },
        InstructionItem("Find Device", "Device Control") {
            bleViewModel.findDevice(true)
        },
        InstructionItem("Stop Finding", "Device Control") {
            bleViewModel.findDevice(false)
        },
        InstructionItem("Camera Control", "Device Control") {
            homeViewModel.toggleCamera()
        },
        InstructionItem("Call Control", "Device Control") {
            homeViewModel.toggleCall()
        },
        InstructionItem("Sync Time", "Device Control") {
            bleViewModel.setTime()
        },
        InstructionItem("Send Message", "Device Control") {
            homeViewModel.toggleMessageOpen()
        },
        InstructionItem("App Store", "Device Control") {
            homeViewModel.toggleAppOpen()
        },
        InstructionItem("World Clock", "Device Control") {
            homeViewModel.toggleWorldClockOpen()
        },
        InstructionItem("Time Format", "Device Control") {
            homeViewModel.toggleDateFormatDialog()
        },
        InstructionItem("Power Consumption Mode", "Device Control") {
            scope.launch { navController.navigate(NavPage.QuickBatteryModePage.name) }
        },
        InstructionItem("Unit Test", "Device Control") {
            bleViewModel.bleDevice?.get()?.let { bleViewModel.unittest() }
        },
        InstructionItem("Restore Factory Settings", "Device Control") {
            bleViewModel.bleDevice?.get()?.let { bleViewModel.reset() }
        },
        InstructionItem("Shut Down", "Device Control") {
            bleViewModel.bleDevice?.get()?.let { bleViewModel.close() }
        },
        InstructionItem("Open Heart Rate Switch", "Device Control") {
            homeViewModel.toggleHeartRateOpen()
        },
        InstructionItem("GTS10 Heart Rate Interval", "Device Control") {
            scope.launch { navController.navigate(NavPage.Gts10HealthIntervalPage.name) }
        },
        InstructionItem("Enable Pressure Measurement", "Device Control") {
            bleViewModel.sendHealthMeasure(true)
        },
        InstructionItem("Disable Pressure Measurement", "Device Control") {
            bleViewModel.sendHealthMeasure(false)
        },
        InstructionItem("Enable Heart Rate Measurement", "Device Control") {
            bleViewModel.sendHeartRateHealthMeasure(true)
        },
        InstructionItem("Disable Heart Rate Measurement", "Device Control") {
            bleViewModel.sendHeartRateHealthMeasure(false)
        },
        InstructionItem("Enable Demo Mode", "Device Control") {
            bleViewModel.setDisplayMode(true)
        },
        InstructionItem("Disable Demo Mode", "Device Control") {
            bleViewModel.setDisplayMode(false)
        },
        InstructionItem("Enable Shipping Mode", "Device Control") {
            bleViewModel.setShipMode(true)
        },
        InstructionItem("Custom Switch", "Device Control") {
            homeViewModel.toggleCustomDialog()
        },
        InstructionItem("Sport Mode On/Off Switch", "Device Control") {
            homeViewModel.toggleSportModeOnOffDialog()
        },

        // ═══════════════════════════════════════════════════════
        // WEATHER & TIME
        // ═══════════════════════════════════════════════════════
        InstructionItem("Send Weather (4 Days)", "Weather & Time") {
            bleViewModel.setWeather()
        },
        InstructionItem("Send Weather (7 Days)", "Weather & Time") {
            bleViewModel.setWeatherSeven()
        },
        InstructionItem("Read Weather (7 Days)", "Weather & Time") {
            bleViewModel.getWeatherSeven()
        },
        InstructionItem("Send Ephemeris (Domestic)", "Weather & Time") {
            bleViewModel.bleDevice?.get()?.let { bleViewModel.sendPgl(true) }
        },
        InstructionItem("Send Ephemeris (Foreign)", "Weather & Time") {
            bleViewModel.bleDevice?.get()?.let { bleViewModel.sendPgl(false) }
        },

        // ═══════════════════════════════════════════════════════
        // DIAL & UI CUSTOMIZATION
        // ═══════════════════════════════════════════════════════
        InstructionItem("Send Custom Dial", "Dial & UI") {
            bleViewModel.bleDevice?.get()?.let {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.type = "image/*"
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                selectImageLauncher.launch(intent)
            }
        },
        InstructionItem("Send Custom Dial V2", "Dial & UI") {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "image/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            selectImageV2Launcher.launch(intent)
        },
        InstructionItem("Send Dial (Local)", "Dial & UI") {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            selectDialLauncher.launch(intent)
        },
        InstructionItem("Send Dial V2 (Local)", "Dial & UI") {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            selectDialV2Launcher.launch(intent)
        },
        InstructionItem("Send Logo (Local)", "Dial & UI") {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            selectLogoLauncher.launch(intent)
        },
        InstructionItem("Send File V2 (Local)", "Dial & UI") {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            selectFileV2Launcher.launch(intent)
        },
    )
}




//package com.starmax.sdkdemo.pages
//
//import android.content.Intent
//import android.util.Log
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.appcompat.app.AppCompatActivity
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.ExperimentalLayoutApi
//import androidx.compose.foundation.layout.FlowRow
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.wrapContentWidth
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.rememberLazyListState
//import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.sharp.KeyboardArrowLeft
//import androidx.compose.material.icons.sharp.KeyboardArrowLeft
//import androidx.compose.material.icons.twotone.Search
//import androidx.compose.material3.Button
//import androidx.compose.material3.CenterAlignedTopAppBar
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.OutlinedTextFieldDefaults.contentPadding
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavController
//import com.google.android.material.datepicker.MaterialDatePicker
//import com.starmax.sdkdemo.NavPage
//import com.starmax.sdkdemo.ui.theme.AppTheme
//import com.starmax.sdkdemo.viewmodel.BleViewModel
//import com.starmax.sdkdemo.viewmodel.HomeViewModel
//import com.starmax.sdkdemo.viewmodel.InstructionListViewModel
//import com.starmax.sdkdemo.viewmodel.OtaType
//import com.starmax.sdkdemo.viewmodel.OtaViewModel
//import kotlinx.coroutines.launch
//
//
//@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
//@Composable
//fun InstructionListPage(navController: NavController,viewModel: InstructionListViewModel = viewModel()){
//
//    val lazyListState = rememberLazyListState()
//
//    val homeViewModel: HomeViewModel by lazyKoinViewModel()
//    val bleViewModel: BleViewModel by lazyKoinViewModel()
//    val otaViewModel: OtaViewModel by lazyKoinViewModel()
//    val scope = rememberCoroutineScope()
//
//    val context = LocalContext.current
//    val activity = context as? AppCompatActivity
//
//
//
//    val selectOtaLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
//            val uri = it.data?.data
//            if(uri != null){
//                otaViewModel.getFromPath(bleViewModel.bleDevice!!.get()!!, uri)
//            }
//            scope.launch {
//                navController.popBackStack()
//            }
//        }
//
//    val selectGts7FirmwareLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
//        val uri = it.data?.data
//        bleViewModel.binUri = uri
//        bleViewModel.sendGts7FirmwareLocal(context)
//        scope.launch {
//            navController.popBackStack()
//        }
//    }
//
//    val selectUiLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
//            val uri = it.data?.data
//            if(uri != null){
//                bleViewModel.sendUiLocal(context,uri)
//            }
//            scope.launch {
//                navController.popBackStack()
//            }
//        }
//
//    val selectGts7CrcLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
//        val uri = it.data?.data
//        bleViewModel.binUri = uri
//        bleViewModel.sendGts7CrcLocal(context)
//        scope.launch {
//            navController.popBackStack()
//        }
//    }
//    val selectBinLauncher =
//        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
//            val uri = it.data?.data
//            bleViewModel.binUri = uri
//            bleViewModel.sendCustomDial(context)
//            scope.launch {
//                navController.popBackStack()
//            }
//        }
//
//    val selectImageLauncher =
//        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
//            val uri = it.data?.data
//            bleViewModel.imageUri = uri
//            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//            intent.type = "*/*"
//            intent.addCategory(Intent.CATEGORY_OPENABLE)
//            selectBinLauncher.launch(intent)
//            scope.launch {
//                navController.popBackStack()
//            }
//        }
//
//    val selectBinV2Launcher =
//        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
//            val uri = it.data?.data
//            Log.d("ActivityResultBin", uri.toString());
//            bleViewModel.binUri = uri
//            bleViewModel.sendCustomDialV2Local(context)
//            scope.launch {
//                navController.popBackStack()
//            }
//        }
//
//    val selectImageV2Launcher =
//        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
//            val uri = it.data?.data
//            Log.d("ActivityResultImage", uri.toString());
//            bleViewModel.imageUri = uri
//            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//            intent.type = "*/*"
//            intent.addCategory(Intent.CATEGORY_OPENABLE)
//            selectBinV2Launcher.launch(intent)
////            scope.launch {
////                navController.popBackStack()
////            }
//        }
//
//    val selectDialLauncher =
//        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
//            val uri = it.data?.data
//            bleViewModel.binUri = uri
//            bleViewModel.sendDialLocal(context)
//            scope.launch {
//                navController.popBackStack()
//            }
//        }
//
//    val selectLogoLauncher =
//        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
//            val uri = it.data?.data
//            bleViewModel.binUri = uri
//            bleViewModel.sendLogoLocal(context)
//            scope.launch {
//                navController.popBackStack()
//            }
//        }
//    val selectFileV2Launcher =
//        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
//            val uri = it.data?.data
//            if(uri != null){
//                bleViewModel.sendFileV2LocalByDiffMd5(context,uri)
//            }
//            scope.launch {
//                navController.popBackStack()
//            }
//        }
//
//    val selectDialV2Launcher =
//        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
//            val uri = it.data?.data
//            if(uri != null){
//                bleViewModel.binUri = uri
//                bleViewModel.sendDialV2Local(context,uri)
//            }
//            scope.launch {
//                navController.popBackStack()
//            }
//        }
//
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
//                            Icon(Icons.Sharp.KeyboardArrowLeft, contentDescription = "Return")
//                        }
//                    },
//                )
//            }
//        ){
//                innerPadding ->
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
//                                    onClick = {
//                                        // Due to Composable limitations, a new method cannot be created
////                                        val instruction = mInstruction.trim()
////                                        val instruction = mInstruction.replace(" ", "")
//
//                                        var isPopBackStack = true;
//
//                                        when(instruction){
//                                            "Connect OTA Bluetooth" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    otaViewModel.connect(bleViewModel.bleDevice?.get())
//                                                }
//                                            }
//                                            "Realtek OTA Upgrade" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    otaViewModel.otaType = OtaType.Real
//                                                    otaViewModel.download(
//                                                        bleViewModel.bleDevice!!.get()!!,
//                                                        bleViewModel.bleModel,
//                                                        bleViewModel.bleVersion
//                                                    )
//                                                }
//                                            }
//                                            "Broadcom OTA Upgrade" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    otaViewModel.otaType = OtaType.BK
//                                                    otaViewModel.download(
//                                                        bleViewModel.bleDevice!!.get()!!,
//                                                        bleViewModel.bleModel,
//                                                        bleViewModel.bleVersion
//                                                    )
//                                                }
//                                            }
//                                            "Jieli OTA Upgrade (Local)" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//                                                    intent.type = "*/*"
//                                                    intent.addCategory(Intent.CATEGORY_OPENABLE)
//                                                    otaViewModel.otaType = OtaType.JieLi
//                                                    selectOtaLauncher.launch(intent)
//                                                    isPopBackStack = false
//                                                }
//                                            }
//                                            "X03 Differential Upgrade (Local)" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//                                                    intent.type = "*/*"
//                                                    intent.addCategory(Intent.CATEGORY_OPENABLE)
//                                                    selectGts7FirmwareLauncher.launch(intent)
//                                                    isPopBackStack = false
//                                                }
//                                            }
//                                            "Realtek OTA Upgrade (Local)" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//                                                    intent.type = "*/*"
//                                                    intent.addCategory(Intent.CATEGORY_OPENABLE)
//                                                    otaViewModel.otaType = OtaType.Real
//                                                    selectOtaLauncher.launch(intent)
//                                                    isPopBackStack = false
//                                                }
//                                            }
//                                            "Broadcom OTA Upgrade (Local)" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//                                                    intent.type = "*/*"
//                                                    intent.addCategory(Intent.CATEGORY_OPENABLE)
//                                                    otaViewModel.otaType = OtaType.BK
//                                                    selectOtaLauncher.launch(intent)
//                                                    isPopBackStack = false
//                                                }
//                                            }
//                                            "Jieli Upgrade" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    otaViewModel.otaType = OtaType.JieLi
//                                                    otaViewModel.download(
//                                                        bleViewModel.bleDevice!!.get()!!,
//                                                        bleViewModel.bleModel,
//                                                        bleViewModel.bleVersion
//                                                    )
//                                                }
//                                            }
//                                            "Sifli OTA Upgrade (Local)" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//                                                    intent.type = "*/*"
//                                                    intent.addCategory(Intent.CATEGORY_OPENABLE)
//                                                    otaViewModel.otaType = OtaType.Sifli
//                                                    selectOtaLauncher.launch(intent)
//                                                    isPopBackStack = false
//                                                }
//                                            }
//                                            "Send UI" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    bleViewModel.sendUi()
//                                                }
//                                            }
//                                            "Send UI (Differential Upgrade)" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    bleViewModel.sendUiDiff()
//                                                }
//                                            }
//                                            "Send UI (Local)" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//                                                    intent.type = "*/*"
//                                                    intent.addCategory(Intent.CATEGORY_OPENABLE)
//                                                    selectUiLauncher.launch(intent)
//                                                    isPopBackStack = false
//                                                }
//                                            }
//                                            "Differential Upgrade CRC Check (Local)" -> {
//                                                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//                                                intent.type = "*/*"
//                                                intent.addCategory(Intent.CATEGORY_OPENABLE)
//                                                selectGts7CrcLauncher.launch(intent)
//                                                isPopBackStack = false
//                                            }
//                                            "Sync Head Shaking Record" -> {
//                                                activity?.let {
//                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
//                                                    picker.addOnPositiveButtonClickListener {
//                                                            that ->
//                                                        bleViewModel.getShakeHeadHistory(that)
//                                                    }
//                                                    picker.show(it.supportFragmentManager, picker.toString())
//                                                }
//                                            }
//                                            "Sync Heart Rate Record" -> {
//                                                activity?.let {
//                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
//                                                    picker.addOnPositiveButtonClickListener {
//                                                            that ->
//                                                        bleViewModel.getHeartRateHistory(that)
//                                                    }
//                                                    picker.show(it.supportFragmentManager, picker.toString())
//                                                }
//                                            }
//                                            "Sync Standing Count Medium-High Intensity" -> {
//                                                activity?.let {
//                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
//                                                    picker.addOnPositiveButtonClickListener {
//                                                            that ->
//                                                        bleViewModel.getExerciseHistory(that)
//                                                    }
//                                                    picker.show(it.supportFragmentManager, picker.toString())
//                                                }
//                                            }
//                                            "Sync Blood Pressure Record" -> {
//                                                activity?.let {
//                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
//                                                    picker.addOnPositiveButtonClickListener {
//                                                            that ->
//                                                        bleViewModel.getBloodPressureHistory(that)
//                                                    }
//                                                    picker.show(it.supportFragmentManager, picker.toString())
//                                                }
//                                            }
//                                            "Sync Respiration Rate Record" -> {
//                                                activity?.let {
//                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
//                                                    picker.addOnPositiveButtonClickListener {
//                                                            that ->
//                                                        bleViewModel.getRespirationRateHistory(that)
//                                                    }
//                                                    picker.show(it.supportFragmentManager, picker.toString())
//                                                }
//                                            }
//                                            "Sync Blood Oxygen Record" -> {
//                                                activity?.let {
//                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
//                                                    picker.addOnPositiveButtonClickListener { that ->
//                                                        bleViewModel.getBloodOxygenHistory(that)
//                                                    }
//                                                    picker.show(it.supportFragmentManager, picker.toString())
//                                                }
//                                            }
//                                            "Sync Pressure Record" -> {
//                                                activity?.let {
//                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
//                                                    picker.addOnPositiveButtonClickListener {
//                                                            that ->
//                                                        bleViewModel.getPressureHistory(that)
//                                                    }
//                                                    picker.show(it.supportFragmentManager, picker.toString())
//                                                }
//                                            }
//                                            "Sync MET Record" -> {
//                                                activity?.let {
//                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
//                                                    picker.addOnPositiveButtonClickListener {
//                                                            that ->
//                                                        bleViewModel.getMetHistory(that)
//                                                    }
//                                                    picker.show(it.supportFragmentManager, picker.toString())
//                                                }
//                                            }
//                                            "Sync Temperature Record" -> {
//                                                activity?.let {
//                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
//                                                    picker.addOnPositiveButtonClickListener {
//                                                            that ->
//                                                        bleViewModel.getTempHistory(that)
//                                                    }
//                                                    picker.show(it.supportFragmentManager, picker.toString())
//                                                }
//                                            }
//                                            "Sync MAI" -> {
//                                                activity?.let {
//                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
//                                                    picker.addOnPositiveButtonClickListener {
//                                                            that ->
//                                                        bleViewModel.getMaiHistory(that)
//                                                    }
//                                                    picker.show(it.supportFragmentManager, picker.toString())
//                                                }
//                                            }
//                                            "Sync Blood Sugar" -> {
//                                                activity?.let {
//                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
//                                                    picker.addOnPositiveButtonClickListener {
//                                                            that ->
//                                                        bleViewModel.getBloodSugarHistory(that)
//                                                    }
//                                                    picker.show(it.supportFragmentManager, picker.toString())
//                                                }
//                                            }
//                                            "Sync Sleep Record" -> {
//                                                activity?.let {
//                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
//                                                    picker.addOnPositiveButtonClickListener {
//                                                            that ->
//                                                        bleViewModel.getSleepHistory(that)
//                                                    }
//                                                    picker.show(it.supportFragmentManager, picker.toString())
//                                                }
//                                            }
//                                            "Sync Raw Sleep Data" -> {
//                                                activity?.let {
//                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
//                                                    picker.addOnPositiveButtonClickListener {
//                                                            that ->
//                                                        bleViewModel.getOriginSleepHistory(that)
//                                                    }
//                                                    picker.show(it.supportFragmentManager, picker.toString())
//                                                }
//                                            }
//                                            "Sync Step Count Valid Dates" -> bleViewModel.getValidHistoryDates()
//                                            "Sync Sleep Valid Dates" -> bleViewModel.getSleepValidHistoryDates()
//                                            "Sync MET Valid Dates" -> bleViewModel.getMetValidHistoryDates()
//                                            "Sync Mai Valid Dates" -> bleViewModel.getMaiValidHistoryDates()
//                                            "Sync Blood Sugar Valid Dates" -> bleViewModel.getBloodSugarValidHistoryDates()
//                                            "Sync Blood Oxygen Valid Dates" -> bleViewModel.getBloodOxygenValidHistoryDates()
//                                            "Sync Head Shaking Valid Dates" -> bleViewModel.getShakeHeadValidHistoryDates()
//                                            "Sync Sport" -> homeViewModel.toggleSportSyncToDeviceDialog()
//                                            "Sync Sport Record" -> bleViewModel.getSportHistory()
//                                            "Sync Step Count Sleep Record" -> {
//                                                activity?.let {
//                                                    val picker = MaterialDatePicker.Builder.datePicker().build()
//                                                    picker.addOnPositiveButtonClickListener {
//                                                            that ->
//                                                        bleViewModel.getStepHistory(that)
//                                                    }
//                                                    picker.show(it.supportFragmentManager, picker.toString())
//                                                }
//                                            }
//                                            "Get Drink Water Reminder" -> bleViewModel.getDrinkWater()
//                                            "Get Sport Mode" -> bleViewModel.getSportMode()
//                                            "Get Watch Mode" -> homeViewModel.toggleCustomDeviceModeDialog()
//                                            "Get NFC Card Info" -> bleViewModel.getNfcCardInfo()
//                                            "Get BT Status" -> bleViewModel.getBtStatus()
//                                            "Get Power" -> bleViewModel.getPower()
//                                            "Get Version Info" -> bleViewModel.getVersion(true)
//                                            "Get Supported Languages" -> bleViewModel.getSupportLanguages()
//                                            "Get Time Zone" -> bleViewModel.getTimeOffset()
//                                            "Get Daylight Saving Time" -> bleViewModel.getSummerWorldClock()
//                                            "Get Current Health Data" -> bleViewModel.getHealthDetail()
//                                            "Get Watch Daily Data" -> bleViewModel.getCustomDeviceDailyData()
//                                            "Get Watch Vibration Duration" -> homeViewModel.toggleCustomDeviceShakeTimeDialog()
//                                            "Get Watch Name" -> homeViewModel.toggleCustomDeviceNameDialog()
//                                            "Get Shake Times" -> bleViewModel.getCustomDeviceShakeTimes()
//                                            "Get Shake On/Off Switch" -> homeViewModel.toggleCustomDeviceShakeOnOffDialog()
//                                            "Get Broadcast Interval" -> homeViewModel.toggleCustomBroadcastDialog()
//                                            "Get File System" -> homeViewModel.toggleFileSystemOpen()
//                                            "Get Dial Info" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    bleViewModel.getDialInfo()
//                                                }
//                                            }
//                                            "Get Alarm Clock" -> bleViewModel.getClock()
//                                            "Get Log" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    bleViewModel.getLog()
//                                                }
//                                            }
//                                            "Set Server" -> homeViewModel.toggleSetNet()
//                                            "Set Status" -> homeViewModel.toggleSetState()
//                                            "Set Time Zone" -> bleViewModel.setTimeOffset()
//                                            "Set Daylight Saving Time" -> bleViewModel.setSummerWorldClock()
//                                            "Set User Info" -> homeViewModel.toggleUserInfo()
//                                            "Set Daily Goal" -> homeViewModel.toggleGoals()
//                                            "Set Frequent Contacts" -> homeViewModel.toggleContactOpen()
//                                            "Set Emergency Contacts" -> homeViewModel.toggleSosOpen()
//                                            "Set Do Not Disturb" -> homeViewModel.toggleNotDisturbOpen()
//                                            "Set Sleep Plan" -> bleViewModel.setSleepClock()
//                                            "Set Sport Mode" -> bleViewModel.setSportMode()
//                                            "Set Password" -> homeViewModel.togglePasswordOpen()
//                                            "Set Female Health" -> homeViewModel.toggleFemaleHealthOpen()
//                                            "Set Sedentary Reminder" -> bleViewModel.setLongSit()
//                                            "Set Drink Water Reminder" -> bleViewModel.setDrinkWater()
//                                            "Keep Foreground Running" -> bleViewModel.bindService()
//                                            "Disconnect Foreground Running" -> bleViewModel.unbindService()
//                                            "Volume" -> homeViewModel.toggleVolume()
//                                            "Pair Command" -> bleViewModel.pair()
//                                            "Bluetooth Broadcast Data Update On"->bleViewModel.setBroadcastOnOff(true)
//                                            "Bluetooth Broadcast Data Update Off"->bleViewModel.setBroadcastOnOff(false)
//                                            "Pair Command (GTS10) Pop-up" -> bleViewModel.pairGts10(0)
//                                            "Pair Command (GTS10) No Pop-up" -> bleViewModel.pairGts10(1)
//                                            "GTS10 Two-way Pairing" -> scope.launch { navController.navigate(NavPage.Gts10PairPage.name)}
//                                            "Find Device" -> bleViewModel.findDevice(true)
//                                            "Stop Finding" -> bleViewModel.findDevice(false)
//                                            "Camera Control" -> homeViewModel.toggleCamera()
//                                            "Call Control" -> homeViewModel.toggleCall()
//                                            "Sync Time" -> bleViewModel.setTime()
//                                            "Send Message" -> homeViewModel.toggleMessageOpen()
//                                            "App Store" -> homeViewModel.toggleAppOpen()
//                                            "World Clock" -> homeViewModel.toggleWorldClockOpen()
//                                            "Time Format" -> homeViewModel.toggleDateFormatDialog()
//                                            "Power Consumption Mode" -> scope.launch { navController.navigate(NavPage.QuickBatteryModePage.name)}
//                                            "Unit Test" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    bleViewModel.unittest()
//                                                }
//                                            }
//                                            "Restore Factory Settings" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    bleViewModel.reset()
//                                                }
//                                            }
//                                            "Shut Down" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    bleViewModel.close()
//                                                }
//                                            }
//                                            "Open Heart Rate Switch" -> homeViewModel.toggleHeartRateOpen()
//                                            "GTS10 Heart Rate Interval" -> scope.launch { navController.navigate(NavPage.Gts10HealthIntervalPage.name)}
//                                            "Enable Pressure Measurement" -> bleViewModel.sendHealthMeasure(true)
//                                            "Disable Pressure Measurement" -> bleViewModel.sendHealthMeasure(false)
//                                            "Enable Heart Rate Measurement" -> bleViewModel.sendHeartRateHealthMeasure(true)
//                                            "Disable Heart Rate Measurement" -> bleViewModel.sendHeartRateHealthMeasure(false)
//                                            "Enable Demo Mode" -> bleViewModel.setDisplayMode(true)
//                                            "Disable Demo Mode" -> bleViewModel.setDisplayMode(false)
//                                            "Enable Shipping Mode" -> bleViewModel.setShipMode(true)
//                                            "Custom Switch" -> homeViewModel.toggleCustomDialog()
//                                            "Sport Mode On/Off Switch" -> homeViewModel.toggleSportModeOnOffDialog()
//                                            "Send Weather (4 Days)" -> bleViewModel.setWeather()
//                                            "Send Weather (7 Days)" -> bleViewModel.setWeatherSeven()
//                                            "Read Weather (7 Days)" -> bleViewModel.getWeatherSeven()
//                                            "Send Ephemeris (Domestic)" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    bleViewModel.sendPgl(true)
//                                                }
//                                            }
//                                            "Send Ephemeris (Foreign)" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    bleViewModel.sendPgl(false)
//                                                }
//                                            }
//                                            "Send Custom Dial" -> {
//                                                if (bleViewModel.bleDevice?.get() != null) {
//                                                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//                                                    intent.type = "image/*"
//                                                    intent.addCategory(Intent.CATEGORY_OPENABLE)
//                                                    selectImageLauncher.launch(intent)
//                                                    isPopBackStack = false
//                                                }
//                                            }
//                                            "Send Custom Dial V2" -> {
//                                                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//                                                intent.type = "image/*"
//                                                intent.addCategory(Intent.CATEGORY_OPENABLE)
//                                                // The original snippet was cut here
//                                            }
//                                        }
//
//                                        if (isPopBackStack) {
//                                            navController.popBackStack()
//                                        }
//
//                                    },
//                                    modifier = Modifier.padding(horizontal = 2.dp),
//                                    content = {
//                                        Text(text = instruction, style = MaterialTheme.typography.labelSmall)
//                                    }
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//    }

