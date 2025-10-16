package com.example.topoclimb.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.topoclimb.data.Sector

/**
 * Sector selector UI component using dropdown menu
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectorSelector(
    sectors: List<Sector>,
    selectedSectorId: Int?,
    onSectorSelected: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    val selectedSector = sectors.find { it.id == selectedSectorId }
    val displayText = selectedSector?.name ?: "All Sectors"
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            label = { Text("Sector") },
            trailingIcon = {
                Row {
                    if (selectedSectorId != null) {
                        IconButton(
                            onClick = { 
                                onSectorSelected(null)
                                expanded = false
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear selection"
                            )
                        }
                    }
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            // "All Sectors" option
            DropdownMenuItem(
                text = { Text("All Sectors") },
                onClick = {
                    onSectorSelected(null)
                    expanded = false
                },
                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
            )
            
            // Individual sector options
            sectors.forEach { sector ->
                DropdownMenuItem(
                    text = { 
                        Column {
                            Text(sector.name)
                            sector.description?.let { desc ->
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    onClick = {
                        onSectorSelected(sector.id)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}
