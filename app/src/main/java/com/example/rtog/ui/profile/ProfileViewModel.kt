package com.example.rtog.ui.profile

import androidx.lifecycle.ViewModel
import com.example.rtog.types.FullName
import kotlinx.coroutines.flow.MutableStateFlow

class ProfileViewModel : ViewModel() {

    val rtogSessionToken = MutableStateFlow<String?>(null)
    val userFullName = MutableStateFlow<FullName?>(null)

}