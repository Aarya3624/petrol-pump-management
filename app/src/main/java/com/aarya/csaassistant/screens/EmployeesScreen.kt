package com.aarya.csaassistant.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aarya.csaassistant.utils.Employee

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EmployeesScreen() {
    val employees = remember {
        listOf(
            Employee(name = "Aarya Sharma", role = "Manager"),
            Employee(name = "Rohan Verma", role = "Sales Associate"),
            Employee(name = "Priya Singh", role = "Cashier"),
            Employee(name = "Amit Patel", role = "Technician"),
            Employee(name = "Sunita Reddy", role = "Sales Associate"),
            Employee(name = "Vikram Kumar", role = "Support Staff")
        )
    }
    val totalEmployeeCount = employees.size

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
                    "Employees",
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    //TODO: Handle Add Employee Action
                },
                modifier = Modifier.padding(24.dp),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Icon(
                    Icons.Filled.PersonAdd,
                    "Add new employee"
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        if (employees.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No Employees Found")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(
                    items = employees,
                    key = { _, employee -> employee.id}
                ) { index, employee ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        onClick = {
                            //TODO: Handle employee item click
                            println("Clicked on ${employee.name}")
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )

                    ) {
                        ListItem(
                            headlineContent = { Text(
                                employee.name,
                                fontWeight = FontWeight.SemiBold
                            ) },
                            supportingContent = { Text(employee.role) },
                            leadingContent = {
                                Icon(
                                    Icons.Rounded.Person,
                                    contentDescription = "Employee Icon",
                                    tint = MaterialTheme.colorScheme.primary
                                )
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EmployeesScreenPreview() {
    EmployeesScreen()
}