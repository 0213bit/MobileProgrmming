package com.example.kickkick

import com.google.firebase.Timestamp

data class Comment(
    val uid: String = "",
    val nickname: String = "",
    val text: String = "",
    val timestamp: Timestamp? = null, // Firebase Timestamp 사용
    val myTeamLogo: String? = null
)