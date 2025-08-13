package com.aarya.csaassistant.screens


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.aarya.csaassistant.screens.utils.EntryImageCarousel
import com.aarya.csaassistant.screens.utils.FullScreenImageViewer
import com.aarya.csaassistant.utils.formatCurrency
import com.aarya.csaassistant.utils.nozzleProductColors
import com.aarya.csaassistant.utils.productDetails
import com.aarya.csaassistant.viewmodel.EntryViewModel
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.text.format
import kotlin.text.uppercase
import kotlin.time.ExperimentalTime
import kotlin.time.toJavaInstant


fun formatCurrency(amount: Float?, currencyCode: String = "INR"): String {
    if (amount == null) return "N/A"
    return try {
        val format = java.text.NumberFormat.getCurrencyInstance(Locale("en", "IN")) // For Indian Rupee format
        format.currency = java.util.Currency.getInstance(currencyCode)
        format.format(amount)
    } catch (e: Exception) {
        "₹${String.format("%.2f", amount)}" // Fallback
    }
}

fun String?.capitalizeFirstLetter(): String {
    return this?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } ?: ""
}

fun LocalDateTime?.formatToTimeOnly(): String {
    this ?: return "N/A"
    return try {
        val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
        this.atZone(ZoneId.systemDefault()).format(formatter)
    } catch (e: Exception) {
        "Invalid Time"
    }
}

fun LocalDateTime?.formatToDateOnly(): String {
    this ?: return "N/A"
    return try {
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        this.atZone(ZoneId.systemDefault()).format(formatter)
    } catch (e: Exception) {
        "Invalid Date"
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalTime::class
)
@Composable
fun EntryDetailsScreen(
    entryId: String,
    navController: NavController,
    entryViewModel: EntryViewModel = hiltViewModel()
) {
    val haptics = LocalHapticFeedback.current
    val entryDetails by entryViewModel.selectedEntryDetails.collectAsState()
    val isLoading by entryViewModel.isLoading.collectAsState()
    var selectedImageUrlForFullscreen by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(entryId) {
        entryViewModel.fetchEntryDetailsById(entryId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Entry Details",
                        style = MaterialTheme.typography.displaySmallEmphasized,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                            navController.popBackStack()
                        },
                        shapes = IconButtonDefaults.shapes(),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .width(34.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            null
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {},
                        shapes = IconButtonDefaults.shapes(),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Edit,
                            null
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (entryDetails == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Entry not found or error loading details.")
            }
        } else {
            val entry = entryDetails!!

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState()),
            ) {
                if (!entry.closing_images_url.isNullOrEmpty()) {
                    EntryImageCarousel(
                        imageUrls = entry.closing_images_url,
                        onImageClick = { imageUrl ->
                            selectedImageUrlForFullscreen = imageUrl
                        }
                    )
                    ListDivider()
                }

                selectedImageUrlForFullscreen?.let { imageUrl ->
                    FullScreenImageViewer(
                        imageUrl = imageUrl,
                        onDismissRequest = { selectedImageUrlForFullscreen = null }
                    )
                }

                SectionTitle("Shift Change Details")
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DetailItem(label = "Shift:", value = entry.shift ?: "N/A")
                    DetailItem(label = "Time:", value = entry.created_at?.toJavaInstant()?.let { javaInstant -> LocalDateTime.ofInstant(javaInstant, ZoneId.systemDefault()) }.formatToTimeOnly())
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    DetailItem(label = "Date:", value = entry.created_at?.toJavaInstant()?.let { javaInstant -> LocalDateTime.ofInstant(javaInstant, ZoneId.systemDefault()) }.formatToDateOnly())
                }
                Spacer(Modifier.height(4.dp))
                InfoRow(label = "From:", value = entry.handed_over_by, valueColor = MaterialTheme.colorScheme.primary)
                InfoRow(label = "To:", value = entry.handed_over_to ?: "N/A", valueColor = MaterialTheme.colorScheme.secondary)

                ListDivider()

                // --- Section: Nozzle Readings ---
                SectionTitle("Nozzle Readings")
                NozzleReadingHeader()

                val allNozzleIds = (entry.opening_readings?.keys ?: emptySet()) +
                        (entry.closing_readings?.keys ?: emptySet())
                if (allNozzleIds.isEmpty()) {
                    NoDataMessage("No nozzle readings available.")
                } else {
                    allNozzleIds.sorted().forEach { nozzleId ->
                        val opening = entry.opening_readings?.get(nozzleId) ?: 0f
                        val closing = entry.closing_readings?.get(nozzleId) ?: 0f
                        // Ensure closing is not less than opening for sales calculation
                        val salesVolume = if (closing >= opening) closing - opening else 0f
                        NozzleReadingCard(
                            nozzleId = nozzleId,
                            openingReading = opening,
                            closingReading = closing,
                            sales = salesVolume
                        )
                    }
                }

                ListDivider()

                // --- Section: Sales Details (Product Breakdown) ---
                SectionTitle("Sales Details")
                ProductSalesHeader()

                if (entry.sales_details.isNullOrEmpty()) {
                    NoDataMessage("No product sales details available.")
                } else {
                    // Calculate total liters sold per product type
                    val productLitersSold = mutableMapOf<String, Float>()
                    productDetails.forEach { prodInfo ->
                        var totalLitersForProduct = 0f
                        prodInfo.displayName.forEach { nozzleId ->
                            val opening = entry.opening_readings?.getOrElse(nozzleId.toString()) { 0f } ?: 0f
                            val closing = entry.closing_readings?.getOrElse(nozzleId.toString()) { 0f } ?: 0f
                            if (closing >= opening) {
                                totalLitersForProduct += (closing - opening)
                            }
                        }
                        productLitersSold[prodInfo.type.name.uppercase()] = totalLitersForProduct
                    }

                    entry.sales_details.forEach { (productKey, totalAmount) ->
                        val productInfo = productDetails.find { it.type.name.equals(productKey, ignoreCase = true) }
                        val displayName = productInfo?.displayName ?: productKey.capitalizeFirstLetter()
                        val color = productInfo?.colorMatcher ?: MaterialTheme.colorScheme.surfaceVariant
                        val quantity = productLitersSold[productKey.uppercase()] ?: 0f
                        val rate = if (quantity > 0f && totalAmount > 0f) totalAmount / quantity else 0f

                        ProductSalesCard(
                            productName = displayName,
                            rate = rate,
                            quantity = quantity,
                            amount = totalAmount,
                            cardColor = color
                        )
                    }
                }
                entry.total_sales?.let {
                    TotalRow(label = "Total Sales:", amount = it)
                }

                ListDivider()

                // --- Section: Settlement Details ---
                SectionTitle("Settlement Details")
                if (entry.settlement_details.isNullOrEmpty() && entry.balance_short == null) {
                    NoDataMessage("No settlement details available.")
                } else {
                    entry.settlement_details?.forEach { (item, value) ->
                        SettlementDetailRow(label = item.capitalizeFirstLetter(), amount = value)
                    }
                    entry.balance_short?.let {
                        val label = if (it > 0f) "Balance (Excess):" else "Balance (Short):"
                        val amount = if (it > 0f) -it else it // Show excess as positive
                        val color = if (it > 0f) Color(0xFF4CAF50) else if (it < 0f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        SettlementDetailRow(
                            label = label,
                            amount = amount,
                            isEmphasized = true,
                            amountColor = color
                        )
                    }
                }

                ListDivider()

                // --- Section: Credits ---
                SectionTitle("Credits")
                if (entry.credit_details.isNullOrEmpty()) {
                    NoDataMessage("No credits recorded.")
                } else {
                    entry.credit_details.forEach { (customer, amount) ->
                        CreditRow(customerName = customer.capitalizeFirstLetter(), amount = amount)
                    }
                }
                Spacer(Modifier.height(16.dp)) // Padding at the end
            }
        }
    }
}

// --- Helper Composables for Structure & Readability ---

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun DetailItem(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun InfoRow(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.primary) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 42.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = valueColor)
    }
}


@Composable
fun NozzleReadingHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Text("Nozzle", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.7f))
        Text("Opening", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
        Text("Closing", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
        Text("Sales (Ltr)", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
    }
}

@Composable
fun ProductSalesHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Text("Product", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.7f))
        Text("Rate", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
        Text("Qty (Ltr)", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
        Text("Amount", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
    }
}


@Composable
fun NoDataMessage(message: String) {
    Text(
        text = message,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun ListDivider() {
    Spacer(Modifier.height(8.dp))
    HorizontalDivider(Modifier.padding(horizontal = 24.dp))
    Spacer(Modifier.height(10.dp)) // Added little more space after divider
}

@Composable
fun TotalRow(label: String, amount: Float?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.End, // Align to end for totals
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            formatCurrency(amount),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

// --- Card Composables ---

@Composable
fun NozzleReadingCard(
    nozzleId: String,
    openingReading: Float,
    closingReading: Float,
    sales: Float
) {
    val cardColor = nozzleProductColors[nozzleId.uppercase()] ?: MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor.copy(alpha = 0.3f)) // Softer color
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(nozzleId, modifier = Modifier.weight(0.7f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(String.format("%.2f", openingReading), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.End)
            Text(String.format("%.2f", closingReading), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.End)
            Text(String.format("%.2f", sales), modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.End)
        }
    }
}

@Composable
fun ProductSalesCard(
    productName: String,
    rate: Float?,
    quantity: Float?,
    amount: Float?,
    cardColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor.copy(alpha = 0.3f)) // Softer color
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(productName, modifier = Modifier.weight(0.7f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(if (rate != null && rate > 0) String.format("%.2f", rate) else "-", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.End)
            Text(if (quantity != null && quantity > 0) String.format("%.2f", quantity) else "-", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.End)
            Text(formatCurrency(amount), modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.End)
        }
    }
}

@Composable
fun SettlementDetailRow(
    label: String,
    amount: Float?,
    isEmphasized: Boolean = false,
    amountColor: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 42.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = if (isEmphasized) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyLarge,
            fontWeight = if (isEmphasized) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            formatCurrency(amount),
            style = if (isEmphasized) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = amountColor
        )
    }
}

@Composable
fun CreditRow(customerName: String, amount: Float?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 42.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(customerName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        Text(
            formatCurrency(amount),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error // Or a distinct color for credits
        )
    }
}


@Preview(showSystemUi = true, showBackground = true)
@Composable
fun EntryDetailsPreview() {
    EntryDetailsScreen(entryId = "123", navController = NavController(LocalContext.current))
}
