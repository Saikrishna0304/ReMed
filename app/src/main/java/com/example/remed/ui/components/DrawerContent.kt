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
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import com.example.remed.data.Family
import com.example.remed.data.UserProfile

@Composable
fun DrawerContent(
    selectedTab: String,
    userProfile: UserProfile?,
    family: Family?,
    isGuest: Boolean,
    onPrescriptionClick: () -> Unit,
    onHydrationClick: () -> Unit,
    onStepClick: () -> Unit,
    onSignOutClick: () -> Unit
) {
    ModalDrawerSheet {
        Spacer(Modifier.height(16.dp))
        
        // User Info Header
        NavigationDrawerItem(
            label = { 
                Text(
                    if (isGuest) "Offline Mode" else (userProfile?.name ?: "User"),
                    style = MaterialTheme.typography.titleMedium
                ) 
            },
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        
        if (isGuest) {
            Text(
                text = "Data is saved locally only",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(start = 28.dp, bottom = 16.dp)
            )
        } else if (family != null) {
            Text(
                text = "Family: ${family.name}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 28.dp, bottom = 8.dp)
            )
            Text(
                text = "Join Code: ${family.joinCode}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 28.dp, bottom = 16.dp)
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        NavigationDrawerItem(
            label = { Text("Prescriptions") },
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

        NavigationDrawerItem(
            label = { Text("Steps") },
            selected = selectedTab == "steps",
            onClick = onStepClick,
            icon = { Icon(Icons.Default.DirectionsWalk, contentDescription = null) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        Spacer(modifier = Modifier.weight(1f))
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        
        NavigationDrawerItem(
            label = { Text(if (isGuest) "Sign In to Backup" else "Sign Out") },
            selected = false,
            onClick = onSignOutClick,
            icon = { 
                Icon(
                    if (isGuest) Icons.Default.CloudUpload else Icons.Default.ExitToApp, 
                    contentDescription = null
                ) 
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        Spacer(Modifier.height(12.dp))
    }
}
