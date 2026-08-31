package com.example.remed.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remed.data.AuthRepository
import com.example.remed.data.Family
import com.example.remed.data.UserProfile
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    companion object {
        const val GUEST_USER_ID = "GUEST_USER"
    }

    val currentUser: StateFlow<FirebaseUser?> = repository.authStateFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.currentUser)

    private val _isGuestMode = MutableStateFlow(false)
    val isGuestMode: StateFlow<Boolean> = _isGuestMode.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
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
                    val profile = repository.getUserProfile(user.uid)
                    _userProfile.value = profile
                    if (profile?.familyId != null) {
                        _family.value = repository.getFamily(profile.familyId)
                    } else {
                        _family.value = null
                    }
                } else {
                    if (!_isGuestMode.value) {
                        _userProfile.value = null
                        _family.value = null
                    }
                }
            }
        }
    }

    private fun checkDatabaseConnection() = viewModelScope.launch {
        try {
            // Try to fetch a non-existent document to check connectivity
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
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

    fun createFamily(familyName: String) = viewModelScope.launch {
        currentUser.value?.let { user ->
            val familyId = UUID.randomUUID().toString()
            val joinCode = UUID.randomUUID().toString().substring(0, 6).uppercase()
            val family = Family(
                id = familyId,
                name = familyName,
                adminId = user.uid,
                memberIds = listOf(user.uid),
                joinCode = joinCode
            )
            repository.createFamily(family)
            _family.value = family
            // Refresh profile to get familyId
            _userProfile.value = repository.getUserProfile(user.uid)
        }
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

    fun signInWithGoogle(idToken: String) = viewModelScope.launch {
        repository.signInWithGoogle(idToken)
    }

    fun signOut() {
        if (_isGuestMode.value) {
            _isGuestMode.value = false
        } else {
            repository.signOut()
        }
        _userProfile.value = null
        _family.value = null
    }
}
