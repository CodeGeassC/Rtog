package com.example.rtog.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "userData.getString(\"surname\")\n" +
                "userData.getString(\"name\")\n" +
                "userData.getString(\"patronymic\")"
    }
    val text: LiveData<String> = _text

    var registered : Boolean = false
}