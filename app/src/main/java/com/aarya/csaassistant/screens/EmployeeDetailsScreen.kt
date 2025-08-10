package com.aarya.csaassistant.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.aarya.csaassistant.R
import com.aarya.csaassistant.model.Employee
import com.aarya.csaassistant.viewmodel.EmployeeViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EmployeeDetailsScreen(
    employeeId: String,
    navController: NavController,
    employeeViewModel: EmployeeViewModel = hiltViewModel()
) {
//    val employee = Employee(
//        full_name = "Aarya Rashinkar",
//        role = "Admin",
//        id = "id",
//        avatar_url = "https://lh3.googleusercontent.com/a/ACg8ocJKG1ww6JnpBAcYZdB7RxdICq2hoCcTK8w0MHh2w0TLQCBJIYZu9g=s96-c"
//    )

    val haptics = LocalHapticFeedback.current
    val employee by employeeViewModel.selectedEmployeeDetails.collectAsState()
    val isLoading by employeeViewModel.isLoading.collectAsState()
    val currentUserRole by employeeViewModel.currentUserRole.collectAsState()

    var employeeRole by remember { mutableStateOf<String?>(null) }
    var submitEnabled by remember { mutableStateOf<Boolean>(false) }
    var dropdownEnabled by remember { mutableStateOf<Boolean>(false) }
    var showDialog by remember { mutableStateOf(false) }

    var refreshTrigger by remember { mutableStateOf(false) }

    if (employeeRole != employee?.role) {
        submitEnabled = true
    } else {
        submitEnabled = false
    }

    LaunchedEffect(employeeId, refreshTrigger) {
        employeeViewModel.fetchEmployeeDetailsById(employeeId)
    }

    LaunchedEffect(employee, currentUserRole) {
        if (employee != null) {
            employeeRole = employee?.role
            if (currentUserRole == "Admin" || currentUserRole == "Owner") {
                dropdownEnabled = true
            } else {
                dropdownEnabled = false
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Save Changes?") },
            text = { Text("Are you sure you want to save the changes to the employee details?") },
            confirmButton = {
                Button(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                        if (employee != null && employeeRole != null) {
                            // Assuming changeUserRole is a suspend function or handles its own scope
                            employeeViewModel.changeUserRole(employeeId, employeeRole!!)
                            // After successfully changing the role, toggle the refreshTrigger
                            refreshTrigger = !refreshTrigger // This will cause LaunchedEffect to re-run
                        }
                        showDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        bottomBar = {
            Button(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                    showDialog = true
                },
                enabled = submitEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp)
                    .height(54.dp)
            ) {
                Text(
                    "Save",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        topBar = {
            TopAppBar(
                title = {
                },
                navigationIcon = {
                    IconButton(
                        onClick = {},
                        shapes = IconButtonDefaults.shapes(),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .width(32.dp)
                    ) {
                        Icon(
                            Icons.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val infiniteTransition = rememberInfiniteTransition()
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 50000, easing = LinearEasing)
                )
            )

            Box(
                modifier = Modifier
                    .size(140.dp)
                    .graphicsLayer {
                        rotationZ = rotation // Rotate the whole box
                    }
                    .clip(MaterialShapes.Cookie9Sided.toShape())
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(employee?.avatar_url)
                        .crossfade(true)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .build(),
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationZ = -rotation // Counter-rotate the image inside
                        }
                )
            }
            Text(
                employee?.full_name ?: "",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(16.dp))

            val roles = listOf("Admin", "Manager", "CSA Assistant", "Owner")

            RoleDropdown(
                roles = roles,
                selectedRole = employeeRole,
                onRoleSelected = { employeeRole = it },
                enabled = dropdownEnabled
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleDropdown(
    roles: List<String>,
    selectedRole: String?,
    enabled: Boolean = true,
    onRoleSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            readOnly = true,
            value = selectedRole.toString(),
            onValueChange = {},
            enabled = enabled,
            label = { Text("Role") },
            shape = RoundedCornerShape(16.dp),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {expanded = false},
        ) {
            roles.forEach { assignee ->
                DropdownMenuItem(
                    text = { Text(assignee) },
                    onClick = {
                        onRoleSelected(assignee)
                        expanded = false
                    }
                )
            }
        }
    }
}



@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EmployeeDetailsScreenPreview() {
//    EmployeeDetailsScreen()
}