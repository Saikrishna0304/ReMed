package com.example.remed.ui.components

import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme

@Composable
fun DrawerContent(
    selectedTab: String,
    onPrescriptionClick: () -> Unit,
    onHydrationClick: () -> Unit
) {
    ModalDrawerSheet {
        Spacer(Modifier.height(12.dp))
        Text(
            "ReMed Menu",
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(24.dp))

        NavigationDrawerItem(
            label = { Text("Prescription") },
            selected = selectedTab == "prescription",
            onClick = onPrescriptionClick,
            icon = { Icon(Icons.Default.LocalPharmacy, contentDescription = null) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            label = { Text("Hydration") },
            selected = selectedTab == "hydration",
            onClick = onHydrationClick,
            icon = { Icon(Icons.Default.WaterDrop, contentDescription = null) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
    }
}
