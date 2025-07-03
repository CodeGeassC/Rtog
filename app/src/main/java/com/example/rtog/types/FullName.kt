package com.example.rtog.types

data class FullName(
    val surname: String,
    val name: String,
    val patronymic: String?
) {
    val full: String get() = "$surname $name $patronymic"
    val short: String get() = if (patronymic != null) "$name ${patronymic.first()}. ${surname.first()}." else "$name ${surname.first()}."
}

