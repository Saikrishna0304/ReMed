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

    fun userProfileFlow(uid: String): Flow<UserProfile?> = callbackFlow {
        val listener = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                val profile = snapshot?.toObject(UserProfile::class.java)
                trySend(profile)
            }
        awaitClose { listener.remove() }
    }

    fun familyFlow(familyId: String): Flow<Family?> = callbackFlow {
        val listener = firestore.collection("families").document(familyId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                val family = snapshot?.toObject(Family::class.java)
                trySend(family)
            }
        awaitClose { listener.remove() }
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

    suspend fun updateUserProfile(profile: UserProfile) {
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
        // Update user's familyId and role
        currentUser?.uid?.let { uid ->
            firestore.collection("users").document(uid).update(
                mapOf(
                    "familyId" to family.id,
                    "role" to "parent"
                )
            ).await()
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
            val updatedMembers = family.memberIds.toMutableList().apply { 
                if (!contains(uid)) add(uid) 
            }
            firestore.collection("families").document(family.id).update("memberIds", updatedMembers).await()
            firestore.collection("users").document(uid).update("familyId", family.id).await()
        }
    }

    suspend fun promoteToParent(memberUid: String, familyId: String): Boolean {
        return try {
            firestore.collection("users").document(memberUid).update("role", "parent").await()
            val familyDoc = firestore.collection("families").document(familyId).get().await()
            val family = familyDoc.toObject(Family::class.java)
            if (family != null) {
                val updatedParents = family.parentIds.toMutableList().apply {
                    if (!contains(memberUid)) add(memberUid)
                }
                firestore.collection("families").document(familyId).update("parentIds", updatedParents).await()
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun demoteToMember(memberUid: String, familyId: String): Boolean {
        return try {
            firestore.collection("users").document(memberUid).update("role", "member").await()
            val familyDoc = firestore.collection("families").document(familyId).get().await()
            val family = familyDoc.toObject(Family::class.java)
            if (family != null) {
                val updatedParents = family.parentIds.toMutableList().apply {
                    remove(memberUid)
                }
                firestore.collection("families").document(familyId).update("parentIds", updatedParents).await()
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
