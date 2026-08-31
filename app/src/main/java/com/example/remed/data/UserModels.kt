package com.example.remed.data

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val familyId: String? = null,
    val role: String = "member" // "parent" or "member"
)

data class Family(
    val id: String = "",
    val name: String = "",
    val adminId: String = "",
    val memberIds: List<String> = emptyList(),
    val joinCode: String = ""
)
