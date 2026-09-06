package com.example.remed.data

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val familyId: String? = null,
    val role: String = "member", // "parent" or "member"
    val photoUrl: String? = null,
    val gender: String? = null,
    val age: Int? = null,
    val weightKg: Float? = null,
    val heightCm: Float? = null
) {
    val bmi: Float?
        get() {
            val h = heightCm ?: return null
            val w = weightKg ?: return null
            if (h <= 0f || w <= 0f) return null
            val heightMeters = h / 100f
            return w / (heightMeters * heightMeters)
        }

    val bmiCategory: String?
        get() {
            val score = bmi ?: return null
            return when {
                score < 18.5f -> "Underweight"
                score < 25.0f -> "Normal weight"
                score < 30.0f -> "Overweight"
                else -> "Obese"
            }
        }
}

data class Family(
    val id: String = "",
    val name: String = "",
    val adminId: String = "",
    val parentIds: List<String> = emptyList(),
    val memberIds: List<String> = emptyList(),
    val joinCode: String = ""
)

data class BranchMemberData(
    val profile: UserProfile,
    val medications: List<Medication> = emptyList(),
    val waterIntake: Int = 0,
    val waterGoal: Int = 2000,
    val stepCount: Int = 0,
    val stepGoal: Int = 5000
)
