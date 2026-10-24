package com.serviseyem.ui.screens.urgent.components

import androidx.compose.foundation.layout.*
import com.serviseyem.ui.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.serviseyem.ui.screens.urgent.UrgentConstants

@Composable
fun UrgentFormFields(
    customerName: String,
    onCustomerNameChange: (String) -> Unit,
    customerPhone: String,
    onCustomerPhoneChange: (String) -> Unit,
    selectedCity: String,
    onCitySelected: (String) -> Unit,
    selectedArea: String,
    onAreaChange: (String) -> Unit
) {
    var expandedCityDropdown by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = customerName,
        onValueChange = onCustomerNameChange,
        label = { Text("الاسم") },
        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
        modifier = Modifier.fillMaxWidth().testTag("urgent_customer_name"),
        singleLine = true
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = customerPhone,
        onValueChange = onCustomerPhoneChange,
        label = { Text("رقم الهاتف (إجباري)*") },
        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        modifier = Modifier.fillMaxWidth().testTag("urgent_customer_phone"),
        singleLine = true
    )

    Spacer(modifier = Modifier.height(8.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = selectedCity,
                onValueChange = {},
                readOnly = true,
                label = { Text("المدينة*") },
                trailingIcon = {
                    IconButton(onClick = { expandedCityDropdown = true }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("urgent_city")
            )
            DropdownMenu(expanded = expandedCityDropdown, onDismissRequest = { expandedCityDropdown = false }) {
                UrgentConstants.cities.forEach { city ->
                    DropdownMenuItem(
                        text = { Text(city) },
                        onClick = {
                            onCitySelected(city)
                            expandedCityDropdown = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = selectedArea,
            onValueChange = onAreaChange,
            label = { Text("الحي / الشارع*") },
            modifier = Modifier.weight(1f).testTag("urgent_area"),
            singleLine = true
        )
    }
}
