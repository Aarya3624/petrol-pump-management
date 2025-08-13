package com.aarya.csaassistant.screens

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.activity.result.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.RemoveCircleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconButtonShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.aarya.csaassistant.ui.theme.CSAAssistantTheme
import com.aarya.csaassistant.utils.ProductSalesSummary
import com.aarya.csaassistant.utils.ProductType
import com.aarya.csaassistant.utils.getProductInfoByNozzleName
import com.aarya.csaassistant.utils.productDetails
import com.aarya.csaassistant.viewmodel.EntryViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.abs

data class CreditEntry(val crediteeName: String, val amount: Float, val id: String = UUID.randomUUID().toString())

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettlementPage(
    navController: NavController,
    viewModel: EntryViewModel
) {
    val entryData by viewModel.newEntryState.collectAsStateWithLifecycle()
    // State to hold the calculated sale summaries
    var salesSummaries by remember { mutableStateOf<Map<ProductType, ProductSalesSummary>>(emptyMap()) }
    var overallTotalSale by remember { mutableStateOf(0f) }

    LaunchedEffect(entryData.opening_readings, entryData.closing_readings) {
        val openingReadings = entryData.opening_readings
        val closingReadings = entryData.closing_readings

        if (openingReadings == null || closingReadings == null) {
            salesSummaries = emptyMap()
            overallTotalSale = 0f
            return@LaunchedEffect
        }

        val currentSummaries = mutableMapOf<ProductType, ProductSalesSummary>()
        productDetails.forEach { prodInfo -> // Ensure productDetails is accessible here
            currentSummaries[prodInfo.type] = ProductSalesSummary(productInfo = prodInfo)
        }
        var currentOverallTotal = 0f
        val allNozzles = (openingReadings.keys + closingReadings.keys).distinct()

        allNozzles.forEach { nozzleId ->
            val opening = openingReadings[nozzleId]
            val closing = closingReadings[nozzleId]

            if (opening != null && closing != null && closing > opening) {
                val productInfo = getProductInfoByNozzleName(nozzleId) // Ensure this function is accessible
                if (productInfo != null) {
                    val quantitySold = closing - opening
                    val amountForNozzle = quantitySold * productInfo.pricePerUnit
                    val summary = currentSummaries[productInfo.type]
                    if (summary != null) {
                        summary.totalQuantity += quantitySold
                        summary.totalAmount += amountForNozzle
                        currentOverallTotal += amountForNozzle
                    }
                }
            } else if (opening != null && closing != null && closing <= opening) {
                Log.w("SettlementPage", "Closing reading not greater for $nozzleId. Op: $opening, Cl: $closing")
            }
        }
        salesSummaries = currentSummaries
        overallTotalSale = currentOverallTotal
        Log.d("SettlementPage", "Calculated Sales Summaries: $salesSummaries, Overall Total: $overallTotalSale")
    }


    CSAAssistantTheme {
        var showBottomSheet by remember { mutableStateOf(false) }
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )

        var hpPayAmountString by remember { mutableStateOf("") }
        var upiCardAmountString by remember { mutableStateOf("") }


        var creditEntries by remember { mutableStateOf<List<CreditEntry>>(emptyList()) }

        var crediteeNameInput by remember { mutableStateOf("") }
        var creditAmountInput by remember { mutableStateOf("") }

        val scope = rememberCoroutineScope()

        val hpPayAmount by remember(hpPayAmountString) {
            derivedStateOf { hpPayAmountString.toFloatOrNull() ?: 0f }
        }
        val upiCardAmount by remember(upiCardAmountString) {
            derivedStateOf { upiCardAmountString.toFloatOrNull() ?: 0f }
        }
        val totalCreditAmount by remember(creditEntries) {
            derivedStateOf { creditEntries.sumOf { it.amount.toDouble() }.toFloat() } // sumOf returns Double
        }

        // Cash Handling
        val expectedCashpayable by remember(overallTotalSale, hpPayAmount, upiCardAmount, totalCreditAmount) {
            derivedStateOf {
                (overallTotalSale - hpPayAmount - upiCardAmount - totalCreditAmount)
                    .coerceAtLeast(0f)
            }
        }

        var actualCashRemittedString by remember { mutableStateOf("") }
        val actualCashRemitted: Float? by remember(actualCashRemittedString) {
            derivedStateOf { actualCashRemittedString.toFloatOrNull() }
        }
        val balanceShortOrExcess by remember(actualCashRemitted, expectedCashpayable) {
            derivedStateOf {
                val cashConsideredForBalance = actualCashRemitted ?: expectedCashpayable
                cashConsideredForBalance - expectedCashpayable
            }
        }

        // Other Details
        var paytmCardNumberString by remember { mutableStateOf("") }
        var closingImagePrimaryUrlString by remember { mutableStateOf("") }

        // For UI Feedback on submission
        val context = LocalContext.current
        val addEntryResult by viewModel.addEntryResult.collectAsStateWithLifecycle()

        var isLoading by remember { mutableStateOf(false) }

        var showConfirmationDialog by remember { mutableStateOf(false) }

        LaunchedEffect(addEntryResult) {
            val result = addEntryResult ?: return@LaunchedEffect

            if (isLoading) { // Only proceed if we were actually in a loading state
                isLoading = false // Reset loading state once result is processed
                if (result.isSuccess) {
                    Toast.makeText(context, "Settlement Submitted Successfully!", Toast.LENGTH_LONG).show()
                    navController.popBackStack(navController.graph.startDestinationId, inclusive = false)
                } else { // isFailure
                    val errorMessage = result.exceptionOrNull()?.message ?: "An unknown error occurred."
                    Toast.makeText(context, "Submission Failed: $errorMessage", Toast.LENGTH_LONG).show()
                    Log.e("SettlementPage", "Submission failed", result.exceptionOrNull())
                }
                viewModel.resetAddEntryResult() // Reset for next time
            }
        }

        Scaffold(
            bottomBar = { // Use the bottomBar slot for fixed bottom content
                Row(
                    verticalAlignment = Alignment.CenterVertically, // Center buttons vertically
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth() // Make the Row take full width
                        .padding(horizontal = 16.dp)
                        .navigationBarsPadding()// Add padding
                ) {
                    val haptic = LocalHapticFeedback.current
                    FilledTonalButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                            navController.popBackStack()
                        },
                        modifier = Modifier
                            .weight(0.6f)
                            .height(54.dp), // Adjust weight as needed
                        shapes = ButtonDefaults.shapes() // Consider consistent shape usage
                    ) {
                        Text(
                            "Back",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                            showConfirmationDialog = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp), // Adjust weight as needed
                        shapes = ButtonDefaults.shapes()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Submit",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (showConfirmationDialog) {
                        AlertDialog(
                            onDismissRequest = {
                                // Allow dismissing the dialog by clicking outside or pressing back
                                if (!isLoading) { // Don't allow dismiss if submission is in progress
                                    showConfirmationDialog = false
                                }
                            },
                            title = { Text("Confirm Submission") },
                            text = { Text("Are you sure you want to submit this settlement? This action cannot be undone.") },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        // Actual Submission Logic starts here
                                        showConfirmationDialog = false // Dismiss dialog first
                                        isLoading = true // Set loading state

                                        haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)

                                        // --- 1. Prepare Sales Details (Per-Product Breakdown) ---
                                        val salesDetailsMap = salesSummaries.map { (productType, summary) ->
                                            summary.productInfo.displayName to summary.totalAmount
                                        }.toMap()

                                        // --- 2. Prepare Credit Details (Breakdown by Creditee) ---
                                        val creditDetailsMap = creditEntries
                                            .groupBy { it.crediteeName.trim() }
                                            .mapValues { groupedEntry ->
                                                groupedEntry.value.sumOf { it.amount.toDouble() }.toFloat()
                                            }

                                        // --- 3. Determine the Cash Component for Settlement ---
                                        val cashComponentForSettlement = actualCashRemitted ?: expectedCashpayable

                                        // --- 4. Prepare Settlement Details Map ---
                                        val settlementDetailsMap = mutableMapOf<String, Float>()
                                        if (hpPayAmount > 0f) settlementDetailsMap["HP Pay/DT"] = hpPayAmount
                                        if (upiCardAmount > 0f) settlementDetailsMap["UPI/Card"] = upiCardAmount
                                        if (totalCreditAmount > 0f) settlementDetailsMap["Total Credits"] = totalCreditAmount
                                        settlementDetailsMap["Cash Remitted"] = cashComponentForSettlement

                                        // --- 5. Balance Short/Excess is already calculated in `balanceShortOrExcess` state ---

                                        // --- 6. Prepare Paytm Card Number ---
                                        val paytmCardNo = paytmCardNumberString.ifBlank { null }

                                        // --- 7. Prepare Closing Images URL ---
                                        val closingImages = if (closingImagePrimaryUrlString.isNotBlank()) {
                                            mapOf("primary_image" to closingImagePrimaryUrlString.trim())
                                        } else {
                                            null
                                        }

                                        Log.d("SettlementPage", "--- CONFIRMED: SUBMITTING FULL SETTLEMENT ---")
                                        Log.d("SettlementPage", "A. Overall Total Sales (Entry.total_sales): $overallTotalSale")
                                        Log.d("SettlementPage", "B. Sales Details Map (Entry.sales_details): $salesDetailsMap")
                                        Log.d("SettlementPage", "C. Settlement Details Map (Entry.settlement_details): $settlementDetailsMap")
                                        Log.d("SettlementPage", "D. Balance Short/Excess (Entry.balance_short): $balanceShortOrExcess")
                                        Log.d("SettlementPage", "E. Credit Details Map (Entry.credit_details): $creditDetailsMap")
                                        Log.d("SettlementPage", "F. Paytm Card No (Entry.paytm_card_no): $paytmCardNo")
                                        Log.d("SettlementPage", "G. Closing Images URL (Entry.closing_images_url): $closingImages")

                                        viewModel.updateSettlementData(
                                            totalSales = overallTotalSale,
                                            salesDetails = salesDetailsMap.ifEmpty { null },
                                            settlementDetails = settlementDetailsMap.ifEmpty { null },
                                            balanceShort = balanceShortOrExcess,
                                            creditDetails = creditDetailsMap.ifEmpty { null },
                                            paytmCardNo = paytmCardNo,
                                            // Ensure created_at is handled correctly in ViewModel or here if needed
                                            // If it's set by DB, no action needed for it here.
                                            // If client sets it, ensure it's up-to-date.
                                        )
                                        viewModel.submitNewEntry()
                                        // isLoading will be set to false in the LaunchedEffect(addEntryResult)
                                    },
                                    enabled = !isLoading // Disable confirm button if already loading
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                                    } else {
                                        Text("Confirm")
                                    }
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = {
                                        if (!isLoading) { // Don't allow dismiss if submission is in progress
                                            showConfirmationDialog = false
                                        }
                                    },
                                    enabled = !isLoading
                                ) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            val haptic = LocalHapticFeedback.current

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "Settlement",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.displaySmallEmphasized,
                    fontWeight = FontWeight.ExtraBold
                )

                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("", modifier = Modifier.weight(1f))
                    Text("Rate", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    Text("Qty", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    Text("Amount", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                }

                productDetails.forEach { prodDetail ->
                    val summary = salesSummaries[prodDetail.type]
                    if (summary != null && (summary.totalQuantity > 0 || summary.totalAmount > 0)) {
                        ProductSalesCard(
                            summary
                        )
                    }
                }

                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Total Sale:", modifier = Modifier.weight(3f), fontWeight = FontWeight.Bold)
                    Text(
                        String.format("%.2f", overallTotalSale),
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Text(
                    "Payments",
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp),
                    style = MaterialTheme.typography.headlineSmallEmphasized,
                    fontWeight = FontWeight.ExtraBold
                )
                OutlinedTextField(
                    value = hpPayAmountString,
                    onValueChange = { hpPayAmountString = it },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CurrencyRupee,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    },
                    label = { Text("HP Pay/DT") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                )
                OutlinedTextField(
                    value = upiCardAmountString,
                    onValueChange = { upiCardAmountString = it },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CurrencyRupee,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    },
                    label = { Text("UPI/Card") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                )

                Row(
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Credits",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.headlineSmallEmphasized,
                        fontWeight = FontWeight.ExtraBold
                    )

                    FilledTonalButton(
                        onClick = {
                            showBottomSheet = true
                        },
                        shapes = ButtonDefaults.shapes()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AddCircleOutline,
                            contentDescription = "Add"
                        )

                        Spacer(Modifier.width(4.dp))

                        Text("Add")
                    }
                }
                if (creditEntries.isEmpty()) {
                    Text(
                        "No credits added yet.",
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    creditEntries.forEach { credit ->
                        CreditListItem(
                            creditEntry = credit,
                            onRemove = {
                                creditEntries = creditEntries.filterNot { it.id == credit.id }
                            }
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        "Total Credits: ",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        String.format("₹%.2f", totalCreditAmount),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                // --- Cash Settlement Section ---
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        "Cash Settlement",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = actualCashRemittedString,
                        onValueChange = { actualCashRemittedString = it },
                        label = { Text("Actual Cash Remitted") },
                        shape = RoundedCornerShape(16.dp),
                        placeholder = { Text(String.format("%.2f", expectedCashpayable)) },
                        leadingIcon = { Icon(Icons.Default.CurrencyRupee, null, tint = MaterialTheme.colorScheme.tertiary) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 2.dp,
                                vertical = 4.dp
                            ), // Slight indent for clarity
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Expected Cash Payable:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            String.format("%.2f", expectedCashpayable),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val balanceLabel = when {
                            balanceShortOrExcess > 0f -> "Excess Cash:"
                            balanceShortOrExcess < 0f -> "Cash Shortage:"
                            else -> "Cash Balance:"
                        }
                        val balanceColor = when {
                            balanceShortOrExcess > 0f -> Color(0xFF388E3C) // Green
                            balanceShortOrExcess < 0f -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                        Text(
                            balanceLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = balanceColor
                        )
                        Text(
                            String.format("%.2f", abs(balanceShortOrExcess)),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = balanceColor
                        )
                    }
                    Spacer(Modifier.height(54.dp))
                }
            }

            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showBottomSheet = false
                    },
                    sheetState = sheetState,
                    dragHandle = {}
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .navigationBarsPadding(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Add New Credit",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = { showBottomSheet = false },
                                shapes = IconButtonDefaults.shapes(),
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Close"
                                )
                            }
                        }

                        OutlinedTextField(
                            value = crediteeNameInput,
                            onValueChange = { crediteeNameInput = it },
                            label = { Text("Creditee Name")},
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = creditAmountInput,
                            onValueChange = { creditAmountInput = it },
                            label = { Text("Credit Amount") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.CurrencyRupee,
                                    contentDescription = null
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(24.dp))

                        Button(
                            onClick = {
                                    val amount = creditAmountInput.toFloatOrNull()
                                    if (crediteeNameInput.isNotBlank() && amount != null && amount > 0) {
                                        creditEntries = creditEntries + CreditEntry(crediteeNameInput, amount)
                                        // Reset inputs for next time
                                        crediteeNameInput = ""
                                        creditAmountInput = ""
                                        scope.launch {
                                            sheetState.hide()// Call the suspending function
                                            // This code below will execute after hide() completes
                                            if (!sheetState.isVisible) { // Double-check if it's truly hidden
                                                showBottomSheet = false
                                            }
                                        }
                                    } else {
                                        // TODO: Show error/validation (e.g., Toast or message in bottom sheet)
                                        Log.w("SettlementPage", "Invalid credit input: Name='${crediteeNameInput}', Amount='${creditAmountInput}'")
                                    }
                                },
                            shapes = ButtonDefaults.shapes(),
                            modifier = Modifier
                                .height(54.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                "Add Credit",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun CreditListItem(creditEntry: CreditEntry, onRemove: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = creditEntry.crediteeName,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = String.format("₹%.2f", creditEntry.amount),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(onClick = onRemove) {
                Icon(Icons.Rounded.RemoveCircleOutline, contentDescription = "Remove Credit", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun ProductSalesCard(summary: ProductSalesSummary) {
    Card(
        modifier = Modifier.padding(horizontal = 8.dp),
        colors = CardDefaults.cardColors(containerColor = summary.productInfo.colorMatcher)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                summary.productInfo.displayName,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold
            )
            Text(
                String.format("%.2f", summary.productInfo.pricePerUnit),
                modifier = Modifier.weight(1f)
            )
            Text(
                String.format("%.2f", summary.totalQuantity),
                modifier = Modifier.weight(1f)
            )
            Text(
                String.format("%.2f", summary.totalAmount),
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//fun SettlementPreview() {
//    SettlementPage(onBackPress = {})
//}