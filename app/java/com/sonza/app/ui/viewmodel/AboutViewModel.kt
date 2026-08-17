package com.sonza.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.sonza.app.data.SessionManager
import javax.inject.Inject

class AboutViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {
    // Developer mode related logic removed
}
