package com.example.remed.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.remed.data.BranchMemberData
import com.example.remed.data.UserProfile
import com.example.remed.ui.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun KeepTrackScreen(
    authViewModel: AuthViewModel
) {
    val family by authViewModel.family.collectAsState()
    val currentUserProfile by authViewModel.userProfile.collectAsState()
    val scope = rememberCoroutineScope()

    var memberProfiles by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var selectedMemberUid by remember { mutableStateOf<String?>(null) }
    var selectedMemberData by remember { mutableStateOf<BranchMemberData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isPromoting by remember { mutableStateOf(false) }

    val memberIds = family?.memberIds ?: emptyList()

    LaunchedEffect(family, currentUserProfile) {
        if (memberIds.isNotEmpty()) {
            isLoading = true
            val profiles = authViewModel.fetchFamilyMembersProfiles(memberIds)
            // Never include current logged-in parent in Keep Track monitoring list
            val others = profiles.filter { it.uid != currentUserProfile?.uid }
            memberProfiles = others
            if (memberProfiles.isNotEmpty()) {
                val firstUid = memberProfiles[0].uid
                selectedMemberUid = firstUid
                selectedMemberData = authViewModel.fetchBranchMemberData(firstUid)
            } else {
                selectedMemberUid = null
                selectedMemberData = null
            }
            isLoading = false
        } else {
            memberProfiles = emptyList()
            selectedMemberUid = null
            selectedMemberData = null
            isLoading = false
        }
    }

    LaunchedEffect(selectedMemberUid) {
        val uid = selectedMemberUid
        if (uid != null) {
            if (selectedMemberData == null || selectedMemberData?.profile?.uid != uid) {
                isLoading = true
                selectedMemberData = authViewModel.fetchBranchMemberData(uid)
                isLoading = false
            } else {
                selectedMemberData = authViewModel.fetchBranchMemberData(uid)
            }

            // Silent real-time auto-refresh every 5 seconds while viewing
            while (isActive) {
                delay(5000L)
                val freshData = authViewModel.fetchBranchMemberData(uid)
                selectedMemberData = freshData
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Text(
            text = "Family - Health Monitor",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Monitor prescriptions, hydration, and steps for family members",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (memberProfiles.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "No Family Members Yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Share your Family Join Code with others to connect them:\nJoin Code: ${family?.joinCode ?: "Generating..."}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            // Horizontal Member Selector
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                items(memberProfiles) { profile ->
                    val isSelected = profile.uid == selectedMemberUid
                    Surface(
                        onClick = { selectedMemberUid = profile.uid },
                        shape = MaterialTheme.shapes.medium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            ProfileAvatar(
                                photoUrl = profile.photoUrl,
                                size = 24.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = profile.name.ifBlank { "Family Member" },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                val data = selectedMemberData
                if (data != null) {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Member Header Card
                        item {
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            ProfileAvatar(
                                                photoUrl = data.profile.photoUrl,
                                                size = 44.dp
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = data.profile.name.ifBlank { "Family Member" },
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = if (data.profile.email.isNotBlank()) data.profile.email else "Family Member Account",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                val details = mutableListOf<String>()
                                                data.profile.gender?.let { if (it.isNotBlank()) details.add(it) }
                                                data.profile.age?.let { details.add("$it yrs") }
                                                data.profile.weightKg?.let { details.add("${it}kg") }
                                                data.profile.heightCm?.let { details.add("${it}cm") }
                                                data.profile.bmi?.let { details.add("BMI: ${String.format(Locale.getDefault(), "%.1f", it)} (${data.profile.bmiCategory})") }

                                                if (details.isNotEmpty()) {
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = details.joinToString(" • "),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }
                                        }

                                        val isMemberParent = data.profile.role == "parent" || (family != null && (family?.adminId == data.profile.uid || family?.parentIds?.contains(data.profile.uid) == true))
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isMemberParent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                                        ) {
                                            Text(
                                                text = if (isMemberParent) "Parent" else "Branch Member",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isMemberParent) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                    }

                                    val isMemberAdmin = data.profile.uid == family?.adminId
                                    val isMemberSelf = data.profile.uid == currentUserProfile?.uid
                                    val isMemberParent = data.profile.role == "parent" || (family != null && (family?.adminId == data.profile.uid || family?.parentIds?.contains(data.profile.uid) == true))

                                    if (!isMemberAdmin && !isMemberSelf) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        if (!isMemberParent) {
                                            Button(
                                                onClick = {
                                                    scope.launch {
                                                        isPromoting = true
                                                        val success = authViewModel.promoteToParent(data.profile.uid)
                                                        if (success) {
                                                            val updatedProfiles = authViewModel.fetchFamilyMembersProfiles(memberIds)
                                                            memberProfiles = updatedProfiles
                                                            selectedMemberData = authViewModel.fetchBranchMemberData(data.profile.uid)
                                                        }
                                                        isPromoting = false
                                                    }
                                                },
                                                enabled = !isPromoting,
                                                modifier = Modifier.fillMaxWidth(),
                                                contentPadding = PaddingValues(vertical = 6.dp)
                                            ) {
                                                if (isPromoting) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(16.dp),
                                                        color = MaterialTheme.colorScheme.onPrimary
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Promoting...")
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Default.Shield,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Promote to Parent Role", style = MaterialTheme.typography.labelMedium)
                                                }
                                            }
                                        } else {
                                            OutlinedButton(
                                                onClick = {
                                                    scope.launch {
                                                        isPromoting = true
                                                        val success = authViewModel.demoteToMember(data.profile.uid)
                                                        if (success) {
                                                            val updatedProfiles = authViewModel.fetchFamilyMembersProfiles(memberIds)
                                                            memberProfiles = updatedProfiles
                                                            selectedMemberData = authViewModel.fetchBranchMemberData(data.profile.uid)
                                                        }
                                                        isPromoting = false
                                                    }
                                                },
                                                enabled = !isPromoting,
                                                modifier = Modifier.fillMaxWidth(),
                                                contentPadding = PaddingValues(vertical = 6.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                            ) {
                                                if (isPromoting) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(16.dp),
                                                        color = MaterialTheme.colorScheme.error
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Demoting...")
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Default.RemoveCircleOutline,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Demote to Branch Member Role", style = MaterialTheme.typography.labelMedium)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Prescriptions Box Card (styled identically to Hydration & Steps cards)
                        item {
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.LocalPharmacy,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Prescriptions",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))

                                    if (data.medications.isEmpty()) {
                                        Text(
                                            text = "No prescription added by ${data.profile.name.ifBlank { "this member" }} today.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else {
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            data.medications.forEach { med ->
                                                val timeStr = remember(med.scheduledTime) {
                                                    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(med.scheduledTime))
                                                }
                                                Surface(
                                                    shape = MaterialTheme.shapes.small,
                                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(8.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                text = med.name,
                                                                style = MaterialTheme.typography.titleSmall,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                            Text(
                                                                text = "${med.dosage} • ${med.frequency}",
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Schedule,
                                                                    contentDescription = null,
                                                                    modifier = Modifier.size(12.dp),
                                                                    tint = MaterialTheme.colorScheme.primary
                                                                )
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text(
                                                                    text = "Alarm: $timeStr",
                                                                    style = MaterialTheme.typography.labelSmall,
                                                                    color = MaterialTheme.colorScheme.primary
                                                                )
                                                            }
                                                        }
                                                        Surface(
                                                            shape = CircleShape,
                                                            color = if (med.isTaken) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                                        ) {
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                            ) {
                                                                if (med.isTaken) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Check,
                                                                        contentDescription = null,
                                                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                                        modifier = Modifier.size(12.dp)
                                                                    )
                                                                    Spacer(modifier = Modifier.width(2.dp))
                                                                }
                                                                Text(
                                                                    text = if (med.isTaken) "Taken" else "Pending",
                                                                    style = MaterialTheme.typography.labelSmall,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = if (med.isTaken) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Hydration Monitor
                        item {
                            val waterProgress = (data.waterIntake / data.waterGoal.toFloat()).coerceIn(0f, 1f)
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.WaterDrop,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Hydration Today",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "${data.waterIntake} ml / ${data.waterGoal} ml",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { waterProgress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    )
                                }
                            }
                        }

                        // Step Counter Monitor
                        item {
                            val stepProgress = (data.stepCount / data.stepGoal.toFloat()).coerceIn(0f, 1f)
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Step Count Today",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "${data.stepCount} / ${data.stepGoal} steps",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { stepProgress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
