package com.adentech.rcvr.ui.onboard

import com.adentech.rcvr.core.viewmodel.BaseViewModel
import com.adentech.rcvr.data.preferences.Preferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardViewModel @Inject constructor(
    val preferences: Preferences
): BaseViewModel()