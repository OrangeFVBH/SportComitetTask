package com.example.sportcomitettask

data class Section(
    val id: Int,
    val org_name: String,
    val leader: String,
    val address: String,
    val phone: String,
    val city: String,
    val sports: List<String>,
    val schedule: String
)
