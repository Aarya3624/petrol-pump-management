package com.aarya.csaassistant.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aarya.csaassistant.R
import com.aarya.csaassistant.ui.theme.CSAAssistantTheme
import com.aarya.csaassistant.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    onShiftChange: () -> Unit,
    onSalesClick: () -> Unit,
    onEntriesClick: () -> Unit,
    onEmployeesClick: () -> Unit,
    onSettingsClick: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val options = listOf("Sales", "Entries", "Employees")
    val icons = listOf(Icons.Rounded.BarChart, Icons.Rounded.Checklist, Icons.Rounded.People)
    val actions = listOf(
        onSalesClick,
        onEntriesClick,
        onEmployeesClick
    )
    val buttonPadding = listOf(8.dp, 8.dp, 0.dp)
    val haptic = LocalHapticFeedback.current
    val userData by  authViewModel.userData.collectAsState()

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.petrolpump),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )
            }
            
            Column(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    "Welcome",
                    style = MaterialTheme.typography.displayMediumEmphasized,
                    modifier = Modifier.padding(top = 12.dp, start = 12.dp),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${userData?.full_name}!",
//                    "Shrikanth Rashinkar",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(horizontal = 12.dp),
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                ButtonGroup(
                    overflowIndicator = {},
                    expandedRatio = ButtonGroupDefaults.ExpandedRatio,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    content = {
                        options.forEachIndexed { index, label ->
                            clickableItem(
                                label = "",
                                icon = {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(
                                            top = 12.dp,
                                            bottom = 12.dp,
                                            start = buttonPadding[index]
                                        )
                                    ) {
                                        Icon(
                                            imageVector = icons[index],
                                            contentDescription = label,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Text(label, maxLines = 1)
                                    }
                                },
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                                    actions[index]()
                                },
                                weight = 1f
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilledIconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                            onSettingsClick()
                        },
                        shapes = IconButtonDefaults.shapes(),
                        modifier = Modifier
                            .height(58.dp)
                            .width(64.dp),
                        colors = IconButtonColors(
                            MaterialTheme.colorScheme.secondary,
                            MaterialTheme.colorScheme.onSecondary,
                            MaterialTheme.colorScheme.onSecondary,
                            MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    FilledIconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                            onShiftChange()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(58.dp),
                        shapes = IconButtonDefaults.shapes(),
                        colors = IconButtonColors(
                            MaterialTheme.colorScheme.tertiary,
                            MaterialTheme.colorScheme.onSecondary,
                            MaterialTheme.colorScheme.onSecondary,
                            MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Row {
                            Icon(
                                imageVector = Icons.Rounded.Repeat,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Change Shift")
                        }
                    }
                }
            }
        }
    }
    
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GreetingPreview() {
    CSAAssistantTheme {
        HomeScreen({}, onSalesClick = {}, onEntriesClick = {}, onEmployeesClick = {}, onSettingsClick = {})
    }
}