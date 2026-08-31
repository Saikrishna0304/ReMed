package com.example.remed.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.remed.ui.AuthViewModel

@Composable
fun FamilySetupScreen(authViewModel: AuthViewModel) {
    var name by remember { mutableStateOf("") }
    var joinCode by remember { mutableStateOf("") }
    var familyName by remember { mutableStateOf("") }
    var isJoining by remember { mutableStateOf(false) }
    var showProfileSetup by remember { mutableStateOf(true) }

    val userProfile by authViewModel.userProfile.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (userProfile == null && showProfileSetup) {
            Text("Complete Your Profile", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Your Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { authViewModel.createProfile(name, "parent") },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()
            ) {
                Text("Continue")
            }
        } else {
            Text(
                text = if (isJoining) "Join a Family" else "Create a Family",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isJoining) "Enter the code shared by your family member" else "Set up a shared space for your household",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (isJoining) {
                OutlinedTextField(
                    value = joinCode,
                    onValueChange = { joinCode = it.uppercase() },
                    label = { Text("Join Code") },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                OutlinedTextField(
                    value = familyName,
                    onValueChange = { familyName = it },
                    label = { Text("Family Name") },
                    leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (isJoining) {
                        authViewModel.joinFamily(joinCode)
                    } else {
                        authViewModel.createFamily(familyName)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = if (isJoining) joinCode.length == 6 else familyName.isNotBlank()
            ) {
                Text(if (isJoining) "Join" else "Create")
            }

            TextButton(onClick = { isJoining = !isJoining }) {
                Text(if (isJoining) "Want to create a new family instead?" else "Have a join code? Join existing")
            }
        }
    }
}
