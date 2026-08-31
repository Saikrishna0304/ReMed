package com.example.remed.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val currentUser: FirebaseUser? get() = auth.currentUser

    suspend fun signInWithGoogle(idToken: String): FirebaseUser? {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return auth.signInWithCredential(credential).await().user
    }

    fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener {
            trySend(it.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun getUserProfile(uid: String): UserProfile? {
        return try {
            firestore.collection("users").document(uid).get().await().toObject(UserProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createUserProfile(profile: UserProfile) {
        firestore.collection("users").document(profile.uid).set(profile).await()
    }

    suspend fun getFamily(familyId: String): Family? {
        return try {
            firestore.collection("families").document(familyId).get().await().toObject(Family::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createFamily(family: Family) {
        firestore.collection("families").document(family.id).set(family).await()
        // Update user's familyId
        currentUser?.uid?.let { uid ->
            firestore.collection("users").document(uid).update("familyId", family.id).await()
        }
    }

    suspend fun joinFamily(joinCode: String) {
        val family = firestore.collection("families")
            .whereEqualTo("joinCode", joinCode)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.toObject(Family::class.java)

        if (family != null) {
            val uid = currentUser?.uid ?: return
            val updatedMembers = family.memberIds.toMutableList().apply { add(uid) }
            firestore.collection("families").document(family.id).update("memberIds", updatedMembers).await()
            firestore.collection("users").document(uid).update("familyId", family.id).await()
        }
    }
    
    fun signOut() {
        auth.signOut()
    }
}
