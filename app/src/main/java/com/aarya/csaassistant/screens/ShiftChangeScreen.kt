package com.aarya.csaassistant.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
// import androidx.compose.foundation.layout.fillMaxHeight // Not used, can be removed
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
// import androidx.compose.runtime.LaunchedEffect // Not used directly here anymore
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
// import androidx.compose.runtime.mutableStateListOf // Not used, can be removed
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
// import androidx.compose.ui.tooling.preview.Preview // Not used, can be removed
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.aarya.csaassistant.ui.theme.CSAAssistantTheme
import com.aarya.csaassistant.utils.Routes
import com.aarya.csaassistant.viewmodel.AuthViewModel
import com.aarya.csaassistant.viewmodel.EmployeeViewModel
import com.aarya.csaassistant.viewmodel.EntryViewModel
import com.aarya.csaassistant.viewmodel.ImageViewModel // <-- Added ImageViewModel import


@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ShiftChangeScreen(
    navController: NavController,
    entryViewModel: EntryViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    imageViewModel: ImageViewModel = hiltViewModel(), // <-- Added ImageViewModel
    employeeViewModel: EmployeeViewModel = hiltViewModel()

) {
    val nozzles = listOf("A1", "B1", "A2", "B2", "C1", "C2")

    val nozzleColors = mapOf(
        "A1" to Color(0xFF4CAF50), // Petrol - Green
        "B1" to Color(0xFF4CAF50),
        "A2" to Color(0xFF2196F3), // Diesel - Blue
        "B2" to Color(0xFF2196F3),
        "C2" to Color(0xFF2196F3),
        "C1" to Color(0xFFF44336)  // Power - Red
    )

    var handedOverTo by remember { mutableStateOf<String?>(null) }
    val openingReadingsMap = remember { mutableStateMapOf<String, String>() }
    val closingReadingsMap = remember { mutableStateMapOf<String, String>() }
    val userData by authViewModel.userData.collectAsState()
    val employees by employeeViewModel.employees.collectAsState()

    // State for the image URLs from ImageCaptureSection
    var currentClosingImageUrls by remember { mutableStateOf<List<String>>(emptyList()) }

    var nextEnabled by remember { mutableStateOf(false) }
    if (handedOverTo != null && openingReadingsMap.isNotEmpty() && closingReadingsMap.isNotEmpty() && currentClosingImageUrls.isNotEmpty()) {
        nextEnabled = true
    } else {
        nextEnabled = false
    }

    Scaffold(
        bottomBar = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp) // Added vertical padding
                    .navigationBarsPadding()
            ) {
                val haptic = LocalHapticFeedback.current
                FilledTonalButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                        imageViewModel.clearAllImageData() // Clear images from ViewModel and Supabase
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .weight(0.6f)
                        .height(54.dp),
                    shapes = ButtonDefaults.shapes()
                ) {
                    Text(
                        "Back",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Button(
                    enabled = nextEnabled,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                        val openingReadingsForDb = mutableMapOf<String, Float>()
                        var conversionErrorOpening = false
                        openingReadingsMap.forEach { (nozzle, readingStr) ->
                            if (readingStr.isNotBlank()) {
                                readingStr.toFloatOrNull()?.let { openingReadingsForDb[nozzle] = it }
                                    ?: run {
                                        conversionErrorOpening = true
                                        Log.e("ShiftChangeScreen", "Invalid opening reading $nozzle: $readingStr")
                                    }
                            }
                        }
                        val closingReadingsForDb = mutableMapOf<String, Float>()
                        var conversionErrorClosing = false
                        closingReadingsMap.forEach { (nozzle, readingStr) ->
                            if (readingStr.isNotBlank()) {
                                readingStr.toFloatOrNull()?.let { closingReadingsForDb[nozzle] = it }
                                    ?: run {
                                        conversionErrorClosing = true
                                        Log.e("ShiftChangeScreen", "Invalid closing reading $nozzle: $readingStr")
                                    }
                            }
                        }
                        if (conversionErrorOpening || conversionErrorClosing) {
                            Log.w("ShiftChangeScreen", "Cannot proceed due to conversion error in readings.")
                            // TODO: Show a Snackbar or Toast to the user
                            return@Button
                        }
                        entryViewModel.updateShiftChangeData(
                            openingReadings = if (openingReadingsForDb.isEmpty()) null else openingReadingsForDb,
                            closingReadings = if (closingReadingsForDb.isEmpty()) null else closingReadingsForDb,
                            handedOverTo = handedOverTo,
                            currentShift = "Morning", // Placeholder
                            handedOverBy = userData?.full_name.toString(), // Consider null safety/default
                            closingImagesUrl = currentClosingImageUrls // <-- Use updated image URLs
                        )
                        imageViewModel.clearAllImageData() // <-- Clear images after submission
                        navController.navigate(Routes.SETTLEMENT)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shapes = ButtonDefaults.shapes(),
                ) {
                    Text(
                        "Next",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Shift Change",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.displaySmallEmphasized, // Ensure this style is defined
                fontWeight = FontWeight.ExtraBold
            )

            val assigneeNames = employees.filter { it.role == "CSA Assistant" } .mapNotNull { it.full_name.takeIf { name -> name.isNotBlank() } }
            ShiftAssigneeDropdown(
                assignees = assigneeNames,
                selectedAssignee = handedOverTo,
                onAssigneeSelected = { handedOverTo = it }
            )
            Spacer(Modifier.height(8.dp))

            NozzleTogglePreview(
                nozzles = nozzles,
                nozzleColors = nozzleColors,
                checkedStatesInitial = nozzles.associateWith { openingReadingsMap.containsKey(it) || closingReadingsMap.containsKey(it) },
                openingReadingsProvider = { nozzle -> openingReadingsMap[nozzle] ?: "" },
                onOpeningReadingChange = { nozzle, value ->
                    if (value.isBlank() && !closingReadingsMap.containsKey(nozzle)) {
                        openingReadingsMap.remove(nozzle)
                    } else {
                        openingReadingsMap[nozzle] = value
                    }
                },
                closingReadingsProvider = { nozzle -> closingReadingsMap[nozzle] ?: "" },
                onClosingReadingChange = { nozzle, value ->
                    if (value.isBlank() && !openingReadingsMap.containsKey(nozzle)) {
                        closingReadingsMap.remove(nozzle)
                    } else {
                        closingReadingsMap[nozzle] = value
                    }
                },
                onNozzleToggle = { nozzle, isChecked ->
                    if (!isChecked) {
                        openingReadingsMap.remove(nozzle)
                        closingReadingsMap.remove(nozzle)
                    }
                }
            )

            // Integrate ImageCaptureSection
            ImageCaptureSection(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                imageViewModel = imageViewModel, // Pass the same ViewModel instance
                onImageUrlsChanged = { urls ->
                    currentClosingImageUrls = urls
                },
                maxImages = imageViewModel.maxImages // Assuming maxImages is a public property in ImageViewModel
            )

            Spacer(Modifier.height(24.dp)) // This spacer remains
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NozzleTogglePreview(
    nozzles: List<String>,
    nozzleColors: Map<String, Color>,
    checkedStatesInitial: Map<String, Boolean>,
    openingReadingsProvider: (nozzle: String) -> String,
    onOpeningReadingChange: (nozzle: String, value: String) -> Unit,
    closingReadingsProvider: (nozzle: String) -> String,
    onClosingReadingChange: (nozzle: String, value: String) -> Unit,
    onNozzleToggle: (nozzle: String, isChecked: Boolean) -> Unit
) {
    CSAAssistantTheme { // Consider if CSAAssistantTheme is needed here or at a higher level
        Surface(modifier = Modifier.fillMaxWidth()) { // Added fillMaxWidth for better layout
            val haptic = LocalHapticFeedback.current

            val checkedStates = remember {
                mutableStateMapOf<String, Boolean>().apply {
                    putAll(checkedStatesInitial)
                    nozzles.forEach { nozzle ->
                        this.putIfAbsent(nozzle, false)
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Select Nozzles",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))

                Row(
                    Modifier
                        .padding(horizontal = 8.dp)
                        .fillMaxWidth(), // Ensure Row takes full width for button distribution
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                ) {
                    nozzles.forEachIndexed { index, nozzle ->
                        val isChecked = checkedStates[nozzle] ?: false
                        val color = nozzleColors[nozzle] ?: MaterialTheme.colorScheme.primary

                        ToggleButton(
                            checked = isChecked,
                            onCheckedChange = { newCheckedState ->
                                checkedStates[nozzle] = newCheckedState
                                onNozzleToggle(nozzle, newCheckedState)
                                haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ToggleButtonDefaults.toggleButtonColors(
                                checkedContentColor = MaterialTheme.colorScheme.onPrimary,
                                checkedContainerColor = color
                            ),
                            shapes =
                            when (index) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                nozzles.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            }
                        ) {
                            Text(nozzle)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                val selectedNozzlesForUI = nozzles.filter { checkedStates[it] == true }

                if (selectedNozzlesForUI.isNotEmpty()) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Opening Readings",
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Closing Readings",
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(4.dp)) // Added space before text fields
                }

                selectedNozzlesForUI.forEach { nozzle ->
                    val color = nozzleColors[nozzle] ?: MaterialTheme.colorScheme.primary
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 8.dp) // Space between nozzle input rows
                    ) {
                        OutlinedTextField(
                            value = openingReadingsProvider(nozzle),
                            onValueChange = { newValue -> onOpeningReadingChange(nozzle, newValue) },
                            label = { Text("$nozzle opening") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = color,
                                focusedLabelColor = color
                            ),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = closingReadingsProvider(nozzle),
                            onValueChange = { newValue -> onClosingReadingChange(nozzle, newValue) },
                            label = { Text("$nozzle closing") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = color,
                                focusedLabelColor = color
                            ),
                            singleLine = true
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Already present, just noting for dropdown
@Composable
fun ShiftAssigneeDropdown(
    assignees: List<String>,
    selectedAssignee: String?,
    onAssigneeSelected: (String) -> Unit,
    modifier: Modifier = Modifier // Not strictly used here but good practice
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.padding(horizontal = 16.dp) // Apply padding to the Box
    ) {
        OutlinedTextField(
            readOnly = true,
            value = selectedAssignee ?: "",
            onValueChange = { },
            label = { Text("Next Shift Assignee") },
            shape = RoundedCornerShape(16.dp),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
            // .padding(horizontal = 16.dp) // Padding moved to parent Box
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth() // Make dropdown menu fill width relative to anchor
        ) {
            assignees.forEach { assignee ->
                DropdownMenuItem(
                    text = { Text(assignee) },
                    onClick = {
                        onAssigneeSelected(assignee)
                        expanded = false
                    }
                )
            }
            if (assignees.isEmpty()){
                 DropdownMenuItem(
                    text = { Text("No users available") },
                    onClick = { expanded = false },
                    enabled = false
                )
            }
        }
    }
}
