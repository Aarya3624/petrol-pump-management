package com.aarya.csaassistant.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.aarya.csaassistant.model.Entry
import com.aarya.csaassistant.utils.Routes
import com.aarya.csaassistant.utils.formatDateHeader
import com.aarya.csaassistant.utils.formatNumberToShortCompactForm
import com.aarya.csaassistant.utils.getListBoundaryShape
import com.aarya.csaassistant.utils.toJavaLocalDate
import com.aarya.csaassistant.utils.toJavaLocalTime
import com.aarya.csaassistant.viewmodel.EntryViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.time.ExperimentalTime

data class SalesEntry(
    val id: String = UUID.randomUUID().toString(),
    val description: String,
    val amount: Double,
    val timestamp: LocalDateTime
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class,
    ExperimentalTime::class
)
@Composable
fun EntriesScreen(
    navController: NavController,
    entryViewModel: EntryViewModel = hiltViewModel()
) {
    val entries by entryViewModel.entries.collectAsState()
    val groupedEntries: Map<LocalDate, List<Entry>> = remember(entries) {
        entries
            .filter { it.created_at != null && it.id != null }
            .sortedByDescending { it.created_at }
            .groupBy { it.created_at!!.toJavaLocalDate() }
            .toSortedMap(compareByDescending { it })
    }
    
    val sampleSalesEntries = remember {
        listOf(
            SalesEntry(description = "Item A", amount = 10.0, timestamp = LocalDateTime.now()),
            SalesEntry(description = "Item B", amount = 15.5, timestamp = LocalDateTime.now().minusHours(2)),
            SalesEntry(description = "Item B", amount = 15.5, timestamp = LocalDateTime.now().minusHours(2)),
            SalesEntry(description = "Item B", amount = 15.5, timestamp = LocalDateTime.now().minusHours(2)),
            SalesEntry(description = "Item B", amount = 15.5, timestamp = LocalDateTime.now().minusHours(2)),
            SalesEntry(description = "Item C", amount = 22.0, timestamp = LocalDateTime.now().minusDays(1)),
            SalesEntry(description = "Item D", amount = 5.0, timestamp = LocalDateTime.now().minusDays(1).minusHours(3)),
            SalesEntry(description = "Item E", amount = 12.0, timestamp = LocalDateTime.now().minusDays(2)),
            SalesEntry(description = "Item F", amount = 7.5, timestamp = LocalDateTime.now().minusDays(2)),
            SalesEntry(description = "Item G", amount = 99.0, timestamp = LocalDateTime.now().minusDays(5)),
        )
    }

    // Group sales entries by date
    val groupedSales: Map<LocalDate, List<SalesEntry>> = remember(sampleSalesEntries) {
        sampleSalesEntries.groupBy { it.timestamp.toLocalDate() }
            .toSortedMap(compareByDescending { it }) // Sort by date, newest first
    }

    Scaffold(
        topBar = {
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        shape = RectangleShape,
                        color = MaterialTheme.colorScheme.background
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    "CSA Entries",
                    Modifier
                        .padding(top = 54.dp, start = 16.dp, bottom = 4.dp),
                    style = MaterialTheme.typography.displaySmallEmphasized,
                    fontWeight = FontWeight.ExtraBold
                )

                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = {},
                        modifier = Modifier
                            .width(34.dp),
                        shapes = IconButtonDefaults.shapes(),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search",
                        )
                    }
                    IconButton(
                        onClick = {},
                        modifier = Modifier
                            .width(42.dp),
                        shapes = IconButtonDefaults.shapes(),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Search",
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        if (groupedSales.isEmpty()) {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("No sales entries yet.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp), // Padding for the overall list
                // verticalArrangement = Arrangement.spacedBy(8.dp) // Spacing between cards can still be useful
            ) {
                groupedEntries.forEach { (date, entriesOnDate) ->
                    // Sticky Header for the Date
                    stickyHeader {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background) // Or surfaceVariant, etc.
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = formatDateHeader(date),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary, // Or onSurfaceVariant
                            )
                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.weight(1f))
                            // Optional: Horizontal line like in your example
                            // This might be better achieved by a full-width divider below or by styling the Text
                        }
                    }

                    // Items for that date
                    itemsIndexed(
                        items = entriesOnDate,
                        key = { _, entry -> entry.id as Any } // Stable key for each entry
                    ) { indexInGroup, entry ->
                        // Determine shape based on position within this date's group
                        val itemShape = getListBoundaryShape(
                            index = indexInGroup, // Index within the current date's list
                            totalItemCount = entriesOnDate.size, // Total items for this specific date
                            topCornerSize = if (indexInGroup == 0) 18.dp else 4.dp, // More rounded if it's the first under a header
                            bottomCornerSize = if (indexInGroup == entriesOnDate.size - 1) 18.dp else 4.dp, // More rounded if last under header
                            defaultCornerSize = 4.dp
                        )
                        // If you want the first item under a header to have top rounding,
                        // and the last to have bottom, and others to be flatter,
                        // the helper needs to be aware it's not the absolute first/last of the whole LazyColumn.
                        // For simplicity here, the provided getListBoundaryShape will round the
                        // first and last of *each sublist*.

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 2.dp) // Padding for each card
                                .clip(itemShape),
                            shape = itemShape,
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                        ) {
                            ListItem(
                                headlineContent = { Text(entry.handed_over_by as String, fontWeight = FontWeight.SemiBold) },
                                supportingContent = { Text("${entry.handed_over_to}") },
                                // You could add a trailingContent for time if needed
                                trailingContent = {
                                    entry.created_at?.let { ktInstant -> // ktInstant is kotlin.time.Instant
                                        // Convert kotlin.time.Instant to java.time.LocalTime
                                        val javaLocalTime = ktInstant.toJavaLocalTime()

                                        // Now format the java.time.LocalTime
                                        val formattedTime = javaLocalTime.format(DateTimeFormatter.ofPattern("h:mm a"))

                                        Text(
                                            text = formattedTime,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                },
                                leadingContent = {
                                    Text(
                                        "₹${formatNumberToShortCompactForm(entry.total_sales)}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                modifier = Modifier
                                    .clickable {
                                        navController.navigate("${Routes.ENTRY_DETAILS}/${entry.id}")
                                    },
                                colors = ListItemDefaults.colors(
                                    containerColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EntriesScreenPreview() {
//    EntriesScreen(navController = )
}