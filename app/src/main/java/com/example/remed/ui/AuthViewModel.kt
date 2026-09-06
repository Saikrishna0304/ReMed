package com.example.remed.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remed.data.AuthRepository
import com.example.remed.data.BranchMemberData
import com.example.remed.data.Family
import com.example.remed.data.Medication
import com.example.remed.data.StepLog
import com.example.remed.data.StepSettings
import com.example.remed.data.UserProfile
import com.example.remed.data.WaterLog
import com.example.remed.data.WaterSettings
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AuthViewModel(
    private val repository: AuthRepository,
    context: Context
) : ViewModel() {

    companion object {
        const val GUEST_USER_ID = "GUEST_USER"
    }

    private val prefs = context.getSharedPreferences("remed_auth_prefs", Context.MODE_PRIVATE)

    val currentUser: StateFlow<FirebaseUser?> = repository.authStateFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.currentUser)

    private val _isGuestMode = MutableStateFlow(prefs.getBoolean("is_guest_mode", false))
    val isGuestMode: StateFlow<Boolean> = _isGuestMode.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(true)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(
        if (_isGuestMode.value) UserProfile(uid = GUEST_USER_ID, name = "Guest User") else null
    )
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _family = MutableStateFlow<Family?>(null)
    val family: StateFlow<Family?> = _family.asStateFlow()

    private val _databaseStatus = MutableStateFlow("Checking...")
    val databaseStatus: StateFlow<String> = _databaseStatus.asStateFlow()

    val userId: StateFlow<String?> = combine(currentUser, _isGuestMode) { user, guest ->
        user?.uid ?: if (guest) GUEST_USER_ID else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.currentUser?.uid)

    init {
        checkDatabaseConnection()
        viewModelScope.launch {
            currentUser.collect { user ->
                if (user != null) {
                    _isGuestMode.value = false
                    prefs.edit().putBoolean("is_guest_mode", false).apply()
                    val profile = repository.getUserProfile(user.uid)
                    _userProfile.value = profile
                    if (profile?.familyId != null) {
                        _family.value = repository.getFamily(profile.familyId)
                    } else {
                        _family.value = null
                    }
                } else if (_isGuestMode.value) {
                    _userProfile.value = UserProfile(uid = GUEST_USER_ID, name = "Guest User")
                    _family.value = null
                } else {
                    _userProfile.value = null
                    _family.value = null
                }
                _isAuthLoading.value = false
            }
        }
    }

    private fun checkDatabaseConnection() = viewModelScope.launch {
        try {
            FirebaseFirestore.getInstance()
                .collection("_connection_test_")
                .document("test")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _databaseStatus.value = "Connected"
                    } else {
                        _databaseStatus.value = "Offline: ${task.exception?.message}"
                    }
                }
        } catch (e: Exception) {
            _databaseStatus.value = "Error: ${e.message}"
        }
    }

    fun setGuestMode(enabled: Boolean) {
        _isGuestMode.value = enabled
        prefs.edit().putBoolean("is_guest_mode", enabled).apply()
        if (enabled) {
            _userProfile.value = UserProfile(uid = GUEST_USER_ID, name = "Guest User")
            _family.value = null
        }
    }

    fun createProfile(name: String, role: String) = viewModelScope.launch {
        currentUser.value?.let { user ->
            val profile = UserProfile(
                uid = user.uid,
                name = name,
                email = user.email ?: "",
                phoneNumber = user.phoneNumber ?: "",
                role = role
            )
            repository.createUserProfile(profile)
            _userProfile.value = profile
        }
    }

    fun updateProfile(
        name: String,
        photoUrl: String?,
        gender: String? = null,
        age: Int? = null,
        weightKg: Float? = null,
        heightCm: Float? = null
    ) = viewModelScope.launch {
        val current = _userProfile.value ?: UserProfile(uid = currentUser.value?.uid ?: GUEST_USER_ID)
        val updated = current.copy(
            name = name,
            photoUrl = photoUrl,
            gender = gender,
            age = age,
            weightKg = weightKg,
            heightCm = heightCm
        )
        _userProfile.value = updated

        if (!_isGuestMode.value && currentUser.value != null) {
            repository.updateUserProfile(updated)
        }
    }

    fun createFamily(familyName: String) = viewModelScope.launch {
        currentUser.value?.let { user ->
            val familyId = UUID.randomUUID().toString()
            val joinCode = UUID.randomUUID().toString().substring(0, 6).uppercase()
            val family = Family(
                id = familyId,
                name = familyName,
                adminId = user.uid,
                parentIds = listOf(user.uid),
                memberIds = listOf(user.uid),
                joinCode = joinCode
            )
            repository.createFamily(family)
            _family.value = family
            _userProfile.value = repository.getUserProfile(user.uid)
        }
    }

    suspend fun promoteToParent(memberUid: String): Boolean {
        val famId = _family.value?.id ?: return false
        val success = repository.promoteToParent(memberUid, famId)
        if (success) {
            _family.value = repository.getFamily(famId)
        }
        return success
    }

    suspend fun demoteToMember(memberUid: String): Boolean {
        val famId = _family.value?.id ?: return false
        val success = repository.demoteToMember(memberUid, famId)
        if (success) {
            _family.value = repository.getFamily(famId)
        }
        return success
    }

    fun joinFamily(joinCode: String) = viewModelScope.launch {
        repository.joinFamily(joinCode)
        currentUser.value?.let { user ->
            val profile = repository.getUserProfile(user.uid)
            _userProfile.value = profile
            if (profile?.familyId != null) {
                _family.value = repository.getFamily(profile.familyId)
            }
        }
    }

    suspend fun fetchBranchMemberData(memberUid: String): BranchMemberData {
        val firestore = FirebaseFirestore.getInstance()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        return try {
            val profileDoc = firestore.collection("users").document(memberUid).get().await()
            val profile = profileDoc.toObject(UserProfile::class.java) ?: UserProfile(uid = memberUid, name = "Branch Member")

            val prescsSnapshot = firestore.collection("users").document(memberUid).collection("prescriptions").get().await()
            val medications = prescsSnapshot.documents.mapNotNull { it.toObject(Medication::class.java) }

            val waterDoc = firestore.collection("users").document(memberUid).collection("water_logs").document(today).get().await()
            val waterLog = waterDoc.toObject(WaterLog::class.java)
            val waterSetDoc = firestore.collection("users").document(memberUid).collection("water_settings").document("settings").get().await()
            val waterGoal = waterSetDoc.toObject(WaterSettings::class.java)?.dailyGoal ?: 2000

            val stepDoc = firestore.collection("users").document(memberUid).collection("step_logs").document(today).get().await()
            val stepLog = stepDoc.toObject(StepLog::class.java)
            val stepSetDoc = firestore.collection("users").document(memberUid).collection("step_settings").document("settings").get().await()
            val stepGoal = stepSetDoc.toObject(StepSettings::class.java)?.dailyGoal ?: 5000

            BranchMemberData(
                profile = profile,
                medications = medications,
                waterIntake = waterLog?.amount ?: 0,
                waterGoal = waterGoal,
                stepCount = stepLog?.count ?: 0,
                stepGoal = stepGoal
            )
        } catch (_: Exception) {
            BranchMemberData(profile = UserProfile(uid = memberUid, name = "Branch Member"))
        }
    }

    suspend fun fetchFamilyMembersProfiles(memberIds: List<String>): List<UserProfile> {
        val firestore = FirebaseFirestore.getInstance()
        return memberIds.mapNotNull { uid ->
            try {
                firestore.collection("users").document(uid).get().await().toObject(UserProfile::class.java)
            } catch (_: Exception) {
                null
            }
        }
    }

    fun signInWithGoogle(idToken: String) = viewModelScope.launch {
        repository.signInWithGoogle(idToken)
    }

    fun signOut() {
        if (_isGuestMode.value) {
            _isGuestMode.value = false
            prefs.edit().putBoolean("is_guest_mode", false).apply()
        } else {
            repository.signOut()
        }
        _userProfile.value = null
        _family.value = null
    }
}
