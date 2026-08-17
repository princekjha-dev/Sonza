package com.sonza.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.sonza.app.ai.AIEqualizerService
import javax.inject.Inject

class AIEqualizerViewModel @Inject constructor(
    val aiService: AIEqualizerService
) : ViewModel()
