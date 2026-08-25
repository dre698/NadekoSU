package com.nadekosu.domain.usecase

import com.nadekosu.data.AppSettingsRepository
import com.nadekosu.data.startup.ApplicationInitializationRepository
import com.nadekosu.data.startup.StartupRepository

class InitializeApplicationUseCase(
    private val settingsRepository: AppSettingsRepository,
    private val startupRepository: StartupRepository,
    private val initializationRepository: ApplicationInitializationRepository,
) {
    suspend operator fun invoke() {
        runCatching {
            settingsRepository.preload()
            initializationRepository.initialize()
        }.onSuccess {
            startupRepository.markReady()
        }.onFailure { error ->
            startupRepository.markFailed(error)
        }
    }
}
