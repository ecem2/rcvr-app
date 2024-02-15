package com.adentech.rcvr.onboard

import com.adentech.rcvr.viewmodel.BaseViewModel
import com.adentech.rcvr.preferences.Preferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardViewModel @Inject constructor(
    val preferences: Preferences
): BaseViewModel()